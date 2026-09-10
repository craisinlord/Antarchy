package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalElementalProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoyalElementalProjectileModel extends GeoModel<RoyalElementalProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(RoyalElementalProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID,
                "geo/" + (animatable.isIceball() ? "ice_ball" : "fire_ball") + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalElementalProjectileEntity animatable) {
        if (animatable.isDreamFireball()) {
            return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/queen/dream_fire_ball.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID,
                "textures/entity/king/" + (animatable.isIceball() ? "ice_ball" : "fire_ball") + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalElementalProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID,
                "animations/" + (animatable.isIceball() ? "ice_ball" : "fire_ball") + ".animation.json");
    }
}
