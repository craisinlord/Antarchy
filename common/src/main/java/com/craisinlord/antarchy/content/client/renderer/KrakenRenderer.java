package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.KrakenModel;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KrakenRenderer extends GeoEntityRenderer<KrakenEntity> {
    public KrakenRenderer(EntityRendererProvider.Context context) {
        super(context, new KrakenModel());
        this.shadowRadius = 3.6F;
        this.withScale(3.0F);
    }
}
