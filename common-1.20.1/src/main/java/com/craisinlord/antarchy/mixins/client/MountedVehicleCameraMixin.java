package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MountedVehicleCameraMixin {
    @Shadow
    private Entity entity;

    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @ModifyArg(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"),
            index = 0
    )
    private double antarchy$extendRoyalMountZoom(double distance) {
        if (this.entity != null && this.entity.getVehicle() instanceof RoyalMountEntity mount) {
            return distance + mount.thirdPersonCameraBonus();
        }
        return distance;
    }

    @Inject(method = "setup", at = @At("RETURN"))
    private void antarchy$adjustMountedCamera(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo ci
    ) {
        if (this.detached && this.entity != null && this.entity.getVehicle() instanceof RoyalMountEntity mount) {
            Camera camera = (Camera) (Object) this;
            this.setPosition(camera.getPosition().add(new Vec3(camera.getUpVector()).scale(mount.thirdPersonCameraLift())));
            return;
        }
        if (!this.antarchy$ridingHerculesBeetle()) {
            return;
        }

        Camera camera = (Camera) (Object) this;
        Vec3 look = new Vec3(camera.getLookVector());
        Vec3 cameraOffset = camera.getPosition().subtract(entity.getEyePosition(partialTick));
        double zoomOffset = cameraOffset.dot(look) * 0.45D;

        Vec3 adjustment = look.scale(zoomOffset)
                .add(new Vec3(camera.getUpVector()).scale(0.2D))
                .add(new Vec3(camera.getLeftVector()).scale(0.45D));
        this.setPosition(camera.getPosition().add(adjustment));
    }

    @Unique
    private boolean antarchy$ridingHerculesBeetle() {
        return this.detached && this.entity != null && this.entity.getVehicle() instanceof HerculesBeetleEntity;
    }
}
