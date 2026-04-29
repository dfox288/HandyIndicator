# Handy Indicator - Future Ideas

Analysis from two Minecraft specialists (gameplay + aesthetics focus), February 2026.

---

## Convergent Ideas (Both Specialists Proposed Independently)

### 1. Fill-Level Gradient Indicator
Replace the binary has/hasn't items with a multi-stage fullness indicator (like a visual comparator). Color shifts from green (nearly empty) to red (full), or overlay thickness scales with capacity used. Server-side `IntegerProperty` (0-4 levels), client-side color interpolation via `BlockColorProvider`. The `ContainerStateHelper` already iterates inventory slots, so computing fullness percentage is straightforward.

### 2. Shulker Box Support
The biggest content gap — shulker boxes are THE endgame container and currently unsupported. 17 variants (16 dyed + default). Color-matching overlays that blend with the shulker's dye color for gorgeous storage walls. `ShulkerBoxBlockEntity` extends `RandomizableContainerBlockEntity` which implements `Container`, so existing `ContainerStateHelper.updateHasItems()` works as-is.

---

## Gameplay-Focused Proposals

### 3. Brewing Stand Indicators
Three-property system (`has_bottles`, `has_ingredient`, `has_fuel`) mirroring the furnace pattern. `BrewingStandBlockEntity` has slots 0-2 (bottles), slot 3 (ingredient), slot 4 (fuel/blaze powder). Follows exact same mixin pattern as existing furnace mixins. Brewing is central to survival gameplay.

### 4. Hopper Direction Flow Visualization
A directional arrow/tick on the overlay showing which way the hopper outputs. `HopperBlock` already has a `FACING` property (DOWN, NORTH, SOUTH, EAST, WEST). The `OverlayQuadFactory` could generate a small arrow quad on the face corresponding to the output direction. Huge QoL for sorting systems.

### 5. Trapped Chest Distinction
Visually distinct overlay for trapped vs regular chests (e.g., red-tinted border). Plus a "redstone active" state when the chest is being opened and emitting signal. The mod already has separate `chestEnabled` and `trappedChestEnabled` config flags. The "currently emitting signal" state is tracked by vanilla via `BlockStateProperties.POWER`.

### 6. Comparator Output Preview
Show the actual comparator signal strength (0-15) as a number overlay on containers. `AbstractContainerMenu.getRedstoneSignalFromContainer()` already exists in vanilla. Could use a texture atlas with digits 0-15, or encode as colored dots. Potentially the killer feature for technical players — no other mod does this as an in-world overlay.

### 7. Crafter Readiness Indicator
Beyond "has items" — show whether the crafter's grid matches a valid recipe (green = ready, amber = invalid layout). `CrafterBlockEntity` stores items in a grid and vanilla already validates recipes during triggering. `IntegerProperty` with values 0 (empty), 1 (has items, no valid recipe), 2 (recipe valid and ready).

---

## Aesthetic/Technical Proposals

### 8. Animated Pulse for Active Furnaces
A breathing/pulsing glow when furnaces are lit and smelting. Client-side `BlockColorProvider` modulates tint brightness based on `GameTime` sine wave — no new blockstate properties needed. Could also set non-zero `lightEmission` on overlay quads for self-illumination.

### 9. Per-Container-Type Color Themes
Each container type gets its own configurable color identity (hoppers amber, chests gold, barrels wood-brown). Assign each block type a unique tint index (0-11 for 12 supported types), register a `BlockColorProvider` that maps each to its configured color. Pure client-side enhancement, no new blockstate properties.

### 10. Overlay Shape Variants
Multiple indicator styles: Border (current), Dot, Corner Marks, X-Mark, Full Face. Alternative `createXxxOverlay()` methods in `OverlayQuadFactory` producing different quad arrangements. No competing mod offers this — a genuine differentiator. The "corners" style would look stunning in modern/futuristic builds.

### 11. Jade/WTHIT Integration
Soft-dependency plugin (~50 lines) that registers an `IBlockComponentProvider` feeding blockstate data into Jade tooltips. Zero extra network traffic since data is already synced via blockstates. Strategic for modpack adoption — Jade has 200M+ downloads.

### 12. Built-in Texture Style Options
Ship multiple sprite variants (solid, dashed, glow, pixel-art) selectable via config. The sprite lookup is already done via resource location — just parameterize the path suffix. Plus documentation for resource pack authors on sprite dimensions and format.

---

## Suggested Priority Tiers

| Tier | Proposals | Rationale |
|------|-----------|-----------|
| **High** | Shulker Boxes (#2), Brewing Stands (#3), Fill-Level Gradient (#1) | Fill obvious container gaps + most-requested feature type |
| **Medium** | Per-Container Colors (#9), Overlay Shapes (#10), Hopper Direction (#4) | Strong differentiation, mostly client-side work |
| **Lower** | Jade Integration (#11), Crafter Readiness (#7), Comparator Preview (#6), Animated Pulse (#8), Trapped Chest (#5), Textures (#12) | Nice-to-have, some technically ambitious |
