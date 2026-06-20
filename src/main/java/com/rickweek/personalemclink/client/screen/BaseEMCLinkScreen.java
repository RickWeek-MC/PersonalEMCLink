package com.rickweek.personalemclink.client.screen;

import com.rickweek.personalemclink.common.menu.BaseEMCLinkMenu;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

public abstract class BaseEMCLinkScreen<T extends BaseEMCLinkMenu> extends AbstractContainerScreen<T> {
    public BaseEMCLinkScreen(T menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    // render the Name and EMC
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

        Player player = this.getMinecraft().player;
        if (player != null) {
            IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
            if (provider != null) {
                BigInteger emc = provider.getEmc();

                String formattedEMC = formatCompactEMC(emc);
                String emcText = "EMC: " + formattedEMC;

                guiGraphics.drawString(this.font, emcText, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
            }
        }
    }

    // EMC formatter
    private String formatCompactEMC(BigInteger emc) {
        if (emc.compareTo(BigInteger.valueOf(1000000)) < 0) {
            return NumberFormat.getNumberInstance(Locale.US).format(emc);
        }

        String[] suffixes = {"", "Thousands", "Millions", "Billions", "Trillions", "Quadrillions", "Quintillions", "Sextillions", "Septillions", "Octillions", "Nonillions", "Decillions"};

        String emcStr = emc.toString();
        int digits = emcStr.length();
        int suffixIndex = (digits - 1) / 3;

        if (suffixIndex >= suffixes.length) {
            suffixIndex = suffixes.length - 1;
        }

        int extraDigits = digits - (suffixIndex * 3);
        String wholePart = emcStr.substring(0, extraDigits);
        String decimalPart = emcStr.substring(extraDigits, Math.min(extraDigits + 2, emcStr.length()));

        return wholePart + "." + decimalPart + " " + suffixes[suffixIndex];
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}