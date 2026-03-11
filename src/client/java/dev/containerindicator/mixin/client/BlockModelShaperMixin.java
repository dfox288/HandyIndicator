package dev.containerindicator.mixin.client;

import dev.containerindicator.ContainerIndicator;
import dev.containerindicator.model.CompositeBlockStateModel;
import dev.containerindicator.model.OverlayBlockModelPart;
import dev.containerindicator.model.OverlayQuadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(BlockStateModelSet.class)
public class BlockModelShaperMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(Map<BlockState, BlockStateModel> map, BlockStateModel missingModel, CallbackInfo ci) {
        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager()
                .getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(
                Identifier.fromNamespaceAndPath("container_indicator", "block/indicator"));
        Material.Baked bakedMaterial = new Material.Baked(sprite, false);

        // Build overlay parts once
        List<BakedQuad> standardQuads = OverlayQuadFactory.createStandardOverlay(sprite, 0);
        List<BakedQuad> bottomQuads = OverlayQuadFactory.createBottomOverlay(sprite, 1);
        List<BakedQuad> potQuads = OverlayQuadFactory.createPotOverlay(sprite, 0);
        List<BakedQuad> chestQuads = OverlayQuadFactory.createChestOverlay(sprite, 0);
        List<BakedQuad> doubleChestQuads = OverlayQuadFactory.createDoubleChestOverlay(sprite, 0);

        BlockStateModelPart standardPart = new OverlayBlockModelPart(standardQuads, bakedMaterial, true);
        BlockStateModelPart bottomPart = new OverlayBlockModelPart(bottomQuads, bakedMaterial, true);
        BlockStateModelPart potPart = new OverlayBlockModelPart(potQuads, bakedMaterial, false);
        BlockStateModelPart chestPart = new OverlayBlockModelPart(chestQuads, bakedMaterial, false);

        // Pre-build double chest overlay rotations per facing
        BlockStateModelPart doubleChestNorth = new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(doubleChestQuads, 0), bakedMaterial, false);
        BlockStateModelPart doubleChestEast = new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(doubleChestQuads, 90), bakedMaterial, false);
        BlockStateModelPart doubleChestSouth = new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(doubleChestQuads, 180), bakedMaterial, false);
        BlockStateModelPart doubleChestWest = new OverlayBlockModelPart(
                OverlayQuadFactory.rotateQuadsY(doubleChestQuads, 270), bakedMaterial, false);

        for (Map.Entry<BlockState, BlockStateModel> entry : map.entrySet()) {
            BlockState state = entry.getKey();
            Block block = state.getBlock();

            if (block instanceof BarrelBlock || block instanceof CrafterBlock
                    || block instanceof HopperBlock || block instanceof DispenserBlock) {
                if (state.getValue(ContainerIndicator.HAS_ITEMS)) {
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(standardPart)));
                }
            } else if (block instanceof DecoratedPotBlock) {
                if (state.getValue(ContainerIndicator.HAS_ITEMS)) {
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(potPart)));
                }
            } else if (block instanceof AbstractFurnaceBlock) {
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
            } else if (block instanceof ChestBlock) {
                if (!state.getValue(ContainerIndicator.HAS_ITEMS)) {
                    continue;
                }
                ChestType type = state.getValue(ChestBlock.TYPE);
                if (type == ChestType.SINGLE) {
                    entry.setValue(new CompositeBlockStateModel(
                            entry.getValue(), List.of(chestPart)));
                } else if (type == ChestType.LEFT) {
                    Direction facing = state.getValue(ChestBlock.FACING);
                    BlockStateModelPart doubleOverlay = switch (facing) {
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
