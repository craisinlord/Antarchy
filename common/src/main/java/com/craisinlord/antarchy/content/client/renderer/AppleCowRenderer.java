package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.AppleCowModel;
import com.craisinlord.antarchy.content.entity.AppleCowEntity;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.EnchantedGoldenAppleCow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class AppleCowRenderer extends GeoEntityRenderer<AppleCowEntity> {
    private static final float ADULT_SHADOW_RADIUS = 0.7F;
    private static final float BABY_SCALE = 0.85F;

    public AppleCowRenderer(EntityRendererProvider.Context context) {
        super(context, new AppleCowModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        this.addRenderLayer(new EnchantedLayer(this));
    }

    @Override
    public RenderType getRenderType(AppleCowEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, AppleCowEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        if (animatable.isBaby()) {
            poseStack.scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
            this.shadowRadius = ADULT_SHADOW_RADIUS * BABY_SCALE;
        } else {
            this.shadowRadius = ADULT_SHADOW_RADIUS;
        }
    }

    private static final class EnchantedLayer extends GeoRenderLayer<AppleCowEntity> {
        private EnchantedLayer(AppleCowRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, AppleCowEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            if (!(animatable instanceof EnchantedGoldenAppleCow)) {
                return;
            }

            ResourceLocation texture = AppleCowModel.textureFor(animatable);
            RenderType foilType = RenderType.entityCutoutNoCull(texture);
            VertexConsumer foilBuffer = ItemRenderer.getFoilBufferDirect(bufferSource, foilType, false, true);
            this.getRenderer().reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    foilType,
                    foilBuffer,
                    partialTick,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    0xFFFFFFFF
            );
        }
    }
}
