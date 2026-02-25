package dev.containerindicator.mixin.client;

import dev.containerindicator.ContainerIndicator;
import dev.containerindicator.model.CompositeBlockStateModel;
import dev.containerindicator.model.OverlayBlockModelPart;
import dev.containerindicator.model.OverlayQuadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {

    private static final Set<Block> SIMPLE_CONTAINERS = Set.of(
            Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
            Blocks.BARREL, Blocks.CRAFTER
    );

    private static final Set<Block> FURNACES = Set.of(
            Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER
    );

    private static final Set<Block> CHESTS = Set.of(
            Blocks.CHEST, Blocks.TRAPPED_CHEST,
            Blocks.COPPER_CHEST, Blocks.EXPOSED_COPPER_CHEST,
            Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER_CHEST,
            Blocks.WAXED_COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER_CHEST,
            Blocks.WAXED_WEATHERED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER_CHEST
    );

    @Inject(method = "replaceCache", at = @At("HEAD"))
    private void onReplaceCache(Map<BlockState, BlockStateModel> map, CallbackInfo ci) {
        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager()
                .getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(
                Identifier.fromNamespaceAndPath("container_indicator", "block/indicator"));

        // Build overlay parts once
        List<BakedQuad> standardQuads = OverlayQuadFactory.createStandardOverlay(sprite, 0);
        List<BakedQuad> bottomQuads = OverlayQuadFactory.createBottomOverlay(sprite, 1);
        List<BakedQuad> potQuads = OverlayQuadFactory.createPotOverlay(sprite, 0);
        List<BakedQuad> chestQuads = OverlayQuadFactory.createChestOverlay(sprite, 0);
        List<BakedQuad> doubleChestQuads = OverlayQuadFactory.createDoubleChestOverlay(sprite, 0);

        BlockModelPart standardPart = new OverlayBlockModelPart(standardQuads, sprite, true);
        BlockModelPart bottomPart = new OverlayBlockModelPart(bottomQuads, sprite, true);
        BlockModelPart potPart = new OverlayBlockModelPart(potQuads, sprite, false);
        BlockModelPart chestPart = new OverlayBlockModelPart(chestQuads, sprite, false);

        // Pre-build double chest overlay rotations per facing
        // Base overlay extends in +X (east). For LEFT type, neighbor direction determines rotation.
        // LEFT facing NORTH -> neighbor EAST -> 0 deg
        // LEFT facing EAST  -> neighbor SOUTH -> 90 deg
        // LEFT facing SOUTH -> neighbor WEST -> 180 deg
        // LEFT facing WEST  -> neighbor NORTH -> 270 deg
        BlockModelPart doubleChestNorth = new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(doubleChestQuads, 0), sprite, false);
        BlockModelPart doubleChestEast = new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(doubleChestQuads, 90), sprite, false);
        BlockModelPart doubleChestSouth = new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(doubleChestQuads, 180), sprite, false);
        BlockModelPart doubleChestWest = new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(doubleChestQuads, 270), sprite, false);

        for (Map.Entry<BlockState, BlockStateModel> entry : map.entrySet()) {
            BlockState state = entry.getKey();
            Block block = state.getBlock();

            if (SIMPLE_CONTAINERS.contains(block)) {
                if (state.getValue(ContainerIndicator.HAS_ITEMS)) {
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(standardPart)));
                }
            } else if (block == Blocks.DECORATED_POT) {
                if (state.getValue(ContainerIndicator.HAS_ITEMS)) {
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(potPart)));
                }
            } else if (FURNACES.contains(block)) {
                boolean hasInput = state.getValue(ContainerIndicator.HAS_INPUT);
                boolean hasFuel = state.getValue(ContainerIndicator.HAS_FUEL);
                if (hasInput && hasFuel) {
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(standardPart, bottomPart)));
                } else if (hasInput) {
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(standardPart)));
                } else if (hasFuel) {
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(bottomPart)));
                }
            } else if (CHESTS.contains(block)) {
                if (!state.getValue(ContainerIndicator.HAS_ITEMS)) {
                    continue;
                }
                ChestType type = state.getValue(ChestBlock.TYPE);
                if (type == ChestType.SINGLE) {
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(chestPart)));
                } else if (type == ChestType.LEFT) {
                    Direction facing = state.getValue(ChestBlock.FACING);
                    BlockModelPart doubleOverlay = switch (facing) {
                        case NORTH -> doubleChestNorth;
                        case EAST -> doubleChestEast;
                        case SOUTH -> doubleChestSouth;
                        case WEST -> doubleChestWest;
                        default -> doubleChestNorth;
                    };
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(doubleOverlay)));
                }
                // RIGHT type: no overlay (left half handles the full double overlay)
            }
        }
    }
}
