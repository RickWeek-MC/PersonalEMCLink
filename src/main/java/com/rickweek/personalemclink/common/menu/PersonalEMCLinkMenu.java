package com.rickweek.personalemclink.common.menu;

import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.block.PersonalEMCLinkBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class PersonalEMCLinkMenu extends BaseEMCLinkMenu {

    public PersonalEMCLinkMenu(int containerId, Inventory playerInv, @Nullable BlockEntity entity) {
        super(ModRegistries.PERSONAL_EMC_LINK_MENU.get(), containerId, entity);

        addGhostGrid(ghostHandler(PersonalEMCLinkBlockEntity.GHOST_SLOTS), 152, 35, 1, 1);
        addInputGrid(inputHandler(PersonalEMCLinkBlockEntity.INPUT_SLOTS), 8, 17, 6, 3);
        addPlayerInventory(playerInv, 8, 84);
    }
}
