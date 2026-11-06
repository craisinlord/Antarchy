package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class ReverieModel extends GeoModel<ReverieEntity> {
    public static final DataTicket<ReverieEntity.Mood> MOOD = DataTicket.create("antarchy_reverie_mood", ReverieEntity.Mood.class);
    private static final Identifier WHITE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_white.png");
    private static final Identifier YELLOW_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_yellow.png");
    private static final Identifier BLUE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_blue.png");
    private static final Identifier PURPLE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_purple.png");
    private static final Identifier RED_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/reverie/reverie_red.png");

    @Override
    public void addAdditionalStateData(ReverieEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(MOOD, animatable.getMood());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "reverie");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return textureForMood(renderState.getOrDefaultGeckolibData(MOOD, ReverieEntity.Mood.WHITE));
    }

    @Override
    public Identifier getAnimationResource(ReverieEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "reverie");
    }

    public static Identifier textureForMood(ReverieEntity.Mood mood) {
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
