package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.SpitBugEggBlock;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record SpitBugEggPatchFeature() implements Feature {
    private static final Identifier SPIT_BUG_EGG_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "spit_bug_egg");
    private static final int SEARCH_RADIUS = 7;
    private static final int VERTICAL_SCAN = 18;
    private static final int SEARCH_ATTEMPTS = 32;
    private static final int MIN_PLACEMENTS = 2;
    private static final int MAX_PLACEMENTS = 5;

    public static final MapCodec<SpitBugEggPatchFeature> CODEC = MapCodec.unit(SpitBugEggPatchFeature::new);

    @Override
    public MapCodec<SpitBugEggPatchFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Block eggBlock = getBlock(SPIT_BUG_EGG_ID);
        if (eggBlock == null) {
            return false;
        }


        boolean placedAny = false;
        int placements = 0;
        int targetPlacements = MIN_PLACEMENTS + random.nextInt(MAX_PLACEMENTS - MIN_PLACEMENTS + 1);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS && placements < targetPlacements; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int startY = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            int minY = Math.max(level.getMinY() + 1, startY - VERTICAL_SCAN);
            int maxY = Math.min(level.getMaxY(), startY + VERTICAL_SCAN);

            BlockPos spot = null;
            for (int y = minY; y <= maxY; y++) {
                mutable.set(x, y, z);
                if (isValidFloorSpot(level, mutable)) {
                    spot = mutable.immutable();
                    break;
                }
            }

            if (spot == null) {
                continue;
            }
            BlockState eggState = eggBlock.defaultBlockState()
                    .setValue(SpitBugEggBlock.ROTATED, random.nextBoolean());
            level.setBlock(spot, eggState, 3);
            placedAny = true;
            placements++;
        }

        return placedAny;
    }

    private static boolean isValidFloorSpot(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir()) {
            return false;
        }

        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    private static Block getBlock(Identifier id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
