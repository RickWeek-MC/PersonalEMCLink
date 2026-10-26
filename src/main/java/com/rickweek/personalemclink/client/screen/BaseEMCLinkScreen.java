package com.rickweek.personalemclink.client.screen;

import com.rickweek.personalemclink.PersonalEMCLink;
import com.rickweek.personalemclink.common.menu.BaseEMCLinkMenu;
import com.rickweek.personalemclink.common.util.EmcHelper;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

public abstract class BaseEMCLinkScreen<T extends BaseEMCLinkMenu> extends AbstractContainerScreen<T> {
    private static final int LABEL_COLOR = 0x404040;
    private static final BigInteger ONE_MILLION = BigInteger.valueOf(1_000_000);
    private static final String[] SUFFIXES = {
            "", "Thousand", "Million", "Billion", "Trillion", "Quadrillion", "Quintillion",
            "Sextillion", "Septillion", "Octillion", "Nonillion", "Decillion"
    };

    private final ResourceLocation texture;

    protected BaseEMCLinkScreen(T menu, Inventory playerInv, Component title, String textureName, int width, int height) {
        super(menu, playerInv, title);
        this.texture = ResourceLocation.fromNamespaceAndPath(PersonalEMCLink.MODID, "textures/gui/" + textureName + ".png");
        this.imageWidth = width;
        this.imageHeight = height;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // super.render() already draws the background (Screen#render -> renderBackground -> renderBg).
        // The old extra renderBackground() call drew it twice per frame.
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** Title + the player's EMC in place of the "Inventory" label. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL_COLOR, false);

        if (minecraft != null && minecraft.player != null) {
            IKnowledgeProvider provider = EmcHelper.getKnowledge(minecraft.player);
            if (provider != null) {
                graphics.drawString(font, "EMC: " + formatCompactEMC(provider.getEmc()),
                        inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
            }
        }
    }

    /** 999,999 -> "999,999" ; 1,500,000 -> "1.50 Million" */
    static String formatCompactEMC(BigInteger emc) {
        if (emc.compareTo(ONE_MILLION) < 0) {
            return NumberFormat.getNumberInstance(Locale.US).format(emc);
        }
        String digits = emc.toString();
        int suffixIndex = Math.min((digits.length() - 1) / 3, SUFFIXES.length - 1);
        int wholeDigits = digits.length() - suffixIndex * 3;

        String whole = digits.substring(0, wholeDigits);
        String decimals = digits.substring(wholeDigits, Math.min(wholeDigits + 2, digits.length()));
        return whole + "." + decimals + " " + SUFFIXES[suffixIndex];
    }
}
