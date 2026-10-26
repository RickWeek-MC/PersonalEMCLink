package com.rickweek.personalemclink.common.util;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

/**
 * Single access point to ProjectE. Uses only the public API (IEMCProxy) instead of
 * ProjectE internals like EMCMappingHandler, which can change between versions.
 */
public final class EmcHelper {
    private EmcHelper() {}

    /** EMC needed to create one item (what the player pays). */
    public static long getValue(ItemStack stack) {
        return stack.isEmpty() ? 0 : IEMCProxy.INSTANCE.getValue(stack);
    }

    /**
     * EMC given when burning one item. Same value the Transmutation Table gives:
     * it respects durability (damaged tools are worth less) and the covalence loss config.
     */
    public static long getSellValue(ItemStack stack) {
        return stack.isEmpty() ? 0 : IEMCProxy.INSTANCE.getSellValue(stack);
    }

    public static boolean canBuy(ItemStack stack) {
        return getValue(stack) > 0;
    }

    public static boolean canBurn(ItemStack stack) {
        return getSellValue(stack) > 0;
    }

    @Nullable
    public static IKnowledgeProvider getKnowledge(Player player) {
        return player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
    }

    /** How many items costing {@code unitCost} each fit in {@code emc}, capped at {@code max}. */
    public static int affordable(BigInteger emc, long unitCost, int max) {
        if (unitCost <= 0 || max <= 0 || emc.signum() <= 0) return 0;
        BigInteger count = emc.divide(BigInteger.valueOf(unitCost));
        return count.min(BigInteger.valueOf(max)).intValue();
    }

    /** unitCost * count without long overflow. */
    public static BigInteger total(long unitCost, long count) {
        return BigInteger.valueOf(unitCost).multiply(BigInteger.valueOf(count));
    }
}
