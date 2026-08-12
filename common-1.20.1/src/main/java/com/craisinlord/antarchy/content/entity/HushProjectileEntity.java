package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class HushProjectileEntity extends ThrowableItemProjectile {
    private static final int DREAD_DURATION_TICKS = 300;
    private static final double TRAVEL_SPEED = 0.27D;
    private static final int RETARGET_GRACE_TICKS = 60;
    private static final double RETARGET_SEARCH_RADIUS = 16.0D;
    private static final DustParticleOptions HUSH_PARTICLES = new DustParticleOptions(new org.joml.Vector3f(1.0F, 0.05F, 0.05F), 1.0F);
    private static final String TARGET_UUID_KEY = "HushTarget";

    private UUID targetUuid;
    private int retargetGraceTicksRemaining;

    public HushProjectileEntity(EntityType<? extends HushProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.REDSTONE;
    }

    @Override
    protected float getGravity() {
        return (float) (0.0D);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!this.level().isClientSide && !this.isRemoved()) {
            this.discard();
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            if (this.tickCount > AntarchySettings.hushweedSporeLifetimeTicks()) {
                this.discard();
                return;
            }

            this.tickGuidedFlight();
        }

        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(HUSH_PARTICLES, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    public void setTargetUuid(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    private void tickGuidedFlight() {
        if (this.targetUuid == null) {
            this.discard();
            return;
        }

        LivingEntity target = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(48.0D),
                entity -> entity.isAlive() && entity.getUUID().equals(this.targetUuid)
        ).stream().findFirst().orElse(null);

        if (target == null) {
            target = this.findReplacementTarget();
            if (target != null) {
                this.targetUuid = target.getUUID();
                this.retargetGraceTicksRemaining = 0;
            } else {
                if (this.retargetGraceTicksRemaining <= 0) {
                    this.retargetGraceTicksRemaining = RETARGET_GRACE_TICKS;
                }
                if (--this.retargetGraceTicksRemaining <= 0) {
                    this.discard();
                }
                return;
            }
        } else {
            this.retargetGraceTicksRemaining = 0;
        }

        Vec3 toTarget = target.getEyePosition().subtract(this.position());
        if (toTarget.lengthSqr() < 1.0E-6D) {
            this.discard();
            return;
        }

        // Smoothly steer toward the target each tick
        Vec3 desired = toTarget.normalize().scale(TRAVEL_SPEED);
        this.setDeltaMovement(this.getDeltaMovement().lerp(desired, 0.1D));
    }

    private LivingEntity findReplacementTarget() {
        return this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        this.getBoundingBox().inflate(RETARGET_SEARCH_RADIUS),
                        entity -> entity.isAlive()
                                && !entity.getUUID().equals(this.targetUuid)
                                && entity.getType().is(AntarchyTags.Entities.HUSHWEED_TARGETS)
                                && (!(entity instanceof Player player) || (!player.isCreative() && !player.isSpectator())))
                .stream()
                .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.hurt(this.damageSources().magic(), 1.0F);
            livingEntity.addEffect(new MobEffectInstance(AntarchyObjects.DREAD.get(), DREAD_DURATION_TICKS, 0, false, true, true));
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(HUSH_PARTICLES, this.getX(), this.getY(), this.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.0D);
            }
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetUuid != null) {
            tag.putUUID(TARGET_UUID_KEY, this.targetUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(TARGET_UUID_KEY)) {
            this.targetUuid = tag.getUUID(TARGET_UUID_KEY);
        }
    }
}
