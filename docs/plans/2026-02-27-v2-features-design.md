# Handy Indicator v2.0 Feature Design

**Date:** 2026-02-27
**Features:** Shulker Box Support, Fill-Level Gradient, Overlay Shapes, Per-Container Colors

---

## 1. Blockstate Properties

### Simple Containers — `FILL_LEVEL` replaces `HAS_ITEMS`

All simple containers switch from `HAS_ITEMS` (BooleanProperty) to `FILL_LEVEL` (IntegerProperty 0-4):

| Level | Fill Range | Visual |
|-------|-----------|--------|
| 0 | Empty | No overlay |
| 1 | 1-25% | Low fill (green) |
| 2 | 26-50% | Mid fill (yellow) |
| 3 | 51-75% | High fill (orange) |
| 4 | 76-100% | Full (red) |

Affected blocks: barrel, chest, trapped chest, copper chest, shulker box, hopper, dispenser, dropper, decorated pot, crafter.

**Furnaces are unchanged** — keep existing `HAS_INPUT` + `HAS_FUEL` booleans.

Fill percentage = `occupiedSlots / totalSlots`, mapped to levels:
- 0 slots occupied → level 0
- 1-25% → level 1
- 26-50% → level 2
- 51-75% → level 3
- 76-100% → level 4

The existing chunk refresh self-heal mechanism ensures worlds update automatically on load.

### Double Chests

For double chests, fill level is computed across both halves combined. Both halves display the same fill level (same as current unified `HAS_ITEMS` behavior).

---

## 2. Shulker Box Support

New mixins following the barrel pattern:

- `ShulkerBoxBlockMixin` — injects `FILL_LEVEL` property into blockstate definition
- `ShulkerBoxBlockEntityMixin` — hooks `setItem`/`removeItem` to call `ContainerStateHelper.updateFillLevel()`

Covers all 17 variants (16 dyed + undyed). `ShulkerBoxBlock` is the single class for all variants.

Client-side: `BlockModelShaperMixin` adds a case for `instanceof ShulkerBoxBlock`. Uses standard overlay geometry (shulker boxes are full-block when closed). The `ContainerIndicatorClient` dynamic registration loop already picks up any block with our injected properties.

Config: `shulkerBoxEnabled` toggle (default `true`).

---

## 3. Overlay Shape Variants

New enum `OverlayShape`:
- `BORDER` — current 1px border strip around top edge + side strips (default)
- `CORNERS` — small L-shaped marks at each corner of top face + short corner side strips
- `DOT` — single centered quad on top face (~4x4 pixels), no side strips

### New OverlayQuadFactory Methods

`createCornersOverlay(sprite, tintIndex)`:
- 4 L-shaped quads on top face (3px arm length at each corner)
- 4 short side strips at corners only

`createDotOverlay(sprite, tintIndex)`:
- 1 centered quad on top face (6,16.01,6 to 10,16.02,10)
- No side strips

Each existing overlay variant (standard, bottom, pot, chest, double chest) gets corners/dot equivalents:
- `createCornersBottomOverlay`, `createDotBottomOverlay`
- `createCornersPotOverlay`, `createDotPotOverlay`
- `createCornersChestOverlay`, `createDotChestOverlay`
- `createCornersDoubleChestOverlay`, `createDotDoubleChestOverlay`

`BlockModelShaperMixin.onReplaceCache()` reads config shape and builds appropriate quad sets.

Shape change requires F3+T resource reload (standard for visual config in MC mods).

---

## 4. Per-Container-Type Colors

### Config Fields

Each container type gets an optional color override. Value `-1` means "use global color":

```json
{
  "indicatorColor": "0x6FA9B4",
  "fuelColor": "0xCF8261",
  "hopperColor": -1,
  "barrelColor": -1,
  "chestColor": -1,
  "trappedChestColor": -1,
  "copperChestColor": -1,
  "shulkerBoxColor": -1,
  "dispenserColor": -1,
  "dropperColor": -1,
  "crafterColor": -1,
  "decoratedPotColor": -1,
  "furnaceInputColor": -1,
  "furnaceFuelColor": -1
}
```

### BlockColorProvider Resolution

The `BlockColorProvider` registered in `ContainerIndicatorClient` resolves color using this priority:

**For tintIndex 0 (item/fill indicator):**
1. If `useGradientColors` is true AND block has `FILL_LEVEL`: interpolate `fillLowColor` → `fillHighColor` based on level 1-4
2. Else if per-type color is set (not `-1`): use per-type color
3. Else: use global `indicatorColor`

**For tintIndex 1 (fuel):**
1. If `furnaceFuelColor` is set (not `-1`): use it
2. Else: use global `fuelColor`

The color provider already receives `BlockState` so it can determine block type and fill level without additional state.

---

## 5. Fill-Level Gradient Colors

Two new config fields:
- `fillLowColor` (default `0x4CAF50` — green)
- `fillHighColor` (default `0xF44336` — red)

Toggle: `useGradientColors` (default `true`)

Color interpolation for levels 1-4:
- Level 1: `fillLowColor` (green)
- Level 2: 33% blend toward `fillHighColor` (yellow-green)
- Level 3: 67% blend toward `fillHighColor` (orange)
- Level 4: `fillHighColor` (red)

Linear interpolation per RGB channel: `result = low + (high - low) * t` where `t = (level - 1) / 3.0`.

When `useGradientColors` is false, all fill levels use the per-type/global solid color (overlay still appears/disappears based on level > 0).

---

## 6. Full Config Structure

```json
{
  "enabled": true,
  "overlayShape": "BORDER",
  "useGradientColors": true,
  "fillLowColor": "0x4CAF50",
  "fillHighColor": "0xF44336",
  "indicatorColor": "0x6FA9B4",
  "fuelColor": "0xCF8261",
  "hopperColor": -1,
  "barrelColor": -1,
  "chestColor": -1,
  "trappedChestColor": -1,
  "copperChestColor": -1,
  "shulkerBoxColor": -1,
  "dispenserColor": -1,
  "dropperColor": -1,
  "crafterColor": -1,
  "decoratedPotColor": -1,
  "furnaceInputColor": -1,
  "furnaceFuelColor": -1,
  "hopperEnabled": true,
  "dispenserEnabled": true,
  "dropperEnabled": true,
  "barrelEnabled": true,
  "crafterEnabled": true,
  "furnaceEnabled": true,
  "blastFurnaceEnabled": true,
  "smokerEnabled": true,
  "decoratedPotEnabled": true,
  "chestEnabled": true,
  "trappedChestEnabled": true,
  "copperChestEnabled": true,
  "shulkerBoxEnabled": true
}
```

---

## 7. Files Changed

### New Files
- `src/main/java/dev/containerindicator/mixin/ShulkerBoxBlockMixin.java`
- `src/main/java/dev/containerindicator/mixin/ShulkerBoxBlockEntityMixin.java`
- `src/main/java/dev/containerindicator/OverlayShape.java` (enum)

### Modified Files
- `ContainerIndicator.java` — replace `HAS_ITEMS` with `FILL_LEVEL`, add `getColorForBlock()` helper
- `ContainerIndicatorConfig.java` — new color fields, shape enum, gradient toggle
- `ContainerStateHelper.java` — `updateFillLevel()` replaces `updateHasItems()`, compute fill percentage
- `BlockModelShaperMixin.java` — check `FILL_LEVEL > 0` instead of `HAS_ITEMS`, shape selection, shulker box case
- `OverlayQuadFactory.java` — `createCornersXxx()` and `createDotXxx()` methods
- `ContainerIndicatorClient.java` — enhanced `BlockColorProvider` with gradient + per-type color resolution
- All existing block mixins — switch from `HAS_ITEMS` to `FILL_LEVEL`
- `container-indicator.mixins.json` — add shulker box mixins
