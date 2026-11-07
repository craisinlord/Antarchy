package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ToreterrorModel extends GeoModel<ToreterrorEntity> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_toreterror_glimmer_echo", Boolean.class);
    private static final Identifier NORMAL_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/toreterror.png");
    private static final Identifier GLIMMER_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/toreterror_glimmer.png");

    @Override
    public void addAdditionalStateData(ToreterrorEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GLIMMER_ECHO, animatable.isGlimmerEcho());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "toreterror");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false) ? GLIMMER_TEXTURE : NORMAL_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(ToreterrorEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "toreterror");
    }
}
