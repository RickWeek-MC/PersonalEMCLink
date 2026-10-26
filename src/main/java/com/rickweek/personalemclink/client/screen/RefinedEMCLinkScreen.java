package com.rickweek.personalemclink.client.screen;

import com.rickweek.personalemclink.common.menu.RefinedEMCLinkMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RefinedEMCLinkScreen extends BaseEMCLinkScreen<RefinedEMCLinkMenu> {
    public RefinedEMCLinkScreen(RefinedEMCLinkMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, "refined_link", 176, 166);
    }
}
