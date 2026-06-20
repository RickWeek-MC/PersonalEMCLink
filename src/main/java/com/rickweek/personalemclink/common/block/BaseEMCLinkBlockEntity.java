package com.rickweek.personalemclink.common.block;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.emc.EMCMappingHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.Connection;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;

public abstract class BaseEMCLinkBlockEntity extends BlockEntity implements MenuProvider {
    private UUID ownerUUID;

    public final ItemStackHandler targetInventory;
    public final ItemStackHandler inputInventory;
    private final IItemHandler outputExtractionHandler;

    public BaseEMCLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int inputSize, int ghostSize) {
        super(type, pos, state);

        // "burning" items slots
        this.inputInventory = new ItemStackHandler(inputSize) {
            @Override
            protected void onContentsChanged(int slot) { setChanged(); }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(stack)) > 0;
            }

            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                if (!stack.isEmpty() && EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(stack)) <= 0) {
                    return;
                }
                super.setStackInSlot(slot, stack);
            }
        };

        // ghost item and output slots
        this.targetInventory = new ItemStackHandler(ghostSize) {
            @Override
            protected void onContentsChanged(int slot) { setChanged(); }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(stack)) > 0;
            }

            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                if (!stack.isEmpty() && EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(stack)) <= 0) {
                    return;
                }
                super.setStackInSlot(slot, stack);
            }
        };

        // extractionHandler
        this.outputExtractionHandler = new IItemHandler() {
            @Override
            public int getSlots() {
                return targetInventory.getSlots();
            }

            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                ItemStack ghostStack = targetInventory.getStackInSlot(slot);
                // if no UUID is available or the ghostStack is empty returns EMPTY
                if (ghostStack.isEmpty() || level == null || level.isClientSide() || level.getServer() == null || ownerUUID == null) {
                    return ItemStack.EMPTY;
                }

                // if the owner goes offline the iventory returns EMPTY
                Player player = level.getServer().getPlayerList().getPlayer(ownerUUID);
                if (!(player instanceof ServerPlayer serverPlayer)) {
                    return ItemStack.EMPTY;
                }

                // if the player has not the necessary EMC the inventory returns EMPTY to capabilities
                IKnowledgeProvider provider = serverPlayer.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
                if (provider == null) return ItemStack.EMPTY;

                long singleCost = EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(ghostStack));
                if (singleCost <= 0 || provider.getEmc().compareTo(BigInteger.valueOf(singleCost)) < 0) {
                    return ItemStack.EMPTY;
                }

                ItemStack fakeStack = ghostStack.copy();
                fakeStack.setCount(fakeStack.getMaxStackSize());
                return fakeStack;
            }

            // the block doesn't accept inputs from this handler
            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return stack;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack ghostStack = targetInventory.getStackInSlot(slot);
                if (ghostStack.isEmpty() || level == null || level.isClientSide() || level.getServer() == null || ownerUUID == null) return ItemStack.EMPTY;

                // if the player is offline this kills the extraction (no dupes)
                Player player = level.getServer().getPlayerList().getPlayer(ownerUUID);
                if (!(player instanceof ServerPlayer serverPlayer)) return ItemStack.EMPTY;

                IKnowledgeProvider provider = serverPlayer.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
                if (provider == null) return ItemStack.EMPTY;

                long singleCost = EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(ghostStack));
                if (singleCost <= 0) return ItemStack.EMPTY;

                BigInteger currentEMC = provider.getEmc();
                BigInteger costBig = BigInteger.valueOf(singleCost);

                BigInteger maxAffordableBig = currentEMC.divide(costBig);
                long maxAffordable = maxAffordableBig.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
                        ? Integer.MAX_VALUE : maxAffordableBig.longValue();

                if (maxAffordable <= 0) return ItemStack.EMPTY;

                int actualAmount = Math.min(amount, (int) maxAffordable);
                actualAmount = Math.min(actualAmount, ghostStack.getMaxStackSize());

                if (actualAmount <= 0) return ItemStack.EMPTY;

                if (!simulate) {
                    BigInteger totalCost = BigInteger.valueOf((long) actualAmount * singleCost);
                    provider.setEmc(currentEMC.subtract(totalCost));
                    provider.syncEmc(serverPlayer);
                    setChanged();
                }

                ItemStack extracted = ghostStack.copy();
                extracted.setCount(actualAmount);
                return extracted;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return false;
            }
        };
    }

    // get owner
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    // set owner
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("TargetInventory", targetInventory.serializeNBT(registries));
        tag.put("InputInventory", inputInventory.serializeNBT(registries));

        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("TargetInventory")) targetInventory.deserializeNBT(registries, tag.getCompound("TargetInventory"));
        if (tag.contains("InputInventory")) inputInventory.deserializeNBT(registries, tag.getCompound("InputInventory"));

        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide() || ownerUUID == null) return;

        boolean hasItems = false;
        for (int i = 0; i < inputInventory.getSlots(); i++) {
            if (!inputInventory.getStackInSlot(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }
        if (!hasItems) return;

        // if the player is offline this stops the tick
        Player player = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        IKnowledgeProvider provider = serverPlayer.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
        if (provider == null) return;

        BigInteger emcToAdd = BigInteger.ZERO;
        boolean hasBurnedSomething = false;

        for (int i = 0; i < inputInventory.getSlots(); i++) {
            ItemStack stack = inputInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                long itemValue = EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(stack));

                if (itemValue > 0) {
                    long totalStackValue = itemValue * stack.getCount();
                    emcToAdd = emcToAdd.add(BigInteger.valueOf(totalStackValue));

                    inputInventory.setStackInSlot(i, ItemStack.EMPTY);
                    hasBurnedSomething = true;
                }
            }
        }

        if (hasBurnedSomething && emcToAdd.compareTo(BigInteger.ZERO) > 0) {
            provider.setEmc(provider.getEmc().add(emcToAdd));
            provider.syncEmc(serverPlayer);
            setChanged();
        }
    }

    // the block accepts input only from the top
    public IItemHandler getItemHandler(Direction side) {
        if (side == Direction.UP) {
            return this.inputInventory;
        }
        return this.outputExtractionHandler;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        if (pkt.getTag() != null) {
            loadAdditional(pkt.getTag(), lookupProvider);
        }
    }

    @Override
    public abstract Component getDisplayName();

    @Nullable
    @Override
    public abstract AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player);
}