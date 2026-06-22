package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.block.state.PotentNyxiteState;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class LucidAntiwaterPoolFeature extends Feature<ThoraxisAntiwaterPoolConfiguration> {
    private static final int MIN_CAVITY_HEIGHT = 10;
    private static final int MAX_CAVITY_HEIGHT = 44;
    private static final int MAX_LOBES = 2;
    private static final int[] BOTTOM_VARIATION = {0, 1, 1};

    public LucidAntiwaterPoolFeature(Codec<ThoraxisAntiwaterPoolConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ThoraxisAntiwaterPoolConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        ThoraxisAntiwaterPoolConfiguration config = context.config();

        int radius = randomBetween(random, Math.max(4, config.minRadius() / 2), Math.max(6, config.maxRadius() / 2));
        int depth = randomBetween(random, config.minDepth(), config.maxDepth());
        int sourceCount = Math.max(2, randomBetween(random, 2, 4));
        BlockState antiwaterSourceState = config.state().getState(random, origin);
        BlockState basaltState = AntarchyObjects.ANTIMETAL.get().defaultBlockState();

        int floorY = findFloorY(level, origin.getX(), origin.getZ());
        if (floorY <= level.getMinBuildHeight()) {
            return false;
        }

        int ceilingY = findCeilingY(level, origin.getX(), origin.getZ(), floorY);
        if (ceilingY <= floorY + MIN_CAVITY_HEIGHT || ceilingY - floorY > MAX_CAVITY_HEIGHT) {
            return false;
        }

        boolean placedAny = false;
        int lobeCount = randomBetween(random, 1, Math.min(MAX_LOBES, 1 + radius / 6));

        for (int lobe = 0; lobe < lobeCount; lobe++) {
            int lobeRadius = Math.max(3, radius - random.nextInt(Math.max(1, radius / 3 + 1)));
            int offsetX = origin.getX() + random.nextInt(Math.max(2, radius)) - Math.max(1, radius / 2);
            int offsetZ = origin.getZ() + random.nextInt(Math.max(2, radius)) - Math.max(1, radius / 2);
            int ventBudget = sourceCount + random.nextInt(2);
            BasinProfile profile = BasinProfile.create(random);

            placedAny |= placeCeilingBasin(level, random, offsetX, floorY, offsetZ, lobeRadius, ceilingY, profile, basaltState, antiwaterSourceState);
            placedAny |= placeAntiwaterSources(level, random, offsetX, floorY, offsetZ, Math.max(2, lobeRadius - 1), ventBudget, antiwaterSourceState);
//            placedAny |= maybePlacePotentNyxite(level, random, offsetX, floorY, offsetZ, Math.max(2, lobeRadius - 1));
        }

        return placedAny;
    }

    private static boolean placeCeilingBasin(
            WorldGenLevel level,
            RandomSource random,
            int centerX,
            int floorY,
            int centerZ,
            int radius,
            int maxCeilingY,
            BasinProfile profile,
            BlockState basaltState,
            BlockState antiwaterSourceState
    ) {
        boolean placedAny = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockState filledAntiwater = flowingAntiwaterState(antiwaterSourceState, 7);

        for (int dx = -radius - 2; dx <= radius + 2; dx++) {
            for (int dz = -radius - 2; dz <= radius + 2; dz++) {
                double shapeDistance = profile.distance(dx, dz);
                if (shapeDistance > radius + 1.35D) {
                    continue;
                }

                int worldX = centerX + dx;
                int worldZ = centerZ + dz;
                int localCeilingY = findCeilingY(level, worldX, worldZ, floorY);
                if (localCeilingY <= floorY + MIN_CAVITY_HEIGHT || localCeilingY > maxCeilingY + 3) {
                    continue;
                }

                int basinTopY = localCeilingY - 1;
                int baseBottomY = Math.max(floorY + 6, basinTopY - 2);
                int variation = BOTTOM_VARIATION[Math.floorMod((dx * 31) + (dz * 17) + profile.seed(), BOTTOM_VARIATION.length)];
                int interiorBottomY = Math.max(floorY + 5, baseBottomY - variation);
                int edgeBottomY = Math.max(interiorBottomY, basinTopY - 3);

                boolean edge = shapeDistance >= radius - (1.3D + profile.edgeWidth());
                boolean shoulder = !edge && shapeDistance >= radius - (2.2D + profile.shoulderWidth()) && random.nextFloat() < 0.55F;
                int columnBottomY = edge ? edgeBottomY : interiorBottomY;

                if (edge || shoulder) {
                    for (int y = columnBottomY; y <= basinTopY; y++) {
                        mutable.set(worldX, y, worldZ);
                        if (!isReplaceable(level.getBlockState(mutable))) {
                            continue;
                        }

                        level.setBlock(mutable, basaltState, 2);
                        placedAny = true;
                    }

                    placedAny |= placeOuterLip(level, random, worldX, worldZ, floorY, basinTopY, profile, dx, dz, radius, basaltState);
                    continue;
                }

                for (int y = columnBottomY; y <= basinTopY; y++) {
                    mutable.set(worldX, y, worldZ);
                    if (!isReplaceable(level.getBlockState(mutable))) {
                        continue;
                    }

                    BlockState antiwaterState = y == basinTopY ? antiwaterSourceState : filledAntiwater;
                    level.setBlock(mutable, antiwaterState, 2);
                    scheduleFluidTick(level, mutable, antiwaterState);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

    private static boolean placeOuterLip(
            WorldGenLevel level,
            RandomSource random,
            int centerX,
            int centerZ,
            int floorY,
            int basinTopY,
            BasinProfile profile,
            int dx,
            int dz,
            int radius,
            BlockState basaltState
    ) {
        boolean placedAny = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int stepX = -1; stepX <= 1; stepX++) {
            for (int stepZ = -1; stepZ <= 1; stepZ++) {
                if (stepX == 0 && stepZ == 0) {
                    continue;
                }

                double outerDistance = profile.distance(dx + stepX, dz + stepZ);
                if (outerDistance <= radius || outerDistance > radius + 1.2D) {
                    continue;
                }
                if (random.nextFloat() > 0.72F) {
                    continue;
                }

                int x = centerX + stepX;
                int z = centerZ + stepZ;
                int localCeilingY = findCeilingY(level, x, z, floorY);
                if (localCeilingY <= floorY + MIN_CAVITY_HEIGHT) {
                    continue;
                }

                int topY = localCeilingY - 1;
                int lipDepth = 2 + random.nextInt(2);
                for (int y = Math.max(topY - lipDepth, basinTopY - 3); y <= topY; y++) {
                    mutable.set(x, y, z);
                    if (!isReplaceable(level.getBlockState(mutable))) {
                        continue;
                    }

                    level.setBlock(mutable, basaltState, 2);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

    private static boolean placeAntiwaterSources(
            WorldGenLevel level,
            RandomSource random,
            int centerX,
            int floorY,
            int centerZ,
            int radius,
            int sourceCount,
            BlockState antiwaterSourceState
    ) {
        boolean placedAny = false;
        int attempts = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        while (sourceCount > 0 && attempts++ < sourceCount * 6) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > radius * 0.45D + random.nextDouble() * 0.35D) {
                continue;
            }

            BlockPos sourcePos = findSupportedSourcePos(level, centerX + dx, floorY, centerZ + dz);
            if (sourcePos == null) {
                continue;
            }

            mutable.set(sourcePos);
            level.setBlock(mutable, antiwaterSourceState, 2);
            scheduleFluidTick(level, mutable, antiwaterSourceState);
            placedAny = true;
            sourceCount--;
        }

        return placedAny;
    }

    private static boolean maybePlacePotentNyxite(
            WorldGenLevel level,
            RandomSource random,
            int centerX,
            int floorY,
            int centerZ,
            int radius
    ) {
        if (random.nextInt(8) != 0) {
            return false;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockState magmaState = Blocks.MAGMA_BLOCK.defaultBlockState();
        BlockState potentState = AntarchyObjects.POTENT_NYXITE.get()
                .defaultBlockState()
                .setValue(PotentNyxiteBlock.STATE, PotentNyxiteState.DORMANT);

        for (int attempt = 0; attempt < 12; attempt++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            int worldX = centerX + dx;
            int worldZ = centerZ + dz;
            int localCeilingY = findCeilingY(level, worldX, worldZ, floorY);
            if (localCeilingY <= floorY + MIN_CAVITY_HEIGHT) {
                continue;
            }

            BlockPos magmaPos = new BlockPos(worldX, localCeilingY, worldZ);
            BlockPos potentPos = magmaPos.below();
            if (!isSolidSurface(level.getBlockState(magmaPos)) || !isOpenBlock(level.getBlockState(potentPos))) {
                continue;
            }

            mutable.set(magmaPos);
            level.setBlock(mutable, magmaState, 2);
            mutable.set(potentPos);
            level.setBlock(mutable, potentState, 2);
            return true;
        }

        return false;
    }

    private static BlockPos findSupportedSourcePos(WorldGenLevel level, int x, int floorY, int z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = floorY + 2; y >= floorY - 3; y--) {
            mutable.set(x, y, z);
            BlockState state = level.getBlockState(mutable);
            if (!isOpenBlock(state)) {
                continue;
            }

            BlockPos belowPos = mutable.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (!isSolidSurface(belowState)) {
                continue;
            }

            return belowPos.immutable();
        }

        return null;
    }

    private static BlockState flowingAntiwaterState(BlockState sourceState, int level) {
        if (!sourceState.hasProperty(LiquidBlock.LEVEL)) {
            return sourceState;
        }

        int clamped = Mth.clamp(level, 1, 7);
        return sourceState.setValue(LiquidBlock.LEVEL, clamped);
    }

    private static void scheduleFluidTick(WorldGenLevel level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    private static int findFloorY(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int floorY = Integer.MIN_VALUE;

        for (int y = level.getMinBuildHeight() + 1; y < level.getMaxBuildHeight() - 1; y++) {
            mutable.set(x, y, z);
            BlockState state = level.getBlockState(mutable);
            if (!isSolidSurface(state)) {
                continue;
            }

            mutable.set(x, y + 1, z);
            if (isOpenBlock(level.getBlockState(mutable))) {
                floorY = y;
            }
        }

        return floorY;
    }

    private static int findCeilingY(WorldGenLevel level, int x, int z, int floorY) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = floorY + 2; y < level.getMaxBuildHeight() - 1; y++) {
            mutable.set(x, y, z);
            BlockState state = level.getBlockState(mutable);
            if (!isSolidSurface(state)) {
                continue;
            }

            mutable.set(x, y - 1, z);
            if (isOpenBlock(level.getBlockState(mutable))) {
                return y;
            }
        }

        return Integer.MIN_VALUE;
    }

    private static boolean isSolidSurface(BlockState state) {
        return !state.isAir() && state.blocksMotion() && state.getFluidState().isEmpty();
    }

    private static boolean isOpenBlock(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !state.blocksMotion();
    }

    private static boolean isReplaceable(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !state.blocksMotion();
    }

    private static int randomBetween(RandomSource random, int minInclusive, int maxInclusive) {
        int min = Math.min(minInclusive, maxInclusive);
        int max = Math.max(minInclusive, maxInclusive);
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }

    private record BasinProfile(
            int seed,
            double scaleX,
            double scaleZ,
            double waveX,
            double waveZ,
            double phaseX,
            double phaseZ,
            double edgeWidth,
            double shoulderWidth
    ) {
        private static BasinProfile create(RandomSource random) {
            int seed = random.nextInt();
            int variant = random.nextInt(3);
            return switch (variant) {
                case 0 -> new BasinProfile(
                        seed,
                        1.0D + random.nextDouble() * 0.12D,
                        1.0D + random.nextDouble() * 0.12D,
                        0.18D,
                        0.15D,
                        random.nextDouble() * Math.PI * 2.0D,
                        random.nextDouble() * Math.PI * 2.0D,
                        0.18D,
                        0.12D
                );
                case 1 -> new BasinProfile(
                        seed,
                        1.2D + random.nextDouble() * 0.2D,
                        0.82D + random.nextDouble() * 0.12D,
                        0.22D,
                        0.18D,
                        random.nextDouble() * Math.PI * 2.0D,
                        random.nextDouble() * Math.PI * 2.0D,
                        0.26D,
                        0.16D
                );
                default -> new BasinProfile(
                        seed,
                        1.05D + random.nextDouble() * 0.18D,
                        0.95D + random.nextDouble() * 0.18D,
                        0.3D,
                        0.28D,
                        random.nextDouble() * Math.PI * 2.0D,
                        random.nextDouble() * Math.PI * 2.0D,
                        0.34D,
                        0.24D
                );
            };
        }

        private double distance(int dx, int dz) {
            double warpedX = (dx / scaleX) + Math.sin((dz * 0.55D) + phaseX) * waveX;
            double warpedZ = (dz / scaleZ) + Math.cos((dx * 0.65D) + phaseZ) * waveZ;
            double ellipse = Math.sqrt((warpedX * warpedX) + (warpedZ * warpedZ));
            double jag = (Math.sin((dx * 0.8D) + phaseX) + Math.cos((dz * 0.9D) + phaseZ)) * 0.22D;
            return ellipse + jag;
        }
    }
}
