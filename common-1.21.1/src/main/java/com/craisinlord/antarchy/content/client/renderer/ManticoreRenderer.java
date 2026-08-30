package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ManticoreModel;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ManticoreRenderer extends GeoEntityRenderer<ManticoreEntity> {
    public ManticoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ManticoreModel());
        this.shadowRadius = 0.9F;
    }
}
