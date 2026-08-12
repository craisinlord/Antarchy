package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

public class PeachLeavesBlock extends LeavesBlock implements BonemealableBlock {
    private static final ResourceLocation HANGING_PEACH_ID = new ResourceLocation(Antarchy.MODID, "hanging_peach");
    public static final IntegerProperty PEACH_DISTANCE = IntegerProperty.create("peach_distance", 1, 13);
    public static final BooleanProperty PERSISTENT = LeavesBlock.PERSISTENT;
    public static final BooleanProperty WATERLOGGED = LeavesBlock.WATERLOGGED;
    public static final int PEACH_MAX_DISTANCE = 12;
    public static final int PEACH_DECAY_DISTANCE = PEACH_MAX_DISTANCE + 1;

    public PeachLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DISTANCE, DECAY_DISTANCE)
                .setValue(PEACH_DISTANCE, PEACH_DECAY_DISTANCE)
                .setValue(PERSISTENT, false)
                .setValue(WATERLOGGED, false));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LeavesBlock.DISTANCE, PEACH_DISTANCE, PERSISTENT, WATERLOGGED);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return decaying(state);
    }

    @Override
    protected boolean decaying(BlockState state) {
        return !state.getValue(PERSISTENT) && state.getValue(PEACH_DISTANCE) > PEACH_MAX_DISTANCE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (decaying(state)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, recalculateDistance(state, level, pos), Block.UPDATE_ALL);
    }

    @Override
    public BlockState updateShape(
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

        int neighborDistance = Math.min(PEACH_DECAY_DISTANCE, getDistanceAt(neighborState) + 1);
        int currentDistance = state.getValue(PEACH_DISTANCE);

        if (neighborDistance != 1 || currentDistance != neighborDistance) {
            level.scheduleTick(pos, this, 1);
        }

        return state;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(18) != 0) {
            return;
        }

        level.addParticle(
                AntarchyObjects.PEACH_LEAVES_PARTICLE.get(),
                pos.getX() + random.nextDouble(),
                pos.getY() + random.nextDouble(),
                pos.getZ() + random.nextDouble(),
                0.0D,
                -0.02D,
                0.0D
        );
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return level.getBlockState(pos.below()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos.below()).isAir()) {
            return;
        }

        level.setBlock(pos.below(), createHangingPeachState(), Block.UPDATE_ALL);
    }

    public static BlockState createHangingPeachState() {
        return BuiltInRegistries.BLOCK.getOptional(HANGING_PEACH_ID)
                .map(Block::defaultBlockState)
                .orElseThrow(() -> new IllegalStateException("Missing hanging peach block: " + HANGING_PEACH_ID));
    }

    private static BlockState recalculateDistance(BlockState state, LevelReader level, BlockPos pos) {
        int bestDistance = PEACH_DECAY_DISTANCE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values()) {
            cursor.setWithOffset(pos, direction);
            bestDistance = Math.min(bestDistance, getDistanceAt(level.getBlockState(cursor)) + 1);
            if (bestDistance == 1) {
                break;
            }
        }

        return setPeachDistance(state, Math.min(PEACH_DECAY_DISTANCE, bestDistance));
    }

    private static BlockState setPeachDistance(BlockState state, int distance) {
        int peachDistance = Math.max(1, Math.min(PEACH_DECAY_DISTANCE, distance));
        int vanillaDistance = Math.min(LeavesBlock.DECAY_DISTANCE, peachDistance);

        return state
                .setValue(PEACH_DISTANCE, peachDistance)
                .setValue(LeavesBlock.DISTANCE, vanillaDistance);
    }

    private static int getDistanceAt(BlockState state) {
        if (state.is(BlockTags.LOGS)) {
            return 0;
        }
        if (state.hasProperty(PEACH_DISTANCE)) {
            return state.getValue(PEACH_DISTANCE);
        }
        if (state.hasProperty(LeavesBlock.DISTANCE)) {
            return state.getValue(LeavesBlock.DISTANCE);
        }

        return PEACH_DECAY_DISTANCE;
    }

    public static BlockState setPeachDistanceForWorldgen(BlockState state, int distance) {
        int peachDistance = Math.max(1, Math.min(PEACH_DECAY_DISTANCE, distance));
        int vanillaDistance = Math.min(LeavesBlock.DECAY_DISTANCE, peachDistance);

        return state
                .setValue(PEACH_DISTANCE, peachDistance)
                .setValue(LeavesBlock.DISTANCE, vanillaDistance);
    }
}
