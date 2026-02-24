package dev.containerindicator;

import net.fabricmc.api.ModInitializer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerIndicator implements ModInitializer {
    public static final String MOD_ID = "container-indicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final BooleanProperty HAS_ITEMS = BooleanProperty.create("has_items");
    public static final BooleanProperty HAS_INPUT = BooleanProperty.create("has_input");
    public static final BooleanProperty HAS_FUEL = BooleanProperty.create("has_fuel");

    @Override
    public void onInitialize() {
        ContainerIndicatorConfig.load();
        LOGGER.info("[Handy Indicator] Loaded!");
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
