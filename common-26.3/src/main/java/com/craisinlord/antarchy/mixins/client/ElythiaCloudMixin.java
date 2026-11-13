package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.craisinlord.antarchy.content.client.renderer.clouds.ElythiaCloudRenderer;
import net.minecraft.client.CloudStatus;
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
    @Inject(method = "prepare(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V", at = @At("HEAD"), cancellable = true)
    private void antarchy$renderElythiaClouds(int color, CloudStatus cloudStatus, float bottomY, int range,
                                               Vec3 cameraPosition, long gameTime, float partialTicks, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !ElythiaSkyRenderer.shouldRender(level)) {
            return;
        }
        ElythiaCloudRenderer.queue(cloudStatus, cameraPosition, gameTime, partialTicks, color, range);
        ci.cancel();
    }

    @Inject(
            method = {
                    "render(Lnet/minecraft/client/CloudStatus;Lcom/mojang/renderpearl/api/commands/RenderPass;)V",
                    "renderOit"
            },
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$skipVanillaClouds(CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && ElythiaSkyRenderer.shouldRender(level)) {
            ci.cancel();
        }
    }
}
