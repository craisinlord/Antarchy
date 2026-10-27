package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class EmperorScorpionModel extends GeoModel<EmperorScorpionEntity> {
    public static final Identifier NORMAL_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/emperor_scorpion/emperor_scorpion.png");
    public static final Identifier HARDEN_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/emperor_scorpion/emperor_scorpion_harden.png");

    public static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/emperor_scorpion/emperor_scorpion_emissive.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "emperor_scorpion");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return NORMAL_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(EmperorScorpionEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "emperor_scorpion");
    }
}
