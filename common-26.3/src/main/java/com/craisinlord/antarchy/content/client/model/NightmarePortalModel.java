package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.nightmare.NightmarePortalEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class NightmarePortalModel extends GeoModel<NightmarePortalEntity> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/nightmare_portal.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "nightmare_portal");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(NightmarePortalEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "nightmare_portal");
    }
}
