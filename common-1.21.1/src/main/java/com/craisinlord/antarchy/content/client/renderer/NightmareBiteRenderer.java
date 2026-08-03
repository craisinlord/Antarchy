package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.NightmareBiteModel;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareBiteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NightmareBiteRenderer extends GeoEntityRenderer<NightmareBiteEntity> {
    public NightmareBiteRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareBiteModel());
        this.shadowRadius = 0.0F;
    }
}
