package com.rickweek.personalemclink;

import com.rickweek.personalemclink.common.ModRegistries;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.StringMatchFilter;

@Mod(PersonalEMCLink.MODID)
public class PersonalEMCLink {
    public static final String MODID = "personalemclink";
    public static final Logger LOGGER = LogUtils.getLogger();


    public PersonalEMCLink(IEventBus modEventBus) {

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        ModRegistries.register(modEventBus);
        ModRegistries.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        silenceProjectESpam();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModRegistries.PERSONAL_EMC_LINK_BE.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModRegistries.REFINED_EMC_LINK_BE.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModRegistries.COMPRESSED_REFINED_EMC_LINK_BE.get(),
                (be, side) -> be.getItemHandler(side)
        );
    }

    // ProjectE has a default log event when the Core receives a change, so it's silenced to preserve log integrity.
    private void silenceProjectESpam() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        Filter filter = StringMatchFilter.newBuilder()
                .setMatchString("RECEIVED TRANSMUTATION EMC DATA CLIENTSIDE")
                .setOnMatch(Filter.Result.DENY)
                .setOnMismatch(Filter.Result.NEUTRAL)
                .build();
        config.addFilter(filter);
        ctx.updateLoggers();
    }
}
