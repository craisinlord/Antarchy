package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class UndertrialSpawnerRenderer implements BlockEntityRenderer<UndertrialSpawnerBlockEntity, SpawnerRenderState> {
    private final EntityRenderDispatcher entityRenderer;

    public UndertrialSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public SpawnerRenderState createRenderState() {
        return new SpawnerRenderState();
    }

    @Override
    public void extractRenderState(UndertrialSpawnerBlockEntity blockEntity, SpawnerRenderState state, float partialTick,
                                   Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
        state.displayEntity = null;
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        TrialSpawner spawner = blockEntity.getTrialSpawner();
        Entity entity = spawner.getStateData().getOrCreateDisplayEntity(spawner, level, spawner.getState());
        if (entity != null) {
            state.displayEntity = this.entityRenderer.extractEntity(entity, partialTick);
            state.displayEntity.lightCoords = state.lightCoords;
            state.spin = (float) blockEntity.getClientSpin(partialTick) * 10.0F;
            state.scale = 0.53125F;
            float maxLength = Math.max(entity.getBbWidth(), entity.getBbHeight());
            if (maxLength > 1.0F) {
                state.scale /= maxLength;
            }
        }
    }

    @Override
    public void submit(SpawnerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.displayEntity != null) {
            SpawnerRenderer.submitEntityInSpawner(poseStack, collector, state.displayEntity, this.entityRenderer, state.spin, state.scale, camera);
        }
    }
}
