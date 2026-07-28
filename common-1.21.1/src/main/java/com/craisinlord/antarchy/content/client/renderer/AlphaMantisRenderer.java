package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.AlphaMantisModel;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AlphaMantisRenderer extends GeoEntityRenderer<AlphaMantisEntity> {
    public AlphaMantisRenderer(EntityRendererProvider.Context context) {
        super(context, new AlphaMantisModel());
        this.shadowRadius = 1.5F;
        // Mantis renders at 1.25x; the alpha is 1.5x bigger overall
        this.withScale(1.875F);
    }
}
