package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class RoyalMountModel extends GeoModel<RoyalMountEntity> {
    private static final String[] HEADS = {"head_left", "head", "head_right"};

    @Override
    public ResourceLocation getModelResource(RoyalMountEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/" + animatable.geoNameForRender() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalMountEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/" + animatable.geoNameForRender() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalMountEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/" + animatable.geoNameForRender() + ".animation.json");
    }

    @Override
    public void setCustomAnimations(RoyalMountEntity animatable, long instanceId,
                                    AnimationState<RoyalMountEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        for (String head : HEADS) {
            this.getBone(head).ifPresent(bone -> bone.setTrackingMatrices(true));
        }
    }

    @Nullable
    public Vec3 getTrackedBeamAnchor(int head) {
        if (head < 0 || head >= HEADS.length) return null;
        GeoBone bone = this.getBone(HEADS[head]).orElse(null);
        if (bone == null || !bone.isTrackingMatrices()) return null;
        org.joml.Vector3d position = bone.getWorldPosition();
        return new Vec3(position.x, position.y, position.z);
    }
}
