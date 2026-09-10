package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.WormEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WormModel extends GeoModel<WormEntity> {
    private static final ResourceLocation SMALL_MODEL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/worm_small.geo.json");
    private static final ResourceLocation MEDIUM_MODEL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/worm_medium.geo.json");
    private static final ResourceLocation LARGE_MODEL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/worm_large.geo.json");
    private static final ResourceLocation SMALL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/worm/worm_small.png");
    private static final ResourceLocation MEDIUM_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/worm/worm_medium.png");
    private static final ResourceLocation LARGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/worm/worm_large.png");
    private static final ResourceLocation SMALL_ANIMATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/worm_small.animation.json");
    private static final ResourceLocation MEDIUM_ANIMATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/worm_medium.animation.json");
    private static final ResourceLocation LARGE_ANIMATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/worm_large.animation.json");

    @Override
    public ResourceLocation getModelResource(WormEntity animatable) {
        return switch (animatable.getGrowthStage()) {
            case SMALL -> SMALL_MODEL;
            case MEDIUM -> MEDIUM_MODEL;
            case LARGE -> LARGE_MODEL;
        };
    }

    @Override
    public ResourceLocation getTextureResource(WormEntity animatable) {
        return switch (animatable.getGrowthStage()) {
            case SMALL -> SMALL_TEXTURE;
            case MEDIUM -> MEDIUM_TEXTURE;
            case LARGE -> LARGE_TEXTURE;
        };
    }

    @Override
    public ResourceLocation getAnimationResource(WormEntity animatable) {
        return switch (animatable.getGrowthStage()) {
            case SMALL -> SMALL_ANIMATION;
            case MEDIUM -> MEDIUM_ANIMATION;
            case LARGE -> LARGE_ANIMATION;
        };
    }
}
