package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.LaunchedAnvilEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class LaunchedAnvilRenderer extends EntityRenderer<LaunchedAnvilEntity, LaunchedAnvilRenderer.LaunchedAnvilRenderState> {
    private static final float TUMBLE_DEGREES_PER_TICK = 18.0F;
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public LaunchedAnvilRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockModelResolver = context.getBlockModelResolver();
        this.shadowRadius = 0.5F;
    }

    @Override
    public LaunchedAnvilRenderState createRenderState() {
        return new LaunchedAnvilRenderState();
    }

    @Override
    public void extractRenderState(LaunchedAnvilEntity entity, LaunchedAnvilRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.falling = entity.isFalling();
        state.yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        this.blockModelResolver.update(state.blockModel, entity.getAnvilState(), BLOCK_DISPLAY_CONTEXT);
    }

    @Override
    public void submit(LaunchedAnvilRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        if (!state.falling) {
            poseStack.rotate(Axis.YP.rotationDegrees(-state.yRot));
            poseStack.rotate(Axis.XP.rotationDegrees(state.ageInTicks * TUMBLE_DEGREES_PER_TICK));
        }
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        state.blockModel.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static class LaunchedAnvilRenderState extends EntityRenderState {
        public final BlockModelRenderState blockModel = new BlockModelRenderState();
        public boolean falling;
        public float yRot;
    }
}
