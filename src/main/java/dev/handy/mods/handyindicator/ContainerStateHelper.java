package dev.handy.mods.handyindicator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

public final class ContainerStateHelper {

    private ContainerStateHelper() {}

    public static void refreshChunk(LevelChunk chunk) {
        int crafterCount = 0;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (!(be instanceof Container container)) continue;
            BlockState state = be.getBlockState();
            if (state.hasProperty(HandyIndicator.HAS_ITEMS)) {
                if (be instanceof ChestBlockEntity) {
                    updateChestHasItems(be, container);
                } else {
                    updateHasItems(be, container);
                }
            }
            if (state.hasProperty(HandyIndicator.HAS_INPUT)) {
                List<ItemStack> items = new ArrayList<>();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    items.add(container.getItem(i));
                }
                updateFurnaceState(be, items);
            }
            if (state.hasProperty(HandyIndicator.HAS_ITEMS_READY)) {
                if (be instanceof CrafterBlockEntity crafter) {
                    crafterCount++;
                    updateCrafterReadyState(be, crafter.getItems());
                }
            }
        }
        if (crafterCount > 0) {
            HandyIndicator.LOGGER.debug("Refreshed {} crafters in chunk {}", crafterCount, chunk.getPos());
        }
    }

    public static void refreshAllContainers(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            level.getChunkSource().chunkMap.forEachReadyToSendChunk(
                    ContainerStateHelper::refreshChunk
            );
        }
    }

    public static void updateHasItems(BlockEntity entity, Container container) {
        updateHasItemsCore(entity, () -> anyNonEmpty(container));
    }

    private static boolean anyNonEmpty(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    private static boolean anyNonEmpty(List<ItemStack> inventory) {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }

    /**
     * Common path for the two updateHasItems overloads. Reads the block's HAS_ITEMS
     * property, evaluates the supplied non-empty check (lazily — only fires when the
     * block is enabled), and writes back if the value changed.
     */
    private static void updateHasItemsCore(BlockEntity entity, java.util.function.BooleanSupplier hasContents) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) return;

        BlockState state = entity.getBlockState();
        if (!state.hasProperty(HandyIndicator.HAS_ITEMS)) return;

        boolean hasItems = HandyIndicator.isBlockEnabled(state.getBlock()) && hasContents.getAsBoolean();

        boolean currentValue = state.getValue(HandyIndicator.HAS_ITEMS);
        if (currentValue != hasItems) {
            entity.getLevel().setBlock(
                    entity.getBlockPos(),
                    state.setValue(HandyIndicator.HAS_ITEMS, hasItems),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public static void updateFurnaceState(BlockEntity entity, List<ItemStack> items) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) {
            return;
        }

        BlockState state = entity.getBlockState();
        if (!state.hasProperty(HandyIndicator.HAS_INPUT) || !state.hasProperty(HandyIndicator.HAS_FUEL)) {
            return;
        }

        Block block = state.getBlock();
        boolean hasInput = false;
        boolean hasFuel = false;

        if (HandyIndicator.isBlockEnabled(block)) {
            hasInput = items.size() > 0 && !items.get(0).isEmpty();
            hasFuel = items.size() > 1 && !items.get(1).isEmpty();
        }

        boolean currentInput = state.getValue(HandyIndicator.HAS_INPUT);
        boolean currentFuel = state.getValue(HandyIndicator.HAS_FUEL);

        if (currentInput != hasInput || currentFuel != hasFuel) {
            entity.getLevel().setBlock(
                    entity.getBlockPos(),
                    state.setValue(HandyIndicator.HAS_INPUT, hasInput)
                         .setValue(HandyIndicator.HAS_FUEL, hasFuel),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public static void updateChestHasItems(BlockEntity entity, Container container) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) {
            return;
        }

        BlockState state = entity.getBlockState();
        if (!state.hasProperty(HandyIndicator.HAS_ITEMS)) {
            return;
        }

        Block block = state.getBlock();
        boolean thisHasItems = false;

        if (HandyIndicator.isBlockEnabled(block)) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) {
                    thisHasItems = true;
                    break;
                }
            }
        }

        // For double chests, check the neighbor half too
        boolean unified = thisHasItems;
        ChestType type = state.getValue(ChestBlock.TYPE);
        if (!unified && type != ChestType.SINGLE && HandyIndicator.isBlockEnabled(block)) {
            Direction neighborDir = type == ChestType.LEFT
                    ? state.getValue(ChestBlock.FACING).getClockWise()
                    : state.getValue(ChestBlock.FACING).getCounterClockWise();
            BlockPos neighborPos = entity.getBlockPos().relative(neighborDir);
            Level level = entity.getLevel();
            BlockEntity neighborEntity = level.getBlockEntity(neighborPos);
            if (neighborEntity instanceof ChestBlockEntity neighborChest) {
                for (int i = 0; i < neighborChest.getContainerSize(); i++) {
                    if (!neighborChest.getItem(i).isEmpty()) {
                        unified = true;
                        break;
                    }
                }
            }
        }

        // Update this half
        boolean currentValue = state.getValue(HandyIndicator.HAS_ITEMS);
        if (currentValue != unified) {
            entity.getLevel().setBlock(
                    entity.getBlockPos(),
                    state.setValue(HandyIndicator.HAS_ITEMS, unified),
                    Block.UPDATE_CLIENTS
            );
        }

        // Update neighbor half to match
        if (type != ChestType.SINGLE) {
            Direction neighborDir = type == ChestType.LEFT
                    ? state.getValue(ChestBlock.FACING).getClockWise()
                    : state.getValue(ChestBlock.FACING).getCounterClockWise();
            BlockPos neighborPos = entity.getBlockPos().relative(neighborDir);
            Level level = entity.getLevel();
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.hasProperty(HandyIndicator.HAS_ITEMS)) {
                boolean neighborCurrent = neighborState.getValue(HandyIndicator.HAS_ITEMS);
                if (neighborCurrent != unified) {
                    level.setBlock(
                            neighborPos,
                            neighborState.setValue(HandyIndicator.HAS_ITEMS, unified),
                            Block.UPDATE_CLIENTS
                    );
                }
            }
        }
    }

    public static void updateHasItems(BlockEntity entity, List<ItemStack> inventory) {
        updateHasItemsCore(entity, () -> anyNonEmpty(inventory));
    }

    public static void updateCrafterReadyState(BlockEntity entity, NonNullList<ItemStack> items) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) {
            return;
        }

        BlockState state = entity.getBlockState();
        if (!state.hasProperty(HandyIndicator.HAS_ITEMS_READY)) {
            return;
        }

        Block block = state.getBlock();
        boolean isReady = false;

        if (HandyIndicator.isBlockEnabled(block)) {
            // Crafter is "ready" if:
            // 1. It has items in the grid (slots 0-8)
            // 2. The output slot (slot 9) is NOT full (can accept more items)
            // We approximate this by checking if output slot is empty or not at max stack size
            // NonNullList always has 10 slots for CrafterBlockEntity, so we can safely access index 9
            ItemStack outputStack = items.size() > 9 ? items.get(9) : ItemStack.EMPTY;
            boolean hasInput = false;
            for (int i = 0; i < 9; i++) {
                if (items.size() > i && !items.get(i).isEmpty()) {
                    hasInput = true;
                    break;
                }
            }
            // Ready if has input AND output can accept more (not full)
            if (hasInput && (outputStack.isEmpty() || outputStack.getCount() < outputStack.getMaxStackSize())) {
                isReady = true;
            }
        }

        boolean currentValue = state.getValue(HandyIndicator.HAS_ITEMS_READY);
        if (currentValue != isReady) {
            HandyIndicator.LOGGER.debug("Crafter at {} ready state: {} -> {}", entity.getBlockPos(), currentValue, isReady);
            entity.getLevel().setBlock(
                    entity.getBlockPos(),
                    state.setValue(HandyIndicator.HAS_ITEMS_READY, isReady),
                    Block.UPDATE_CLIENTS
            );
        }
    }
}
