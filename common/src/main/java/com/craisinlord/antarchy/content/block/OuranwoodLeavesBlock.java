package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class OuranwoodLeavesBlock extends LeavesBlock implements BonemealableBlock {
    public static final MapCodec<OuranwoodLeavesBlock> CODEC = Block.simpleCodec(OuranwoodLeavesBlock::new);

    /*
     * We still include vanilla LeavesBlock.DISTANCE in the state definition because
     * LeavesBlock's constructor sets it.
     *
     * This is the real Ouranwood decay distance.
     */
    public static final IntegerProperty OURANWOOD_DISTANCE = IntegerProperty.create("ouranwood_distance", 1, 17);

    public static final BooleanProperty PERSISTENT = LeavesBlock.PERSISTENT;
    public static final BooleanProperty WATERLOGGED = LeavesBlock.WATERLOGGED;

    public static final int OURANWOOD_MAX_DISTANCE = 16;
    public static final int OURANWOOD_DECAY_DISTANCE = OURANWOOD_MAX_DISTANCE + 1;

    public OuranwoodLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LeavesBlock.DISTANCE, LeavesBlock.DECAY_DISTANCE)
                .setValue(OURANWOOD_DISTANCE, OURANWOOD_DECAY_DISTANCE)
                .setValue(PERSISTENT, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public MapCodec<OuranwoodLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LeavesBlock.DISTANCE, OURANWOOD_DISTANCE, PERSISTENT, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());

        BlockState state = this.defaultBlockState()
                .setValue(PERSISTENT, true)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        return recalculateDistance(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return decaying(state);
    }

    @Override
    protected boolean decaying(BlockState state) {
        return !state.getValue(PERSISTENT)
                && state.getValue(OURANWOOD_DISTANCE) > OURANWOOD_MAX_DISTANCE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (decaying(state)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, recalculateDistance(state, level, pos), Block.UPDATE_ALL);
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
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        int neighborDistance = Math.min(OURANWOOD_DECAY_DISTANCE, getDistanceAt(neighborState) + 1);
        int currentDistance = state.getValue(OURANWOOD_DISTANCE);

        if (neighborDistance != 1 || currentDistance != neighborDistance) {
            level.scheduleTick(pos, this, 1);
        }

        return state;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return this.canGrowHangingAcorn(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (this.canGrowHangingAcorn(level, pos)) {
            level.setBlock(pos.below(), createHangingAcornState(0), Block.UPDATE_ALL);
        }
    }

    private boolean canGrowHangingAcorn(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isAir();
    }

    public static BlockState createHangingAcornState(int stage) {
        return AntarchyObjects.OURANWOOD_ACORN_BLOCK.get().defaultBlockState()
                .setValue(OuranwoodAcornBlock.HANGING, true)
                .setValue(OuranwoodAcornBlock.STAGE, stage);
    }

    private static BlockState recalculateDistance(BlockState state, LevelReader level, BlockPos pos) {
        int bestDistance = OURANWOOD_DECAY_DISTANCE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values()) {
            cursor.setWithOffset(pos, direction);
            bestDistance = Math.min(bestDistance, getDistanceAt(level.getBlockState(cursor)) + 1);

            if (bestDistance == 1) {
                break;
            }
        }

        bestDistance = Math.min(OURANWOOD_DECAY_DISTANCE, bestDistance);
        return setOuranwoodDistance(state, bestDistance);
    }

    private static BlockState setOuranwoodDistance(BlockState state, int distance) {
        int ouranwoodDistance = Math.max(1, Math.min(OURANWOOD_DECAY_DISTANCE, distance));
        int vanillaDistance = Math.min(LeavesBlock.DECAY_DISTANCE, ouranwoodDistance);

        return state
                .setValue(OURANWOOD_DISTANCE, ouranwoodDistance)
                .setValue(LeavesBlock.DISTANCE, vanillaDistance);
    }

    private static int getDistanceAt(BlockState state) {
        if (state.is(BlockTags.LOGS)) {
            return 0;
        }
        if (state.hasProperty(OURANWOOD_DISTANCE)) {
            return state.getValue(OURANWOOD_DISTANCE);
        }
        if (state.hasProperty(LeavesBlock.DISTANCE)) {
            return state.getValue(LeavesBlock.DISTANCE);
        }

        return OURANWOOD_DECAY_DISTANCE;
    }

    public static BlockState setOuranwoodDistanceForWorldgen(BlockState state, int distance) {
        int ouranwoodDistance = Math.max(1, Math.min(OURANWOOD_DECAY_DISTANCE, distance));
        int vanillaDistance = Math.min(LeavesBlock.DECAY_DISTANCE, ouranwoodDistance);

        return state
                .setValue(OURANWOOD_DISTANCE, ouranwoodDistance)
                .setValue(LeavesBlock.DISTANCE, vanillaDistance);
    }
}