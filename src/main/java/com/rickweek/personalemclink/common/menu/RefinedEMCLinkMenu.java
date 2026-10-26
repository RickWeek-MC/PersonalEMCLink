package com.rickweek.personalemclink.common.menu;

import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.block.RefinedEMCLinkBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class RefinedEMCLinkMenu extends BaseEMCLinkMenu {

    public RefinedEMCLinkMenu(int containerId, Inventory playerInv, @Nullable BlockEntity entity) {
        super(ModRegistries.REFINED_EMC_LINK_MENU.get(), containerId, entity);

        addGhostGrid(ghostHandler(RefinedEMCLinkBlockEntity.GHOST_SLOTS), 89, 17, 3, 3);
        addInputGrid(inputHandler(RefinedEMCLinkBlockEntity.INPUT_SLOTS), 35, 35, 1, 1);
        addPlayerInventory(playerInv, 8, 84);
    }
}
