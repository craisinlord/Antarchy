package com.craisinlord.antarchy.content.time;

import java.util.function.BiConsumer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class TimeDilationApi {
    private static BiConsumer<Entity, Double> syncDispatcher = (entity, rate) -> {
    };
    private static BiConsumer<ServerPlayer, List<TimeDilationFieldSnapshot>> fieldSyncDispatcher = (player, fields) -> {
    };
    private static final Map<UUID, Double> SYNCED_CLIENT_RATES = new ConcurrentHashMap<>();
    private static final ThreadLocal<Integer> ROTATION_BYPASS_DEPTH = ThreadLocal.withInitial(() -> 0);

    private TimeDilationApi() {
    }

    public static void setSyncDispatcher(BiConsumer<Entity, Double> dispatcher) {
        syncDispatcher = dispatcher != null ? dispatcher : (entity, rate) -> {
        };
    }

    public static void setFieldSyncDispatcher(BiConsumer<ServerPlayer, List<TimeDilationFieldSnapshot>> dispatcher) {
        fieldSyncDispatcher = dispatcher != null ? dispatcher : (player, fields) -> {
        };
    }

    public static TimeDilationFieldEntity createField(ServerLevel level, Vec3 center, double radius, double rate, int durationTicks) {
        return createField(level, center, radius, rate, durationTicks, null);
    }

    public static TimeDilationFieldEntity createField(ServerLevel level, Vec3 center, double radius, double rate, int durationTicks, UUID ownerId) {
        return createField(level, center, radius, rate, durationTicks, ownerId, true);
    }

    public static TimeDilationFieldEntity createField(ServerLevel level, Vec3 center, double radius, double rate, int durationTicks, UUID ownerId, boolean visual) {
        TimeDilationFieldEntity field = TimeDilationFieldEntity.create(level, center, radius, rate, durationTicks, ownerId, visual);
        level.addFreshEntity(field);
        return field;
    }

    public static double getRate(Entity entity) {
        if (entity instanceof TimeDilationEntityAccess access) {
            if (entity.level().isClientSide) {
                Double syncedRate = SYNCED_CLIENT_RATES.get(entity.getUUID());
                if (syncedRate != null) {
                    return syncedRate;
                }
            }
            return access.antarchy$getTimeDilationRate();
        }
        return TimeDilationMath.NORMAL_RATE;
    }

    public static boolean isDilated(Entity entity) {
        return getRate(entity) < TimeDilationMath.NORMAL_RATE;
    }

    public static void enterRotationBypass() {
        ROTATION_BYPASS_DEPTH.set(ROTATION_BYPASS_DEPTH.get() + 1);
    }

    public static void exitRotationBypass() {
        int depth = ROTATION_BYPASS_DEPTH.get() - 1;
        if (depth <= 0) {
            ROTATION_BYPASS_DEPTH.remove();
        } else {
            ROTATION_BYPASS_DEPTH.set(depth);
        }
    }

    public static boolean isRotationBypassed() {
        return ROTATION_BYPASS_DEPTH.get() > 0;
    }

    public static void setInheritedRate(Entity entity, double rate) {
        if (entity instanceof TimeDilationEntityAccess access) {
            access.antarchy$setInheritedTimeDilationRate(rate);
        }
    }

    public static void applySyncedRate(Entity entity, double rate) {
        applySyncedRate(entity.getUUID(), rate);
        if (entity instanceof TimeDilationEntityAccess access) {
            access.antarchy$setTimeDilationRate(rate);
        }
    }

    public static void applySyncedRate(UUID entityUuid, double rate) {
        double clampedRate = TimeDilationMath.clampRate(rate);
        SYNCED_CLIENT_RATES.put(entityUuid, clampedRate);
    }

    public static void clearRate(Entity entity) {
        if (entity instanceof TimeDilationEntityAccess access) {
            boolean wasDilated = Math.abs(access.antarchy$getTimeDilationRate() - TimeDilationMath.NORMAL_RATE) > 0.001D;
            access.antarchy$setTimeDilationRate(TimeDilationMath.NORMAL_RATE);
            if (wasDilated) {
                syncEntityRate(entity, TimeDilationMath.NORMAL_RATE);
            }
        }
        SYNCED_CLIENT_RATES.remove(entity.getUUID());
    }

    public static void clearSyncedClientRates() {
        SYNCED_CLIENT_RATES.clear();
    }

    public static void syncEntityRate(Entity entity, double rate) {
        syncDispatcher.accept(entity, TimeDilationMath.clampRate(rate));
    }

    public static void syncFields(ServerPlayer player, List<TimeDilationFieldSnapshot> fields) {
        fieldSyncDispatcher.accept(player, fields);
    }

    public static boolean consumeTick(Entity entity, String timerKey) {
        return consumeTicks(entity, timerKey) > 0;
    }

    public static int consumeTicks(Entity entity, String timerKey) {
        double rate = getRate(entity);
        if (entity instanceof TimeDilationEntityAccess access) {
            return access.antarchy$consumeTimeDilationTicks(timerKey, rate);
        }
        return 1;
    }

    public static int scaleCooldownTicks(Entity entity, int ticks) {
        double rate = getRate(entity);
        if (ticks <= 0 || Math.abs(rate - TimeDilationMath.NORMAL_RATE) < 0.001D) {
            return ticks;
        }
        return (int) Math.ceil(ticks / Math.max(TimeDilationMath.MIN_RATE, rate));
    }
}
