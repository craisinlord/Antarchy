package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class RoyalBossModel extends GeoModel<RoyalBossEntity> {
    public static final DataTicket<String> GEO_NAME = DataTicket.create("antarchy_royal_boss_geo_name", String.class);
    public static final String[] BEAM_ANCHORS = {
            "beam_anchor_left", "beam_anchor_center", "beam_anchor_right"
    };

    @Override
    public void addAdditionalStateData(RoyalBossEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GEO_NAME, animatable.geoNameForRender());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, renderState.getGeckolibData(GEO_NAME));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        String name = renderState.getGeckolibData(GEO_NAME);
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/" + name + "/" + name + ".png");
    }

    @Override
    public Identifier getAnimationResource(RoyalBossEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, animatable.geoNameForRender());
    }
}
