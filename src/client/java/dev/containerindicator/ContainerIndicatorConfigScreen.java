package dev.containerindicator;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.awt.Color;

public class ContainerIndicatorConfigScreen {

    static Screen makeScreen(Screen parent) {
        ContainerIndicatorConfig defaults = new ContainerIndicatorConfig();
        ContainerIndicatorConfig config = ContainerIndicatorConfig.instance();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Container Indicator"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Enabled"))
                                .description(OptionDescription.of(Component.literal("Master toggle for all container indicators")))
                                .binding(defaults.enabled, () -> config.enabled, v -> config.enabled = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Indicator Color"))
                                .description(OptionDescription.of(Component.literal("Color of the indicator on standard containers")))
                                .binding(defaults.getIndicatorColorObj(), config::getIndicatorColorObj, v -> config.indicatorColor = v.getRGB() & 0x00FFFFFF)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Fuel Indicator Color"))
                                .description(OptionDescription.of(Component.literal("Color of the fuel indicator on furnace-type blocks")))
                                .binding(defaults.getFuelColorObj(), config::getFuelColorObj, v -> config.fuelColor = v.getRGB() & 0x00FFFFFF)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Blocks"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Standard Containers"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Hopper"))
                                        .binding(defaults.hopperEnabled, () -> config.hopperEnabled, v -> config.hopperEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Dispenser"))
                                        .binding(defaults.dispenserEnabled, () -> config.dispenserEnabled, v -> config.dispenserEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Dropper"))
                                        .binding(defaults.dropperEnabled, () -> config.dropperEnabled, v -> config.dropperEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Barrel"))
                                        .binding(defaults.barrelEnabled, () -> config.barrelEnabled, v -> config.barrelEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Crafter"))
                                        .binding(defaults.crafterEnabled, () -> config.crafterEnabled, v -> config.crafterEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Decorated Pot"))
                                        .binding(defaults.decoratedPotEnabled, () -> config.decoratedPotEnabled, v -> config.decoratedPotEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Chests"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Chest"))
                                        .binding(defaults.chestEnabled, () -> config.chestEnabled, v -> config.chestEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Trapped Chest"))
                                        .binding(defaults.trappedChestEnabled, () -> config.trappedChestEnabled, v -> config.trappedChestEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Copper Chest"))
                                        .description(OptionDescription.of(Component.literal("Covers all copper chest variants (oxidized, waxed, etc.)")))
                                        .binding(defaults.copperChestEnabled, () -> config.copperChestEnabled, v -> config.copperChestEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Furnaces"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Furnace"))
                                        .binding(defaults.furnaceEnabled, () -> config.furnaceEnabled, v -> config.furnaceEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Blast Furnace"))
                                        .binding(defaults.blastFurnaceEnabled, () -> config.blastFurnaceEnabled, v -> config.blastFurnaceEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Smoker"))
                                        .binding(defaults.smokerEnabled, () -> config.smokerEnabled, v -> config.smokerEnabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .save(() -> {
                    ContainerIndicatorConfig.save();
                    Minecraft minecraft = Minecraft.getInstance();
                    // Force all chunks to re-render for color changes
                    minecraft.levelRenderer.allChanged();
                    // Re-evaluate all container blockstates for toggle changes
                    MinecraftServer server = minecraft.getSingleplayerServer();
                    if (server != null) {
                        server.execute(() -> ContainerStateHelper.refreshAllContainers(server));
                    }
                })
                .build()
                .generateScreen(parent);
    }
}
