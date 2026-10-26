package com.rickweek.personalemclink.common.menu;

import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.block.CompressedRefinedEMCLinkBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class CompressedRefinedEMCLinkMenu extends BaseEMCLinkMenu {

    public CompressedRefinedEMCLinkMenu(int containerId, Inventory playerInv, @Nullable BlockEntity entity) {
        super(ModRegistries.COMPRESSED_REFINED_EMC_LINK_MENU.get(), containerId, entity);

        addGhostGrid(ghostHandler(CompressedRefinedEMCLinkBlockEntity.GHOST_SLOTS), 8, 41, 9, 6);
        addInputGrid(inputHandler(CompressedRefinedEMCLinkBlockEntity.INPUT_SLOTS), 8, 17, 1, 1);
        addPlayerInventory(playerInv, 8, 162);
    }
}
