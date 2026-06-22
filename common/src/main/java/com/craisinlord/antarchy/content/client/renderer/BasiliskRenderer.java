package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.BasiliskModel;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BasiliskRenderer extends GeoEntityRenderer<BasiliskEntity> {

    public BasiliskRenderer(EntityRendererProvider.Context context) {
        super(context, new BasiliskModel());
        this.shadowRadius = 1.0F;
    }
}
