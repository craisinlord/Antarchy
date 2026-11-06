package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class ParalyzedMouseTurnMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void antarchy$cancelMouseLookWhileParalyzed(double deltaX, CallbackInfo ci) {
        if (this.minecraft.player != null && this.minecraft.player.hasEffect(AntarchyObjects.PARALYZED_EFFECT.get())) {
            ci.cancel();
        }
    }
}
