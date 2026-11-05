package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalBlackHoleEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class RoyalBlackHoleModel<T extends RoyalBlackHoleEntity> extends GeoModel<T> {
    public static final DataTicket<Boolean> GLIMMER_ECHO = DataTicket.create("antarchy_royal_black_hole_glimmer_echo", Boolean.class);
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "black_hole");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState.getOrDefaultGeckolibData(GLIMMER_ECHO, false)) {
            return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/queen_black_hole_glimmer.png");
        }
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/queen/black_hole.png");
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "black_hole");
    }
}
