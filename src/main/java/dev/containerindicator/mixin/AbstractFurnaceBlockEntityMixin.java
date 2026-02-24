package dev.containerindicator.mixin;

import dev.containerindicator.ContainerStateHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Shadow
    protected NonNullList<ItemStack> items;

    @Inject(method = "setItem", at = @At("TAIL"))
    private void containerindicator$onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        ContainerStateHelper.updateFurnaceState((AbstractFurnaceBlockEntity) (Object) this, this.items);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void containerindicator$onLoadAdditional(ValueInput input, CallbackInfo ci) {
        ContainerStateHelper.updateFurnaceState((AbstractFurnaceBlockEntity) (Object) this, this.items);
    }
}
