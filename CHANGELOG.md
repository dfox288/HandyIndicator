# Changelog

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
