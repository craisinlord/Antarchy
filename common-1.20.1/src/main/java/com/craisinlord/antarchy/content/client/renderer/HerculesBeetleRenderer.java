package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.HerculesBeetleModel;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
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

public class HerculesBeetleRenderer extends GeoEntityRenderer<HerculesBeetleEntity> {
    public HerculesBeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new HerculesBeetleModel());
        this.addRenderLayer(new HerculesBeetleEmissiveLayer(this));
    }

    private static final class HerculesBeetleEmissiveLayer extends GeoRenderLayer<HerculesBeetleEntity> {
        private HerculesBeetleEmissiveLayer(GeoEntityRenderer<HerculesBeetleEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, HerculesBeetleEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            RenderType emissiveType = RenderType.eyes(HerculesBeetleModel.EMISSIVE_TEXTURE);
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
