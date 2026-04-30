package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.HandyIndicator;
import dev.handy.mods.handyindicator.ContainerStateHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin {

    private void handyindicator$tryUpdate() {
        BaseContainerBlockEntity self = (BaseContainerBlockEntity) (Object) this;
        BlockState state = self.getBlockState();
        if (state.hasProperty(HandyIndicator.HAS_ITEMS)) {
            if (self instanceof ChestBlockEntity chest) {
                ContainerStateHelper.updateChestHasItems(chest, chest);
            } else {
                ContainerStateHelper.updateHasItems(self, self);
            }
        }
    }

    @Inject(method = "setItem(ILnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void handyindicator$onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        handyindicator$tryUpdate();
    }

    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("TAIL"))
    private void handyindicator$onRemoveItem(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        handyindicator$tryUpdate();
    }
}
