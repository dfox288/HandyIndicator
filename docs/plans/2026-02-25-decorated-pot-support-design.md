# Decorated Pot Indicator Support

## Context

Add `has_items` indicator support to Decorated Pot blocks, following the established pattern used by hoppers, barrels, dispensers, etc.

## Key Differences from Existing Blocks

- `DecoratedPotBlockEntity` extends `BlockEntity` directly, NOT `BaseContainerBlockEntity`
- Uses `ContainerSingleItem` interface with a single `ItemStack` field (not `NonNullList<ItemStack>`)
- `BaseContainerBlockEntityMixin` does NOT cover it — needs its own entity mixin
- Must hook `setTheItem()` and `splitTheItem()` instead of `setItem()`/`removeItem()`
- Rendered via BlockEntityRenderer, but multipart overlay still works (same as other BER blocks)

## Pot Shape (block-space)

```
Y=1.31  [5/16 to 11/16]  neck lip
Y=1.06  [4/16 to 12/16]  neck base
Y=1.00  [1/16 to 15/16]  top cap
Y=0-1   [1/16 to 15/16]  main body
Y=0.00  [1/16 to 15/16]  bottom cap
```

## Implementation

### Server Side
- `DecoratedPotBlockMixin` — inject `HAS_ITEMS` property, set default false
- `DecoratedPotBlockEntityMixin` — hook `setTheItem()`, `splitTheItem()`, `loadAdditional()`

### Client Side
- `indicator_overlay_pot.json` — custom overlay model tracing the pot's top rim at 1/16 inset
- `decorated_pot.json` blockstate override — multipart with conditional overlay
- Register CUTOUT render layer and color provider for `Blocks.DECORATED_POT`

### Config
- Add `decoratedPotEnabled` toggle to config, YACL screen, and `isBlockEnabled()`

### Files

| File | Action |
|------|--------|
| `src/main/java/.../mixin/DecoratedPotBlockMixin.java` | Create |
| `src/main/java/.../mixin/DecoratedPotBlockEntityMixin.java` | Create |
| `src/main/resources/assets/container_indicator/models/block/indicator_overlay_pot.json` | Create |
| `src/main/resources/assets/minecraft/blockstates/decorated_pot.json` | Create |
| `src/main/java/.../ContainerIndicator.java` | Edit — add DECORATED_POT to isBlockEnabled |
| `src/main/java/.../ContainerIndicatorConfig.java` | Edit — add decoratedPotEnabled |
| `src/client/java/.../ContainerIndicatorClient.java` | Edit — register render layer + color |
| `src/client/java/.../ContainerIndicatorConfigScreen.java` | Edit — add toggle |
| `src/main/resources/container-indicator.mixins.json` | Edit — register new mixins |
