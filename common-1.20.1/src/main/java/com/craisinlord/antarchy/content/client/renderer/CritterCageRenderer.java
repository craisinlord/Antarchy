package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.CritterCageBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CritterCageRenderer implements BlockEntityRenderer<CritterCageBlockEntity> {
    public CritterCageRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CritterCageBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
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

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.06D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(scale, scale, scale);

        if (displayEntity instanceof LivingEntity living) {
            living.yBodyRot = rotation;
            living.yBodyRotO = rotation;
            living.yHeadRot = rotation;
            living.yHeadRotO = rotation;
        }

        Minecraft.getInstance().getEntityRenderDispatcher().render(
                displayEntity,
                0.0D,
                0.0D,
                0.0D,
                rotation,
                partialTick,
                poseStack,
                buffer,
                packedLight
        );
        poseStack.popPose();
    }
}
