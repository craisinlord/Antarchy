package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.LucidBoltModel;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LucidBoltRenderer extends GeoEntityRenderer<LucidBoltEntity> {
    public LucidBoltRenderer(EntityRendererProvider.Context context) {
        super(context, new LucidBoltModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    protected void applyRotations(LucidBoltEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        float appliedYaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        float appliedPitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - appliedYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-appliedPitch));
    }
}
