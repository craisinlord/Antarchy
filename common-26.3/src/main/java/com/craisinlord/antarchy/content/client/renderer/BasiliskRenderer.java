package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.BasiliskModel;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;

public class BasiliskRenderer extends GeoEntityRenderer<BasiliskEntity, LivingEntityRenderState> {
    private static final DataTicket<Boolean> DEAD_OR_DYING = DataTicket.create("antarchy_basilisk_dead_or_dying", Boolean.class);

    public BasiliskRenderer(EntityRendererProvider.Context context) {
        super(context, new BasiliskModel());
        this.shadowRadius = 1.0F;
        this.withRenderLayer(new TextureLayerGeoLayer<>(this, BasiliskModel.EMISSIVE_TEXTURE, RenderTypes::eyes));
    }

    @Override
    public void addRenderData(BasiliskEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(DEAD_OR_DYING, animatable.isDeadOrDying());
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);

        if (renderPassInfo.getOrDefaultGeckolibData(DEAD_OR_DYING, false)) {
            renderPassInfo.poseStack().translate(0.0D, -0.35D, 0.0D);
        }
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }
}
