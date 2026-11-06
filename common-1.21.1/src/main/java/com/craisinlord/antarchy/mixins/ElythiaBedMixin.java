package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ElythiaBedMixin {
    @ModifyReturnValue(method = "startSleepInBed", at = @At("RETURN"))
    private Either<Player.BedSleepingProblem, Unit> antarchy$alreadyDreaming(Either<Player.BedSleepingProblem, Unit> result, BlockPos pos) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (!AntarchySettings.elythiaAlreadyDreaming() || !player.level().dimension().equals(ThrowbackZones.ELYTHIA)) {
            return result;
        }
        if (result.left().orElse(null) == Player.BedSleepingProblem.NOT_POSSIBLE_NOW) {
            if (!player.isCreative() && antarchy$monstersNearby(player, pos)) {
                return Either.left(Player.BedSleepingProblem.NOT_SAFE);
            }
            player.startSleeping(pos);
            player.awardStat(Stats.SLEEP_IN_BED);
            CriteriaTriggers.SLEPT_IN_BED.trigger(player);
            player.serverLevel().updateSleepingPlayerList();
            result = Either.right(Unit.INSTANCE);
        }
        if (result.right().isPresent()) {
            player.displayClientMessage(Component.translatable("message.antarchy.already_dreaming"), true);
        }
        return result;
    }

    @Unique
    private static boolean antarchy$monstersNearby(ServerPlayer player, BlockPos pos) {
        Vec3 center = Vec3.atBottomCenterOf(pos);
        AABB area = new AABB(center.x - 8.0D, center.y - 5.0D, center.z - 8.0D, center.x + 8.0D, center.y + 5.0D, center.z + 8.0D);
        return !player.level().getEntitiesOfClass(Monster.class, area, monster -> monster.isPreventingPlayerRest(player)).isEmpty();
    }
}
