# Handy Indicator - Fabric Mod

## Project Overview

A Fabric mod that shows subtle visual indicators on container blocks (chests, hoppers, dispensers, droppers, barrels, crafters, furnaces, decorated pots, copper chests) when they contain items. Fully configurable with per-block toggles and custom indicator colors via YACL.

## Tech Stack

- **Minecraft**: 26.1-rc-1 (first unobfuscated version — no mappings needed)
- **Fabric Loader**: 0.18.4
- **Fabric API**: 0.143.14+26.1
- **Fabric Loom**: 1.15.5
- **Java**: 25
- **YACL**: 3.9.0+26.1-fabric (soft dependency, via Modrinth Maven `maven.modrinth:yacl`)
- **ModMenu**: 18.0.0-alpha.6 (soft dependency)

## Build

```bash
cd /Users/dfox/Development/minecraft/HandyIndicator && ./gradlew build
cd /Users/dfox/Development/minecraft/HandyIndicator && ./gradlew runClient
cd /Users/dfox/Development/minecraft/HandyIndicator && ./gradlew genSources
```

Always prefix commands with `cd /path &&` so they auto-approve via permission rules.

## Project Structure

Uses `splitEnvironmentSourceSets()`:
- `src/main/` — shared code: blockstate properties, mixins, config, state helper
- `src/client/` — client-only: render layers, color providers, config screen, ModMenu integration

Package: `dev.containerindicator`

## Dependencies

YACL and ModMenu are **soft dependencies** — `compileOnly`/`localRuntime` in build.gradle. The mod works without them. ModMenu integration checks `FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")` at runtime before wiring the config screen.

Maven repos:
- `https://maven.terraformersmc.com/` — ModMenu
- `https://api.modrinth.com/maven` — YACL (artifact: `maven.modrinth:yacl`)

## Config

YACL config screen with:
- **General**: master toggle, indicator color, fuel indicator color
- **Blocks**: per-block toggles grouped by category (standard containers, chests, furnaces)

Config class: `ContainerIndicatorConfig` — JSON file at `config/container-indicator.json`

On config save, the mod forces chunk re-renders and refreshes all container blockstates.

## Key Architecture

- Custom blockstate properties (`HAS_ITEMS`, `HAS_INPUT`) injected via mixins
- `ContainerStateHelper` evaluates container contents and updates blockstates
- Overlay models rendered via `BlockModelShaperMixin` for resource pack compatibility
- Block color providers tint indicator overlays based on config colors
