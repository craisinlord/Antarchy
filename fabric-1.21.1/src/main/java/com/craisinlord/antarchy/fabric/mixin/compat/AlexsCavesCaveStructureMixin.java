package com.craisinlord.antarchy.fabric.mixin.compat;

import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesStructures;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.level.structure.AbstractCaveGenerationStructure", remap = false)
public abstract class AlexsCavesCaveStructureMixin {
    @Shadow
    @Final
    private ResourceKey<Biome> matchingBiome;

    @WrapOperation(
            method = "biomeContinuesInDirectionFor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z", remap = true),
            require = 0)
    private boolean antarchy$matchVariant(Holder<Biome> biome, ResourceKey<Biome> key, Operation<Boolean> original) {
        return original.call(biome, key) || AlexsCavesStructures.matches(biome, key);
    }

    @WrapOperation(
            method = {"atYCaveBiomePoint", "generatePieces"},
            at = {
                    @At(value = "INVOKE", target = "Lcom/github/alexmodguy/alexscaves/server/level/structure/AbstractCaveGenerationStructure;getGenerateYHeight(Lnet/minecraft/world/level/levelgen/WorldgenRandom;II)I", remap = false),
                    @At(value = "INVOKE", target = "Lcom/github/alexmodguy/alexscaves/server/level/structure/AbstractCaveGenerationStructure;getGenerateYHeight(Lnet/minecraft/class_2919;II)I", remap = false)
            },
            require = 0)
    private int antarchy$variantY(@Coerce Structure self, WorldgenRandom random, int x, int z, Operation<Integer> original,
                                  @Local(argsOnly = true) Structure.GenerationContext context) {
        return AlexsCavesStructures.structureY(original.call(self, random, x, z), context, this.matchingBiome);
    }
}
