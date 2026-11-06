package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class WaspModel extends GeoModel<WaspEntity> {
    public static final DataTicket<Boolean> PANDA_VARIANT = DataTicket.create("antarchy_wasp_panda_variant", Boolean.class);

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "wasp");
    private static final Identifier ANIMATION =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "wasp");
    private static final Identifier DEFAULT_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/wasp.png");
    private static final Identifier PANDA_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/wasp_panda.png");

    @Override
    public void addAdditionalStateData(WaspEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(PANDA_VARIANT, animatable.isPandaVariant());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(PANDA_VARIANT, false) ? PANDA_TEXTURE : DEFAULT_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(WaspEntity animatable) {
        return ANIMATION;
    }
}
