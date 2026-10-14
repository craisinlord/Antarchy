package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public abstract class ThoraxisCeilingSpawnMixin {
    @WrapOperation(
            method = "isValidSpawnPostitionForType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;isSpawnPositionOk(Lnet/minecraft/world/entity/SpawnPlacements$Type;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntityType;)Z")
    )
    private static boolean antarchy$allowCeilingPlacement(SpawnPlacements.Type placementType, LevelReader level, BlockPos pos, EntityType<?> type,
                                                          Operation<Boolean> original) {
        if (level instanceof ServerLevel serverLevel && ThoraxisUndersideManager.isUndersideCeilingSpawn(type, serverLevel, pos)) {
            return true;
        }
        return original.call(placementType, level, pos, type);
    }

    @WrapOperation(
            method = "isValidSpawnPostitionForType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;noCollision(Lnet/minecraft/world/phys/AABB;)Z")
    )
    private static boolean antarchy$checkCeilingSpawnBounds(ServerLevel level, AABB bounds, Operation<Boolean> original,
                                                            @Local(argsOnly = true) MobSpawnSettings.SpawnerData spawnerData,
                                                            @Local(argsOnly = true) BlockPos.MutableBlockPos pos) {
        if (ThoraxisUndersideManager.isUndersideCeilingSpawn(spawnerData.type, level, pos)) {
            return original.call(level, ThoraxisUndersideManager.ceilingSpawnBounds(spawnerData.type, pos));
        }
        return original.call(level, bounds);
    }

    @WrapOperation(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;moveTo(DDDFF)V")
    )
    private static void antarchy$placeCeilingSpawn(Mob mob, double x, double y, double z, float yRot, float xRot, Operation<Void> original,
                                                   @Local(argsOnly = true) ServerLevel level) {
        BlockPos pos = BlockPos.containing(x, y, z);
        if (ThoraxisUndersideManager.isUndersideCeilingSpawn(mob.getType(), level, pos)) {
            original.call(mob, x, ThoraxisUndersideManager.ceilingSpawnFeetY(mob.getType(), pos, mob.getBbHeight()), z, yRot, xRot);
            ThoraxisUndersideManager.applyUndersideInversion(mob);
            return;
        }
        original.call(mob, x, y, z, yRot, xRot);
    }
}
