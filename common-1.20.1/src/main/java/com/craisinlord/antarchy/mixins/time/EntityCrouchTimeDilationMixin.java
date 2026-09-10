package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityCrouchTimeDilationMixin {
    @Inject(method = "setPose", at = @At("HEAD"), cancellable = true)
    private void antarchy$slowCrouchTransition(Pose pose, CallbackInfo callbackInfo) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity living
                && (pose == Pose.CROUCHING || living.getPose() == Pose.CROUCHING)
                && !TimeDilationApi.consumeTick(entity, "living_crouch")) {
            callbackInfo.cancel();
        }
    }
}
