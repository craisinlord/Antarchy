package com.craisinlord.antarchy.neoforge.mixins.entity;

import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import com.craisinlord.antarchy.neoforge.entity.multipart.MultipartPartLevelRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
/*
 * Keeps multipart child entities registered in NeoForge levels.
 */
public abstract class MultipartEntityLevelRegistryMixin {
    @Inject(method = "setLevelCallback", at = @At("TAIL"))
    private void antarchy$registerMultipartParts(EntityInLevelCallback entityInLevelCallback, CallbackInfo ci) {
        if ((Object) this instanceof MultipartEntityOwner owner
                && entityInLevelCallback != EntityInLevelCallback.NULL) {
            if (owner.antarchy$getMultipartParts() == null) {
                owner.antarchy$spawnMultipartParts();
            }
            MultipartPartLevelRegistry.register(owner);
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void antarchy$unregisterMultipartParts(Entity.RemovalReason removalReason, CallbackInfo ci) {
        if ((Object) this instanceof MultipartEntityOwner owner) {
            MultipartPartLevelRegistry.unregister(owner);
        }
    }
}
