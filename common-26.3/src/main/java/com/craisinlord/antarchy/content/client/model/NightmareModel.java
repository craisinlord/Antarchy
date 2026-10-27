package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class NightmareModel extends GeoModel<NightmareEntity> {
    public static final DataTicket<Boolean> PHASE_TWO_TEXTURE_TICKET = DataTicket.create("antarchy_nightmare_phase_two_texture", Boolean.class);

    private static final Identifier PHASE_ONE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare.png");
    private static final Identifier PHASE_TWO_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare_phase_2.png");
    private static final Identifier PHASE_ONE_EMISSIVE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare_emissive.png");
    private static final Identifier PHASE_TWO_EMISSIVE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare_phase_2_emissive.png");

    @Override
    public void addAdditionalStateData(NightmareEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(PHASE_TWO_TEXTURE_TICKET, animatable.hasPhaseTwoTexture());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "nightmare");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return textureFor(renderState);
    }

    @Override
    public Identifier getAnimationResource(NightmareEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "nightmare");
    }

    public static Identifier textureFor(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(PHASE_TWO_TEXTURE_TICKET, false) ? PHASE_TWO_TEXTURE : PHASE_ONE_TEXTURE;
    }

    public static Identifier emissiveTextureFor(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(PHASE_TWO_TEXTURE_TICKET, false) ? PHASE_TWO_EMISSIVE : PHASE_ONE_EMISSIVE;
    }

    public static Identifier defaultEmissiveTexture() {
        return PHASE_ONE_EMISSIVE;
    }
}
