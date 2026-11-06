package com.craisinlord.antarchy.mixins.gravity.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockRenderer.class)
public abstract class FallingBlockRendererBlockEntityMixin {
    @Unique
    private static final DataTicket<BlockEntityRenderState> ANTARCHY$BLOCK_ENTITY_STATE =
            DataTicket.create("antarchy_falling_block_entity_state", BlockEntityRenderState.class);

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/item/FallingBlockEntity;Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;F)V",
            at = @At("TAIL")
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void antarchy$extractAnimatedBlockEntity(FallingBlockEntity entity, FallingBlockRenderState renderState, float partialTick, CallbackInfo ci) {
        renderState.getDataMap().remove(ANTARCHY$BLOCK_ENTITY_STATE);
        BlockState state = entity.getBlockState();
        if (state.getRenderShape() != RenderShape.INVISIBLE || !(state.getBlock() instanceof EntityBlock entityBlock)) {
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
            blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, minecraft.level.registryAccess(), renderTag));
        }

        blockEntity.setLevel(minecraft.level);
        BlockEntityRenderDispatcher dispatcher = minecraft.getBlockEntityRenderDispatcher();
        BlockEntityRenderer renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null) {
            return;
        }
        BlockEntityRenderState blockEntityState = (BlockEntityRenderState) renderer.createRenderState();
        renderer.extractRenderState(blockEntity, blockEntityState, partialTick, minecraft.gameRenderer.mainCamera().position(), null);
        blockEntityState.lightCoords = renderState.lightCoords;
        renderState.addGeckolibData(ANTARCHY$BLOCK_ENTITY_STATE, blockEntityState);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$renderAnimatedBlockEntity(FallingBlockRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                                    CameraRenderState camera, CallbackInfo ci) {
        BlockEntityRenderState blockEntityState = renderState.getGeckolibData(ANTARCHY$BLOCK_ENTITY_STATE);
        if (blockEntityState == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        Minecraft.getInstance().getBlockEntityRenderDispatcher().submit(blockEntityState, poseStack, submitNodeCollector, camera);
        poseStack.popPose();
        ci.cancel();
    }
}
