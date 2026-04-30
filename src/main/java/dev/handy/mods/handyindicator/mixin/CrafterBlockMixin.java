package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.HandyIndicator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterBlock.class)
public abstract class CrafterBlockMixin extends Block {

    private CrafterBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void handyindicator$addProperties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(HandyIndicator.HAS_ITEMS);
        builder.add(HandyIndicator.HAS_ITEMS_READY);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void handyindicator$setDefaultProperties(BlockBehaviour.Properties properties, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState().setValue(HandyIndicator.HAS_ITEMS, false));
        this.registerDefaultState(this.defaultBlockState().setValue(HandyIndicator.HAS_ITEMS_READY, false));
    }
}
