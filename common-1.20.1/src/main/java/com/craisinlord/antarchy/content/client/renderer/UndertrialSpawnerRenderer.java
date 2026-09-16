package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** 1.20 visual equivalent for the 1.21 trial-spawner display entity. */
public final class UndertrialSpawnerRenderer implements BlockEntityRenderer<UndertrialSpawnerBlockEntity> {
    private final EntityRenderDispatcher dispatcher;

    public UndertrialSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getEntityRenderer();
    }

    @Override
    public void render(UndertrialSpawnerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;
        Entity display = EntityType.ZOMBIE.create(level);
        if (display == null) return;
        display.setPos(blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D);
        float spin = (level.getGameTime() + partialTick) * 2.0F;
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.5D, 0.0D);
        dispatcher.render(display, 0.0D, 0.0D, 0.0D, spin, partialTick,
                poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
