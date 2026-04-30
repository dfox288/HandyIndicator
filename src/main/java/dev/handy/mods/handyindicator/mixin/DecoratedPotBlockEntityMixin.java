package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.HandyIndicator;
import dev.handy.mods.handyindicator.ContainerStateHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotBlockEntity.class)
public abstract class DecoratedPotBlockEntityMixin {

    private void handyindicator$tryUpdate() {
        BlockEntity self = (BlockEntity) (Object) this;
        BlockState state = self.getBlockState();
        if (state.hasProperty(HandyIndicator.HAS_ITEMS)) {
            ContainerStateHelper.updateHasItems(self, (Container) self);
        }
    }

    @Inject(method = "setTheItem", at = @At("TAIL"))
    private void handyindicator$onSetTheItem(ItemStack stack, CallbackInfo ci) {
        handyindicator$tryUpdate();
    }

    @Inject(method = "splitTheItem", at = @At("TAIL"))
    private void handyindicator$onSplitTheItem(int amount, CallbackInfoReturnable<ItemStack> cir) {
        handyindicator$tryUpdate();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void handyindicator$onLoadAdditional(ValueInput input, CallbackInfo ci) {
        handyindicator$tryUpdate();
    }
}
