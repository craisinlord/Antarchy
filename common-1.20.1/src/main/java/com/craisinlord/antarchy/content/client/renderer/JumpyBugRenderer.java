package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.JumpyBugModel;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.core.object.Color;

public class JumpyBugRenderer extends GeoEntityRenderer<JumpyBugEntity> {
    public JumpyBugRenderer(EntityRendererProvider.Context context) {
        super(context, new JumpyBugModel());
        this.shadowRadius = 6.0F;
        this.addRenderLayer(new JumpyBugEmissiveLayer(this));
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
    public void preRender(PoseStack poseStack, JumpyBugEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        this.shadowRadius = animatable.isCamouflaged() ? 1.0F : 6.0F;

        if (animatable.isClingingToCeiling()) {
            poseStack.translate(0.0F, animatable.getBbHeight() + 0.05F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    private static final class JumpyBugEmissiveLayer extends GeoRenderLayer<JumpyBugEntity> {
        private JumpyBugEmissiveLayer(GeoEntityRenderer<JumpyBugEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, JumpyBugEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            RenderType emissiveType = animatable.isCamouflaged()
                    ? RenderType.entityTranslucent(JumpyBugModel.EMISSIVE_TEXTURE)
                    : RenderType.eyes(JumpyBugModel.EMISSIVE_TEXTURE);
            float alpha = Math.max(10, Math.min(255, Math.round(animatable.getVisualAlpha(partialTick) * 255.0F))) / 255.0F;
            VertexConsumer emissiveBuffer = bufferSource.getBuffer(emissiveType);
            this.getRenderer().reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    emissiveType,
                    emissiveBuffer,
                    partialTick,
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    1.0F,
                    1.0F,
                    1.0F,
                    alpha
            );
        }
    }
}
