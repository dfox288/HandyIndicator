package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.ContainerStateHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterBlockEntity.class)
public abstract class CrafterBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Inject(method = "setItem", at = @At("TAIL"))
    private void handyindicator$onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((CrafterBlockEntity) (Object) this, this.items);
        ContainerStateHelper.updateCrafterReadyState((CrafterBlockEntity) (Object) this, this.items);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void handyindicator$onLoadAdditional(ValueInput input, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((CrafterBlockEntity) (Object) this, this.items);
        ContainerStateHelper.updateCrafterReadyState((CrafterBlockEntity) (Object) this, this.items);
    }
}
