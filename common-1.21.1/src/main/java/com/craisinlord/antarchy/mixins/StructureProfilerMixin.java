package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaGenProfiler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Structure.class)
public abstract class StructureProfilerMixin {
    @WrapMethod(method = "generate")
    private StructureStart antarchy$profileStructure(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager templateManager, long seed, ChunkPos chunkPos, int references, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome, Operation<StructureStart> original) {
        long start = ElythiaGenProfiler.begin();
        StructureStart result = original.call(registryAccess, chunkGenerator, biomeSource, randomState, templateManager, seed, chunkPos, references, heightAccessor, validBiome);
        if (start != 0L) {
            ResourceLocation id = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey((Structure) (Object) this);
            ElythiaGenProfiler.endDetail(start, "structure", id == null ? "unknown" : id.toString(), chunkPos);
        }
        return result;
    }
}
