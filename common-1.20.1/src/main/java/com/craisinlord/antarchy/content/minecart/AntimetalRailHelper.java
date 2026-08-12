package com.craisinlord.antarchy.content.minecart;

import com.craisinlord.antarchy.content.block.AbstractAntimetalRailBlock;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

import java.util.EnumMap;
import java.util.Map;

public final class AntimetalRailHelper {
    public static final double RAIL_TOP_OFFSET = 0.0625D;
    public static final double CART_MODEL_HEIGHT = 0.7D;
    public static final double CART_HANG_OFFSET = CART_MODEL_HEIGHT - 0.1875D;
    public static final double CART_MODEL_PIVOT_Y = CART_MODEL_HEIGHT / 2.0D;

    private static final Map<RailShape, Pair<Vec3i, Vec3i>> ANTIMETAL_EXITS = new EnumMap<>(RailShape.class);

    static {
        ANTIMETAL_EXITS.put(RailShape.NORTH_SOUTH, Pair.of(new Vec3i(0, 0, -1), new Vec3i(0, 0, 1)));
        ANTIMETAL_EXITS.put(RailShape.EAST_WEST, Pair.of(new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0)));
        ANTIMETAL_EXITS.put(RailShape.ASCENDING_EAST, Pair.of(new Vec3i(-1, -1, 0), new Vec3i(1, 0, 0)));
        ANTIMETAL_EXITS.put(RailShape.ASCENDING_WEST, Pair.of(new Vec3i(-1, 0, 0), new Vec3i(1, -1, 0)));
        ANTIMETAL_EXITS.put(RailShape.ASCENDING_NORTH, Pair.of(new Vec3i(0, 0, -1), new Vec3i(0, -1, 1)));
        ANTIMETAL_EXITS.put(RailShape.ASCENDING_SOUTH, Pair.of(new Vec3i(0, -1, -1), new Vec3i(0, 0, 1)));
        ANTIMETAL_EXITS.put(RailShape.SOUTH_EAST, Pair.of(new Vec3i(0, 0, 1), new Vec3i(1, 0, 0)));
        ANTIMETAL_EXITS.put(RailShape.SOUTH_WEST, Pair.of(new Vec3i(0, 0, 1), new Vec3i(-1, 0, 0)));
        ANTIMETAL_EXITS.put(RailShape.NORTH_WEST, Pair.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0)));
        ANTIMETAL_EXITS.put(RailShape.NORTH_EAST, Pair.of(new Vec3i(0, 0, -1), new Vec3i(1, 0, 0)));
    }

    private AntimetalRailHelper() {
    }

    public static Pair<Vec3i, Vec3i> getExits(RailShape shape) {
        return ANTIMETAL_EXITS.get(shape);
    }

    public static double attachY(int railY, int exitDy) {
        return railY + exitDy + 1.0D - RAIL_TOP_OFFSET - CART_HANG_OFFSET;
    }

    private static boolean isRail(LevelReader level, BlockPos pos) {
        return AbstractAntimetalRailBlock.isAntimetalRail(level.getBlockState(pos));
    }

    private static RailShape computeShape(AbstractAntimetalRailBlock block, LevelReader level, BlockPos pos, Direction.Axis fallbackAxis) {
        boolean northDown = isRail(level, pos.north().below());
        boolean southDown = isRail(level, pos.south().below());
        boolean eastDown = isRail(level, pos.east().below());
        boolean westDown = isRail(level, pos.west().below());

        boolean north = isRail(level, pos.north()) || isRail(level, pos.north().above()) || northDown;
        boolean south = isRail(level, pos.south()) || isRail(level, pos.south().above()) || southDown;
        boolean east = isRail(level, pos.east()) || isRail(level, pos.east().above()) || eastDown;
        boolean west = isRail(level, pos.west()) || isRail(level, pos.west().above()) || westDown;

        if (block.isStraight()) {
            if (northDown) return RailShape.ASCENDING_SOUTH;
            if (southDown) return RailShape.ASCENDING_NORTH;
            if (eastDown) return RailShape.ASCENDING_WEST;
            if (westDown) return RailShape.ASCENDING_EAST;
            if ((east || west) && !(north || south)) return RailShape.EAST_WEST;
            if ((north || south) && !(east || west)) return RailShape.NORTH_SOUTH;
            return fallbackAxis == Direction.Axis.X ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
        }

        if (northDown) return RailShape.ASCENDING_SOUTH;
        if (southDown) return RailShape.ASCENDING_NORTH;
        if (eastDown) return RailShape.ASCENDING_WEST;
        if (westDown) return RailShape.ASCENDING_EAST;

        if (south && east && !north && !west) return RailShape.SOUTH_EAST;
        if (south && west && !north && !east) return RailShape.SOUTH_WEST;
        if (north && west && !south && !east) return RailShape.NORTH_WEST;
        if (north && east && !south && !west) return RailShape.NORTH_EAST;

        if ((east || west) && !(north || south)) return RailShape.EAST_WEST;
        if ((north || south) && !(east || west)) return RailShape.NORTH_SOUTH;
        return fallbackAxis == Direction.Axis.X ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
    }

    public static BlockState getStateForPlacement(AbstractAntimetalRailBlock block, BlockPlaceContext context) {
        Direction.Axis axis = context.getHorizontalDirection().getAxis();
        RailShape shape = computeShape(block, context.getLevel(), context.getClickedPos(), axis);
        return block.defaultBlockState().setValue(block.getShapeProperty(), shape);
    }

    public static void updateConnections(AbstractAntimetalRailBlock block, BlockState state, LevelAccessor level, BlockPos pos) {
        if (!(level instanceof Level realLevel) || realLevel.isClientSide()) {
            return;
        }
        RailShape shape = computeShape(block, realLevel, pos, Direction.Axis.Z);
        if (state.getValue(block.getShapeProperty()) != shape) {
            realLevel.setBlock(pos, state.setValue(block.getShapeProperty(), shape), 3);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos horizontalPos = pos.relative(direction);
            recomputeIfRail(realLevel, horizontalPos);
            recomputeIfRail(realLevel, horizontalPos.above());
            recomputeIfRail(realLevel, horizontalPos.below());
        }
    }

    private static void recomputeIfRail(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof AbstractAntimetalRailBlock block) {
            RailShape shape = computeShape(block, level, pos, Direction.Axis.Z);
            if (state.getValue(block.getShapeProperty()) != shape) {
                level.setBlock(pos, state.setValue(block.getShapeProperty(), shape), 3);
            }
        }
    }

}
