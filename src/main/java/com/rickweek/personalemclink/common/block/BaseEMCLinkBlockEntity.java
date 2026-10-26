package com.rickweek.personalemclink.common.block;

import com.rickweek.personalemclink.common.util.EmcHelper;
import com.rickweek.personalemclink.common.util.EmcSyncScheduler;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;

public abstract class BaseEMCLinkBlockEntity extends BlockEntity implements MenuProvider {
    @Nullable
    private UUID ownerUUID;

    /** Ghost/filter slots: which items the link produces. Never contains real items. */
    public final ItemStackHandler targetInventory;
    /** Items here get burned into the owner's EMC every tick. */
    public final ItemStackHandler inputInventory;
    /** Exposed on every side except UP: lets pipes pull the ghost items, paid with the owner's EMC. */
    private final IItemHandler outputExtractionHandler;

    protected BaseEMCLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int inputSize, int ghostSize) {
        super(type, pos, state);

        this.inputInventory = new ItemStackHandler(inputSize) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return EmcHelper.canBurn(stack);
            }
            // NOTE: no setStackInSlot override anymore. Refusing a stack there silently deleted
            // the item when something called Slot.set() (sorters, other mods).
        };

        this.targetInventory = new ItemStackHandler(ghostSize) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return EmcHelper.canBuy(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        this.outputExtractionHandler = new OutputHandler();
    }

    // ---------------------------------------------------------------- owner

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    /** Only the owner (or an operator) can open the GUI. A link without owner gets claimed on first use. */
    public boolean canUse(Player player) {
        return ownerUUID == null || ownerUUID.equals(player.getUUID()) || player.hasPermissions(2);
    }

    /** The owner, only if online on this server. Offline owner = the link is paused. */
    @Nullable
    private ServerPlayer getOnlineOwner() {
        if (ownerUUID == null || level == null || level.isClientSide()) return null;
        MinecraftServer server = level.getServer();
        return server == null ? null : server.getPlayerList().getPlayer(ownerUUID);
    }

    // ---------------------------------------------------------------- logic

    /** Burns the input slots into the owner's EMC. Server side only (see BaseEMCLinkBlock#getTicker). */
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (ownerUUID == null || isInputEmpty()) return;

        ServerPlayer owner = getOnlineOwner();
        if (owner == null) return;
        IKnowledgeProvider provider = EmcHelper.getKnowledge(owner);
        if (provider == null) return;

        BigInteger gained = BigInteger.ZERO;
        for (int i = 0; i < inputInventory.getSlots(); i++) {
            ItemStack stack = inputInventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            long unitValue = EmcHelper.getSellValue(stack);
            if (unitValue <= 0) continue; // stays in the slot, can be taken back out

            gained = gained.add(EmcHelper.total(unitValue, stack.getCount()));
            inputInventory.setStackInSlot(i, ItemStack.EMPTY);
        }

        if (gained.signum() > 0) {
            provider.setEmc(provider.getEmc().add(gained));
            EmcSyncScheduler.markDirty(owner);
        }
    }

    private boolean isInputEmpty() {
        for (int i = 0; i < inputInventory.getSlots(); i++) {
            if (!inputInventory.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    /** Called when the block is broken: drops what's still in the input slots (e.g. owner was offline). */
    public void dropInputContents(Level level, BlockPos pos) {
        for (int i = 0; i < inputInventory.getSlots(); i++) {
            ItemStack stack = inputInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                inputInventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    /** Inputs only from the top, outputs from all the other sides. */
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return side == Direction.UP ? inputInventory : outputExtractionHandler;
    }

    /** Read-only view of the ghost slots: each one looks like a full stack, extraction costs the owner's EMC. */
    private class OutputHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return targetInventory.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            ItemStack ghost = targetInventory.getStackInSlot(slot);
            if (ghost.isEmpty()) return ItemStack.EMPTY;

            ServerPlayer owner = getOnlineOwner();
            if (owner == null) return ItemStack.EMPTY;
            IKnowledgeProvider provider = EmcHelper.getKnowledge(owner);
            if (provider == null) return ItemStack.EMPTY;

            int count = EmcHelper.affordable(provider.getEmc(), EmcHelper.getValue(ghost), ghost.getMaxStackSize());
            return count > 0 ? ghost.copyWithCount(count) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) return ItemStack.EMPTY;
            ItemStack ghost = targetInventory.getStackInSlot(slot);
            if (ghost.isEmpty()) return ItemStack.EMPTY;

            // offline owner = no extraction (no free items)
            ServerPlayer owner = getOnlineOwner();
            if (owner == null) return ItemStack.EMPTY;
            IKnowledgeProvider provider = EmcHelper.getKnowledge(owner);
            if (provider == null) return ItemStack.EMPTY;

            long unitCost = EmcHelper.getValue(ghost);
            BigInteger emc = provider.getEmc();
            int count = EmcHelper.affordable(emc, unitCost, Math.min(amount, ghost.getMaxStackSize()));
            if (count <= 0) return ItemStack.EMPTY;

            if (!simulate) {
                provider.setEmc(emc.subtract(EmcHelper.total(unitCost, count)));
                EmcSyncScheduler.markDirty(owner);
            }
            return ghost.copyWithCount(count);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }

    // ---------------------------------------------------------------- save / sync

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
        ownerUUID = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    // NOTE: no onDataPacket override: NeoForge's default already loads the tag,
    // the old override loaded it a second time.

    // ---------------------------------------------------------------- menu

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public abstract AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player);
}
