package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.LucidBoltModel;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;

public class LucidBoltRenderer extends GeoEntityRenderer<LucidBoltEntity, EntityRenderState> {
    private static final DataTicket<Float> APPLIED_YAW = DataTicket.create("antarchy_lucid_bolt_applied_yaw", Float.class);
    private static final DataTicket<Float> APPLIED_PITCH = DataTicket.create("antarchy_lucid_bolt_applied_pitch", Float.class);

    public LucidBoltRenderer(EntityRendererProvider.Context context) {
        super(context, new LucidBoltModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void addRenderData(LucidBoltEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(APPLIED_YAW, Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot()));
        renderState.addGeckolibData(APPLIED_PITCH, Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot()));
    }

    @Override
    protected void applyRotations(RenderPassInfo<EntityRenderState> renderPassInfo, PoseStack poseStack, float nativeScale) {
        float appliedYaw = renderPassInfo.getOrDefaultGeckolibData(APPLIED_YAW, 0.0F);
        float appliedPitch = renderPassInfo.getOrDefaultGeckolibData(APPLIED_PITCH, 0.0F);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0F - appliedYaw));
        poseStack.rotate(Axis.XP.rotationDegrees(-appliedPitch));
    }

    @Override
    protected boolean affectedByCulling(LucidBoltEntity entity) {
        return false;
    }
}
