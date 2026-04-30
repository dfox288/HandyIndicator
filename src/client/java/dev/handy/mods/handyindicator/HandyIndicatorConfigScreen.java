package dev.handy.mods.handyindicator;

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

    static Screen makeScreen(Screen parent) {
        HandyIndicatorConfig defaults = new HandyIndicatorConfig();
        HandyIndicatorConfig config = HandyIndicatorConfig.get();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Container Indicator"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .option(booleanOption(
                                Component.literal("Enabled"),
                                Component.literal("Master toggle for all container indicators"),
                                defaults.enabled, () -> config.enabled, v -> config.enabled = v))
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Indicator Color"))
                                .description(OptionDescription.of(Component.literal("Color of the indicator on standard containers")))
                                .binding(new Color(defaults.indicatorColor), () -> new Color(config.indicatorColor), v -> config.indicatorColor = v.getRGB() & 0x00FFFFFF)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Fuel Indicator Color"))
                                .description(OptionDescription.of(Component.literal("Color of the fuel indicator on furnace-type blocks")))
                                .binding(new Color(defaults.fuelColor), () -> new Color(config.fuelColor), v -> config.fuelColor = v.getRGB() & 0x00FFFFFF)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Blocks"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Standard Containers"))
                                .option(booleanOption(Component.literal("Hopper"), null,
                                        defaults.hopperEnabled, () -> config.hopperEnabled, v -> config.hopperEnabled = v))
                                .option(booleanOption(Component.literal("Dispenser"), null,
                                        defaults.dispenserEnabled, () -> config.dispenserEnabled, v -> config.dispenserEnabled = v))
                                .option(booleanOption(Component.literal("Dropper"), null,
                                        defaults.dropperEnabled, () -> config.dropperEnabled, v -> config.dropperEnabled = v))
                                .option(booleanOption(Component.literal("Barrel"), null,
                                        defaults.barrelEnabled, () -> config.barrelEnabled, v -> config.barrelEnabled = v))
                                .option(booleanOption(Component.literal("Crafter"), null,
                                        defaults.crafterEnabled, () -> config.crafterEnabled, v -> config.crafterEnabled = v))
                                .option(booleanOption(Component.literal("Decorated Pot"), null,
                                        defaults.decoratedPotEnabled, () -> config.decoratedPotEnabled, v -> config.decoratedPotEnabled = v))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Chests"))
                                .option(booleanOption(Component.literal("Chest"), null,
                                        defaults.chestEnabled, () -> config.chestEnabled, v -> config.chestEnabled = v))
                                .option(booleanOption(Component.literal("Trapped Chest"), null,
                                        defaults.trappedChestEnabled, () -> config.trappedChestEnabled, v -> config.trappedChestEnabled = v))
                                .option(booleanOption(
                                        Component.literal("Copper Chest"),
                                        Component.literal("Covers all copper chest variants (oxidized, waxed, etc.)"),
                                        defaults.copperChestEnabled, () -> config.copperChestEnabled, v -> config.copperChestEnabled = v))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Furnaces"))
                                .option(booleanOption(Component.literal("Furnace"), null,
                                        defaults.furnaceEnabled, () -> config.furnaceEnabled, v -> config.furnaceEnabled = v))
                                .option(booleanOption(Component.literal("Blast Furnace"), null,
                                        defaults.blastFurnaceEnabled, () -> config.blastFurnaceEnabled, v -> config.blastFurnaceEnabled = v))
                                .option(booleanOption(Component.literal("Smoker"), null,
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
     * Builds a tick-box {@link Option} for a boolean config field. {@code description}
     * may be {@code null}; most per-block toggles are self-explanatory and don't need
     * a description popover.
     *
     * <p>Takes a {@link Component} for the name (rather than a translation key) so the
     * helper supports both literal and translatable strings without changing signature
     * — the i18n migration in #14 just swaps {@code Component.literal} for
     * {@code Component.translatable} at the call sites.
     */
    private static Option<Boolean> booleanOption(Component name, Component description,
                                                 boolean defaultValue,
                                                 Supplier<Boolean> getter,
                                                 Consumer<Boolean> setter) {
        Option.Builder<Boolean> builder = Option.<Boolean>createBuilder()
                .name(name)
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create);
        if (description != null) {
            builder.description(OptionDescription.of(description));
        }
        return builder.build();
    }
}
