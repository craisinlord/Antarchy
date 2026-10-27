package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.JerryModel;
import com.craisinlord.antarchy.content.entity.JerryEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class JerryRenderer extends GeoEntityRenderer<JerryEntity, LivingEntityRenderState> {
    public JerryRenderer(EntityRendererProvider.Context context) {
        super(context, new JerryModel());
        this.shadowRadius = 0.7F;
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }
}
