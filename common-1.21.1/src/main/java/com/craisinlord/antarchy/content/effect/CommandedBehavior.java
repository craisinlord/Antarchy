package com.craisinlord.antarchy.content.effect;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class CommandedBehavior {
    private CommandedBehavior() {
    }

    public static void tick(Mob mob) {
        if (!(mob instanceof CommandedEntityAccess access)) {
            return;
        }
        UUID commanderUuid = access.antarchy$getCommanderUuid();
        if (commanderUuid == null || mob.level().getServer() == null) {
            clear(mob);
            return;
        }
        ServerPlayer commander = mob.level().getServer().getPlayerList().getPlayer(commanderUuid);
        if (commander == null || commander.level() != mob.level() || !RoyalEffectEligibility.canApplyCommanded(mob)) {
            mob.removeEffect(RoyalEffectHooks.commandedHolder());
            clear(mob);
            return;
        }
        LivingEntity target = mob.getTarget();
        if (target != null && isProtectedAlly(mob, target)) {
            mob.setTarget(null);
            target = null;
        }
        if (mob.tickCount % 10 != 0 || target != null) {
            return;
        }
        LivingEntity candidate = commander.getLastHurtByMob();
        if (!isValidTarget(mob, commander, candidate)) {
            candidate = commander.getLastHurtMob();
        }
        if (!isValidTarget(mob, commander, candidate)) {
            candidate = mob.getLastHurtByMob();
        }
        if (!isValidTarget(mob, commander, candidate)) {
            candidate = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(16.0D),
                    entity -> isValidTarget(mob, commander, entity)).stream()
                    .min((a, b) -> {
                        int distance = Double.compare(mob.distanceToSqr(a), mob.distanceToSqr(b));
                        return distance != 0 ? distance : Integer.compare(a.getId(), b.getId());
                    }).orElse(null);
        }
        if (candidate != null) {
            mob.setTarget(candidate);
            access.antarchy$setCommandedTargetOwned(true);
        }
    }

    public static boolean isProtectedAlly(Mob mob, Entity entity) {
        if (!(mob instanceof CommandedEntityAccess access) || entity == null) {
            return false;
        }
        UUID commanderUuid = access.antarchy$getCommanderUuid();
        if (commanderUuid == null) {
            return false;
        }
        if (entity.getUUID().equals(commanderUuid)) {
            return true;
        }
        if (entity instanceof Mob other && other instanceof CommandedEntityAccess otherAccess) {
            return commanderUuid.equals(otherAccess.antarchy$getCommanderUuid());
        }
        if (entity instanceof LivingEntity living && living.isAlliedTo(mob)) {
            return true;
        }
        if (mob.level().getServer() != null
                && mob.level().getServer().getPlayerList().getPlayer(commanderUuid) instanceof Player commander
                && entity instanceof LivingEntity living) {
            return living.isAlliedTo(commander);
        }
        return false;
    }

    private static boolean isValidTarget(Mob mob, ServerPlayer commander, LivingEntity target) {
        return target != null && target.isAlive() && target != mob && target != commander
                && !isProtectedAlly(mob, target) && !target.isAlliedTo(commander)
                && mob.canAttack(target);
    }

    public static void clear(Mob mob) {
        if (!(mob instanceof CommandedEntityAccess access)) {
            return;
        }
        if (access.antarchy$isCommandedTargetOwned()) {
            mob.setTarget(null);
        }
        access.antarchy$setCommandedTargetOwned(false);
        access.antarchy$setCommanderUuid(null);
    }
}
