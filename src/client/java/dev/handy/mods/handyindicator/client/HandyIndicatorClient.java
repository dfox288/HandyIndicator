package dev.handy.mods.handyindicator.client;

import dev.handy.mods.handyindicator.HandyIndicator;
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

        // Register color providers for any block that has our mixin-injected
        // properties.
        //
        // We iterate the live BuiltInRegistries.BLOCK rather than maintaining
        // a hand-coded list of target blocks. This keeps the wiring honest —
        // if a future mixin adds HAS_ITEMS to a new block class, the iteration
        // picks it up automatically without a parallel list to maintain.
        //
        // Iteration order on the registry is initialization order. Other mods
        // adding their own blocks to this registry will appear here too if
        // they happen to declare any of our properties (vanishingly unlikely
        // in practice — these are mod-private property singletons — but
        // cheap to scan and harmless if it ever happens).
        //
        // Called once from onInitializeClient, so the linear scan over the
        // ~700-block registry is one-shot startup cost, not a hot path.
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
