package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class MountedVehicleCameraMixin {
    @Shadow
    private Entity entity;

    @ModifyArgs(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V")
    )
    private void antarchy$adjustMountedCameraOffset(
            Args args,
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean mirrored,
            float partialTick
    ) {
        if (!detached || this.entity == null || this.entity.getVehicle() == null) {
            return;
        }

        Entity vehicle = this.entity.getVehicle();
        if (vehicle instanceof HerculesBeetleEntity) {
            args.set(0, (Float) args.get(0) * 1.45F);
            args.set(1, (Float) args.get(1) + 0.2F);
            args.set(2, (Float) args.get(2) + 0.45F);
            return;
        }

//        if (vehicle instanceof DorrieEntity) {
//            args.set(0, (Float) args.get(0) * 0.25F);
//            args.set(1, (Float) args.get(1) + 0.1F);
//            args.set(2, (Float) args.get(2) + 0.55F);
//        }
    }
}
