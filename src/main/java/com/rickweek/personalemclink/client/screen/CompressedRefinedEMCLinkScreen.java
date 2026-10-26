package com.rickweek.personalemclink.client.screen;

import com.rickweek.personalemclink.common.menu.CompressedRefinedEMCLinkMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompressedRefinedEMCLinkScreen extends BaseEMCLinkScreen<CompressedRefinedEMCLinkMenu> {
    public CompressedRefinedEMCLinkScreen(CompressedRefinedEMCLinkMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, "compressed_refined_link", 176, 244);
    }
}
