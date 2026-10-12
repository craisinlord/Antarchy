package com.craisinlord.antarchy.mixins.gravity.client;

import com.craisinlord.antarchy.content.client.PortalGunRollClientState;
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

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("HEAD"))
    private void antarchy$storeTickDelta(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
        this.antarchy$storedTickDelta = partialTick;
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

    @Inject(
            method = "setRotation",
            at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;", shift = At.Shift.AFTER)
    )
    private void antarchy$applyGravityRotation(CallbackInfo ci) {
        if (this.entity == null) {
            return;
        }
        if (AntarchyGravityApi.isGravityInverted(this.entity) || AntarchyGravityApi.getGravityFlipProgress(this.entity, this.antarchy$storedTickDelta) > 0.0F) {
            this.rotation.premul(AntarchyGravityRotationUtil.getCameraRotationQuaternion(this.entity, this.antarchy$storedTickDelta));
        }
        this.rotation.premul(new Quaternionf().fromAxisAngleDeg(0.0F, 0.0F, -1.0F, PortalGunRollClientState.getRollDegrees(this.antarchy$storedTickDelta)));
    }
}
