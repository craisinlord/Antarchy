package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Explosion.class)
/*
 * Fixes explosion knockback for inverted gravity.
 */
public abstract class ExplosionGravityMixin {

    @WrapOperation(method = "explode",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0))
    private Vec3 antarchy$fixExplosionGetVelocity(Entity entity, Operation<Vec3> original) {
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) return original.call(entity);
        return AntarchyGravityRotationUtil.vecPlayerToWorld(original.call(entity), direction);
    }

    @WrapOperation(method = "explode",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
                    ordinal = 0))
    private void antarchy$fixExplosionSetVelocity(Entity entity, Vec3 velocity, Operation<Void> original) {
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            original.call(entity, velocity);
            return;
        }
        original.call(entity, AntarchyGravityRotationUtil.vecWorldToPlayer(velocity, direction));
    }
}
