package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SpeleothemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;

public record ChitinSpikeFeature(
        int floorToCeilingSearchRange,
        int radius,
        int maxColumnHeight,
        int heightDeviation,
        float chanceOfColumnAtMaxDistanceFromCenter,
        int maxDistanceFromCenterAffectingChanceOfColumn,
        int maxDistanceFromCenterAffectingHeightBias,
        int maxHeightDifference,
        float largeVariantChance,
        int largeVariantExtraRadius,
        int largeVariantExtraHeight,
        int largeVariantExtraHeightDeviation,
        float largeVariantSurfaceSpikeMultiplier
) implements Feature {
    public static final MapCodec<ChitinSpikeFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, 128).fieldOf("floor_to_ceiling_search_range").forGetter(ChitinSpikeFeature::floorToCeilingSearchRange),
            Codec.intRange(1, 32).fieldOf("radius").forGetter(ChitinSpikeFeature::radius),
            Codec.intRange(1, 32).fieldOf("max_column_height").forGetter(ChitinSpikeFeature::maxColumnHeight),
            Codec.intRange(1, 16).fieldOf("height_deviation").forGetter(ChitinSpikeFeature::heightDeviation),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_column_at_max_distance_from_center").forGetter(ChitinSpikeFeature::chanceOfColumnAtMaxDistanceFromCenter),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_chance_of_column").forGetter(ChitinSpikeFeature::maxDistanceFromCenterAffectingChanceOfColumn),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter(ChitinSpikeFeature::maxDistanceFromCenterAffectingHeightBias),
            Codec.intRange(0, 32).fieldOf("max_height_difference").forGetter(ChitinSpikeFeature::maxHeightDifference),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("large_variant_chance", 0.0F).forGetter(ChitinSpikeFeature::largeVariantChance),
            Codec.intRange(0, 24).optionalFieldOf("large_variant_extra_radius", 0).forGetter(ChitinSpikeFeature::largeVariantExtraRadius),
            Codec.intRange(0, 32).optionalFieldOf("large_variant_extra_height", 0).forGetter(ChitinSpikeFeature::largeVariantExtraHeight),
            Codec.intRange(0, 16).optionalFieldOf("large_variant_extra_height_deviation", 0).forGetter(ChitinSpikeFeature::largeVariantExtraHeightDeviation),
            Codec.floatRange(1.0F, 8.0F).optionalFieldOf("large_variant_surface_spike_multiplier", 1.0F).forGetter(ChitinSpikeFeature::largeVariantSurfaceSpikeMultiplier)
    ).apply(instance, ChitinSpikeFeature::new));

    private record ResolvedVariant(int radius, int maxColumnHeight, int heightDeviation, int surfaceSpikeAttempts) {
    }

    @Override
    public MapCodec<ChitinSpikeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        ChitinSpikeFeature config = this;

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

    private ResolvedVariant resolveVariant(ChitinSpikeFeature config, RandomSource random) {
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
            ChitinSpikeFeature config,
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
            placedAny |= this.growChitinColumn(level, pos.atY(ceiling.getAsInt() - 1), Direction.DOWN, downLength, placedBlocks);
        }
        if (floor.isPresent() && upLength > 0) {
            placedAny |= this.growChitinColumn(level, pos.atY(floor.getAsInt() + 1), Direction.UP, upLength, placedBlocks);
        }
        return placedAny;
    }

    private int getMaxLength(ResolvedVariant variant, OptionalInt ceiling, OptionalInt floor) {
        if (ceiling.isPresent() && floor.isPresent()) {
            return Math.min(variant.maxColumnHeight(), ceiling.getAsInt() - floor.getAsInt() - 1);
        }
        return variant.maxColumnHeight();
    }

    private boolean growChitinColumn(WorldGenLevel level, BlockPos rootStart, Direction direction, int length, List<BlockPos> placedBlocks) {
        boolean placedAny = false;
        for (int offset = 0; offset < length; offset++) {
            BlockPos spikePos = rootStart.relative(direction, offset);
            if (spikePos.getY() < level.getMinY() || spikePos.getY() >= level.getMaxY() + 1) {
                break;
            }

            BlockState current = level.getBlockState(spikePos);
            if (!current.isAir()) {
                break;
            }

            BlockState chitinState = AntarchyObjects.CHITIN_BLOCK.get().defaultBlockState();
            level.setBlock(spikePos, chitinState, 3);
            placedBlocks.add(spikePos.immutable());
            placedAny = true;
        }
        return placedAny;
    }

    private boolean dotSurfaceSpikes(
            WorldGenLevel level,
            RandomSource random,
            List<BlockPos> placedBlocks,
            ChitinSpikeFeature config,
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
            if (spikePos.getY() < level.getMinY() || spikePos.getY() >= level.getMaxY() + 1) {
                break;
            }
            if (!level.getBlockState(spikePos).isAir()) {
                break;
            }

            BlockState spikeState = AntarchyObjects.CHITIN_SPIKE.get().defaultBlockState()
                    .setValue(SpeleothemBlock.TIP_DIRECTION, direction)
                    .setValue(SpeleothemBlock.THICKNESS, getThicknessForRootOffset(offset, length))
                    .setValue(SpeleothemBlock.WATERLOGGED, false);
            if (!spikeState.canSurvive(level, spikePos)) {
                break;
            }

            level.setBlock(spikePos, spikeState, 3);
            placedAny = true;
        }
        return placedAny;
    }

    private static SpeleothemThickness getThicknessForRootOffset(int offsetFromRoot, int length) {
        if (length <= 1 || offsetFromRoot == length - 1) {
            return SpeleothemThickness.TIP;
        }
        if (offsetFromRoot == length - 2) {
            return SpeleothemThickness.FRUSTUM;
        }
        if (offsetFromRoot == 0) {
            return SpeleothemThickness.BASE;
        }
        return SpeleothemThickness.MIDDLE;
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

    private double getChanceOfColumn(int radius, int dx, int dz, ChitinSpikeFeature config) {
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
