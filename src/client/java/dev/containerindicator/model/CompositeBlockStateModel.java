package dev.containerindicator.model;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

import java.util.List;

public class CompositeBlockStateModel implements BlockStateModel {

    private final BlockStateModel original;
    private final List<BlockModelPart> overlayParts;

    public CompositeBlockStateModel(BlockStateModel original, List<BlockModelPart> overlayParts) {
        this.original = original;
        this.overlayParts = overlayParts;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        original.collectParts(random, parts);
        parts.addAll(overlayParts);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return original.particleIcon();
    }
}
