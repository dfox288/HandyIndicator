package dev.containerindicator.mixin;

import dev.containerindicator.ContainerIndicator;
import dev.containerindicator.ContainerStateHelper;
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

    private void containerindicator$tryUpdate() {
        BaseContainerBlockEntity self = (BaseContainerBlockEntity) (Object) this;
        BlockState state = self.getBlockState();
        if (state.hasProperty(ContainerIndicator.HAS_ITEMS)) {
            if (self instanceof ChestBlockEntity chest) {
                ContainerStateHelper.updateChestHasItems(chest, chest);
            } else {
                ContainerStateHelper.updateHasItems(self, self);
            }
        }
    }

    @Inject(method = "setItem", at = @At("TAIL"))
    private void containerindicator$onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        containerindicator$tryUpdate();
    }

    @Inject(method = "removeItem", at = @At("TAIL"))
    private void containerindicator$onRemoveItem(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        containerindicator$tryUpdate();
    }
}
