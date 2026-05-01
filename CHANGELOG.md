# Changelog

## 2.1.0-beta.3

### Breaking
- **Mod ID renamed from `container-indicator` to `handyindicator`** to match the rest of the Handy series convention. Existing config at `config/container-indicator.json` is migrated automatically on first launch — no settings lost.
- Internal package moved from `dev.containerindicator` to `dev.handy.mods.handyindicator`. No user-facing impact unless another mod was depending on internal classes.
- Asset namespace moved from `container_indicator:*` to `handyindicator:*`. Resource packs targeting the old namespace need to update.

### Bug fixes
- **Indicators now render again on 26.2-snapshot-5.** A 26.2 atlas-stitching change required the mod to declare its texture directory via a new atlases JSON; without it our overlay sprite silently fell back to the missing-texture sprite. Combined with two latent material-flag regressions (overlays routed through the translucent render bucket where the alphaless sprite was discarded, and `materialFlags` returned 0 so the chunk compiler skipped allocating a render bucket), no overlays reached the GPU. All three fixes ship together.
- **CrafterBlockMixin double `registerDefaultState` corrected** — the second call was overwriting the first's mutation. Worked by accident because both default to false, would have broken silently if either default ever flipped.
- **Hopper inventory no longer scanned every tick** — the per-tick `pushItemsTick` inject was redundant with `setItem`/`removeItem` and was scanning hundreds of inventories per second on busy farms.

### Changes
- **Config persistence rebuilt on YACL `ConfigClassHandler`** (matches the rest of the Handy suite). On-disk JSON shape unchanged; users without YACL installed still run on defaults. Dedicated servers no longer touch any YACL classes.
- **Config screen strings translatable** via `assets/handyindicator/lang/<locale>.json` — drop a JSON with the same keys to localize.
- **CI release workflow fixed for prerelease tags** — the grep that detects `-beta`/`-alpha`/`-rc` was failing silently because the regex started with `-`. The CurseForge step is also gated to stable releases (snapshot betas only ship to Modrinth + GitHub Releases).

### Internal
- Cleanup wave aligned this mod with the rest of the suite — JAVA_25 mixin compatibility level, 34 `@At` descriptors pinned across 18 mixins, mixin renamed `BlockModelShaperMixin` → `BlockStateModelSetMixin` to reflect its real target, `handyindicator$` prefix on every handler, log messages no longer carry the `[Handy Indicator]` prefix (logger name covers it), `isBlockEnabled` switched to a lazy `Map<Block, ConfigToggle>` dispatch from a 13-case if-ladder, package layout split into `client/` and `config/` subpackages, magic numbers named (RGB_MASK, ENDER_GLOW_PERIOD_MS), java.awt.Color dropped from the shared sourceset, redundant `@Environment(EnvType.CLIENT)` annotations cleaned up, exception handlers narrowed.

## 2.1.0-beta.1

- Preview build for Minecraft **26.2 snapshots** (tested against 26.2-snapshot-3)
- Rebuilt against Fabric API 0.146.1+26.2
- Adapted to two 26.2 refactors:
  - Copper chest variants consolidated into `WeatheringCopperCollection` — the 8-way `||` chain on copper-chest block identity now checks `Blocks.COPPER_CHEST.asList().contains(block)` instead
  - `LevelRenderer.allChanged()` replaced by `invalidateCompiledGeometry(level, options, camera, blockColors)` — the YACL save hook calls the new signature and skips the refresh when no level is loaded

## 2.0.2

- Update to Minecraft 26.1.2 compatibility
- Update Fabric Loader to 0.19.2, Fabric API to 0.146.1

## 2.0.1

- Update to Minecraft 26.1.1 compatibility
- Update Fabric Loader to 0.18.6, Fabric API to 0.145.3, YACL to 3.9.2

## 2.0.0

- Port to Minecraft 26.1 (Java 25, unobfuscated)
- Restore YACL config screen integration

## 2.0.0-beta.1

- Port to Minecraft 26.1-rc-1 (Java 25, unobfuscated)
- Restore YACL config screen integration
- Add CI/CD pipeline with automated Modrinth publishing
