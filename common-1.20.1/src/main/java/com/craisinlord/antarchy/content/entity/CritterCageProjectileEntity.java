package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.item.CritterCageItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class CritterCageProjectileEntity extends ThrowableItemProjectile {
    private static final int MAX_LIFETIME_TICKS = 600;

    public CritterCageProjectileEntity(EntityType<? extends CritterCageProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public CritterCageProjectileEntity(EntityType<? extends CritterCageProjectileEntity> entityType, LivingEntity owner, Level level) {
        super(entityType, owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return AntarchyObjects.CRITTER_CAGE.get();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.tickCount > MAX_LIFETIME_TICKS) {
            this.dropEmptyCage();
            this.discard();
            return;
        }

        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.level().isClientSide) {
            return;
        }

        Entity target = hitResult.getEntity();
        if (!CritterCageItem.canCapture(target)) {
            this.dropEmptyCage();
            this.discard();
            return;
        }

        ItemStack filledStack = CritterCageItem.createFilledStack(target);
        target.discard();
        this.dropFilledCage(filledStack);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!this.level().isClientSide) {
            this.dropEmptyCage();
            this.discard();
        }
    }

    private void dropFilledCage(ItemStack filledStack) {
        this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), filledStack.copyWithCount(1)));
    }

    private void dropEmptyCage() {
        if (this.level().isClientSide) {
            return;
        }
        this.spawnAtLocation(new ItemStack(AntarchyObjects.CRITTER_CAGE.get()));
    }
}
