package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalBlackHoleModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalBlackHoleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoyalBlackHoleRenderer extends GeoEntityRenderer<RoyalBlackHoleEntity> {
    public RoyalBlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalBlackHoleModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(RoyalBlackHoleEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        float scale = 1.5F * entity.visualGrowth();
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, 0xF000F0);
        poseStack.popPose();
    }

    @Override
    public @Nullable RenderType getRenderType(RoyalBlackHoleEntity animatable, ResourceLocation texture,
                                               @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}
