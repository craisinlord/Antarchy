package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.FlytrapEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FlytrapModel extends GeoModel<FlytrapEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/flytrap.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/flytrap.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/flytrap.animation.json");

    @Override
    public ResourceLocation getModelResource(FlytrapEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FlytrapEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FlytrapEntity animatable) {
        return ANIMATION;
    }
}
