package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.DorrieModel;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DorrieRenderer extends GeoEntityRenderer<DorrieEntity> {
    public DorrieRenderer(EntityRendererProvider.Context context) {
        super(context, new DorrieModel());
    }
}
