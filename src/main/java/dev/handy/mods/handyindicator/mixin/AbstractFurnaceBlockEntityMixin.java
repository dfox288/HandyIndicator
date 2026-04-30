package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.ContainerStateHelper;
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

    @Inject(method = "setItem(ILnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void handyindicator$onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        ContainerStateHelper.updateFurnaceState((AbstractFurnaceBlockEntity) (Object) this, this.items);
    }

    @Inject(method = "loadAdditional(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"))
    private void handyindicator$onLoadAdditional(ValueInput input, CallbackInfo ci) {
        ContainerStateHelper.updateFurnaceState((AbstractFurnaceBlockEntity) (Object) this, this.items);
    }
}
