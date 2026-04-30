package dev.handy.mods.handyindicator.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.handy.mods.handyindicator.client.config.HandyIndicatorConfigScreen;
import net.fabricmc.loader.api.FabricLoader;

public class HandyIndicatorModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            return HandyIndicatorConfigScreen::makeScreen;
        }
        return parent -> null;
    }
}
