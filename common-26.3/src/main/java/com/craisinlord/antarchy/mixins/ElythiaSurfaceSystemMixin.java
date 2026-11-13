package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaSurfaceBuilder;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.material.MaterialSystem;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MaterialSystem.class)
public abstract class ElythiaSurfaceSystemMixin {
    @Shadow
    @Final
    private BlockState defaultBlock;

    @Inject(
            method = "buildSurface(Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/levelgen/material/rule/MaterialRule;Ljava/util/Set;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$buildElythiaSurface(
            RandomState randomState,
            BiomeManager biomeManager,
            WorldGenerationContext generationContext,
            ChunkAccess chunk,
            NoiseChunk noiseChunk,
            MaterialRule ruleSource,
            @Nullable Set<Holder<Biome>> possibleBiomes,
            CallbackInfo ci
    ) {
        if (ruleSource instanceof ElythiaSurfaceBuilder.Root root) {
            ElythiaSurfaceBuilder.build(root, (MaterialSystem) (Object) this, randomState, biomeManager, generationContext, chunk, noiseChunk, possibleBiomes, this.defaultBlock);
            ci.cancel();
        }
    }
}
