package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CloudSharkModel extends GeoModel<CloudSharkEntity> {
    public static final ResourceLocation BASE_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/cloud_shark/cloud_shark.png");
    public static final ResourceLocation EMISSIVE_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/cloud_shark/cloud_shark_emissive.png");

    @Override
    public ResourceLocation getModelResource(CloudSharkEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/cloud_shark.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CloudSharkEntity animatable) {
        return BASE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CloudSharkEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/cloud_shark.animation.json");
    }
}
