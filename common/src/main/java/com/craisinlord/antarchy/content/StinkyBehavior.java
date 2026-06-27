package com.craisinlord.antarchy.content;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class StinkyBehavior {
    public static final double STINK_BUG_AURA_RADIUS = 5.0D;
    public static final double STINKY_NAUSEA_RADIUS = 4.0D;
    private static final int TRAIL_INTERVAL_TICKS = 4;
    private static final int REACTION_INTERVAL_TICKS = 10;
    private static final double ARTHROPOD_ALERT_RADIUS = 28.0D;
    private static final double FLEE_RADIUS = 10.0D;
    private static final double FLEE_DISTANCE = 8.0D;
    private static final int STINKY_NAUSEA_DURATION_TICKS = 60;
    private static final int STINK_BUG_BURST_NAUSEA_DURATION_TICKS = 100;

    private StinkyBehavior() {
    }

    public static void tickStinkyTrail(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!entity.isAlive() || entity.tickCount % TRAIL_INTERVAL_TICKS != 0 || !entity.hasEffect(AntarchyObjects.STINKY_EFFECT.get())) {
            return;
        }

        Vec3 motion = entity.getDeltaMovement();
        Vec3 horizontalMotion = new Vec3(motion.x, 0.0D, motion.z);
        Vec3 direction = horizontalMotion.lengthSqr() > 1.0E-4D
                ? horizontalMotion.normalize()
                : entity.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() <= 1.0E-4D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            direction = direction.normalize();
        }

        Vec3 origin = entity.position()
                .subtract(direction.scale(0.35D))
                .add(0.0D, Math.max(0.15D, entity.getBbHeight() * 0.2D), 0.0D);
        serverLevel.sendParticles(
                AntarchyObjects.STINKY_GAS.get(),
                origin.x,
                origin.y,
                origin.z,
                2,
                entity.getBbWidth() * 0.12D,
                0.04D,
                entity.getBbWidth() * 0.12D,
                0.005D
        );
        serverLevel.sendParticles(
                AntarchyObjects.STINKY_FLY.get(),
                entity.getX(),
                entity.getY(0.45D),
                entity.getZ(),
                1,
                entity.getBbWidth() * 0.35D,
                entity.getBbHeight() * 0.2D,
                entity.getBbWidth() * 0.35D,
                0.0D
        );
        applyNearbyNausea(entity, STINKY_NAUSEA_RADIUS, STINKY_NAUSEA_DURATION_TICKS);
        if (entity.tickCount % REACTION_INTERVAL_TICKS == 0) {
            reactToStinkySource(entity);
        }
    }

    public static void emitBurst(LivingEntity entity, int count) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
                AntarchyObjects.STINKY_GAS.get(),
                entity.getX(),
                entity.getY(0.45D),
                entity.getZ(),
                count,
                entity.getBbWidth() * 0.35D,
                entity.getBbHeight() * 0.25D,
                entity.getBbWidth() * 0.35D,
                0.045D
        );
        serverLevel.sendParticles(
                AntarchyObjects.STINKY_FLY.get(),
                entity.getX(),
                entity.getY(0.45D),
                entity.getZ(),
                Math.max(2, count / 6),
                entity.getBbWidth() * 0.45D,
                entity.getBbHeight() * 0.25D,
                entity.getBbWidth() * 0.45D,
                0.0D
        );
        applyNearbyNausea(entity, STINK_BUG_AURA_RADIUS, STINK_BUG_BURST_NAUSEA_DURATION_TICKS);
    }

    public static void applyNearbyNausea(LivingEntity source, double radius, int durationTicks) {
        if (source.level().isClientSide()) {
            return;
        }

        for (Player player : source.level().getEntitiesOfClass(
                Player.class,
                source.getBoundingBox().inflate(radius),
                player -> player.isAlive() && !player.isSpectator() && player != source
        )) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, durationTicks, 0, false, true, true));
        }
    }

    private static void reactToStinkySource(LivingEntity source) {
        if (source.level().isClientSide() || !source.isAlive()) {
            return;
        }

        for (Mob mob : source.level().getEntitiesOfClass(
                Mob.class,
                source.getBoundingBox().inflate(ARTHROPOD_ALERT_RADIUS),
                mob -> mob.isAlive() && mob != source
        )) {
            if (mob.getType().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
                if (source.getType().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
                    continue;
                }
                attractArthropod(mob, source);
                continue;
            }
            if (shouldRepelMob(mob, source)) {
                repelPassiveOrNeutralMob(mob, source);
            }
        }
    }

    private static void attractArthropod(Mob mob, LivingEntity source) {
        if (!isValidStinkySourceForMob(mob, source)) {
            return;
        }

        LivingEntity currentTarget = mob.getTarget();
        if (currentTarget != null && currentTarget.isAlive() && currentTarget.hasEffect(AntarchyObjects.STINKY_EFFECT.get())
                && mob.distanceToSqr(currentTarget) <= ARTHROPOD_ALERT_RADIUS * ARTHROPOD_ALERT_RADIUS) {
            return;
        }

        mob.setTarget(source);
    }

    private static void repelPassiveOrNeutralMob(Mob mob, LivingEntity source) {
        if (!(mob instanceof PathfinderMob pathfinderMob) || !isValidStinkySourceForMob(mob, source)) {
            return;
        }

        Vec3 away = mob.position().subtract(source.position());
        if (away.lengthSqr() < 1.0E-4D) {
            away = new Vec3(mob.getRandom().nextDouble() - 0.5D, 0.0D, mob.getRandom().nextDouble() - 0.5D);
        }

        Vec3 escapeTarget = mob.position().add(away.normalize().scale(FLEE_DISTANCE));
        pathfinderMob.setTarget(null);
        pathfinderMob.getNavigation().moveTo(escapeTarget.x, escapeTarget.y, escapeTarget.z, 1.2D);
    }

    private static boolean shouldRepelMob(Mob mob, LivingEntity source) {
        if (mob.hasEffect(AntarchyObjects.STINKY_EFFECT.get()) || mob.distanceToSqr(source) > FLEE_RADIUS * FLEE_RADIUS) {
            return false;
        }

        if (mob.getType().is(AntarchyTags.Entities.STINKY_REPELLED_HOSTILES)) {
            return true;
        }

        return !(mob instanceof Enemy);
    }

    private static boolean isValidStinkySourceForMob(Mob mob, LivingEntity source) {
        if (source == mob || !source.isAlive() || !source.hasEffect(AntarchyObjects.STINKY_EFFECT.get())) {
            return false;
        }
        if (source instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return !mob.isAlliedTo(source);
    }

}
