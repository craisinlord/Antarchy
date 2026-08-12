package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ticks.TickPriority;
import org.joml.Vector3f;

public class BluestoneComparatorBlock extends ComparatorBlock {
    private static final VoxelShape SHAPE = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final DustParticleOptions BLUESTONE_PARTICLE = new DustParticleOptions(new Vector3f(0.18F, 0.54F, 1.0F), 1.0F);
    private final Supplier<? extends BlockEntityType<BluestoneComparatorBlockEntity>> blockEntityTypeSupplier;

    public BluestoneComparatorBlock(BlockBehaviour.Properties properties) {
        this(properties, null);
    }

    public BluestoneComparatorBlock(BlockBehaviour.Properties properties, Supplier<? extends BlockEntityType<BluestoneComparatorBlockEntity>> blockEntityTypeSupplier) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return BluestonePlacementHelper.hasCeilingSupport(level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return BluestonePlacementHelper.canPlaceOnCeiling(context) ? super.getStateForPlacement(context) : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP && !state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BluestoneComparatorBlockEntity(pos, state, this.blockEntityTypeSupplier);
    }

    @Override
    protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof BluestoneComparatorBlockEntity comparator ? comparator.getOutputSignal() : 0;
    }

    @Override
    protected int getInputSignal(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos inputPos = pos.relative(facing);
        BlockState inputState = level.getBlockState(inputPos);
        int signal = BluestoneSignalHelper.getSignalExcludingRedstoneWire(level, inputPos, facing);
        signal = Math.max(signal, this.getDirectBluestoneRearSignal(level, inputPos, inputState, facing));

        if (signal < 15 && inputState.isRedstoneConductor(level, inputPos)) {
            BlockPos fartherInputPos = inputPos.relative(facing);
            BlockState fartherInputState = level.getBlockState(fartherInputPos);
            signal = Math.max(signal, BluestoneSignalHelper.getSignalExcludingRedstoneWire(level, fartherInputPos, facing));
            signal = Math.max(signal, this.getDirectBluestoneRearSignal(level, fartherInputPos, fartherInputState, facing));
        }

        return signal;
    }

    @Override
    protected int getAlternateSignal(SignalGetter level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction clockwise = facing.getClockWise();
        Direction counterClockwise = facing.getCounterClockWise();
        int signal = this.getBluestoneSideSignal(level, pos.relative(clockwise), clockwise.getOpposite());
        signal = Math.max(signal, this.getBluestoneSideSignal(level, pos.relative(counterClockwise), counterClockwise.getOpposite()));
        return signal;
    }

    private int getBluestoneSideSignal(SignalGetter level, BlockPos pos, Direction direction) {
        BlockState neighborState = level.getBlockState(pos);
        if (neighborState.getBlock() instanceof BluestoneSignalSource source) {
            return source.getBluestoneSignal(level instanceof LevelReader levelReader ? levelReader : null, pos, neighborState, direction);
        }
        return 0;
    }

    private int getDirectBluestoneRearSignal(LevelReader level, BlockPos pos, BlockState state, Direction direction) {
        if (state.getBlock() instanceof BluestoneSignalSource source) {
            return source.getBluestoneSignal(level, pos, state, direction);
        }
        return BluestoneSignalHelper.getDirectBluestoneSignal(level, pos, direction);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockTicks().willTickThisTick(pos, this)) {
            return;
        }
        int outputSignal = this.calculateBluestoneOutputSignal(level, pos, state);
        int storedOutputSignal = this.getOutputSignal(level, pos, state);
        if (outputSignal != storedOutputSignal || state.getValue(POWERED) != this.shouldTurnOn(level, pos, state)) {
            TickPriority tickPriority = this.shouldPrioritize(level, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
            level.scheduleTick(pos, this, 2, tickPriority);
        }
    }

    @Override
    public void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        this.refreshBluestoneOutputState(level, pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        BlockState updated = state.cycle(MODE);
        float pitch = updated.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55F : 0.5F;
        level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, pitch);
        level.setBlock(pos, updated, 2);
        this.refreshBluestoneOutputState(level, pos, updated);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private int calculateBluestoneOutputSignal(Level level, BlockPos pos, BlockState state) {
        int inputSignal = this.getInputSignal(level, pos, state);
        if (inputSignal == 0) {
            return 0;
        }
        int alternateSignal = this.getAlternateSignal(level, pos, state);
        if (alternateSignal > inputSignal) {
            return 0;
        }
        return state.getValue(MODE) == ComparatorMode.SUBTRACT ? inputSignal - alternateSignal : inputSignal;
    }

    private void refreshBluestoneOutputState(Level level, BlockPos pos, BlockState state) {
        int outputSignal = this.calculateBluestoneOutputSignal(level, pos, state);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        int storedOutputSignal = 0;
        if (blockEntity instanceof BluestoneComparatorBlockEntity comparator) {
            storedOutputSignal = comparator.getOutputSignal();
            comparator.setOutputSignal(outputSignal);
        }
        boolean shouldTurnOn = this.shouldTurnOn(level, pos, state);
        boolean powered = state.getValue(DiodeBlock.POWERED);
        if (powered != shouldTurnOn) {
            state = state.setValue(DiodeBlock.POWERED, shouldTurnOn);
            level.setBlock(pos, state, 2);
        }
        if (storedOutputSignal != outputSignal || powered != shouldTurnOn) {
            this.updateNeighborsInFront(level, pos, state);
        }
    }

    @Override
    public void animateTick(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) {
            return;
        }
        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
        double y = pos.getY() + 0.8D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
        level.addParticle(BLUESTONE_PARTICLE, x, y, z, 0.0D, 0.0D, 0.0D);
    }
}
