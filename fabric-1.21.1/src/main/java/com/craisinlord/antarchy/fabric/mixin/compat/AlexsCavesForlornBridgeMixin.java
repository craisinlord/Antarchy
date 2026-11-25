package com.craisinlord.antarchy.fabric.mixin.compat;

import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesStructures;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.level.structure.ForlornBridgeStructure", remap = false)
public abstract class AlexsCavesForlornBridgeMixin {
    @WrapOperation(
            method = "biomeContinuesInDirectionFor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z", remap = true),
            require = 0)
    private boolean antarchy$matchVariant(Holder<Biome> biome, ResourceKey<Biome> key, Operation<Boolean> original) {
        return original.call(biome, key) || AlexsCavesStructures.matches(biome, key);
    }

    @ModifyConstant(method = {"atYCaveBiomePoint", "generatePieces"}, constant = @Constant(intValue = -10), require = 0)
    private int antarchy$variantY(int y, @Local(argsOnly = true) Structure.GenerationContext context) {
        return AlexsCavesStructures.structureY(y, context, AlexsCavesStructures.FORLORN_HOLLOWS);
    }
}
