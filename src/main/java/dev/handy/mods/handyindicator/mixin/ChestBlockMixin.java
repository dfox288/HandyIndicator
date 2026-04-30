package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.HandyIndicator;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin extends Block {

    private ChestBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void handyindicator$addHasItems(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(HandyIndicator.HAS_ITEMS);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void handyindicator$setDefaultHasItems(
            Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityType,
            SoundEvent openSound, SoundEvent closeSound,
            BlockBehaviour.Properties properties, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState().setValue(HandyIndicator.HAS_ITEMS, false));
    }
}
