package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.ContainerStateHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlockEntity.class)
public abstract class DispenserBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Inject(method = "setItems(Lnet/minecraft/core/NonNullList;)V", at = @At("TAIL"))
    private void onSetItems(NonNullList<ItemStack> items, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((DispenserBlockEntity) (Object) this, this.items);
    }

    @Inject(method = "loadAdditional(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"))
    private void onLoadAdditional(ValueInput input, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((DispenserBlockEntity) (Object) this, this.items);
    }
}
