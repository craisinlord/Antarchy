package com.craisinlord.antarchy.content.client.model.glimmer;

import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class GlimmerModel extends GeoModel<GlimmerEntity> {
    public static final DataTicket<Identifier> MODEL = DataTicket.create("antarchy_glimmer_model", Identifier.class);
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("antarchy_glimmer_texture", Identifier.class);

    @Override
    public void addAdditionalStateData(GlimmerEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(MODEL, animatable.getVariant().getBehavior().modelGeo());
        renderState.addGeckolibData(TEXTURE, animatable.getVariant().getBehavior().texture(animatable));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getGeckolibData(MODEL);
    }

    @Override
    public Identifier getAnimationResource(GlimmerEntity animatable) {
        return animatable.getVariant().getBehavior().animationFile();
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getGeckolibData(TEXTURE);
    }
}
