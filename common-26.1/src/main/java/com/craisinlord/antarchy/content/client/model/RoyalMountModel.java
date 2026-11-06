package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class RoyalMountModel extends GeoModel<RoyalMountEntity> {
    public static final DataTicket<String> GEO_NAME = DataTicket.create("antarchy_royal_mount_geo_name", String.class);
    public static final DataTicket<Boolean> RIDDEN = DataTicket.create("antarchy_royal_mount_ridden", Boolean.class);
    public static final String[] HEADS = {"head_left", "head", "head_right"};
    private static final String[] RIDDEN_NECK_CHAIN = {
            "neck", "neck2", "neck3", "head",
            "neck_left", "neck_left2", "neck_left3", "head_left",
            "neck_right", "neck_right2", "neck_right3", "head_right"
    };
    private static final float RIDDEN_NECK_RAISE = 0.35F;

    @Override
    public void addAdditionalStateData(RoyalMountEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(GEO_NAME, animatable.geoNameForRender());
        renderState.addGeckolibData(RIDDEN, animatable.isVehicle());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, renderState.getGeckolibData(GEO_NAME));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID,
                "textures/entity/" + renderState.getGeckolibData(GEO_NAME) + ".png");
    }

    @Override
    public Identifier getAnimationResource(RoyalMountEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, animatable.geoNameForRender());
    }

    public static void applyRiddenNeckPose(GeoRenderState renderState, BoneSnapshots snapshots) {
        if (!renderState.getOrDefaultGeckolibData(RIDDEN, false)) {
            return;
        }
        for (String name : RIDDEN_NECK_CHAIN) {
            snapshots.ifPresent(name, bone -> bone.setRotX(bone.getRotX() * RIDDEN_NECK_RAISE));
        }
    }
}
