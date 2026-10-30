package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaAquifer;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaHydrology;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.densityfunction.DensitySamplerSet;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseChunk.class)
public abstract class ElythiaAquiferMixin {
    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/Aquifer$Config;create(Lnet/minecraft/world/level/levelgen/densityfunction/DensitySamplerSet;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;Lnet/minecraft/world/level/levelgen/densityfunction/DensityVolume;Lnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;)Lnet/minecraft/world/level/levelgen/Aquifer;"
            )
    )
    private Aquifer antarchy$wrapElythiaAquifer(
            Aquifer.Config config,
            DensitySamplerSet samplers,
            PositionalRandomFactory random,
            DensityVolume volume,
            Aquifer.FluidPicker fluidPicker,
            Operation<Aquifer> original,
            @Local(argsOnly = true) NoiseGeneratorSettings settings
    ) {
        Aquifer aquifer = original.call(config, samplers, random, volume, fluidPicker);
        for (NoiseGeneratorSettings.DebugFunctionEntry entry : settings.debugFunctions().functions()) {
            if (entry.function() instanceof ElythiaHydrology hydrology) {
                return new ElythiaAquifer(aquifer, hydrology, samplers, settings.noiseRouter().chunkSurfaceLevel());
            }
        }
        return aquifer;
    }
}
