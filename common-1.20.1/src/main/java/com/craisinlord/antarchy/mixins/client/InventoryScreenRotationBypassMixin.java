package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenRotationBypassMixin {
    @Inject(method = "renderEntityInInventoryFollowsMouse", at = @At("HEAD"))
    private static void antarchy$enterRotationBypass(CallbackInfo callbackInfo) {
        TimeDilationApi.enterRotationBypass();
    }

    @Inject(method = "renderEntityInInventoryFollowsMouse", at = @At("RETURN"))
    private static void antarchy$exitRotationBypass(CallbackInfo callbackInfo) {
        TimeDilationApi.exitRotationBypass();
    }
}
