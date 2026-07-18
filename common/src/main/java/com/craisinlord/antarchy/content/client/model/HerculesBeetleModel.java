package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HerculesBeetleModel extends GeoModel<HerculesBeetleEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hercules_beetle/hercules_beetle.png");
    private static final ResourceLocation TEXTURE_SADDLED =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hercules_beetle/hercules_beetle_saddle.png");
    public static final ResourceLocation EMISSIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hercules_beetle/hercules_beetle_emissive.png");

    @Override
    public ResourceLocation getModelResource(HerculesBeetleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/hercules_beetle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HerculesBeetleEntity animatable) {
        return animatable.hasSaddle() ? TEXTURE_SADDLED : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HerculesBeetleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/hercules_beetle.animation.json");
    }
}
