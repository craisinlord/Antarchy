package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.SimpleDirectionalVineBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class NaturalDirectionalVineFeature implements Feature {
    public static final MapCodec<NaturalDirectionalVineFeature> SPIRALING_VINES_CODEC =
            MapCodec.unit(() -> new NaturalDirectionalVineFeature("spiraling_vines", Direction.UP, 4, 12));
    public static final MapCodec<NaturalDirectionalVineFeature> GOREVINE_CODEC =
            MapCodec.unit(() -> new NaturalDirectionalVineFeature("gorevine", Direction.DOWN, 8, 16));

    private final Identifier blockId;
    private final Direction direction;
    private final int minimumLength;
    private final int maximumLength;

    private NaturalDirectionalVineFeature(String blockName, Direction direction, int minimumLength, int maximumLength) {
        this.blockId = Identifier.fromNamespaceAndPath(Antarchy.MODID, blockName);
        this.direction = direction;
        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
    }

    @Override
    public MapCodec<NaturalDirectionalVineFeature> codec() {
        return this.direction == Direction.UP ? SPIRALING_VINES_CODEC : GOREVINE_CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Block block = BuiltInRegistries.BLOCK.getValue(this.blockId);
        int length = Mth.randomBetweenInclusive(random, this.minimumLength, this.maximumLength);
        boolean placed = false;

        for (int index = 0; index < length; index++) {
            BlockPos pos = origin.relative(this.direction, index);
            if (!level.getBlockState(pos).isAir()) {
                break;
            }

            BlockState state = block.defaultBlockState()
                    .setValue(SimpleDirectionalVineBlock.GROWTH_DIRECTION, this.direction)
                    .setValue(SimpleDirectionalVineBlock.DISTANCE, Math.min(index, 15))
                    .setValue(SimpleDirectionalVineBlock.TOP_CAP, this.direction == Direction.UP && index == length - 1)
                    .setValue(SimpleDirectionalVineBlock.BOTTOM_CAP, this.direction == Direction.DOWN && index == length - 1);
            if (!state.canSurvive(level, pos)) {
                break;
            }
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            placed = true;
        }
        return placed;
    }
}
