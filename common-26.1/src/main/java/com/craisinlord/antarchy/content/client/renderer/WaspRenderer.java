package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.WaspModel;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class WaspRenderer extends GeoEntityRenderer<WaspEntity, LivingEntityRenderState> {
    public WaspRenderer(EntityRendererProvider.Context context) {
        super(context, new WaspModel());
        this.shadowRadius = 0.55F;
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        renderPassInfo.poseStack().translate(0.0D, -0.5D, 0.0D);
    }
}
