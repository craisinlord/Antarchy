package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.block.PrimordialGrassBlock;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class PrimordialGrassMeshingMixin {
    @ModifyExpressionValue(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;immutable()Lnet/minecraft/core/BlockPos;", ordinal = 0))
    private BlockPos antarchy$beginGrassMeshing(BlockPos origin) {
        PrimordialGrassBlock.beginMeshing(origin.getX() >> 4, origin.getY() >> 4, origin.getZ() >> 4);
        return origin;
    }

    @Inject(method = "compile", at = @At("RETURN"))
    private void antarchy$endGrassMeshing(CallbackInfoReturnable<?> callback) {
        PrimordialGrassBlock.endMeshing();
    }
}
