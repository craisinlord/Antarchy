package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.NyxiteSpikeBlock;
import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.block.state.PotentNyxiteState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.FluidState;

public record AntiwaterSpringsFeature(
        Holder<BlockStateProvider> state,
        int horizontalSearchRadius,
        int floorSearchBelow,
        int floorSearchAbove,
        int ventOffsetRadius,
        int baseClusters,
        int extraClusterChance,
        int minSmallVentCount,
        int maxSmallVentCount,
        int minLargeVentCount,
        int maxLargeVentCount,
        int largeVentClusterChance,
        int minDepth,
        int maxDepth,
        int largeClusterMinDepth,
        int largeClusterMaxDepth
) implements Feature {
    public static final MapCodec<AntiwaterSpringsFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("state").forGetter(AntiwaterSpringsFeature::state),
            Codec.intRange(0, 64).fieldOf("horizontal_search_radius").forGetter(AntiwaterSpringsFeature::horizontalSearchRadius),
            Codec.intRange(1, 64).fieldOf("floor_search_below").forGetter(AntiwaterSpringsFeature::floorSearchBelow),
            Codec.intRange(1, 64).fieldOf("floor_search_above").forGetter(AntiwaterSpringsFeature::floorSearchAbove),
            Codec.intRange(0, 32).fieldOf("vent_offset_radius").forGetter(AntiwaterSpringsFeature::ventOffsetRadius),
            Codec.intRange(1, 16).fieldOf("base_clusters").forGetter(AntiwaterSpringsFeature::baseClusters),
            Codec.intRange(1, 64).fieldOf("extra_cluster_chance").forGetter(AntiwaterSpringsFeature::extraClusterChance),
            Codec.intRange(1, 16).fieldOf("min_small_vent_count").forGetter(AntiwaterSpringsFeature::minSmallVentCount),
            Codec.intRange(1, 16).fieldOf("max_small_vent_count").forGetter(AntiwaterSpringsFeature::maxSmallVentCount),
            Codec.intRange(1, 16).fieldOf("min_large_vent_count").forGetter(AntiwaterSpringsFeature::minLargeVentCount),
            Codec.intRange(1, 16).fieldOf("max_large_vent_count").forGetter(AntiwaterSpringsFeature::maxLargeVentCount),
            Codec.intRange(1, 64).fieldOf("large_vent_cluster_chance").forGetter(AntiwaterSpringsFeature::largeVentClusterChance),
            Codec.intRange(1, 32).fieldOf("min_depth").forGetter(AntiwaterSpringsFeature::minDepth),
            Codec.intRange(1, 32).fieldOf("max_depth").forGetter(AntiwaterSpringsFeature::maxDepth),
            Codec.intRange(1, 32).fieldOf("large_cluster_min_depth").forGetter(AntiwaterSpringsFeature::largeClusterMinDepth),
            Codec.intRange(1, 32).fieldOf("large_cluster_max_depth").forGetter(AntiwaterSpringsFeature::largeClusterMaxDepth)
    ).apply(instance, AntiwaterSpringsFeature::new));
    private static final Identifier DREAM_DUNES_BIOME_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "dream_dunes");

    @Override
    public MapCodec<AntiwaterSpringsFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockState antiwaterSourceState = this.state().value().getState(level, random, origin);
        if (antiwaterSourceState.isAir() || isDreamDunes(level, origin)) {
            return false;
        }

        boolean placedAny = false;
        int clusterCount = this.baseClusters();
        if (this.extraClusterChance() > 1 && random.nextInt(this.extraClusterChance()) == 0) {
            clusterCount++;
        }

        for (int cluster = 0; cluster < clusterCount; cluster++) {
            BlockPos searchOrigin = origin.offset(
                    random.nextInt(this.horizontalSearchRadius() * 2 + 1) - this.horizontalSearchRadius(),
                    0,
                    random.nextInt(this.horizontalSearchRadius() * 2 + 1) - this.horizontalSearchRadius()
            );
            BlockPos anchor = findVentAnchor(level, searchOrigin, this.floorSearchBelow(), this.floorSearchAbove());
            if (anchor == null) {
                continue;
            }

            boolean largeCluster = this.largeVentClusterChance() > 1 && random.nextInt(this.largeVentClusterChance()) == 0;
            int ventCount = largeCluster
                    ? randomBetween(random, this.minLargeVentCount(), this.maxLargeVentCount())
                    : randomBetween(random, this.minSmallVentCount(), this.maxSmallVentCount());

            for (int i = 0; i < ventCount; i++) {
                BlockPos ventPos = anchor.offset(
                        random.nextInt(this.ventOffsetRadius() * 2 + 1) - this.ventOffsetRadius(),
                        0,
                        random.nextInt(this.ventOffsetRadius() * 2 + 1) - this.ventOffsetRadius()
                );
                BlockPos surfacePos = findVentAnchor(level, ventPos, this.floorSearchBelow(), this.floorSearchAbove());
                if (surfacePos == null) {
                    continue;
                }

                int depth = largeCluster
                        ? randomBetween(random, this.largeClusterMinDepth(), this.largeClusterMaxDepth())
                        : randomBetween(random, this.minDepth(), this.maxDepth());
                placedAny |= placeSpring(level, surfacePos, depth, antiwaterSourceState, random);
            }
        }

        return placedAny;
    }

    private static boolean placeSpring(WorldGenLevel level, BlockPos ventPos, int depth, BlockState antiwaterSourceState, RandomSource random) {
        if (!isOpen(level, ventPos.above())) {
            return false;
        }
        if (!hasTerrainSupport(level, ventPos, depth)) {
            return false;
        }

        BlockState smoothNyxiteState = AntarchyObjects.PALE_NYXITE.get().defaultBlockState();
        BlockState potentState = AntarchyObjects.POTENT_NYXITE.get()
                .defaultBlockState()
                .setValue(PotentNyxiteBlock.STATE, PotentNyxiteState.DORMANT);

        for (int i = 1; i <= depth; i++) {
            BlockPos antiwaterPos = ventPos.below(i);
            if (level.getBlockState(antiwaterPos).is(Blocks.BEDROCK)) {
                return false;
            }
        }

        BlockPos terminatorPos = ventPos.below(depth + 1);
        if (level.getBlockState(terminatorPos).is(Blocks.BEDROCK)) {
            return false;
        }

        sealSurface(level, ventPos, smoothNyxiteState);
        level.setBlock(ventPos, potentState, 2);

        for (int i = 1; i <= depth; i++) {
            BlockPos antiwaterPos = ventPos.below(i);
            level.setBlock(antiwaterPos, antiwaterSourceState, 2);
            scheduleFluidTick(level, antiwaterPos, antiwaterSourceState);
            sealRing(level, antiwaterPos, smoothNyxiteState);
        }

        level.setBlock(terminatorPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
        sealRing(level, terminatorPos, smoothNyxiteState);
        if (isReplaceable(level, terminatorPos.below())) {
            level.setBlock(terminatorPos.below(), smoothNyxiteState, 2);
        }
        decorateSurface(level, ventPos, smoothNyxiteState, random);

        return true;
    }

    private static BlockPos findVentAnchor(WorldGenLevel level, BlockPos center, int searchBelow, int searchAbove) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinY() + 3, center.getY() - searchBelow);
        int maxY = Math.min(level.getMaxY() - 2, center.getY() + searchAbove);

        for (int y = maxY; y >= minY; y--) {
            cursor.set(center.getX(), y, center.getZ());
            if (!isOpen(level, cursor.above())) {
                continue;
            }

            BlockState floorState = level.getBlockState(cursor);
            if (floorState.is(AntarchyObjects.NYXITE.get()) || floorState.is(AntarchyObjects.PALE_NYXITE.get())) {
                return cursor.immutable();
            }

            if (blocksMotion(floorState) && !floorState.is(Blocks.BEDROCK)) {
                return cursor.immutable();
            }
        }

        return null;
    }

    private static void sealSurface(WorldGenLevel level, BlockPos center, BlockState nyxiteState) {
        if (!level.getBlockState(center).is(AntarchyObjects.POTENT_NYXITE.get())) {
            level.setBlock(center, nyxiteState, 2);
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos ringPos = center.relative(direction);
            if (isReplaceable(level, ringPos) || blocksMotion(level.getBlockState(ringPos))) {
                level.setBlock(ringPos, nyxiteState, 2);
            }
        }
    }

    private static void sealRing(WorldGenLevel level, BlockPos center, BlockState nyxiteState) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos ringPos = center.relative(direction);
            if (isReplaceable(level, ringPos) || !blocksMotion(level.getBlockState(ringPos))) {
                level.setBlock(ringPos, nyxiteState, 2);
            }
        }
    }

    private static void decorateSurface(WorldGenLevel level, BlockPos ventPos, BlockState smoothNyxiteState, RandomSource random) {
        int patchRadius = 3 + random.nextInt(2);
        for (int dx = -patchRadius; dx <= patchRadius; dx++) {
            for (int dz = -patchRadius; dz <= patchRadius; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }

                BlockPos target = ventPos.offset(dx, 0, dz);
                if (Math.abs(dx) + Math.abs(dz) > patchRadius + random.nextInt(2)) {
                    continue;
                }

                BlockState targetState = level.getBlockState(target);
                if (blocksMotion(targetState) && !targetState.is(Blocks.BEDROCK)) {
                    level.setBlock(target, smoothNyxiteState, 2);
                }
            }
        }

        int pillarCount = 3 + random.nextInt(4);
        for (int i = 0; i < pillarCount; i++) {
            BlockPos pillarBase = ventPos.offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
            if (pillarBase.equals(ventPos) || !level.getBlockState(pillarBase).is(AntarchyObjects.PALE_NYXITE.get())) {
                continue;
            }

            int pillarHeight = 1 + random.nextInt(4);
            if (!canPlacePillar(level, pillarBase, pillarHeight)) {
                continue;
            }

            for (int y = 1; y <= pillarHeight; y++) {
                level.setBlock(pillarBase.above(y), smoothNyxiteState, 2);
            }

            placeSpikeColumn(level, pillarBase.above(pillarHeight + 1), 1 + random.nextInt(2));
        }

        int surfaceSpikeCount = 4 + random.nextInt(5);
        for (int i = 0; i < surfaceSpikeCount; i++) {
            BlockPos spikeBase = ventPos.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
            if (!level.getBlockState(spikeBase).is(AntarchyObjects.PALE_NYXITE.get())) {
                continue;
            }
            placeSpikeColumn(level, spikeBase.above(), 1 + random.nextInt(2));
        }
    }

    private static boolean canPlacePillar(WorldGenLevel level, BlockPos basePos, int height) {
        for (int y = 1; y <= height + 1; y++) {
            if (!isOpen(level, basePos.above(y))) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasTerrainSupport(WorldGenLevel level, BlockPos ventPos, int depth) {
        for (int i = 0; i <= depth + 2; i++) {
            BlockPos layerPos = ventPos.below(i);
            if (!isSolidOrSelf(level, layerPos)) {
                return false;
            }

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos ringPos = layerPos.relative(direction);
                if (!isSolidOrSelf(level, ringPos)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isSolidOrSelf(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return blocksMotion(state)
                || state.is(AntarchyObjects.NYXITE.get())
                || state.is(AntarchyObjects.PALE_NYXITE.get())
                || state.is(AntarchyObjects.POTENT_NYXITE.get());
    }

    private static void placeSpikeColumn(WorldGenLevel level, BlockPos startPos, int height) {
        for (int i = 0; i < height; i++) {
            BlockPos spikePos = startPos.above(i);
            if (!isOpen(level, spikePos)) {
                return;
            }
        }

        for (int i = 0; i < height; i++) {
            BlockPos spikePos = startPos.above(i);
            SpeleothemThickness thickness;
            if (height == 1) {
                thickness = SpeleothemThickness.TIP;
            } else if (i == 0) {
                thickness = SpeleothemThickness.BASE;
            } else if (i == height - 1) {
                thickness = SpeleothemThickness.TIP;
            } else {
                thickness = SpeleothemThickness.MIDDLE;
            }

            BlockState spikeState = AntarchyObjects.NYXITE_SPIKE.get()
                    .defaultBlockState()
                    .setValue(NyxiteSpikeBlock.TIP_DIRECTION, Direction.UP)
                    .setValue(NyxiteSpikeBlock.THICKNESS, thickness)
                    .setValue(NyxiteSpikeBlock.WATERLOGGED, false);
            level.setBlock(spikePos, spikeState, 2);
        }
    }

    private static boolean isOpen(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();
        return state.isAir() || (state.getCollisionShape(level, pos).isEmpty() && fluidState.isEmpty());
    }

    private static boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();
        return !state.is(Blocks.BEDROCK) && (state.isAir() || state.getCollisionShape(level, pos).isEmpty() || !fluidState.isEmpty());
    }

    private static void scheduleFluidTick(WorldGenLevel level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    private static int randomBetween(RandomSource random, int min, int max) {
        return min >= max ? min : random.nextInt(max - min + 1) + min;
    }

    private static boolean isDreamDunes(WorldGenLevel level, BlockPos pos) {
        return level.getBiome(pos)
                .unwrapKey()
                .map(key -> key.identifier().equals(DREAM_DUNES_BIOME_ID))
                .orElse(false);
    }

    private static boolean blocksMotion(BlockState state) {
        Block block = state.getBlock();
        return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && state.isSolid();
    }
}
