package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.LucidModel;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LucidRenderer extends GeoEntityRenderer<LucidEntity> {

    private static final float VISUAL_SCALE = 0.9F;
    private static final double VISUAL_Y_OFFSET = -0.30D;

    public LucidRenderer(EntityRendererProvider.Context context) {
        super(context, new LucidModel());
        this.shadowRadius = 0.5F;
        this.withScale(VISUAL_SCALE);
    }

    @Override
    public void preRender(PoseStack poseStack, LucidEntity animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        poseStack.translate(0.0D, VISUAL_Y_OFFSET, 0.0D);
    }

    @Override
    protected float getDeathMaxRotation(LucidEntity animatable) {
        return 0.0F;
    }
}
