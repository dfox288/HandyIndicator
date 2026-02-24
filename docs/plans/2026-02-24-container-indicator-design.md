# Container Indicator - Design Document

## Summary

A Minecraft 1.21.1 datapack + resource pack combo that provides subtle visual feedback when hoppers, droppers, and dispensers contain items. A thin amber/copper edge highlight/stripe appears on the block when it has items inside, and disappears when empty.

## Goals

- Visual indicator for whether a container block (hopper, dropper, dispenser) has items
- Binary state: empty vs has-items (no fill-level granularity)
- Subtle aesthetic matching existing redstone utility resource packs (directional hoppers, locked hoppers)
- No text overlays, particles, or floating items - just a clean texture change
- Vanilla server compatible (no mods required)

## Visual Design

### Indicator Style
- Thin warm-toned stripe (amber/copper) along block edges
- Visible on both top and side faces for any-angle visibility

### Per-Block Placement
- **Hopper:** Stripe along the top rim of the wide opening + bottom edge of side faces
- **Dropper:** Stripe along the top edge of the face with the mouth + top face edges
- **Dispenser:** Stripe along the top edge of the face with the arrow + top face edges

### Color
- Warm amber/copper that complements the grey stone (dropper/dispenser) and dark iron (hopper) palettes
- Noticeable at a glance but not distracting

## Technical Architecture

### Approach: Block Display Entity Overlay

The real block is never modified. A `block_display` entity is summoned at the exact block position, rendering a custom model (the "filled" variant with the stripe texture). From the player's perspective, it looks like a seamless texture change.

### Resource Pack

Provides custom block models for the "filled" variant of each block:
- Identical to vanilla models but with edge stripe pixels added to the textures
- These models are referenced by block_display entities, not by placed blocks
- Vanilla blocks remain untouched; the overlay sits on top

**Files:**
```
resourcepack/
├── pack.mcmeta
└── assets/
    └── minecraft/
        ├── models/
        │   └── block/
        │       ├── hopper_filled.json
        │       ├── dropper_filled.json
        │       └── dispenser_filled.json
        └── textures/
            └── block/
                ├── hopper_top_filled.png
                ├── hopper_side_filled.png
                ├── dropper_front_filled.png
                ├── dispenser_front_filled.png
                └── (other face variants as needed)
```

### Datapack

Provides the detection and overlay management logic:

**Tick function (every 10 ticks / 0.5s):**
1. Find all hoppers/droppers/dispensers near players (within ~48 blocks)
2. For each, check `execute if items block ~ ~ ~ container.*`
3. If items present and no overlay entity exists: summon `block_display` with custom model
4. If empty and overlay entity exists: remove the entity

**Entity tracking:**
- Overlay entities are tagged (e.g., `container_indicator.overlay`)
- Additional tags identify the block type (`container_indicator.hopper`, etc.)
- Paired to their block position via the entity's coordinates

**Files:**
```
datapack/
├── pack.mcmeta
└── data/
    └── container_indicator/
        ├── function/
        │   ├── tick.mcfunction
        │   ├── scan_hoppers.mcfunction
        │   ├── scan_droppers.mcfunction
        │   ├── scan_dispensers.mcfunction
        │   ├── add_overlay.mcfunction
        │   └── remove_overlay.mcfunction
        └── tags/
            └── function/
                └── tick.json
```

### Performance

- Only scans blocks near players (configurable radius, default ~48 blocks)
- Staggered scanning: spreads checks across ticks to avoid lag spikes
- Entity cleanup on block break detection (overlay checks if its block still exists)
- Overhead is negligible for typical survival bases (20-50 containers)

## Non-Goals

- No fill-level indication (just empty vs has-items)
- No support for chests, barrels, furnaces, or other containers (scope limited to hopper/dropper/dispenser)
- No OptiFine/CIT dependency
- No mod loader requirement

## Dependencies

- Minecraft Java Edition 1.21.1+
- `execute if items` command (available since 1.20.5)
- `block_display` entities (available since 1.19.4)
