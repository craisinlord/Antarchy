package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.StratosharkModel;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkEntity;
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

public class StratosharkRenderer extends GeoEntityRenderer<StratosharkEntity> {
    private static final float SHADOW_RADIUS = 0.6F;

    public StratosharkRenderer(EntityRendererProvider.Context context) {
        super(context, new StratosharkModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.addRenderLayer(new StratosharkEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(StratosharkEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    private static final class StratosharkEmissiveLayer extends GeoRenderLayer<StratosharkEntity> {
        private StratosharkEmissiveLayer(GeoEntityRenderer<StratosharkEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, StratosharkEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            RenderType emissiveType = RenderType.eyes(StratosharkModel.EMISSIVE_TEXTURE);
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
