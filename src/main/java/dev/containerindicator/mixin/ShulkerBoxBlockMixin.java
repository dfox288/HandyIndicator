package dev.containerindicator.mixin;

import dev.containerindicator.ContainerIndicator;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlock.class)
public abstract class ShulkerBoxBlockMixin extends Block {

    private ShulkerBoxBlockMixin(DyeColor color, Properties properties) {
        super(properties);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void containerindicator$addHasItems(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(ContainerIndicator.HAS_ITEMS);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void containerindicator$setDefaultHasItems(DyeColor color, BlockBehaviour.Properties properties, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState().setValue(ContainerIndicator.HAS_ITEMS, false));
    }
}