package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityRotationTimeDilationMixin {
    @ModifyVariable(method = "setYRot", at = @At("HEAD"), argsOnly = true)
    private float antarchy$slowYaw(float nextYaw) {
        Entity entity = (Entity) (Object) this;
        double rate = TimeDilationApi.getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return nextYaw;
        }
        return entity.getYRot() + Mth.wrapDegrees(nextYaw - entity.getYRot()) * (float) rate;
    }

    @ModifyVariable(method = "setXRot", at = @At("HEAD"), argsOnly = true)
    private float antarchy$slowPitch(float nextPitch) {
        Entity entity = (Entity) (Object) this;
        double rate = TimeDilationApi.getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return nextPitch;
        }
        return entity.getXRot() + (nextPitch - entity.getXRot()) * (float) rate;
    }
}
