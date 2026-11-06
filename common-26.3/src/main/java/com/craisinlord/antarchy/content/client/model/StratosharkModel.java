package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class StratosharkModel extends GeoModel<StratosharkEntity> {
    public static final Identifier BASE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/stratoshark/stratoshark.png");
    public static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/stratoshark/stratoshark_emissive.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "stratoshark");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BASE_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(StratosharkEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "stratoshark");
    }
}
