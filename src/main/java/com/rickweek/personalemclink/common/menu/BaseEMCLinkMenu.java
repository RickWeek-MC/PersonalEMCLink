package com.rickweek.personalemclink.common.menu;

import com.rickweek.personalemclink.common.block.BaseEMCLinkBlockEntity;
import com.rickweek.personalemclink.common.util.EmcHelper;
import com.rickweek.personalemclink.common.util.EmcSyncScheduler;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Ghost slot controls:
 * - click with an item on the cursor -> sets the ghost (the item is NOT consumed)
 * - left click with empty cursor     -> buys a stack (or as many as you can afford)
 * - right click with empty cursor    -> buys 1
 * - shift click                      -> clears the ghost
 */
public abstract class BaseEMCLinkMenu extends AbstractContainerMenu {
    /** Null only client side if the block entity wasn't there (chunk desync): the menu then just closes. */
    @Nullable
    public final BaseEMCLinkBlockEntity blockEntity;

    private int inputStart = -1, inputEnd = -1;
    private int playerStart = -1, playerEnd = -1;

    protected BaseEMCLinkMenu(MenuType<?> type, int containerId, @Nullable BlockEntity entity) {
        super(type, containerId);
        this.blockEntity = entity instanceof BaseEMCLinkBlockEntity link ? link : null;
    }

    // ---------------------------------------------------------------- layout helpers

    protected IItemHandler ghostHandler(int fallbackSize) {
        return blockEntity != null ? blockEntity.targetInventory : new ItemStackHandler(fallbackSize);
    }

    protected IItemHandler inputHandler(int fallbackSize) {
        return blockEntity != null ? blockEntity.inputInventory : new ItemStackHandler(fallbackSize);
    }

    protected void addGhostGrid(IItemHandler handler, int x, int y, int cols, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                addSlot(new GhostSlot(handler, col + row * cols, x + col * 18, y + row * 18));
            }
        }
    }

    protected void addInputGrid(IItemHandler handler, int x, int y, int cols, int rows) {
        inputStart = slots.size();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                addSlot(new SlotItemHandler(handler, col + row * cols, x + col * 18, y + row * 18));
            }
        }
        inputEnd = slots.size();
    }

    protected void addPlayerInventory(Inventory playerInventory, int x, int y) {
        playerStart = slots.size();
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInventory, col, x + col * 18, y + 58));
        }
        playerEnd = slots.size();
    }

    // ---------------------------------------------------------------- validity

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null || blockEntity.isRemoved() || blockEntity.getLevel() == null) return false;
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock())
                && blockEntity.canUse(player);
    }

    // ---------------------------------------------------------------- ghost slots

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < slots.size() && slots.get(slotId) instanceof GhostSlot ghost) {
            // ghost slots are 100% server-driven, the client just waits for the sync
            if (player instanceof ServerPlayer serverPlayer && blockEntity != null) {
                handleGhostClick(ghost, button, clickType, serverPlayer);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void handleGhostClick(GhostSlot ghost, int button, ClickType clickType, ServerPlayer player) {
        ItemStack carried = getCarried();

        if (clickType == ClickType.QUICK_MOVE) {
            ghost.set(ItemStack.EMPTY);
            return;
        }
        if (clickType != ClickType.PICKUP) return; // ignore swap, throw, clone, drag...

        if (!carried.isEmpty()) {
            if (EmcHelper.canBuy(carried)) {
                ghost.set(carried.copyWithCount(1));
            }
            return;
        }

        ItemStack target = ghost.getItem();
        if (!target.isEmpty()) {
            buy(player, target, button == 1 ? 1 : target.getMaxStackSize());
        }
    }

    /** Buys up to {@code wanted} items with the player's EMC; if not enough EMC, buys as many as possible. */
    private void buy(ServerPlayer player, ItemStack target, int wanted) {
        IKnowledgeProvider provider = EmcHelper.getKnowledge(player);
        if (provider == null) return;

        long unitCost = EmcHelper.getValue(target);
        int count = EmcHelper.affordable(provider.getEmc(), unitCost, wanted);
        if (count <= 0) return;

        provider.setEmc(provider.getEmc().subtract(EmcHelper.total(unitCost, count)));
        EmcSyncScheduler.markDirty(player);

        ItemStack bought = target.copyWithCount(count);
        player.getInventory().add(bought); // shrinks `bought` by what fit
        if (!bought.isEmpty()) {
            player.drop(bought, false);
        }
    }

    // vanilla features that iterate over all slots: keep them away from ghost slots

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof GhostSlot) && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return !(slot instanceof GhostSlot) && super.canDragTo(slot);
    }

    // ---------------------------------------------------------------- shift click

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot instanceof GhostSlot || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index >= playerStart && index < playerEnd) {
            // player -> input (only items that can be burned, checked by the slot's mayPlace)
            if (!EmcHelper.canBurn(stack) || !moveItemStackTo(stack, inputStart, inputEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= inputStart && index < inputEnd) {
            // input -> player
            if (!moveItemStackTo(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stack);
        return original;
    }
}
