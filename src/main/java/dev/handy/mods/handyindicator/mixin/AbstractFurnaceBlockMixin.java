package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.HandyIndicator;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlock.class)
public abstract class AbstractFurnaceBlockMixin extends Block {

    private AbstractFurnaceBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void handyindicator$addHasItems(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(HandyIndicator.HAS_INPUT);
        builder.add(HandyIndicator.HAS_FUEL);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void handyindicator$setDefaultHasItems(BlockBehaviour.Properties properties, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState()
                .setValue(HandyIndicator.HAS_INPUT, false)
                .setValue(HandyIndicator.HAS_FUEL, false));
    }
}
