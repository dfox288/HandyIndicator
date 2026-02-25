package dev.containerindicator;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public class ContainerIndicatorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Dynamically register render layer and color provider for any block
        // that has our mixin-injected properties (covers vanilla + modded blocks)
        for (Block block : BuiltInRegistries.BLOCK) {
            boolean hasIndicator = block.defaultBlockState().getProperties().contains(ContainerIndicator.HAS_ITEMS)
                    || block.defaultBlockState().getProperties().contains(ContainerIndicator.HAS_INPUT);
            if (hasIndicator) {
                BlockRenderLayerMap.putBlock(block, ChunkSectionLayer.CUTOUT);
                ColorProviderRegistry.BLOCK.register(
                        (state, world, pos, tintIndex) -> {
                            if (tintIndex == 0) return ContainerIndicator.getIndicatorColor();
                            if (tintIndex == 1) return ContainerIndicator.getFuelColor();
                            return 0xFFFFFF;
                        },
                        block
                );
            }
        }
    }
}
