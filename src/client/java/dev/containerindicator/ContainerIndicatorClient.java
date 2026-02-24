package dev.containerindicator;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Blocks;

public class ContainerIndicatorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlock(Blocks.HOPPER, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.DROPPER, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.DISPENSER, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.BARREL, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.CRAFTER, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.FURNACE, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.BLAST_FURNACE, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.SMOKER, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.DECORATED_POT, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.CHEST, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.TRAPPED_CHEST, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.COPPER_CHEST, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.EXPOSED_COPPER_CHEST, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.WEATHERED_COPPER_CHEST, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.OXIDIZED_COPPER_CHEST, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.WAXED_COPPER_CHEST, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.WAXED_EXPOSED_COPPER_CHEST, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.WAXED_WEATHERED_COPPER_CHEST, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Blocks.WAXED_OXIDIZED_COPPER_CHEST, ChunkSectionLayer.CUTOUT);

        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tintIndex) -> {
                    if (tintIndex == 0) return ContainerIndicator.getIndicatorColor();
                    if (tintIndex == 1) return ContainerIndicator.getFuelColor();
                    return 0xFFFFFF;
                },
                Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
                Blocks.BARREL, Blocks.CRAFTER,
                Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
                Blocks.DECORATED_POT,
                Blocks.CHEST, Blocks.TRAPPED_CHEST,
                Blocks.COPPER_CHEST, Blocks.EXPOSED_COPPER_CHEST,
                Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER_CHEST,
                Blocks.WAXED_COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER_CHEST,
                Blocks.WAXED_WEATHERED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER_CHEST
        );
    }
}
