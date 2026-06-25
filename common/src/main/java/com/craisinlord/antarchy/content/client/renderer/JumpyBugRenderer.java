package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.JumpyBugModel;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class JumpyBugRenderer extends GeoEntityRenderer<JumpyBugEntity> {
    public JumpyBugRenderer(EntityRendererProvider.Context context) {
        super(context, new JumpyBugModel());
        this.shadowRadius = 3.0F;
    }

    @Override
    public RenderType getRenderType(JumpyBugEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return animatable.isCamouflaged() ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public Color getRenderColor(JumpyBugEntity animatable, float partialTick, int packedLight) {
        int alpha = Math.max(10, Math.min(255, Math.round(animatable.getVisualAlpha(partialTick) * 255.0F)));
        return Color.ofRGBA(255, 255, 255, alpha);
    }

    @Override
    public void preRender(PoseStack poseStack, JumpyBugEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        this.shadowRadius = animatable.isCamouflaged() ? 1.0F : 3.0F;

        if (animatable.isClingingToCeiling()) {
            poseStack.translate(0.0F, animatable.getBbHeight() + 0.05F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }
}
