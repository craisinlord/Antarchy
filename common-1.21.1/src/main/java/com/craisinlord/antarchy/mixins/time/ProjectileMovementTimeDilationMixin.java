package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractArrow.class, ThrowableProjectile.class, AbstractHurtingProjectile.class})
public abstract class ProjectileMovementTimeDilationMixin {
    @Unique
    private Vec3 antarchy$preTickPos;
    @Unique
    private Vec3 antarchy$preTickDelta;

    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$recordPreTickMotion(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (TimeDilationApi.getRate(entity) < TimeDilationMath.NORMAL_RATE) {
            this.antarchy$preTickPos = entity.position();
            this.antarchy$preTickDelta = entity.getDeltaMovement();
        } else {
            this.antarchy$preTickPos = null;
            this.antarchy$preTickDelta = null;
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void antarchy$scaleTickDisplacement(CallbackInfo ci) {
        Vec3 pre = this.antarchy$preTickPos;
        Vec3 preDelta = this.antarchy$preTickDelta;
        this.antarchy$preTickPos = null;
        this.antarchy$preTickDelta = null;
        if (pre == null) {
            return;
        }

        Entity entity = (Entity) (Object) this;
        if (entity.isRemoved()) {
            return;
        }
        double rate = TimeDilationApi.getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return;
        }

        Vec3 actual = entity.position().subtract(pre);
        if (actual.lengthSqr() < 1.0E-12D) {
            return;
        }
        if (preDelta != null && actual.lengthSqr() + 1.0E-9D < preDelta.lengthSqr()) {
            return;
        }

        Vec3 lerped = pre.add(actual.scale(rate));
        entity.setPos(lerped.x, lerped.y, lerped.z);
    }
}
