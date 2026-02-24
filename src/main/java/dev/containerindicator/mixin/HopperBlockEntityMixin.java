package dev.containerindicator.mixin;

import dev.containerindicator.ContainerStateHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Inject(method = "setItem", at = @At("TAIL"))
    private void containerindicator$onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((HopperBlockEntity) (Object) this, this.items);
    }

    @Inject(method = "removeItem", at = @At("TAIL"))
    private void containerindicator$onRemoveItem(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        ContainerStateHelper.updateHasItems((HopperBlockEntity) (Object) this, this.items);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void containerindicator$onLoadAdditional(ValueInput input, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((HopperBlockEntity) (Object) this, this.items);
    }
}
