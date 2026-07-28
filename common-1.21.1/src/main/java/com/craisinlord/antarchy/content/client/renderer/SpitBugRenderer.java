package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.SpitBugModel;
import com.craisinlord.antarchy.content.entity.SpitBugEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SpitBugRenderer extends GeoEntityRenderer<SpitBugEntity> {
    public SpitBugRenderer(EntityRendererProvider.Context context) {
        super(context, new SpitBugModel());
        this.shadowRadius = 1.6F;
        this.withScale(0.85F);
    }
}
