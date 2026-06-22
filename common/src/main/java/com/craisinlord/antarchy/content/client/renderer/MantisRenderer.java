package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.MantisModel;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MantisRenderer extends GeoEntityRenderer<MantisEntity> {
    public MantisRenderer(EntityRendererProvider.Context context) {
        super(context, new MantisModel());
        this.shadowRadius = 1.0F;
        this.withScale(1.25F);
    }
}
