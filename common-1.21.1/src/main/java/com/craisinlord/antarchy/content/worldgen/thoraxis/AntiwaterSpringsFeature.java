package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.NyxiteSpikeBlock;
import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.block.state.PotentNyxiteState;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.FluidState;

public final class AntiwaterSpringsFeature extends Feature<AntiwaterSpringsConfiguration> {
    private static final ResourceLocation DREAM_DUNES_BIOME_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_dunes");

    public AntiwaterSpringsFeature(Codec<AntiwaterSpringsConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<AntiwaterSpringsConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        AntiwaterSpringsConfiguration config = context.config();

        BlockState antiwaterSourceState = config.state().getState(random, origin);
        if (antiwaterSourceState.isAir() || isDreamDunes(level, origin)) {
            return false;
        }

        boolean placedAny = false;
        int clusterCount = config.baseClusters();
        if (config.extraClusterChance() > 1 && random.nextInt(config.extraClusterChance()) == 0) {
            clusterCount++;
        }

        for (int cluster = 0; cluster < clusterCount; cluster++) {
            BlockPos searchOrigin = origin.offset(
                    random.nextInt(config.horizontalSearchRadius() * 2 + 1) - config.horizontalSearchRadius(),
                    0,
                    random.nextInt(config.horizontalSearchRadius() * 2 + 1) - config.horizontalSearchRadius()
            );
            BlockPos anchor = findVentAnchor(level, searchOrigin, config.floorSearchBelow(), config.floorSearchAbove());
            if (anchor == null) {
                continue;
            }

            boolean largeCluster = config.largeVentClusterChance() > 1 && random.nextInt(config.largeVentClusterChance()) == 0;
            int ventCount = largeCluster
                    ? randomBetween(random, config.minLargeVentCount(), config.maxLargeVentCount())
                    : randomBetween(random, config.minSmallVentCount(), config.maxSmallVentCount());

            for (int i = 0; i < ventCount; i++) {
                BlockPos ventPos = anchor.offset(
                        random.nextInt(config.ventOffsetRadius() * 2 + 1) - config.ventOffsetRadius(),
                        0,
                        random.nextInt(config.ventOffsetRadius() * 2 + 1) - config.ventOffsetRadius()
                );
                BlockPos surfacePos = findVentAnchor(level, ventPos, config.floorSearchBelow(), config.floorSearchAbove());
                if (surfacePos == null) {
                    continue;
                }

                int depth = largeCluster
                        ? randomBetween(random, config.largeClusterMinDepth(), config.largeClusterMaxDepth())
                        : randomBetween(random, config.minDepth(), config.maxDepth());
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
        int minY = Math.max(level.getMinBuildHeight() + 3, center.getY() - searchBelow);
        int maxY = Math.min(level.getMaxBuildHeight() - 3, center.getY() + searchAbove);

        for (int y = maxY; y >= minY; y--) {
            cursor.set(center.getX(), y, center.getZ());
            if (!isOpen(level, cursor.above())) {
                continue;
            }

            BlockState floorState = level.getBlockState(cursor);
            if (floorState.is(AntarchyObjects.NYXITE.get()) || floorState.is(AntarchyObjects.PALE_NYXITE.get())) {
                return cursor.immutable();
            }

            if (floorState.blocksMotion() && !floorState.is(Blocks.BEDROCK)) {
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
            if (isReplaceable(level, ringPos) || level.getBlockState(ringPos).blocksMotion()) {
                level.setBlock(ringPos, nyxiteState, 2);
            }
        }
    }

    private static void sealRing(WorldGenLevel level, BlockPos center, BlockState nyxiteState) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos ringPos = center.relative(direction);
            if (isReplaceable(level, ringPos) || !level.getBlockState(ringPos).blocksMotion()) {
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
                if (targetState.blocksMotion() && !targetState.is(Blocks.BEDROCK)) {
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
        return state.blocksMotion()
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
            DripstoneThickness thickness;
            if (height == 1) {
                thickness = DripstoneThickness.TIP;
            } else if (i == 0) {
                thickness = DripstoneThickness.BASE;
            } else if (i == height - 1) {
                thickness = DripstoneThickness.TIP;
            } else {
                thickness = DripstoneThickness.MIDDLE;
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
                .map(key -> key.location().equals(DREAM_DUNES_BIOME_ID))
                .orElse(false);
    }
}
