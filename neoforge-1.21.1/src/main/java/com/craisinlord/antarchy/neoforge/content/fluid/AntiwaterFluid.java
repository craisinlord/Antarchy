package com.craisinlord.antarchy.neoforge.content.fluid;

import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class AntiwaterFluid extends BaseFlowingFluid {
    private static final int MAX_HORIZONTAL_SPREAD_AMOUNT = 5;
    private static final int MAX_VERTICAL_FLOW_AMOUNT = 7;

    protected AntiwaterFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return AntarchyNeoforgeBlocks.ANTIWATER_BLOCK.get()
                .defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
        double flowX = 0.0D;
        double flowZ = 0.0D;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            FluidState neighborFluid = level.getFluidState(neighborPos);
            int neighborAmount = this.isSame(neighborFluid.getType()) ? neighborFluid.getAmount() : 0;
            int currentAmount = state.getAmount();
            int delta = neighborAmount - currentAmount;
            if (delta != 0) {
                flowX += direction.getStepX() * delta;
                flowZ += direction.getStepZ() * delta;
            }
        }

        Vec3 flow = new Vec3(flowX, 1.0D, flowZ);
        return flow.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 1.0D, 0.0D) : flow.normalize();
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected int getSpreadDelay(Level level, BlockPos pos, FluidState currentState, FluidState newState) {
        return this.getTickDelay(level);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction != Direction.DOWN && !this.isSame(fluid);
    }

    @Override
    protected boolean canSpreadTo(
            BlockGetter level,
            BlockPos fromPos,
            BlockState fromState,
            Direction direction,
            BlockPos toPos,
            BlockState toState,
            FluidState toFluidState,
            Fluid fluid
    ) {
        if (direction == Direction.DOWN) {
            return false;
        }

        return super.canSpreadTo(level, fromPos, fromState, direction, toPos, toState, toFluidState, fluid);
    }

    @Override
    protected FluidState getNewLiquid(Level level, BlockPos pos, BlockState blockState) {
        FluidState currentState = blockState.getFluidState();
        if (!currentState.isSource() && !this.isFlowingStateValid(level, pos, currentState)) {
            return Fluids.EMPTY.defaultFluidState();
        }

        if (this.canCreateSourceAt(level, pos)) {
            return this.getSource(false);
        }

        if (this.isValidVerticalSupport(level, pos)) {
            return this.getFlowing(MAX_VERTICAL_FLOW_AMOUNT, false);
        }

        int bestNeighborAmount = this.getBestHorizontalNeighborAmount(level, pos);
        if (bestNeighborAmount > 1) {
            return this.getFlowing(bestNeighborAmount - 1, false);
        }

        return net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState();
    }

    /*
    @Override
    protected void spread(Level level, BlockPos pos, FluidState state) {
        if (state.isEmpty()) {
            return;
        }
        if (!state.isSource() && !this.isFlowingStateValid(level, pos, state)) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            this.scheduleCleanup(level, pos);
            return;
        }

        BlockState blockState = level.getBlockState(pos);
        int nextAmount = state.getAmount() - 1;

        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        FluidState aboveFluid = level.getFluidState(abovePos);

        if (!aboveState.isSolid() && !aboveFluid.getType().isSame(this)) {
            FluidState upwardState = this.getFlowing(8, false);
            if (this.canSpreadTo(level, pos, blockState, Direction.UP, abovePos, aboveState, aboveFluid, upwardState.getType())) {
                this.spreadTo(level, abovePos, aboveState, Direction.UP, upwardState);
            }
        }

        if (!this.canHangAt(level, pos)) {
            return;
        }

        if (nextAmount <= 0) {
            return;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos targetPos = pos.relative(direction);
            BlockState targetState = level.getBlockState(targetPos);
            FluidState targetFluid = targetState.getFluidState();
            if (!this.canSpreadHorizontallyInto(level, targetPos)) {
                continue;
            }

            FluidState horizontalState = this.getFlowing(Math.min(nextAmount, MAX_HORIZONTAL_SPREAD_AMOUNT), false);
            if (!this.canSpreadTo(level, pos, blockState, direction, targetPos, targetState, targetFluid, horizontalState.getType())) {
                continue;
            }

            this.spreadTo(level, targetPos, targetState, direction, horizontalState);
        }
    }
    */

    @Override
    protected void spread(Level level, BlockPos pos, FluidState state) {
        if (state.isEmpty()) {
            return;
        }

        if (!state.isSource() && !this.isFlowingStateValid(level, pos, state)) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            this.scheduleCleanup(level, pos);
            return;
        }

        BlockState blockState = level.getBlockState(pos);
        this.spreadUpward(level, pos, state, blockState);

        if (!this.canHangAt(level, pos)) {
            return;
        }

        int nextAmount = state.getAmount() - 1;
        if (nextAmount <= 0) {
            return;
        }

        this.spreadHorizontally(level, pos, blockState, nextAmount);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState state) {
        if (!state.isSource() && !this.isFlowingStateValid(level, pos, state)) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            this.scheduleDependentCleanup(level, pos);
            return;
        }

        super.tick(level, pos, state);
    }

    private boolean hasCeiling(Level level, BlockPos pos) {
        return level.getBlockState(pos.above()).isSolid();
    }

    private boolean canHangAt(Level level, BlockPos pos) {
        return this.hasCeiling(level, pos);
    }

    private boolean hasHorizontalSupport(Level level, BlockPos pos) {
        FluidState state = level.getFluidState(pos);
        if (!this.isAntiwater(state)) {
            return false;
        }

        int currentAmount = state.getAmount();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            FluidState neighbor = level.getFluidState(pos.relative(direction));
            if (!this.isAntiwater(neighbor)) {
                continue;
            }

            if (this.getHorizontalSpreadAmount(neighbor) > currentAmount) {
                return true;
            }
        }

        return false;
    }

    private boolean canSpreadHorizontallyInto(Level level, BlockPos pos) {
        return true;
    }

    private int getBestHorizontalNeighborAmount(Level level, BlockPos pos) {
        int bestAmount = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            FluidState neighborFluid = level.getFluidState(neighborPos);
            if (neighborFluid.isEmpty() || !neighborFluid.getType().isSame(this)) {
                continue;
            }

            if (!this.hasSourceConnection(level, neighborPos, neighborFluid, new HashSet<>())) {
                continue;
            }

            bestAmount = Math.max(bestAmount, this.getHorizontalSpreadAmount(neighborFluid));
        }

        return bestAmount;
    }

    private int getHorizontalSpreadAmount(FluidState state) {
        if (state.isSource() || this.isValidVerticalSupportState(state)) {
            return MAX_HORIZONTAL_SPREAD_AMOUNT + 1;
        }

        return state.getAmount();
    }

    private boolean isValidVerticalSupportState(FluidState state) {
        return this.isAntiwater(state) && !state.isSource() && state.getAmount() >= MAX_VERTICAL_FLOW_AMOUNT;
    }

    private boolean isFlowingStateValid(Level level, BlockPos pos, FluidState state) {
        return state.isSource()
                || this.hasImmediateVerticalSupport(level, pos)
                || this.hasHorizontalSupport(level, pos);
    }

    private boolean canCreateSourceAt(Level level, BlockPos pos) {
        if (!this.canHangAt(level, pos)) {
            return false;
        }

        int sourceNeighbors = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            FluidState neighbor = level.getFluidState(pos.relative(direction));
            if (this.isAntiwater(neighbor) && neighbor.isSource()) {
                sourceNeighbors++;
                if (sourceNeighbors >= 2) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isValidVerticalSupport(Level level, BlockPos pos) {
        return this.hasImmediateVerticalSupport(level, pos);
    }

    private boolean isAntiwater(FluidState state) {
        return !state.isEmpty() && state.getType().isSame(this);
    }

    private boolean hasImmediateVerticalSupport(Level level, BlockPos pos) {
        FluidState below = level.getFluidState(pos.below());
        return this.isAntiwater(below);
    }

    private void spreadUpward(Level level, BlockPos pos, FluidState state, BlockState blockState) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        FluidState aboveFluid = level.getFluidState(abovePos);

        // Antiwater flowing into water solidifies rather than displacing it
        if (aboveFluid.getFluidType() == NeoForgeMod.WATER_TYPE.value()) {
            BlockState result = state.isSource()
                ? AntarchyNeoforgeBlocks.ANTIMETAL.get().defaultBlockState()
                : AntarchyNeoforgeBlocks.NYXITE.get().defaultBlockState();
            level.setBlockAndUpdate(pos, EventHooks.fireFluidPlaceBlockEvent(level, pos, pos, result));
            level.levelEvent(1501, pos, 0);
            return;
        }

        FluidState upwardState = this.getFlowing(MAX_VERTICAL_FLOW_AMOUNT, false);

        if (!this.canSpreadTo(level, pos, blockState, Direction.UP, abovePos, aboveState, aboveFluid, upwardState.getType())) {
            return;
        }

        if (aboveFluid.getType().isSame(this) && aboveFluid.getAmount() >= upwardState.getAmount()) {
            return;
        }

        this.spreadTo(level, abovePos, aboveState, Direction.UP, upwardState);
    }

    private void spreadHorizontally(Level level, BlockPos pos, BlockState blockState, int nextAmount) {
        FluidState horizontalState = this.getFlowing(Math.min(nextAmount, MAX_HORIZONTAL_SPREAD_AMOUNT), false);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos targetPos = pos.relative(direction);
            BlockState targetState = level.getBlockState(targetPos);
            FluidState targetFluid = targetState.getFluidState();
            if (!this.canSpreadHorizontallyInto(level, targetPos)) {
                continue;
            }

            if (targetFluid.getType().isSame(this) && targetFluid.getAmount() >= horizontalState.getAmount()) {
                continue;
            }

            if (!this.canSpreadTo(level, pos, blockState, direction, targetPos, targetState, targetFluid, horizontalState.getType())) {
                continue;
            }

            this.spreadTo(level, targetPos, targetState, direction, horizontalState);
        }
    }

    private boolean hasSourceConnection(Level level, BlockPos pos, FluidState state, Set<BlockPos> visited) {
        if (!this.isAntiwater(state)) {
            return false;
        }

        if (state.isSource()) {
            return true;
        }

        if (!visited.add(pos)) {
            return false;
        }

        FluidState below = level.getFluidState(pos.below());
        if (this.hasSourceConnection(level, pos.below(), below, visited)) {
            return true;
        }

        int currentAmount = state.getAmount();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            FluidState neighbor = level.getFluidState(neighborPos);
            if (!this.isAntiwater(neighbor)) {
                continue;
            }

            if (this.getHorizontalSpreadAmount(neighbor) <= currentAmount) {
                continue;
            }

            if (this.hasSourceConnection(level, neighborPos, neighbor, visited)) {
                return true;
            }
        }

        return false;
    }

    private void scheduleCleanup(Level level, BlockPos pos) {
        int tickDelay = this.getTickDelay(level);
        level.scheduleTick(pos, this, tickDelay);
        level.scheduleTick(pos.above(), this, tickDelay);
        level.scheduleTick(pos.below(), this, tickDelay);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            level.scheduleTick(pos.relative(direction), this, tickDelay);
        }
    }

    private void scheduleDependentCleanup(Level level, BlockPos pos) {
        int tickDelay = this.getTickDelay(level);
        level.scheduleTick(pos.above(), this, tickDelay);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            level.scheduleTick(pos.relative(direction), this, tickDelay);
        }
    }

    public static final class Source extends AntiwaterFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static final class Flowing extends AntiwaterFluid {
        public Flowing(Properties properties) {
            super(properties);
            this.registerDefaultState(this.getStateDefinition().any().setValue(LEVEL, 7));
        }

        @Override
        protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
