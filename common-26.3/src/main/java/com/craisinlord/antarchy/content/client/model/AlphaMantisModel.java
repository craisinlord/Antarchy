package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class AlphaMantisModel extends GeoModel<AlphaMantisEntity> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_alpha_mantis_glimmer_echo", Boolean.class);
    private static final Identifier NORMAL_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/mantis_alpha.png");
    private static final Identifier GLIMMER_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/alpha_mantis_glimmer.png");

    @Override
    public void addAdditionalStateData(AlphaMantisEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GLIMMER_ECHO, animatable.isGlimmerEcho());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "mantis");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false) ? GLIMMER_TEXTURE : NORMAL_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(AlphaMantisEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "mantis");
    }
}
