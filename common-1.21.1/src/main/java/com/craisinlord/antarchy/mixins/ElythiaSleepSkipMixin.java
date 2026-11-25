package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ElythiaSleepSkipMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z"))
    private boolean antarchy$noDaytimeSkip(SleepStatus status, int percentage, Operation<Boolean> original) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (AntarchySettings.elythiaAlreadyDreaming() && level.dimension().equals(ThrowbackZones.ELYTHIA) && level.isDay()) {
            return false;
        }
        return original.call(status, percentage);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
    private void antarchy$skipSharedNight(ServerLevel level, long time, Operation<Void> original) {
        original.call(antarchy$sleepClock(level), time);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;resetWeatherCycle()V"))
    private void antarchy$clearSharedWeather(ServerLevel level, Operation<Void> original) {
        original.call(antarchy$sleepClock(level));
    }

    @Unique
    private static ServerLevel antarchy$sleepClock(ServerLevel level) {
        return level.dimension().equals(ThrowbackZones.ELYTHIA) ? level.getServer().overworld() : level;
    }
}
