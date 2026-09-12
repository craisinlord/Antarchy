package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class UndertrialSpawnerRenderer implements BlockEntityRenderer<UndertrialSpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public UndertrialSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(UndertrialSpawnerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        var spawner = blockEntity.getTrialSpawner();
        var data = spawner.getData();
        Entity entity = data.getOrCreateDisplayEntity(spawner, level, spawner.getState());
        if (entity != null) {
            SpawnerRenderer.renderEntityInSpawner(partialTick, poseStack, buffer, packedLight, entity,
                    entityRenderer, blockEntity.getClientSpin(partialTick), blockEntity.getClientSpin(partialTick));
        }
    }
}
