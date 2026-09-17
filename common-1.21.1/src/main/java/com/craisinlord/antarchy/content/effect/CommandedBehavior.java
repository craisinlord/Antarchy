package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class CommandedBehavior {
    private static final ResourceLocation INVESTITURE_ATTACK = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_investiture_attack");
    private static final ResourceLocation INVESTITURE_SPEED = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_investiture_speed");
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
        Entity commander = mob.level() instanceof ServerLevel level ? level.getEntity(commanderUuid) : null;
        if (commander == null || commander.level() != mob.level() || !RoyalEffectEligibility.canApplyCommanded(mob)) {
            mob.removeEffect(RoyalEffectHooks.commandedHolder());
            clear(mob);
            return;
        }
        syncInvestiture(mob, access.antarchy$isRoyalInvested());
        if (mob.tickCount % 10 == 0) {
            LivingEntity commandedCandidate = findCommanderTarget(mob, commander);
            if (commandedCandidate != null) {
                mob.setTarget(commandedCandidate);
                access.antarchy$setCommandedTargetOwned(true);
            }
        }
        LivingEntity target = mob.getTarget();
        if (target != null && isProtectedAlly(mob, target)) {
            mob.setTarget(null);
            target = null;
        }
        if (target != null) {
            tryPassiveCommandedAttack(mob, target);
        }
        if (mob.tickCount % 10 != 0 || target != null) {
            return;
        }
        LivingEntity candidate = commander instanceof Mob commanderMob ? commanderMob.getTarget() : null;
        if (!isValidTarget(mob, commander, candidate)) {
            candidate = commander instanceof net.minecraft.world.entity.player.Player player ? player.getLastHurtByMob() : null;
        }
        if (!isValidTarget(mob, commander, candidate)) {
            candidate = mob.getLastHurtByMob();
        }
        if (!isValidTarget(mob, commander, candidate)) {
            candidate = commander instanceof net.minecraft.world.entity.player.Player player ? player.getLastHurtMob() : null;
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
        if (entity instanceof LivingEntity living && commanderEntity(mob) instanceof LivingEntity livingCommander) {
            return living.isAlliedTo(livingCommander);
        }
        return false;
    }

    private static boolean isValidTarget(Mob mob, Entity commander, LivingEntity target) {
        return target != null && target.isAlive() && target != mob && target != commander
                && !isProtectedAlly(mob, target) && !target.isAlliedTo(commander);
    }

    private static LivingEntity findCommanderTarget(Mob mob, Entity commander) {
        if (mob instanceof CommandedEntityAccess access && access.antarchy$isRoyalInvested()
                && commander instanceof net.minecraft.world.entity.player.Player player) {
            LivingEntity marked = JudgmentMarkManager.markedTarget(player);
            if (isValidTarget(mob, commander, marked)) return marked;
        }
        LivingEntity target = commander instanceof Mob commanderMob ? commanderMob.getTarget() : null;
        if (isValidTarget(mob, commander, target)) {
            return target;
        }
        if (commander instanceof net.minecraft.world.entity.player.Player player) {
            target = player.getLastHurtMob();
            if (isValidTarget(mob, commander, target)) {
                return target;
            }
            target = player.getLastHurtByMob();
            if (isValidTarget(mob, commander, target)) {
                return target;
            }
        }
        return mob.level().getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(32.0D),
                other -> other != mob && other != commander && commander.equals(other.getTarget())
                        && isValidTarget(mob, commander, other)).stream()
                .min((a, b) -> Double.compare(mob.distanceToSqr(a), mob.distanceToSqr(b)))
                .orElse(null);
    }

    private static void tryPassiveCommandedAttack(Mob mob, LivingEntity target) {
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null
                && mob.getAttributeValue(Attributes.ATTACK_DAMAGE) > 0.0D) {
            return;
        }
        mob.getNavigation().moveTo(target, 1.0D);
        if (mob.tickCount % 20 != 0 || mob.distanceToSqr(target) > 4.0D) {
            return;
        }
        float damage = mob instanceof CommandedEntityAccess access && access.antarchy$isRoyalInvested()
                ? (float) AntarchySettings.royalGuardianMusterPassiveDamageFloor() : 1.0F;
        if (target.hurt(mob.damageSources().mobAttack(mob), damage)) {
            mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
    }

    private static Entity commanderEntity(Mob mob) {
        if (!(mob instanceof CommandedEntityAccess access) || mob.level().getServer() == null) {
            return null;
        }
        UUID uuid = access.antarchy$getCommanderUuid();
        return uuid == null || !(mob.level() instanceof ServerLevel level) ? null : level.getEntity(uuid);
    }

    public static void clear(Mob mob) {
        if (!(mob instanceof CommandedEntityAccess access)) {
            return;
        }
        if (access.antarchy$isCommandedTargetOwned()) {
            mob.setTarget(null);
        }
        access.antarchy$setCommandedTargetOwned(false);
        access.antarchy$setRoyalInvested(false);
        syncInvestiture(mob, false);
        access.antarchy$setCommanderUuid(null);
    }

    public static void syncInvestiture(Mob mob, boolean active) {
        setModifier(mob, Attributes.ATTACK_DAMAGE, INVESTITURE_ATTACK,
                active ? AntarchySettings.royalGuardianMusterAttackBonus() : 0.0D);
        setModifier(mob, Attributes.MOVEMENT_SPEED, INVESTITURE_SPEED,
                active ? AntarchySettings.royalGuardianMusterSpeedBonus() : 0.0D);
    }

    private static void setModifier(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            ResourceLocation id, double amount) {
        var instance = mob.getAttribute(attribute);
        if (instance == null) return;
        if (amount > 0.0D) {
            if (!instance.hasModifier(id)) {
                instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        } else {
            instance.removeModifier(id);
        }
    }
}
