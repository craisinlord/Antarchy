package com.craisinlord.antarchy.content;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.SizeRayProjectileEntity;
import com.craisinlord.antarchy.content.item.SizeRayItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

public class SizeRayDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        if (!AntarchySettings.sizeChangingRaysEnabled()) {
            return super.execute(source, stack);
        }

        if (!(stack.getItem() instanceof SizeRayItem sizeRayItem)) {
            return super.execute(source, stack);
        }

        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(source);
        SizeRayProjectileEntity projectile = sizeRayItem.createProjectile(source.level(), stack);
        projectile.setPos(position.x(), position.y(), position.z());
        projectile.shoot(direction.getStepX(), direction.getStepY() + 0.1D, direction.getStepZ(), 1.6F, 0.0F);
        source.level().addFreshEntity(projectile);
        if (stack.isDamageableItem()) {
            int nextDamage = stack.getDamageValue() + 1;
            if (nextDamage >= stack.getMaxDamage()) {
                stack.shrink(1);
            } else {
                stack.setDamageValue(nextDamage);
            }
        } else {
            stack.shrink(1);
        }
        return stack;
    }
}
