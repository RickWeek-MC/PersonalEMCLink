package com.rickweek.personalemclink.common.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Filter slot: it only shows which item the link produces, it never holds a real item.
 * <p>
 * With a plain SlotItemHandler, other mods (Inventory Sorter, double-click pickup-all,
 * mouse tweaks...) treated the ghost as a real item and could pull it out = dupes / ghost items.
 * All clicks on this slot are handled manually in {@link BaseEMCLinkMenu#clicked}.
 */
public class GhostSlot extends SlotItemHandler {

    public GhostSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        // SlotItemHandler.remove() would extract the stored copy as a real item
        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean allowModification(Player player) {
        return false;
    }
}
