package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.CloudSharkModel;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CloudSharkRenderer extends GeoEntityRenderer<CloudSharkEntity> {
    private static final float SHADOW_RADIUS = 0.6F;

    public CloudSharkRenderer(EntityRendererProvider.Context context) {
        super(context, new CloudSharkModel());
        this.shadowRadius = SHADOW_RADIUS;
    }
}
