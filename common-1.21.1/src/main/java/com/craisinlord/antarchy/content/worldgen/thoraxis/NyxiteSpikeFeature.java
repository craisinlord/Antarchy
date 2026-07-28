package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
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

public class NyxiteSpikeFeature extends Feature<NyxiteSpikeConfiguration> {
    public NyxiteSpikeFeature(Codec<NyxiteSpikeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NyxiteSpikeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        NyxiteSpikeConfiguration config = context.config();
        RandomSource random = context.random();
        if (!level.getBlockState(origin).isAir()) {
            return false;
        }

        boolean placedAny = false;
        int radius = config.radius();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double chance = this.getChanceOfColumn(radius, dx, dz, config);
                if (chance <= 0.0D) {
                    continue;
                }

                if (this.placeColumn(level, random, origin.offset(dx, 0, dz), dx, dz, chance, config)) {
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

    private boolean placeColumn(
            WorldGenLevel level,
            RandomSource random,
            BlockPos pos,
            int dx,
            int dz,
            double chance,
            NyxiteSpikeConfiguration config
    ) {
        Optional<Column> optional = Column.scan(level, pos, config.floorToCeilingSearchRange(), state -> state.isAir(), state -> !state.isAir());
        if (optional.isEmpty()) {
            return false;
        }

        Column column = optional.get();
        OptionalInt ceiling = column.getCeiling();
        OptionalInt floor = column.getFloor();
        boolean canGrowDown = ceiling.isPresent() && level.getBlockState(pos.atY(ceiling.getAsInt())).is(AntarchyObjects.NYXITE.get());
        boolean canGrowUp = floor.isPresent() && level.getBlockState(pos.atY(floor.getAsInt())).is(AntarchyObjects.NYXITE.get());
        if (!canGrowDown && !canGrowUp) {
            return false;
        }

        int downLength = 0;
        int upLength = 0;
        if (canGrowDown) {
            int maxLength = this.getMaxLength(config, ceiling, floor);
            downLength = this.getSpikeHeight(random, dx, dz, chance, maxLength, config);
        }
        if (canGrowUp) {
            int maxLength = this.getMaxLength(config, ceiling, floor);
            if (downLength > 0) {
                upLength = Mth.clamp(
                        downLength + Mth.randomBetweenInclusive(random, -config.maxHeightDifference(), config.maxHeightDifference()),
                        0,
                        maxLength
                );
            } else {
                upLength = this.getSpikeHeight(random, dx, dz, chance, maxLength, config);
            }
        }

        if (ceiling.isPresent() && floor.isPresent() && downLength > 0 && upLength > 0 && ceiling.getAsInt() - downLength <= floor.getAsInt() + upLength) {
            int floorY = floor.getAsInt();
            int ceilingY = ceiling.getAsInt();
            int minSplit = Math.max(ceilingY - downLength, floorY + 1);
            int maxSplit = Math.min(floorY + upLength, ceilingY - 1);
            int split = Mth.randomBetweenInclusive(random, minSplit, maxSplit + 1);
            int tipBelowCeiling = split - 1;
            downLength = ceilingY - split;
            upLength = tipBelowCeiling - floorY;
        }

        boolean placedAny = false;
        if (downLength > 0 && ceiling.isPresent()) {
            placedAny |= this.growPointedNyxite(level, pos.atY(ceiling.getAsInt() - 1), Direction.DOWN, downLength);
        }
        if (upLength > 0 && floor.isPresent()) {
            placedAny |= this.growPointedNyxite(level, pos.atY(floor.getAsInt() + 1), Direction.UP, upLength);
        }
        return placedAny;
    }

    private int getMaxLength(NyxiteSpikeConfiguration config, OptionalInt ceiling, OptionalInt floor) {
        if (ceiling.isPresent() && floor.isPresent()) {
            return Math.min(config.maxColumnHeight(), ceiling.getAsInt() - floor.getAsInt() - 1);
        }
        return config.maxColumnHeight();
    }

    private boolean growPointedNyxite(WorldGenLevel level, BlockPos rootStart, Direction direction, int length) {
        boolean placedAny = false;
        for (int offset = 0; offset < length; offset++) {
            BlockPos spikePos = rootStart.relative(direction, offset);
            if (spikePos.getY() < level.getMinBuildHeight() || spikePos.getY() >= level.getMaxBuildHeight()) {
                break;
            }
            if (!level.getBlockState(spikePos).isAir()) {
                break;
            }

            BlockState spikeState = AntarchyObjects.NYXITE_SPIKE.get().defaultBlockState()
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
        if (length <= 1) {
            return DripstoneThickness.TIP;
        }
        if (offsetFromRoot == length - 1) {
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

    private int getSpikeHeight(RandomSource random, int dx, int dz, double chance, int maxHeight, NyxiteSpikeConfiguration config) {
        if (maxHeight <= 0 || random.nextDouble() > chance) {
            return 0;
        }

        int distance = Math.abs(dx) + Math.abs(dz);
        float mean = (float) Mth.clampedMap(
                (double) distance,
                0.0D,
                (double) config.maxDistanceFromCenterAffectingHeightBias(),
                (double) maxHeight,
                1.0D
        );
        int sampled = Math.round((float) (mean + random.nextGaussian() * config.heightDeviation()));
        return Mth.clamp(sampled, 1, maxHeight);
    }

    private double getChanceOfColumn(int radius, int dx, int dz, NyxiteSpikeConfiguration config) {
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
