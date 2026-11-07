package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnPlacements.class)
public abstract class UndertrialSpawnPlacementsMixin {
    @Inject(method = "checkSpawnRules", at = @At("RETURN"), cancellable = true)
    private static <T extends Entity> void antarchy$allowUndertrialSpawn(
            EntityType<T> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnReason,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()
                && spawnReason == MobSpawnType.TRIAL_SPAWNER
                && UndertrialSpawnContext.isActive()) {
            cir.setReturnValue(true);
        }
    }
}
