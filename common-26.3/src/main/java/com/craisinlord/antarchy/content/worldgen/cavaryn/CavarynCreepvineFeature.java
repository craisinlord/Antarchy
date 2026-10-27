package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record CavarynCreepvineFeature() implements Feature {
    private static final Identifier CREEPVINE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "creepvine");
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int VERTICAL_SCAN = 12;

    public static final MapCodec<CavarynCreepvineFeature> CODEC = MapCodec.unit(CavarynCreepvineFeature::new);

    @Override
    public MapCodec<CavarynCreepvineFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Block creepvineBlock = getBlock(CREEPVINE_ID);
        if (!(creepvineBlock instanceof VineBlock vineBlock)) {
            return false;
        }

        Anchor anchor = findAnchor(level, origin, random);
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
            startY = Math.max(level.getMinY() + 3, Math.min(level.getMaxY() - 2, startY));

            for (int y = startY; y >= Math.max(level.getMinY() + 1, startY - VERTICAL_SCAN); y--) {
                mutable.set(x, y, z);
                if (!level.getBlockState(mutable).canBeReplaced()) {
                    continue;
                }
                if (AntarchyFluidChecks.hasBileNearby(level, mutable, 1)) {
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
            if (vinePos.getY() <= level.getMinY()) {
                break;
            }

            BlockState existing = level.getBlockState(vinePos);
            if (!existing.canBeReplaced() && !existing.is(vineBlock)) {
                break;
            }
            if (AntarchyFluidChecks.hasBileNearby(level, vinePos, 1)) {
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

    private static Block getBlock(Identifier id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }

    private record Anchor(BlockPos pos, Direction supportDirection) {
    }
}
