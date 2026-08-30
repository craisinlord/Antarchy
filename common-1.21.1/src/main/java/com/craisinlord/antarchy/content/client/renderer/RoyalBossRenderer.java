package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalBossModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoyalBossRenderer extends GeoEntityRenderer<RoyalBossEntity> {
    public RoyalBossRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalBossModel());
        this.shadowRadius = 8.0F;
    }

    @Override
    public RenderType getRenderType(RoyalBossEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, RoyalBossEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        poseStack.scale(RoyalBossEntity.MODEL_RENDER_SCALE, RoyalBossEntity.MODEL_RENDER_SCALE, RoyalBossEntity.MODEL_RENDER_SCALE);
        this.shadowRadius = 8.0F * RoyalBossEntity.MODEL_RENDER_SCALE;
    }
}
