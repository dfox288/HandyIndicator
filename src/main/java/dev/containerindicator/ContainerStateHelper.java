package dev.containerindicator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.ArrayList;
import java.util.List;

public final class ContainerStateHelper {

    private ContainerStateHelper() {}

    public static void refreshAllContainers(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            level.getChunkSource().chunkMap.forEachReadyToSendChunk(chunk -> {
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof Container container)) continue;
                    BlockState state = be.getBlockState();
                    if (state.hasProperty(ContainerIndicator.HAS_ITEMS)) {
                        if (be instanceof ChestBlockEntity) {
                            updateChestHasItems(be, container);
                        } else {
                            updateHasItems(be, container);
                        }
                    } else if (state.hasProperty(ContainerIndicator.HAS_INPUT)) {
                        List<ItemStack> items = new ArrayList<>();
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            items.add(container.getItem(i));
                        }
                        updateFurnaceState(be, items);
                    }
                }
            });
        }
    }

    public static void updateHasItems(BlockEntity entity, Container container) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) {
            return;
        }

        BlockState state = entity.getBlockState();
        if (!state.hasProperty(ContainerIndicator.HAS_ITEMS)) {
            return;
        }

        Block block = state.getBlock();
        boolean hasItems = false;

        if (ContainerIndicator.isBlockEnabled(block)) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) {
                    hasItems = true;
                    break;
                }
            }
        }

        boolean currentValue = state.getValue(ContainerIndicator.HAS_ITEMS);
        if (currentValue != hasItems) {
            entity.getLevel().setBlock(
                    entity.getBlockPos(),
                    state.setValue(ContainerIndicator.HAS_ITEMS, hasItems),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public static void updateFurnaceState(BlockEntity entity, List<ItemStack> items) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) {
            return;
        }

        BlockState state = entity.getBlockState();
        if (!state.hasProperty(ContainerIndicator.HAS_INPUT) || !state.hasProperty(ContainerIndicator.HAS_FUEL)) {
            return;
        }

        Block block = state.getBlock();
        boolean hasInput = false;
        boolean hasFuel = false;

        if (ContainerIndicator.isBlockEnabled(block)) {
            hasInput = items.size() > 0 && !items.get(0).isEmpty();
            hasFuel = items.size() > 1 && !items.get(1).isEmpty();
        }

        boolean currentInput = state.getValue(ContainerIndicator.HAS_INPUT);
        boolean currentFuel = state.getValue(ContainerIndicator.HAS_FUEL);

        if (currentInput != hasInput || currentFuel != hasFuel) {
            entity.getLevel().setBlock(
                    entity.getBlockPos(),
                    state.setValue(ContainerIndicator.HAS_INPUT, hasInput)
                         .setValue(ContainerIndicator.HAS_FUEL, hasFuel),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public static void updateChestHasItems(BlockEntity entity, Container container) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) {
            return;
        }

        BlockState state = entity.getBlockState();
        if (!state.hasProperty(ContainerIndicator.HAS_ITEMS)) {
            return;
        }

        Block block = state.getBlock();
        boolean thisHasItems = false;

        if (ContainerIndicator.isBlockEnabled(block)) {
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
        if (!unified && type != ChestType.SINGLE && ContainerIndicator.isBlockEnabled(block)) {
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
        boolean currentValue = state.getValue(ContainerIndicator.HAS_ITEMS);
        if (currentValue != unified) {
            entity.getLevel().setBlock(
                    entity.getBlockPos(),
                    state.setValue(ContainerIndicator.HAS_ITEMS, unified),
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
            if (neighborState.hasProperty(ContainerIndicator.HAS_ITEMS)) {
                boolean neighborCurrent = neighborState.getValue(ContainerIndicator.HAS_ITEMS);
                if (neighborCurrent != unified) {
                    level.setBlock(
                            neighborPos,
                            neighborState.setValue(ContainerIndicator.HAS_ITEMS, unified),
                            Block.UPDATE_CLIENTS
                    );
                }
            }
        }
    }

    public static void updateHasItems(BlockEntity entity, List<ItemStack> inventory) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) {
            return;
        }

        BlockState state = entity.getBlockState();
        if (!state.hasProperty(ContainerIndicator.HAS_ITEMS)) {
            return;
        }

        Block block = state.getBlock();
        boolean hasItems = false;

        if (ContainerIndicator.isBlockEnabled(block)) {
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) {
                    hasItems = true;
                    break;
                }
            }
        }

        boolean currentValue = state.getValue(ContainerIndicator.HAS_ITEMS);
        if (currentValue != hasItems) {
            entity.getLevel().setBlock(
                    entity.getBlockPos(),
                    state.setValue(ContainerIndicator.HAS_ITEMS, hasItems),
                    Block.UPDATE_CLIENTS
            );
        }
    }
}
