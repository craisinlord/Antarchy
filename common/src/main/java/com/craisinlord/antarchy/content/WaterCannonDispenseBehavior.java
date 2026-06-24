package com.craisinlord.antarchy.content;

import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

public class WaterCannonDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(source);

        WaterBombEntity bomb = AntarchyObjects.WATER_BOMB.get().create(source.level());
        if (bomb == null) return super.execute(source, stack);
        bomb.setPos(position.x(), position.y(), position.z());
        bomb.shoot(direction.getStepX(), direction.getStepY() + 0.1D, direction.getStepZ(), 1.5F, 0.0F);
        source.level().addFreshEntity(bomb);

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
