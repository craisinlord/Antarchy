package com.craisinlord.antarchy.mixins.multipart;

import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
/*
 * Ticks and cleans up multipart child entities with their parent.
 */
public abstract class EntityMultipartLifecycleMixin {
    @Inject(method = "setLevelCallback", at = @At("TAIL"))
    private void antarchy$spawnMultipartParts(EntityInLevelCallback entityInLevelCallback, CallbackInfo ci) {
        if ((Object) this instanceof MultipartEntityOwner owner
                && entityInLevelCallback != EntityInLevelCallback.NULL
                && owner.antarchy$getMultipartParts() == null) {
            owner.antarchy$spawnMultipartParts();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$syncMultipartParts(CallbackInfo ci) {
        if ((Object) this instanceof MultipartEntityOwner owner) {
            owner.antarchy$syncMultipartParts();
        }
    }

    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void antarchy$discardMultipartParts(Entity.RemovalReason removalReason, CallbackInfo ci) {
        if ((Object) this instanceof MultipartEntityOwner owner) {
            owner.antarchy$discardMultipartParts();
        }
    }
}
