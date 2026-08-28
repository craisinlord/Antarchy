package com.craisinlord.antarchy.content.dispenser;

import com.craisinlord.antarchy.content.entity.vortex.VortexChargeProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;

public class VortexChargeDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        if (VortexChargeProjectileEntity.defaultItemSupplier == null) {
            return super.execute(source, stack);
        }

        ServerLevel level = source.level();
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        BlockPos pos = source.pos().relative(direction);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        VortexChargeProjectileEntity projectile = new VortexChargeProjectileEntity(level, x, y, z);
        projectile.shoot(direction.getStepX(), direction.getStepY() + 0.1D, direction.getStepZ(), 1.5F, 1.0F);
        level.addFreshEntity(projectile);
        stack.shrink(1);
        return stack;
    }

    @Override
    protected void playSound(BlockSource source) {
        source.level().levelEvent(1002, source.pos(), 0);
    }
}
