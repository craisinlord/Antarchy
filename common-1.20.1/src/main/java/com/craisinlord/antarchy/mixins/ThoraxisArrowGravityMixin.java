package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.weather.ThoraxisEventMath;
import com.craisinlord.anthology.gravity.GravityHooks;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractArrow.class)
public abstract class ThoraxisArrowGravityMixin {
    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 0.05000000074505806D))
    private double antarchy$thoraxisGravity(double gravity) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (GravityHooks.shouldInvertProjectileGravity(self)) {
            return gravity;
        }
        return ThoraxisEventMath.scaleGravity(self, gravity);
    }
}
