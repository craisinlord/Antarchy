package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.KrakenModel;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
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

public class KrakenRenderer extends GeoEntityRenderer<KrakenEntity> {
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("kraken");

    public KrakenRenderer(EntityRendererProvider.Context context) {
        super(context, new KrakenModel());
        this.shadowRadius = 2.0F;
        this.withScale(1.5F);
        this.addRenderLayer(new KrakenEmissiveLayer(this));
        this.addRenderLayer(new GlimmerEchoRendering.EmissiveLayer<>(this, GlimmerEchoRendering.emissiveTexture("kraken")));
    }

    @Override
    public ResourceLocation getTextureLocation(KrakenEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, super.getTextureLocation(animatable));
    }

    @Override
    public RenderType getRenderType(KrakenEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return GlimmerEchoRendering.renderType(animatable, texture, super.getRenderType(animatable, texture, bufferSource, partialTick));
    }

    @Override
    public Color getRenderColor(KrakenEntity animatable, float partialTick, int packedLight) {
        return GlimmerEchoRendering.renderColor(animatable, super.getRenderColor(animatable, partialTick, packedLight));
    }

    private static final class KrakenEmissiveLayer extends GeoRenderLayer<KrakenEntity> {
        private KrakenEmissiveLayer(GeoEntityRenderer<KrakenEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, KrakenEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            if (animatable.isGlimmerEcho()) {
                return;
            }
            RenderType emissiveType = RenderType.eyes(KrakenModel.EMISSIVE_TEXTURE);
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
