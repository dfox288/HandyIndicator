# Resource Pack Compatibility Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace 19 static blockstate JSON overrides with programmatic overlay injection via mixin, so indicator overlays work alongside any resource pack.

**Architecture:** Mixin into `BlockModelShaper.replaceCache()` wraps baked `BlockStateModel` instances for container blocks with a composite that appends overlay quads. Overlay geometry is constructed from `BakedQuad` instances using the indicator sprite from the block texture atlas.

**Tech Stack:** Fabric Mixin, Minecraft 1.21.11 (Mojang mappings), Java 21

---

### Task 1: Create Client Mixin Config

**Files:**
- Create: `src/client/resources/container-indicator-client.mixins.json`
- Modify: `src/main/resources/fabric.mod.json`

**Step 1: Create the client mixin config**

```json
{
    "required": true,
    "package": "dev.containerindicator.mixin.client",
    "compatibilityLevel": "JAVA_21",
    "client": [
        "BlockModelShaperMixin"
    ],
    "injectors": {
        "defaultRequire": 1
    }
}
```

**Step 2: Register in fabric.mod.json**

Add to the `"mixins"` array:
```json
"mixins": [
    "container-indicator.mixins.json",
    "container-indicator-client.mixins.json"
]

```

Note: The client mixin JSON lives in `src/client/resources/` since it contains client-only class references. Fabric Loom merges client resources into the final jar.

**Step 3: Build to verify config loads**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (mixin class doesn't exist yet, but config should parse)

Note: The build may fail because `BlockModelShaperMixin` doesn't exist yet. If so, create a placeholder first (see Task 5) or temporarily remove the mixin entry and re-add it in Task 5.

**Step 4: Commit**

```bash
git add src/client/resources/container-indicator-client.mixins.json src/main/resources/fabric.mod.json
git commit -m "Add client-side mixin config for model injection"
```

---

### Task 2: Create OverlayQuadFactory

This is the core geometry builder. It creates `BakedQuad` instances for each overlay type.

**Files:**
- Create: `src/client/java/dev/containerindicator/model/OverlayQuadFactory.java`

**Step 1: Create the factory class**

```java
package dev.containerindicator.model;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds BakedQuad instances for indicator overlay geometry.
 * All coordinates use model-space (0-16 scale) internally,
 * converted to block-space (0-1) for BakedQuad positions.
 */
public class OverlayQuadFactory {

    /**
     * Standard top-edge overlay (hoppers, droppers, dispensers, barrels, crafters).
     * 4 top-face strips + 4 side strips at Y=15-16. All culled.
     */
    public static List<BakedQuad> createStandardOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        // Top border strips (UP face, cullface=up)
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 0,16.01f,0, 16,16.02f,1));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 0,16.01f,15, 16,16.02f,16));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 0,16.01f,1, 1,16.02f,15));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 15,16.01f,1, 16,16.02f,15));
        // Side strips at top (1px tall, slightly outside block bounds)
        quads.add(makeQuad(sprite, tintIndex, Direction.NORTH, 0,15,-0.01f, 16,16,-0.001f));
        quads.add(makeQuad(sprite, tintIndex, Direction.SOUTH, 0,15,16.001f, 16,16,16.01f));
        quads.add(makeQuad(sprite, tintIndex, Direction.WEST, -0.01f,15,0, -0.001f,16,16));
        quads.add(makeQuad(sprite, tintIndex, Direction.EAST, 16.001f,15,0, 16.01f,16,16));
        return quads;
    }

    /**
     * Bottom-edge overlay for furnace fuel indicator.
     * 4 bottom-face strips + 4 side strips at Y=0-1. All culled.
     */
    public static List<BakedQuad> createBottomOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        quads.add(makeQuad(sprite, tintIndex, Direction.DOWN, 0,-0.02f,0, 16,-0.01f,1));
        quads.add(makeQuad(sprite, tintIndex, Direction.DOWN, 0,-0.02f,15, 16,-0.01f,16));
        quads.add(makeQuad(sprite, tintIndex, Direction.DOWN, 0,-0.02f,1, 1,-0.01f,15));
        quads.add(makeQuad(sprite, tintIndex, Direction.DOWN, 15,-0.02f,1, 16,-0.01f,15));
        quads.add(makeQuad(sprite, tintIndex, Direction.NORTH, 0,0,-0.01f, 16,1,-0.001f));
        quads.add(makeQuad(sprite, tintIndex, Direction.SOUTH, 0,0,16.001f, 16,1,16.01f));
        quads.add(makeQuad(sprite, tintIndex, Direction.WEST, -0.01f,0,0, -0.001f,1,16));
        quads.add(makeQuad(sprite, tintIndex, Direction.EAST, 16.001f,0,0, 16.01f,1,16));
        return quads;
    }

    /**
     * Pot overlay - rim at Y=15-16, inset by 1 from edges. No cullface.
     */
    public static List<BakedQuad> createPotOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 1,16.01f,1, 15,16.02f,2));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 1,16.01f,14, 15,16.02f,15));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 1,16.01f,2, 2,16.02f,14));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 14,16.01f,2, 15,16.02f,14));
        quads.add(makeQuad(sprite, tintIndex, Direction.NORTH, 1,15,0.99f, 15,16,0.999f));
        quads.add(makeQuad(sprite, tintIndex, Direction.SOUTH, 1,15,15.001f, 15,16,15.01f));
        quads.add(makeQuad(sprite, tintIndex, Direction.WEST, 0.99f,15,1, 0.999f,16,15));
        quads.add(makeQuad(sprite, tintIndex, Direction.EAST, 15.001f,15,1, 15.01f,16,15));
        return quads;
    }

    /**
     * Single chest overlay - rim at hinge level Y=8-9, inset by 1. No cullface.
     */
    public static List<BakedQuad> createChestOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 1,9.01f,1, 15,9.02f,2));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 1,9.01f,14, 15,9.02f,15));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 1,9.01f,2, 2,9.02f,14));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 14,9.01f,2, 15,9.02f,14));
        quads.add(makeQuad(sprite, tintIndex, Direction.NORTH, 1,8,0.99f, 15,9,0.999f));
        quads.add(makeQuad(sprite, tintIndex, Direction.SOUTH, 1,8,15.001f, 15,9,15.01f));
        quads.add(makeQuad(sprite, tintIndex, Direction.WEST, 0.99f,8,1, 0.999f,9,15));
        quads.add(makeQuad(sprite, tintIndex, Direction.EAST, 15.001f,8,1, 15.01f,9,15));
        return quads;
    }

    /**
     * Double chest overlay - spans 32 model units wide.
     * North-facing (0 deg rotation) geometry. Call rotateQuadsY() for other facings.
     */
    public static List<BakedQuad> createDoubleChestOverlay(TextureAtlasSprite sprite, int tintIndex) {
        List<BakedQuad> quads = new ArrayList<>();
        // Top rim spans X=1..31 (both halves)
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 1,9.01f,1, 31,9.02f,2));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 1,9.01f,14, 31,9.02f,15));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 1,9.01f,2, 2,9.02f,14));
        quads.add(makeQuad(sprite, tintIndex, Direction.UP, 30,9.01f,2, 31,9.02f,14));
        // Side strips
        quads.add(makeQuad(sprite, tintIndex, Direction.NORTH, 1,8,0.99f, 31,9,0.999f));
        quads.add(makeQuad(sprite, tintIndex, Direction.SOUTH, 1,8,15.001f, 31,9,15.01f));
        quads.add(makeQuad(sprite, tintIndex, Direction.WEST, 0.99f,8,1, 0.999f,9,15));
        quads.add(makeQuad(sprite, tintIndex, Direction.EAST, 31.001f,8,1, 31.01f,9,15));
        return quads;
    }

    /**
     * Rotate a list of quads by the given angle (90, 180, 270) around Y axis
     * at center (0.5, y, 0.5) in block space. Returns new list.
     */
    public static List<BakedQuad> rotateQuadsY(List<BakedQuad> quads, int degrees) {
        List<BakedQuad> rotated = new ArrayList<>(quads.size());
        for (BakedQuad q : quads) {
            rotated.add(new BakedQuad(
                rotateY(q.position0(), degrees),
                rotateY(q.position1(), degrees),
                rotateY(q.position2(), degrees),
                rotateY(q.position3(), degrees),
                q.packedUV0(), q.packedUV1(), q.packedUV2(), q.packedUV3(),
                q.tintIndex(), rotateDirection(q.direction(), degrees),
                q.sprite(), q.shade(), q.lightEmission()
            ));
        }
        return rotated;
    }

    // --- Private helpers ---

    /**
     * Create a BakedQuad for a single face of a box element.
     * from/to are in model-space (0-16). Positions are converted to block-space (0-1).
     */
    private static BakedQuad makeQuad(TextureAtlasSprite sprite, int tintIndex,
            Direction face, float fx, float fy, float fz, float tx, float ty, float tz) {
        // Convert model-space to block-space
        float x0 = fx / 16f, y0 = fy / 16f, z0 = fz / 16f;
        float x1 = tx / 16f, y1 = ty / 16f, z1 = tz / 16f;

        // Simple UV mapping - map full sprite to face
        long uv00 = UVPair.pack(sprite.getU(0f), sprite.getV(0f));
        long uv01 = UVPair.pack(sprite.getU(0f), sprite.getV(1f));
        long uv11 = UVPair.pack(sprite.getU(1f), sprite.getV(1f));
        long uv10 = UVPair.pack(sprite.getU(1f), sprite.getV(0f));

        // Vertex positions depend on face direction (standard Minecraft winding order)
        Vector3f p0, p1, p2, p3;
        long puv0, puv1, puv2, puv3;

        switch (face) {
            case UP -> {
                p0 = new Vector3f(x0, y1, z0);
                p1 = new Vector3f(x0, y1, z1);
                p2 = new Vector3f(x1, y1, z1);
                p3 = new Vector3f(x1, y1, z0);
                puv0 = uv00; puv1 = uv01; puv2 = uv11; puv3 = uv10;
            }
            case DOWN -> {
                p0 = new Vector3f(x0, y0, z1);
                p1 = new Vector3f(x0, y0, z0);
                p2 = new Vector3f(x1, y0, z0);
                p3 = new Vector3f(x1, y0, z1);
                puv0 = uv00; puv1 = uv01; puv2 = uv11; puv3 = uv10;
            }
            case NORTH -> {
                p0 = new Vector3f(x1, y1, z0);
                p1 = new Vector3f(x1, y0, z0);
                p2 = new Vector3f(x0, y0, z0);
                p3 = new Vector3f(x0, y1, z0);
                puv0 = uv00; puv1 = uv01; puv2 = uv11; puv3 = uv10;
            }
            case SOUTH -> {
                p0 = new Vector3f(x0, y1, z1);
                p1 = new Vector3f(x0, y0, z1);
                p2 = new Vector3f(x1, y0, z1);
                p3 = new Vector3f(x1, y1, z1);
                puv0 = uv00; puv1 = uv01; puv2 = uv11; puv3 = uv10;
            }
            case WEST -> {
                p0 = new Vector3f(x0, y1, z1);
                p1 = new Vector3f(x0, y0, z1);
                p2 = new Vector3f(x0, y0, z0);
                p3 = new Vector3f(x0, y1, z0);
                puv0 = uv00; puv1 = uv01; puv2 = uv11; puv3 = uv10;
            }
            case EAST -> {
                p0 = new Vector3f(x1, y1, z0);
                p1 = new Vector3f(x1, y0, z0);
                p2 = new Vector3f(x1, y0, z1);
                p3 = new Vector3f(x1, y1, z1);
                puv0 = uv00; puv1 = uv01; puv2 = uv11; puv3 = uv10;
            }
            default -> throw new IllegalArgumentException("Unknown direction: " + face);
        }

        return new BakedQuad(p0, p1, p2, p3, puv0, puv1, puv2, puv3,
            tintIndex, face, sprite, false, 0);
    }

    private static Vector3f rotateY(Vector3f pos, int degrees) {
        // Rotate around Y axis at center (0.5, y, 0.5) in block space
        float cx = pos.x() - 0.5f, cz = pos.z() - 0.5f;
        return switch (degrees) {
            case 90 -> new Vector3f(-cz + 0.5f, pos.y(), cx + 0.5f);
            case 180 -> new Vector3f(-cx + 0.5f, pos.y(), -cz + 0.5f);
            case 270 -> new Vector3f(cz + 0.5f, pos.y(), -cx + 0.5f);
            default -> new Vector3f(pos);
        };
    }

    private static Direction rotateDirection(Direction dir, int degrees) {
        if (dir == Direction.UP || dir == Direction.DOWN) return dir;
        Direction result = dir;
        for (int i = 0; i < degrees / 90; i++) {
            result = result.getClockWise();
        }
        return result;
    }
}
```

**Step 2: Build to verify compilation**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

Note: If `UVPair` or `Vector3f` import paths differ, check decompiled sources. `UVPair` is at `net.minecraft.client.model.geom.builders.UVPair`. `Vector3f` is `org.joml.Vector3f`. `BakedQuad` is at `net.minecraft.client.renderer.block.model.BakedQuad`.

**Step 3: Commit**

```bash
git add src/client/java/dev/containerindicator/model/OverlayQuadFactory.java
git commit -m "Add OverlayQuadFactory for programmatic indicator quad construction"
```

---

### Task 3: Create OverlayBlockModelPart

**Files:**
- Create: `src/client/java/dev/containerindicator/model/OverlayBlockModelPart.java`

**Step 1: Create the class**

```java
package dev.containerindicator.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.*;

/**
 * A BlockModelPart that holds pre-built overlay quads organized by face direction.
 * Quads with matching cullface direction are returned for that direction;
 * unculled quads are returned for null direction.
 */
public class OverlayBlockModelPart implements BlockModelPart {
    private final Map<Direction, List<BakedQuad>> culledQuads;
    private final List<BakedQuad> unculledQuads;
    private final TextureAtlasSprite sprite;
    private final boolean useCullface;

    /**
     * @param quads       All overlay quads for this part
     * @param sprite      The indicator texture sprite
     * @param useCullface If true, quads are organized by their direction as cullface.
     *                    If false, all quads are unculled (always rendered).
     */
    public OverlayBlockModelPart(List<BakedQuad> quads, TextureAtlasSprite sprite, boolean useCullface) {
        this.sprite = sprite;
        this.useCullface = useCullface;

        if (useCullface) {
            Map<Direction, List<BakedQuad>> map = new EnumMap<>(Direction.class);
            for (BakedQuad quad : quads) {
                map.computeIfAbsent(quad.direction(), d -> new ArrayList<>()).add(quad);
            }
            this.culledQuads = map;
            this.unculledQuads = List.of();
        } else {
            this.culledQuads = Map.of();
            this.unculledQuads = List.copyOf(quads);
        }
    }

    @Override
    public List<BakedQuad> getQuads(Direction direction) {
        if (direction == null) {
            return unculledQuads;
        }
        return culledQuads.getOrDefault(direction, List.of());
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
```

**Step 2: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add src/client/java/dev/containerindicator/model/OverlayBlockModelPart.java
git commit -m "Add OverlayBlockModelPart for face-organized overlay quads"
```

---

### Task 4: Create CompositeBlockStateModel

**Files:**
- Create: `src/client/java/dev/containerindicator/model/CompositeBlockStateModel.java`

**Step 1: Create the class**

```java
package dev.containerindicator.model;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Wraps an existing BlockStateModel and appends overlay parts.
 * The original model renders first, then overlay parts are added on top.
 */
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
```

**Step 2: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add src/client/java/dev/containerindicator/model/CompositeBlockStateModel.java
git commit -m "Add CompositeBlockStateModel wrapper for overlay injection"
```

---

### Task 5: Create BlockModelShaperMixin

This is the core mixin that injects overlay models into the block model cache.

**Files:**
- Create: `src/client/java/dev/containerindicator/mixin/client/BlockModelShaperMixin.java`

**Step 1: Create the mixin class**

```java
package dev.containerindicator.mixin.client;

import dev.containerindicator.ContainerIndicator;
import dev.containerindicator.model.CompositeBlockStateModel;
import dev.containerindicator.model.OverlayBlockModelPart;
import dev.containerindicator.model.OverlayQuadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {

    @Inject(method = "replaceCache", at = @At("HEAD"))
    private void injectOverlays(Map<BlockState, BlockStateModel> map, CallbackInfo ci) {
        // Get indicator sprite from block atlas
        TextureAtlasSprite sprite = Minecraft.getInstance()
            .getModelManager()
            .getAtlas(TextureAtlas.LOCATION_BLOCKS)
            .getSprite(ResourceLocation.fromNamespaceAndPath(
                "container_indicator", "block/indicator"));

        // Pre-build overlay parts (one set shared across all blocks of same type)
        List<BlockModelPart> standardOverlay = List.of(
            new OverlayBlockModelPart(
                OverlayQuadFactory.createStandardOverlay(sprite, 0), sprite, true));
        List<BlockModelPart> bottomOverlay = List.of(
            new OverlayBlockModelPart(
                OverlayQuadFactory.createBottomOverlay(sprite, 1), sprite, true));
        List<BlockModelPart> potOverlay = List.of(
            new OverlayBlockModelPart(
                OverlayQuadFactory.createPotOverlay(sprite, 0), sprite, false));
        List<BlockModelPart> chestOverlay = List.of(
            new OverlayBlockModelPart(
                OverlayQuadFactory.createChestOverlay(sprite, 0), sprite, false));

        // Pre-build rotated double chest overlays
        List<BakedQuad> baseDoubleQuads = OverlayQuadFactory.createDoubleChestOverlay(sprite, 0);
        Map<Direction, List<BlockModelPart>> doubleChestOverlays = new EnumMap<>(Direction.class);
        doubleChestOverlays.put(Direction.NORTH, List.of(
            new OverlayBlockModelPart(baseDoubleQuads, sprite, false)));
        doubleChestOverlays.put(Direction.SOUTH, List.of(
            new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(baseDoubleQuads, 180), sprite, false)));
        doubleChestOverlays.put(Direction.WEST, List.of(
            new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(baseDoubleQuads, 270), sprite, false)));
        doubleChestOverlays.put(Direction.EAST, List.of(
            new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(baseDoubleQuads, 90), sprite, false)));

        // Chest block set for fast lookup
        Set<Block> chestBlocks = Set.of(
            Blocks.CHEST, Blocks.TRAPPED_CHEST,
            Blocks.COPPER_CHEST, Blocks.EXPOSED_COPPER_CHEST,
            Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER_CHEST,
            Blocks.WAXED_COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER_CHEST,
            Blocks.WAXED_WEATHERED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER_CHEST
        );

        Set<Block> simpleBlocks = Set.of(
            Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
            Blocks.BARREL, Blocks.CRAFTER
        );

        Set<Block> furnaceBlocks = Set.of(
            Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER
        );

        // Wrap models for target block states
        for (Map.Entry<BlockState, BlockStateModel> entry : map.entrySet()) {
            BlockState state = entry.getKey();
            Block block = state.getBlock();

            if (simpleBlocks.contains(block)) {
                if (state.getValue(ContainerIndicator.HAS_ITEMS)) {
                    entry.setValue(new CompositeBlockStateModel(
                        entry.getValue(), standardOverlay));
                }
            } else if (block == Blocks.DECORATED_POT) {
                if (state.getValue(ContainerIndicator.HAS_ITEMS)) {
                    entry.setValue(new CompositeBlockStateModel(
                        entry.getValue(), potOverlay));
                }
            } else if (furnaceBlocks.contains(block)) {
                BlockStateModel model = entry.getValue();
                boolean hasInput = state.getValue(ContainerIndicator.HAS_INPUT);
                boolean hasFuel = state.getValue(ContainerIndicator.HAS_FUEL);
                if (hasInput && hasFuel) {
                    // Both overlays - chain two composites
                    List<BlockModelPart> combined = new ArrayList<>();
                    combined.addAll(standardOverlay);
                    combined.addAll(bottomOverlay);
                    entry.setValue(new CompositeBlockStateModel(model, combined));
                } else if (hasInput) {
                    entry.setValue(new CompositeBlockStateModel(model, standardOverlay));
                } else if (hasFuel) {
                    entry.setValue(new CompositeBlockStateModel(model, bottomOverlay));
                }
            } else if (chestBlocks.contains(block)) {
                if (state.getValue(ContainerIndicator.HAS_ITEMS)) {
                    ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);
                    if (type == ChestType.SINGLE) {
                        entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), chestOverlay));
                    } else if (type == ChestType.LEFT) {
                        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                        List<BlockModelPart> overlay = doubleChestOverlays.get(facing);
                        if (overlay != null) {
                            entry.setValue(new CompositeBlockStateModel(
                                entry.getValue(), overlay));
                        }
                    }
                    // RIGHT type: no overlay (left half handles the full double overlay)
                }
            }
        }
    }
}
```

**Important notes:**
- `BlockStateProperties.CHEST_TYPE` is the vanilla chest type property. If your mixin uses a different property reference, adjust accordingly.
- `BlockStateProperties.HORIZONTAL_FACING` is the vanilla horizontal facing property.
- `state.getValue()` on a property that doesn't exist on that block will throw. This is safe because we only check properties on blocks we know have them (via the Set membership check).

**Step 2: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

If import issues arise:
- `ChestType` may be at `net.minecraft.world.level.block.state.properties.ChestType` or `net.minecraft.world.level.block.entity.ChestType` — check decompiled sources
- `BlockStateProperties` provides common property constants
- If the chest `type` property is accessed differently on copper chests, check `ChestBlock` or `CopperChestBlock` for the property name

**Step 3: Commit**

```bash
git add src/client/java/dev/containerindicator/mixin/client/BlockModelShaperMixin.java
git commit -m "Add BlockModelShaperMixin to inject indicator overlays into model cache"
```

---

### Task 6: Delete Blockstate JSON Overrides

Remove all 19 static blockstate override files. The overlay model JSONs stay (as geometry reference, and they're still referenced by the indicator texture).

**Files:**
- Delete: All 19 files in `src/main/resources/assets/minecraft/blockstates/`

**Step 1: Delete the files**

```bash
rm src/main/resources/assets/minecraft/blockstates/hopper.json
rm src/main/resources/assets/minecraft/blockstates/dropper.json
rm src/main/resources/assets/minecraft/blockstates/dispenser.json
rm src/main/resources/assets/minecraft/blockstates/barrel.json
rm src/main/resources/assets/minecraft/blockstates/crafter.json
rm src/main/resources/assets/minecraft/blockstates/furnace.json
rm src/main/resources/assets/minecraft/blockstates/blast_furnace.json
rm src/main/resources/assets/minecraft/blockstates/smoker.json
rm src/main/resources/assets/minecraft/blockstates/decorated_pot.json
rm src/main/resources/assets/minecraft/blockstates/chest.json
rm src/main/resources/assets/minecraft/blockstates/trapped_chest.json
rm src/main/resources/assets/minecraft/blockstates/copper_chest.json
rm src/main/resources/assets/minecraft/blockstates/exposed_copper_chest.json
rm src/main/resources/assets/minecraft/blockstates/weathered_copper_chest.json
rm src/main/resources/assets/minecraft/blockstates/oxidized_copper_chest.json
rm src/main/resources/assets/minecraft/blockstates/waxed_copper_chest.json
rm src/main/resources/assets/minecraft/blockstates/waxed_exposed_copper_chest.json
rm src/main/resources/assets/minecraft/blockstates/waxed_weathered_copper_chest.json
rm src/main/resources/assets/minecraft/blockstates/waxed_oxidized_copper_chest.json
```

**Step 2: Verify the directory is empty, then remove it**

```bash
rmdir src/main/resources/assets/minecraft/blockstates/
rmdir src/main/resources/assets/minecraft/
```

**Step 3: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add -A src/main/resources/assets/minecraft/
git commit -m "Remove static blockstate JSON overrides (replaced by programmatic injection)"
```

---

### Task 7: Build and In-Game Verification

**Step 1: Full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 2: Launch the game**

Run: `./gradlew runClient`

**Step 3: Verify indicator overlays work**

Test checklist:
- [ ] Hopper with items shows top-rim indicator
- [ ] Dropper with items shows top-rim indicator
- [ ] Dispenser with items shows top-rim indicator
- [ ] Barrel with items shows top-rim indicator
- [ ] Crafter with items shows top-rim indicator
- [ ] Furnace with input shows top indicator
- [ ] Furnace with fuel shows bottom indicator
- [ ] Furnace with both shows both indicators
- [ ] Blast furnace indicators work
- [ ] Smoker indicators work
- [ ] Decorated pot with items shows pot overlay
- [ ] Single chest with items shows chest overlay
- [ ] Double chest (left half) shows spanning overlay
- [ ] Double chest (right half) shows NO overlay
- [ ] Double chest works in all 4 facing directions
- [ ] Copper chest variants work
- [ ] Indicators respect per-block config toggles
- [ ] Indicators have correct tint colors

**Step 4: Test resource pack compatibility**

- Install a resource pack that modifies container block models (e.g., Redstone Tweaks)
- Verify the resource pack's visual changes are preserved
- Verify indicator overlays still appear on top

**Step 5: Test resource reload**

- Press F3+T to reload resources
- Verify indicators still work after reload

**Step 6: Commit and push**

```bash
git add -A
git commit -m "Resource pack compatible indicator overlays via programmatic model injection"
git push
```

---

## Troubleshooting

### Vertex winding order issues
If quads appear inside-out (invisible from the correct side), the vertex order in `OverlayQuadFactory.makeQuad()` is wrong for that face direction. Swap p0↔p2 and puv0↔puv2 to reverse winding, or compare against `FaceBakery` decompiled source for the correct order per direction.

### UV mapping issues
If the overlay texture appears garbled, the UV packing might be wrong. Verify `UVPair.pack()` puts U in high 32 bits and V in low 32 bits. If the class doesn't exist, define it manually:
```java
long packUV(float u, float v) {
    return ((long)Float.floatToIntBits(u) << 32) | (Float.floatToIntBits(v) & 0xFFFFFFFFL);
}
```

### Sprite not found
If the indicator sprite returns the missing texture, ensure `src/main/resources/assets/container_indicator/textures/block/indicator.png` exists. The `ResourceLocation` path should be `container_indicator:block/indicator` (no `.png` extension, no `textures/` prefix).

### Property not found on block state
If `state.getValue(HAS_ITEMS)` throws for a block, that block's mixin isn't adding the property. Verify the block mixin (e.g., `HopperBlockMixin`) appends the property to the state definition.

### Quads not rendering (transparent)
Ensure `BlockRenderLayerMap.putBlock(block, ChunkSectionLayer.CUTOUT)` is still called in `ContainerIndicatorClient.onInitializeClient()` for all target blocks. The CUTOUT layer is required for transparent overlay rendering.
