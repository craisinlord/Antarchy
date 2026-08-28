package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.vortex.VortexEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VortexModel extends GeoModel<VortexEntity> {
    @Override
    public ResourceLocation getModelResource(VortexEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/vortex.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VortexEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/vortex.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VortexEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/vortex.animation.json");
    }
}
