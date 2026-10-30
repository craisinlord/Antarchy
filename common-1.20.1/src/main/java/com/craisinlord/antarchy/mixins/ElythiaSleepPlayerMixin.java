package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class ElythiaSleepPlayerMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/event/ForgeEventFactory;fireSleepingTimeCheck(Lnet/minecraft/world/entity/player/Player;Ljava/util/Optional;)Z"))
    private boolean antarchy$keepDreaming(Player player, Optional<BlockPos> sleepingPos, Operation<Boolean> original) {
        Level level = player.level();
        if (AntarchySettings.elythiaAlreadyDreaming() && level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            return true;
        }
        return original.call(player, sleepingPos);
    }
}
