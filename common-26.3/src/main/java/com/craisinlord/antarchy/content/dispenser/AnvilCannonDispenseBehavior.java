package com.craisinlord.antarchy.content.dispenser;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.AnvilCannonItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AnvilCannonDispenseBehavior extends DefaultDispenseItemBehavior {
    private boolean fired;

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        this.fired = false;
        DispenserBlockEntity dispenser = source.blockEntity();
        ItemStack ammo = ItemStack.EMPTY;
        for (int slot = 0; slot < dispenser.getContainerSize(); slot++) {
            ItemStack candidate = dispenser.getItem(slot);
            if (AnvilCannonItem.isAmmo(candidate)) {
                ammo = candidate;
                break;
            }
        }
        if (ammo.isEmpty()) {
            return stack;
        }

        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(source);
        BlockState anvilState = AnvilCannonItem.anvilStateFor(ammo, direction);
        ammo.shrink(1);

        Vec3 spawn = new Vec3(position.x(), position.y() - 0.5D, position.z())
                .add(direction.getStepX() * 0.5D, direction.getStepY() * 0.5D, direction.getStepZ() * 0.5D);
        Vec3 velocity = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ())
                .scale(AntarchySettings.anvilCannonLaunchVelocity());
        AnvilCannonItem.launchAnvil(source.level(), spawn, velocity, null, anvilState);
        AnvilCannonItem.playFireSound(source.level(), position.x(), position.y(), position.z());
        this.fired = true;

        if (stack.isDamageableItem()) {
            int nextDamage = stack.getDamageValue() + 1;
            if (nextDamage >= stack.getMaxDamage()) {
                stack.shrink(1);
            } else {
                stack.setDamageValue(nextDamage);
            }
        }
        return stack;
    }

    @Override
    protected void playSound(BlockSource source) {
        if (!this.fired) {
            source.level().levelEvent(1001, source.pos(), 0);
        }
    }
}
