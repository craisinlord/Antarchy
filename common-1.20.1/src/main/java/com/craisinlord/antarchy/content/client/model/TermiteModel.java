package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ant.TermiteEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TermiteModel extends GeoModel<TermiteEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Antarchy.MODID, "geo/termite.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/termite.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(Antarchy.MODID, "animations/termite.animation.json");

    @Override
    public ResourceLocation getModelResource(TermiteEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TermiteEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TermiteEntity animatable) {
        return ANIMATIONS;
    }
}
