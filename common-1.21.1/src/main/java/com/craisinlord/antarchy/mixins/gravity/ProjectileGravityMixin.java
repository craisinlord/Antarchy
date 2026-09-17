package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.ProjectileLaunchGravity;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
/*
 * Keeps projectile movement in world space while reversing gravity and launch
 * direction only inside the Thoraxis Underside.
 */
public abstract class ProjectileGravityMixin {

    @Inject(method = "shootFromRotation", at = @At("HEAD"))
    private void antarchy$prepareGravityAwareShot(
            Entity shooter,
            float xRot,
            float yRot,
            float rotationOffset,
            float velocity,
            float inaccuracy,
            CallbackInfo ci
    ) {
        ThoraxisUndersideManager.normalizeProjectileGravityFrame((Projectile) (Object) this);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$keepWorldMovementFrame(CallbackInfo ci) {
        ThoraxisUndersideManager.normalizeProjectileGravityFrame((Projectile) (Object) this);
    }

    @WrapOperation(
            method = "shootFromRotation",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;shoot(DDDFF)V"
            )
    )
    private void antarchy$rotateUndersideLaunchDirection(
            Projectile projectile,
            double x,
            double y,
            double z,
            float velocity,
            float inaccuracy,
            Operation<Void> original,
            @Local(argsOnly = true) Entity shooter
    ) {
        Vec3 direction = new Vec3(x, y, z);
        if (AntarchyGravityApi.isGravityInverted(shooter)) {
            direction = ProjectileLaunchGravity.toWorld(
                    direction,
                    AntarchyGravityApi.getGravityDirection(shooter)
            );
        }
        original.call(projectile, direction.x, direction.y, direction.z, velocity, inaccuracy);
    }

}
