package com.rickweek.personalemclink;
 
import com.rickweek.personalemclink.client.screen.CompressedRefinedEMCLinkScreen;
import com.rickweek.personalemclink.client.screen.PersonalEMCLinkScreen;
import com.rickweek.personalemclink.client.screen.RefinedEMCLinkScreen;
import com.rickweek.personalemclink.common.ModRegistries;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = PersonalEMCLink.MODID, dist = Dist.CLIENT)
public class PersonalEMCLinkClient {
    public PersonalEMCLinkClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        container.getEventBus().addListener(PersonalEMCLinkClient::onClientSetup);
        container.getEventBus().addListener(PersonalEMCLinkClient::registerScreens);
    }

    static void onClientSetup(FMLClientSetupEvent event) {
        PersonalEMCLink.LOGGER.info("Hi from PersonalEMCLink clientSetup!");
    }

    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModRegistries.PERSONAL_EMC_LINK_MENU.get(), PersonalEMCLinkScreen::new);
        event.register(ModRegistries.REFINED_EMC_LINK_MENU.get(), RefinedEMCLinkScreen::new);
        event.register(ModRegistries.COMPRESSED_REFINED_EMC_LINK_MENU.get(), CompressedRefinedEMCLinkScreen::new);
    }
}
