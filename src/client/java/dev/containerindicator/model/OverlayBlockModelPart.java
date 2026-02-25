package dev.containerindicator.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class OverlayBlockModelPart implements BlockModelPart {

    private final Map<Direction, List<BakedQuad>> culledQuads;
    private final List<BakedQuad> unculledQuads;
    private final TextureAtlasSprite sprite;

    public OverlayBlockModelPart(List<BakedQuad> quads, TextureAtlasSprite sprite, boolean useCullface) {
        this.sprite = sprite;
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
    public TextureAtlasSprite particleIcon() {
        return sprite;
    }
}
