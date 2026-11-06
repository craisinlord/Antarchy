package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ElythiaBedMixin {
    @WrapOperation(method = "*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z"))
    private boolean antarchy$allowDaytimeRest(BedRule rule, Level level, Operation<Boolean> original) {
        if (AntarchySettings.elythiaAlreadyDreaming() && level.dimension().equals(ThrowbackZones.ELYTHIA)
                && rule.canSleep() == BedRule.Rule.WHEN_DARK) {
            return true;
        }
        return original.call(rule, level);
    }

    @ModifyReturnValue(method = "startSleepInBed", at = @At("RETURN"))
    private Either<Player.BedSleepingProblem, Unit> antarchy$alreadyDreaming(Either<Player.BedSleepingProblem, Unit> result) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (AntarchySettings.elythiaAlreadyDreaming() && player.level().dimension().equals(ThrowbackZones.ELYTHIA)
                && result.right().isPresent()) {
            player.sendOverlayMessage(Component.translatable("message.antarchy.already_dreaming"));
        }
        return result;
    }
}
