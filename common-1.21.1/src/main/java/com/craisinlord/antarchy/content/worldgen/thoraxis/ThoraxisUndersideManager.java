package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.DreamSandBlock;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import java.util.HashSet;
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
    private static final int EFFECT_DURATION_TICKS = 40;
    private static final int MAX_FLIPS_PER_TICK = 8;
    private static final Set<UUID> UNDERSIDE_APPLIED_EFFECT = new HashSet<>();
    private static final Set<UUID> UNDERSIDE_FORCED_GRAVITY = new HashSet<>();
    private static final Set<UUID> ACTIVE_ITEMS_SCRATCH = new HashSet<>();

    private ThoraxisUndersideManager() {
    }

    public static void tick(ServerLevel level) {
        if (!isThoraxis(level) || level.players().isEmpty()) {
            return;
        }

        Set<UUID> activeThisTick = ACTIVE_ITEMS_SCRATCH;
        activeThisTick.clear();
        int flipsRemaining = MAX_FLIPS_PER_TICK;
        for (Entity entity : level.getAllEntities()) {
            if (!entity.isAlive() || entity.isSpectator()) {
                continue;
            }

            boolean inUnderside = entity.getY() < GRAVITY_FLIP_Y;

            if (entity instanceof LivingEntity living) {
                UUID uuid = entity.getUUID();
                if (inUnderside) {
                    boolean needsFlip = AntarchyGravityApi.getGravityDirection(living) != AntarchyGravityDirection.UP
                            || !AntarchyGravityApi.isGravityForced(living);
                    if (needsFlip && flipsRemaining <= 0) {
                        continue;
                    }
                    if (needsFlip) {
                        flipsRemaining--;
                    }
                    applyUndersideInversion(living);
                } else if (UNDERSIDE_APPLIED_EFFECT.remove(uuid)) {
                    if (living.hasEffect(AntarchyObjects.INVERTED_EFFECT.get())) {
                        living.removeEffect(AntarchyObjects.INVERTED_EFFECT.get());
                    }
                    if (AntarchyGravityApi.getGravityDirection(living) == AntarchyGravityDirection.UP
                            && AntarchyGravityApi.isGravityForced(living)) {
                        AntarchyGravityApi.setGravityDirection(living, AntarchyGravityDirection.DOWN, false, TRANSITION);
                    }
                }
                continue;
            }

            if (!(entity instanceof ItemEntity)) {
                continue;
            }

            UUID uuid = entity.getUUID();
            if (inUnderside) {
                boolean needsFlip = AntarchyGravityApi.getGravityDirection(entity) != AntarchyGravityDirection.UP
                        || !AntarchyGravityApi.isGravityForced(entity);
                if (needsFlip && flipsRemaining <= 0) {
                    continue;
                }
                activeThisTick.add(uuid);
                UNDERSIDE_FORCED_GRAVITY.add(uuid);
                if (needsFlip) {
                    flipsRemaining--;
                    AntarchyGravityApi.setForcedGravityDirection(entity, AntarchyGravityDirection.UP, TRANSITION);
                }
                continue;
            }

            if (UNDERSIDE_FORCED_GRAVITY.remove(uuid)
                    && AntarchyGravityApi.getGravityDirection(entity) == AntarchyGravityDirection.UP
                    && AntarchyGravityApi.isGravityForced(entity)) {
                AntarchyGravityApi.setGravityDirection(entity, AntarchyGravityDirection.DOWN, false, TRANSITION);
            }
        }

        UNDERSIDE_FORCED_GRAVITY.retainAll(activeThisTick);
    }

    public static void applyUndersideInversion(LivingEntity living) {
        UNDERSIDE_APPLIED_EFFECT.add(living.getUUID());
        if (!living.hasEffect(AntarchyObjects.INVERTED_EFFECT.get())) {
            living.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), EFFECT_DURATION_TICKS, 0, true, false, false));
        }
        if (AntarchyGravityApi.getGravityDirection(living) != AntarchyGravityDirection.UP
                || !AntarchyGravityApi.isGravityForced(living)) {
            AntarchyGravityApi.setForcedGravityDirection(living, AntarchyGravityDirection.UP, TRANSITION);
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
