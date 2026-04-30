package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.ContainerStateHelper;
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
    private void handyindicator$onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((HopperBlockEntity) (Object) this, this.items);
    }

    @Inject(method = "removeItem", at = @At("TAIL"))
    private void handyindicator$onRemoveItem(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        ContainerStateHelper.updateHasItems((HopperBlockEntity) (Object) this, this.items);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void handyindicator$onLoadAdditional(ValueInput input, CallbackInfo ci) {
        ContainerStateHelper.updateHasItems((HopperBlockEntity) (Object) this, this.items);
    }

    // No pushItemsTick inject. Every actual transfer goes through setItem or
    // removeItem, both of which already trigger updateHasItems above. Hooking
    // pushItemsTick on top of that meant a per-tick (every 8 game ticks per
    // hopper) inventory scan + state-compare for every hopper in every loaded
    // chunk, almost all of which were no-ops because nothing had changed.
    // For a 50+ hopper sorting farm that's hundreds of redundant scans per
    // second. The corner case it covered — other mods mutating `items` via the
    // raw list reference without going through setItem — is rare and resolves
    // on the next vanilla mutation anyway.
}
