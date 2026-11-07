package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class NightmareModel extends GeoModel<NightmareEntity> {
    private static final float LOOK_YAW_LIMIT = 40.0F;
    private static final float LOOK_PITCH_LIMIT = 30.0F;
    private static final float NECK_SHARE = 0.4F;
    private static final ResourceLocation PHASE_ONE_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/nightmare/nightmare.png");
    private static final ResourceLocation PHASE_TWO_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/nightmare/nightmare_phase_2.png");
    private static final ResourceLocation PHASE_ONE_EMISSIVE = new ResourceLocation(Antarchy.MODID, "textures/entity/nightmare/nightmare_emissive.png");
    private static final ResourceLocation PHASE_TWO_EMISSIVE = new ResourceLocation(Antarchy.MODID, "textures/entity/nightmare/nightmare_phase_2_emissive.png");

    @Override
    public ResourceLocation getModelResource(NightmareEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/nightmare.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NightmareEntity animatable) {
        return textureFor(animatable);
    }

    @Override
    public ResourceLocation getAnimationResource(NightmareEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/nightmare.animation.json");
    }

    @Override
    public void setCustomAnimations(NightmareEntity animatable, long instanceId, AnimationState<NightmareEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        if (animatable.getActiveSpecialAnimation() != 0 || animatable.isDeadOrDying()) {
            return;
        }
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (data == null) {
            return;
        }
        float yaw = Mth.clamp(data.netHeadYaw(), -LOOK_YAW_LIMIT, LOOK_YAW_LIMIT) * Mth.DEG_TO_RAD;
        float pitch = Mth.clamp(data.headPitch(), -LOOK_PITCH_LIMIT, LOOK_PITCH_LIMIT) * Mth.DEG_TO_RAD;
        this.getBone("neck").ifPresent(bone -> {
            bone.setRotX(bone.getRotX() + pitch * NECK_SHARE);
            bone.setRotY(bone.getRotY() + yaw * NECK_SHARE);
        });
        this.getBone("head").ifPresent(bone -> {
            bone.setRotX(bone.getRotX() + pitch * (1.0F - NECK_SHARE));
            bone.setRotY(bone.getRotY() + yaw * (1.0F - NECK_SHARE));
        });
    }

    public static ResourceLocation textureFor(NightmareEntity animatable) {
        return animatable.hasPhaseTwoTexture() ? PHASE_TWO_TEXTURE : PHASE_ONE_TEXTURE;
    }

    public static ResourceLocation emissiveTextureFor(NightmareEntity animatable) {
        return animatable.hasPhaseTwoTexture() ? PHASE_TWO_EMISSIVE : PHASE_ONE_EMISSIVE;
    }
}
