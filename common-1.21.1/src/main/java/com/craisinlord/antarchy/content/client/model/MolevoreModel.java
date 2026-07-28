package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MolevoreModel extends GeoModel<MolevoreEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/molevore.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/molevore.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/molevore.animation.json");

    @Override
    public ResourceLocation getModelResource(MolevoreEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MolevoreEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MolevoreEntity animatable) {
        return ANIMATION;
    }
}
