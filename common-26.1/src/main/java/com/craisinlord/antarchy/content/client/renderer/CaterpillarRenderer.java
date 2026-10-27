package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.CaterpillarModel;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class CaterpillarRenderer extends GeoEntityRenderer<CaterpillarEntity, LivingEntityRenderState> {
    private static final float VISUAL_SCALE = 0.8F;
    private static final float SHADOW_RADIUS = 0.25F;

    public CaterpillarRenderer(EntityRendererProvider.Context context) {
        super(context, new CaterpillarModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.withScale(VISUAL_SCALE);
    }
}
