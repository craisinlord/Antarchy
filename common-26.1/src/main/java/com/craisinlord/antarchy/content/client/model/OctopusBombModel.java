package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class OctopusBombModel extends GeoModel<OctopusBombEntity> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_octopus_bomb_glimmer_echo", Boolean.class);
    public static final Identifier GLIMMER_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/octopus_bomb_glimmer.png");
    public static final Identifier GLIMMER_EMISSIVE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/octopus_bomb_glimmer_emissive.png");
    public static final Identifier EMISSIVE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/octopus_bomb_emissive.png");

    @Override
    public void addAdditionalStateData(OctopusBombEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GLIMMER_ECHO, animatable.isGlimmerEcho());
    }
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "octopus_bomb");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false) ? GLIMMER_TEXTURE : Identifier.fromNamespaceAndPath("antarchy", "textures/entity/octopus_bomb.png");
    }

    @Override
    public Identifier getAnimationResource(OctopusBombEntity animatable) {
        return Identifier.fromNamespaceAndPath("antarchy", "octopus_bomb");
    }
}
