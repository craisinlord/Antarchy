package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import software.bernie.geckolib.model.GeoModel;

public class HoverboardModel extends GeoModel<HoverboardEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/hoverboard.geo.json");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/hoverboard.animation.json");
    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hoverboard/hoverboard.png");

    @Override
    public ResourceLocation getModelResource(HoverboardEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HoverboardEntity animatable) {
        DyeColor color = animatable.getColor();
        if (color == null) {
            return BASE_TEXTURE;
        }
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hoverboard/hoverboard_" + color.getName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(HoverboardEntity animatable) {
        return ANIMATION;
    }
}
