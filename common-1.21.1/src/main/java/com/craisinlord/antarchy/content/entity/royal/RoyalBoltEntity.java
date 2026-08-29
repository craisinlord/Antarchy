package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class RoyalBoltEntity extends AbstractHurtingProjectile {
    public RoyalBoltEntity(EntityType<? extends RoyalBoltEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.02D;
    }

    @Override
    protected float getInertia() {
        return 0.97F;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.END_ROD;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (target == owner || (owner != null && target == owner.getVehicle())) {
            return;
        }
        float damage = (float) AntarchySettings.royalBoltDamage();
        target.hurt(this.damageSources().mobProjectile(this, owner instanceof LivingEntity le ? le : null), damage);
        if (target instanceof LivingEntity living) {
            living.knockback(0.35D, this.getX() - target.getX(), this.getZ() - target.getZ());
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
