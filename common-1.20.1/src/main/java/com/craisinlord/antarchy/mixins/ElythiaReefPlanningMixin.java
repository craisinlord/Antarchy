package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaBiomeSource;
import com.craisinlord.antarchy.content.worldgen.elythia.reef.ElythiaReefFeature;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class ElythiaReefPlanningMixin {
    @Inject(method = "createBiomes", at = @At("HEAD"))
    private void antarchy$prepareReefCell(
            Executor executor,
            RandomState randomState,
            Blender blender,
            StructureManager structureManager,
            ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        ChunkGenerator generator = (ChunkGenerator) (Object) this;
        if (generator.getBiomeSource() instanceof ElythiaBiomeSource) {
            LevelAccessor level = ((StructureManagerLevelAccessor) structureManager).antarchy$level();
            if (level instanceof ServerLevelAccessor serverLevel) {
                ElythiaReefFeature.prepare(serverLevel.getLevel(), generator, chunk.getPos());
            }
        }
    }
}
