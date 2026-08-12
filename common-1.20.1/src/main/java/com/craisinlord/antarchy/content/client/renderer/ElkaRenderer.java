package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ElkaModel;
import com.craisinlord.antarchy.content.entity.ElkaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ElkaRenderer extends GeoEntityRenderer<ElkaEntity> {
    private static final float SHADOW_RADIUS = 0.9F;

    public ElkaRenderer(EntityRendererProvider.Context context) {
        super(context, new ElkaModel());
        this.shadowRadius = SHADOW_RADIUS;
    }
}
