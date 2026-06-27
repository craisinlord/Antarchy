package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;

import java.util.*;

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

public final class CavarynBileVeinFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation BILE_VEIN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bile_vein");
    private static final ResourceLocation BILE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bile");
    private static final int SEARCH_RADIUS = 10;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int VERTICAL_SCAN = 12;
    private static final float BRANCH_CHANCE = 0.12f;
    private static final float LIQUID_POCKET_CHANCE = 0.04f;

    public CavarynBileVeinFeature(Codec<NoneFeatureConfiguration> codec) {
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

        BlockPos start = findStart(level, context.origin(), random);
        if (start == null) {
            return false;
        }

        return growVeinNetwork(level, start, bileVeinBlock, bileBlock, random);
    }

    // Find a solid block that sits at a cave boundary
    private static BlockPos findStart(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int y = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            y = Mth.clamp(y, level.getMinBuildHeight() + 2, level.getMaxBuildHeight() - 3);
            mutable.set(x, y, z);

            if (!isVeinCandidate(level.getBlockState(mutable))) {
                continue;
            }

            for (Direction dir : Direction.values()) {
                if (level.getBlockState(mutable.relative(dir)).canBeReplaced()) {
                    return mutable.immutable();
                }
            }
        }
        return null;
    }

    private boolean growVeinNetwork(WorldGenLevel level, BlockPos start, Block veinBlock, Block bileBlock, RandomSource random) {
        Set<BlockPos> visited = new HashSet<>();
        List<BranchStart> pendingBranches = new ArrayList<>();
        boolean placedAny = false;

        Direction mainDir = Direction.values()[random.nextInt(6)];
        placedAny |= growArm(level, start, mainDir, 45 + random.nextInt(25), visited, pendingBranches, veinBlock, bileBlock, random);

        for (BranchStart branch : pendingBranches) {
            Direction branchDir = perpendiculars(branch.dir())[random.nextInt(4)];
            int branchLen = 15 + random.nextInt(20);
            growArm(level, branch.pos(), branchDir, branchLen, visited, null, veinBlock, bileBlock, random);
        }

        return placedAny;
    }

    private boolean growArm(
            WorldGenLevel level,
            BlockPos start,
            Direction initialDir,
            int maxSteps,
            Set<BlockPos> visited,
            List<BranchStart> branches,
            Block veinBlock,
            Block bileBlock,
            RandomSource random
    ) {
        BlockPos current = start;
        Direction dir = initialDir;
        boolean placedAny = false;

        for (int step = 0; step < maxSteps; step++) {
            if (visited.contains(current)) {
                current = current.relative(biasedNextDir(dir, random));
                continue;
            }

            BlockState state = level.getBlockState(current);
            if (!isVeinCandidate(state)) {
                // Try to redirect
                BlockPos redirect = findSolidNeighbor(current, dir, visited, level, random);
                if (redirect == null) {
                    break;
                }
                current = redirect;
                state = level.getBlockState(current);
                if (!isVeinCandidate(state)) {
                    break;
                }
            }

            visited.add(current);
            level.setBlock(current, veinBlock.defaultBlockState(), 2);
            placedAny = true;

            if (random.nextFloat() < LIQUID_POCKET_CHANCE) {
                tryPlaceLiquidPocket(level, current, bileBlock, visited, random);
            }

            if (branches != null && random.nextFloat() < BRANCH_CHANCE) {
                Direction[] perps = perpendiculars(dir);
                branches.add(new BranchStart(current, perps[random.nextInt(perps.length)]));
            }

            dir = biasedNextDir(dir, random);
            current = current.relative(dir);
        }

        return placedAny;
    }

    private static Direction biasedNextDir(Direction current, RandomSource random) {
        int roll = random.nextInt(10);
        if (roll < 6) {
            return current;
        }
        return perpendiculars(current)[roll - 6];
    }

    private static BlockPos findSolidNeighbor(BlockPos from, Direction preferredDir, Set<BlockPos> visited, WorldGenLevel level, RandomSource random) {
        Direction[] perps = perpendiculars(preferredDir);
        for (int i = perps.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction tmp = perps[i];
            perps[i] = perps[j];
            perps[j] = tmp;
        }
        for (Direction d : perps) {
            BlockPos candidate = from.relative(d);
            if (!visited.contains(candidate) && isVeinCandidate(level.getBlockState(candidate))) {
                return candidate;
            }
        }
        return null;
    }

    private static void tryPlaceLiquidPocket(WorldGenLevel level, BlockPos veinPos, Block bileBlock, Set<BlockPos> visited, RandomSource random) {
        Direction[] dirs = Direction.values().clone();
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction tmp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = tmp;
        }
        for (Direction d : dirs) {
            BlockPos candidate = veinPos.relative(d);
            if (visited.contains(candidate)) {
                continue;
            }
            BlockState candidateState = level.getBlockState(candidate);
            if (!isVeinCandidate(candidateState)) {
                continue;
            }
            if (isEnclosed(level, candidate) || random.nextFloat() < 0.05f) {
                BlockState bileState = bileBlock.defaultBlockState();
                level.setBlock(candidate, bileState, 2);
                scheduleFluidTick(level, candidate, bileState);
                visited.add(candidate);
                return;
            }
        }
    }

    private static boolean isEnclosed(WorldGenLevel level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(d));
            if (neighbor.isAir() || neighbor.canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isVeinCandidate(BlockState state) {
        return state.blocksMotion() && !state.is(Blocks.BEDROCK) && state.getFluidState().isEmpty();
    }

    private static Direction[] perpendiculars(Direction dir) {
        return switch (dir.getAxis()) {
            case X -> new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
            case Z -> new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST};
            case Y -> new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        };
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

    private record BranchStart(BlockPos pos, Direction dir) {}
}
