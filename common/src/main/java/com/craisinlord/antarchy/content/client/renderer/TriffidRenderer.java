package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.TriffidModel;
import com.craisinlord.antarchy.content.entity.TriffidEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TriffidRenderer extends GeoEntityRenderer<TriffidEntity> {
    public TriffidRenderer(EntityRendererProvider.Context context) {
        super(context, new TriffidModel());
        this.shadowRadius = 1.4F;
    }

    @Override
    protected float getDeathMaxRotation(TriffidEntity animatable) {
        return 0.0F;
    }
}
