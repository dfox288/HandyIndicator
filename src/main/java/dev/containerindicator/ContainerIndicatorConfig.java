package dev.containerindicator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContainerIndicatorConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("container-indicator.json");

    private static ContainerIndicatorConfig instance = new ContainerIndicatorConfig();

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

    public static ContainerIndicatorConfig instance() {
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(reader, ContainerIndicatorConfig.class);
                if (instance == null) {
                    instance = new ContainerIndicatorConfig();
                }
            } catch (IOException e) {
                ContainerIndicator.LOGGER.error("[Container Indicator] Failed to load config, using defaults", e);
                instance = new ContainerIndicatorConfig();
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            ContainerIndicator.LOGGER.error("[Container Indicator] Failed to save config", e);
        }
    }

    public Color getIndicatorColorObj() {
        return new Color(indicatorColor);
    }

    public Color getFuelColorObj() {
        return new Color(fuelColor);
    }
}
