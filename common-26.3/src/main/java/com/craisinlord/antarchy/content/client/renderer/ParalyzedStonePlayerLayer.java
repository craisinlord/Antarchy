package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public final class ParalyzedStonePlayerLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public ParalyzedStonePlayerLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (!ParalyzedStoneLivingLayer.isParalyzed(state)) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.05f, 1.05f, 1.05f);
        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                ParalyzedStoneLivingLayer.STONE_RENDER_TYPE,
                lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                ParalyzedStoneLivingLayer.STONE_TINT,
                null,
                state.outlineColor
        );
        poseStack.popPose();
    }
}
