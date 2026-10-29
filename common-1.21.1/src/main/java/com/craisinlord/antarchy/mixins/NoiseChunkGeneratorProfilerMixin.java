package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaGenProfiler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseChunkGeneratorProfilerMixin {
    @WrapMethod(method = "doCreateBiomes")
    private void antarchy$profileBiomes(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk, Operation<Void> original) {
        long start = ElythiaGenProfiler.begin();
        original.call(blender, randomState, structureManager, chunk);
        ElythiaGenProfiler.end(start, (ChunkGenerator) (Object) this, ElythiaGenProfiler.Stage.BIOMES, chunk);
    }

    @WrapMethod(method = "doFill")
    private ChunkAccess antarchy$profileNoise(Blender blender, StructureManager structureManager, RandomState randomState, ChunkAccess chunk, int minCellY, int cellCountY, Operation<ChunkAccess> original) {
        long start = ElythiaGenProfiler.begin();
        ChunkAccess result = original.call(blender, structureManager, randomState, chunk, minCellY, cellCountY);
        ElythiaGenProfiler.end(start, (ChunkGenerator) (Object) this, ElythiaGenProfiler.Stage.NOISE, chunk);
        return result;
    }

    @WrapMethod(method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V")
    private void antarchy$profileSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk, Operation<Void> original) {
        long start = ElythiaGenProfiler.begin();
        original.call(region, structureManager, randomState, chunk);
        ElythiaGenProfiler.end(start, (ChunkGenerator) (Object) this, ElythiaGenProfiler.Stage.SURFACE, chunk);
    }
}
