package dev.containerindicator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayDeque;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerIndicator implements ModInitializer {
    public static final String MOD_ID = "container-indicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final BooleanProperty HAS_ITEMS = BooleanProperty.create("has_items");
    public static final BooleanProperty HAS_INPUT = BooleanProperty.create("has_input");
    public static final BooleanProperty HAS_FUEL = BooleanProperty.create("has_fuel");

    private static final int CHUNKS_PER_TICK = 10;
    private static final Queue<LevelChunk> pendingRefresh = new ArrayDeque<>();
    private static boolean refreshQueued = false;

    @Override
    public void onInitialize() {
        ContainerIndicatorConfig.load();

        // Queue all loaded chunks for gradual refresh after server starts
        ServerTickEvents.END_SERVER_TICK.register(this::processRefreshQueue);

        LOGGER.info("[Handy Indicator] Loaded!");
    }

    private void processRefreshQueue(MinecraftServer server) {
        if (!refreshQueued) {
            // First tick: collect all chunks to refresh
            refreshQueued = true;
            for (ServerLevel level : server.getAllLevels()) {
                level.getChunkSource().chunkMap.forEachReadyToSendChunk(pendingRefresh::add);
            }
            if (!pendingRefresh.isEmpty()) {
                LOGGER.info("[Handy Indicator] Refreshing {} chunks...", pendingRefresh.size());
            }
        }

        // Process a batch each tick
        for (int i = 0; i < CHUNKS_PER_TICK && !pendingRefresh.isEmpty(); i++) {
            ContainerStateHelper.refreshChunk(pendingRefresh.poll());
        }
    }

    public static int getIndicatorColor() {
        return ContainerIndicatorConfig.instance().indicatorColor & 0x00FFFFFF;
    }

    public static int getFuelColor() {
        return ContainerIndicatorConfig.instance().fuelColor & 0x00FFFFFF;
    }

    public static boolean isBlockEnabled(Block block) {
        ContainerIndicatorConfig config = ContainerIndicatorConfig.instance();
        if (!config.enabled) return false;

        if (block == Blocks.HOPPER) return config.hopperEnabled;
        if (block == Blocks.DISPENSER) return config.dispenserEnabled;
        if (block == Blocks.DROPPER) return config.dropperEnabled;
        if (block == Blocks.BARREL) return config.barrelEnabled;
        if (block == Blocks.CRAFTER) return config.crafterEnabled;
        if (block == Blocks.FURNACE) return config.furnaceEnabled;
        if (block == Blocks.BLAST_FURNACE) return config.blastFurnaceEnabled;
        if (block == Blocks.SMOKER) return config.smokerEnabled;
        if (block == Blocks.DECORATED_POT) return config.decoratedPotEnabled;
        if (block == Blocks.CHEST) return config.chestEnabled;
        if (block == Blocks.TRAPPED_CHEST) return config.trappedChestEnabled;
        if (block == Blocks.COPPER_CHEST || block == Blocks.EXPOSED_COPPER_CHEST
                || block == Blocks.WEATHERED_COPPER_CHEST || block == Blocks.OXIDIZED_COPPER_CHEST
                || block == Blocks.WAXED_COPPER_CHEST || block == Blocks.WAXED_EXPOSED_COPPER_CHEST
                || block == Blocks.WAXED_WEATHERED_COPPER_CHEST || block == Blocks.WAXED_OXIDIZED_COPPER_CHEST)
            return config.copperChestEnabled;

        return true;
    }
}
