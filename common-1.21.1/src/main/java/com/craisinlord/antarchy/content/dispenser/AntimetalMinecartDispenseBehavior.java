package com.craisinlord.antarchy.content.dispenser;

import com.craisinlord.antarchy.content.block.AbstractAntimetalRailBlock;
import com.craisinlord.antarchy.content.minecart.AntimetalRailHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public final class AntimetalMinecartDispenseBehavior extends DefaultDispenseItemBehavior {
    private final DefaultDispenseItemBehavior fallback = new DefaultDispenseItemBehavior();

    @Override
    public ItemStack execute(BlockSource source, ItemStack stack) {
        if (!(stack.getItem() instanceof MinecartItem)) {
            return this.fallback.dispense(source, stack);
        }

        Direction direction = source.state().getValue(net.minecraft.world.level.block.DispenserBlock.FACING);
        BlockPos frontPos = source.pos().relative(direction);
        BlockState frontState = source.level().getBlockState(frontPos);

        if (frontState.getBlock() instanceof AbstractAntimetalRailBlock) {
            return this.spawnOnAntimetalRail(source, stack, frontPos);
        }

        if (frontState.isAir()) {
            BlockPos hangingRailPos = frontPos.above();
            BlockState hangingRailState = source.level().getBlockState(hangingRailPos);
            if (hangingRailState.getBlock() instanceof AbstractAntimetalRailBlock) {
                return this.spawnOnAntimetalRail(source, stack, hangingRailPos);
            }
        }

        return this.dispenseVanillaMinecart(source, stack, direction, frontPos, frontState);
    }

    private ItemStack spawnOnAntimetalRail(BlockSource source, ItemStack stack, BlockPos railPos) {
        AbstractMinecart.Type minecartType = minecartType(stack.getItem());
        if (minecartType == null) {
            return this.fallback.dispense(source, stack);
        }

        double x = railPos.getX() + 0.5D;
        double y = AntimetalRailHelper.attachY(railPos.getY(), 0);
        double z = railPos.getZ() + 0.5D;
        AbstractMinecart minecart = AbstractMinecart.createMinecart(source.level(), x, y, z, minecartType, stack, null);
        if (minecart == null) {
            return this.fallback.dispense(source, stack);
        }

        source.level().addFreshEntity(minecart);
        stack.shrink(1);
        return stack;
    }

    private ItemStack dispenseVanillaMinecart(BlockSource source, ItemStack stack, Direction direction, BlockPos frontPos, BlockState frontState) {
        Vec3 center = source.center();
        double x = center.x() + direction.getStepX() * 1.125D;
        double y = Math.floor(center.y()) + direction.getStepY();
        double z = center.z() + direction.getStepZ() * 1.125D;
        double yOffset;

        if (frontState.getBlock() instanceof BaseRailBlock) {
            yOffset = railShape(frontState).isAscending() ? 0.6D : 0.1D;
        } else if (frontState.isAir()) {
            BlockState belowFrontState = source.level().getBlockState(frontPos.below());
            if (!(belowFrontState.getBlock() instanceof BaseRailBlock)) {
                return this.fallback.dispense(source, stack);
            }
            yOffset = direction != Direction.DOWN && railShape(belowFrontState).isAscending() ? -0.4D : -0.9D;
        } else {
            return this.fallback.dispense(source, stack);
        }

        AbstractMinecart.Type minecartType = minecartType(stack.getItem());
        if (minecartType == null) {
            return this.fallback.dispense(source, stack);
        }

        AbstractMinecart minecart = AbstractMinecart.createMinecart(source.level(), x, y + yOffset, z, minecartType, stack, null);
        if (minecart == null) {
            return this.fallback.dispense(source, stack);
        }

        source.level().addFreshEntity(minecart);
        stack.shrink(1);
        return stack;
    }

    private static RailShape railShape(BlockState state) {
        if (state.getBlock() instanceof BaseRailBlock railBlock) {
            return state.getValue(railBlock.getShapeProperty());
        }
        return RailShape.NORTH_SOUTH;
    }

    private static AbstractMinecart.Type minecartType(Item item) {
        if (item == Items.MINECART) return AbstractMinecart.Type.RIDEABLE;
        if (item == Items.CHEST_MINECART) return AbstractMinecart.Type.CHEST;
        if (item == Items.FURNACE_MINECART) return AbstractMinecart.Type.FURNACE;
        if (item == Items.TNT_MINECART) return AbstractMinecart.Type.TNT;
        if (item == Items.HOPPER_MINECART) return AbstractMinecart.Type.HOPPER;
        if (item == Items.COMMAND_BLOCK_MINECART) return AbstractMinecart.Type.COMMAND_BLOCK;
        return null;
    }
}
