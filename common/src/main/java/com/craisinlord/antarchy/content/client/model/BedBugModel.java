package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BedBugModel extends GeoModel<BedBugEntity> {
    @Override
    public ResourceLocation getModelResource(BedBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/bed_bug.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BedBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/bed_bug.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BedBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/bed_bug.animation.json");
    }
}
