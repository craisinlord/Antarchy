package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.PortalGunPortalRenderState;
import com.craisinlord.antarchy.content.portalgun.PortalGunWorldPortalShape;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherPortalMixin {
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void antarchy$clipPortalViewBlockEntities(E blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        if (PortalGunPortalRenderState.renderAll() || blockEntity == null || blockEntity.isRemoved()) {
            return;
        }
        PortalGunWorldPortalShape destinationShape = PortalGunPortalRenderState.getDestinationShape();
        PortalGunWorldPortalShape sourceShape = PortalGunPortalRenderState.getSourceShape();
        if (destinationShape == null) {
            return;
        }
        BlockPos pos = blockEntity.getBlockPos();
        AABB bounds = new AABB(pos);
        if (!destinationShape.intersectsFront(bounds, 0.1D)) {
            ci.cancel();
            return;
        }
        if (sourceShape != null && sourceShape.intersectsFront(bounds, -0.02D)) {
            ci.cancel();
        }
    }
}
