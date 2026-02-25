# Handy Indicator

A Fabric mod for Minecraft 1.21.11 that shows a subtle visual indicator on container blocks when they contain items.

At a glance, see which hoppers are loaded, which chests have loot, and which furnaces are fueled — without opening anything.

## Features

- **Visual overlay indicators** — A colored border appears on containers that have items inside.
- **Wide block support** — Chests, trapped chests, copper chests (all variants), hoppers, dispensers, droppers, barrels, crafters, furnaces, blast furnaces, smokers, and decorated pots.
- **Double chest awareness** — Double chests show a single unified indicator spanning both halves.
- **Furnace detail** — Furnaces show separate indicators for input and fuel slots with distinct colors.
- **Per-block toggles** — Enable or disable indicators for each block type individually.
- **Custom colors** — Choose your own indicator and fuel colors via the config screen.
- **Configurable** — All settings accessible via YACL config screen or JSON file.

## Requirements

- Minecraft Java Edition 1.21.11
- [Fabric Loader](https://fabricmc.net/use/installer/) 0.18.1+
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.139.5+

### Optional (for config screen)

- [ModMenu](https://modrinth.com/mod/modmenu) — adds a Configure button in the mod list
- [YACL](https://modrinth.com/mod/yacl) — powers the in-game config screen

Without these, all features work with sensible defaults. You can also edit `config/container-indicator.json` manually.

## Installation

### Single Player

1. Install Fabric Loader for Minecraft 1.21.11
2. Download Fabric API and place it in your `mods/` folder
3. Download Handy Indicator and place it in your `mods/` folder
4. Launch the game!

### Server

The mod is required on **both the server and all connecting clients**.

**Server setup:**
1. Install Fabric Loader on your server
2. Place Fabric API and Handy Indicator in the server's `mods/` folder
3. Start the server

**Client setup:**
1. Each player needs Fabric Loader, Fabric API, and Handy Indicator installed
2. Players without the mod will not see container indicators

## Building from Source

```bash
git clone https://github.com/dfox288/HandyIndicator.git
cd HandyIndicator

./gradlew build
# The compiled JAR will be in build/libs/
```

## Development

```bash
# Generate Minecraft sources for reference
./gradlew genSources

# Run Minecraft with the mod loaded
./gradlew runClient
```

## License

MIT License — see [LICENSE](LICENSE) for details.
