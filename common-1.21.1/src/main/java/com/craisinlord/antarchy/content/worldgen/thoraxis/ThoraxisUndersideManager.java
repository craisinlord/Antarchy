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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ThoraxisUndersideManager {
    public static final int GRAVITY_FLIP_Y = 0;
    private static final net.minecraft.resources.ResourceLocation THORAXIS_DIMENSION = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis");
    private static final AntarchyGravityTransition TRANSITION = new AntarchyGravityTransition(12);
    private static final AntarchyGravityTransition THROAT_TRANSITION = new AntarchyGravityTransition(20);
    private static final int EFFECT_DURATION_TICKS = 50;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 10;
    private static final int DISCOVERY_INTERVAL_TICKS = 5;
    private static final int SIMULATION_DISTANCE_MARGIN_CHUNKS = 1;
    private static final int MAX_FLIPS_PER_TICK = 8;
    private static final double SLOW_TICK_WARN_MS = 25.0D;
    private static final long DIAGNOSTIC_INTERVAL_TICKS = 100L;
    private static final int ENTER_UNDERSIDE_Y = GRAVITY_FLIP_Y - 4;
    private static final int EXIT_UNDERSIDE_Y = GRAVITY_FLIP_Y + 4;
    private static final int FLIP_COOLDOWN_TICKS = 40;
    private static final int CROSSING_TIMEOUT_TICKS = 30;
    private static final int SETTLE_TIMEOUT_TICKS = 70;
    private static final double MIN_CROSSING_SPEED = 0.48D;
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

        long startNanos = System.nanoTime();
        long now = level.getGameTime();
        tickPlayerTransitions(level, tracking, now);
        int discoveredLiving = 0;
        int discoveredItems = 0;
        if (now % DISCOVERY_INTERVAL_TICKS == 0L) {
            DiscoveryStats stats = tracking.discover(level);
            discoveredLiving = stats.livingEntities();
            discoveredItems = stats.itemEntities();
        }

        int flipsRemaining = MAX_FLIPS_PER_TICK;
        int refreshedLiving = 0;
        int flippedItems = 0;
        int restoredItems = 0;
        Set<Entity> evicted = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Entity entity : tracking.entities) {
            if (!entity.isAlive() || entity.isSpectator()) {
                continue;
            }

            if (entity instanceof LivingEntity living) {
                if (living instanceof Player) {
                    continue;
                }
                boolean hasEffect = living.hasEffect(AntarchyObjects.INVERTED_EFFECT.get());
                int threshold = hasEffect ? EXIT_UNDERSIDE_Y : ENTER_UNDERSIDE_Y;
                if (living.getY() < threshold) {
                    refreshInvertedEffect(living);
                    refreshedLiving++;
                } else if (!hasEffect) {
                    evicted.add(entity);
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
                    flippedItems++;
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
                    restoredItems++;
                }
                evicted.add(entity);
            }
        }

        tracking.entities.removeAll(evicted);
        tracking.forcedItems.removeAll(evicted);
        tracking.entities.removeIf(entity -> !entity.isAlive() || entity.isRemoved());
        tracking.forcedItems.removeIf(entity -> !entity.isAlive() || entity.isRemoved());
        tracking.lastFlipTick.entrySet().removeIf(entry -> now - entry.getValue() > FLIP_COOLDOWN_TICKS * 4L);

        double elapsedMs = (System.nanoTime() - startNanos) / 1_000_000.0D;
        if (elapsedMs >= SLOW_TICK_WARN_MS && now - tracking.lastDiagnosticTick >= DIAGNOSTIC_INTERVAL_TICKS) {
            tracking.lastDiagnosticTick = now;
            Antarchy.LOGGER.warn(
                    "[antarchy-thoraxis] slow underside tick dim={} gameTime={} elapsedMs={} trackedEntities={} forcedItems={} discoveredLiving={} discoveredItems={} refreshedLiving={} flippedItems={} restoredItems={} players={}",
                    level.dimension().location(), now, elapsedMs, tracking.entities.size(), tracking.forcedItems.size(),
                    discoveredLiving, discoveredItems, refreshedLiving, flippedItems, restoredItems, level.players().size()
            );
        }
    }

    private static final class TrackingState {
        private final Set<Entity> entities = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<Entity> forcedItems = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<UUID, Long> lastFlipTick = new java.util.HashMap<>();
        private final Map<UUID, PlayerCrossing> playerCrossings = new java.util.HashMap<>();
        private long lastDiagnosticTick = Long.MIN_VALUE / 2L;

        private DiscoveryStats discover(ServerLevel level) {
            int radius = (level.getServer().getPlayerList().getSimulationDistance() + SIMULATION_DISTANCE_MARGIN_CHUNKS) * 16;
            int discoveredLiving = 0;
            int discoveredItems = 0;
            for (ServerPlayer player : level.players()) {
                AABB area = new AABB(
                        player.getX() - radius, level.getMinBuildHeight(), player.getZ() - radius,
                        player.getX() + radius, level.getMaxBuildHeight(), player.getZ() + radius
                );
                entities.add(player);
                java.util.List<LivingEntity> livingEntities = level.getEntitiesOfClass(LivingEntity.class, area,
                        entity -> !entity.isSpectator() && entity.getY() < EXIT_UNDERSIDE_Y);
                java.util.List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, area,
                        entity -> !entity.isSpectator() && entity.getY() < EXIT_UNDERSIDE_Y);
                discoveredLiving += livingEntities.size();
                discoveredItems += itemEntities.size();
                entities.addAll(livingEntities);
                entities.addAll(itemEntities);
            }
            return new DiscoveryStats(discoveredLiving, discoveredItems);
        }

        private void clear() {
            entities.clear();
            forcedItems.clear();
            lastFlipTick.clear();
            playerCrossings.clear();
        }
    }

    private static void tickPlayerTransitions(ServerLevel level, TrackingState tracking, long now) {
        tracking.playerCrossings.keySet().removeIf(uuid -> level.getPlayerByUUID(uuid) == null);
        for (ServerPlayer player : level.players()) {
            PlayerCrossing crossing = tracking.playerCrossings.get(player.getUUID());
            if (crossing != null) {
                tickPlayerCrossing(player, crossing, tracking, now);
                continue;
            }

            if (player.isSpectator()) {
                continue;
            }

            if (isCrossingGravityBoundary(player) && flipReady(tracking, player.getUUID(), now)) {
                boolean toUnderside = AntarchyGravityApi.getGravityDirection(player) == AntarchyGravityDirection.DOWN;
                if (!toUnderside) {
                    refreshInvertedEffect(player);
                }
                tracking.playerCrossings.put(player.getUUID(), new PlayerCrossing(toUnderside));
                continue;
            }

            MobEffectInstance effect = player.getEffect(AntarchyObjects.INVERTED_EFFECT.get());
            if (player.getY() < ENTER_UNDERSIDE_Y) {
                refreshInvertedEffect(player);
            } else if (effect != null && isUndersideEffect(effect) && player.getY() < EXIT_UNDERSIDE_Y) {
                refreshInvertedEffect(player);
            }
        }
    }

    private static void tickPlayerCrossing(ServerPlayer player, PlayerCrossing crossing, TrackingState tracking, long now) {
        crossing.ticks++;
        Vec3 velocity = AntarchyGravityApi.getWorldVelocity(player);
        if (!crossing.flipped) {
            if (!crossing.toUnderside) {
                refreshInvertedEffect(player);
            }
            if (crossing.ticks > CROSSING_TIMEOUT_TICKS
                    || (crossing.toUnderside && velocity.y > 0.08D)
                    || (!crossing.toUnderside && velocity.y < -0.08D)) {
                tracking.playerCrossings.remove(player.getUUID());
                return;
            }

            AABB bounds = player.getBoundingBox();
            boolean clear = crossing.toUnderside ? bounds.maxY < GRAVITY_FLIP_Y : bounds.minY > EXIT_UNDERSIDE_Y;
            if (!clear) {
                return;
            }

            AntarchyGravityDirection destination = crossing.toUnderside
                    ? AntarchyGravityDirection.UP
                    : AntarchyGravityDirection.DOWN;
            AntarchyGravityApi.setAirborneGravityDirection(player, destination, crossing.toUnderside, THROAT_TRANSITION);
            player.resetFallDistance();
            crossing.flipped = true;
            crossing.ticks = 0;
            tracking.lastFlipTick.put(player.getUUID(), now);
            if (crossing.toUnderside) {
                refreshInvertedEffect(player);
            } else {
                player.removeEffect(AntarchyObjects.INVERTED_EFFECT.get());
            }
            return;
        }

        if (crossing.toUnderside) {
            refreshInvertedEffect(player);
        }
        if (player.onGround() || crossing.ticks >= SETTLE_TIMEOUT_TICKS) {
            tracking.playerCrossings.remove(player.getUUID());
        }
    }

    private static boolean isCrossingGravityBoundary(ServerPlayer player) {
        Vec3 velocity = AntarchyGravityApi.getWorldVelocity(player);
        AABB swept = player.getBoundingBox().expandTowards(velocity.scale(-1.0D)).inflate(0.08D);
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(player);
        return direction == AntarchyGravityDirection.DOWN
                ? swept.minY < GRAVITY_FLIP_Y && swept.maxY >= GRAVITY_FLIP_Y && velocity.y <= -MIN_CROSSING_SPEED
                : swept.minY <= GRAVITY_FLIP_Y && swept.maxY > GRAVITY_FLIP_Y && velocity.y >= MIN_CROSSING_SPEED;
    }

    private static boolean isUndersideEffect(MobEffectInstance effect) {
        return effect.isAmbient() && !effect.isVisible();
    }

    private static final class PlayerCrossing {
        private final boolean toUnderside;
        private int ticks;
        private boolean flipped;

        private PlayerCrossing(boolean toUnderside) {
            this.toUnderside = toUnderside;
        }
    }

    private static boolean flipReady(TrackingState tracking, UUID uuid, long now) {
        Long last = tracking.lastFlipTick.get(uuid);
        return last == null || now - last >= FLIP_COOLDOWN_TICKS;
    }

    private record DiscoveryStats(int livingEntities, int itemEntities) {
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

    /** True only while an entity is physically below Thoraxis' gravity boundary. */
    public static boolean isInUnderside(Entity entity) {
        return isThoraxis(entity.level()) && entity.getY() < GRAVITY_FLIP_Y;
    }

    /**
     * Projectiles perform their own movement in world coordinates, so they must
     * not retain an inverted entity-local movement frame. Underside gravity is
     * reversed separately when gravitational acceleration is applied.
     */
    public static void normalizeProjectileGravityFrame(Projectile projectile) {
        boolean stateMismatch = AntarchyGravityApi.getGravityDirection(projectile) != AntarchyGravityDirection.DOWN
                || AntarchyGravityApi.isGravityForced(projectile);
        if (stateMismatch) {
            AntarchyGravityApi.setAirborneGravityDirection(
                    projectile,
                    AntarchyGravityDirection.DOWN,
                    false,
                    AntarchyGravityTransition.INSTANT
            );
        }
    }

    public static boolean isAboveUndersideExit(Entity entity) {
        return isThoraxis(entity.level()) && entity.getY() >= EXIT_UNDERSIDE_Y;
    }

    /** True while an entity is in the region where the underside gravity rule applies. */
    public static boolean shouldInvertInUnderside(Entity entity) {
        return AntarchyGravityApi.isGravityInverted(entity)
                && isInUnderside(entity);
    }
}
