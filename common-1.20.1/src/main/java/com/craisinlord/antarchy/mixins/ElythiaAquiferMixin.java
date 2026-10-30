package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaAquifer;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaHydrology;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseChunk.class)
public abstract class ElythiaAquiferMixin {
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
            Operation<Aquifer> original
    ) {
        Aquifer aquifer = original.call(noiseChunk, chunkPos, router, random, minY, height, fluidPicker);
        return router.veinToggle() instanceof ElythiaHydrology hydrology ? new ElythiaAquifer(aquifer, noiseChunk, hydrology) : aquifer;
    }
}
