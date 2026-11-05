package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.Antarchy;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class MissileSquidModel extends GeoModel<MissileSquidEntity> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_missile_squid_glimmer_echo", Boolean.class);
    public static final Identifier GLIMMER_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/missile_squid_glimmer.png");
    public static final Identifier GLIMMER_EMISSIVE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/missile_squid_glimmer_emissive.png");
    public static final Identifier EMISSIVE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/missile_squid_emissive.png");

    @Override
    public void addAdditionalStateData(MissileSquidEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GLIMMER_ECHO, animatable.isGlimmerEcho());
    }
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "missile_squid");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false) ? GLIMMER_TEXTURE : Identifier.fromNamespaceAndPath("antarchy", "textures/entity/missile_squid.png");
    }

    @Override
    public Identifier getAnimationResource(MissileSquidEntity animatable) {
        return Identifier.fromNamespaceAndPath("antarchy", "missile_squid");
    }
}
