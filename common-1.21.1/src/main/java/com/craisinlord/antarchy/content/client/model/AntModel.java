package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.craisinlord.antarchy.content.entity.ant.BrownAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RainbowAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RedAntEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AntModel extends GeoModel<BaseAntEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/ant.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/ant.animation.json");
    private static final ResourceLocation BROWN_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/brown_ant.png");
    private static final ResourceLocation RED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/red_ant.png");
    private static final ResourceLocation RAINBOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/rainbow_ant.png");

    @Override
    public ResourceLocation getModelResource(BaseAntEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BaseAntEntity animatable) {
        if (animatable instanceof RedAntEntity) {
            return RED_TEXTURE;
        }
        if (animatable instanceof RainbowAntEntity) {
            return RAINBOW_TEXTURE;
        }
        if (animatable instanceof BrownAntEntity) {
            return BROWN_TEXTURE;
        }
        return BROWN_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BaseAntEntity animatable) {
        return ANIMATIONS;
    }
}
