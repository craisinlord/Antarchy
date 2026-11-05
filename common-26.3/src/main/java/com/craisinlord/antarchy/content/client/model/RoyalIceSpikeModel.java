package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class RoyalIceSpikeModel extends GeoModel<RoyalIceSpikeEntity> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_royal_ice_spike_glimmer_echo", Boolean.class);

    @Override
    public void addAdditionalStateData(RoyalIceSpikeEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GLIMMER_ECHO, animatable.isGlimmerEcho());
    }
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "ice_spike");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false)) {
            return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/king_ice_spike_glimmer.png");
        }
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/king/ice_spike.png");
    }

    @Override
    public Identifier getAnimationResource(RoyalIceSpikeEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "ice_spike");
    }
}
