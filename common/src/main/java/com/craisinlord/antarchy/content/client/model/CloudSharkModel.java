package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CloudSharkModel extends GeoModel<CloudSharkEntity> {
    @Override
    public ResourceLocation getModelResource(CloudSharkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/cloud_shark.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CloudSharkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/cloud_shark.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CloudSharkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/cloud_shark.animation.json");
    }
}
