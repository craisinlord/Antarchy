package com.craisinlord.antarchy.content.dispenser;

import com.craisinlord.antarchy.content.item.BugSprayItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public final class BugSprayDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        if (!(source.level() instanceof ServerLevel level)) return stack;

        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(source);
        Vec3 origin = new Vec3(position.x(), position.y(), position.z());
        Vec3 facing = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        BugSprayItem.spray(level, origin, facing, origin, net.minecraft.sounds.SoundSource.BLOCKS, source.pos());

        stack.setDamageValue(stack.getDamageValue() + 1);
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    protected void playSound(BlockSource source) {
        source.level().levelEvent(1002, source.pos(), 0);
    }
}
