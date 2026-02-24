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

        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tintIndex) -> {
                    if (tintIndex == 0) return ContainerIndicator.getIndicatorColor();
                    if (tintIndex == 1) return ContainerIndicator.getFuelColor();
                    return 0xFFFFFF;
                },
                Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
                Blocks.BARREL, Blocks.CRAFTER,
                Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER
        );
    }
}
