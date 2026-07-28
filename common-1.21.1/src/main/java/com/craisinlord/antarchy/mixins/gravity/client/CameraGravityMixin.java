package com.craisinlord.antarchy.mixins.gravity.client;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
/*
 * Fixes camera pos/rot for inverted gravity and the flip anim.
 * Rotation is applied before move() so third-person pull-back uses the
 * correct gravity-adjusted forward direction. First-person falls through
 * to the TAIL inject since setup never calls move() in that case.
 */
public abstract class CameraGravityMixin {
    @Shadow
    private Entity entity;

    @Shadow
    private Quaternionf rotation;

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    private float antarchy$storedTickDelta;
    private boolean antarchy$gravityApplied;

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    protected abstract void move(float forward, float up, float left);

    @Inject(method = "setup", at = @At("HEAD"))
    private void antarchy$storeTickDelta(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
        this.antarchy$storedTickDelta = partialTick;
        this.antarchy$gravityApplied = false;
    }

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

    /*
     * Intercept the move() call inside setup so the pull-back direction
     * already has gravity rotation applied. This covers detached (third-person)
     * and sleeping cameras. Sets a flag so the TAIL inject doesn't double-apply.
     */
    @Redirect(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V")
    )
    private void antarchy$moveWithGravity(
            Camera camera,
            float forward,
            float up,
            float left,
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean mirrored,
            float partialTick
    ) {
        antarchy$applyGravityRotationOnce();
        this.move(forward, up, left);
    }

    /*
     * First-person fallthrough: setup never calls move(), so gravity rotation
     * hasn't been applied yet when we reach the end of the method.
     */
    @Inject(method = "setup", at = @At("TAIL"))
    private void antarchy$applyGravityAtTail(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
        antarchy$applyGravityRotationOnce();
    }

    private void antarchy$applyGravityRotationOnce() {
        if (this.antarchy$gravityApplied) {
            return;
        }
        if (this.entity == null) {
            return;
        }
        if (!AntarchyGravityApi.isGravityInverted(this.entity) && AntarchyGravityApi.getGravityFlipProgress(this.entity, this.antarchy$storedTickDelta) <= 0.0F) {
            return;
        }
        this.rotation.premul(AntarchyGravityRotationUtil.getCameraRotationQuaternion(this.entity, this.antarchy$storedTickDelta));
        this.antarchy$gravityApplied = true;
    }
}
