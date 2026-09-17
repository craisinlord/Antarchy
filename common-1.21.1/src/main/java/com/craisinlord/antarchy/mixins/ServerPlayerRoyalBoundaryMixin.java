package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.RoyalBoundaryManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerRoyalBoundaryMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$tickRoyalBoundary(CallbackInfo ci) {
        RoyalBoundaryManager.tickOwner((ServerPlayer) (Object) this);
    }
}
