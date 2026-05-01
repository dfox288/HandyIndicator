package dev.handy.mods.handyindicator.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class OverlayBlockModelPart implements BlockStateModelPart {

    private final Map<Direction, List<BakedQuad>> culledQuads;
    private final List<BakedQuad> unculledQuads;
    private final Material.Baked particleMaterial;
    private final int materialFlags;

    public OverlayBlockModelPart(List<BakedQuad> quads, Material.Baked particleMaterial, boolean useCullface) {
        this.particleMaterial = particleMaterial;
        this.culledQuads = new EnumMap<>(Direction.class);
        this.unculledQuads = new ArrayList<>();

        if (useCullface) {
            for (Direction dir : Direction.values()) {
                culledQuads.put(dir, new ArrayList<>());
            }
            for (BakedQuad quad : quads) {
                culledQuads.get(quad.direction()).add(quad);
            }
        } else {
            unculledQuads.addAll(quads);
        }

        // Material flags must be the OR of every quad's MaterialInfo.flags() —
        // this is what vanilla SimpleModelWrapper does via QuadCollection. Returning
        // a flat 0 here was the 26.2-snapshot-5 regression: SectionCompiler uses
        // materialFlags to decide which RenderType buckets to allocate, and a 0
        // result tells it "nothing to render" so the overlay is collected but never
        // submitted to the GPU.
        int flags = 0;
        for (BakedQuad q : quads) {
            flags |= q.materialInfo().flags();
        }
        this.materialFlags = flags;
    }

    @Override
    public List<BakedQuad> getQuads(Direction direction) {
        if (direction == null) {
            return unculledQuads;
        }
        List<BakedQuad> list = culledQuads.get(direction);
        return list != null ? list : List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public Material.Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    public int materialFlags() {
        return materialFlags;
    }
}
