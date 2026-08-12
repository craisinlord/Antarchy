package com.craisinlord.antarchy.content.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class BluestoneTorchBlock extends Block implements BluestoneSignalSource {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape CEILING_SHAPE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final Map<Direction, VoxelShape> WALL_SHAPES = Map.of(
            Direction.NORTH, Block.box(5.5D, 3.0D, 11.0D, 10.5D, 13.0D, 16.0D),
            Direction.SOUTH, Block.box(5.5D, 3.0D, 0.0D, 10.5D, 13.0D, 5.0D),
            Direction.WEST, Block.box(11.0D, 3.0D, 5.5D, 16.0D, 13.0D, 10.5D),
            Direction.EAST, Block.box(0.0D, 3.0D, 5.5D, 5.0D, 13.0D, 10.5D)
    );
    private static final DustParticleOptions BLUESTONE_PARTICLE = new DustParticleOptions(new Vector3f(0.18F, 0.54F, 1.0F), 1.0F);
    private static final Map<Level, List<Toggle>> RECENT_TOGGLES = new WeakHashMap<>();

    public BluestoneTorchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(LIT, true)
                .setValue(FACE, AttachFace.CEILING)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACE) == AttachFace.WALL ? WALL_SHAPES.get(state.getValue(FACING)) : CEILING_SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return switch (state.getValue(FACE)) {
            case CEILING -> BluestonePlacementHelper.hasCeilingSupport(level, pos);
            case WALL -> this.hasWallSupport(level, pos, state.getValue(FACING));
            default -> false;
        };
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction == Direction.DOWN && BluestonePlacementHelper.hasCeilingSupport(level, pos)) {
                return this.defaultBlockState().setValue(FACE, AttachFace.CEILING);
            }
            if (direction.getAxis().isHorizontal()) {
                Direction facing = direction.getOpposite();
                BlockState state = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, facing);
                if (state.canSurvive(level, pos)) {
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (this.isDirectlyPoweringRedstoneWire(level, pos, direction)) {
            return 0;
        }
        if (this.isBlockedOutputDirection(state, direction)) {
            return 0;
        }
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!state.getValue(LIT)) {
            return 0;
        }
        if (this.isDirectlyPoweringRedstoneWire(level, pos, direction)) {
            return 0;
        }
        if (state.getValue(FACE) == AttachFace.WALL) {
            return direction == Direction.UP ? 15 : 0;
        }
        return this.isBlockedOutputDirection(state, direction) ? 0 : 15;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        BluestoneSignalHelper.updateBluestoneNeighbors(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock())) {
            BluestoneSignalHelper.updateBluestoneNeighbors(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!level.isClientSide) {
            boolean powered = this.hasInputSignal(level, pos, state);
            if (state.getValue(LIT) == powered) {
                level.scheduleTick(pos, this, 2);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean powered = this.hasInputSignal(level, pos, state);
        boolean lit = state.getValue(LIT);

        if (lit) {
            if (powered) {
                level.setBlock(pos, state.setValue(LIT, false), 3);
                if (this.isToggledTooFrequently(level, pos, true)) {
                    level.levelEvent(1502, pos, 0);
                    level.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
                    level.scheduleTick(pos, this, 160);
                }
            }
        } else if (!powered && !this.isToggledTooFrequently(level, pos, false)) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + (state.getValue(FACE) == AttachFace.CEILING ? 0.3D : 0.35D);
        double z = pos.getZ() + 0.5D;
        if (state.getValue(FACE) == AttachFace.WALL) {
            Direction facing = state.getValue(FACING).getOpposite();
            x += 0.27D * facing.getStepX();
            z += 0.27D * facing.getStepZ();
        }
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(BLUESTONE_PARTICLE, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACE, FACING);
    }

    @Override
    public int getBluestoneSignal(LevelReader level, BlockPos pos, BlockState state, Direction direction) {
        return this.isBlockedOutputDirection(state, direction) ? 0 : state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.getValue(FACE) == AttachFace.WALL ? state.setValue(FACING, rotation.rotate(state.getValue(FACING))) : state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.getValue(FACE) == AttachFace.WALL ? state.rotate(mirror.getRotation(state.getValue(FACING))) : state;
    }

    private boolean hasInputSignal(Level level, BlockPos pos, BlockState state) {
        Direction supportDirection = state.getValue(FACE) == AttachFace.CEILING ? Direction.UP : state.getValue(FACING).getOpposite();
        return level.hasSignal(pos.relative(supportDirection), supportDirection);
    }

    private boolean hasWallSupport(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    private boolean isBlockedOutputDirection(BlockState state, Direction direction) {
        return this.getBlockedOutputDirection(state) == direction;
    }

    private boolean isDirectlyPoweringRedstoneWire(BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos.relative(direction.getOpposite())).is(Blocks.REDSTONE_WIRE);
    }

    private Direction getBlockedOutputDirection(BlockState state) {
        return state.getValue(FACE) == AttachFace.WALL ? state.getValue(FACING) : Direction.DOWN;
    }

    private boolean isToggledTooFrequently(Level level, BlockPos pos, boolean addNew) {
        List<Toggle> toggles = RECENT_TOGGLES.computeIfAbsent(level, ignored -> new ArrayList<>());
        if (addNew) {
            toggles.add(new Toggle(pos.immutable(), level.getGameTime()));
        }

        long cutoff = level.getGameTime() - 60L;
        toggles.removeIf(toggle -> toggle.gameTime < cutoff);

        int count = 0;
        for (Toggle toggle : toggles) {
            if (toggle.pos.equals(pos)) {
                count++;
                if (count >= 8) {
                    return true;
                }
            }
        }
        return false;
    }

    private record Toggle(BlockPos pos, long gameTime) {
    }
}
