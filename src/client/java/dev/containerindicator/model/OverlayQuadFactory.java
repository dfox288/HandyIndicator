package dev.containerindicator.model;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public final class OverlayQuadFactory {

    private OverlayQuadFactory() {}

    // --- Public factory methods ---

    public static List<BakedQuad> createStandardOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        // UP faces at Y=16.01-16.02
        quads.add(createQuad(sprite, tintIndex, Direction.UP, Direction.UP,
                0, 16.01f, 0, 16, 16.02f, 1));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, Direction.UP,
                0, 16.01f, 15, 16, 16.02f, 16));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, Direction.UP,
                0, 16.01f, 1, 1, 16.02f, 15));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, Direction.UP,
                15, 16.01f, 1, 16, 16.02f, 15));
        // Side strips at Y=15-16
        quads.add(createQuad(sprite, tintIndex, Direction.NORTH, Direction.NORTH,
                0, 15, -0.01f, 16, 16, -0.001f));
        quads.add(createQuad(sprite, tintIndex, Direction.SOUTH, Direction.SOUTH,
                0, 15, 16.001f, 16, 16, 16.01f));
        quads.add(createQuad(sprite, tintIndex, Direction.WEST, Direction.WEST,
                -0.01f, 15, 0, -0.001f, 16, 16));
        quads.add(createQuad(sprite, tintIndex, Direction.EAST, Direction.EAST,
                16.001f, 15, 0, 16.01f, 16, 16));
        return quads;
    }

    public static List<BakedQuad> createBottomOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        // DOWN faces at Y=-0.02 to -0.01
        quads.add(createQuad(sprite, tintIndex, Direction.DOWN, Direction.DOWN,
                0, -0.02f, 0, 16, -0.01f, 1));
        quads.add(createQuad(sprite, tintIndex, Direction.DOWN, Direction.DOWN,
                0, -0.02f, 15, 16, -0.01f, 16));
        quads.add(createQuad(sprite, tintIndex, Direction.DOWN, Direction.DOWN,
                0, -0.02f, 1, 1, -0.01f, 15));
        quads.add(createQuad(sprite, tintIndex, Direction.DOWN, Direction.DOWN,
                15, -0.02f, 1, 16, -0.01f, 15));
        // Side strips at Y=0-1
        quads.add(createQuad(sprite, tintIndex, Direction.NORTH, Direction.NORTH,
                0, 0, -0.01f, 16, 1, -0.001f));
        quads.add(createQuad(sprite, tintIndex, Direction.SOUTH, Direction.SOUTH,
                0, 0, 16.001f, 16, 1, 16.01f));
        quads.add(createQuad(sprite, tintIndex, Direction.WEST, Direction.WEST,
                -0.01f, 0, 0, -0.001f, 1, 16));
        quads.add(createQuad(sprite, tintIndex, Direction.EAST, Direction.EAST,
                16.001f, 0, 0, 16.01f, 1, 16));
        return quads;
    }

    public static List<BakedQuad> createPotOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        // UP faces at Y=16.01-16.02, inset by 1
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                1, 16.01f, 1, 15, 16.02f, 2));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                1, 16.01f, 14, 15, 16.02f, 15));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                1, 16.01f, 2, 2, 16.02f, 14));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                14, 16.01f, 2, 15, 16.02f, 14));
        // Side strips at Y=15-16, inset by 1
        quads.add(createQuad(sprite, tintIndex, Direction.NORTH, null,
                1, 15, 0.99f, 15, 16, 0.999f));
        quads.add(createQuad(sprite, tintIndex, Direction.SOUTH, null,
                1, 15, 15.001f, 15, 16, 15.01f));
        quads.add(createQuad(sprite, tintIndex, Direction.WEST, null,
                0.99f, 15, 1, 0.999f, 16, 15));
        quads.add(createQuad(sprite, tintIndex, Direction.EAST, null,
                15.001f, 15, 1, 15.01f, 16, 15));
        return quads;
    }

    public static List<BakedQuad> createChestOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        // UP faces at Y=9.01-9.02, inset by 1
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                1, 9.01f, 1, 15, 9.02f, 2));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                1, 9.01f, 14, 15, 9.02f, 15));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                1, 9.01f, 2, 2, 9.02f, 14));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                14, 9.01f, 2, 15, 9.02f, 14));
        // Side strips at Y=8-9, inset by 1
        quads.add(createQuad(sprite, tintIndex, Direction.NORTH, null,
                1, 8, 0.99f, 15, 9, 0.999f));
        quads.add(createQuad(sprite, tintIndex, Direction.SOUTH, null,
                1, 8, 15.001f, 15, 9, 15.01f));
        quads.add(createQuad(sprite, tintIndex, Direction.WEST, null,
                0.99f, 8, 1, 0.999f, 9, 15));
        quads.add(createQuad(sprite, tintIndex, Direction.EAST, null,
                15.001f, 8, 1, 15.01f, 9, 15));
        return quads;
    }

    public static List<BakedQuad> createDoubleChestOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        // UP faces at Y=9.01-9.02, X spans 1-31
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                1, 9.01f, 1, 31, 9.02f, 2));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                1, 9.01f, 14, 31, 9.02f, 15));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                1, 9.01f, 2, 2, 9.02f, 14));
        quads.add(createQuad(sprite, tintIndex, Direction.UP, null,
                30, 9.01f, 2, 31, 9.02f, 14));
        // Side strips at Y=8-9
        quads.add(createQuad(sprite, tintIndex, Direction.NORTH, null,
                1, 8, 0.99f, 31, 9, 0.999f));
        quads.add(createQuad(sprite, tintIndex, Direction.SOUTH, null,
                1, 8, 15.001f, 31, 9, 15.01f));
        quads.add(createQuad(sprite, tintIndex, Direction.WEST, null,
                0.99f, 8, 1, 0.999f, 9, 15));
        quads.add(createQuad(sprite, tintIndex, Direction.EAST, null,
                31.001f, 8, 1, 31.01f, 9, 15));
        return quads;
    }

    public static List<BakedQuad> rotateQuadsY(List<BakedQuad> quads, int degrees) {
        if (degrees == 0) return quads;
        int steps = ((degrees % 360) + 360) % 360 / 90;
        if (steps == 0) return quads;

        List<BakedQuad> rotated = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            rotated.add(rotateQuadY(quad, steps));
        }
        return rotated;
    }

    // --- Internal helpers ---

    private static BakedQuad rotateQuadY(BakedQuad quad, int steps) {
        Vector3fc p0 = rotatePointY(quad.position0(), steps);
        Vector3fc p1 = rotatePointY(quad.position1(), steps);
        Vector3fc p2 = rotatePointY(quad.position2(), steps);
        Vector3fc p3 = rotatePointY(quad.position3(), steps);

        Direction newDir = rotateDirection(quad.direction(), steps);

        return new BakedQuad(p0, p1, p2, p3,
                quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                quad.tintIndex(), newDir, quad.sprite(), quad.shade(), quad.lightEmission());
    }

    private static Vector3fc rotatePointY(Vector3fc point, int steps) {
        float x = point.x();
        float y = point.y();
        float z = point.z();
        float cx = x - 0.5f;
        float cz = z - 0.5f;

        float nx, nz;
        switch (steps) {
            case 1: // 90 degrees
                nx = -cz + 0.5f;
                nz = cx + 0.5f;
                break;
            case 2: // 180 degrees
                nx = -cx + 0.5f;
                nz = -cz + 0.5f;
                break;
            case 3: // 270 degrees
                nx = cz + 0.5f;
                nz = -cx + 0.5f;
                break;
            default:
                nx = x;
                nz = z;
                break;
        }
        return new Vector3f(nx, y, nz);
    }

    private static Direction rotateDirection(Direction dir, int steps) {
        if (dir.getAxis() == Direction.Axis.Y) return dir;
        Direction result = dir;
        for (int i = 0; i < steps; i++) {
            result = result.getClockWise();
        }
        return result;
    }

    /**
     * Creates a single BakedQuad from model-space coordinates.
     *
     * @param sprite    the texture sprite
     * @param tintIndex the tint index for color provider
     * @param face      the face direction (determines vertex winding)
     * @param cullface  the cullface direction, or null for unculled
     * @param x0        from X in model-space (0-16)
     * @param y0        from Y in model-space
     * @param z0        from Z in model-space
     * @param x1        to X in model-space
     * @param y1        to Y in model-space
     * @param z1        to Z in model-space
     */
    private static BakedQuad createQuad(TextureAtlasSprite sprite, int tintIndex,
                                        Direction face, Direction cullface,
                                        float x0, float y0, float z0,
                                        float x1, float y1, float z1) {
        // Convert to block-space (0-1)
        float bx0 = x0 / 16f, by0 = y0 / 16f, bz0 = z0 / 16f;
        float bx1 = x1 / 16f, by1 = y1 / 16f, bz1 = z1 / 16f;

        // UV coordinates: 0-1 fraction within sprite
        long packedUV00 = UVPair.pack(sprite.getU(0f), sprite.getV(0f));
        long packedUV01 = UVPair.pack(sprite.getU(0f), sprite.getV(1f));
        long packedUV10 = UVPair.pack(sprite.getU(1f), sprite.getV(0f));
        long packedUV11 = UVPair.pack(sprite.getU(1f), sprite.getV(1f));

        // Build vertex positions based on face direction (CCW winding when viewed from outside)
        Vector3fc p0, p1, p2, p3;
        long uv0, uv1, uv2, uv3;

        switch (face) {
            case UP -> {
                p0 = new Vector3f(bx0, by1, bz0);
                p1 = new Vector3f(bx0, by1, bz1);
                p2 = new Vector3f(bx1, by1, bz1);
                p3 = new Vector3f(bx1, by1, bz0);
                uv0 = packedUV00; uv1 = packedUV01; uv2 = packedUV11; uv3 = packedUV10;
            }
            case DOWN -> {
                p0 = new Vector3f(bx0, by0, bz1);
                p1 = new Vector3f(bx0, by0, bz0);
                p2 = new Vector3f(bx1, by0, bz0);
                p3 = new Vector3f(bx1, by0, bz1);
                uv0 = packedUV00; uv1 = packedUV01; uv2 = packedUV11; uv3 = packedUV10;
            }
            case NORTH -> {
                p0 = new Vector3f(bx1, by1, bz0);
                p1 = new Vector3f(bx1, by0, bz0);
                p2 = new Vector3f(bx0, by0, bz0);
                p3 = new Vector3f(bx0, by1, bz0);
                uv0 = packedUV00; uv1 = packedUV01; uv2 = packedUV11; uv3 = packedUV10;
            }
            case SOUTH -> {
                p0 = new Vector3f(bx0, by1, bz1);
                p1 = new Vector3f(bx0, by0, bz1);
                p2 = new Vector3f(bx1, by0, bz1);
                p3 = new Vector3f(bx1, by1, bz1);
                uv0 = packedUV00; uv1 = packedUV01; uv2 = packedUV11; uv3 = packedUV10;
            }
            case WEST -> {
                p0 = new Vector3f(bx0, by1, bz0);
                p1 = new Vector3f(bx0, by0, bz0);
                p2 = new Vector3f(bx0, by0, bz1);
                p3 = new Vector3f(bx0, by1, bz1);
                uv0 = packedUV00; uv1 = packedUV01; uv2 = packedUV11; uv3 = packedUV10;
            }
            case EAST -> {
                p0 = new Vector3f(bx1, by1, bz1);
                p1 = new Vector3f(bx1, by0, bz1);
                p2 = new Vector3f(bx1, by0, bz0);
                p3 = new Vector3f(bx1, by1, bz0);
                uv0 = packedUV00; uv1 = packedUV01; uv2 = packedUV11; uv3 = packedUV10;
            }
            default -> throw new IllegalArgumentException("Unknown face direction: " + face);
        }

        // cullface: use the cullface direction for the BakedQuad's direction field
        Direction quadDir = cullface != null ? cullface : face;

        return new BakedQuad(p0, p1, p2, p3, uv0, uv1, uv2, uv3,
                tintIndex, quadDir, sprite, false, 0);
    }
}
