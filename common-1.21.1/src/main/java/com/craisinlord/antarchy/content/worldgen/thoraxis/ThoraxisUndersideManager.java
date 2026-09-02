package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.DreamSandBlock;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ThoraxisUndersideManager {
    public static final int GRAVITY_FLIP_Y = 0;
    private static final ResourceLocation THORAXIS_DIMENSION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis");
    private static final AntarchyGravityTransition TRANSITION = new AntarchyGravityTransition(12);
    private static final int EFFECT_DURATION_TICKS = 50;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 10;
    private static final int DISCOVERY_INTERVAL_TICKS = 5;
    private static final int SIMULATION_DISTANCE_MARGIN_CHUNKS = 1;
    private static final int MAX_FLIPS_PER_TICK = 8;
    private static final int ENTER_UNDERSIDE_Y = GRAVITY_FLIP_Y - 4;
    private static final int EXIT_UNDERSIDE_Y = GRAVITY_FLIP_Y + 4;
    private static final int FLIP_COOLDOWN_TICKS = 40;
    private static final Map<ServerLevel, TrackingState> STATES = new WeakHashMap<>();

    private ThoraxisUndersideManager() {
    }

    public static void tick(ServerLevel level) {
        if (!isThoraxis(level)) {
            return;
        }

        TrackingState tracking = STATES.computeIfAbsent(level, ignored -> new TrackingState());
        if (level.players().isEmpty()) {
            tracking.clear();
            STATES.remove(level);
            return;
        }

        long now = level.getGameTime();
        if (now % DISCOVERY_INTERVAL_TICKS == 0L) {
            tracking.discover(level);
        }

        int flipsRemaining = MAX_FLIPS_PER_TICK;
        for (Entity entity : tracking.entities) {
            if (!entity.isAlive() || entity.isSpectator()) {
                continue;
            }

            if (entity instanceof LivingEntity living) {
                boolean hasEffect = living.hasEffect(AntarchyObjects.INVERTED_EFFECT.get());
                int threshold = hasEffect ? EXIT_UNDERSIDE_Y : ENTER_UNDERSIDE_Y;
                if (living.getY() < threshold) {
                    refreshInvertedEffect(living);
                }
                continue;
            }

            if (!(entity instanceof ItemEntity)) {
                continue;
            }

            UUID uuid = entity.getUUID();
            boolean tracked = tracking.forcedItems.contains(entity);
            boolean shouldInvert = tracked ? entity.getY() < EXIT_UNDERSIDE_Y : entity.getY() < ENTER_UNDERSIDE_Y;

            if (shouldInvert) {
                boolean needsFlip = AntarchyGravityApi.getGravityDirection(entity) != AntarchyGravityDirection.UP
                        || !AntarchyGravityApi.isGravityForced(entity);
                tracking.forcedItems.add(entity);
                if (needsFlip && (flipsRemaining <= 0 || !flipReady(tracking, uuid, now))) {
                    continue;
                }
                if (needsFlip) {
                    flipsRemaining--;
                    tracking.lastFlipTick.put(uuid, now);
                    AntarchyGravityApi.setForcedGravityDirection(entity, AntarchyGravityDirection.UP, TRANSITION);
                }
                continue;
            }

            if (tracked) {
                boolean isForcedUp = AntarchyGravityApi.getGravityDirection(entity) == AntarchyGravityDirection.UP
                        && AntarchyGravityApi.isGravityForced(entity);
                if (isForcedUp && !flipReady(tracking, uuid, now)) {
                    continue;
                }
                tracking.forcedItems.remove(entity);
                if (isForcedUp) {
                    tracking.lastFlipTick.put(uuid, now);
                    AntarchyGravityApi.setGravityDirection(entity, AntarchyGravityDirection.DOWN, false, TRANSITION);
                }
            }
        }

        tracking.entities.removeIf(entity -> !entity.isAlive() || entity.isRemoved());
        tracking.forcedItems.removeIf(entity -> !entity.isAlive() || entity.isRemoved());
        tracking.lastFlipTick.entrySet().removeIf(entry -> now - entry.getValue() > FLIP_COOLDOWN_TICKS * 4L);
    }

    private static final class TrackingState {
        private final Set<Entity> entities = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<Entity> forcedItems = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<UUID, Long> lastFlipTick = new java.util.HashMap<>();

        private void discover(ServerLevel level) {
            int radius = (level.getServer().getPlayerList().getSimulationDistance() + SIMULATION_DISTANCE_MARGIN_CHUNKS) * 16;
            for (ServerPlayer player : level.players()) {
                AABB area = new AABB(
                        player.getX() - radius, level.getMinBuildHeight(), player.getZ() - radius,
                        player.getX() + radius, level.getMaxBuildHeight(), player.getZ() + radius
                );
                entities.add(player);
                entities.addAll(level.getEntitiesOfClass(LivingEntity.class, area,
                        entity -> !entity.isSpectator() && entity.getY() < EXIT_UNDERSIDE_Y));
                entities.addAll(level.getEntitiesOfClass(ItemEntity.class, area,
                        entity -> !entity.isSpectator() && entity.getY() < EXIT_UNDERSIDE_Y));
            }
        }

        private void clear() {
            entities.clear();
            forcedItems.clear();
            lastFlipTick.clear();
        }
    }

    private static boolean flipReady(TrackingState tracking, UUID uuid, long now) {
        Long last = tracking.lastFlipTick.get(uuid);
        return last == null || now - last >= FLIP_COOLDOWN_TICKS;
    }

    public static void applyUndersideInversion(LivingEntity living) {
        refreshInvertedEffect(living);
    }

    private static void refreshInvertedEffect(LivingEntity living) {
        MobEffectInstance current = living.getEffect(AntarchyObjects.INVERTED_EFFECT.get());
        if (current == null || current.getDuration() <= EFFECT_REFRESH_THRESHOLD_TICKS) {
            living.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), EFFECT_DURATION_TICKS, 0, true, false, false));
        }
    }

    public static boolean shouldSpawnInvertedOnDreamSand(ServerLevelAccessor level, BlockPos pos) {
        if (!isThoraxis(level.getLevel()) || pos.getY() >= GRAVITY_FLIP_Y) {
            return false;
        }

        BlockState bodyState = level.getBlockState(pos);
        if (!bodyState.isAir() && !bodyState.getCollisionShape(level, pos).isEmpty()) {
            return false;
        }

        BlockPos supportPos = pos.above();
        BlockState supportState = level.getBlockState(supportPos);
        if (!(supportState.getBlock() instanceof DreamSandBlock)) {
            return false;
        }

        return supportState.isFaceSturdy(level, supportPos, Direction.DOWN)
                && level.getFluidState(pos).isEmpty()
                && level.isEmptyBlock(pos.below());
    }

    public static boolean isThoraxis(Level level) {
        return level.dimension().location().equals(THORAXIS_DIMENSION);
    }
}
