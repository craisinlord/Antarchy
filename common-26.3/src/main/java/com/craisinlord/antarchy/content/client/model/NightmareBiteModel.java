package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareBiteEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class NightmareBiteModel extends GeoModel<NightmareBiteEntity> {
    public static final DataTicket<Boolean> PHASE_TWO = DataTicket.create("antarchy_nightmare_bite_phase_two", Boolean.class);

    private static final Identifier PHASE_ONE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/nightmare_bite.png");
    private static final Identifier PHASE_TWO_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/nightmare_bite_phase_2.png");

    @Override
    public void addAdditionalStateData(NightmareBiteEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(PHASE_TWO, animatable.isPhaseTwo());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "nightmare_bite");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(PHASE_TWO, false) ? PHASE_TWO_TEXTURE : PHASE_ONE_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(NightmareBiteEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "nightmare_bite");
    }
}
