package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.block.CranberryLeavesBlock;
import com.craisinlord.antarchy.content.block.ElythiaPalette;
import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CranberryTreeFeature extends Feature<NoneFeatureConfiguration> {

    public CranberryTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        return placeTree(level, origin, random);
    }

    public static boolean placeTree(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockState ground = level.getBlockState(origin.below());
        if (!ground.is(BlockTags.DIRT) && !ground.is(Blocks.FARMLAND) && !ground.is(Blocks.MOSS_BLOCK)) {
            return false;
        }
        CranberryTreeShape.Shape shape = CranberryTreeShape.create(origin, random);
        if (!shape.fits(pos -> canReplace(level, pos))) {
            return false;
        }
        Map<BlockPos, Direction.Axis> logs = shape.logs();
        Set<BlockPos> leaves = shape.leaves();
        Map<BlockPos, Integer> distances = shape.distances();
        BlockState log = ElythiaPalette.block("cranberry_log").defaultBlockState();
        for (Map.Entry<BlockPos, Direction.Axis> entry : logs.entrySet()) {
            level.setBlock(entry.getKey(), log.setValue(RotatedPillarBlock.AXIS, entry.getValue()), Block.UPDATE_CLIENTS);
        }
        BlockState leaf = ElythiaPalette.block("cranberry_leaves").defaultBlockState();
        for (BlockPos pos : leaves) {
            Integer distance = distances.get(pos);
            if (distance == null) {
                continue;
            }
            boolean exposed = Direction.Plane.HORIZONTAL.stream().anyMatch(direction -> !leaves.contains(pos.relative(direction)) && !logs.containsKey(pos.relative(direction)));
            boolean hanging = !leaves.contains(pos.below()) && !logs.containsKey(pos.below());
            boolean fruiting = (exposed || hanging) && random.nextInt(3) == 0;
            level.setBlock(pos, leaf.setValue(LeavesBlock.DISTANCE, distance).setValue(CranberryLeavesBlock.FRUITING, fruiting), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        if (pos.getY() < level.getMinY() || pos.getY() >= level.getMaxY() + 1 || level instanceof WorldGenRegion region && !region.ensureCanWrite(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty() && (state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES)) && !state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES);
    }
}
