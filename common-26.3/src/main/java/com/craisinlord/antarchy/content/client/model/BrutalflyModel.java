package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class BrutalflyModel extends GeoModel<BrutalflyEntity> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_brutalfly_glimmer_echo", Boolean.class);
    public static final Identifier GLIMMER_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/brutalfly_glimmer.png");
    public static final Identifier GLIMMER_EMISSIVE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/brutalfly_glimmer_emissive.png");
    public static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/brutalfly_emissive.png");

    @Override
    public void addAdditionalStateData(BrutalflyEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GLIMMER_ECHO, animatable.isGlimmerEcho());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "brutalfly");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false)
                ? GLIMMER_TEXTURE
                : Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/brutalfly.png");
    }

    @Override
    public Identifier getAnimationResource(BrutalflyEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "brutalfly");
    }
}
