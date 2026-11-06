package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.block.PrimordialGrassBlock;
import com.craisinlord.antarchy.content.client.renderer.PrimordialGrassRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class PrimordialGrassChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void antarchy$grassChanged(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> callback) {
        BlockState previous = callback.getReturnValue();
        if (previous == null || !((LevelChunk) (Object) this).getLevel().isClientSide()) return;
        if (previous.getBlock() instanceof PrimordialGrassBlock || state.getBlock() instanceof PrimordialGrassBlock) PrimordialGrassRenderer.blockChanged(pos);
    }
}
