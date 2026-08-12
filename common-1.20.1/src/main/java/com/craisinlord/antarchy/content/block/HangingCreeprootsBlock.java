package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingCreeprootsBlock extends BushBlock {
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, 4);
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty STUNTED = BooleanProperty.create("stunted");
    private static final int MAX_DISTANCE = 4;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public HangingCreeprootsBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, 0).setValue(BOTTOM, true).setValue(STUNTED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(this) || state.isFaceSturdy(level, pos, Direction.DOWN);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        return this.mayPlaceOn(level.getBlockState(abovePos), level, abovePos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.DOWN) {
            boolean bottom = !neighborState.is(this);
            if (bottom != state.getValue(BOTTOM)) {
                state = state.setValue(BOTTOM, bottom);
            }
        }
        return state;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(BOTTOM) && !state.getValue(STUNTED) && state.getValue(DISTANCE) < MAX_DISTANCE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(STUNTED) || !state.getValue(BOTTOM) || state.getValue(DISTANCE) >= MAX_DISTANCE) {
            return;
        }

        BlockPos belowPos = pos.below();
        if (random.nextInt(4) != 0 || !level.getBlockState(belowPos).isAir()) {
            return;
        }

        int distance = state.getValue(DISTANCE);
        level.setBlock(pos, state.setValue(BOTTOM, false), Block.UPDATE_CLIENTS);
        level.setBlock(belowPos, this.defaultBlockState().setValue(DISTANCE, distance + 1).setValue(BOTTOM, true), Block.UPDATE_ALL);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.SHEARS) || state.getValue(STUNTED)) {
            return super.use(state, level, pos, player, hand, hitResult);
        }

        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(STUNTED, true), Block.UPDATE_ALL);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, BOTTOM, STUNTED);
    }
}
