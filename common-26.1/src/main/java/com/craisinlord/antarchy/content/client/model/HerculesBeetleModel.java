package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class HerculesBeetleModel extends GeoModel<HerculesBeetleEntity> {
    public static final DataTicket<Boolean> SADDLED = DataTicket.create("antarchy_hercules_beetle_saddled", Boolean.class);

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hercules_beetle/hercules_beetle.png");
    private static final Identifier TEXTURE_SADDLED =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hercules_beetle/hercules_beetle_saddle.png");
    public static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hercules_beetle/hercules_beetle_emissive.png");

    @Override
    public void addAdditionalStateData(HerculesBeetleEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(SADDLED, animatable.hasSaddle());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "hercules_beetle");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(SADDLED, false) ? TEXTURE_SADDLED : TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(HerculesBeetleEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "hercules_beetle");
    }
}
