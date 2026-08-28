package com.craisinlord.antarchy.content.entity.vortex;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class VortexChargeProjectileEntity extends ThrowableItemProjectile {
    public static Supplier<Item> defaultItemSupplier;
    public static Supplier<EntityType<VortexChargeProjectileEntity>> projectileTypeSupplier;
    public static Supplier<EntityType<WindVortexEntity>> windVortexTypeSupplier;

    public VortexChargeProjectileEntity(EntityType<? extends VortexChargeProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public VortexChargeProjectileEntity(EntityType<? extends VortexChargeProjectileEntity> entityType,
            LivingEntity thrower, Level level) {
        super(entityType, thrower, level);
    }

    public VortexChargeProjectileEntity(Level level, double x, double y, double z) {
        super(projectileTypeSupplier.get(), x, y, z, level);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03D;
    }

    @Override
    protected Item getDefaultItem() {
        return defaultItemSupplier != null ? defaultItemSupplier.get() : Items.WIND_CHARGE;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if (this.level() instanceof ServerLevel serverLevel && windVortexTypeSupplier != null) {
            Vec3 position = hitResult.getLocation();
            WindVortexEntity vortex = WindVortexEntity.create(
                    serverLevel,
                    windVortexTypeSupplier.get(),
                    position,
                    this.getDeltaMovement().normalize().scale(0.035D),
                    null,
                    false
            );
            serverLevel.addFreshEntity(vortex);
            serverLevel.playSound(null, position.x, position.y, position.z,
                    SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.9F, 1.0F);
        }

        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }
}
