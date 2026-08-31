package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class RoyalBoltEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Integer> ELEMENT =
            SynchedEntityData.defineId(RoyalBoltEntity.class, EntityDataSerializers.INT);

    public RoyalBoltEntity(EntityType<? extends RoyalBoltEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ELEMENT, RoyalBeamElement.GENERIC.ordinal());
    }

    public void setElement(RoyalBeamElement element) {
        this.entityData.set(ELEMENT, element.ordinal());
    }

    public RoyalBeamElement getElement() {
        RoyalBeamElement[] values = RoyalBeamElement.values();
        return values[Math.floorMod(this.entityData.get(ELEMENT), values.length)];
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Element", this.entityData.get(ELEMENT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Element")) {
            this.entityData.set(ELEMENT, tag.getInt("Element"));
        }
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
        return switch (this.getElement()) {
            case FIRE -> ParticleTypes.FLAME;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case ICE -> ParticleTypes.SNOWFLAKE;
            case GENERIC -> ParticleTypes.WITCH;
        };
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
            switch (this.getElement()) {
                case FIRE -> living.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), 80));
                case ICE -> {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                    living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 40, living.getTicksFrozen() + 140));
                }
                case LIGHTNING -> {
                    if (this.level() instanceof ServerLevel serverLevel) {
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                        if (bolt != null) {
                            bolt.moveTo(target.getX(), target.getY(), target.getZ());
                            bolt.setVisualOnly(true);
                            if (owner instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                                bolt.setCause(serverPlayer);
                            }
                            serverLevel.addFreshEntity(bolt);
                        }
                    }
                }
                case GENERIC -> {
                }
            }
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
