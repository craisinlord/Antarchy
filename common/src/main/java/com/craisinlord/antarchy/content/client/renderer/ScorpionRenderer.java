package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ScorpionModel;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ScorpionRenderer extends GeoEntityRenderer<ScorpionEntity> {

    public ScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new ScorpionModel());
        this.shadowRadius = 0.6F;
    }
}
