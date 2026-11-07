package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ManticoreModel extends GeoModel<ManticoreEntity> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_manticore_glimmer_echo", Boolean.class);
    public static final Identifier GLIMMER_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/manticore_glimmer.png");
    public static final Identifier GLIMMER_EMISSIVE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/manticore_glimmer_emissive.png");

    @Override
    public void addAdditionalStateData(ManticoreEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GLIMMER_ECHO, animatable.isGlimmerEcho());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "manticore");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false)) {
            return GLIMMER_TEXTURE;
        }
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/manticore.png");
    }

    @Override
    public Identifier getAnimationResource(ManticoreEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "manticore");
    }
}
