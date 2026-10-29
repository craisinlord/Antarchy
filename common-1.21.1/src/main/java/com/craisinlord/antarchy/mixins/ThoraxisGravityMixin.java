package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.weather.ThoraxisEventMath;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ThoraxisGravityMixin {
    @Inject(method = "getGravity", at = @At("RETURN"), cancellable = true)
    private void antarchy$thoraxisGravity(CallbackInfoReturnable<Double> cir) {
        double gravity = cir.getReturnValueD();
        double scaled = ThoraxisEventMath.scaleGravity((Entity) (Object) this, gravity);
        if (scaled != gravity) {
            cir.setReturnValue(scaled);
        }
    }
}
