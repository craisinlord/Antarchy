package com.craisinlord.antarchy.content;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.DispenserBlock;

public class SquidzookaDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(source);

        MissileSquidEntity squid = AntarchyObjects.MISSILE_SQUID.get().create(source.level());
        if (squid == null) return super.execute(source, stack);

        squid.setPos(position.x(), position.y(), position.z());
        Vec3 vel = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ())
                .normalize()
                .scale(AntarchySettings.squidzookaLaunchVelocity());
        squid.launchAsProjectile(null, vel);
        source.level().addFreshEntity(squid);

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
