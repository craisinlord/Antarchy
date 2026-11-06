package com.craisinlord.antarchy.content.client.model;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class ResourceBackedGeoItemModel<T extends Item & GeoAnimatable> extends GeoModel<T> {
    private final Identifier modelLocation;
    private final Identifier textureLocation;
    private final Identifier animationLocation;

    public ResourceBackedGeoItemModel(Identifier modelLocation, Identifier textureLocation, Identifier animationLocation) {
        this.modelLocation = modelLocation;
        this.textureLocation = textureLocation;
        this.animationLocation = animationLocation;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return this.modelLocation;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return this.textureLocation;
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return this.animationLocation;
    }
}
