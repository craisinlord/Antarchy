package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.TentacleModel;
import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TentacleRenderer extends GeoEntityRenderer<TentacleEntity> {
    public TentacleRenderer(EntityRendererProvider.Context context) {
        super(context, new TentacleModel());
        this.shadowRadius = 0.0F;
    }
}
