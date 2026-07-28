package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ScorpionModel;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
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

public class ScorpionRenderer extends GeoEntityRenderer<ScorpionEntity> {

    public ScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new ScorpionModel());
        this.shadowRadius = 0.6F;
        this.addRenderLayer(new ScorpionEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(ScorpionEntity animatable, net.minecraft.resources.ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    private static final class ScorpionEmissiveLayer extends GeoRenderLayer<ScorpionEntity> {
        private ScorpionEmissiveLayer(GeoEntityRenderer<ScorpionEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, ScorpionEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
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
