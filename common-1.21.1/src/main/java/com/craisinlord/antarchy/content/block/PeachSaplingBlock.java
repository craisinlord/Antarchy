package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.worldgen.elythia.PeachTreeGrowers;
import com.mojang.serialization.MapCodec;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PeachSaplingBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<PeachSaplingBlock> CODEC = Block.simpleCodec(PeachSaplingBlock::new);

    public PeachSaplingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<PeachSaplingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            this.growConfiguredTree(level, pos, state, random);
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            net.minecraft.core.Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        return state.canSurvive(level, pos)
                ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.45F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.growConfiguredTree(level, pos, state, random);
    }

    private void growConfiguredTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Footprint largeFootprint = this.findFootprint(level, pos);
        if (largeFootprint != null) {
            this.placeConfiguredTree(level, largeFootprint, PeachTreeGrowers.PEACH_LARGE_TREE, random);
            return;
        }

        this.placeConfiguredTree(level, new Footprint(pos, Map.of(pos, state)), PeachTreeGrowers.PEACH_TREE, random);
    }

    private Footprint findFootprint(ServerLevel level, BlockPos pos) {
        for (int offsetX = 0; offsetX < 2; offsetX++) {
            for (int offsetZ = 0; offsetZ < 2; offsetZ++) {
                BlockPos origin = pos.offset(-offsetX, 0, -offsetZ);
                Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
                boolean matches = true;

                for (int x = 0; x < 2 && matches; x++) {
                    for (int z = 0; z < 2; z++) {
                        BlockPos checkPos = origin.offset(x, 0, z);
                        BlockState checkState = level.getBlockState(checkPos);
                        if (!checkState.is(this)) {
                            matches = false;
                            break;
                        }
                        blocks.put(checkPos, checkState);
                    }
                }

                if (matches) {
                    return new Footprint(origin, blocks);
                }
            }
        }

        return null;
    }

    private void placeConfiguredTree(
            ServerLevel level,
            Footprint footprint,
            net.minecraft.resources.ResourceKey<ConfiguredFeature<?, ?>> featureKey,
            RandomSource random
    ) {
        Holder.Reference<ConfiguredFeature<?, ?>> feature = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(featureKey);

        for (BlockPos saplingPos : footprint.blocks().keySet()) {
            level.removeBlock(saplingPos, false);
        }

        if (feature.value().place(level, level.getChunkSource().getGenerator(), random, footprint.origin())) {
            return;
        }

        for (Map.Entry<BlockPos, BlockState> entry : footprint.blocks().entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), Block.UPDATE_ALL);
        }
    }

    private record Footprint(BlockPos origin, Map<BlockPos, BlockState> blocks) {
    }
}
