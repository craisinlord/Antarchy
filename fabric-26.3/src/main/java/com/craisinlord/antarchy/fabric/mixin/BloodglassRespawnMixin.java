package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.fabric.BloodglassManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class BloodglassRespawnMixin {

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void antarchy$onRespawn(ServerPlayer oldPlayer, boolean keepEverything, CallbackInfo ci) {
        BloodglassManager.handleRespawn((ServerPlayer) (Object) this);
    }
}
