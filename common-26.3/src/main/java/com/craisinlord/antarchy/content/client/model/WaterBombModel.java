package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class WaterBombModel extends GeoModel<WaterBombEntity> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_water_bomb_glimmer_echo", Boolean.class);
    private static final Identifier NORMAL_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/water_bomb.png");
    private static final Identifier GLIMMER_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/water_bomb_glimmer.png");

    @Override
    public void addAdditionalStateData(WaterBombEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GLIMMER_ECHO, animatable.isGlimmerEcho());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "water_bomb");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false) ? GLIMMER_TEXTURE : NORMAL_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(WaterBombEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "water_bomb");
    }
}
