package com.craisinlord.antarchy.forge.mixins.compat;

import com.craisinlord.antarchy.forge.compat.AlexsCavesStructures;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.level.structure.piece.AcidPitStructurePiece", remap = false)
public abstract class AlexsCavesAcidPitPieceMixin {
    @WrapOperation(
            method = {"postProcess", "m_213694_"},
            at = @At(value = "INVOKE", target = "Lcom/github/alexmodguy/alexscaves/server/level/structure/piece/AcidPitStructurePiece;replaceBiomes(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/resources/ResourceKey;I)V"),
            require = 0)
    private void antarchy$variantBiome(@Coerce StructurePiece piece, WorldGenLevel level, ResourceKey<Biome> biome, int radius, Operation<Void> original) {
        original.call(piece, level, AlexsCavesStructures.replacementBiome(level, biome), radius);
    }
}
