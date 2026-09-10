package com.craisinlord.antarchy.content.time;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.effect.DilatedMobEffect;
import com.craisinlord.antarchy.content.effect.ContractedMobEffect;
import com.craisinlord.antarchy.content.effect.TimeDilationEligibility;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

public final class TimeDilationManager {
    private static final Map<ServerLevel, TrackingState> TRACKING = new WeakHashMap<>();

    private TimeDilationManager() {
    }

    public static void tickServer(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            List<TimeDilationFieldEntity> fields = collectFields(level);
            TrackingState tracking = TRACKING.computeIfAbsent(level, ignored -> new TrackingState());
            discoverFieldEntities(level, fields, tracking);
            syncFieldSnapshots(level, fields, tracking);
            updateEntities(level, fields, tracking);
            if (fields.isEmpty() && tracking.entities.isEmpty()) {
                TRACKING.remove(level);
            }
            if (!fields.isEmpty()) {
                TimeDilationParticles.spawnFieldBorders(level, fields);
            }
        }
    }

    public static void trackPotentiallyAffected(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level) || entity instanceof TimeDilationFieldEntity) {
            return;
        }
        if (entity instanceof TimeDilationEntityAccess access
                && Math.abs(access.antarchy$getTimeDilationRate() - TimeDilationMath.NORMAL_RATE) < 0.001D
                && Math.abs(access.antarchy$getInheritedTimeDilationRate() - TimeDilationMath.NORMAL_RATE) < 0.001D
                && (!(entity instanceof LivingEntity living) || !living.hasEffect(AntarchyObjects.DILATED_EFFECT.get()))) {
            return;
        }
        TRACKING.computeIfAbsent(level, ignored -> new TrackingState()).entities.add(entity);
    }

    private static void syncFieldSnapshots(ServerLevel level, List<TimeDilationFieldEntity> fields, TrackingState tracking) {
        List<TimeDilationFieldSnapshot> snapshots = fields.stream()
                .map(field -> new TimeDilationFieldSnapshot(field.getX(), field.getY(), field.getZ(),
                        field.fieldRadius(), field.fieldRate(), field.fieldAge(), field.fieldDurationTicks()))
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

    private static List<TimeDilationFieldEntity> collectFields(ServerLevel level) {
        List<TimeDilationFieldEntity> fields = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof TimeDilationFieldEntity field && field.isAlive()) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static void discoverFieldEntities(ServerLevel level, List<TimeDilationFieldEntity> fields, TrackingState tracking) {
        for (TimeDilationFieldEntity field : fields) {
            double radius = field.fieldRadius();
            var center = field.position();
            var area = new net.minecraft.world.phys.AABB(center.x - radius, center.y - radius, center.z - radius,
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
            boolean blocked = entity.getType().is(AntarchyTags.Entities.TIME_DILATION_IMMUNE)
                    || entity.getType().is(AntarchyTags.Entities.DILATED_BLACKLIST);
            if (entity instanceof LivingEntity living) {
                blocked = blocked || !TimeDilationEligibility.canApply(living);
            }
            if (blocked) {
                if (entity instanceof LivingEntity living) {
                    living.removeEffect(AntarchyObjects.DILATED_EFFECT.get());
                }
                TimeDilationApi.clearRate(entity);
                iterator.remove();
                continue;
            }

            double previousRate = access.antarchy$getTimeDilationRate();
            double fieldRate = TimeDilationFieldSampler.sample(fields, entity);
            double effectRate = TimeDilationMath.NORMAL_RATE;
            if (entity instanceof LivingEntity living) {
                var effect = living.getEffect(AntarchyObjects.DILATED_EFFECT.get());
                if (effect != null) {
                    effectRate = DilatedMobEffect.rateForAmplifier(effect.getAmplifier());
                }
                var contracted = living.getEffect(AntarchyObjects.CONTRACTED_EFFECT.get());
                if (contracted != null) {
                    effectRate = Math.max(effectRate, ContractedMobEffect.rateForAmplifier(contracted.getAmplifier()));
                }
            }
            double inheritedRate = access.antarchy$getInheritedTimeDilationRate();
            if (entity instanceof Projectile projectile && projectile.getOwner() != null) {
                double ownerRate = TimeDilationApi.getRate(projectile.getOwner());
                inheritedRate = ownerRate < TimeDilationMath.NORMAL_RATE
                        ? Math.min(inheritedRate, ownerRate) : Math.max(inheritedRate, ownerRate);
            }
            double slowRate = TimeDilationMath.NORMAL_RATE;
            double fastRate = TimeDilationMath.NORMAL_RATE;
            for (double candidate : new double[]{fieldRate, effectRate, inheritedRate}) {
                if (candidate < TimeDilationMath.NORMAL_RATE) slowRate = Math.min(slowRate, candidate);
                else fastRate = Math.max(fastRate, candidate);
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
            boolean hasEffect = entity instanceof LivingEntity living && living.hasEffect(AntarchyObjects.DILATED_EFFECT.get());
            boolean hasInheritedRate = Math.abs(access.antarchy$getInheritedTimeDilationRate() - TimeDilationMath.NORMAL_RATE) >= 0.001D;
            boolean hasActiveRate = Math.abs(access.antarchy$getTimeDilationRate() - TimeDilationMath.NORMAL_RATE) >= 0.001D;
            if (!hasEffect && !hasInheritedRate && !hasActiveRate
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
