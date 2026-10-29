package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaGenProfiler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorProfilerMixin {
    @WrapMethod(method = "applyBiomeDecoration")
    private void antarchy$profileFeatures(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, Operation<Void> original) {
        long start = ElythiaGenProfiler.begin();
        original.call(level, chunk, structureManager);
        ElythiaGenProfiler.end(start, (ChunkGenerator) (Object) this, ElythiaGenProfiler.Stage.FEATURES, chunk);
    }

    @WrapMethod(method = "createStructures")
    private void antarchy$profileStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager, Operation<Void> original) {
        long start = ElythiaGenProfiler.begin();
        original.call(registryAccess, structureState, structureManager, chunk, templateManager);
        ElythiaGenProfiler.end(start, (ChunkGenerator) (Object) this, ElythiaGenProfiler.Stage.STRUCTURES, chunk);
    }
}
