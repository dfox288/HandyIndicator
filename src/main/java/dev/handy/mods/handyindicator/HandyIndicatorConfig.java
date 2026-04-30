package dev.handy.mods.handyindicator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class HandyIndicatorConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("handyindicator.json");
    // Legacy path from before the v2.1 mod-id rename (container-indicator → handyindicator).
    // Read once on first load so user settings carry over; safe to remove after a few releases.
    private static final Path LEGACY_CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("container-indicator.json");

    private static HandyIndicatorConfig instance = new HandyIndicatorConfig();

    public boolean enabled = true;
    public int indicatorColor = 0x6FA9B4;
    public int fuelColor = 0xCF8261;
    public boolean hopperEnabled = true;
    public boolean dispenserEnabled = true;
    public boolean dropperEnabled = true;
    public boolean barrelEnabled = true;
    public boolean crafterEnabled = true;
    public boolean furnaceEnabled = true;
    public boolean blastFurnaceEnabled = true;
    public boolean smokerEnabled = true;
    public boolean decoratedPotEnabled = true;
    public boolean chestEnabled = true;
    public boolean trappedChestEnabled = true;
    public boolean copperChestEnabled = true;
    public boolean shulkerBoxEnabled = true;
    public int crafterReadyColor = 0x6FDE74;  // Green for "ready"

    public static HandyIndicatorConfig instance() {
        return instance;
    }

    public static void load() {
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

        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(reader, HandyIndicatorConfig.class);
                if (instance == null) {
                    instance = new HandyIndicatorConfig();
                }
            } catch (IOException e) {
                HandyIndicator.LOGGER.error("[Handy Indicator] Failed to load config, using defaults", e);
                instance = new HandyIndicatorConfig();
            }
        } else {
            // No config on disk yet — write the defaults once so the user has
            // a file to edit. Existing files must NOT be rewritten here:
            // - It rewrites mtime every startup (annoying for users tracking
            //   the dotfile in git or watching it for hot-reload).
            // - It races with manual edits if the user is mid-save when the
            //   game starts.
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            HandyIndicator.LOGGER.error("[Handy Indicator] Failed to save config", e);
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
}
