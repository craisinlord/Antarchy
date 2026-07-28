package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.ElythiaFrog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frog.class)
public abstract class FrogSpawnVariantMixin {
    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void antarchy$setElythiaSpawnVariant(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> cir
    ) {
        if (spawnType == MobSpawnType.BUCKET || !ElythiaFrog.shouldBecomeElythiaFrog(level)) {
            return;
        }

        ElythiaFrog.applyVariant((Frog) (Object) this);
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void antarchy$setElythiaOffspringVariant(ServerLevel level, AgeableMob otherParent, CallbackInfoReturnable<AgeableMob> cir) {
        if (!(cir.getReturnValue() instanceof Frog frog) || !ElythiaFrog.shouldBecomeElythiaFrog(level)) {
            return;
        }

        ElythiaFrog.applyVariant(frog);
    }
}
