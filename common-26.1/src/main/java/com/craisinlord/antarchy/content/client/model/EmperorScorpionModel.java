package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class EmperorScorpionModel extends GeoModel<EmperorScorpionEntity> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_emperor_scorpion_glimmer_echo", Boolean.class);
    public static final Identifier NORMAL_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/emperor_scorpion/emperor_scorpion.png");
    public static final Identifier HARDEN_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/emperor_scorpion/emperor_scorpion_harden.png");

    public static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/emperor_scorpion/emperor_scorpion_emissive.png");
    public static final Identifier GLIMMER_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/emperor_scorpion_glimmer.png");
    public static final Identifier GLIMMER_HARDEN_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/emperor_scorpion_harden_glimmer.png");
    public static final Identifier GLIMMER_EMISSIVE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/emperor_scorpion_glimmer_emissive.png");

    @Override
    public void addAdditionalStateData(EmperorScorpionEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GLIMMER_ECHO, animatable.isGlimmerEcho());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "emperor_scorpion");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false) ? GLIMMER_TEXTURE : NORMAL_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(EmperorScorpionEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "emperor_scorpion");
    }
}
