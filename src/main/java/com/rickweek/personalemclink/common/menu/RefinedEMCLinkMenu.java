package com.rickweek.personalemclink.common.menu;

import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.block.BaseEMCLinkBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RefinedEMCLinkMenu extends BaseEMCLinkMenu {

    public RefinedEMCLinkMenu(int containerId, Inventory playerInv, BlockEntity entity) {
        super(ModRegistries.REFINED_EMC_LINK_MENU.get(), containerId, playerInv, (BaseEMCLinkBlockEntity) entity, 9);

        IItemHandler targetInv = blockEntity != null ? blockEntity.targetInventory : new ItemStackHandler(9);
        IItemHandler inputInv = blockEntity != null ? blockEntity.inputInventory : new ItemStackHandler(1);

        // more ghostSlots than inputs
        int ghostX = 89;
        int ghostY = 17;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new SlotItemHandler(targetInv, j + i * 3, ghostX + j * 18, ghostY + i * 18));
            }
        }

        this.addSlot(new SlotItemHandler(inputInv, 0, 35, 35));
        addPlayerInventory(playerInv, 8, 84);
    }
}