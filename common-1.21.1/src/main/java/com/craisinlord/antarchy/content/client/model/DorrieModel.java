package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DorrieModel extends GeoModel<DorrieEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dorrie.png");
    private static final ResourceLocation TEXTURE_SADDLED =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dorrie_saddle.png");

    @Override
    public ResourceLocation getModelResource(DorrieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/dorrie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DorrieEntity animatable) {
        return animatable.hasSaddle() ? TEXTURE_SADDLED : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DorrieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/dorrie.animation.json");
    }
}
