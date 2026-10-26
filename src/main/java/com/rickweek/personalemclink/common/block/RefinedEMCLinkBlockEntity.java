package com.rickweek.personalemclink.common.block;

import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.menu.RefinedEMCLinkMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class RefinedEMCLinkBlockEntity extends BaseEMCLinkBlockEntity {
    public static final int INPUT_SLOTS = 1;
    public static final int GHOST_SLOTS = 9;

    public RefinedEMCLinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.REFINED_EMC_LINK_BE.get(), pos, state, INPUT_SLOTS, GHOST_SLOTS);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RefinedEMCLinkMenu(containerId, playerInventory, this);
    }
}
