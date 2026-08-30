package com.craisinlord.antarchy.content.time;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class TimeDilationApi {
    private static BiConsumer<ServerPlayer, Double> syncDispatcher = (player, rate) -> {
    };

    private TimeDilationApi() {
    }

    public static void setSyncDispatcher(BiConsumer<ServerPlayer, Double> dispatcher) {
        syncDispatcher = dispatcher != null ? dispatcher : (player, rate) -> {
        };
    }

    public static TimeDilationFieldEntity createField(ServerLevel level, Vec3 center, double radius, double rate, int durationTicks) {
        TimeDilationFieldEntity field = TimeDilationFieldEntity.create(level, center, radius, rate, durationTicks);
        level.addFreshEntity(field);
        return field;
    }

    public static double getRate(Entity entity) {
        if (entity instanceof TimeDilationEntityAccess access) {
            return access.antarchy$getTimeDilationRate();
        }
        return TimeDilationMath.NORMAL_RATE;
    }

    public static boolean isDilated(Entity entity) {
        return getRate(entity) < TimeDilationMath.NORMAL_RATE;
    }

    public static void applySyncedRate(Entity entity, double rate) {
        if (entity instanceof TimeDilationEntityAccess access) {
            access.antarchy$setTimeDilationRate(rate);
        }
    }

    public static void syncPlayerRate(ServerPlayer player, double rate) {
        syncDispatcher.accept(player, TimeDilationMath.clampRate(rate));
    }

    public static boolean consumeTick(Entity entity, String timerKey) {
        if (entity instanceof Player) {
            return true;
        }
        double rate = getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE || !(entity instanceof TimeDilationEntityAccess access)) {
            return true;
        }
        return access.antarchy$consumeTimeDilationTick(timerKey, rate);
    }

    public static int scaleCooldownTicks(Entity entity, int ticks) {
        double rate = getRate(entity);
        if (ticks <= 0 || rate >= TimeDilationMath.NORMAL_RATE) {
            return ticks;
        }
        return (int) Math.ceil(ticks / Math.max(TimeDilationMath.MIN_RATE, rate));
    }
}
