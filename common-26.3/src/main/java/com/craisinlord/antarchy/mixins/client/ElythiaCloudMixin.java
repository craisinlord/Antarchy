package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.craisinlord.antarchy.content.client.renderer.clouds.ElythiaCloudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import com.mojang.renderpearl.api.commands.RenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public abstract class ElythiaCloudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void antarchy$renderElythiaClouds(net.minecraft.client.CloudStatus cloudStatus, RenderPass renderPass, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !ElythiaSkyRenderer.shouldRender(level)) {
            return;
        }
        LevelRenderState state = ((LevelRendererStateAccessor) minecraft.levelRenderer).antarchy$getLevelRenderState();
        Vec3 cameraPosition = state.cameraRenderState.pos;
        if (cameraPosition == null) {
            return;
        }
        ElythiaCloudRenderer.render(minecraft, level, cloudStatus, cameraPosition, state.gameTime, state.worldPartialTicks,
                state.cloudColor, 32, renderPass);
        ci.cancel();
    }
}
