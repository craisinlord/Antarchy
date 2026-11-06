package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;

public class UpwardFallingBlockRenderer extends EntityRenderer<UpwardFallingBlockEntity, UpwardFallingBlockRenderer.UpwardFallingBlockRenderState> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver blockModelResolver;

    public UpwardFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public UpwardFallingBlockRenderState createRenderState() {
        return new UpwardFallingBlockRenderState();
    }

    @Override
    public void extractRenderState(UpwardFallingBlockEntity entity, UpwardFallingBlockRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        BlockState blockState = entity.getBlockState();
        state.isAir = blockState.isAir();
        if (state.isAir) {
            state.blockModel.clear();
        } else {
            this.blockModelResolver.update(state.blockModel, blockState, BLOCK_DISPLAY_CONTEXT);
        }
    }

    @Override
    public void submit(UpwardFallingBlockRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.isAir) return;

        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);
        state.blockModel.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();

        super.submit(state, poseStack, collector, camera);
    }

    public static class UpwardFallingBlockRenderState extends EntityRenderState {
        public final BlockModelRenderState blockModel = new BlockModelRenderState();
        public boolean isAir = true;
    }
}
