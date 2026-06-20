package com.rickweek.personalemclink.client.screen;

import com.rickweek.personalemclink.common.menu.RefinedEMCLinkMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RefinedEMCLinkScreen extends BaseEMCLinkScreen<RefinedEMCLinkMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("personalemclink", "textures/gui/refined_link.png");

    public RefinedEMCLinkScreen(RefinedEMCLinkMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;

        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
}