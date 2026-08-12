package com.craisinlord.antarchy.mixins.gravity.client;

import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
/*
 * Fixes camera anchor position for inverted gravity and the flip anim.
 * The actual view-rotation flip is handled by ViewportEvent.ComputeCameraAngles
 * (GravityCameraRollHandler) since GameRenderer builds its view matrix from
 * Camera.getXRot()/getYRot() directly, not from the Camera.rotation quaternion
 * mutated here.
 */
public abstract class CameraGravityMixin {
    @Shadow
    private Entity entity;

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    protected abstract void move(double forward, double up, double left);

    @Redirect(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V")
    )
    private void antarchy$moveGravityCameraAnchor(
            Camera camera,
            double x,
            double y,
            double z,
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean mirrored,
            float partialTick
    ) {
        if (entity == null) {
            this.setPosition(x, y, z);
            return;
        }

        float flipProgress = AntarchyGravityApi.getGravityFlipProgress(entity, partialTick);
        if (!AntarchyGravityApi.isGravityInverted(entity) && flipProgress <= 0.0F) {
            this.setPosition(x, y, z);
            return;
        }

        Vec3 interpolatedPos = entity.getPosition(partialTick);
        float normalEyeHeight = this.eyeHeightOld + (this.eyeHeight - this.eyeHeightOld) * partialTick;
        Vector3f eyeOffset = new Vector3f(0.0F, normalEyeHeight, 0.0F);
        eyeOffset.rotate(AntarchyGravityRotationUtil.getWorldRotationQuaternion(entity, partialTick));
        this.setPosition(
                interpolatedPos.x + eyeOffset.x(),
                interpolatedPos.y + eyeOffset.y(),
                interpolatedPos.z + eyeOffset.z()
        );
    }

    @Redirect(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(DDD)V")
    )
    private void antarchy$moveWithGravity(
            Camera camera,
            double forward,
            double up,
            double left,
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean mirrored,
            float partialTick
    ) {
        if (detached && this.entity != null && this.entity.getVehicle() instanceof HerculesBeetleEntity) {
            forward *= 1.45D;
            up += 0.2D;
            left += 0.45D;
        }

        this.move(forward, up, left);
    }
}
