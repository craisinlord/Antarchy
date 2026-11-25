package com.craisinlord.antarchy.fabric.mixin.compat;

import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesSpawnGuard;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NaturalSpawner.class, priority = 500)
public abstract class AlexsCavesSpawnGuardMixin {
    @Inject(method = "spawnMobsForChunkGeneration", at = @At("TAIL"), cancellable = true, require = 0)
    private static void antarchy$skipEndlessCaveSpawns(ServerLevelAccessor level, Holder<Biome> biome, ChunkPos chunk, RandomSource random, CallbackInfo ci) {
        if (AlexsCavesSpawnGuard.neverEnds(level, chunk)) {
            ci.cancel();
        }
    }
}
