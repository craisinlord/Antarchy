package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.SpitBugEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpitBugModel extends GeoModel<SpitBugEntity> {
    @Override
    public ResourceLocation getModelResource(SpitBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/spit_bug.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpitBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/spit_bug.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpitBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/spit_bug.animation.json");
    }
}
