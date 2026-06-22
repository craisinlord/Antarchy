package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ButterflyModel;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ButterflyRenderer extends GeoEntityRenderer<ButterflyEntity> {
    private static final float VISUAL_SCALE = 0.8F;
    private static final float SHADOW_RADIUS = 0.18F;

    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.withScale(VISUAL_SCALE);
    }

    @Override
    public void preRender(PoseStack poseStack, ButterflyEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        this.shadowRadius = SHADOW_RADIUS;
    }
}
