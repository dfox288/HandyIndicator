package dev.handy.mods.handyindicator.mixin;

import dev.handy.mods.handyindicator.ContainerStateHelper;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin {

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void handyindicator$onLoadAdditional(ValueInput input, CallbackInfo ci) {
        ShulkerBoxBlockEntity self = (ShulkerBoxBlockEntity) (Object) this;
        ContainerStateHelper.updateHasItems(self, self);
    }
}