package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;

public record TyphoniteSpikeFeature(
        int floorToCeilingSearchRange,
        int radius,
        int maxColumnHeight,
        int heightDeviation,
        float chanceOfColumnAtMaxDistanceFromCenter,
        int maxDistanceFromCenterAffectingChanceOfColumn,
        int maxDistanceFromCenterAffectingHeightBias,
        int maxHeightDifference
) implements Feature {
    public static final MapCodec<TyphoniteSpikeFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, 128).fieldOf("floor_to_ceiling_search_range").forGetter(TyphoniteSpikeFeature::floorToCeilingSearchRange),
            Codec.intRange(1, 32).fieldOf("radius").forGetter(TyphoniteSpikeFeature::radius),
            Codec.intRange(1, 32).fieldOf("max_column_height").forGetter(TyphoniteSpikeFeature::maxColumnHeight),
            Codec.intRange(1, 16).fieldOf("height_deviation").forGetter(TyphoniteSpikeFeature::heightDeviation),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_column_at_max_distance_from_center").forGetter(TyphoniteSpikeFeature::chanceOfColumnAtMaxDistanceFromCenter),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_chance_of_column").forGetter(TyphoniteSpikeFeature::maxDistanceFromCenterAffectingChanceOfColumn),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter(TyphoniteSpikeFeature::maxDistanceFromCenterAffectingHeightBias),
            Codec.intRange(0, 32).fieldOf("max_height_difference").forGetter(TyphoniteSpikeFeature::maxHeightDifference)
    ).apply(instance, TyphoniteSpikeFeature::new));
    private static final Identifier TYPHONITE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "typhonite");
    private static final Identifier TYPHONITE_SPIKE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "typhonite_spike");

    @Override
    public MapCodec<TyphoniteSpikeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!level.getBlockState(origin).isAir()) {
            return false;
        }

        boolean placedAny = false;
        int radius = this.radius();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double chance = this.getChanceOfColumn(radius, dx, dz, this);
                if (chance <= 0.0D) {
                    continue;
                }

                if (this.placeColumn(level, random, origin.offset(dx, 0, dz), dx, dz, chance, this)) {
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
            TyphoniteSpikeFeature config
    ) {
        Optional<Column> optional = Column.scan(level, pos, config.floorToCeilingSearchRange(), state -> state.isAir(), state -> !state.isAir());
        if (optional.isEmpty()) {
            return false;
        }

        Column column = optional.get();
        OptionalInt ceiling = column.getCeiling();
        OptionalInt floor = column.getFloor();
        boolean canGrowDown = ceiling.isPresent() && isTyphoniteAnchor(level.getBlockState(pos.atY(ceiling.getAsInt())));
        boolean canGrowUp = floor.isPresent() && isTyphoniteAnchor(level.getBlockState(pos.atY(floor.getAsInt())));
        if (!canGrowDown && !canGrowUp) {
            return false;
        }

        int downLength = 0;
        int upLength = 0;
        if (canGrowDown) {
            int maxLength = this.getMaxLength(config, ceiling, floor);
            downLength = this.getSpikeHeight(random, dx, dz, chance, maxLength, this);
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
                upLength = this.getSpikeHeight(random, dx, dz, chance, maxLength, this);
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
            placedAny |= this.growPointedTyphonite(level, pos.atY(ceiling.getAsInt() - 1), Direction.DOWN, downLength);
        }
        if (upLength > 0 && floor.isPresent()) {
            placedAny |= this.growPointedTyphonite(level, pos.atY(floor.getAsInt() + 1), Direction.UP, upLength);
        }
        return placedAny;
    }

    private int getMaxLength(TyphoniteSpikeFeature config, OptionalInt ceiling, OptionalInt floor) {
        if (ceiling.isPresent() && floor.isPresent()) {
            return Math.min(config.maxColumnHeight(), ceiling.getAsInt() - floor.getAsInt() - 1);
        }
        return config.maxColumnHeight();
    }

    private boolean growPointedTyphonite(WorldGenLevel level, BlockPos rootStart, Direction direction, int length) {
        Block typhoniteSpike = BuiltInRegistries.BLOCK.getValue(TYPHONITE_SPIKE_ID);
        boolean placedAny = false;
        for (int offset = 0; offset < length; offset++) {
            BlockPos spikePos = rootStart.relative(direction, offset);
            if (spikePos.getY() < level.getMinY() || spikePos.getY() >= level.getMaxY() + 1) {
                break;
            }
            if (!level.getBlockState(spikePos).isAir()) {
                break;
            }

            BlockState spikeState = typhoniteSpike.defaultBlockState()
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

    private static SpeleothemThickness getThicknessForRootOffset(int offsetFromRoot, int length) {
        if (length <= 1) {
            return SpeleothemThickness.TIP;
        }
        if (offsetFromRoot == length - 1) {
            return SpeleothemThickness.TIP;
        }
        if (offsetFromRoot == length - 2) {
            return SpeleothemThickness.MIDDLE;
        }
        return SpeleothemThickness.BASE;
    }

    private int getSpikeHeight(RandomSource random, int dx, int dz, double chance, int maxHeight, TyphoniteSpikeFeature config) {
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

    private double getChanceOfColumn(int radius, int dx, int dz, TyphoniteSpikeFeature config) {
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

    private static boolean isTyphoniteAnchor(BlockState state) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return blockId.equals(TYPHONITE_ID) || blockId.equals(Identifier.fromNamespaceAndPath(Antarchy.MODID, "verdant_nyxium"));
    }
}
