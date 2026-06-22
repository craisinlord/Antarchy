package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrutalflyCocoonFeature extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrutalflyCocoonFeature.class);
    private static final int MAX_ANCHOR_Y = 160;
    private static final int COCOON_CLEAR_RADIUS = 1;
    private static final int COCOON_CLEAR_MIN_Y_OFFSET = -16;
    private static final int COCOON_CLEAR_MAX_Y_OFFSET = -1;
    private static final int MIN_CANOPY_SUPPORTS = 4;

    public BrutalflyCocoonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ServerLevel serverLevel;
        if (level instanceof WorldGenRegion region) {
            serverLevel = region.getLevel();
        } else if (level instanceof ServerLevel sl) {
            serverLevel = sl;
        } else {
            return false;
        }

        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int chunkX = origin.getX() & ~15;
        int chunkZ = origin.getZ() & ~15;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int failNoLog = 0;
        int failClearBelow = 0;

        for (int attempt = 0; attempt < 12; attempt++) {
            int x = chunkX + 1 + random.nextInt(14);
            int z = chunkZ + 1 + random.nextInt(14);
            int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            if (groundY <= level.getMinBuildHeight() + 12) {
                continue;
            }

            int searchTopY = Math.min(MAX_ANCHOR_Y, level.getMaxBuildHeight() - 2);
            boolean foundLog = false;
            for (int y = searchTopY; y > groundY + 12; y--) {
                pos.set(x, y, z);
                if (!level.getBlockState(pos).is(BlockTags.LOGS)) {
                    continue;
                }

                foundLog = true;
                BlockPos anchor = pos.immutable();
                BlockPos blocker = findCocoonBlocker(level, anchor);
                if (blocker != null) {
                    failClearBelow++;
                    LOGGER.info(
                            "[BrutalflyCocoon] chamber failed at log ({},{},{}) blocked by {} ({}) at {}",
                            x,
                            y,
                            z,
                            level.getBlockState(blocker),
                            level.getBlockState(blocker).getBlock(),
                            blocker
                    );
                    continue;
                }

                if (spawnCocoonedBrutalfly(serverLevel, level, anchor, random)) {
                    LOGGER.info("[BrutalflyCocoon] SUCCESS at ({},{},{})", x, y, z);
                    return true;
                }
                return false;
            }

            if (!foundLog) {
                failNoLog++;
            }
        }

        LOGGER.info(
                "[BrutalflyCocoon] FAILED for chunk ({},{}): no-log={}, chamber={}",
                chunkX >> 4,
                chunkZ >> 4,
                failNoLog,
                failClearBelow
        );
        return false;
    }

    static int maxAnchorY() {
        return MAX_ANCHOR_Y;
    }

    static boolean canUseAnchor(WorldGenLevel level, BlockPos anchor) {
        return findCocoonBlocker(level, anchor) == null;
    }

    static boolean spawnCocoonedBrutalfly(ServerLevel serverLevel, WorldGenLevel level, BlockPos anchor, RandomSource random) {
        if (!canUseAnchor(level, anchor)) {
            return false;
        }

        clearCocoonVolume(level, anchor);
        BrutalflyEntity brutalfly = AntarchyObjects.BRUTALFLY.get().create(serverLevel);
        if (brutalfly == null) {
            return false;
        }

        brutalfly.moveTo(
                anchor.getX() + 0.5,
                anchor.getY() - 15.0,
                anchor.getZ() + 0.5,
                random.nextFloat() * 360.0F,
                0.0F
        );
        brutalfly.setCocooned(true, anchor);
        brutalfly.setHealth(brutalfly.getMaxHealth());
        serverLevel.addFreshEntity(brutalfly);
        return true;
    }

    static @org.jetbrains.annotations.Nullable BlockPos findCocoonBlocker(WorldGenLevel level, BlockPos anchor) {
        if (!hasCanopySupport(level, anchor)) {
            return anchor;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = COCOON_CLEAR_MIN_Y_OFFSET; dy <= COCOON_CLEAR_MAX_Y_OFFSET; dy++) {
            int worldY = anchor.getY() + dy;
            for (int dx = -COCOON_CLEAR_RADIUS; dx <= COCOON_CLEAR_RADIUS; dx++) {
                for (int dz = -COCOON_CLEAR_RADIUS; dz <= COCOON_CLEAR_RADIUS; dz++) {
                    if (dy == 0 && dx == 0 && dz == 0) {
                        continue;
                    }

                    pos.set(anchor.getX() + dx, worldY, anchor.getZ() + dz);
                    if (!isClearableForCocoon(level.getBlockState(pos))) {
                        return pos.immutable();
                    }
                }
            }
        }

        return null;
    }

    static void clearCocoonVolume(WorldGenLevel level, BlockPos anchor) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = COCOON_CLEAR_MIN_Y_OFFSET; dy <= COCOON_CLEAR_MAX_Y_OFFSET; dy++) {
            int worldY = anchor.getY() + dy;
            for (int dx = -COCOON_CLEAR_RADIUS; dx <= COCOON_CLEAR_RADIUS; dx++) {
                for (int dz = -COCOON_CLEAR_RADIUS; dz <= COCOON_CLEAR_RADIUS; dz++) {
                    if (dy == 0 && dx == 0 && dz == 0) {
                        continue;
                    }

                    pos.set(anchor.getX() + dx, worldY, anchor.getZ() + dz);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static boolean hasCanopySupport(WorldGenLevel level, BlockPos anchor) {
        int supports = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }

                BlockState neighbor = level.getBlockState(anchor.offset(dx, 0, dz));
                if (neighbor.is(BlockTags.LOGS) || neighbor.is(BlockTags.LEAVES)) {
                    supports++;
                }
            }
        }

        BlockState above = level.getBlockState(anchor.above());
        if (above.is(BlockTags.LOGS) || above.is(BlockTags.LEAVES)) {
            supports += 2;
        }

        return supports >= MIN_CANOPY_SUPPORTS;
    }

    private static boolean isClearableForCocoon(BlockState state) {
        return state.isAir()
                || state.canBeReplaced()
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || !state.getFluidState().isEmpty();
    }
}
