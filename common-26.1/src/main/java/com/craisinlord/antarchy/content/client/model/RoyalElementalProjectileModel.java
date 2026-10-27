package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalElementalProjectileEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class RoyalElementalProjectileModel extends GeoModel<RoyalElementalProjectileEntity> {
    public static final DataTicket<Boolean> ICEBALL =
            DataTicket.create("antarchy_royal_elemental_projectile_iceball", Boolean.class);
    public static final DataTicket<Boolean> DREAM_FIREBALL =
            DataTicket.create("antarchy_royal_elemental_projectile_dream_fireball", Boolean.class);

    @Override
    public void addAdditionalStateData(RoyalElementalProjectileEntity animatable, Object relatedObject,
                                       GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(ICEBALL, animatable.isIceball());
        renderState.addGeckolibData(DREAM_FIREBALL, animatable.isDreamFireball());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID,
                renderState.getOrDefaultGeckolibData(ICEBALL, false) ? "ice_ball" : "fire_ball");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState.getOrDefaultGeckolibData(DREAM_FIREBALL, false)) {
            return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/queen/dream_fire_ball.png");
        }
        return Identifier.fromNamespaceAndPath(Antarchy.MODID,
                "textures/entity/king/" + (renderState.getOrDefaultGeckolibData(ICEBALL, false) ? "ice_ball" : "fire_ball") + ".png");
    }

    @Override
    public Identifier getAnimationResource(RoyalElementalProjectileEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, animatable.isIceball() ? "ice_ball" : "fire_ball");
    }
}
