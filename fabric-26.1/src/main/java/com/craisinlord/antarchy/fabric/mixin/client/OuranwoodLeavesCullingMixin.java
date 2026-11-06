package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.block.LeafFaceCulling;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBlockRenderer.class)
public abstract class OuranwoodLeavesCullingMixin {

    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private void antarchy$cullBuriedLeafFaces(BlockAndTintGetter level, BlockState state, Direction face, BlockPos neighborPos, CallbackInfoReturnable<Boolean> cir) {
        BlockState neighbor = level.getBlockState(neighborPos);
        if (LeafFaceCulling.appliesTo(neighbor) && LeafFaceCulling.hidesNeighborFace(level, neighborPos, state)) {
            cir.setReturnValue(false);
        }
    }
}
