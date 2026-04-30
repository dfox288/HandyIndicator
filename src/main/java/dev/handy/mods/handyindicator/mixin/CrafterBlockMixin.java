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

    @Inject(method = "createBlockStateDefinition(Lnet/minecraft/world/level/block/state/StateDefinition$Builder;)V", at = @At("TAIL"))
    private void handyindicator$addProperties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(HandyIndicator.HAS_ITEMS);
        builder.add(HandyIndicator.HAS_ITEMS_READY);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V", at = @At("RETURN"))
    private void handyindicator$setDefaultProperties(BlockBehaviour.Properties properties, CallbackInfo ci) {
        // Chain both setValue calls into a single registerDefaultState. Each
        // registerDefaultState call resolves its arg from defaultBlockState()
        // *before* assigning, so calling it twice would have the second call
        // read the original (un-mutated) defaultBlockState and silently
        // overwrite the first call's mutation. Worked by accident here only
        // because both properties default to false — break the moment either
        // default changes. Mirrors the pattern in ShulkerBoxBlockMixin.
        this.registerDefaultState(this.defaultBlockState()
                .setValue(HandyIndicator.HAS_ITEMS, false)
                .setValue(HandyIndicator.HAS_ITEMS_READY, false));
    }
}
