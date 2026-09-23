package com.craisinlord.antarchy.content.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import com.craisinlord.antarchy.content.item.TemporalTunerItem;
import com.craisinlord.antarchy.content.effect.DilatedMobEffect;
import com.craisinlord.antarchy.content.effect.ContractedMobEffect;
import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;

public final class TimeDilationManager {
    private static final Map<ServerLevel, TrackingState> TRACKING = new WeakHashMap<>();
    private static final Map<ServerLevel, Set<TimeDilationFieldEntity>> ACTIVE_FIELDS = new WeakHashMap<>();

    private TimeDilationManager() {
    }

    public static void tickServer(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            Set<TimeDilationFieldEntity> activeFields = ACTIVE_FIELDS.get(level);
            if (activeFields != null) {
                activeFields.removeIf(field -> !field.isAlive() || field.isRemoved());
                if (activeFields.isEmpty()) {
                    ACTIVE_FIELDS.remove(level);
                    activeFields = null;
                }
            }
            TrackingState tracking = TRACKING.get(level);
            for (ServerPlayer player : level.players()) {
                trackPotentiallyAffected(player);
            }
            if (activeFields == null && (tracking == null || tracking.entities.isEmpty())) {
                continue;
            }
            List<TimeDilationFieldEntity> fields = activeFields == null ? List.of() : List.copyOf(activeFields);
            if (tracking == null) {
                tracking = TRACKING.computeIfAbsent(level, ignored -> new TrackingState());
            }
            discoverFieldEntities(level, fields, tracking);
            syncFieldSnapshots(level, fields, tracking);
            updateEntities(level, fields, tracking);
            if (fields.isEmpty() && tracking.entities.isEmpty()) {
                TRACKING.remove(level);
            }
            if (!fields.isEmpty()) {
                TimeDilationParticles.spawnFieldBorders(level, fields.stream().filter(TimeDilationFieldEntity::isVisual).toList());
            }
        }
    }

    public static void registerActiveField(TimeDilationFieldEntity field) {
        if (field.level() instanceof ServerLevel level) {
            ACTIVE_FIELDS.computeIfAbsent(level, ignored -> Collections.newSetFromMap(new IdentityHashMap<>())).add(field);
        }
    }

    public static void trackPotentiallyAffected(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level) || entity instanceof TimeDilationFieldEntity) {
            return;
        }
        boolean hasTuner = entity instanceof Player player && TemporalTunerItem.isAvailable(player);
        boolean hasEffect = entity instanceof net.minecraft.world.entity.LivingEntity living
                && ((RoyalEffectHooks.dilatedHolder() != null && living.hasEffect(RoyalEffectHooks.dilatedHolder()))
                || (RoyalEffectHooks.contractedHolder() != null && living.hasEffect(RoyalEffectHooks.contractedHolder())));
        if (entity instanceof TimeDilationEntityAccess access
                && Math.abs(access.antarchy$getTimeDilationRate() - TimeDilationMath.NORMAL_RATE) < 0.001D
                && Math.abs(access.antarchy$getInheritedTimeDilationRate() - TimeDilationMath.NORMAL_RATE) < 0.001D
                && Math.abs(TimeDilationApi.getVehicleRate(entity) - TimeDilationMath.NORMAL_RATE) < 0.001D
                && !hasTuner && !hasEffect) {
            return;
        }
        TRACKING.computeIfAbsent(level, ignored -> new TrackingState()).entities.add(entity);
    }

    public static void resyncPersistentEffects(ServerPlayer player) {
        if (!(player instanceof TimeDilationEntityAccess access)) {
            return;
        }

        double targetRate = TimeDilationMath.NORMAL_RATE;
        if (RoyalEffectHooks.dilatedHolder() != null) {
            var effect = player.getEffect(RoyalEffectHooks.dilatedHolder());
            if (effect != null) {
                targetRate = Math.min(targetRate, DilatedMobEffect.rateForAmplifier(effect.getAmplifier()));
            }
        }
        if (RoyalEffectHooks.contractedHolder() != null) {
            var effect = player.getEffect(RoyalEffectHooks.contractedHolder());
            if (effect != null && targetRate >= TimeDilationMath.NORMAL_RATE) {
                targetRate = Math.max(targetRate, ContractedMobEffect.rateForAmplifier(effect.getAmplifier()));
            }
        }

        if (Math.abs(targetRate - TimeDilationMath.NORMAL_RATE) >= 0.001D) {
            access.antarchy$setTimeDilationRate(targetRate);
            TimeDilationApi.syncEntityRate(player, targetRate);
        }
        trackPotentiallyAffected(player);
    }

    private static void syncFieldSnapshots(ServerLevel level, List<TimeDilationFieldEntity> fields, TrackingState tracking) {
        List<TimeDilationFieldSnapshot> snapshots = fields.stream()
                .filter(field -> !field.isChronosphere())
                .map(field -> new TimeDilationFieldSnapshot(
                        field.getX(), field.getY(), field.getZ(), field.fieldRadius(), field.fieldRate(),
                        field.fieldAge(), field.fieldDurationTicks()))
                .limit(128)
                .toList();
        if (level.getGameTime() % 4L != 0L && snapshots.size() == tracking.lastSnapshots.size()) {
            return;
        }
        tracking.lastSnapshots = snapshots;
        for (ServerPlayer player : level.players()) {
            TimeDilationApi.syncFields(player, snapshots);
        }
    }

    private static void discoverFieldEntities(ServerLevel level, List<TimeDilationFieldEntity> fields, TrackingState tracking) {
        for (TimeDilationFieldEntity field : fields) {
            double radius = field.influenceRadius();
            var center = field.position();
            var area = new net.minecraft.world.phys.AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius);
            tracking.entities.addAll(level.getEntities(field, area,
                    entity -> entity.isAlive() && !(entity instanceof TimeDilationFieldEntity)
                            && entity instanceof TimeDilationEntityAccess));
        }
    }

    private static void updateEntities(ServerLevel level, List<TimeDilationFieldEntity> fields, TrackingState tracking) {
        var iterator = tracking.entities.iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (!(entity instanceof TimeDilationEntityAccess access) || entity instanceof TimeDilationFieldEntity) {
                continue;
            }
            if (entity.getType().is(com.craisinlord.antarchy.content.AntarchyTags.Entities.TIME_DILATION_IMMUNE)
                    || (entity instanceof net.minecraft.world.entity.LivingEntity living && !RoyalEffectEligibility.canApplyDilated(living))) {
                if (RoyalEffectHooks.dilatedHolder() != null && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    living.removeEffect(RoyalEffectHooks.dilatedHolder());
                }
                TimeDilationApi.clearRate(entity);
                iterator.remove();
                continue;
            }
            double previousRate = access.antarchy$getTimeDilationRate();
            double fieldRate = TimeDilationFieldSampler.sample(fields, entity);
            double effectRate = TimeDilationMath.NORMAL_RATE;
            if (RoyalEffectHooks.dilatedHolder() != null
                    && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                var effect = living.getEffect(RoyalEffectHooks.dilatedHolder());
                if (effect != null) {
                    effectRate = DilatedMobEffect.rateForAmplifier(effect.getAmplifier());
                }
            }
            if (RoyalEffectHooks.contractedHolder() != null
                    && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                var effect = living.getEffect(RoyalEffectHooks.contractedHolder());
                if (effect != null) {
                    effectRate = Math.max(effectRate, ContractedMobEffect.rateForAmplifier(effect.getAmplifier()));
                }
            }
            double tunerRate = TimeDilationMath.NORMAL_RATE;
            if (entity instanceof Player player && player instanceof TemporalTunerAccess tunerAccess) {
                if (TemporalTunerItem.isAvailable(player)) {
                    tunerRate = TemporalTunerItem.getRate(player);
                } else {
                    tunerAccess.antarchy$setTemporalTunerRate(TimeDilationMath.NORMAL_RATE);
                    TemporalTunerItem.syncTooltipRate(player, TimeDilationMath.NORMAL_RATE);
                }
            }
            double inheritedRate = access.antarchy$getInheritedTimeDilationRate();
            double vehicleRate = TimeDilationApi.getVehicleRate(entity);
            if (vehicleRate < TimeDilationMath.NORMAL_RATE) {
                inheritedRate = Math.min(inheritedRate, vehicleRate);
            } else {
                inheritedRate = Math.max(inheritedRate, vehicleRate);
            }
            if (entity instanceof Projectile projectile && projectile.getOwner() != null) {
                double ownerRate = TimeDilationApi.getRate(projectile.getOwner());
                inheritedRate = ownerRate < TimeDilationMath.NORMAL_RATE
                        ? Math.min(inheritedRate, ownerRate)
                        : Math.max(inheritedRate, ownerRate);
            }
            double slowRate = TimeDilationMath.NORMAL_RATE;
            double fastRate = TimeDilationMath.NORMAL_RATE;
            if (fieldRate < TimeDilationMath.NORMAL_RATE) {
                slowRate = Math.min(slowRate, fieldRate);
            } else {
                fastRate = Math.max(fastRate, fieldRate);
            }
            if (effectRate < TimeDilationMath.NORMAL_RATE) {
                slowRate = Math.min(slowRate, effectRate);
            } else {
                fastRate = Math.max(fastRate, effectRate);
            }
            if (tunerRate < TimeDilationMath.NORMAL_RATE) {
                slowRate = Math.min(slowRate, tunerRate);
            } else {
                fastRate = Math.max(fastRate, tunerRate);
            }
            if (inheritedRate < TimeDilationMath.NORMAL_RATE) {
                slowRate = Math.min(slowRate, inheritedRate);
            } else {
                fastRate = Math.max(fastRate, inheritedRate);
            }
            double targetRate = slowRate < TimeDilationMath.NORMAL_RATE ? slowRate : fastRate;
            double rate = TimeDilationMath.transitionRate(previousRate, targetRate);
            if (Math.abs(targetRate - TimeDilationMath.NORMAL_RATE) < 0.001D
                    && Math.abs(rate - TimeDilationMath.NORMAL_RATE) < 0.001D) {
                TimeDilationApi.clearRate(entity);
            } else {
                access.antarchy$setTimeDilationRate(rate);
            }
            if (Math.abs(previousRate - rate) > 0.001D) {
                TimeDilationApi.syncEntityRate(entity, rate);
            }

            boolean hasDilationEffect = entity instanceof net.minecraft.world.entity.LivingEntity living
                    && ((RoyalEffectHooks.dilatedHolder() != null && living.hasEffect(RoyalEffectHooks.dilatedHolder()))
                    || (RoyalEffectHooks.contractedHolder() != null && living.hasEffect(RoyalEffectHooks.contractedHolder())));
            boolean hasInheritedRate = Math.abs(access.antarchy$getInheritedTimeDilationRate() - TimeDilationMath.NORMAL_RATE) >= 0.001D;
            boolean hasActiveRate = Math.abs(access.antarchy$getTimeDilationRate() - TimeDilationMath.NORMAL_RATE) >= 0.001D;
            if (!hasDilationEffect && !hasInheritedRate && !hasActiveRate
                    && Math.abs(targetRate - TimeDilationMath.NORMAL_RATE) < 0.001D) {
                iterator.remove();
            }
        }
        tracking.entities.removeIf(entity -> !entity.isAlive() || entity.isRemoved());
    }

    private static final class TrackingState {
        private final Set<Entity> entities = Collections.newSetFromMap(new IdentityHashMap<>());
        private List<TimeDilationFieldSnapshot> lastSnapshots = List.of();
    }

}
