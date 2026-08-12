package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class TigerEyeArmorHideMixin {
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$hideArmorWhileCamouflaged(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                                      LivingEntity entity, float limbSwing, float limbSwingAmount,
                                                      float partialTick, float ageInTicks, float netHeadYaw,
                                                      float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player) || !TigerEyeArmorUtil.hasFullSet(player)) {
            return;
        }

        TigerEyeCamouflageClientState.CamouflageState state = TigerEyeCamouflageClientState.get(player.getId());
        if (state != null && state.active()) {
            ci.cancel();
        }
    }
}
