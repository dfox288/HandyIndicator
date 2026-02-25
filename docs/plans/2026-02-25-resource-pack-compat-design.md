# Resource Pack Compatibility — Programmatic Model Injection

**Date:** 2026-02-25
**Status:** Approved

## Problem

The mod overrides 19 blockstate JSON files in `assets/minecraft/blockstates/`. When a resource pack (e.g., Redstone Tweaks) provides its own blockstate JSON for the same block, one override wins and the other is lost — either our indicator overlays disappear, or the resource pack's visual changes are removed.

## Solution

Replace static blockstate JSON overrides with programmatic model injection using Fabric API's `ModelLoadingPlugin` and `modifyBlockModelAfterBake()`. Overlays are composited on top of whatever model is baked — vanilla or resource pack — at model loading time.

## Architecture

### New Files

- `src/client/java/dev/containerindicator/model/IndicatorModelPlugin.java` — `ModelLoadingPlugin` implementation that registers overlay injection
- `src/client/java/dev/containerindicator/model/CompositeBlockStateModel.java` — `BlockStateModel` wrapper that delegates to original model and appends overlay model parts
- 4 pre-rotated double chest overlay model JSONs in `assets/container_indicator/models/block/`:
  - `indicator_overlay_chest_double_north.json` (0 deg, same geometry as existing)
  - `indicator_overlay_chest_double_south.json` (180 deg rotation)
  - `indicator_overlay_chest_double_east.json` (90 deg rotation)
  - `indicator_overlay_chest_double_west.json` (270 deg rotation)

### Modified Files

- `ContainerIndicatorClient.java` — Add `ModelLoadingPlugin.register()` call

### Deleted Files

- All 19 blockstate JSON overrides in `src/main/resources/assets/minecraft/blockstates/`
- `indicator_overlay_chest_double.json` (replaced by 4 directional variants)

## How It Works

### Plugin Registration Flow

```
ContainerIndicatorClient.onInitializeClient()
  └─ ModelLoadingPlugin.register(IndicatorModelPlugin::new)

IndicatorModelPlugin.initialize(Context pluginContext)
  ├─ pluginContext.addModels(all overlay model IDs)
  └─ pluginContext.modifyBlockModelAfterBake().register(this::modifyModel)
```

### AfterBakeBlock Handler

For each block state being baked:

1. Check if the block is a target container block
2. Check the block state's custom properties (`has_items`, `has_input`, `has_fuel`)
3. If a property is `true`, determine the correct overlay model(s)
4. Wrap the original baked model in `CompositeBlockStateModel(original, overlay)`
5. For furnaces, both `has_input` and `has_fuel` can be true simultaneously — chain two wrappers

### CompositeBlockStateModel

```java
class CompositeBlockStateModel implements BlockStateModel {
    private final BlockStateModel original;
    private final BlockStateModel overlay;

    void addParts(Random random, List<BlockModelPart> parts) {
        original.addParts(random, parts);
        overlay.addParts(random, parts);
    }

    Sprite particleSprite() { return original.particleSprite(); }
}
```

### Overlay Selection Table

| Block Type | Condition | Overlay Model |
|---|---|---|
| Hopper, Dropper, Dispenser, Barrel, Crafter | `has_items=true` | `indicator_overlay` |
| Decorated Pot | `has_items=true` | `indicator_overlay_pot` |
| Furnace, Blast Furnace, Smoker | `has_input=true` | `indicator_overlay` |
| Furnace, Blast Furnace, Smoker | `has_fuel=true` | `indicator_overlay_bottom` |
| Chest family (type=single) | `has_items=true` | `indicator_overlay_chest` |
| Chest family (type=left, facing=north) | `has_items=true` | `indicator_overlay_chest_double_north` |
| Chest family (type=left, facing=south) | `has_items=true` | `indicator_overlay_chest_double_south` |
| Chest family (type=left, facing=east) | `has_items=true` | `indicator_overlay_chest_double_east` |
| Chest family (type=left, facing=west) | `has_items=true` | `indicator_overlay_chest_double_west` |

"Chest family" includes: chest, trapped_chest, copper_chest, exposed_copper_chest, weathered_copper_chest, oxidized_copper_chest, waxed_copper_chest, waxed_exposed_copper_chest, waxed_weathered_copper_chest, waxed_oxidized_copper_chest.

### Pre-rotated Double Chest Models

The existing `indicator_overlay_chest_double.json` has geometry for north-facing. Create 3 additional model JSONs with all cube element coordinates rotated by 90, 180, and 270 degrees around the Y axis (center at 8,8,8).

## Key Design Decisions

1. **AfterBakeBlock over OnLoadBlock** — Simpler API, operates on fully baked models, doesn't need to understand unbaked model internals
2. **Pre-rotated model JSONs over runtime rotation** — Simple and reliable; only 4 files needed, avoids low-level quad manipulation
3. **Composite wrapper over model replacement** — Preserves the original model from any source (vanilla or resource pack), just adds parts on top
4. **Overlay model JSONs stay** — The 5 overlay model definitions are still needed; only the 19 blockstate override JSONs are removed
