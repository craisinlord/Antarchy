package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.EmperorScorpionModel;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EmperorScorpionRenderer extends GeoEntityRenderer<EmperorScorpionEntity> {

    public EmperorScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new EmperorScorpionModel());
        this.shadowRadius = 3.0F;
        this.withScale(1.5F);
    }
}
