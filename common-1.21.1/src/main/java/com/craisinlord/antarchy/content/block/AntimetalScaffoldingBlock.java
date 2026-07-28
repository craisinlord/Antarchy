package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AntimetalScaffoldingBlock extends ScaffoldingBlock {

    public static final MapCodec<AntimetalScaffoldingBlock> CODEC = simpleCodec(AntimetalScaffoldingBlock::new);
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    private static final VoxelShape MIRRORED_STABLE_SHAPE;
    private static final VoxelShape MIRRORED_UNSTABLE_SHAPE;

    static {
        VoxelShape frame = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
        VoxelShape post1 = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 2.0D);
        VoxelShape post2 = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
        VoxelShape post3 = Block.box(0.0D, 0.0D, 14.0D, 2.0D, 16.0D, 16.0D);
        VoxelShape post4 = Block.box(14.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
        MIRRORED_STABLE_SHAPE = Shapes.or(frame, post1, post2, post3, post4);

        VoxelShape topSlab = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        VoxelShape edge1 = Block.box(0.0D, 14.0D, 0.0D, 2.0D, 16.0D, 16.0D);
        VoxelShape edge2 = Block.box(14.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        VoxelShape edge3 = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 2.0D);
        VoxelShape edge4 = Block.box(0.0D, 14.0D, 14.0D, 16.0D, 16.0D, 16.0D);
        MIRRORED_UNSTABLE_SHAPE = Shapes.or(topSlab, MIRRORED_STABLE_SHAPE, edge1, edge2, edge3, edge4);
    }

    public AntimetalScaffoldingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DISTANCE, 7)
                .setValue(WATERLOGGED, false)
                .setValue(BOTTOM, false)
                .setValue(INVERTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INVERTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        boolean inverted = context.getPlayer() != null && AntarchyGravityApi.isGravityInverted(context.getPlayer());
        int distance = getDistance(level, pos, inverted);
        if (inverted && distance >= 7) {
            return null;
        }
        return this.defaultBlockState()
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER)
                .setValue(DISTANCE, distance)
                .setValue(BOTTOM, antarchy$isBottom(level, pos, distance, inverted))
                .setValue(INVERTED, inverted);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return getDistance(level, pos, state.getValue(INVERTED)) < 7;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean inverted = state.getValue(INVERTED);
        int distance = getDistance(level, pos, inverted);
        BlockState updated = state.setValue(DISTANCE, distance).setValue(BOTTOM, antarchy$isBottom(level, pos, distance, inverted));
        if (updated.getValue(DISTANCE) == 7) {
            if (state.getValue(DISTANCE) == 7) {
                if (inverted) {
                    UpwardFallingBlockEntity.fallUp(level, pos, updated);
                } else {
                    FallingBlockEntity.fall(level, pos, updated);
                }
            } else {
                level.destroyBlock(pos, true);
            }
        } else if (state != updated) {
            level.setBlock(pos, updated, 3);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(INVERTED)) {
            return super.getShape(state, level, pos, context);
        }
        if (context.isHoldingItem(state.getBlock().asItem())) {
            return Shapes.block();
        }
        return state.getValue(BOTTOM) ? MIRRORED_UNSTABLE_SHAPE : MIRRORED_STABLE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(INVERTED)) {
            return super.getCollisionShape(state, level, pos, context);
        }
        return this.getShape(state, level, pos, context);
    }

    private static boolean antarchy$isMatchingScaffold(BlockState state, boolean inverted) {
        return state.getBlock() instanceof AntimetalScaffoldingBlock && state.getValue(INVERTED) == inverted;
    }

    public static int getDistance(LevelReader level, BlockPos pos, boolean inverted) {
        Direction supportDirection = inverted ? Direction.UP : Direction.DOWN;
        BlockPos.MutableBlockPos cursor = pos.mutable().move(supportDirection);
        BlockState supportNeighbor = level.getBlockState(cursor);
        int distance = 7;
        if (antarchy$isMatchingScaffold(supportNeighbor, inverted)) {
            distance = supportNeighbor.getValue(DISTANCE);
        } else if (supportNeighbor.isFaceSturdy(level, cursor, supportDirection.getOpposite())) {
            return 0;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos.MutableBlockPos neighborPos = pos.mutable().move(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (!antarchy$isMatchingScaffold(neighbor, inverted)) {
                continue;
            }
            distance = Math.min(distance, neighbor.getValue(DISTANCE) + 1);
            if (distance == 1) {
                break;
            }
        }

        return distance;
    }

    private static boolean antarchy$isBottom(LevelReader level, BlockPos pos, int distance, boolean inverted) {
        if (distance <= 0) {
            return false;
        }
        Direction supportDirection = inverted ? Direction.UP : Direction.DOWN;
        return !antarchy$isMatchingScaffold(level.getBlockState(pos.relative(supportDirection)), inverted);
    }
}
