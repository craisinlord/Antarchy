package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.block.PrimordialGrassBlock;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public abstract class PrimordialGrassSodiumMeshingMixin {
    @Shadow
    @Final
    private RenderSection render;

    @Inject(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At("HEAD"))
    private void antarchy$beginGrassMeshing(ChunkBuildContext context, CancellationToken token, CallbackInfoReturnable<ChunkBuildOutput> callback) {
        PrimordialGrassBlock.beginMeshing(this.render.getChunkX(), this.render.getChunkY(), this.render.getChunkZ());
    }

    @Inject(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At("RETURN"))
    private void antarchy$endGrassMeshing(ChunkBuildContext context, CancellationToken token, CallbackInfoReturnable<ChunkBuildOutput> callback) {
        PrimordialGrassBlock.endMeshing();
    }
}
