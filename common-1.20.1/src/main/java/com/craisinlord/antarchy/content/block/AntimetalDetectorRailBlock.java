package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.minecart.AntimetalMinecartAccess;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AntimetalDetectorRailBlock extends AbstractAntimetalRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int CHECK_PERIOD = 20;

    public AntimetalDetectorRailBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(POWERED, false).setValue(WATERLOGGED, false));
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, POWERED, WATERLOGGED);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock()) && !level.isClientSide()) {
            level.scheduleTick(pos, this, CHECK_PERIOD);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean detected = !getDetectedCarts(level, pos).isEmpty();
        if (detected != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, detected), 3);
            AntimetalPowerHelper.notifyAllSignalNeighbors(level, pos);
        }
        level.scheduleTick(pos, this, CHECK_PERIOD);
    }

    private static List<AbstractMinecart> getDetectedCarts(Level level, BlockPos pos) {
        AABB box = new AABB(pos.getX(), pos.getY() - 2.0D, pos.getZ(), pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D);
        return level.getEntitiesOfClass(AbstractMinecart.class, box, cart ->
                cart instanceof AntimetalMinecartAccess access && pos.equals(access.antarchy$getAntimetalRailPos()));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(POWERED)) {
            return 0;
        }
        int best = 0;
        for (AbstractMinecart cart : getDetectedCarts(level, pos)) {
            if (cart instanceof Container container) {
                best = Math.max(best, AbstractContainerMenu.getRedstoneSignalFromContainer(container));
            }
        }
        return best;
    }
}
