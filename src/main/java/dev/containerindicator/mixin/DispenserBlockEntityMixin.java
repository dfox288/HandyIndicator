package dev.containerindicator.mixin;

import dev.containerindicator.ContainerStateHelper;
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

    @Inject(method = "setItems", at = @At("TAIL"))
    private void onSetItems(NonNullList<ItemStack> items, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((DispenserBlockEntity) (Object) this, this.items);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void onLoadAdditional(ValueInput input, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((DispenserBlockEntity) (Object) this, this.items);
    }
}
