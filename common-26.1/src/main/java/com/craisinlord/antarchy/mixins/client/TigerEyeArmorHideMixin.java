package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.util.AntarchyLivingRenderStateAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class TigerEyeArmorHideMixin {
    @Inject(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$hideArmorWhileCamouflaged(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                                                      HumanoidRenderState state, float yRot, float xRot, CallbackInfo ci) {
        if (((AntarchyLivingRenderStateAccess) state).antarchy$isTigerEyeCamouflaged()) {
            ci.cancel();
        }
    }
}
