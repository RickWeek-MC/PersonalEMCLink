package com.rickweek.personalemclink;

import com.rickweek.personalemclink.client.screen.CompressedRefinedEMCLinkScreen;
import com.rickweek.personalemclink.client.screen.PersonalEMCLinkScreen;
import com.rickweek.personalemclink.client.screen.RefinedEMCLinkScreen;
import com.rickweek.personalemclink.common.ModRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.filter.StringMatchFilter;

@Mod(value = PersonalEMCLink.MODID, dist = Dist.CLIENT)
public class PersonalEMCLinkClient {

    public PersonalEMCLinkClient(ModContainer container) {
        // (removed the ConfigurationScreen registration: the mod has no config, it opened an empty screen)
        container.getEventBus().addListener(PersonalEMCLinkClient::onClientSetup);
        container.getEventBus().addListener(PersonalEMCLinkClient::registerScreens);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // enqueueWork = main thread, after the parallel mod loading phase
        event.enqueueWork(PersonalEMCLinkClient::silenceProjectESpam);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModRegistries.PERSONAL_EMC_LINK_MENU.get(), PersonalEMCLinkScreen::new);
        event.register(ModRegistries.REFINED_EMC_LINK_MENU.get(), RefinedEMCLinkScreen::new);
        event.register(ModRegistries.COMPRESSED_REFINED_EMC_LINK_MENU.get(), CompressedRefinedEMCLinkScreen::new);
    }

    /**
     * ProjectE logs a line on every client EMC sync. Syncs are now batched (EmcSyncScheduler),
     * so this is just a safety net. The filter goes on the root logger only: no changes to the
     * global Log4j Configuration, no updateLoggers(), other mods' loggers are left alone.
     */
    private static void silenceProjectESpam() {
        try {
            if (!(LogManager.getRootLogger() instanceof org.apache.logging.log4j.core.Logger root)) return;
            Filter filter = StringMatchFilter.newBuilder()
                    .setMatchString("RECEIVED TRANSMUTATION EMC DATA CLIENTSIDE")
                    .setOnMatch(Filter.Result.DENY)
                    .setOnMismatch(Filter.Result.NEUTRAL)
                    .build();
            filter.start();
            root.addFilter(filter);
        } catch (Throwable t) {
            // a cosmetic log filter must never break the game
            PersonalEMCLink.LOGGER.warn("Could not install ProjectE log filter, skipping", t);
        }
    }
}
