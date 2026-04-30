package dev.handy.mods.handyindicator;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class HandyIndicatorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Tint sources: indicator (index 0), fuel (index 1), ready (index 2)
        BlockTintSource indicatorTint = (state) -> HandyIndicator.getIndicatorColor();
        BlockTintSource fuelTint = (state) -> HandyIndicator.getFuelColor();
        BlockTintSource readyTint = (state) -> HandyIndicator.getReadyColor();
        List<BlockTintSource> tintSources = List.of(indicatorTint, fuelTint, readyTint);

        // Register color providers for any block that has our mixin-injected properties
        for (Block block : BuiltInRegistries.BLOCK) {
            boolean hasIndicator = block.defaultBlockState().getProperties().contains(HandyIndicator.HAS_ITEMS)
                    || block.defaultBlockState().getProperties().contains(HandyIndicator.HAS_INPUT)
                    || block.defaultBlockState().getProperties().contains(HandyIndicator.HAS_ITEMS_READY);
            if (hasIndicator) {
                BlockColorRegistry.register(tintSources, block);
            }
        }
    }
}
