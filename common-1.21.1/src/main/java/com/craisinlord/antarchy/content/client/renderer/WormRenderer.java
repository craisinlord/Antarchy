package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.WormModel;
import com.craisinlord.antarchy.content.entity.WormEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WormRenderer extends GeoEntityRenderer<WormEntity> {
    public WormRenderer(EntityRendererProvider.Context context) {
        super(context, new WormModel());
        this.shadowRadius = 0.75F;
    }
}
