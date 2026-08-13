package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyMountedGravityHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMountedGravityMixin {
    @Unique
    private Entity antarchy$previousVehicle;

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("RETURN"))
    private void antarchy$syncMountedStack(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            AntarchyMountedGravityHelper.syncConnectedStack((Entity) (Object) this);
        }
    }

    @Inject(method = "stopRiding", at = @At("HEAD"))
    private void antarchy$capturePreviousVehicle(CallbackInfo ci) {
        this.antarchy$previousVehicle = ((Entity) (Object) this).getVehicle();
    }

    @Inject(method = "stopRiding", at = @At("TAIL"))
    private void antarchy$syncDismountedStacks(CallbackInfo ci) {
        AntarchyMountedGravityHelper.syncSeparatedStacks(this.antarchy$previousVehicle, (Entity) (Object) this);
        this.antarchy$previousVehicle = null;
    }
}
