package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ScorpionModel;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.Color;

public class ScorpionRenderer extends GeoEntityRenderer<ScorpionEntity> {
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("scorpion");

    public ScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new ScorpionModel());
        this.shadowRadius = 0.6F;
        this.addRenderLayer(new ScorpionEmissiveLayer(this));
        this.addRenderLayer(new GlimmerEchoRendering.EmissiveLayer<>(this, GlimmerEchoRendering.emissiveTexture("scorpion")));
    }

    @Override
    public ResourceLocation getTextureLocation(ScorpionEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, super.getTextureLocation(animatable));
    }

    @Override
    public RenderType getRenderType(ScorpionEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return GlimmerEchoRendering.renderType(animatable, texture, RenderType.entityCutoutNoCull(texture));
    }

    @Override
    public Color getRenderColor(ScorpionEntity animatable, float partialTick, int packedLight) {
        return GlimmerEchoRendering.renderColor(animatable, super.getRenderColor(animatable, partialTick, packedLight));
    }

    private static final class ScorpionEmissiveLayer extends GeoRenderLayer<ScorpionEntity> {
        private ScorpionEmissiveLayer(GeoEntityRenderer<ScorpionEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, ScorpionEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            if (GlimmerEchoes.isGlimmerEcho(animatable)) {
                return;
            }
            RenderType emissiveType = RenderType.eyes(ScorpionModel.emissiveTextureFor(animatable));
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
