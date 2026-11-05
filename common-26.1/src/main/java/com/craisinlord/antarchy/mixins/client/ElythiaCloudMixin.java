package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.craisinlord.antarchy.content.client.renderer.clouds.ElythiaCloudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public abstract class ElythiaCloudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void antarchy$renderElythiaClouds(int color, net.minecraft.client.CloudStatus cloudStatus, float bottomY, int range,
                                               Vec3 cameraPosition, long gameTime, float partialTicks, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !ElythiaSkyRenderer.shouldRender(level)) {
            return;
        }
        ElythiaCloudRenderer.render(minecraft, level, cloudStatus, cameraPosition, gameTime, partialTicks, color, range);
        ci.cancel();
    }
}
