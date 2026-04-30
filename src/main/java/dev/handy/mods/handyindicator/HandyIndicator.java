package dev.handy.mods.handyindicator;

import dev.handy.mods.handyindicator.config.HandyIndicatorConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandyIndicator implements ModInitializer {
    public static final String MOD_ID = "handyindicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final BooleanProperty HAS_ITEMS = BooleanProperty.create("has_items");
    public static final BooleanProperty HAS_INPUT = BooleanProperty.create("has_input");
    public static final BooleanProperty HAS_FUEL = BooleanProperty.create("has_fuel");
    public static final BooleanProperty HAS_ITEMS_READY = BooleanProperty.create("has_items_ready");

    /** Mask that drops the alpha byte from a packed ARGB int — config stores RGB only,
     *  but YACL's color picker hands back full ARGB. AND with this on every save. */
    public static final int RGB_MASK = 0x00FFFFFF;

    private static final int CHUNKS_PER_TICK = 10;
    /**
     * Pending chunk refreshes, processed in batches of {@link #CHUNKS_PER_TICK} per server tick.
     *
     * <p>Thread-safety: this queue is only ever touched from the server thread —
     * {@code ServerChunkEvents.CHUNK_LOAD} fires on the server thread, and
     * {@code processRefreshQueue} runs from {@code ServerTickEvents.END_SERVER_TICK}
     * which is also server-thread-only. {@link ArrayDeque} therefore needs no external
     * synchronization. Do not hand a reference to this queue to any code that might
     * touch it from a worker thread or off the server tick.
     */
    private static final Queue<LevelChunk> pendingRefresh = new ArrayDeque<>();
    private static boolean refreshQueued = false;

    @Override
    public void onInitialize() {
        HandyIndicatorConfig.load();

        // Queue all loaded chunks for gradual refresh after server starts
        ServerTickEvents.END_SERVER_TICK.register(this::processRefreshQueue);

        // Queue newly loaded chunks for refresh (handles player login/teleport)
        ServerChunkEvents.CHUNK_LOAD.register((ServerLevel level, LevelChunk chunk, boolean isNewChunk) -> {
            pendingRefresh.add(chunk);
        });

        // Reset state on server stop (integrated server can restart within same JVM)
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            refreshQueued = false;
            pendingRefresh.clear();
        });

        LOGGER.info("Loaded!");
    }

    private void processRefreshQueue(MinecraftServer server) {
        if (!refreshQueued) {
            // First tick: collect all chunks to refresh
            refreshQueued = true;
            for (ServerLevel level : server.getAllLevels()) {
                level.getChunkSource().chunkMap.forEachReadyToSendChunk(pendingRefresh::add);
            }
            if (!pendingRefresh.isEmpty()) {
                LOGGER.info("Refreshing {} chunks...", pendingRefresh.size());
            }
        }

        // Process a batch each tick
        for (int i = 0; i < CHUNKS_PER_TICK && !pendingRefresh.isEmpty(); i++) {
            ContainerStateHelper.refreshChunk(pendingRefresh.poll());
        }
    }

    public static int getIndicatorColor() {
        return HandyIndicatorConfig.get().indicatorColor & RGB_MASK;
    }

    public static int getFuelColor() {
        return HandyIndicatorConfig.get().fuelColor & RGB_MASK;
    }

    public static int getReadyColor() {
        return HandyIndicatorConfig.get().crafterReadyColor & RGB_MASK;
    }

    /** Reads the per-block toggle flag from a config snapshot. */
    @FunctionalInterface
    private interface ConfigToggle {
        boolean enabled(HandyIndicatorConfig config);
    }

    /**
     * Map from exact vanilla block instance to its config toggle. Initialized lazily
     * from {@link #exactToggles()} on first call, after vanilla {@code Blocks} has
     * finished bootstrap (we'd NPE if this ran during static init since the class can
     * load before vanilla registries do).
     */
    private static volatile Map<Block, ConfigToggle> EXACT_TOGGLES;

    /**
     * Modded-block fallback rules: instanceof check + toggle. Iterated when a block
     * isn't in the exact-block map. Order matters only weakly here — none of these
     * vanilla parent classes overlap. Final fallback ({@code true}) keeps any
     * unknown block enabled because reaching this code at all means the block has
     * one of our injected blockstate properties, which means a mixin already
     * targeted it.
     */
    private static final Map<Predicate<Block>, ConfigToggle> FALLBACK_TOGGLES = Map.of(
            b -> b instanceof BarrelBlock,         c -> c.barrelEnabled,
            b -> b instanceof ChestBlock,          c -> c.chestEnabled,
            b -> b instanceof AbstractFurnaceBlock, c -> c.furnaceEnabled,
            b -> b instanceof DispenserBlock,      c -> c.dispenserEnabled,
            b -> b instanceof HopperBlock,         c -> c.hopperEnabled,
            b -> b instanceof ShulkerBoxBlock,     c -> c.shulkerBoxEnabled);

    private static Map<Block, ConfigToggle> exactToggles() {
        Map<Block, ConfigToggle> m = EXACT_TOGGLES;
        if (m != null) return m;
        synchronized (HandyIndicator.class) {
            if (EXACT_TOGGLES != null) return EXACT_TOGGLES;
            Map<Block, ConfigToggle> built = new HashMap<>();
            built.put(Blocks.HOPPER,        c -> c.hopperEnabled);
            built.put(Blocks.DISPENSER,     c -> c.dispenserEnabled);
            built.put(Blocks.DROPPER,       c -> c.dropperEnabled);
            built.put(Blocks.BARREL,        c -> c.barrelEnabled);
            built.put(Blocks.CRAFTER,       c -> c.crafterEnabled);
            built.put(Blocks.FURNACE,       c -> c.furnaceEnabled);
            built.put(Blocks.BLAST_FURNACE, c -> c.blastFurnaceEnabled);
            built.put(Blocks.SMOKER,        c -> c.smokerEnabled);
            built.put(Blocks.DECORATED_POT, c -> c.decoratedPotEnabled);
            built.put(Blocks.CHEST,         c -> c.chestEnabled);
            built.put(Blocks.TRAPPED_CHEST, c -> c.trappedChestEnabled);
            // 26.2 consolidated copper variants into a WeatheringCopperCollection;
            // asList() yields all 8 (4 weather stages × waxed/unwaxed).
            for (Block copper : Blocks.COPPER_CHEST.asList()) {
                built.put(copper, c -> c.copperChestEnabled);
            }
            EXACT_TOGGLES = Map.copyOf(built);
            return EXACT_TOGGLES;
        }
    }

    public static boolean isBlockEnabled(Block block) {
        HandyIndicatorConfig config = HandyIndicatorConfig.get();
        if (!config.enabled) return false;

        ConfigToggle exact = exactToggles().get(block);
        if (exact != null) return exact.enabled(config);

        // Modded blocks extending vanilla classes — check by inheritance.
        for (Map.Entry<Predicate<Block>, ConfigToggle> rule : FALLBACK_TOGGLES.entrySet()) {
            if (rule.getKey().test(block)) return rule.getValue().enabled(config);
        }

        return true;
    }
}
