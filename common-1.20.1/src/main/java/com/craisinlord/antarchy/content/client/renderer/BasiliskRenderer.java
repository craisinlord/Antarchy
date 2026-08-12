package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.BasiliskModel;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
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

public class BasiliskRenderer extends GeoEntityRenderer<BasiliskEntity> {

    public BasiliskRenderer(EntityRendererProvider.Context context) {
        super(context, new BasiliskModel());
        this.shadowRadius = 1.0F;
        this.addRenderLayer(new BasiliskEmissiveLayer(this));
    }

    @Override
    public void preRender(PoseStack poseStack, BasiliskEntity animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        if (animatable.isDeadOrDying()) {
            poseStack.translate(0.0D, -0.35D, 0.0D);
        }
    }

    @Override
    protected float getDeathMaxRotation(BasiliskEntity animatable) {
        return 0.0F;
    }

    private static final class BasiliskEmissiveLayer extends GeoRenderLayer<BasiliskEntity> {
        private BasiliskEmissiveLayer(GeoEntityRenderer<BasiliskEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, BasiliskEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            RenderType emissiveType = RenderType.eyes(BasiliskModel.EMISSIVE_TEXTURE);
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
