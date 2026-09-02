package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.DreamSandBlock;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ThoraxisUndersideManager {
    public static final int GRAVITY_FLIP_Y = 0;
    private static final ResourceLocation THORAXIS_DIMENSION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis");
    private static final AntarchyGravityTransition TRANSITION = new AntarchyGravityTransition(12);
    private static final int EFFECT_DURATION_TICKS = 50;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 10;
    private static final int MAX_FLIPS_PER_TICK = 8;
    private static final int ENTER_UNDERSIDE_Y = GRAVITY_FLIP_Y - 4;
    private static final int EXIT_UNDERSIDE_Y = GRAVITY_FLIP_Y + 4;
    private static final int FLIP_COOLDOWN_TICKS = 40;
    private static final Set<UUID> UNDERSIDE_FORCED_GRAVITY = new HashSet<>();
    private static final Set<UUID> ACTIVE_ITEMS_SCRATCH = new HashSet<>();
    private static final Map<UUID, Long> LAST_FLIP_TICK = new HashMap<>();

    private ThoraxisUndersideManager() {
    }

    public static void tick(ServerLevel level) {
        if (!isThoraxis(level) || level.players().isEmpty()) {
            return;
        }

        long now = level.getGameTime();
        Set<UUID> activeThisTick = ACTIVE_ITEMS_SCRATCH;
        activeThisTick.clear();
        int flipsRemaining = MAX_FLIPS_PER_TICK;
        for (Entity entity : level.getAllEntities()) {
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
            boolean tracked = UNDERSIDE_FORCED_GRAVITY.contains(uuid);
            boolean shouldInvert = tracked ? entity.getY() < EXIT_UNDERSIDE_Y : entity.getY() < ENTER_UNDERSIDE_Y;

            if (shouldInvert) {
                boolean needsFlip = AntarchyGravityApi.getGravityDirection(entity) != AntarchyGravityDirection.UP
                        || !AntarchyGravityApi.isGravityForced(entity);
                activeThisTick.add(uuid);
                UNDERSIDE_FORCED_GRAVITY.add(uuid);
                if (needsFlip && (flipsRemaining <= 0 || !flipReady(uuid, now))) {
                    continue;
                }
                if (needsFlip) {
                    flipsRemaining--;
                    LAST_FLIP_TICK.put(uuid, now);
                    AntarchyGravityApi.setForcedGravityDirection(entity, AntarchyGravityDirection.UP, TRANSITION);
                }
                continue;
            }

            if (tracked) {
                boolean isForcedUp = AntarchyGravityApi.getGravityDirection(entity) == AntarchyGravityDirection.UP
                        && AntarchyGravityApi.isGravityForced(entity);
                if (isForcedUp && !flipReady(uuid, now)) {
                    activeThisTick.add(uuid);
                    continue;
                }
                UNDERSIDE_FORCED_GRAVITY.remove(uuid);
                if (isForcedUp) {
                    LAST_FLIP_TICK.put(uuid, now);
                    AntarchyGravityApi.setGravityDirection(entity, AntarchyGravityDirection.DOWN, false, TRANSITION);
                }
            }
        }

        UNDERSIDE_FORCED_GRAVITY.retainAll(activeThisTick);
        LAST_FLIP_TICK.values().removeIf(t -> now - t > FLIP_COOLDOWN_TICKS * 4L);
    }

    private static boolean flipReady(UUID uuid, long now) {
        Long last = LAST_FLIP_TICK.get(uuid);
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
