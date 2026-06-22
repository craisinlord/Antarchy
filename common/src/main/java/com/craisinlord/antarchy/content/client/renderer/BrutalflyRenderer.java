package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.BrutalflyModel;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BrutalflyRenderer extends GeoEntityRenderer<BrutalflyEntity> {

    private static final double COCOONED_VISUAL_Y_OFFSET = 1.0D;

    public BrutalflyRenderer(EntityRendererProvider.Context context) {
        super(context, new BrutalflyModel());
        this.shadowRadius = 1.35F;
    }

    @Override
    public RenderType getRenderType(BrutalflyEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (animatable.isCocooned()) {
            return RenderType.entityCutoutNoCull(texture);
        }

        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public void preRender(PoseStack poseStack, BrutalflyEntity animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (animatable.isCocooned()) {
            this.shadowRadius = 0.25F;
            poseStack.translate(0.0D, COCOONED_VISUAL_Y_OFFSET, 0.0D);
        } else {
            this.shadowRadius = 1.35F;
        }
    }

    @Override
    protected int getBlockLightLevel(BrutalflyEntity entity, BlockPos pos) {
        return entity.isCocooned() ? 8 : super.getBlockLightLevel(entity, pos);
    }

    @Override
    protected float getDeathMaxRotation(BrutalflyEntity animatable) {
        return 0.0F;
    }
}
