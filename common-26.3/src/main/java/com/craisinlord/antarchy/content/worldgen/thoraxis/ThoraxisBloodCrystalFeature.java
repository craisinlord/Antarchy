package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record ThoraxisBloodCrystalFeature(
        Holder<BlockStateProvider> baseState,
        Holder<BlockStateProvider> crystalState,
        int minHeight,
        int maxHeight,
        int minBaseRadius,
        int maxBaseRadius,
        int tipRadius,
        int sway,
        float sideCrystalChance,
        float branchChance
) implements Feature {
    public static final MapCodec<ThoraxisBloodCrystalFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("base_state").forGetter(ThoraxisBloodCrystalFeature::baseState),
            BlockStateProvider.CODEC.fieldOf("crystal_state").forGetter(ThoraxisBloodCrystalFeature::crystalState),
            Codec.intRange(1, 256).fieldOf("min_height").forGetter(ThoraxisBloodCrystalFeature::minHeight),
            Codec.intRange(1, 256).fieldOf("max_height").forGetter(ThoraxisBloodCrystalFeature::maxHeight),
            Codec.intRange(1, 24).fieldOf("min_base_radius").forGetter(ThoraxisBloodCrystalFeature::minBaseRadius),
            Codec.intRange(1, 24).fieldOf("max_base_radius").forGetter(ThoraxisBloodCrystalFeature::maxBaseRadius),
            Codec.intRange(1, 24).fieldOf("tip_radius").forGetter(ThoraxisBloodCrystalFeature::tipRadius),
            Codec.intRange(0, 32).fieldOf("sway").forGetter(ThoraxisBloodCrystalFeature::sway),
            Codec.floatRange(0.0F, 1.0F).fieldOf("side_crystal_chance").forGetter(ThoraxisBloodCrystalFeature::sideCrystalChance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("branch_chance").forGetter(ThoraxisBloodCrystalFeature::branchChance)
    ).apply(instance, ThoraxisBloodCrystalFeature::new));
    private static final int SURFACE_SEARCH_RANGE = 32;

    @Override
    public MapCodec<ThoraxisBloodCrystalFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        boolean hanging = random.nextBoolean();
        int anchorY = hanging ? findCeilingY(level, origin) : findFloorY(level, origin);
        if (anchorY < level.getMinY()) {
            hanging = !hanging;
            anchorY = hanging ? findCeilingY(level, origin) : findFloorY(level, origin);
        }
        if (anchorY < level.getMinY()) {
            return false;
        }

        int length = randomBetween(random, this.minHeight(), this.maxHeight());
        int baseRadius = randomBetween(random, this.minBaseRadius(), this.maxBaseRadius());
        int tipRadius = Math.max(1, this.tipRadius());
        int sway = Math.max(0, this.sway());
        BlockState baseState = this.baseState().value().getState(level, random, origin);
        BlockState buddingState = AntarchyObjects.BUDDING_BLOOD_CRYSTAL.get().defaultBlockState();
        BlockState smallCrystalState = this.crystalState().value().getState(level, random, origin);

        boolean placedAny = false;
        int startY = hanging ? anchorY - 1 : anchorY + 1;
        for (int step = 0; step < length; step++) {
            int y = hanging ? startY - step : startY + step;
            if (y <= level.getMinY() || y >= level.getMaxY()) {
                break;
            }

            double progress = length <= 1 ? 0.0D : step / (double) (length - 1);
            int radius = Math.max(tipRadius, (int) Math.round(Mth.lerp(progress, baseRadius, tipRadius)));
            int swayX = (int) Math.round(Math.sin(progress * Math.PI * 1.4D) * sway * 0.5D);
            int swayZ = (int) Math.round(Math.cos(progress * Math.PI * 1.2D) * sway * 0.5D);
            BlockPos center = new BlockPos(origin.getX() + swayX, y, origin.getZ() + swayZ);

            placedAny |= placeLayer(level, center, radius, baseState, buddingState, smallCrystalState, this.sideCrystalChance(), random);

            if (step > 2 && step < length - 2 && random.nextFloat() < this.branchChance()) {
                BlockPos branchCenter = center.offset(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
                placedAny |= placeLayer(level, branchCenter, Math.max(1, radius - 1), baseState, buddingState, smallCrystalState, this.sideCrystalChance() * 0.9F, random);
            }
        }

        return placedAny;
    }

    private boolean placeLayer(
            WorldGenLevel level,
            BlockPos center,
            int radius,
            BlockState baseState,
            BlockState buddingState,
            BlockState smallCrystalState,
            float crystalChance,
            RandomSource random
    ) {
        boolean placedAny = false;
        int actualRadius = Math.max(1, radius);

        for (int x = -actualRadius; x <= actualRadius; x++) {
            for (int z = -actualRadius; z <= actualRadius; z++) {
                double distance = x * x + z * z;
                if (distance > actualRadius * actualRadius + random.nextInt(3)) {
                    continue;
                }

                BlockPos target = center.offset(x, 0, z);
                BlockState current = level.getBlockState(target);
                if (current.is(Blocks.BEDROCK)) {
                    continue;
                }

                level.setBlock(target, baseState, 2);
                placedAny = true;
                boolean supportsCrystalGrowth = false;
                List<Direction> exposedDirections = collectExposedDirections(level, target, random);
                if (!exposedDirections.isEmpty() && random.nextFloat() < Math.min(0.95F, crystalChance * 1.35F)) {
                    int placements = 1 + random.nextInt(Math.min(3, exposedDirections.size()));
                    if (exposedDirections.size() >= 4 && random.nextFloat() < crystalChance) {
                        placements = Math.min(exposedDirections.size(), placements + 1);
                    }

                    for (int i = 0; i < placements; i++) {
                        Direction direction = exposedDirections.get(i);
                        supportsCrystalGrowth |= tryPlaceCrystal(level, target, direction, smallCrystalState, random);
                    }
                }

                if (supportsCrystalGrowth) {
                    level.setBlock(target, buddingState, 2);
                }
            }
        }

        return placedAny;
    }

    private boolean tryPlaceCrystal(WorldGenLevel level, BlockPos supportPos, Direction direction, BlockState smallCrystalState, RandomSource random) {
        BlockPos crystalPos = supportPos.relative(direction);
        BlockState existingState = level.getBlockState(crystalPos);
        if (existingState.is(Blocks.BEDROCK) || (!existingState.isAir() && !existingState.canBeReplaced() && existingState.getFluidState().isEmpty())) {
            return false;
        }

        BlockState fullCrystalState = orientGrowthState(smallCrystalState, direction);
        BlockState budState = orientGrowthState(chooseBudState(random), direction);

        if (random.nextFloat() < 0.75F && fullCrystalState.canSurvive(level, crystalPos)) {
            level.setBlock(crystalPos, fullCrystalState, 2);
            return true;
        }
        if (budState.canSurvive(level, crystalPos)) {
            level.setBlock(crystalPos, budState, 2);
            return true;
        }
        if (fullCrystalState.canSurvive(level, crystalPos)) {
            level.setBlock(crystalPos, fullCrystalState, 2);
            return true;
        }

        return false;
    }

    private List<Direction> collectExposedDirections(WorldGenLevel level, BlockPos target, RandomSource random) {
        List<Direction> directions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos sidePos = target.relative(direction);
            BlockState sideState = level.getBlockState(sidePos);
            if (sideState.is(Blocks.BEDROCK)) {
                continue;
            }
            if (sideState.isAir() || sideState.canBeReplaced() || !sideState.getFluidState().isEmpty()) {
                directions.add(direction);
            }
        }

        for (int i = directions.size() - 1; i > 0; i--) {
            int swapIndex = random.nextInt(i + 1);
            Direction current = directions.get(i);
            directions.set(i, directions.get(swapIndex));
            directions.set(swapIndex, current);
        }

        return directions;
    }

    private BlockState orientGrowthState(BlockState state, Direction direction) {
        BlockState placedState = state;
        if (placedState.hasProperty(AmethystClusterBlock.FACING)) {
            placedState = placedState.setValue(AmethystClusterBlock.FACING, direction);
        }
        if (placedState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            placedState = placedState.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return placedState;
    }

    private BlockState chooseBudState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.55F) {
            return AntarchyObjects.SMALL_BLOOD_CRYSTAL_BUD.get().defaultBlockState();
        }
        if (roll < 0.83F) {
            return AntarchyObjects.MEDIUM_BLOOD_CRYSTAL_BUD.get().defaultBlockState();
        }
        return AntarchyObjects.LARGE_BLOOD_CRYSTAL_BUD.get().defaultBlockState();
    }

    private int findFloorY(WorldGenLevel level, BlockPos origin) {
        int x = origin.getX();
        int z = origin.getZ();
        int startY = Mth.clamp(origin.getY(), level.getMinY() + 1, level.getMaxY() - 1);

        int minY = Math.max(level.getMinY() + 1, startY - SURFACE_SEARCH_RANGE);
        for (int y = startY; y > minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty()) {
                continue;
            }
            if (state.is(Blocks.BEDROCK)) {
                return -1;
            }
            return y;
        }

        return -1;
    }

    private int findCeilingY(WorldGenLevel level, BlockPos origin) {
        int x = origin.getX();
        int z = origin.getZ();
        int startY = Mth.clamp(origin.getY(), level.getMinY() + 1, level.getMaxY() - 1);

        int maxY = Math.min(level.getMaxY() - 1, startY + SURFACE_SEARCH_RANGE);
        for (int y = startY; y < maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty()) {
                continue;
            }
            if (state.is(Blocks.BEDROCK)) {
                return -1;
            }
            return y;
        }

        return -1;
    }

    private static int randomBetween(RandomSource random, int minInclusive, int maxInclusive) {
        int min = Math.min(minInclusive, maxInclusive);
        int max = Math.max(minInclusive, maxInclusive);
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }
}
