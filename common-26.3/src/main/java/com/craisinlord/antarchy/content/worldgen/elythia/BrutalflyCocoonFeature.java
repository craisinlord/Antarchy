package com.craisinlord.antarchy.content.worldgen.elythia;

import net.minecraft.world.level.chunk.ChunkGenerator;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public class BrutalflyCocoonFeature implements Feature {
    public static final MapCodec<BrutalflyCocoonFeature> CODEC = MapCodec.unit(BrutalflyCocoonFeature::new);

    @Override
    public MapCodec<BrutalflyCocoonFeature> codec() { return CODEC; }
    private static final int MAX_ANCHOR_Y = 160;
    private static final int COCOON_CLEAR_RADIUS = 1;
    private static final int COCOON_CLEAR_MIN_Y_OFFSET = -16;
    private static final int COCOON_CLEAR_MAX_Y_OFFSET = -1;
    private static final int MIN_CANOPY_SUPPORTS = 4;


    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int chunkX = origin.getX() & ~15;
        int chunkZ = origin.getZ() & ~15;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < 12; attempt++) {
            int x = chunkX + 1 + random.nextInt(14);
            int z = chunkZ + 1 + random.nextInt(14);
            int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            if (groundY <= level.getMinY() + 12) {
                continue;
            }

            int searchTopY = Math.min(MAX_ANCHOR_Y, (level.getMaxY() + 1) - 2);
            for (int y = searchTopY; y > groundY + 12; y--) {
                pos.set(x, y, z);
                if (!level.getBlockState(pos).is(BlockTags.LOGS)) {
                    continue;
                }

                BlockPos anchor = pos.immutable();
                if (findCocoonBlocker(level, anchor) != null) {
                    continue;
                }

                return placeCocoonSpawner(level, anchor);
            }
        }

        return false;
    }

    static int maxAnchorY() {
        return MAX_ANCHOR_Y;
    }

    static boolean canUseAnchor(WorldGenLevel level, BlockPos anchor) {
        return findCocoonBlocker(level, anchor) == null;
    }

    static boolean placeCocoonSpawner(WorldGenLevel level, BlockPos anchor) {
        if (!canUseAnchor(level, anchor)) {
            return false;
        }

        clearCocoonVolume(level, anchor);
        setCocoonSpawnerMarker(level, anchor);
        return true;
    }

    static void setCocoonSpawnerMarker(WorldGenLevel level, BlockPos anchor) {
        Block marker = AntarchyObjects.BRUTALFLY_COCOON_SPAWNER.get();
        BlockPos markerPos = anchor.below();
        level.setBlock(markerPos, marker.defaultBlockState(), 19);
        level.scheduleTick(markerPos, marker, 20);
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
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 19);
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
