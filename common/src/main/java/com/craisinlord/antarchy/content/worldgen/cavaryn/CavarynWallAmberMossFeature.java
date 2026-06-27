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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CavarynWallAmberMossFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation AMBER_LICHEN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "amber_lichen");
    private static final ResourceLocation GLOW_LICHEN_ID = ResourceLocation.withDefaultNamespace("glow_lichen");
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_ATTEMPTS = 28;
    private static final int VERTICAL_SCAN = 10;

    public CavarynWallAmberMossFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Block amberLichenBlock = getBlock(AMBER_LICHEN_ID);
        Block glowLichenBlock = getBlock(GLOW_LICHEN_ID);
        if (amberLichenBlock == null || glowLichenBlock == null) {
            return false;
        }

        Anchor anchor = findAnchor(level, context.origin(), random);
        if (anchor == null) {
            return false;
        }

        List<BlockPos> placedPatch = placeWallPatch(level, anchor, amberLichenBlock, random);
        if (placedPatch.isEmpty()) {
            return false;
        }

        placeLichenFringe(level, placedPatch, anchor.supportDirection(), amberLichenBlock, glowLichenBlock, random);
        return true;
    }

    private static Anchor findAnchor(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int startY = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            startY = Math.max(level.getMinBuildHeight() + 2, Math.min(level.getMaxBuildHeight() - 3, startY));

            for (int y = startY; y >= Math.max(level.getMinBuildHeight() + 1, startY - VERTICAL_SCAN); y--) {
                mutable.set(x, y, z);
                if (!level.getBlockState(mutable).canBeReplaced()) {
                    continue;
                }

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos supportPos = mutable.relative(direction);
                    BlockState supportState = level.getBlockState(supportPos);
                    if (supportState.isFaceSturdy(level, supportPos, direction.getOpposite())) {
                        return new Anchor(mutable.immutable(), direction);
                    }
                }
            }
        }

        return null;
    }

    private static List<BlockPos> placeWallPatch(WorldGenLevel level, Anchor anchor, Block amberLichenBlock, RandomSource random) {
        List<BlockPos> placed = new ArrayList<>();
        Direction lateral = anchor.supportDirection().getClockWise();
        int width = 1 + random.nextInt(3);
        int height = 2 + random.nextInt(4);

        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int lateralOffset = -width; lateralOffset <= width; lateralOffset++) {
                if (Math.abs(lateralOffset) == width && random.nextBoolean()) {
                    continue;
                }

                BlockPos pos = anchor.pos().above(yOffset).relative(lateral, lateralOffset);
                if (!canPlaceWallMoss(level, pos, anchor.supportDirection())) {
                    continue;
                }

                BlockState state = createLichenState(amberLichenBlock, anchor.supportDirection());
                if (!state.canSurvive(level, pos)) {
                    continue;
                }

                level.setBlock(pos, state, 3);
                placed.add(pos.immutable());
            }
        }

        return placed;
    }

    private static void placeLichenFringe(
            WorldGenLevel level,
            List<BlockPos> patchPositions,
            Direction supportDirection,
            Block amberLichenBlock,
            Block glowLichenBlock,
            RandomSource random
    ) {
        for (BlockPos patchPos : patchPositions) {
            for (Direction spread : List.of(Direction.UP, Direction.DOWN, supportDirection.getClockWise(), supportDirection.getCounterClockWise())) {
                if (random.nextFloat() > 0.45F) {
                    continue;
                }

                BlockPos targetPos = patchPos.relative(spread);
                if (!level.getBlockState(targetPos).canBeReplaced()) {
                    continue;
                }
                if (!hasSupport(level, targetPos, supportDirection)) {
                    continue;
                }

                BlockState state = createLichenState(random.nextFloat() < 0.65F ? amberLichenBlock : glowLichenBlock, supportDirection);
                if (!state.canSurvive(level, targetPos)) {
                    continue;
                }

                level.setBlock(targetPos, state, 3);
            }
        }
    }

    private static boolean canPlaceWallMoss(WorldGenLevel level, BlockPos pos, Direction supportDirection) {
        return level.getBlockState(pos).canBeReplaced() && hasSupport(level, pos, supportDirection);
    }

    private static boolean hasSupport(WorldGenLevel level, BlockPos pos, Direction supportDirection) {
        BlockPos supportPos = pos.relative(supportDirection);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, supportDirection.getOpposite());
    }

    private static BlockState createLichenState(Block lichenBlock, Direction supportDirection) {
        return lichenBlock.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(supportDirection), true);
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }

    private record Anchor(BlockPos pos, Direction supportDirection) {
    }
}
