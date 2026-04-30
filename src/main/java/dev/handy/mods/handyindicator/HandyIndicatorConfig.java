package dev.handy.mods.handyindicator;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HandyIndicatorConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve(HandyIndicator.MOD_ID + ".json");
    // Legacy path from before the v2.1 mod-id rename (container-indicator → handyindicator).
    // Migrated once on first load so user settings carry over; safe to remove after a few releases.
    private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("container-indicator.json");

    private static HandyIndicatorConfig INSTANCE;

    @SerialEntry public boolean enabled = true;
    @SerialEntry public int indicatorColor = 0x6FA9B4;
    @SerialEntry public int fuelColor = 0xCF8261;
    @SerialEntry public int crafterReadyColor = 0x6FDE74;  // Green for "ready"
    @SerialEntry public boolean hopperEnabled = true;
    @SerialEntry public boolean dispenserEnabled = true;
    @SerialEntry public boolean dropperEnabled = true;
    @SerialEntry public boolean barrelEnabled = true;
    @SerialEntry public boolean crafterEnabled = true;
    @SerialEntry public boolean furnaceEnabled = true;
    @SerialEntry public boolean blastFurnaceEnabled = true;
    @SerialEntry public boolean smokerEnabled = true;
    @SerialEntry public boolean decoratedPotEnabled = true;
    @SerialEntry public boolean chestEnabled = true;
    @SerialEntry public boolean trappedChestEnabled = true;
    @SerialEntry public boolean copperChestEnabled = true;
    @SerialEntry public boolean shulkerBoxEnabled = true;

    public HandyIndicatorConfig() {}

    public static HandyIndicatorConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        migrateLegacyConfigIfNeeded();
        if (yaclLoaded()) {
            YaclStorage.HANDLER.load();
            INSTANCE = YaclStorage.HANDLER.instance();
        } else {
            // Without YACL, the config screen can't be opened anyway — run with defaults.
            // Any previously persisted config sits on disk and gets picked up the moment YACL is installed.
            INSTANCE = new HandyIndicatorConfig();
        }
    }

    public static void save() {
        if (yaclLoaded()) {
            YaclStorage.HANDLER.save();
        }
    }

    private static boolean yaclLoaded() {
        return FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3");
    }

    private static void migrateLegacyConfigIfNeeded() {
        if (!Files.exists(CONFIG_PATH) && Files.exists(LEGACY_CONFIG_PATH)) {
            try {
                Files.copy(LEGACY_CONFIG_PATH, CONFIG_PATH);
                HandyIndicator.LOGGER.info("[Handy Indicator] Migrated config from {} to {}",
                        LEGACY_CONFIG_PATH.getFileName(), CONFIG_PATH.getFileName());
            } catch (IOException e) {
                HandyIndicator.LOGGER.warn("[Handy Indicator] Failed to migrate legacy config from {}",
                        LEGACY_CONFIG_PATH.getFileName(), e);
            }
        }
    }

    public Color getIndicatorColorObj() {
        return new Color(indicatorColor);
    }

    public Color getFuelColorObj() {
        return new Color(fuelColor);
    }

    public Color getReadyColorObj() {
        return new Color(crafterReadyColor);
    }

    /**
     * Holds the YACL handler. Inner-class loading is lazy in the JVM, so this class is only
     * resolved when {@link #yaclLoaded()} is true and we actually reference it. That keeps
     * YACL classes off the always-executed code path and avoids a {@code NoClassDefFoundError}
     * for users (or dedicated servers) running without YACL installed.
     */
    private static final class YaclStorage {
        static final ConfigClassHandler<HandyIndicatorConfig> HANDLER =
                ConfigClassHandler.createBuilder(HandyIndicatorConfig.class)
                        .id(Identifier.fromNamespaceAndPath(HandyIndicator.MOD_ID, "config"))
                        .serializer(cfg -> GsonConfigSerializerBuilder.create(cfg)
                                .setPath(CONFIG_PATH)
                                .build())
                        .build();
    }
}
