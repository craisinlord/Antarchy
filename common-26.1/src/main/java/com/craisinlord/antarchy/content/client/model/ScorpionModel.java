package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ScorpionModel extends GeoModel<ScorpionEntity> {
    public static final DataTicket<Integer> TEXTURE_VARIANT = DataTicket.create("antarchy_scorpion_texture_variant", Integer.class);

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "scorpion");
    private static final Identifier BLUE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/scorpion/scorpion_blue.png");
    private static final Identifier GREEN_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/scorpion/scorpion_green.png");
    private static final Identifier BLUE_EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/scorpion/scorpion_blue_emissive.png");
    private static final Identifier GREEN_EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/scorpion/scorpion_green_emissive.png");
    private static final Identifier ANIMATION =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "scorpion");

    @Override
    public void addAdditionalStateData(ScorpionEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(TEXTURE_VARIANT, animatable.getTextureVariant());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return textureFor(renderState);
    }

    @Override
    public Identifier getAnimationResource(ScorpionEntity animatable) {
        return ANIMATION;
    }

    public static Identifier textureFor(GeoRenderState renderState) {
        return isGreen(renderState) ? GREEN_TEXTURE : BLUE_TEXTURE;
    }

    public static Identifier emissiveTextureFor(GeoRenderState renderState) {
        return isGreen(renderState) ? GREEN_EMISSIVE_TEXTURE : BLUE_EMISSIVE_TEXTURE;
    }

    private static boolean isGreen(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE_VARIANT, 0) == ScorpionEntity.GREEN_VARIANT;
    }
}
