package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaSurfaceBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SurfaceSystem.class)
public abstract class ElythiaSurfaceSystemMixin {
    @Shadow
    @Final
    private BlockState defaultBlock;

    @Inject(
            method = "buildSurface(Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/core/Registry;ZLnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/levelgen/SurfaceRules$RuleSource;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$buildElythiaSurface(
            RandomState randomState,
            BiomeManager biomeManager,
            Registry<Biome> biomes,
            boolean legacyRandomSource,
            WorldGenerationContext generationContext,
            ChunkAccess chunk,
            NoiseChunk noiseChunk,
            SurfaceRules.RuleSource ruleSource,
            CallbackInfo ci
    ) {
        if (ruleSource instanceof ElythiaSurfaceBuilder.Root root) {
            ElythiaSurfaceBuilder.build(root, (SurfaceSystem) (Object) this, randomState, biomeManager, biomes, generationContext, chunk, noiseChunk, this.defaultBlock);
            ci.cancel();
        }
    }
}
