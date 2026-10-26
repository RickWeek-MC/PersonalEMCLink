package com.rickweek.personalemclink;

import com.mojang.logging.LogUtils;
import com.rickweek.personalemclink.common.ModRegistries;
import com.rickweek.personalemclink.common.util.EmcSyncScheduler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(PersonalEMCLink.MODID)
public class PersonalEMCLink {
    public static final String MODID = "personalemclink";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PersonalEMCLink(IEventBus modEventBus) {
        ModRegistries.register(modEventBus);
        modEventBus.addListener(PersonalEMCLink::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(EmcSyncScheduler::onServerTick);
        NeoForge.EVENT_BUS.addListener(EmcSyncScheduler::onServerStopped);
        // NOTE: never touch the global Log4j configuration here. Mod constructors run in parallel,
        // and doing it broke other mods' loggers (Inventory Sorter IllegalArgumentException).
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModRegistries.PERSONAL_EMC_LINK_BE.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModRegistries.REFINED_EMC_LINK_BE.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModRegistries.COMPRESSED_REFINED_EMC_LINK_BE.get(),
                (be, side) -> be.getItemHandler(side));
    }
}
