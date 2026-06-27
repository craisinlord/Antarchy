package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class ChitenSpikeFeature extends Feature<ChitenSpikeConfiguration> {
    private record ResolvedVariant(int radius, int maxColumnHeight, int heightDeviation, int surfaceSpikeAttempts) {
    }

    public ChitenSpikeFeature(Codec<ChitenSpikeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ChitenSpikeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        ChitenSpikeConfiguration config = context.config();
        RandomSource random = context.random();

        if (!level.getBlockState(origin).isAir()) {
            return false;
        }

        boolean placedAny = false;
        List<BlockPos> placedBlocks = new ArrayList<>();
        ResolvedVariant variant = this.resolveVariant(config, random);
        int radius = variant.radius();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double chance = this.getChanceOfColumn(radius, dx, dz, config);
                if (chance <= 0.0D) {
                    continue;
                }
                if (this.placeColumn(level, random, origin.offset(dx, 0, dz), dx, dz, chance, config, variant, placedBlocks)) {
                    placedAny = true;
                }
            }
        }

        if (this.dotSurfaceSpikes(level, random, placedBlocks, config, variant)) {
            placedAny = true;
        }
        return placedAny;
    }

    private ResolvedVariant resolveVariant(ChitenSpikeConfiguration config, RandomSource random) {
        if (config.largeVariantChance() > 0.0F && random.nextFloat() < config.largeVariantChance()) {
            int radius = config.radius() + config.largeVariantExtraRadius();
            int maxColumnHeight = config.maxColumnHeight() + config.largeVariantExtraHeight();
            int heightDeviation = config.heightDeviation() + config.largeVariantExtraHeightDeviation();
            int baseAttempts = Math.max(8, ((radius * 2) + 1) * ((radius * 2) + 1) / 6);
            int surfaceSpikeAttempts = Math.max(baseAttempts + 4, Math.round(baseAttempts * config.largeVariantSurfaceSpikeMultiplier()));
            return new ResolvedVariant(radius, maxColumnHeight, heightDeviation, surfaceSpikeAttempts);
        }

        int radius = config.radius();
        int baseAttempts = Math.max(8, ((radius * 2) + 1) * ((radius * 2) + 1) / 6);
        return new ResolvedVariant(radius, config.maxColumnHeight(), config.heightDeviation(), baseAttempts);
    }

    private boolean placeColumn(
            WorldGenLevel level,
            RandomSource random,
            BlockPos pos,
            int dx,
            int dz,
            double chance,
            ChitenSpikeConfiguration config,
            ResolvedVariant variant,
            List<BlockPos> placedBlocks
    ) {
        Optional<Column> optional = Column.scan(level, pos, config.floorToCeilingSearchRange(), state -> state.isAir(), state -> !state.isAir());
        if (optional.isEmpty()) {
            return false;
        }

        Column column = optional.get();
        OptionalInt ceiling = column.getCeiling();
        OptionalInt floor = column.getFloor();
        int maxHeight = this.getMaxLength(variant, ceiling, floor);
        int downLength = 0;
        int upLength = 0;

        if (ceiling.isPresent()) {
            downLength = this.getSpikeHeight(random, dx, dz, chance, maxHeight, config.maxDistanceFromCenterAffectingHeightBias(), variant.heightDeviation());
        }
        if (floor.isPresent()) {
            if (downLength > 0) {
                upLength = Mth.clamp(
                        downLength + Mth.randomBetweenInclusive(random, -config.maxHeightDifference(), config.maxHeightDifference()),
                        0,
                        maxHeight
                );
            } else {
                upLength = this.getSpikeHeight(random, dx, dz, chance, maxHeight, config.maxDistanceFromCenterAffectingHeightBias(), variant.heightDeviation());
            }
        }

        boolean placedAny = false;
        if (ceiling.isPresent() && downLength > 0) {
            placedAny |= this.growChitenColumn(level, pos.atY(ceiling.getAsInt() - 1), Direction.DOWN, downLength, placedBlocks);
        }
        if (floor.isPresent() && upLength > 0) {
            placedAny |= this.growChitenColumn(level, pos.atY(floor.getAsInt() + 1), Direction.UP, upLength, placedBlocks);
        }
        return placedAny;
    }

    private int getMaxLength(ResolvedVariant variant, OptionalInt ceiling, OptionalInt floor) {
        if (ceiling.isPresent() && floor.isPresent()) {
            return Math.min(variant.maxColumnHeight(), ceiling.getAsInt() - floor.getAsInt() - 1);
        }
        return variant.maxColumnHeight();
    }

    private boolean growChitenColumn(WorldGenLevel level, BlockPos rootStart, Direction direction, int length, List<BlockPos> placedBlocks) {
        boolean placedAny = false;
        for (int offset = 0; offset < length; offset++) {
            BlockPos spikePos = rootStart.relative(direction, offset);
            if (spikePos.getY() < level.getMinBuildHeight() || spikePos.getY() >= level.getMaxBuildHeight()) {
                break;
            }

            BlockState current = level.getBlockState(spikePos);
            if (!current.isAir()) {
                break;
            }

            BlockState chitenState = AntarchyObjects.CHITEN_BLOCK.get().defaultBlockState();
            level.setBlock(spikePos, chitenState, 3);
            placedBlocks.add(spikePos.immutable());
            placedAny = true;
        }
        return placedAny;
    }

    private boolean dotSurfaceSpikes(
            WorldGenLevel level,
            RandomSource random,
            List<BlockPos> placedBlocks,
            ChitenSpikeConfiguration config,
            ResolvedVariant variant
    ) {
        boolean placedAny = false;
        if (placedBlocks.isEmpty()) {
            return false;
        }

        int attempts = Math.max(variant.surfaceSpikeAttempts(), placedBlocks.size() / 6);
        for (int i = 0; i < attempts; i++) {
            BlockPos anchor = placedBlocks.get(random.nextInt(placedBlocks.size()));
            Direction direction = random.nextBoolean() ? Direction.UP : Direction.DOWN;
            int length = 1 + random.nextInt(Math.max(1, config.maxHeightDifference() + 1));
            BlockPos start = anchor.relative(direction);
            if (this.growSurfaceSpike(level, start, direction, length)) {
                placedAny = true;
            }
        }
        return placedAny;
    }

    private boolean growSurfaceSpike(WorldGenLevel level, BlockPos startPos, Direction direction, int length) {
        boolean placedAny = false;
        for (int offset = 0; offset < length; offset++) {
            BlockPos spikePos = startPos.relative(direction, offset);
            if (spikePos.getY() < level.getMinBuildHeight() || spikePos.getY() >= level.getMaxBuildHeight()) {
                break;
            }
            if (!level.getBlockState(spikePos).isAir()) {
                break;
            }

            BlockState spikeState = AntarchyObjects.CHITEN_SPIKE.get().defaultBlockState()
                    .setValue(PointedDripstoneBlock.TIP_DIRECTION, direction)
                    .setValue(PointedDripstoneBlock.THICKNESS, getThicknessForRootOffset(offset, length))
                    .setValue(PointedDripstoneBlock.WATERLOGGED, false);
            if (!spikeState.canSurvive(level, spikePos)) {
                break;
            }

            level.setBlock(spikePos, spikeState, 3);
            placedAny = true;
        }
        return placedAny;
    }

    private static DripstoneThickness getThicknessForRootOffset(int offsetFromRoot, int length) {
        if (length <= 1 || offsetFromRoot == length - 1) {
            return DripstoneThickness.TIP;
        }
        if (offsetFromRoot == length - 2) {
            return DripstoneThickness.FRUSTUM;
        }
        if (offsetFromRoot == 0) {
            return DripstoneThickness.BASE;
        }
        return DripstoneThickness.MIDDLE;
    }

    private int getSpikeHeight(
            RandomSource random,
            int dx,
            int dz,
            double chance,
            int maxHeight,
            int maxDistanceFromCenterAffectingHeightBias,
            int heightDeviation
    ) {
        if (maxHeight <= 0 || random.nextDouble() > chance) {
            return 0;
        }

        int distance = Math.abs(dx) + Math.abs(dz);
        float mean = (float) Mth.clampedMap(
                (double) distance,
                0.0D,
                (double) maxDistanceFromCenterAffectingHeightBias,
                (double) maxHeight,
                1.0D
        );
        int sampled = Math.round((float) (mean + random.nextGaussian() * Math.max(1, Math.min(maxHeight, heightDeviation))));
        return Mth.clamp(sampled, 1, maxHeight);
    }

    private double getChanceOfColumn(int radius, int dx, int dz, ChitenSpikeConfiguration config) {
        int remainingX = radius - Math.abs(dx);
        int remainingZ = radius - Math.abs(dz);
        int edgeDistance = Math.min(remainingX, remainingZ);
        return Mth.clampedMap(
                (double) edgeDistance,
                0.0D,
                (double) config.maxDistanceFromCenterAffectingChanceOfColumn(),
                (double) config.chanceOfColumnAtMaxDistanceFromCenter(),
                1.0D
        );
    }
}
