package dev.containerindicator;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ContainerIndicatorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Tint sources for indicator (index 0) and fuel (index 1)
        BlockTintSource indicatorTint = (state) -> ContainerIndicator.getIndicatorColor();
        BlockTintSource fuelTint = (state) -> ContainerIndicator.getFuelColor();
        List<BlockTintSource> tintSources = List.of(indicatorTint, fuelTint);

        // Register color providers for any block that has our mixin-injected properties
        for (Block block : BuiltInRegistries.BLOCK) {
            boolean hasIndicator = block.defaultBlockState().getProperties().contains(ContainerIndicator.HAS_ITEMS)
                    || block.defaultBlockState().getProperties().contains(ContainerIndicator.HAS_INPUT);
            if (hasIndicator) {
                BlockColorRegistry.register(tintSources, block);
            }
        }
    }
}
