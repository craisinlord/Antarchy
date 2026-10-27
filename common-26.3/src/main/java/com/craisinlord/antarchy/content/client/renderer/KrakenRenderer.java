package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.KrakenModel;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;

public class KrakenRenderer extends GeoEntityRenderer<KrakenEntity, LivingEntityRenderState> {
    public KrakenRenderer(EntityRendererProvider.Context context) {
        super(context, new KrakenModel());
        this.shadowRadius = 2.0F;
        this.withScale(1.5F);
        this.withRenderLayer(new TextureLayerGeoLayer<>(this, KrakenModel.EMISSIVE_TEXTURE, RenderTypes::eyes));
    }
}
