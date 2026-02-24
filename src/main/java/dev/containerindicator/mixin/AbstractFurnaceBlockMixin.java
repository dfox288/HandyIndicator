package dev.containerindicator.mixin;

import dev.containerindicator.ContainerIndicator;
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
    private void containerindicator$addHasItems(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(ContainerIndicator.HAS_INPUT);
        builder.add(ContainerIndicator.HAS_FUEL);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void containerindicator$setDefaultHasItems(BlockBehaviour.Properties properties, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState()
                .setValue(ContainerIndicator.HAS_INPUT, false)
                .setValue(ContainerIndicator.HAS_FUEL, false));
    }
}
