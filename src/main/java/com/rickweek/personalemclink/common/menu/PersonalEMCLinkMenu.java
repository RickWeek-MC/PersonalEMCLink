package com.rickweek.personalemclink.common.menu;

import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.block.BaseEMCLinkBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PersonalEMCLinkMenu extends BaseEMCLinkMenu {

    public PersonalEMCLinkMenu(int containerId, Inventory playerInv, BlockEntity entity) {
        super(ModRegistries.PERSONAL_EMC_LINK_MENU.get(), containerId, playerInv, (BaseEMCLinkBlockEntity) entity, 1);

        IItemHandler targetInv = blockEntity != null ? blockEntity.targetInventory : new ItemStackHandler(1);
        IItemHandler inputInv = blockEntity != null ? blockEntity.inputInventory : new ItemStackHandler(18);

        // personal emc link has only 1 ghostSlot so it's registered first
        this.addSlot(new SlotItemHandler(targetInv, 0, 152, 35));
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 6; ++col) {
                this.addSlot(new SlotItemHandler(inputInv, col + row * 6, 8 + col * 18, 17 + row * 18));
            }
        }

        this.addPlayerInventory(playerInv, 8, 84);
    }
}