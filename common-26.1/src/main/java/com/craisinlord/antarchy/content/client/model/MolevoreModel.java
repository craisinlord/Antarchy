package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class MolevoreModel extends GeoModel<MolevoreEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "molevore");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/molevore.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(Antarchy.MODID, "molevore");
    private static final float MAX_HEAD_YAW_DEGREES = 40.0F;
    private static final float MAX_HEAD_PITCH_DEGREES = 25.0F;

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(MolevoreEntity animatable) {
        return ANIMATION;
    }

    public static void applyHeadTracking(LivingEntityRenderState state, BoneSnapshots snapshots) {
        snapshots.ifPresent("head", head -> {
            head.setRotX(Mth.clamp(state.xRot, -MAX_HEAD_PITCH_DEGREES, MAX_HEAD_PITCH_DEGREES) * Mth.DEG_TO_RAD);
            head.setRotY(Mth.clamp(state.yRot, -MAX_HEAD_YAW_DEGREES, MAX_HEAD_YAW_DEGREES) * Mth.DEG_TO_RAD);
        });
    }
}
