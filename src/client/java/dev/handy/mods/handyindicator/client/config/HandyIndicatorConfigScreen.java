package dev.handy.mods.handyindicator.client.config;

import dev.handy.mods.handyindicator.ContainerStateHelper;
import dev.handy.mods.handyindicator.HandyIndicator;
import dev.handy.mods.handyindicator.config.HandyIndicatorConfig;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HandyIndicatorConfigScreen {

    private static final String I18N_PREFIX = "config.handyindicator.";

    public static Screen makeScreen(Screen parent) {
        HandyIndicatorConfig defaults = new HandyIndicatorConfig();
        HandyIndicatorConfig config = HandyIndicatorConfig.get();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable(I18N_PREFIX + "title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable(I18N_PREFIX + "category.general"))
                        .option(booleanOption("enabled",
                                defaults.enabled, () -> config.enabled, v -> config.enabled = v))
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable(I18N_PREFIX + "indicatorColor"))
                                .description(OptionDescription.of(Component.translatable(I18N_PREFIX + "indicatorColor.desc")))
                                .binding(new Color(defaults.indicatorColor), () -> new Color(config.indicatorColor), v -> config.indicatorColor = v.getRGB() & HandyIndicator.RGB_MASK)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable(I18N_PREFIX + "fuelColor"))
                                .description(OptionDescription.of(Component.translatable(I18N_PREFIX + "fuelColor.desc")))
                                .binding(new Color(defaults.fuelColor), () -> new Color(config.fuelColor), v -> config.fuelColor = v.getRGB() & HandyIndicator.RGB_MASK)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable(I18N_PREFIX + "category.blocks"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable(I18N_PREFIX + "group.standard"))
                                .option(booleanOption("hopper",
                                        defaults.hopperEnabled, () -> config.hopperEnabled, v -> config.hopperEnabled = v))
                                .option(booleanOption("dispenser",
                                        defaults.dispenserEnabled, () -> config.dispenserEnabled, v -> config.dispenserEnabled = v))
                                .option(booleanOption("dropper",
                                        defaults.dropperEnabled, () -> config.dropperEnabled, v -> config.dropperEnabled = v))
                                .option(booleanOption("barrel",
                                        defaults.barrelEnabled, () -> config.barrelEnabled, v -> config.barrelEnabled = v))
                                .option(booleanOption("crafter",
                                        defaults.crafterEnabled, () -> config.crafterEnabled, v -> config.crafterEnabled = v))
                                .option(booleanOption("decoratedPot",
                                        defaults.decoratedPotEnabled, () -> config.decoratedPotEnabled, v -> config.decoratedPotEnabled = v))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable(I18N_PREFIX + "group.chests"))
                                .option(booleanOption("chest",
                                        defaults.chestEnabled, () -> config.chestEnabled, v -> config.chestEnabled = v))
                                .option(booleanOption("trappedChest",
                                        defaults.trappedChestEnabled, () -> config.trappedChestEnabled, v -> config.trappedChestEnabled = v))
                                .option(booleanOption("copperChest",
                                        defaults.copperChestEnabled, () -> config.copperChestEnabled, v -> config.copperChestEnabled = v))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable(I18N_PREFIX + "group.furnaces"))
                                .option(booleanOption("furnace",
                                        defaults.furnaceEnabled, () -> config.furnaceEnabled, v -> config.furnaceEnabled = v))
                                .option(booleanOption("blastFurnace",
                                        defaults.blastFurnaceEnabled, () -> config.blastFurnaceEnabled, v -> config.blastFurnaceEnabled = v))
                                .option(booleanOption("smoker",
                                        defaults.smokerEnabled, () -> config.smokerEnabled, v -> config.smokerEnabled = v))
                                .build())
                        .build())
                .save(() -> {
                    HandyIndicatorConfig.save();
                    Minecraft minecraft = Minecraft.getInstance();
                    // Force all chunks to re-render for color changes.
                    // 26.2 replaced LevelRenderer.allChanged() with invalidateCompiledGeometry();
                    // skip when not in a level (config may be edited from the title screen).
                    if (minecraft.level != null) {
                        minecraft.levelRenderer.invalidateCompiledGeometry(
                                minecraft.level,
                                minecraft.options,
                                minecraft.gameRenderer.mainCamera(),
                                minecraft.getBlockColors());
                    }
                    // Re-evaluate all container blockstates for toggle changes
                    MinecraftServer server = minecraft.getSingleplayerServer();
                    if (server != null) {
                        server.execute(() -> ContainerStateHelper.refreshAllContainers(server));
                    }
                })
                .build()
                .generateScreen(parent);
    }

    /**
     * Builds a tick-box {@link Option} for a boolean config field, sourcing both
     * name and (optional) description from the standard translation keys
     * {@code config.handyindicator.<key>} and {@code .desc}. The description key
     * is looked up only if the lang file defines it; otherwise the option renders
     * without a description popover.
     */
    private static Option<Boolean> booleanOption(String key,
                                                 boolean defaultValue,
                                                 Supplier<Boolean> getter,
                                                 Consumer<Boolean> setter) {
        Component name = Component.translatable(I18N_PREFIX + key);
        Option.Builder<Boolean> builder = Option.<Boolean>createBuilder()
                .name(name)
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create);
        // OptionDescription is always wired with the .desc translation key —
        // if the lang file doesn't have one, vanilla just shows the literal
        // key, but the per-block options here all read fine without a popover
        // anyway and we accept the trade-off for helper simplicity.
        builder.description(OptionDescription.of(
                Component.translatable(I18N_PREFIX + key + ".desc")));
        return builder.build();
    }
}
