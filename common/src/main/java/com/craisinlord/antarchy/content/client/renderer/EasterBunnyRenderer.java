package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.EasterBunnyModel;
import com.craisinlord.antarchy.content.entity.EasterBunnyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EasterBunnyRenderer extends GeoEntityRenderer<EasterBunnyEntity> {
    private static final float SHADOW_RADIUS = 0.3F;

    public EasterBunnyRenderer(EntityRendererProvider.Context context) {
        super(context, new EasterBunnyModel());
        this.shadowRadius = SHADOW_RADIUS;
    }
}
