package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LucidBoltModel extends GeoModel<LucidBoltEntity> {
    @Override
    public ResourceLocation getModelResource(LucidBoltEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/lucid_bolt.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LucidBoltEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "textures/vfx/lucid_bolt.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LucidBoltEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/lucid_bolt.animation.json");
    }
}
