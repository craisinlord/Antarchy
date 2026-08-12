package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class SeashellModel extends GeoModel<SeashellBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Antarchy.MODID, "geo/seashell.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/block/seashell.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Antarchy.MODID, "animations/seashell.animation.json");

    @Override
    public ResourceLocation getModelResource(SeashellBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SeashellBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SeashellBlockEntity animatable) {
        return ANIMATION;
    }
}
