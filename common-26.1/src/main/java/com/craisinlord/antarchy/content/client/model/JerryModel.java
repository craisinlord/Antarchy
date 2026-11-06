package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.JerryEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class JerryModel extends GeoModel<JerryEntity> {
    public static final DataTicket<String> STAGE = DataTicket.create("antarchy_jerry_stage", String.class);

    @Override
    public void addAdditionalStateData(JerryEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(STAGE, stageName(animatable));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, stageName(renderState) + "_jerry");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/jerry/" + stageName(renderState) + "_jerry.png");
    }

    @Override
    public Identifier getAnimationResource(JerryEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, stageName(animatable) + "_jerry");
    }

    private static String stageName(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(STAGE, JerryEntity.Stage.INFANT.getSerializedName());
    }

    private static String stageName(JerryEntity animatable) {
        return animatable.getStage().getSerializedName();
    }
}
