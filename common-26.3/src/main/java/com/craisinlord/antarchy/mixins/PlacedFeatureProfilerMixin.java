package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaGenProfiler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.FeaturePlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FeaturePlacer.class)
public abstract class PlacedFeatureProfilerMixin {
    @Shadow
    @Final
    private WorldGenLevel level;

    @WrapMethod(method = "placeWithBiomeCheck")
    private boolean antarchy$profileFeature(PlacedFeature feature, RandomSource random, BlockPos origin, Operation<Boolean> original) {
        long start = ElythiaGenProfiler.begin();
        boolean result = original.call(feature, random, origin);
        if (start != 0L) {
            Identifier id = this.level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).getKey(feature);
            ElythiaGenProfiler.endDetail(start, "feature", id == null ? "inline" : id.toString(), ChunkPos.containing(origin));
        }
        return result;
    }
}
