package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class UpwardFallingBlockRenderer extends EntityRenderer<UpwardFallingBlockEntity> {

    public UpwardFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(UpwardFallingBlockEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        BlockState blockState = entity.getBlockState();
        if (blockState.isAir()) return;

        poseStack.pushPose();
        // Center the block model on the entity origin
        poseStack.translate(-0.5, 0.0, -0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockState, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, bufferSource, light);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResourceLocation getTextureLocation(UpwardFallingBlockEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
