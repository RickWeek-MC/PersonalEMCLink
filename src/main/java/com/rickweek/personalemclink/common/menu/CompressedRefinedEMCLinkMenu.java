package com.rickweek.personalemclink.common.menu;

import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.block.BaseEMCLinkBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CompressedRefinedEMCLinkMenu extends BaseEMCLinkMenu{

    public CompressedRefinedEMCLinkMenu(int containerId, Inventory playerInv, BlockEntity entity) {
        super(ModRegistries.COMPRESSED_REFINED_EMC_LINK_MENU.get(), containerId, playerInv, (BaseEMCLinkBlockEntity) entity, 54);

        IItemHandler targetInv = blockEntity != null ? blockEntity.targetInventory : new ItemStackHandler(54);
        IItemHandler inputInv = blockEntity != null ? blockEntity.inputInventory : new ItemStackHandler(1);

        // more ghostSlots than inputs
        int ghostX = 8;
        int ghostY = 41;
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new SlotItemHandler(targetInv, j + i * 9, ghostX + j * 18, ghostY + i * 18));
            }
        }

        this.addSlot(new SlotItemHandler(inputInv, 0, 8, 17));
        addPlayerInventory(playerInv, 8, 162);
    }
}