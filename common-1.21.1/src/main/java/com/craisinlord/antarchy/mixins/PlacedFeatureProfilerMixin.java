package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaGenProfiler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureProfilerMixin {
    @WrapMethod(method = "placeWithBiomeCheck")
    private boolean antarchy$profileFeature(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin, Operation<Boolean> original) {
        long start = ElythiaGenProfiler.begin();
        boolean result = original.call(level, generator, random, origin);
        if (start != 0L) {
            ResourceLocation id = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getKey((PlacedFeature) (Object) this);
            ElythiaGenProfiler.endDetail(start, "feature", id == null ? "inline" : id.toString(), new ChunkPos(origin));
        }
        return result;
    }
}
