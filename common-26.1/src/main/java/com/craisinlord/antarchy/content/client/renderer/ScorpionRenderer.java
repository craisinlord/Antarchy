package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ScorpionModel;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class ScorpionRenderer extends GeoEntityRenderer<ScorpionEntity, LivingEntityRenderState> {

    public ScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new ScorpionModel());
        this.shadowRadius = 0.6F;
        this.withRenderLayer(new ScorpionEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    private static final class ScorpionEmissiveLayer extends GeoRenderLayer<ScorpionEntity, Void, LivingEntityRenderState> {
        private ScorpionEmissiveLayer(GeoRenderer<ScorpionEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }

            RenderType emissiveType = RenderTypes.eyes(ScorpionModel.emissiveTextureFor(renderPassInfo.renderState()));
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, emissiveType, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
                poseStack.popPose();
            });
        }
    }
}
