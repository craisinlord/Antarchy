package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class StinkBugModel extends GeoModel<StinkBugEntity> {
    @Override
    public ResourceLocation getModelResource(StinkBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/stink_bug.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StinkBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/stink_bug.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StinkBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/stink_bug.animation.json");
    }
}
