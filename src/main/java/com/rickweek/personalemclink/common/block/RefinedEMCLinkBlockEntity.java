package com.rickweek.personalemclink.common.block;

import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.menu.RefinedEMCLinkMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RefinedEMCLinkBlockEntity extends BaseEMCLinkBlockEntity {

    public RefinedEMCLinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.REFINED_EMC_LINK_BE.get(), pos, state, 1, 9);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Refined EMC Link");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RefinedEMCLinkMenu(containerId, playerInventory, this);
    }
}