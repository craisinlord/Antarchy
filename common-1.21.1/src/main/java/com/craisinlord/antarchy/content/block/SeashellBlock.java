package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class SeashellBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<SeashellBlock> CODEC = Block.simpleCodec(SeashellBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final double SHELL_MIN = 1.0D;
    private static final double SHELL_MAX = 15.0D;
    private static final double SHELL_HEIGHT = 4.333333D;
    private static final double COLLISION_FORWARD_OFFSET = 1.0D;
    private static final VoxelShape OUTLINE_SHAPE = Block.box(SHELL_MIN, 0.0D, SHELL_MIN, SHELL_MAX, SHELL_HEIGHT, SHELL_MAX);

    public SeashellBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public MapCodec<SeashellBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(POWERED, powered)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return collisionShape(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
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

        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (level instanceof Level actualLevel && !actualLevel.isClientSide) {
            this.updatePowerState(actualLevel, pos, state);
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block block,
            BlockPos neighborPos,
            boolean movedByPiston
    ) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            this.updatePowerState(level, pos, state);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            this.updatePowerState(level, pos, state);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SeashellBlockEntity seashell) {
            seashell.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(pos) instanceof SeashellBlockEntity seashell)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            if (seashell.closeManually()) {
                level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.6F, 0.95F + level.random.nextFloat() * 0.1F);
                return ItemInteractionResult.CONSUME;
            }

            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!seashell.isEffectivelyOpen()) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            if (seashell.openManually()) {
                level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.6F, 0.95F + level.random.nextFloat() * 0.1F);
                return ItemInteractionResult.CONSUME;
            }

            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.isEmpty() && !seashell.hasAnyContents()) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            if (seashell.closeManually()) {
                level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.6F, 0.95F + level.random.nextFloat() * 0.1F);
                return ItemInteractionResult.CONSUME;
            }

            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!stack.isEmpty()) {
            if (level.isClientSide) {
                return seashell.canAcceptItem(player, stack) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            if (seashell.tryInsert(player, hand, stack)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.6F, 0.95F + level.random.nextFloat() * 0.1F);
                return ItemInteractionResult.CONSUME;
            }

            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return seashell.hasAnyContents() ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (seashell.tryRemove(player, hand)) {
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.6F, 0.95F + level.random.nextFloat() * 0.1F);
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeashellBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? createTickerHelper(blockEntityType, AntarchyObjects.SEASHELL_BLOCK_ENTITY.get(), SeashellBlockEntity::clientTick)
                : createTickerHelper(blockEntityType, AntarchyObjects.SEASHELL_BLOCK_ENTITY.get(), SeashellBlockEntity::serverTick);
    }

    private void updatePowerState(Level level, BlockPos pos, BlockState state) {
        boolean powered = level.hasNeighborSignal(pos);
        if (state.getValue(POWERED) == powered) {
            if (level.getBlockEntity(pos) instanceof SeashellBlockEntity seashell) {
                seashell.syncVisualState(powered);
            }
            return;
        }

        BlockState updatedState = state.setValue(POWERED, powered);
        level.setBlock(pos, updatedState, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof SeashellBlockEntity seashell) {
            if (seashell.onPowerStateChanged(powered)) {
                level.playSound(null, pos, powered ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.6F, 0.95F + level.random.nextFloat() * 0.1F);
            }
        }
    }

    private static VoxelShape collisionShape(Direction facing) {
        return switch (facing) {
            case NORTH -> Block.box(SHELL_MIN, 0.0D, SHELL_MIN - COLLISION_FORWARD_OFFSET, SHELL_MAX, SHELL_HEIGHT, SHELL_MAX - COLLISION_FORWARD_OFFSET);
            case SOUTH -> Block.box(SHELL_MIN, 0.0D, SHELL_MIN + COLLISION_FORWARD_OFFSET, SHELL_MAX, SHELL_HEIGHT, SHELL_MAX + COLLISION_FORWARD_OFFSET);
            case WEST -> Block.box(SHELL_MIN - COLLISION_FORWARD_OFFSET, 0.0D, SHELL_MIN, SHELL_MAX - COLLISION_FORWARD_OFFSET, SHELL_HEIGHT, SHELL_MAX);
            case EAST -> Block.box(SHELL_MIN + COLLISION_FORWARD_OFFSET, 0.0D, SHELL_MIN, SHELL_MAX + COLLISION_FORWARD_OFFSET, SHELL_HEIGHT, SHELL_MAX);
            default -> OUTLINE_SHAPE;
        };
    }
}
