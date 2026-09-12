package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.FlytrapModel;
import com.craisinlord.antarchy.content.entity.FlytrapEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FlytrapRenderer extends GeoEntityRenderer<FlytrapEntity> {
    public FlytrapRenderer(EntityRendererProvider.Context context) {
        super(context, new FlytrapModel());
        this.shadowRadius = 1.4F;
    }

    @Override
    protected float getDeathMaxRotation(FlytrapEntity animatable) {
        return 0.0F;
    }
}
