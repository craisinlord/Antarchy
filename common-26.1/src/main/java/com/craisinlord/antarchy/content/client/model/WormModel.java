package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.WormEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;

public class WormModel extends GeoModel<WormEntity> {
    public static final DataTicket<WormEntity.GrowthStage> GROWTH_STAGE = DataTicket.create("antarchy_worm_growth_stage", WormEntity.GrowthStage.class);

    private static final Identifier SMALL_MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "worm_small");
    private static final Identifier MEDIUM_MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "worm_medium");
    private static final Identifier LARGE_MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "worm_large");
    private static final Identifier SMALL_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/worm/worm_small.png");
    private static final Identifier MEDIUM_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/worm/worm_medium.png");
    private static final Identifier LARGE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/worm/worm_large.png");
    private static final Identifier SMALL_ANIMATION = Identifier.fromNamespaceAndPath(Antarchy.MODID, "worm_small");
    private static final Identifier MEDIUM_ANIMATION = Identifier.fromNamespaceAndPath(Antarchy.MODID, "worm_medium");
    private static final Identifier LARGE_ANIMATION = Identifier.fromNamespaceAndPath(Antarchy.MODID, "worm_large");

    @Override
    public void addAdditionalStateData(WormEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GROWTH_STAGE, animatable.getGrowthStage());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return switch (renderState.getOrDefaultGeckolibData(GROWTH_STAGE, WormEntity.GrowthStage.SMALL)) {
            case SMALL -> SMALL_MODEL;
            case MEDIUM -> MEDIUM_MODEL;
            case LARGE -> LARGE_MODEL;
        };
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return switch (renderState.getOrDefaultGeckolibData(GROWTH_STAGE, WormEntity.GrowthStage.SMALL)) {
            case SMALL -> SMALL_TEXTURE;
            case MEDIUM -> MEDIUM_TEXTURE;
            case LARGE -> LARGE_TEXTURE;
        };
    }

    @Override
    public Identifier getAnimationResource(WormEntity animatable) {
        return switch (animatable.getGrowthStage()) {
            case SMALL -> SMALL_ANIMATION;
            case MEDIUM -> MEDIUM_ANIMATION;
            case LARGE -> LARGE_ANIMATION;
        };
    }
}
