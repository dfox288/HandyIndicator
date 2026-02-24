# Container Indicator - Design Document

## Summary

A Minecraft 1.21.11 Fabric mod that provides subtle visual feedback when hoppers, droppers, and dispensers contain items. A thin amber/copper edge highlight/stripe appears on the block when it has items inside, and disappears when empty.

## Goals

- Visual indicator for whether a container block (hopper, dropper, dispenser) has items
- Binary state: empty vs has-items (no fill-level granularity)
- Subtle aesthetic matching existing redstone utility resource packs (directional hoppers, locked hoppers)
- No text overlays, particles, or floating items - just a clean texture change
- Single `.jar` installation - textures bundled inside the mod

## Visual Design

### Indicator Style
- Thin warm-toned stripe (amber/copper) along block edges
- Visible on both top and side faces for any-angle visibility

### Per-Block Placement
- **Hopper:** Stripe along the top rim of the wide opening + bottom edge of side faces
- **Dropper:** Stripe along the top edge of the face with the mouth + top face edges
- **Dispenser:** Stripe along the top edge of the face with the arrow + top face edges

### Color
- Warm amber/copper (#D4881A) that complements the grey stone (dropper/dispenser) and dark iron (hopper) palettes
- Noticeable at a glance but not distracting

## Technical Architecture

### Approach: Mixin-injected Blockstate Property

Use Fabric Mixins to add a `has_items` BooleanProperty to HopperBlock, DropperBlock, and DispenserBlock. When the inventory changes, the blockstate updates and Minecraft's standard rendering pipeline shows the correct model variant - no entities, no tick scanning.

### Event Flow

```
Item enters/leaves container
  -> BlockEntity.setChanged() is called
  -> Our mixin checks: are there any items?
  -> Updates blockstate: has_items=true/false
  -> Minecraft re-renders the block with the correct model
  -> Player sees stripe appear/disappear
```

### Project Structure

```
container-indicator/
├── build.gradle
├── gradle.properties
├── settings.gradle
└── src/main/
    ├── java/dev/containerindicator/
    │   ├── ContainerIndicator.java             # Mod entrypoint
    │   ├── ContainerStateHelper.java           # Shared blockstate update logic
    │   └── mixin/
    │       ├── HopperBlockMixin.java           # Add has_items property
    │       ├── DispenserBlockMixin.java         # Add has_items property (covers dropper)
    │       ├── HopperBlockEntityMixin.java      # Update state on inventory change
    │       └── DispenserBlockEntityMixin.java   # Update state on inventory change
    └── resources/
        ├── fabric.mod.json
        ├── container-indicator.mixins.json
        └── assets/minecraft/
            ├── blockstates/
            │   ├── hopper.json
            │   ├── dropper.json
            │   └── dispenser.json
            ├── models/block/
            │   ├── hopper_filled.json
            │   ├── hopper_side_filled.json
            │   ├── dropper_filled.json
            │   └── dispenser_filled.json
            └── textures/block/
                ├── hopper_top_filled.png
                ├── hopper_outside_filled.png
                ├── dropper_front_filled.png
                ├── dropper_top_filled.png
                ├── dispenser_front_filled.png
                └── dispenser_top_filled.png
```

### Performance

- Zero tick overhead - purely event-driven
- Blockstate changes only fire when inventory actually changes
- Standard Minecraft block rendering - no extra entities

## Non-Goals

- No fill-level indication (just empty vs has-items)
- No support for chests, barrels, furnaces, or other containers
- No vanilla/datapack fallback (Fabric required)

## Dependencies

- Minecraft Java Edition 1.21.11
- Fabric Loader >= 0.18.1
- Fabric API
