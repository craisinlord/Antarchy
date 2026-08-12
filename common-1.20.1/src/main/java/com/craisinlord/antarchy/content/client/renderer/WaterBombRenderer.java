package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.WaterBombModel;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WaterBombRenderer extends GeoEntityRenderer<WaterBombEntity> {

    public WaterBombRenderer(EntityRendererProvider.Context context) {
        super(context, new WaterBombModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    protected void applyRotations(WaterBombEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        float appliedYaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        float appliedPitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(appliedYaw - 90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-appliedPitch));
    }

    @Override
    protected float getDeathMaxRotation(WaterBombEntity animatable) {
        return 0.0F;
    }

    @Override
    public void preRender(PoseStack poseStack, WaterBombEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                           @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        float scale = animatable.isHuge() ? 1.5F : 0.5F;
        poseStack.scale(scale, scale, scale);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
