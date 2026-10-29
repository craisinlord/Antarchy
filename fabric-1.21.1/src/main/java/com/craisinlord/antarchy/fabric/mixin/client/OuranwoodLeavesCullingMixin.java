package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.block.LeafFaceCulling;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class OuranwoodLeavesCullingMixin {

    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void antarchy$cullBuriedLeafFaces(BlockState state, BlockGetter level, BlockPos offset, Direction face, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState neighbor = level.getBlockState(pos);
        if (LeafFaceCulling.appliesTo(neighbor) && LeafFaceCulling.hidesNeighborFace(level, pos, state)) {
            cir.setReturnValue(false);
        }
    }
}
