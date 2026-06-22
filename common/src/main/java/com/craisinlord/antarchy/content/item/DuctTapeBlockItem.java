package com.craisinlord.antarchy.content.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DuctTapeBlockItem extends BlockItem {
    public DuctTapeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.antarchy.duct_tape.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        BlockPlaceContext updatedContext = super.updatePlacementContext(context);
        if (updatedContext == null) {
            return null;
        }

        Level level = updatedContext.getLevel();
        BlockPos clickedPos = updatedContext.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!clickedState.is(this.getBlock())) {
            return updatedContext;
        }

        for (Direction direction : getSpreadDirections(updatedContext)) {
            BlockPos targetPos = clickedPos.relative(direction);
            BlockState targetState = level.getBlockState(targetPos);
            if (canSpreadTo(targetState)) {
                return BlockPlaceContext.at(updatedContext, targetPos, updatedContext.getClickedFace());
            }
        }

        return null;
    }

    private boolean canSpreadTo(BlockState state) {
        return !state.is(this.getBlock()) && (state.isAir() || state.canBeReplaced());
    }

    private Direction[] getSpreadDirections(BlockPlaceContext context) {
        Direction preferredDirection = getPreferredDirection(context);
        List<Direction> directions = new ArrayList<>(4);
        directions.add(preferredDirection);
        directions.add(preferredDirection.getClockWise());
        directions.add(preferredDirection.getCounterClockWise());
        directions.add(preferredDirection.getOpposite());
        return directions.toArray(Direction[]::new);
    }

    private Direction getPreferredDirection(BlockPlaceContext context) {
        if (context.getClickedFace().getAxis().isHorizontal()) {
            return context.getClickedFace();
        }

        Vec3 hitLocation = context.getClickLocation();
        BlockPos clickedPos = context.getClickedPos();
        double offsetX = hitLocation.x - (clickedPos.getX() + 0.5D);
        double offsetZ = hitLocation.z - (clickedPos.getZ() + 0.5D);
        if (Math.abs(offsetX) > Math.abs(offsetZ)) {
            return offsetX >= 0.0D ? Direction.EAST : Direction.WEST;
        }

        return offsetZ >= 0.0D ? Direction.SOUTH : Direction.NORTH;
    }
}
