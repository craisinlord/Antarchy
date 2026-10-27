package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.WaterBombModel;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;

public class WaterBombRenderer extends GeoEntityRenderer<WaterBombEntity, EntityRenderState> {
    private static final DataTicket<Float> APPLIED_YAW = DataTicket.create("antarchy_water_bomb_applied_yaw", Float.class);
    private static final DataTicket<Float> APPLIED_PITCH = DataTicket.create("antarchy_water_bomb_applied_pitch", Float.class);
    private static final DataTicket<Boolean> HUGE = DataTicket.create("antarchy_water_bomb_huge", Boolean.class);

    public WaterBombRenderer(EntityRendererProvider.Context context) {
        super(context, new WaterBombModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void addRenderData(WaterBombEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(APPLIED_YAW, Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot()));
        renderState.addGeckolibData(APPLIED_PITCH, Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot()));
        renderState.addGeckolibData(HUGE, animatable.isHuge());
    }

    @Override
    protected void applyRotations(RenderPassInfo<EntityRenderState> renderPassInfo, PoseStack poseStack, float nativeScale) {
        float appliedYaw = renderPassInfo.getOrDefaultGeckolibData(APPLIED_YAW, 0.0F);
        float appliedPitch = renderPassInfo.getOrDefaultGeckolibData(APPLIED_PITCH, 0.0F);
        poseStack.rotate(Axis.YP.rotationDegrees(appliedYaw - 90.0F));
        poseStack.rotate(Axis.XP.rotationDegrees(-appliedPitch));
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }

    @Override
    public void preRenderPass(RenderPassInfo<EntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        float scale = renderPassInfo.getOrDefaultGeckolibData(HUGE, false) ? 1.5F : 0.5F;
        renderPassInfo.poseStack().scale(scale, scale, scale);
        super.preRenderPass(renderPassInfo, renderTasks);
    }

    @Override
    protected boolean affectedByCulling(WaterBombEntity entity) {
        return false;
    }
}
