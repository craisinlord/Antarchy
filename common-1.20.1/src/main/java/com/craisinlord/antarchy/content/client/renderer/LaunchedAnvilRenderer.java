package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.LaunchedAnvilEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LaunchedAnvilRenderer extends EntityRenderer<LaunchedAnvilEntity> {
    private static final float TUMBLE_DEGREES_PER_TICK = 18.0F;

    private final BlockRenderDispatcher blockRenderer;

    public LaunchedAnvilRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(LaunchedAnvilEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        if (!entity.isFalling()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
            poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTick) * TUMBLE_DEGREES_PER_TICK));
        }
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        this.blockRenderer.renderSingleBlock(entity.getAnvilState(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LaunchedAnvilEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
