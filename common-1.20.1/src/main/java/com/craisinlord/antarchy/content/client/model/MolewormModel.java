package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MolewormModel extends GeoModel<MolewormEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Antarchy.MODID, "geo/moleworm.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/moleworm.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Antarchy.MODID, "animations/moleworm.animation.json");

    @Override
    public ResourceLocation getModelResource(MolewormEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MolewormEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MolewormEntity animatable) {
        return ANIMATION;
    }
}
