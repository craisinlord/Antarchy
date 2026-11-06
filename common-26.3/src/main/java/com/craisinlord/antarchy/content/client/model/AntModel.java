package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.craisinlord.antarchy.content.entity.ant.BrownAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RainbowAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RedAntEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class AntModel extends GeoModel<BaseAntEntity> {
    public static final DataTicket<String> VARIANT = DataTicket.create("antarchy_ant_variant", String.class);
    private static final String VARIANT_BROWN = "brown";
    private static final String VARIANT_RED = "red";
    private static final String VARIANT_RAINBOW = "rainbow";
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "ant");
    private static final Identifier ANIMATIONS = Identifier.fromNamespaceAndPath(Antarchy.MODID, "ant");
    private static final Identifier BROWN_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/brown_ant.png");
    private static final Identifier RED_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/red_ant.png");
    private static final Identifier RAINBOW_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/rainbow_ant.png");

    @Override
    public void addAdditionalStateData(BaseAntEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(VARIANT, variantOf(animatable));
    }

    private static String variantOf(BaseAntEntity animatable) {
        if (animatable instanceof RedAntEntity) {
            return VARIANT_RED;
        }
        if (animatable instanceof RainbowAntEntity) {
            return VARIANT_RAINBOW;
        }
        if (animatable instanceof BrownAntEntity) {
            return VARIANT_BROWN;
        }
        return VARIANT_BROWN;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return switch (renderState.getOrDefaultGeckolibData(VARIANT, VARIANT_BROWN)) {
            case VARIANT_RED -> RED_TEXTURE;
            case VARIANT_RAINBOW -> RAINBOW_TEXTURE;
            default -> BROWN_TEXTURE;
        };
    }

    @Override
    public Identifier getAnimationResource(BaseAntEntity animatable) {
        return ANIMATIONS;
    }
}
