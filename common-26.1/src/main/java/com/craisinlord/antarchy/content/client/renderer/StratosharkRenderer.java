package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.StratosharkModel;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;

public class StratosharkRenderer extends GeoEntityRenderer<StratosharkEntity, LivingEntityRenderState> {
    private static final float SHADOW_RADIUS = 0.6F;

    public StratosharkRenderer(EntityRendererProvider.Context context) {
        super(context, new StratosharkModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.withRenderLayer(new TextureLayerGeoLayer<>(this, StratosharkModel.EMISSIVE_TEXTURE, RenderTypes::eyes));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }
}
