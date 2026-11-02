package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaAquifer;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaHydrology;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaHydrologyHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseChunk.class)
public abstract class ElythiaAquiferMixin {
    @Shadow
    protected abstract DensityFunction wrap(DensityFunction densityFunction);

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/Aquifer;create(Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/NoiseRouter;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;IILnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;)Lnet/minecraft/world/level/levelgen/Aquifer;"
            )
    )
    private Aquifer antarchy$wrapElythiaAquifer(
            NoiseChunk noiseChunk,
            ChunkPos chunkPos,
            NoiseRouter router,
            PositionalRandomFactory random,
            int minY,
            int height,
            Aquifer.FluidPicker fluidPicker,
            Operation<Aquifer> original,
            @Local(argsOnly = true) RandomState randomState
    ) {
        Aquifer aquifer = original.call(noiseChunk, chunkPos, router, random, minY, height, fluidPicker);
        // Prefer the hydrology captured on RandomState before C2ME's density function compiler rebuilt the router, and give
        // it this chunk's vanilla caches. That is exactly what vanilla hands the aquifer, whatever the router turned into.
        ElythiaHydrology captured = ((ElythiaHydrologyHolder) (Object) randomState).antarchy$hydrology();
        ElythiaHydrology hydrology;
        if (captured != null) {
            hydrology = captured.mapAll(this::wrap) instanceof ElythiaHydrology chunkWrapped ? chunkWrapped : captured;
        } else {
            hydrology = ElythiaHydrology.find(router.veinToggle());
        }
        return hydrology != null ? new ElythiaAquifer(aquifer, noiseChunk, hydrology) : aquifer;
    }
}
