package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.SpringbugEggBlock;
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

public record SpringbugEggCeilingFeature() implements Feature {
    private static final Identifier SPRINGBUG_EGG_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "springbug_egg");
    private static final int SEARCH_RADIUS = 8;
    private static final int VERTICAL_SCAN = 24;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int MAX_PLACEMENTS = 3;

    public static final MapCodec<SpringbugEggCeilingFeature> CODEC = MapCodec.unit(SpringbugEggCeilingFeature::new);

    @Override
    public MapCodec<SpringbugEggCeilingFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Block eggBlock = getBlock(SPRINGBUG_EGG_ID);
        if (eggBlock == null) {
            return false;
        }


        boolean placedAny = false;
        int placements = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS && placements < MAX_PLACEMENTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int startY = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            int minY = Math.max(level.getMinY() + 1, startY - VERTICAL_SCAN);
            int maxY = Math.min(level.getMaxY() - 1, startY + VERTICAL_SCAN);

            BlockPos spot = null;
            for (int y = maxY; y >= minY; y--) {
                mutable.set(x, y, z);
                if (isValidCeilingSpot(level, mutable)) {
                    spot = mutable.immutable();
                    break;
                }
            }

            if (spot == null) {
                continue;
            }
            BlockState eggState = eggBlock.defaultBlockState()
                    .setValue(SpringbugEggBlock.HANGING, true)
                    .setValue(SpringbugEggBlock.ROTATED, random.nextBoolean());
            level.setBlock(spot, eggState, 3);
            placedAny = true;
            placements++;
        }

        return placedAny;
    }

    private static boolean isValidCeilingSpot(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir()) {
            return false;
        }

        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        return aboveState.isFaceSturdy(level, abovePos, Direction.DOWN);
    }

    private static Block getBlock(Identifier id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
