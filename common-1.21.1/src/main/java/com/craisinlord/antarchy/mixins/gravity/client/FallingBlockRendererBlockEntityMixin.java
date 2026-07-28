package com.craisinlord.antarchy.mixins.gravity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockRenderer.class)
/*
 * Keeps falling block render data lined up with carried state.
 */
public abstract class FallingBlockRendererBlockEntityMixin {
    @Inject(
            method = "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$renderAnimatedBlockEntity(
            FallingBlockEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        BlockState state = entity.getBlockState();
        if (state.getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED || !(state.getBlock() instanceof EntityBlock entityBlock)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        BlockEntity blockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, state);
        if (blockEntity == null) {
            return;
        }

        CompoundTag blockData = entity.blockData;
        if (blockData != null && !blockData.isEmpty()) {
            CompoundTag renderTag = blockData.copy();
            renderTag.remove("x");
            renderTag.remove("y");
            renderTag.remove("z");
            blockEntity.loadWithComponents(renderTag, minecraft.level.registryAccess());
        }

        blockEntity.setLevel(minecraft.level);
        BlockEntityRenderDispatcher dispatcher = minecraft.getBlockEntityRenderDispatcher();
        poseStack.pushPose();
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        dispatcher.renderItem(blockEntity, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        ci.cancel();
    }
}
