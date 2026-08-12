package com.craisinlord.antarchy.content.dispenser;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.DispenserBlock;

public class RpoLauncherDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(source);

        OctopusBombEntity bomb = AntarchyObjects.OCTOPUS_BOMB.get().create(source.getLevel());
        if (bomb == null) return super.execute(source, stack);

        bomb.setPos(position.x(), position.y(), position.z());
        Vec3 vel = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ())
                .normalize()
                .scale(AntarchySettings.rpoLauncherLaunchVelocity());
        bomb.launchAsProjectile(null, vel);
        source.getLevel().addFreshEntity(bomb);

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
