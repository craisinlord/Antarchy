package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.CloudSharkModel;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
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

public class CloudSharkRenderer extends GeoEntityRenderer<CloudSharkEntity> {
    private static final float SHADOW_RADIUS = 0.6F;

    public CloudSharkRenderer(EntityRendererProvider.Context context) {
        super(context, new CloudSharkModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.addRenderLayer(new CloudSharkEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(CloudSharkEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    private static final class CloudSharkEmissiveLayer extends GeoRenderLayer<CloudSharkEntity> {
        private CloudSharkEmissiveLayer(GeoEntityRenderer<CloudSharkEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, CloudSharkEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            RenderType emissiveType = RenderType.eyes(CloudSharkModel.EMISSIVE_TEXTURE);
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
