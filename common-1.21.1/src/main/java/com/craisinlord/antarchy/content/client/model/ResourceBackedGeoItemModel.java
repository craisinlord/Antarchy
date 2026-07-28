package com.craisinlord.antarchy.content.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class ResourceBackedGeoItemModel<T extends Item & GeoAnimatable> extends GeoModel<T> {
    private final ResourceLocation modelLocation;
    private final ResourceLocation textureLocation;
    private final ResourceLocation animationLocation;

    public ResourceBackedGeoItemModel(ResourceLocation modelLocation, ResourceLocation textureLocation, ResourceLocation animationLocation) {
        this.modelLocation = modelLocation;
        this.textureLocation = textureLocation;
        this.animationLocation = animationLocation;
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return this.modelLocation;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return this.textureLocation;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return this.animationLocation;
    }
}
