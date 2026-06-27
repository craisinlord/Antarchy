package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;

public final class CavarynBileCystFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation BILE_VEIN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bile_vein");
    private static final ResourceLocation BILE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bile");
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_ATTEMPTS = 20;
    private static final int VERTICAL_SCAN = 10;
    // Chance that each exposed vein face oozes bile liquid outward into the cave
    private static final float OOZE_CHANCE = 0.22f;

    public CavarynBileCystFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Block bileVeinBlock = getBlock(BILE_VEIN_ID);
        Block bileBlock = getBlock(BILE_ID);
        if (bileVeinBlock == null || !(bileBlock instanceof LiquidBlock)) {
            return false;
        }

        WorldGenLevel level = context.level();
        RandomSource random = context.random();

        BlockPos center = findCenter(level, context.origin(), random);
        if (center == null) {
            return false;
        }

        return placeCyst(level, center, bileVeinBlock, bileBlock, random);
    }

    // Find a solid block adjacent to cave air, then step 1-2 blocks deeper into the wall
    // so the cyst is embedded rather than sitting right on the surface.
    private static BlockPos findCenter(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int y = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            y = Mth.clamp(y, level.getMinBuildHeight() + 6, level.getMaxBuildHeight() - 6);
            mutable.set(x, y, z);

            if (!isSolid(level.getBlockState(mutable))) {
                continue;
            }

            // Need at least one open neighbor (at cave surface)
            Direction openDir = null;
            for (Direction dir : Direction.values()) {
                if (level.getBlockState(mutable.relative(dir)).canBeReplaced()) {
                    openDir = dir;
                    break;
                }
            }
            if (openDir == null) {
                continue;
            }

            // Step 1-2 blocks away from the opening so the cyst sits inside the wall
            int depth = 1 + random.nextInt(2);
            BlockPos candidate = mutable.immutable().relative(openDir.getOpposite(), depth);
            if (isSolid(level.getBlockState(candidate))) {
                return candidate;
            }
        }
        return null;
    }

    private boolean placeCyst(WorldGenLevel level, BlockPos center, Block veinBlock, Block bileBlock, RandomSource random) {
        // Per-axis radii: 1.5–13.5 so the cyst diameter ranges roughly 3–27
        double rx = 1.5 + random.nextDouble() * 12.0;
        double ry = 1.5 + random.nextDouble() * 12.0;
        double rz = 1.5 + random.nextDouble() * 12.0;

        // Random stretch: pick one axis and scale it up 1.3–2x for elongated cysts
        int stretchAxis = random.nextInt(3);
        if (stretchAxis == 0) rx *= 1.3 + random.nextDouble() * 0.7;
        else if (stretchAxis == 1) ry *= 1.3 + random.nextDouble() * 0.7;
        else rz *= 1.3 + random.nextDouble() * 0.7;

        // Independent sine frequencies per wobble term — drives the lumpy distortion
        double f1x = 0.6 + random.nextDouble() * 1.2;
        double f1z = 0.6 + random.nextDouble() * 1.2;
        double f2y = 0.7 + random.nextDouble() * 1.0;
        double f2x = 0.5 + random.nextDouble() * 0.9;
        double f3z = 0.8 + random.nextDouble() * 1.1;
        double f3y = 0.5 + random.nextDouble() * 0.8;
        double wobbleAmp = 0.15 + random.nextDouble() * 0.2; // surface roughness

        int maxR = (int) Math.ceil(Math.max(Math.max(rx, ry), rz)) + 2;
        List<BlockPos> placed = new ArrayList<>();

        for (int dx = -maxR; dx <= maxR; dx++) {
            for (int dy = -maxR; dy <= maxR; dy++) {
                for (int dz = -maxR; dz <= maxR; dz++) {
                    double baseDist = Math.sqrt(
                            (dx * dx) / (rx * rx) +
                            (dy * dy) / (ry * ry) +
                            (dz * dz) / (rz * rz));

                    // Multi-frequency sine interference — gives lumpy, non-spherical surface
                    double wobble = wobbleAmp * (
                            0.4 * Math.sin(dx * f1x + dz * f1z) +
                            0.35 * Math.cos(dy * f2y + dx * f2x) +
                            0.25 * Math.sin(dz * f3z + dy * f3y));

                    double distorted = baseDist + wobble;
                    if (distorted > 1.0) {
                        continue;
                    }

                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState existing = level.getBlockState(pos);
                    if (!isSolid(existing)) {
                        continue;
                    }

                    if (distorted < 0.7) {
                        // Interior — fill with bile liquid
                        BlockState bileState = bileBlock.defaultBlockState();
                        level.setBlock(pos, bileState, 2);
                        scheduleFluidTick(level, pos, bileState);
                    } else {
                        // Outer shell — bile vein blocks
                        level.setBlock(pos, veinBlock.defaultBlockState(), 2);
                        placed.add(pos.immutable());
                    }
                }
            }
        }

        if (placed.isEmpty()) {
            return false;
        }

        // 30% chance this cyst oozes at all
        if (random.nextFloat() >= 0.30f) {
            return true;
        }

        // Ooze bile out of exposed faces — "popped pimple" seep
        for (BlockPos cystPos : placed) {
            for (Direction dir : Direction.values()) {
                BlockPos adj = cystPos.relative(dir);
                BlockState adjState = level.getBlockState(adj);
                if ((adjState.isAir() || adjState.canBeReplaced()) && random.nextFloat() < OOZE_CHANCE) {
                    BlockState bileState = bileBlock.defaultBlockState();
                    level.setBlock(adj, bileState, 2);
                    scheduleFluidTick(level, adj, bileState);
                }
            }
        }

        return true;
    }

    private static boolean isSolid(BlockState state) {
        return state.blocksMotion() && !state.is(Blocks.BEDROCK) && state.getFluidState().isEmpty();
    }

    private static void scheduleFluidTick(WorldGenLevel level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
