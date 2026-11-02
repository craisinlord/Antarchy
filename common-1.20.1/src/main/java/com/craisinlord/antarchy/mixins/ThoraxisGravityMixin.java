package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.weather.ThoraxisEventMath;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class ThoraxisGravityMixin {
    @ModifyVariable(method = "travel", at = @At("STORE"), ordinal = 0)
    private double antarchy$thoraxisGravity(double gravity) {
        return ThoraxisEventMath.scaleGravity((LivingEntity) (Object) this, gravity);
    }
}
