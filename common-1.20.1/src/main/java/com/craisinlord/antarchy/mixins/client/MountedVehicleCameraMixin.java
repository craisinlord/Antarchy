package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Camera.class)
public abstract class MountedVehicleCameraMixin {
    @Shadow
    private Entity entity;

    @Shadow
    private boolean detached;

    @ModifyArg(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(DDD)V"),
            index = 0
    )
    private double antarchy$adjustMountedCameraX(double value) {
        return this.antarchy$ridingHerculesBeetle() ? value * 1.45D : value;
    }

    @ModifyArg(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(DDD)V"),
            index = 1
    )
    private double antarchy$adjustMountedCameraY(double value) {
        return this.antarchy$ridingHerculesBeetle() ? value + 0.2D : value;
    }

    @ModifyArg(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(DDD)V"),
            index = 2
    )
    private double antarchy$adjustMountedCameraZ(double value) {
        return this.antarchy$ridingHerculesBeetle() ? value + 0.45D : value;
    }

    @Unique
    private boolean antarchy$ridingHerculesBeetle() {
        return this.detached && this.entity != null && this.entity.getVehicle() instanceof HerculesBeetleEntity;
    }
}
