package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class FlyingSquirrelModel extends GeoModel<FlyingSquirrelEntity> {
    public static final DataTicket<String> TEXTURE_VARIANT = DataTicket.create("antarchy_flying_squirrel_texture_variant", String.class);
    private static final Identifier REGULAR_TEXTURE = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/flying_squirrel/flying_squirrel.png");
    private static final Identifier BROWN_TEXTURE = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/flying_squirrel/flying_squirrel_brown.png");
    private static final Identifier ALBINO_TEXTURE = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/flying_squirrel/flying_squirrel_albino.png");

    @Override
    public void addAdditionalStateData(FlyingSquirrelEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(TEXTURE_VARIANT, animatable.getTextureVariantName());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "flying_squirrel");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return switch (renderState.getOrDefaultGeckolibData(TEXTURE_VARIANT, "")) {
            case "brown" -> BROWN_TEXTURE;
            case "albino" -> ALBINO_TEXTURE;
            default -> REGULAR_TEXTURE;
        };
    }

    @Override
    public Identifier getAnimationResource(FlyingSquirrelEntity animatable) {
        return Identifier.fromNamespaceAndPath("antarchy", "flying_squirrel");
    }
}
