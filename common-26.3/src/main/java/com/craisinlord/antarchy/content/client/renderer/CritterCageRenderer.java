package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.CritterCageBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CritterCageRenderer implements BlockEntityRenderer<CritterCageBlockEntity, CritterCageRenderer.CritterCageRenderState> {
    private final EntityRenderDispatcher entityRenderer;

    public CritterCageRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public CritterCageRenderState createRenderState() {
        return new CritterCageRenderState();
    }

    @Override
    public void extractRenderState(CritterCageBlockEntity blockEntity, CritterCageRenderState state, float partialTick,
                                   Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
        state.displayEntity = null;
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        Entity displayEntity = blockEntity.getOrCreateClientDisplayEntity(level);
        if (displayEntity == null) {
            return;
        }

        displayEntity.tickCount = (int) (level.getGameTime() & Integer.MAX_VALUE);
        float scale = 0.36F / Math.max(Math.max(displayEntity.getBbWidth(), displayEntity.getBbHeight()), 0.5F);
        float rotation = ((level.getGameTime() + partialTick) * 4.0F) % 360.0F;

        if (displayEntity instanceof LivingEntity living) {
            living.yBodyRot = rotation;
            living.yBodyRotO = rotation;
            living.yHeadRot = rotation;
            living.yHeadRotO = rotation;
        } else {
            displayEntity.setYRot(rotation);
            displayEntity.yRotO = rotation;
        }

        state.displayEntity = this.entityRenderer.extractEntity(displayEntity, partialTick);
        state.displayEntity.lightCoords = state.lightCoords;
        state.scale = scale;
        state.rotation = rotation;
    }

    @Override
    public void submit(CritterCageRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.displayEntity == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.06D, 0.5D);
        poseStack.rotate(Axis.YP.rotationDegrees(state.rotation));
        poseStack.scale(state.scale, state.scale, state.scale);
        this.entityRenderer.submit(state.displayEntity, camera, 0.0D, 0.0D, 0.0D, poseStack, collector);
        poseStack.popPose();
    }

    public static class CritterCageRenderState extends BlockEntityRenderState {
        public @Nullable EntityRenderState displayEntity;
        public float scale;
        public float rotation;
    }
}
