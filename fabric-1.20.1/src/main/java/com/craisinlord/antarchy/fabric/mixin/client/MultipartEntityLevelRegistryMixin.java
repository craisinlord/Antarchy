package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import com.craisinlord.antarchy.fabric.client.multipart.MultipartClientPartLevelRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MultipartEntityLevelRegistryMixin {
    @Inject(method = "setLevelCallback", at = @At("TAIL"))
    private void antarchy$registerMultipartParts(EntityInLevelCallback entityInLevelCallback, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof MultipartEntityOwner owner
                && entityInLevelCallback != EntityInLevelCallback.NULL
                && self.level().isClientSide()) {
            if (owner.antarchy$getMultipartParts() == null) {
                owner.antarchy$spawnMultipartParts();
            }
            MultipartClientPartLevelRegistry.register(owner);
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void antarchy$unregisterMultipartParts(Entity.RemovalReason removalReason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof MultipartEntityOwner owner && self.level().isClientSide()) {
            MultipartClientPartLevelRegistry.unregister(owner);
        }
    }
}
