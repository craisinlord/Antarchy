package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.NightmareModel;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class NightmareRenderer extends GeoEntityRenderer<NightmareEntity, LivingEntityRenderState> {
    public NightmareRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareModel());
        this.shadowRadius = 1.8F;
        this.withScale(1.15F);
        this.withRenderLayer(new NightmareEmissiveLayer(this));
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }

    private static final class NightmareEmissiveLayer extends TextureLayerGeoLayer<NightmareEntity, Void, LivingEntityRenderState> {
        private NightmareEmissiveLayer(GeoEntityRenderer<NightmareEntity, LivingEntityRenderState> renderer) {
            super(renderer, NightmareModel.defaultEmissiveTexture(), RenderTypes::eyes);
        }

        @Override
        protected Identifier getTextureResource(LivingEntityRenderState renderState) {
            return NightmareModel.emissiveTextureFor(renderState);
        }
    }
}
