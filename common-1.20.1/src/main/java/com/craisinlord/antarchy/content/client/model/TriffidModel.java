package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.TriffidEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TriffidModel extends GeoModel<TriffidEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Antarchy.MODID, "geo/triffid.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/triffid.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Antarchy.MODID, "animations/triffid.animation.json");

    @Override
    public ResourceLocation getModelResource(TriffidEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TriffidEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TriffidEntity animatable) {
        return ANIMATION;
    }
}
