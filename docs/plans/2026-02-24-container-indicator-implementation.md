# Container Indicator - Implementation Plan (Fabric Mod)

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a Fabric mod for Minecraft 1.21.11 that shows a subtle amber edge stripe on hoppers, droppers, and dispensers when they contain items, using mixin-injected blockstate properties.

**Architecture:** Fabric Mixins inject a `has_items` BooleanProperty into the three container block classes. Separate mixins on the block entity classes detect inventory changes and update the blockstate. The mod's bundled resource pack overrides the vanilla blockstate JSONs to map `has_items=true` to model variants with amber stripe textures.

**Tech Stack:** Java 21, Fabric Loader 0.18.1, Fabric API 0.139.5+1.21.11, Fabric Loom 1.14.10, Mojang official mappings, Gradle 8.14.

**Reference project:** `~/Development/handyshulkers` - copy build scaffolding patterns from there.

---

### Task 1: Scaffold Fabric Mod Project

Copy the build setup from `handyshulkers`, stripping out YACL/ModMenu deps and client source set (this mod doesn't need client-only code - it's all shared).

**Files:**
- Create: `build.gradle`
- Create: `gradle.properties`
- Create: `settings.gradle`
- Create: `src/main/java/dev/containerindicator/ContainerIndicator.java`
- Create: `src/main/resources/fabric.mod.json`
- Create: `src/main/resources/container-indicator.mixins.json`
- Copy: `gradle/` directory and `gradlew`/`gradlew.bat` from handyshulkers

**Step 1: Copy Gradle wrapper from handyshulkers**

```bash
cp -r ~/Development/handyshulkers/gradle ~/Development/container-indicator/
cp ~/Development/handyshulkers/gradlew ~/Development/container-indicator/
cp ~/Development/handyshulkers/gradlew.bat ~/Development/container-indicator/
```

**Step 2: Create gradle.properties**

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true

# Fabric Properties
# check these on https://fabricmc.net/develop/
minecraft_version=1.21.11
loader_version=0.18.1

# Mod Properties
mod_version=1.0.0
maven_group=dev.containerindicator
archives_base_name=container-indicator

# Dependencies
fabric_version=0.139.5+1.21.11

# Loom
loom_version=1.14.10
```

**Step 3: Create settings.gradle**

```groovy
// settings.gradle
pluginManagement {
    repositories {
        maven { url "https://maven.fabricmc.net/" }
        mavenCentral()
        gradlePluginPortal()
    }
}
```

**Step 4: Create build.gradle**

```groovy
// build.gradle
plugins {
    id 'fabric-loom' version "${loom_version}"
}

version = project.mod_version
group = project.maven_group

base {
    archivesName = project.archives_base_name
}

repositories {
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
}

processResources {
    inputs.property "version", project.version
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = 21
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}
```

> **Note:** No `splitEnvironmentSourceSets()` needed - this mod has no client-only code. All mixins operate on shared (server+client) classes.

**Step 5: Create fabric.mod.json**

```json
// src/main/resources/fabric.mod.json
{
    "schemaVersion": 1,
    "id": "container-indicator",
    "version": "${version}",
    "name": "Container Indicator",
    "description": "Shows a subtle amber stripe on hoppers, droppers, and dispensers when they contain items.",
    "authors": ["D.Fox"],
    "contact": {},
    "license": "MIT",
    "environment": "*",
    "entrypoints": {
        "main": [
            "dev.containerindicator.ContainerIndicator"
        ]
    },
    "mixins": [
        "container-indicator.mixins.json"
    ],
    "depends": {
        "fabricloader": ">=0.18.1",
        "minecraft": "~1.21.11",
        "java": ">=21",
        "fabric-api": "*"
    }
}
```

**Step 6: Create mixin config**

```json
// src/main/resources/container-indicator.mixins.json
{
    "required": true,
    "package": "dev.containerindicator.mixin",
    "compatibilityLevel": "JAVA_21",
    "mixins": [
        "HopperBlockMixin",
        "DispenserBlockMixin",
        "HopperBlockEntityMixin",
        "DispenserBlockEntityMixin"
    ],
    "injectors": {
        "defaultRequire": 1
    }
}
```

**Step 7: Create mod entrypoint**

```java
// src/main/java/dev/containerindicator/ContainerIndicator.java
package dev.containerindicator;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerIndicator implements ModInitializer {
    public static final String MOD_ID = "container-indicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final BooleanProperty HAS_ITEMS = BooleanProperty.create("has_items");

    @Override
    public void onInitialize() {
        LOGGER.info("[Container Indicator] Loaded!");
    }
}
```

> **Mojang mappings note:** `BooleanProperty` is at `net.minecraft.world.level.block.state.properties.BooleanProperty`, not `net.minecraft.state.property.BooleanProperty` (Yarn).

**Step 8: Generate sources and verify compilation**

```bash
cd ~/Development/container-indicator
./gradlew genSources
./gradlew build
```

Expected: BUILD SUCCESSFUL

**Step 9: Commit**

```bash
git add -A
git commit -m "feat: scaffold Fabric mod project for container indicator"
```

---

### Task 2: Add Block Mixins (has_items Blockstate Property)

**Files:**
- Create: `src/main/java/dev/containerindicator/mixin/HopperBlockMixin.java`
- Create: `src/main/java/dev/containerindicator/mixin/DispenserBlockMixin.java`

**IMPORTANT: Before writing any mixin, inspect the decompiled source.**

```bash
# After genSources, find and read:
# - HopperBlock.java: look for createBlockStateDefinition method
# - DispenserBlock.java: same
# - DropperBlock.java: check if it overrides createBlockStateDefinition or inherits
```

The method that registers blockstate properties is `createBlockStateDefinition` in Mojang mappings.

**Step 1: Write HopperBlockMixin**

```java
// src/main/java/dev/containerindicator/mixin/HopperBlockMixin.java
package dev.containerindicator.mixin;

import dev.containerindicator.ContainerIndicator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlock.class)
public abstract class HopperBlockMixin {

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void containerindicator$addHasItemsProperty(
            StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(ContainerIndicator.HAS_ITEMS);
    }
}
```

> **Verify:** Run `genSources`, open the decompiled `HopperBlock`, confirm it has `createBlockStateDefinition(StateDefinition.Builder<Block, BlockState>)`. Also note what other properties it registers (should be `ENABLED` and `FACING`).

**Step 2: Write DispenserBlockMixin**

```java
// src/main/java/dev/containerindicator/mixin/DispenserBlockMixin.java
package dev.containerindicator.mixin;

import dev.containerindicator.ContainerIndicator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin {

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void containerindicator$addHasItemsProperty(
            StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(ContainerIndicator.HAS_ITEMS);
    }
}
```

> **Critical check:** Inspect `DropperBlock.java` after genSources. If `DropperBlock` does NOT override `createBlockStateDefinition`, then it inherits from `DispenserBlock` and this mixin covers both. If it DOES override, you need a separate `DropperBlockMixin`.

**Step 3: Handle default state**

We need `has_items` to default to `false`. Inject into the constructor to update the default state after the super() call registers the property:

Add to HopperBlockMixin:
```java
@Inject(method = "<init>", at = @At("RETURN"))
private void containerindicator$setDefaultHasItems(CallbackInfo ci) {
    Block self = (Block)(Object)this;
    self.registerDefaultState(
        self.defaultBlockState().setValue(ContainerIndicator.HAS_ITEMS, false)
    );
}
```

> **Mojang mapping:** `registerDefaultState()` sets the default blockstate. `defaultBlockState()` gets the current default. `setValue()` sets a property value.

Add the same pattern to `DispenserBlockMixin`.

**Step 4: Verify compilation**

```bash
./gradlew build
```

**Step 5: Test in-game**

```bash
./gradlew runClient
```

1. Create a creative world
2. Place a hopper
3. Press F3 and look at the block - blockstate should show `has_items: false`
4. (Value won't toggle yet - that's Task 3)

**Step 6: Commit**

```bash
git add src/main/java/dev/containerindicator/mixin/
git commit -m "feat: add has_items blockstate property to hopper/dropper/dispenser via mixins"
```

---

### Task 3: Add Block Entity Mixins (Inventory Change Detection)

**Files:**
- Create: `src/main/java/dev/containerindicator/ContainerStateHelper.java`
- Create: `src/main/java/dev/containerindicator/mixin/HopperBlockEntityMixin.java`
- Create: `src/main/java/dev/containerindicator/mixin/DispenserBlockEntityMixin.java`

**IMPORTANT: Inspect decompiled sources first.**

```bash
# After genSources, examine:
# - HopperBlockEntity.java: find setItem() or setStack(), inventory field name
# - DispenserBlockEntity.java: same
# - Check what fires when hopper transfers items (static methods?)
```

**Step 1: Create ContainerStateHelper**

```java
// src/main/java/dev/containerindicator/ContainerStateHelper.java
package dev.containerindicator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class ContainerStateHelper {

    private ContainerStateHelper() {}

    public static void updateHasItems(BlockEntity entity, List<ItemStack> inventory) {
        Level level = entity.getLevel();
        BlockPos pos = entity.getBlockPos();

        if (level == null || level.isClientSide()) return;

        boolean hasItems = false;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                hasItems = true;
                break;
            }
        }

        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(ContainerIndicator.HAS_ITEMS)
                && state.getValue(ContainerIndicator.HAS_ITEMS) != hasItems) {
            level.setBlock(pos, state.setValue(ContainerIndicator.HAS_ITEMS, hasItems),
                Block.UPDATE_CLIENTS);
        }
    }
}
```

> **Mojang mappings:**
> - `getLevel()` (not `getWorld()`)
> - `getBlockPos()` (not `getPos()`)
> - `isClientSide()` (not `isClient()`)
> - `hasProperty()` (not `contains()`)
> - `getValue()` / `setValue()` (not `get()` / `with()`)
> - `setBlock()` (not `setBlockState()`)
> - `Block.UPDATE_CLIENTS` = flag 2, sends update to clients

**Step 2: Write HopperBlockEntityMixin**

Hook into `setItem` (Mojang mapping for the method that sets an item in a slot):

```java
// src/main/java/dev/containerindicator/mixin/HopperBlockEntityMixin.java
package dev.containerindicator.mixin;

import dev.containerindicator.ContainerStateHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Inject(method = "setItem", at = @At("TAIL"))
    private void containerindicator$updateHasItemsOnSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((HopperBlockEntity)(Object)this, this.items);
    }
}
```

> **Verify after genSources:**
> - The inventory field name (`items` vs `inventory`)
> - The method name for setting a slot (`setItem` is likely correct for Mojang mappings)
> - Whether `setItem` is defined on `HopperBlockEntity` directly or inherited from a parent class
> - If inherited, target the parent class instead, or use `remap = true` (default)

**Step 3: Write DispenserBlockEntityMixin**

```java
// src/main/java/dev/containerindicator/mixin/DispenserBlockEntityMixin.java
package dev.containerindicator.mixin;

import dev.containerindicator.ContainerStateHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlockEntity.class)
public abstract class DispenserBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Inject(method = "setItem", at = @At("TAIL"))
    private void containerindicator$updateHasItemsOnSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((DispenserBlockEntity)(Object)this, this.items);
    }
}
```

> **Note:** `DispenserBlockEntity` is used by both dispensers AND droppers. One mixin covers both.

**Step 4: Handle loading existing worlds**

When an existing world loads, containers with items need their blockstate set correctly. Hook into `loadAdditional` (Mojang mapping for reading NBT on load):

Add to both entity mixins:

```java
@Inject(method = "loadAdditional", at = @At("TAIL"))
private void containerindicator$updateStateOnLoad(
        net.minecraft.nbt.CompoundTag tag,
        net.minecraft.core.HolderLookup.Provider registries,
        CallbackInfo ci) {
    ContainerStateHelper.updateHasItems((HopperBlockEntity)(Object)this, this.items);
}
```

> **Verify:** The exact method signature of `loadAdditional` in 1.21.11. It may be `load`, `readNbt`, or `loadAdditional` depending on the mapping version. Check the decompiled source.

**Step 5: Test in-game**

```bash
./gradlew runClient
```

1. Place a hopper, F3 shows `has_items: false`
2. Put an item in the hopper - F3 should now show `has_items: true`
3. Remove all items - goes back to `has_items: false`
4. Repeat for dropper and dispenser
5. Build a hopper chain, flow items through - each hopper should toggle
6. Place a filled hopper, save/quit, reload - state should persist

**Step 6: Commit**

```bash
git add src/main/java/dev/containerindicator/
git commit -m "feat: detect inventory changes and update has_items blockstate"
```

---

### Task 4: Create Blockstate JSON Overrides

These files override vanilla blockstate JSONs to include the `has_items` property. Every vanilla variant is duplicated with `has_items=false` (vanilla model) and `has_items=true` (filled model).

**Files:**
- Create: `src/main/resources/assets/minecraft/blockstates/hopper.json`
- Create: `src/main/resources/assets/minecraft/blockstates/dropper.json`
- Create: `src/main/resources/assets/minecraft/blockstates/dispenser.json`

**IMPORTANT: Extract vanilla blockstate JSONs first to get exact rotation values.**

```bash
# Find the Minecraft jar in the Gradle cache:
find ~/.gradle/caches -name "minecraft-*-client-extra.jar" -path "*1.21.11*" 2>/dev/null
# OR: find ~/.gradle -name "*.jar" | grep "1.21.11"
# Extract vanilla blockstates:
# unzip -p <jar-path> assets/minecraft/blockstates/hopper.json
# unzip -p <jar-path> assets/minecraft/blockstates/dropper.json
# unzip -p <jar-path> assets/minecraft/blockstates/dispenser.json
```

**Step 1: Create hopper.json blockstate override**

Hopper has: `enabled` (true/false) × `facing` (down/north/south/west/east) = 10 vanilla variants → 20 with has_items.

The `enabled` property doesn't affect the model, and hopper has two model types:
- `hopper` (facing down)
- `hopper_side` (facing north/south/east/west, with y-rotation)

```json
// src/main/resources/assets/minecraft/blockstates/hopper.json
// IMPORTANT: Extract the vanilla file and add has_items variants.
// The structure below is the expected pattern - verify rotations against vanilla.
{
    "variants": {
        "enabled=false,facing=down,has_items=false": {"model": "minecraft:block/hopper"},
        "enabled=false,facing=down,has_items=true": {"model": "container_indicator:block/hopper_filled"},
        "enabled=false,facing=north,has_items=false": {"model": "minecraft:block/hopper_side"},
        "enabled=false,facing=north,has_items=true": {"model": "container_indicator:block/hopper_side_filled"},
        "enabled=false,facing=south,has_items=false": {"model": "minecraft:block/hopper_side", "y": 180},
        "enabled=false,facing=south,has_items=true": {"model": "container_indicator:block/hopper_side_filled", "y": 180},
        "enabled=false,facing=west,has_items=false": {"model": "minecraft:block/hopper_side", "y": 90},
        "enabled=false,facing=west,has_items=true": {"model": "container_indicator:block/hopper_side_filled", "y": 90},
        "enabled=false,facing=east,has_items=false": {"model": "minecraft:block/hopper_side", "y": 270},
        "enabled=false,facing=east,has_items=true": {"model": "container_indicator:block/hopper_side_filled", "y": 270},
        "enabled=true,facing=down,has_items=false": {"model": "minecraft:block/hopper"},
        "enabled=true,facing=down,has_items=true": {"model": "container_indicator:block/hopper_filled"},
        "enabled=true,facing=north,has_items=false": {"model": "minecraft:block/hopper_side"},
        "enabled=true,facing=north,has_items=true": {"model": "container_indicator:block/hopper_side_filled"},
        "enabled=true,facing=south,has_items=false": {"model": "minecraft:block/hopper_side", "y": 180},
        "enabled=true,facing=south,has_items=true": {"model": "container_indicator:block/hopper_side_filled", "y": 180},
        "enabled=true,facing=west,has_items=false": {"model": "minecraft:block/hopper_side", "y": 90},
        "enabled=true,facing=west,has_items=true": {"model": "container_indicator:block/hopper_side_filled", "y": 90},
        "enabled=true,facing=east,has_items=false": {"model": "minecraft:block/hopper_side", "y": 270},
        "enabled=true,facing=east,has_items=true": {"model": "container_indicator:block/hopper_side_filled", "y": 270}
    }
}
```

**Step 2: Create dropper.json blockstate override**

Dropper has: `facing` (all 6 directions) × `triggered` (true/false) = 12 vanilla variants → 24 with has_items.

```json
// src/main/resources/assets/minecraft/blockstates/dropper.json
// Extract vanilla dropper.json, duplicate each variant adding has_items=false/true.
// For has_items=false: use vanilla model (minecraft:block/dropper or minecraft:block/dropper_vertical)
// For has_items=true: use container_indicator:block/dropper_filled (or dropper_vertical_filled)
// VERIFY all x/y rotations from the vanilla file.
```

> The exact content depends on vanilla's rotation scheme. Extract and duplicate programmatically if needed.

**Step 3: Create dispenser.json blockstate override**

Same structure as dropper but with dispenser models. Dispenser has `facing` × `triggered` too.

```json
// src/main/resources/assets/minecraft/blockstates/dispenser.json
// Same pattern as dropper.json but with dispenser models.
```

**Step 4: Test - blocks should render (with missing textures for filled variants)**

```bash
./gradlew runClient
```

1. Place a hopper - should render normally
2. Put an item in - will show pink/black (missing texture for filled model) - that's expected
3. No crashes = blockstate JSON is valid

**Step 5: Commit**

```bash
git add src/main/resources/assets/minecraft/blockstates/
git commit -m "feat: add blockstate JSON overrides with has_items variants"
```

---

### Task 5: Create Filled Block Models

These models parent the vanilla models but swap in "filled" textures.

**Files:**
- Create: `src/main/resources/assets/container_indicator/models/block/hopper_filled.json`
- Create: `src/main/resources/assets/container_indicator/models/block/hopper_side_filled.json`
- Create: `src/main/resources/assets/container_indicator/models/block/dropper_filled.json`
- Create: `src/main/resources/assets/container_indicator/models/block/dispenser_filled.json`

**IMPORTANT: Extract vanilla model JSONs first to get exact texture variable names.**

```bash
# Extract from Minecraft jar:
# unzip -p <jar-path> assets/minecraft/models/block/hopper.json
# unzip -p <jar-path> assets/minecraft/models/block/hopper_side.json
# unzip -p <jar-path> assets/minecraft/models/block/dropper.json
# unzip -p <jar-path> assets/minecraft/models/block/dispenser.json
```

**Step 1: Create hopper_filled.json**

```json
// src/main/resources/assets/container_indicator/models/block/hopper_filled.json
{
    "parent": "minecraft:block/hopper",
    "textures": {
        "top": "container_indicator:block/hopper_top_filled",
        "side": "container_indicator:block/hopper_outside_filled"
    }
}
```

> **Verify texture variable names** by reading the vanilla `hopper.json` model. They might be `top`/`side`/`inside`/`particle` or different names.

**Step 2: Create hopper_side_filled.json**

```json
// src/main/resources/assets/container_indicator/models/block/hopper_side_filled.json
{
    "parent": "minecraft:block/hopper_side",
    "textures": {
        "top": "container_indicator:block/hopper_top_filled",
        "side": "container_indicator:block/hopper_outside_filled"
    }
}
```

**Step 3: Create dropper_filled.json**

```json
// src/main/resources/assets/container_indicator/models/block/dropper_filled.json
{
    "parent": "minecraft:block/dropper",
    "textures": {
        "front": "container_indicator:block/dropper_front_filled",
        "top": "container_indicator:block/dropper_top_filled"
    }
}
```

**Step 4: Create dispenser_filled.json**

```json
// src/main/resources/assets/container_indicator/models/block/dispenser_filled.json
{
    "parent": "minecraft:block/dispenser",
    "textures": {
        "front": "container_indicator:block/dispenser_front_filled",
        "top": "container_indicator:block/dispenser_top_filled"
    }
}
```

**Step 5: Commit**

```bash
git add src/main/resources/assets/container_indicator/models/
git commit -m "feat: add filled model variants for hopper/dropper/dispenser"
```

---

### Task 6: Create Filled Textures

**Files:**
- Create: `src/main/resources/assets/container_indicator/textures/block/hopper_top_filled.png`
- Create: `src/main/resources/assets/container_indicator/textures/block/hopper_outside_filled.png`
- Create: `src/main/resources/assets/container_indicator/textures/block/dropper_front_filled.png`
- Create: `src/main/resources/assets/container_indicator/textures/block/dropper_top_filled.png`
- Create: `src/main/resources/assets/container_indicator/textures/block/dispenser_front_filled.png`
- Create: `src/main/resources/assets/container_indicator/textures/block/dispenser_top_filled.png`

**Step 1: Extract vanilla textures**

```bash
# Find Minecraft jar in Gradle cache
find ~/.gradle/caches -name "*.jar" -path "*1.21.11*" | head -5

# Extract textures (adjust jar path):
# mkdir -p /tmp/vanilla_textures
# cd /tmp/vanilla_textures
# unzip -j <jar-path> assets/minecraft/textures/block/hopper_top.png
# unzip -j <jar-path> assets/minecraft/textures/block/hopper_outside.png
# unzip -j <jar-path> assets/minecraft/textures/block/dropper_front.png
# unzip -j <jar-path> assets/minecraft/textures/block/dispenser_front.png
# unzip -j <jar-path> assets/minecraft/textures/block/furnace_top.png  (dropper/dispenser top)
```

**Step 2: Create filled texture variants**

Copy each vanilla texture and add a 1-pixel amber (#D4881A) stripe to the edges:

**Programmatic approach (Python + Pillow):**

```python
from PIL import Image

AMBER = (212, 136, 26, 255)

def add_border(src_path, dst_path, edges="all"):
    img = Image.open(src_path).convert("RGBA")
    w, h = img.size
    if edges in ("all", "top"):
        for x in range(w):
            img.putpixel((x, 0), AMBER)
    if edges in ("all", "bottom"):
        for x in range(w):
            img.putpixel((x, h-1), AMBER)
    if edges in ("all",):
        for y in range(h):
            img.putpixel((0, y), AMBER)
            img.putpixel((w-1, y), AMBER)
    img.save(dst_path)

# Hopper top: border around the opening
add_border("hopper_top.png", "hopper_top_filled.png", "all")

# Hopper side: stripe along the bottom edge (where wide meets narrow, ~row 10)
img = Image.open("hopper_outside.png").convert("RGBA")
for x in range(16):
    img.putpixel((x, 9), AMBER)
    img.putpixel((x, 10), AMBER)
img.save("hopper_outside_filled.png")

# Dropper/Dispenser front: stripe along the top edge
add_border("dropper_front.png", "dropper_front_filled.png", "top")
add_border("dispenser_front.png", "dispenser_front_filled.png", "top")

# Dropper/Dispenser top: border
add_border("furnace_top.png", "dropper_top_filled.png", "all")
add_border("furnace_top.png", "dispenser_top_filled.png", "all")
```

> **Alternative:** Use GIMP, Aseprite, or Paint.net to manually edit the 16x16 textures. Or use ImageMagick one-liners.

**Step 3: Copy textures to the correct location**

```bash
# Copy generated PNGs to:
# src/main/resources/assets/container_indicator/textures/block/
```

**Step 4: Full end-to-end test**

```bash
./gradlew runClient
```

1. Place a hopper - looks normal (no stripe)
2. Put an item in - amber stripe appears!
3. Remove all items - stripe disappears
4. Check from above AND from the side - visible both ways
5. Repeat for dropper and dispenser
6. Hopper chain: items flowing through, stripes toggle correctly
7. Save, quit, reload - stripes persist on filled containers
8. Break a filled container - stripe gone (block destroyed naturally)

**Step 5: Visual polish iteration**

If the stripe is:
- Too bright: darken the amber or reduce to a subtler tone
- Too subtle: widen to 2 pixels or brighten
- Misaligned: check which pixel rows correspond to which geometry in the model
- Only visible from one angle: verify both `top` and `side` textures are being applied

**Step 6: Commit**

```bash
git add src/main/resources/assets/container_indicator/textures/
git commit -m "feat: add filled texture variants with amber edge stripe"
```

---

### Task 7: Package, Document, and Final Test

**Files:**
- Create: `README.md`
- Create: `LICENSE`

**Step 1: Build the mod jar**

```bash
./gradlew build
```

Output: `build/libs/container-indicator-1.0.0.jar`

**Step 2: Clean install test**

1. Fresh `.minecraft/mods/` folder with only Fabric API + container-indicator jar
2. Launch Minecraft via Fabric Loader
3. Create new world: place all 3 block types, fill/empty, verify stripes
4. Create an existing-style test: place hoppers with items, save, uninstall mod, reinstall, reload - should recover
5. Hopper chain test
6. Verify no log errors or warnings

**Step 3: Write README**

```markdown
# Container Indicator

A Fabric mod for Minecraft 1.21.11 that shows a subtle amber stripe on hoppers,
droppers, and dispensers when they contain items.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.11
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop both `fabric-api-*.jar` and `container-indicator-1.0.0.jar` into `.minecraft/mods/`
4. Launch Minecraft

## How It Works

- Hoppers, droppers, and dispensers now show a subtle amber edge stripe when they contain items
- The stripe disappears when the container is emptied
- Works automatically with no configuration needed

## Requirements

- Minecraft Java Edition 1.21.11
- Fabric Loader >= 0.18.1
- Fabric API
```

**Step 4: Commit**

```bash
git add README.md LICENSE
git commit -m "docs: add README and LICENSE"
```

---

## Implementation Notes

### Mojang Mappings Quick Reference

| Concept | Mojang Mapping | Yarn Equivalent |
|---------|---------------|-----------------|
| Register blockstate properties | `createBlockStateDefinition` | `appendProperties` |
| Set default state | `registerDefaultState` | `setDefaultState` |
| Get default state | `defaultBlockState` | `getDefaultState` |
| Get property value | `getValue` | `get` |
| Set property value | `setValue` | `with` |
| Set block in world | `setBlock` | `setBlockState` |
| Block position | `getBlockPos` | `getPos` |
| Get world/level | `getLevel` | `getWorld` |
| Is client side | `isClientSide` | `isClient` |
| Has property | `hasProperty` | `contains` |
| Set item in slot | `setItem` | `setStack` |
| Load NBT | `loadAdditional` | `readNbt` |
| Item list type | `NonNullList<ItemStack>` | `DefaultedList<ItemStack>` |
| Update flag | `Block.UPDATE_CLIENTS` | `Block.NOTIFY_LISTENERS` |

### Key Decompiled Classes to Inspect

Run `./gradlew genSources` then check:
- `net.minecraft.world.level.block.HopperBlock` - properties, createBlockStateDefinition
- `net.minecraft.world.level.block.DispenserBlock` - same
- `net.minecraft.world.level.block.DropperBlock` - check if it overrides createBlockStateDefinition
- `net.minecraft.world.level.block.entity.HopperBlockEntity` - setItem, inventory field name
- `net.minecraft.world.level.block.entity.DispenserBlockEntity` - same
- Vanilla blockstate JSONs + model JSONs in the jar

### DropperBlock Inheritance

DropperBlock extends DispenserBlock. If DropperBlock does NOT override `createBlockStateDefinition`, the DispenserBlockMixin covers both blocks automatically. Similarly, if dropper uses `DispenserBlockEntity`, the entity mixin covers both. Verify this after genSources.

### Texture Variable Names

The model JSONs reference textures by variable name (e.g., `"top"`, `"side"`, `"front"`). These names are defined in the vanilla model file. Extract the vanilla models to get the exact names before creating the filled variants. Wrong names = textures won't apply.

### Blockstate Variant Count

| Block | Vanilla Properties | Vanilla Variants | With has_items |
|-------|-------------------|-----------------|----------------|
| Hopper | enabled × facing(5) | 10 | 20 |
| Dropper | facing(6) × triggered | 12 | 24 |
| Dispenser | facing(6) × triggered | 12 | 24 |

Every variant must be listed. Missing variants = broken rendering.
