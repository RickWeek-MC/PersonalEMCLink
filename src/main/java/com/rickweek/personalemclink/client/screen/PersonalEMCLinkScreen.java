package com.rickweek.personalemclink.client.screen;

import com.rickweek.personalemclink.common.menu.PersonalEMCLinkMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PersonalEMCLinkScreen extends BaseEMCLinkScreen<PersonalEMCLinkMenu> {
    public PersonalEMCLinkScreen(PersonalEMCLinkMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, "personal_link", 176, 166);
    }
}
