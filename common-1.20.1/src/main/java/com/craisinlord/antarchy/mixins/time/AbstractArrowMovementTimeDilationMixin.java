package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMovementTimeDilationMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 antarchy$slowCollisionPath(Vec3 position, Vec3 movement) {
        double rate = TimeDilationApi.getRate((AbstractArrow) (Object) this);
        return position.add(rate >= TimeDilationMath.NORMAL_RATE ? movement : movement.scale(rate));
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setPos(DDD)V"))
    private void antarchy$slowPosition(AbstractArrow arrow, double x, double y, double z) {
        double rate = Math.max(TimeDilationMath.MIN_RATE, TimeDilationApi.getRate(arrow));
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            arrow.setPos(x, y, z);
            return;
        }
        arrow.setPos(arrow.getX() + (x - arrow.getX()) * rate,
                arrow.getY() + (y - arrow.getY()) * rate,
                arrow.getZ() + (z - arrow.getZ()) * rate);
    }
}
