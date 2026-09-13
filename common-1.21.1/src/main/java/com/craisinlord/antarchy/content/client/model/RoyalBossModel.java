package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalHead;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class RoyalBossModel extends GeoModel<RoyalBossEntity> {
    private static final String[] BEAM_ANCHORS = {
            "beam_anchor_left", "beam_anchor_center", "beam_anchor_right"
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
