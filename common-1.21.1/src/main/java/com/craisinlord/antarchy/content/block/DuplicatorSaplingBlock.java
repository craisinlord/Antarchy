package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DuplicatorSaplingBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<DuplicatorSaplingBlock> CODEC = Block.simpleCodec(DuplicatorSaplingBlock::new);
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);

    public DuplicatorSaplingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    public MapCodec<DuplicatorSaplingBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!AntarchySettings.duplicatorTreeEnabled()) {
            return;
        }

        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            this.advanceTree(level, pos, state, random);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.75F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (!AntarchySettings.duplicatorTreeEnabled()) {
            return;
        }

        this.advanceTree(level, pos, state, random);
    }

    private void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.setValue(STAGE, 1), Block.UPDATE_ALL);
            return;
        }

        this.growTree(level, pos);
    }

    private void growTree(ServerLevel level, BlockPos pos) {
        if (!this.canGenerate(level, pos)) {
            return;
        }

        BlockState logState = AntarchyObjects.DUPLICATOR_LOG.get().defaultBlockState();
        BlockState leafState = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        for (int y = 0; y < 4; y++) {
            level.setBlock(pos.above(y), logState, Block.UPDATE_ALL);
        }

        BlockPos canopyBase = pos.above(3);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 && Math.abs(z) == 2) {
                    continue;
                }

                this.placeLeaves(level, canopyBase.offset(x, 0, z), leafState);
                this.placeLeaves(level, canopyBase.offset(x, 1, z), leafState);
            }
        }

        this.placeLeaves(level, canopyBase.above(2), leafState);
        this.placeLeaves(level, canopyBase.above(1).north(), leafState);
        this.placeLeaves(level, canopyBase.above(1).south(), leafState);
        this.placeLeaves(level, canopyBase.above(1).east(), leafState);
        this.placeLeaves(level, canopyBase.above(1).west(), leafState);
    }

    private void placeLeaves(ServerLevel level, BlockPos pos, BlockState leafState) {
        BlockState existingState = level.getBlockState(pos);
        if (existingState.isAir() || existingState.canBeReplaced()) {
            level.setBlock(pos, leafState, Block.UPDATE_ALL);
        }
    }

    private boolean canGenerate(ServerLevel level, BlockPos pos) {
        for (int y = 0; y <= 6; y++) {
            int radius = y >= 3 ? 2 : 1;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (!this.canReplaceForTree(pos, checkPos, checkState)) {
                        return false;
                    }
                }
            }
        }

        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private boolean canReplaceForTree(BlockPos saplingPos, BlockPos checkPos, BlockState checkState) {
        if (checkPos.equals(saplingPos) && checkState.is(this)) {
            return true;
        }

        return checkState.isAir() || checkState.canBeReplaced();
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }
}
