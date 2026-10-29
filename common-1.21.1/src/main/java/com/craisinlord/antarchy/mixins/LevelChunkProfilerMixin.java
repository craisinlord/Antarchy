package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaGenProfiler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelChunk.class)
public abstract class LevelChunkProfilerMixin {
    @WrapMethod(method = "postProcessGeneration")
    private void antarchy$profilePostProcess(Operation<Void> original) {
        long start = ElythiaGenProfiler.begin();
        original.call();
        LevelChunk chunk = (LevelChunk) (Object) this;
        if (start != 0L && chunk.getLevel() instanceof ServerLevel level) {
            ElythiaGenProfiler.end(start, ElythiaGenProfiler.label(level.getChunkSource().getGenerator()), ElythiaGenProfiler.Stage.POST_PROCESS, chunk.getPos());
        }
    }
}
