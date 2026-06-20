package com.rickweek.personalemclink.common.menu;

import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.block.BaseEMCLinkBlockEntity;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.emc.EMCMappingHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.math.BigInteger;

public abstract class BaseEMCLinkMenu extends AbstractContainerMenu {
    public final BaseEMCLinkBlockEntity blockEntity;
    private final int ghostSlotCount;

    public BaseEMCLinkMenu(MenuType<?> type, int containerId, Inventory playerInv, BaseEMCLinkBlockEntity entity, int ghostSlotCount) {
        super(type, containerId);
        this.blockEntity = entity;
        this.ghostSlotCount = ghostSlotCount;
    }

    protected void addPlayerInventory(Inventory playerInventory, int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, x + k * 18, y + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.blockEntity.getLevel() == null) return false;

        return stillValid(
                ContainerLevelAccess.create(this.blockEntity.getLevel(), this.blockEntity.getBlockPos()),
                player,
                this.blockEntity.getBlockState().getBlock()
        );
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId < 0 || slotId >= this.slots.size()) {
            super.clicked(slotId, button, clickType, player);
            return;
        }

        if (slotId < ghostSlotCount) {
            if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                ItemStack carried = this.getCarried();
                ItemStack ghostStack = this.slots.get(slotId).getItem();

                if (clickType == ClickType.QUICK_MOVE) {
                    blockEntity.targetInventory.setStackInSlot(slotId, ItemStack.EMPTY);
                    return;
                }

                if (!carried.isEmpty()) {
                    long emcValue = EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(carried));
                    if (emcValue > 0) {
                        ItemStack copy = carried.copy();
                        copy.setCount(1);
                        blockEntity.targetInventory.setStackInSlot(slotId, copy);
                    }
                    return;
                }

                if (carried.isEmpty() && !ghostStack.isEmpty() && clickType == ClickType.PICKUP) {
                    IKnowledgeProvider provider = serverPlayer.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
                    if (provider != null) {
                        long singleCostValue = EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(ghostStack));
                        if (singleCostValue > 0) {
                            int amountToBuy = (button == 1) ? 1 : ghostStack.getMaxStackSize();
                            BigInteger totalCost = BigInteger.valueOf(singleCostValue * amountToBuy);
                            BigInteger currentEMC = provider.getEmc();

                            if (currentEMC.compareTo(totalCost) >= 0) {
                                provider.setEmc(currentEMC.subtract(totalCost));
                                provider.syncEmc(serverPlayer);

                                ItemStack boughtStack = ghostStack.copy();
                                boughtStack.setCount(amountToBuy);

                                boolean added = serverPlayer.getInventory().add(boughtStack);
                                if (!added || !boughtStack.isEmpty()) {
                                    serverPlayer.drop(boughtStack, false);
                                }
                            }
                        }
                    }
                    return;
                }
            }
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copiedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            copiedStack = stackInSlot.copy();

            int inputStart = -1, inputEnd = -1;
            int playerStart = -1;

            for (int i = 0; i < this.slots.size(); i++) {
                Slot s = this.slots.get(i);
                if (s instanceof SlotItemHandler sih && sih.getItemHandler() == this.blockEntity.inputInventory) {
                    if (inputStart == -1) inputStart = i;
                    inputEnd = i + 1;
                }
                else if (s.container == player.getInventory()) {
                    if (playerStart == -1) playerStart = i;
                }
            }

            if (inputStart == -1 || playerStart == -1) return ItemStack.EMPTY;

            if (index >= playerStart) {
                long emcValue = EMCMappingHandler.getStoredEmcValue(ItemInfo.fromStack(stackInSlot));
                if (emcValue > 0) {
                    if (!this.moveItemStackTo(stackInSlot, inputStart, inputEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= inputStart && index < inputEnd) {
                if (!this.moveItemStackTo(stackInSlot, playerStart, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == copiedStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stackInSlot);
        }

        return copiedStack;
    }
}