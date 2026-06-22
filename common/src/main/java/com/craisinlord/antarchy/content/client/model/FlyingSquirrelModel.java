package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class FlyingSquirrelModel extends GeoModel<FlyingSquirrelEntity> {
    private static final ResourceLocation REGULAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/flying_squirrel/flying_squirrel.png");
    private static final ResourceLocation BROWN_TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/flying_squirrel/flying_squirrel_brown.png");
    private static final ResourceLocation ALBINO_TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/flying_squirrel/flying_squirrel_albino.png");
    private static final float SHOULDER_BODY_ROT_X = 65.0F * Mth.DEG_TO_RAD;
    private static final float SHOULDER_LEFT_ARM_ROT_Z = 0.0F;
    private static final float SHOULDER_RIGHT_ARM_ROT_Z = 0.0F;
    private static final float SHOULDER_TAIL_ROT_X = 42.5F * Mth.DEG_TO_RAD;
    private static final float SHOULDER_HEAD_ROT_X = -60.0F * Mth.DEG_TO_RAD;

    @Override
    public ResourceLocation getModelResource(FlyingSquirrelEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "geo/flying_squirrel.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FlyingSquirrelEntity animatable) {
        return switch (animatable.getTextureVariantName()) {
            case "brown" -> BROWN_TEXTURE;
            case "albino" -> ALBINO_TEXTURE;
            default -> REGULAR_TEXTURE;
        };
    }

    @Override
    public ResourceLocation getAnimationResource(FlyingSquirrelEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "animations/flying_squirrel.animation.json");
    }

    @Override
    public void setCustomAnimations(FlyingSquirrelEntity animatable, long instanceId, AnimationState<FlyingSquirrelEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        this.getBone("squirrel").ifPresent(root -> root.setScaleX(animatable.shouldMirrorIdleAnimation() ? -1.0F : 1.0F));

        if (animatable.isOnShoulder()) {
            this.getBone("body").ifPresent(bone -> bone.setRotX(SHOULDER_BODY_ROT_X));
            this.getBone("left_arm").ifPresent(bone -> bone.setRotZ(SHOULDER_LEFT_ARM_ROT_Z));
            this.getBone("right_arm").ifPresent(bone -> bone.setRotZ(SHOULDER_RIGHT_ARM_ROT_Z));
            this.getBone("tail").ifPresent(bone -> bone.setRotX(SHOULDER_TAIL_ROT_X));
            this.getBone("head").ifPresent(bone -> {
                bone.setRotX(SHOULDER_HEAD_ROT_X);
                bone.setPosY(-1.0425F);
                bone.setPosZ(-1.31366F);
            });
            this.getBone("left_flying_part").ifPresent(FlyingSquirrelModel::hideBone);
            this.getBone("right_flying_part").ifPresent(FlyingSquirrelModel::hideBone);
            this.getBone("left_cheeks").ifPresent(FlyingSquirrelModel::hideBone);
            this.getBone("right_cheeks").ifPresent(FlyingSquirrelModel::hideBone);
        }
    }

    private static void hideBone(GeoBone bone) {
        bone.setScaleX(0.0F);
        bone.setScaleY(0.0F);
        bone.setScaleZ(0.0F);
    }
}
