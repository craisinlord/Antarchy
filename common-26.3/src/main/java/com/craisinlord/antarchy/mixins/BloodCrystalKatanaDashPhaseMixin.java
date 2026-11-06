package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.BloodCrystalKatanaItem;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class BloodCrystalKatanaDashPhaseMixin {

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void antarchy$katanaDashPassThrough(Entity entity, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (BloodCrystalKatanaItem.isDashing(self) || BloodCrystalKatanaItem.isDashing(entity)) {
            ci.cancel();
        }
    }
}
