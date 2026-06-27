package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CavarynCreepvineFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation CREEPVINE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "creepvine");
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int VERTICAL_SCAN = 12;

    public CavarynCreepvineFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Block creepvineBlock = getBlock(CREEPVINE_ID);
        if (!(creepvineBlock instanceof VineBlock vineBlock)) {
            return false;
        }

        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Anchor anchor = findAnchor(level, context.origin(), random);
        if (anchor == null) {
            return false;
        }

        return placeVineColumn(level, vineBlock, anchor, random);
    }

    private static Anchor findAnchor(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int startY = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            startY = Math.max(level.getMinBuildHeight() + 3, Math.min(level.getMaxBuildHeight() - 3, startY));

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

    private static boolean placeVineColumn(WorldGenLevel level, VineBlock vineBlock, Anchor anchor, RandomSource random) {
        boolean placedAny = false;
        int length = 3 + random.nextInt(6);

        for (int offset = 0; offset < length; offset++) {
            BlockPos vinePos = anchor.pos().below(offset);
            if (vinePos.getY() <= level.getMinBuildHeight()) {
                break;
            }

            BlockState existing = level.getBlockState(vinePos);
            if (!existing.canBeReplaced() && !existing.is(vineBlock)) {
                break;
            }

            BlockState vineState = createVineState(vineBlock, anchor.supportDirection());
            if (vineState == null || !vineState.canSurvive(level, vinePos)) {
                break;
            }

            level.setBlock(vinePos, vineState, 3);
            placedAny = true;
        }

        return placedAny;
    }

    private static BlockState createVineState(VineBlock vineBlock, Direction attachedFace) {
        BlockState state = vineBlock.defaultBlockState();
        return switch (attachedFace) {
            case NORTH -> state.setValue(VineBlock.NORTH, Boolean.TRUE);
            case SOUTH -> state.setValue(VineBlock.SOUTH, Boolean.TRUE);
            case EAST -> state.setValue(VineBlock.EAST, Boolean.TRUE);
            case WEST -> state.setValue(VineBlock.WEST, Boolean.TRUE);
            default -> null;
        };
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }

    private record Anchor(BlockPos pos, Direction supportDirection) {
    }
}
