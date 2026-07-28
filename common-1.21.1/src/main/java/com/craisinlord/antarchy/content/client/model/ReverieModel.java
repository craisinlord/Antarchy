package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ReverieModel extends GeoModel<ReverieEntity> {
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_white.png");
    private static final ResourceLocation YELLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_yellow.png");
    private static final ResourceLocation BLUE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_blue.png");
    private static final ResourceLocation PURPLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_purple.png");
    private static final ResourceLocation RED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_red.png");

    @Override
    public ResourceLocation getModelResource(ReverieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/reverie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ReverieEntity animatable) {
        return textureForMood(animatable.getMood());
    }

    @Override
    public ResourceLocation getAnimationResource(ReverieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/reverie.animation.json");
    }

    public static ResourceLocation textureForMood(ReverieEntity.Mood mood) {
        return switch (mood) {
            case WHITE -> WHITE_TEXTURE;
            case YELLOW -> YELLOW_TEXTURE;
            case BLUE -> BLUE_TEXTURE;
            case PURPLE -> PURPLE_TEXTURE;
            case RED -> RED_TEXTURE;
            default -> WHITE_TEXTURE;
        };
    }
}
