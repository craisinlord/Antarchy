package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class BedBugModel extends GeoModel<BedBugEntity> {
    public static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/bed_bug_emissive.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "bed_bug");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/bed_bug.png");
    }

    @Override
    public Identifier getAnimationResource(BedBugEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "bed_bug");
    }
}
