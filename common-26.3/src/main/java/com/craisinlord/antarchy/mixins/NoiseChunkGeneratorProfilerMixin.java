package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaGenProfiler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseChunkGeneratorProfilerMixin {
    @WrapMethod(method = "doFill")
    private void antarchy$profileNoise(NoiseChunk noiseChunk, ChunkAccess chunk, Operation<Void> original) {
        long start = ElythiaGenProfiler.begin();
        original.call(noiseChunk, chunk);
        ElythiaGenProfiler.end(start, (ChunkGenerator) (Object) this, ElythiaGenProfiler.Stage.NOISE, chunk);
    }

    @WrapMethod(method = "buildSurface")
    private void antarchy$profileSurface(ChunkAccess chunk, NoiseChunk noiseChunk, RandomState randomState, BiomeManager biomeManager, Set<Holder<Biome>> biomes, MaterialRule materialRule, Operation<Void> original) {
        long start = ElythiaGenProfiler.begin();
        original.call(chunk, noiseChunk, randomState, biomeManager, biomes, materialRule);
        ElythiaGenProfiler.end(start, (ChunkGenerator) (Object) this, ElythiaGenProfiler.Stage.SURFACE, chunk);
    }
}
