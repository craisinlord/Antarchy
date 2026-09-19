package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnContext;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the Undertrial's vanilla TrialSpawner path spawn entities whose normal
 * placement predicate assumes floor support. TrialSpawner already performs
 * its own no-collision and obstruction checks before this method is reached.
 */
@Mixin(SpawnPlacements.class)
public abstract class UndertrialSpawnPlacementsMixin {
    @Inject(method = "checkSpawnRules", at = @At("RETURN"), cancellable = true)
    private static <T extends net.minecraft.world.entity.Entity> void antarchy$allowUndertrialSpawn(
            net.minecraft.world.entity.EntityType<T> entityType,
            net.minecraft.world.level.ServerLevelAccessor level,
            MobSpawnType spawnReason,
            net.minecraft.core.BlockPos pos,
            net.minecraft.util.RandomSource random,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()
                && spawnReason == MobSpawnType.TRIAL_SPAWNER
                && UndertrialSpawnContext.isActive()) {
            cir.setReturnValue(true);
        }
    }
}
