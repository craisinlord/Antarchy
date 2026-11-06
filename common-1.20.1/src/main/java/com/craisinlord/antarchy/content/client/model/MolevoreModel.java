package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class MolevoreModel extends GeoModel<MolevoreEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Antarchy.MODID, "geo/molevore.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/molevore.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Antarchy.MODID, "animations/molevore.animation.json");
    private static final float MAX_HEAD_YAW_DEGREES = 40.0F;
    private static final float MAX_HEAD_PITCH_DEGREES = 25.0F;

    @Override
    public ResourceLocation getModelResource(MolevoreEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MolevoreEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MolevoreEntity animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(MolevoreEntity animatable, long instanceId, AnimationState<MolevoreEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        if (!animatable.isHeadTrackingActive()) {
            return;
        }

        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (data == null) {
            return;
        }

        this.getBone("head").ifPresent(head -> {
            head.setRotX(Mth.clamp(data.headPitch(), -MAX_HEAD_PITCH_DEGREES, MAX_HEAD_PITCH_DEGREES) * Mth.DEG_TO_RAD);
            head.setRotY(Mth.clamp(data.netHeadYaw(), -MAX_HEAD_YAW_DEGREES, MAX_HEAD_YAW_DEGREES) * Mth.DEG_TO_RAD);
        });
    }
}
