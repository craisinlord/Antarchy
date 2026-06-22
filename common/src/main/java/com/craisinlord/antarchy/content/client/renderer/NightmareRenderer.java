package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.NightmareModel;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NightmareRenderer extends GeoEntityRenderer<NightmareEntity> {
    public NightmareRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareModel());
        this.shadowRadius = 1.8F;
        this.withScale(1.15F);
    }

    @Override
    protected float getDeathMaxRotation(NightmareEntity animatable) {
        return 0.0F;
    }
}
