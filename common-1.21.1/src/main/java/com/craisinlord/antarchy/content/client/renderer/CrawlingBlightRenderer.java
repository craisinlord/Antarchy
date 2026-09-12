package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.CrawlingBlightModel;
import com.craisinlord.antarchy.content.entity.CrawlingBlightEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CrawlingBlightRenderer extends GeoEntityRenderer<CrawlingBlightEntity> {
    public CrawlingBlightRenderer(EntityRendererProvider.Context context) {
        super(context, new CrawlingBlightModel());
        this.shadowRadius = 0.5F;
        this.withScale(0.85F);
    }

    @Override
    public void preRender(PoseStack poseStack, CrawlingBlightEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (animatable.isWallClimbing()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.translate(0, -0.5, 0.5);
        }
    }
}
