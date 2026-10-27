package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.SpitBugEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class SpitBugModel extends GeoModel<SpitBugEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "spit_bug");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/spit_bug.png");
    }

    @Override
    public Identifier getAnimationResource(SpitBugEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "spit_bug");
    }
}
