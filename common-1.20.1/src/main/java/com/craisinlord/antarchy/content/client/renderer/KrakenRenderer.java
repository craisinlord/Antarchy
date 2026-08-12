package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.KrakenModel;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class KrakenRenderer extends GeoEntityRenderer<KrakenEntity> {
    public KrakenRenderer(EntityRendererProvider.Context context) {
        super(context, new KrakenModel());
        this.shadowRadius = 2.0F;
        this.withScale(1.5F);
        this.addRenderLayer(new KrakenEmissiveLayer(this));
    }

    private static final class KrakenEmissiveLayer extends GeoRenderLayer<KrakenEntity> {
        private KrakenEmissiveLayer(GeoEntityRenderer<KrakenEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, KrakenEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
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
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );
        }
    }
}
