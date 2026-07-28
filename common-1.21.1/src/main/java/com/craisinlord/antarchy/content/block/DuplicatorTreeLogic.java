package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class DuplicatorTreeLogic {
    private static final int SOURCE_RADIUS = 1;
    private static final int LOG_SEARCH_RADIUS = 2;
    private static final int LOG_SEARCH_BELOW = 5;
    private static final int LOG_SEARCH_ABOVE = 1;

    // Cache center log pos per chunk. WeakHashMap lets levels be GC'd when unloaded.
    private static final WeakHashMap<ServerLevel, HashMap<Long, BlockPos>> CENTER_LOG_CACHE = new WeakHashMap<>();

    private DuplicatorTreeLogic() {
    }

    public static void invalidateCacheNear(ServerLevel level, BlockPos pos) {
        HashMap<Long, BlockPos> map = CENTER_LOG_CACHE.get(level);
        if (map != null) {
            map.remove(ChunkPos.asLong(pos));
        }
    }

    public static void tryDuplicate(ServerLevel level, BlockPos origin, RandomSource random) {
        if (!AntarchySettings.duplicatorTreeEnabled()) {
            return;
        }

        if (random.nextInt(10) != 0) {
            return;
        }

        BlockPos centerLogPos = findCenterLog(level, origin);
        if (centerLogPos == null) {
            return;
        }

        List<BlockPos> sourceCandidates = collectSourceCandidates(level, centerLogPos);
        if (sourceCandidates.isEmpty()) {
            return;
        }

        List<BlockPos> targetCandidates = collectTargetCandidates(level, centerLogPos);
        if (targetCandidates.isEmpty()) {
            return;
        }

        BlockPos sourcePos = sourceCandidates.get(random.nextInt(sourceCandidates.size()));
        BlockState sourceState = level.getBlockState(sourcePos);
        BlockPos targetPos = targetCandidates.get(random.nextInt(targetCandidates.size()));

        BlockState placedState = Block.updateFromNeighbourShapes(sourceState, level, targetPos);
        if (!placedState.canSurvive(level, targetPos)) {
            return;
        }

        level.setBlock(targetPos, placedState, Block.UPDATE_ALL);
        level.levelEvent(2001, targetPos, Block.getId(sourceState));
        playDuplicateEffects(level, centerLogPos, sourcePos, targetPos);
    }

    private static boolean isValidSource(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.is(AntarchyTags.Blocks.DUPLICATOR_TREE_BLACKLIST) || AntarchyObjects.isDuplicatorTreeBlock(state)) {
            return false;
        }

        if (state.getBlock() instanceof EntityBlock || !state.getFluidState().isEmpty()) {
            return false;
        }

        if (state.is(AntarchyObjects.DUCT_TAPE.get())) {
            return true;
        }

        return state.isCollisionShapeFullBlock(level, pos);
    }

    private static boolean isValidTarget(ServerLevel level, BlockPos pos) {
        BlockState targetState = level.getBlockState(pos);
        if (!targetState.isAir() && !targetState.canBeReplaced()) {
            return false;
        }

        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private static List<BlockPos> collectSourceCandidates(ServerLevel level, BlockPos centerLogPos) {
        List<BlockPos> positions = new ArrayList<>();

        for (int x = -SOURCE_RADIUS; x <= SOURCE_RADIUS; x++) {
            for (int z = -SOURCE_RADIUS; z <= SOURCE_RADIUS; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                BlockPos checkPos = centerLogPos.offset(x, 0, z);
                BlockState checkState = level.getBlockState(checkPos);
                if (isValidSource(level, checkPos, checkState)) {
                    positions.add(checkPos);
                }
            }
        }

        return positions;
    }

    private static List<BlockPos> collectTargetCandidates(ServerLevel level, BlockPos centerLogPos) {
        List<BlockPos> positions = new ArrayList<>();

        for (int x = -SOURCE_RADIUS; x <= SOURCE_RADIUS; x++) {
            for (int z = -SOURCE_RADIUS; z <= SOURCE_RADIUS; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                BlockPos checkPos = centerLogPos.offset(x, 0, z);
                if (isValidTarget(level, checkPos)) {
                    positions.add(checkPos);
                }
            }
        }

        return positions;
    }

    private static BlockPos findCenterLog(ServerLevel level, BlockPos origin) {
        if (level.getBlockState(origin).is(AntarchyObjects.DUPLICATOR_LOG.get())) {
            return descendToBaseLog(level, origin);
        }

        long chunkKey = ChunkPos.asLong(origin);
        HashMap<Long, BlockPos> cache = CENTER_LOG_CACHE.computeIfAbsent(level, k -> new HashMap<>());
        BlockPos cached = cache.get(chunkKey);
        if (cached != null) {
            if (level.getBlockState(cached).is(AntarchyObjects.DUPLICATOR_LOG.get())) {
                return cached;
            }
            cache.remove(chunkKey);
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos bestPos = null;
        int bestDistance = Integer.MAX_VALUE;

        for (int y = -LOG_SEARCH_BELOW; y <= LOG_SEARCH_ABOVE; y++) {
            for (int x = -LOG_SEARCH_RADIUS; x <= LOG_SEARCH_RADIUS; x++) {
                for (int z = -LOG_SEARCH_RADIUS; z <= LOG_SEARCH_RADIUS; z++) {
                    cursor.setWithOffset(origin, x, y, z);
                    if (!level.getBlockState(cursor).is(AntarchyObjects.DUPLICATOR_LOG.get())) {
                        continue;
                    }

                    int distance = Math.abs(x) + Math.abs(y) + Math.abs(z);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestPos = cursor.immutable();
                    }
                }
            }
        }

        BlockPos result = bestPos == null ? null : descendToBaseLog(level, bestPos);
        if (result != null) {
            cache.put(chunkKey, result);
        }
        return result;
    }

    private static BlockPos descendToBaseLog(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (level.getBlockState(cursor.below()).is(AntarchyObjects.DUPLICATOR_LOG.get())) {
            cursor.move(Direction.DOWN);
        }

        return cursor.immutable();
    }

    private static void playDuplicateEffects(ServerLevel level, BlockPos centerLogPos, BlockPos sourcePos, BlockPos targetPos) {
        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                centerLogPos.getX() + 0.5D,
                centerLogPos.getY() + 0.9D,
                centerLogPos.getZ() + 0.5D,
                4,
                0.35D,
                0.35D,
                0.35D,
                0.02D
        );
        level.sendParticles(
                ParticleTypes.ENCHANT,
                sourcePos.getX() + 0.5D,
                sourcePos.getY() + 0.5D,
                sourcePos.getZ() + 0.5D,
                5,
                0.15D,
                0.15D,
                0.15D,
                0.04D
        );
        level.sendParticles(
                ParticleTypes.WAX_ON,
                targetPos.getX() + 0.5D,
                targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D,
                6,
                0.25D,
                0.25D,
                0.25D,
                0.01D
        );

        level.playSound(null, centerLogPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7F, 1.25F);
        level.playSound(null, targetPos, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.BLOCKS, 0.55F, 0.9F + level.random.nextFloat() * 0.2F);
    }
}
