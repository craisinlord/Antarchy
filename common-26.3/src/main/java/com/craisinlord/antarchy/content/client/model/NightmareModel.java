package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.base.BoneSnapshots;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class NightmareModel extends GeoModel<NightmareEntity> {
    private static final float LOOK_YAW_LIMIT = 40.0F;
    private static final float LOOK_PITCH_LIMIT = 30.0F;
    private static final float NECK_SHARE = 0.4F;
    public static final DataTicket<Boolean> PHASE_TWO_TEXTURE_TICKET = DataTicket.create("antarchy_nightmare_phase_two_texture", Boolean.class);

    private static final Identifier PHASE_ONE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare.png");
    private static final Identifier PHASE_TWO_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare_phase_2.png");
    private static final Identifier PHASE_ONE_EMISSIVE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare_emissive.png");
    private static final Identifier PHASE_TWO_EMISSIVE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare_phase_2_emissive.png");

    @Override
    public void addAdditionalStateData(NightmareEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(PHASE_TWO_TEXTURE_TICKET, animatable.hasPhaseTwoTexture());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "nightmare");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return textureFor(renderState);
    }

    @Override
    public Identifier getAnimationResource(NightmareEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "nightmare");
    }

    public static void applyHeadTracking(LivingEntityRenderState state, BoneSnapshots snapshots) {
        float yaw = Mth.clamp(state.yRot, -LOOK_YAW_LIMIT, LOOK_YAW_LIMIT) * Mth.DEG_TO_RAD;
        float pitch = Mth.clamp(state.xRot, -LOOK_PITCH_LIMIT, LOOK_PITCH_LIMIT) * Mth.DEG_TO_RAD;
        snapshots.ifPresent("neck", bone -> {
            bone.setRotX(bone.getRotX() + pitch * NECK_SHARE);
            bone.setRotY(bone.getRotY() + yaw * NECK_SHARE);
        });
        snapshots.ifPresent("head", bone -> {
            bone.setRotX(bone.getRotX() + pitch * (1.0F - NECK_SHARE));
            bone.setRotY(bone.getRotY() + yaw * (1.0F - NECK_SHARE));
        });
    }

    public static Identifier textureFor(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(PHASE_TWO_TEXTURE_TICKET, false) ? PHASE_TWO_TEXTURE : PHASE_ONE_TEXTURE;
    }

    public static Identifier emissiveTextureFor(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(PHASE_TWO_TEXTURE_TICKET, false) ? PHASE_TWO_EMISSIVE : PHASE_ONE_EMISSIVE;
    }

    public static Identifier defaultEmissiveTexture() {
        return PHASE_ONE_EMISSIVE;
    }
}
