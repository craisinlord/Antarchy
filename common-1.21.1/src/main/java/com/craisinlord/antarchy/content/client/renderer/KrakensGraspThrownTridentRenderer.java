package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.kraken.KrakensGraspThrownTrident;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class KrakensGraspThrownTridentRenderer extends EntityRenderer<KrakensGraspThrownTrident> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/item/krakens_grasp/krakens_grasp.png");

    public KrakensGraspThrownTridentRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(KrakensGraspThrownTrident entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity.getItem(),
                ItemDisplayContext.NONE,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(KrakensGraspThrownTrident entity) {
        return TEXTURE;
    }
}
