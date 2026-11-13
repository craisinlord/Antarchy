package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.FlytrapEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class FlytrapModel extends GeoModel<FlytrapEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "flytrap");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/flytrap.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(Antarchy.MODID, "flytrap");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(FlytrapEntity animatable) {
        return ANIMATION;
    }
}
