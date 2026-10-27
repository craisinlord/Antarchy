package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class DorrieModel extends GeoModel<DorrieEntity> {
    public static final DataTicket<Boolean> SADDLED = DataTicket.create("antarchy_dorrie_saddled", Boolean.class);

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dorrie.png");
    private static final Identifier TEXTURE_SADDLED =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dorrie_saddle.png");

    @Override
    public void addAdditionalStateData(DorrieEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(SADDLED, animatable.hasSaddle());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "dorrie");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(SADDLED, false) ? TEXTURE_SADDLED : TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(DorrieEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "dorrie");
    }
}
