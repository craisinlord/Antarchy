package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class StinkBugModel extends GeoModel<StinkBugEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "stink_bug");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/stink_bug.png");
    }

    @Override
    public Identifier getAnimationResource(StinkBugEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "stink_bug");
    }
}
