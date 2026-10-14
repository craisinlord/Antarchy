package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.SkulkingFrightModel;
import com.craisinlord.antarchy.content.entity.SkulkingFrightEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SkulkingFrightRenderer extends GeoEntityRenderer<SkulkingFrightEntity> {
    public SkulkingFrightRenderer(EntityRendererProvider.Context context) {
        super(context, new SkulkingFrightModel());
        this.shadowRadius = 0.5F;
        this.withScale(0.85F);
    }
}
