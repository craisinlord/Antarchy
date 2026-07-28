package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.StinkBugModel;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StinkBugRenderer extends GeoEntityRenderer<StinkBugEntity> {
    private static final float ADULT_SCALE = 0.95F;
    private static final float ADULT_SHADOW_RADIUS = 0.2F;

    public StinkBugRenderer(EntityRendererProvider.Context context) {
        super(context, new StinkBugModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        this.withScale(ADULT_SCALE);
    }
}
