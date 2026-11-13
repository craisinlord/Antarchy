package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.block.PrimordialGrassBlock;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public abstract class PrimordialGrassSodiumMeshingMixin extends ChunkBuilderTask<ChunkBuildOutput> {
    protected PrimordialGrassSodiumMeshingMixin(RenderSection render, int time, Vector3dc camera) {
        super(render, time, camera);
    }

    @Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At("HEAD"))
    private void antarchy$beginGrassMeshing(ChunkBuildContext context, CancellationToken token, CallbackInfoReturnable<ChunkBuildOutput> callback) {
        PrimordialGrassBlock.beginMeshing(this.render.getChunkX(), this.render.getChunkY(), this.render.getChunkZ());
    }

    @Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At("RETURN"))
    private void antarchy$endGrassMeshing(ChunkBuildContext context, CancellationToken token, CallbackInfoReturnable<ChunkBuildOutput> callback) {
        PrimordialGrassBlock.endMeshing();
    }
}
