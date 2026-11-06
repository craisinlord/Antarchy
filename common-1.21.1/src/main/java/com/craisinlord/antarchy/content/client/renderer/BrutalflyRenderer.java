package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.BrutalflyModel;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.Color;

public class BrutalflyRenderer extends GeoEntityRenderer<BrutalflyEntity> {

    private static final double COCOONED_VISUAL_Y_OFFSET = 1.0D;
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("brutalfly");

    public BrutalflyRenderer(EntityRendererProvider.Context context) {
        super(context, new BrutalflyModel());
        this.shadowRadius = 1.35F;
        this.addRenderLayer(new BrutalflyEmissiveLayer(this));
        this.addRenderLayer(new GlimmerEchoRendering.EmissiveLayer<>(this, GlimmerEchoRendering.emissiveTexture("brutalfly")));
    }

    @Override
    public ResourceLocation getTextureLocation(BrutalflyEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, super.getTextureLocation(animatable));
    }

    @Override
    public Color getRenderColor(BrutalflyEntity animatable, float partialTick, int packedLight) {
        return GlimmerEchoRendering.renderColor(animatable, super.getRenderColor(animatable, partialTick, packedLight));
    }

    @Override
    public RenderType getRenderType(BrutalflyEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (animatable.isCocooned()) {
            return RenderType.entityCutoutNoCull(texture);
        }

        return GlimmerEchoRendering.renderType(animatable, texture, super.getRenderType(animatable, texture, bufferSource, partialTick));
    }

    @Override
    public void preRender(PoseStack poseStack, BrutalflyEntity animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (animatable.isCocooned()) {
            this.shadowRadius = 0.25F;
            poseStack.translate(0.0D, COCOONED_VISUAL_Y_OFFSET, 0.0D);
        } else {
            this.shadowRadius = 1.35F;
        }
    }

    @Override
    protected int getBlockLightLevel(BrutalflyEntity entity, BlockPos pos) {
        return entity.isCocooned() ? 8 : super.getBlockLightLevel(entity, pos);
    }

    @Override
    protected float getDeathMaxRotation(BrutalflyEntity animatable) {
        return 0.0F;
    }

    private static final class BrutalflyEmissiveLayer extends GeoRenderLayer<BrutalflyEntity> {
        private BrutalflyEmissiveLayer(GeoEntityRenderer<BrutalflyEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, BrutalflyEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            if (GlimmerEchoes.isGlimmerEcho(animatable)) {
                return;
            }
            RenderType emissiveType = RenderType.eyes(BrutalflyModel.EMISSIVE_TEXTURE);
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
                    0xFFFFFFFF
            );
        }
    }
}
