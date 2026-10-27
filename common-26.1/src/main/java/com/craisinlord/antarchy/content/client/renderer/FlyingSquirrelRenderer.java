package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.FlyingSquirrelModel;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;

public class FlyingSquirrelRenderer extends GeoEntityRenderer<FlyingSquirrelEntity, LivingEntityRenderState> {
    private static final DataTicket<Boolean> BABY = DataTicket.create("antarchy_flying_squirrel_baby", Boolean.class);
    private static final DataTicket<Boolean> ON_SHOULDER = DataTicket.create("antarchy_flying_squirrel_on_shoulder", Boolean.class);
    private static final DataTicket<Boolean> MIRRORED = DataTicket.create("antarchy_flying_squirrel_mirrored", Boolean.class);
    private static final DataTicket<Float> APPLIED_PITCH = DataTicket.create("antarchy_flying_squirrel_applied_pitch", Float.class);
    private static final float ADULT_SHADOW_RADIUS = 0.3F;
    private static final float BABY_SCALE = 0.6F;
    private static final float SHOULDER_BODY_ROT_X = 65.0F * Mth.DEG_TO_RAD;
    private static final float SHOULDER_LEFT_ARM_ROT_Z = 0.0F;
    private static final float SHOULDER_RIGHT_ARM_ROT_Z = 0.0F;
    private static final float SHOULDER_TAIL_ROT_X = 42.5F * Mth.DEG_TO_RAD;
    private static final float SHOULDER_HEAD_ROT_X = -60.0F * Mth.DEG_TO_RAD;

    public FlyingSquirrelRenderer(EntityRendererProvider.Context context) {
        super(context, new FlyingSquirrelModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
    }

    @Override
    public void extractRenderState(FlyingSquirrelEntity entity, LivingEntityRenderState renderState, float partialTick) {
        this.shadowRadius = entity.isBaby() ? ADULT_SHADOW_RADIUS * BABY_SCALE : ADULT_SHADOW_RADIUS;
        super.extractRenderState(entity, renderState, partialTick);
    }

    @Override
    public void addRenderData(FlyingSquirrelEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        boolean onShoulder = animatable.isOnShoulder();
        float appliedPitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        if (onShoulder && animatable.getVehicle() instanceof Player player) {
            renderState.addGeckolibData(DataTickets.ENTITY_BODY_YAW, Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot));
            appliedPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        }
        renderState.addGeckolibData(BABY, animatable.isBaby());
        renderState.addGeckolibData(ON_SHOULDER, onShoulder);
        renderState.addGeckolibData(MIRRORED, animatable.shouldMirrorIdleAnimation());
        renderState.addGeckolibData(APPLIED_PITCH, appliedPitch);
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);

        if (renderPassInfo.getOrDefaultGeckolibData(BABY, false)) {
            renderPassInfo.poseStack().scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
        }
    }

    @Override
    protected void applyRotations(RenderPassInfo<LivingEntityRenderState> renderPassInfo, PoseStack poseStack, float nativeScale) {
        super.applyRotations(renderPassInfo, poseStack, nativeScale);

        if (renderPassInfo.getOrDefaultGeckolibData(ON_SHOULDER, false)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-renderPassInfo.getOrDefaultGeckolibData(APPLIED_PITCH, 0.0F)));
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        boolean mirrored = renderPassInfo.getOrDefaultGeckolibData(MIRRORED, false);
        snapshots.ifPresent("squirrel", root -> root.setScaleX(mirrored ? -1.0F : 1.0F));

        if (renderPassInfo.getOrDefaultGeckolibData(ON_SHOULDER, false)) {
            snapshots.ifPresent("body", bone -> bone.setRotX(SHOULDER_BODY_ROT_X));
            snapshots.ifPresent("left_arm", bone -> bone.setRotZ(SHOULDER_LEFT_ARM_ROT_Z));
            snapshots.ifPresent("right_arm", bone -> bone.setRotZ(SHOULDER_RIGHT_ARM_ROT_Z));
            snapshots.ifPresent("tail", bone -> bone.setRotX(SHOULDER_TAIL_ROT_X));
            snapshots.ifPresent("head", bone -> {
                bone.setRotX(SHOULDER_HEAD_ROT_X);
                bone.setTranslateY(-1.0425F);
                bone.setTranslateZ(-1.31366F);
            });
            snapshots.ifPresent("left_flying_part", FlyingSquirrelRenderer::hideBone);
            snapshots.ifPresent("right_flying_part", FlyingSquirrelRenderer::hideBone);
            snapshots.ifPresent("left_cheeks", FlyingSquirrelRenderer::hideBone);
            snapshots.ifPresent("right_cheeks", FlyingSquirrelRenderer::hideBone);
        }
    }

    private static void hideBone(BoneSnapshot bone) {
        bone.setScaleX(0.0F);
        bone.setScaleY(0.0F);
        bone.setScaleZ(0.0F);
    }
}
