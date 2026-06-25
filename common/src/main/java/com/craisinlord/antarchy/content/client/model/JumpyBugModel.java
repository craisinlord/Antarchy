package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class JumpyBugModel extends GeoModel<JumpyBugEntity> {
    @Override
    public ResourceLocation getModelResource(JumpyBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/jumpy_bug.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JumpyBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/jumpy_bug.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JumpyBugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/jumpy_bug.animation.json");
    }
}
