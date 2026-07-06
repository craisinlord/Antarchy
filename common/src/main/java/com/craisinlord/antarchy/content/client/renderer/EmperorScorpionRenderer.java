package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.EmperorScorpionModel;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
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

public class EmperorScorpionRenderer extends GeoEntityRenderer<EmperorScorpionEntity> {

    public EmperorScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new EmperorScorpionModel());
        this.shadowRadius = 3.0F;
        this.addRenderLayer(new EmperorScorpionEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(EmperorScorpionEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    protected float getDeathMaxRotation(EmperorScorpionEntity animatable) {
        return 0.0F;
    }

    private static final class EmperorScorpionEmissiveLayer extends GeoRenderLayer<EmperorScorpionEntity> {
        private EmperorScorpionEmissiveLayer(GeoEntityRenderer<EmperorScorpionEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, EmperorScorpionEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            RenderType emissiveType = RenderType.eyes(EmperorScorpionModel.EMISSIVE_TEXTURE);
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
