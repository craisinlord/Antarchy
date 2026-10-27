package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.RoyalBoundaryManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class RoyalBoundaryOwnerCleanupMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void antarchy$removeRoyalBoundary(Entity.RemovalReason reason, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) RoyalBoundaryManager.clearOwner(player);
    }
}
