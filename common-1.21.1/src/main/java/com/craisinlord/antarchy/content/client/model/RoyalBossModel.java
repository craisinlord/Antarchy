package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalHead;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class RoyalBossModel extends GeoModel<RoyalBossEntity> {
    private static final String[] BEAM_ANCHORS = {
            "beam_anchor_left", "beam_anchor_center", "beam_anchor_right"
    };
    private static final String[][] AIM_CHAINS = {
            {"L_neck", "L_neck2", "L_neck3"},
            {"neck", "neck3", "neck4"},
            {"R_neck", "R_neck2", "R_neck3"}
    };

    @Override
    public ResourceLocation getModelResource(RoyalBossEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/" + animatable.geoNameForRender() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalBossEntity animatable) {
        String name = animatable.geoNameForRender();
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/" + name + "/" + name + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalBossEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/" + animatable.geoNameForRender() + ".animation.json");
    }

    @Override
    public void setCustomAnimations(RoyalBossEntity animatable, long instanceId,
                                    AnimationState<RoyalBossEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        for (String anchor : BEAM_ANCHORS) {
            this.getBone(anchor).ifPresent(bone -> bone.setTrackingMatrices(true));
        }

        float partialTick = animationState.getPartialTick();
        for (RoyalHead.Slot slot : RoyalHead.Slot.values()) {
            Vec3 beamEnd = animatable.getRoyalBeamEndPosition(slot);
            if (!animatable.isFiringRoyalBeam(slot) || beamEnd == null) continue;
            Vec3 origin = animatable.getRoyalBeamShootFrom(slot, partialTick);
            Vec3 direction = com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil.vecWorldToPlayer(
                    beamEnd.subtract(origin),
                    com.craisinlord.antarchy.content.gravity.AntarchyGravityApi.getGravityDirection(animatable));
            double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
            if (direction.lengthSqr() < 1.0E-6D || horizontal < 1.0E-4D) continue;

            float desiredYaw = (float) (Mth.atan2(-direction.x, direction.z) * Mth.RAD_TO_DEG);
            float bodyYaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
            float relativeYaw = Mth.clamp(Mth.wrapDegrees(desiredYaw - bodyYaw), -55.0F, 55.0F) * Mth.DEG_TO_RAD;
            float pitch = Mth.clamp((float) -Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG,
                    -45.0F, 45.0F) * Mth.DEG_TO_RAD;

            String[] chain = AIM_CHAINS[slot.ordinal()];
            float yawPerBone = relativeYaw / chain.length;
            float pitchPerBone = pitch / chain.length;
            for (String boneName : chain) {
                this.getBone(boneName).ifPresent(bone -> {
                    bone.setRotY(yawPerBone);
                    bone.setRotX(pitchPerBone);
                });
            }
        }
    }

    @Nullable
    public Vec3 getTrackedBeamAnchor(RoyalHead.Slot slot) {
        GeoBone bone = this.getBone(BEAM_ANCHORS[slot.ordinal()]).orElse(null);
        if (bone == null || !bone.isTrackingMatrices()) {
            return null;
        }
        org.joml.Vector3d position = bone.getWorldPosition();
        return new Vec3(position.x, position.y, position.z);
    }
}
