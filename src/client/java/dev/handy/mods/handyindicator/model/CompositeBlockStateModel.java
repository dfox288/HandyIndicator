package dev.handy.mods.handyindicator.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;

import java.util.List;

public class CompositeBlockStateModel implements BlockStateModel {

    private final BlockStateModel original;
    private final List<BlockStateModelPart> overlayParts;

    public CompositeBlockStateModel(BlockStateModel original, List<BlockStateModelPart> overlayParts) {
        this.original = original;
        this.overlayParts = overlayParts;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        original.collectParts(random, parts);
        parts.addAll(overlayParts);
    }

    @Override
    public Material.Baked particleMaterial() {
        return original.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return original.materialFlags();
    }
}
