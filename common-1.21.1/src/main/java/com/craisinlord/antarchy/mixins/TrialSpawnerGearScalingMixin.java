package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.MobGearScaling;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TrialSpawner.class)
public abstract class TrialSpawnerGearScalingMixin {
    @ModifyArg(method = "spawnMob", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"))
    private Entity antarchy$scaleGear(Entity entity) {
        MobGearScaling.equipTrialSpawn(entity);
        return entity;
    }
}
