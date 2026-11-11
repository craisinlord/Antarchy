package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.clouds.ElythiaCloudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class ElythiaCloudOrderMixin {
    @Inject(method = "executeOutline", at = @At("HEAD"))
    private void antarchy$renderQueuedElythiaClouds(CallbackInfo ci) {
        ElythiaCloudRenderer.renderQueued(Minecraft.getInstance());
    }
}
