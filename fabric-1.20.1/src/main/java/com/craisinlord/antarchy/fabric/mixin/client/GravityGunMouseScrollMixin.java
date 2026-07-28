package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.fabric.client.GravityGunClientHandler;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class GravityGunMouseScrollMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void antarchy$handleGravityGunScroll(long window, double horizontalAmount, double verticalAmount, CallbackInfo ci) {
        if (GravityGunClientHandler.onScroll(verticalAmount)) {
            ci.cancel();
        }
    }
}
