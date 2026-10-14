package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnerBlockEntity;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSpawner;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSpawnerData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
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
        UndertrialSpawner spawner = blockEntity.getTrialSpawner();
        UndertrialSpawnerData data = spawner.getData();
        Entity entity = data.getOrCreateDisplayEntity(spawner, level, spawner.getState());
        if (entity != null) {
            renderEntityInSpawner(partialTick, poseStack, buffer, packedLight, entity,
                    entityRenderer, blockEntity.getClientSpin(partialTick), blockEntity.getClientSpin(partialTick));
        }
    }

    public static void renderEntityInSpawner(float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                             Entity entity, EntityRenderDispatcher dispatcher, double oldSpin, double spin) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        float scale = 0.53125F;
        float size = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (size > 1.0D) {
            scale /= size;
        }
        poseStack.translate(0.0F, 0.4F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Mth.lerp(partialTick, oldSpin, spin) * 10.0F));
        poseStack.translate(0.0F, -0.2F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.scale(scale, scale, scale);
        dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
