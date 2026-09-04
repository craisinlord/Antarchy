package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.SimpleDirectionalVineBlock;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Places the complete natural-grown shape so every segment gets the body state. */
public final class NaturalDirectionalVineFeature extends Feature<NoneFeatureConfiguration> {
    private final ResourceLocation blockId;
    private final Direction direction;
    private final int minimumLength;
    private final int maximumLength;

    public NaturalDirectionalVineFeature(Codec<NoneFeatureConfiguration> codec, String blockName,
                                         Direction direction, int minimumLength, int maximumLength) {
        super(codec);
        this.blockId = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, blockName);
        this.direction = direction;
        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        Block block = BuiltInRegistries.BLOCK.get(this.blockId);
        RandomSource random = context.random();
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
