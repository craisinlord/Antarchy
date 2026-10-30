package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ElythiaSleepSkipMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z"))
    private boolean antarchy$noSleepSkip(SleepStatus status, int percentage, Operation<Boolean> original) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (AntarchySettings.elythiaAlreadyDreaming() && level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            return false;
        }
        return original.call(status, percentage);
    }
}
