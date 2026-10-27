package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class ButterflyModel extends GeoModel<ButterflyEntity> {
    public static final DataTicket<String> TEXTURE_VARIANT = DataTicket.create("antarchy_butterfly_texture_variant", String.class);
    private static final String TEXTURE_PATH_PREFIX = "textures/entity/butterfly/butterfly_";
    private static final String DEFAULT_TEXTURE_VARIANT = "brown";

    @Override
    public void addAdditionalStateData(ButterflyEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(TEXTURE_VARIANT, animatable.getTextureVariantName());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "butterfly");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(
                Antarchy.MODID,
                TEXTURE_PATH_PREFIX + renderState.getOrDefaultGeckolibData(TEXTURE_VARIANT, DEFAULT_TEXTURE_VARIANT) + ".png"
        );
    }

    @Override
    public Identifier getAnimationResource(ButterflyEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "butterfly");
    }
}
