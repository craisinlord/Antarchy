package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.MobGearScaling;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobGearScalingMixin {
    @Unique
    private MobSpawnType antarchy$gearSpawnType;

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void antarchy$rememberSpawnType(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
            SpawnGroupData spawnGroupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        this.antarchy$gearSpawnType = spawnType;
    }

    @Inject(method = "populateDefaultEquipmentEnchantments", at = @At("TAIL"))
    private void antarchy$scaleGear(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        MobSpawnType spawnType = this.antarchy$gearSpawnType;
        this.antarchy$gearSpawnType = null;
        if (spawnType != null) {
            MobGearScaling.equip((Mob) (Object) this, level, spawnType);
        }
    }
}
