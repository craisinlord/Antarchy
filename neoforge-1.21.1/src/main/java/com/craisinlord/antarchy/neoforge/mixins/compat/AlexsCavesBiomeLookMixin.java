package com.craisinlord.antarchy.neoforge.mixins.compat;

import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesStructures;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry", remap = false)
public abstract class AlexsCavesBiomeLookMixin {
    @WrapOperation(
            method = {"getBiomeAmbientLight", "getBiomeFogNearness", "getBiomeWaterFogFarness", "getBiomeSkyOverride", "getBiomeLightColorOverride"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z"),
            require = 0)
    private static boolean antarchy$matchVariant(Holder<Biome> biome, ResourceKey<Biome> key, Operation<Boolean> original) {
        return original.call(biome, key) || AlexsCavesStructures.matches(biome, key);
    }
}
