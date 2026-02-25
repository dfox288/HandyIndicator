# Resource Pack Compatibility — Programmatic Model Injection

**Date:** 2026-02-25
**Status:** Approved (Revised)

## Problem

The mod overrides 19 blockstate JSON files in `assets/minecraft/blockstates/`. When a resource pack (e.g., Redstone Tweaks) provides its own blockstate JSON for the same block, one override wins and the other is lost — either our indicator overlays disappear, or the resource pack's visual changes are removed.

## Solution

Replace static blockstate JSON overrides with programmatic overlay injection via a Mixin into `BlockModelShaper.replaceCache()`. After Minecraft bakes all block models (from vanilla or any resource pack), we wrap target container block models with a composite that appends overlay quads. Overlay geometry is constructed programmatically from `BakedQuad` instances.

**Note:** Fabric API 0.139.5+1.21.11 does not include `ModelLoadingPlugin` or `ModelModifier` (the model loading API module was removed). A mixin-based approach is required.

## Architecture

### New Files

- `src/client/java/dev/containerindicator/model/CompositeBlockStateModel.java` — `BlockStateModel` wrapper that delegates to original model and appends overlay parts via `collectParts()`
- `src/client/java/dev/containerindicator/model/OverlayBlockModelPart.java` — `BlockModelPart` implementation holding pre-built overlay quads organized by face direction
- `src/client/java/dev/containerindicator/model/OverlayQuadFactory.java` — Builds `BakedQuad` instances for each overlay type (standard, bottom, pot, chest, double chest)
- `src/client/java/dev/containerindicator/mixin/BlockModelShaperMixin.java` — Injects overlay wrapping into model cache
- `src/client/resources/container-indicator-client.mixins.json` — Client-side mixin config

### Modified Files

- `src/main/resources/fabric.mod.json` — Register client mixin config

### Deleted Files

- All 19 blockstate JSON overrides in `src/main/resources/assets/minecraft/blockstates/`

### Kept Files (reference only, no longer loaded at runtime)

- 5 overlay model JSONs in `assets/container_indicator/models/block/` — geometry reference

## How It Works

### Injection Point

```
Minecraft baking pipeline:
  BlockStateModelLoader → ModelDiscovery → ModelBakery → BakingResult
  ModelManager.createBlockStateToModelDispatch() → fills missing states
  BlockModelShaper.replaceCache(map)  ← OUR MIXIN INJECTS HERE
    └─ map is mutable IdentityHashMap<BlockState, BlockStateModel>
```

### BlockModelShaperMixin Flow

```java
@Inject(method = "replaceCache", at = @At("HEAD"))
private void injectOverlays(Map<BlockState, BlockStateModel> map, CallbackInfo ci) {
    // 1. Get indicator texture sprite from block atlas
    // 2. Build overlay BlockModelPart instances using OverlayQuadFactory
    // 3. Iterate map entries for target container blocks
    // 4. For states where has_items/has_input/has_fuel == true:
    //    wrap BlockStateModel with CompositeBlockStateModel(original, overlayParts)
}
```

### CompositeBlockStateModel

```java
class CompositeBlockStateModel implements BlockStateModel {
    private final BlockStateModel original;
    private final List<BlockModelPart> overlayParts;

    void collectParts(RandomSource random, List<BlockModelPart> parts) {
        original.collectParts(random, parts);
        parts.addAll(overlayParts);
    }

    TextureAtlasSprite particleIcon() { return original.particleIcon(); }
}
```

### BakedQuad Construction

Overlay quads are built manually using the `BakedQuad` record constructor:
```java
new BakedQuad(pos0, pos1, pos2, pos3,
    UVPair.pack(sprite.getU(0f), sprite.getV(0f)),  // packed UVs
    UVPair.pack(sprite.getU(0f), sprite.getV(1f)),
    UVPair.pack(sprite.getU(1f), sprite.getV(1f)),
    UVPair.pack(sprite.getU(1f), sprite.getV(0f)),
    tintIndex, direction, sprite, shade, lightEmission)
```

Positions are in block-space (0.0–1.0), converted from model-space (0–16) by dividing by 16.

### Overlay Selection Table

| Block Type | Condition | Overlay Type |
|---|---|---|
| Hopper, Dropper, Dispenser, Barrel, Crafter | `has_items=true` | Standard (top rim, tint 0) |
| Decorated Pot | `has_items=true` | Pot (top rim at Y=16, tint 0) |
| Furnace, Blast Furnace, Smoker | `has_input=true` | Standard (top rim, tint 0) |
| Furnace, Blast Furnace, Smoker | `has_fuel=true` | Bottom (bottom rim, tint 1) |
| Chest family (type=single) | `has_items=true` | Chest (hinge rim at Y=9, tint 0) |
| Chest family (type=left) | `has_items=true` | Double chest (rotated per facing, tint 0) |

Double chest rotation is computed programmatically by rotating vertex positions around (0.5, y, 0.5) in block space.

## Key Design Decisions

1. **Mixin over Fabric API** — `ModelLoadingPlugin` doesn't exist in Fabric API 0.139.5+1.21.11; mixin into `BlockModelShaper.replaceCache()` is the cleanest injection point
2. **Programmatic quads over model JSONs** — Self-contained, no model pipeline coordination needed, single mixin
3. **Composite wrapper** — Preserves the original model from any source (vanilla or resource pack), appends overlay parts on top
4. **Runtime rotation for double chests** — Avoids creating 4 separate model files; simple Y-axis rotation math on vertex positions
5. **Quads rebuilt on every resource reload** — `replaceCache()` runs on each reload; sprites may change, so quads are recreated
