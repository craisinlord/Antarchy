package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.effect.GoopedEffectHooks;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SpitBugProjectileEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> LINGERING =
            SynchedEntityData.defineId(SpitBugProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int MAX_FLIGHT_TICKS = 100;
    private static final int LINGER_DURATION_TICKS = 300;
    private static final int DIRECT_HIT_DAMAGE = 8;
    private static final float LINGER_RADIUS = 1.65F;

    private boolean lingering;
    private int lingeringTicks;

    public SpitBugProjectileEntity(EntityType<? extends SpitBugProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SpitBugProjectileEntity(EntityType<? extends SpitBugProjectileEntity> entityType, LivingEntity owner, Level level) {
        super(entityType, owner, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LINGERING, false);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    protected float getGravity() {
        return (float) (0.03D);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public boolean isLingering() {
        return this.entityData.get(LINGERING);
    }

    public float getRenderScale(float partialTick) {
        if (!this.isLingering()) {
            return 0.65F;
        }
        float progress = Math.min(1.0F, (this.lingeringTicks + partialTick) / 8.0F);
        return 0.6F + progress * 0.5F;
    }

    @Override
    public void tick() {
        if (this.isLingering()) {
            this.tickLingering();
            return;
        }

        super.tick();
        if (this.tickCount > MAX_FLIGHT_TICKS) {
            this.discard();
            return;
        }

        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.ITEM_SLIME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    private void tickLingering() {
        this.lingeringTicks++;
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) {
            double radius = 0.4D + this.random.nextDouble() * 0.45D;
            this.level().addParticle(
                    ParticleTypes.ITEM_SLIME,
                    this.getX() + (this.random.nextDouble() - 0.5D) * radius,
                    this.getY() + 0.04D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * radius,
                    0.0D,
                    0.0D,
                    0.0D
            );
            return;
        }

        if (this.lingeringTicks % 4 == 0) {
            this.applyLingeringSpit();
        }

        if (this.lingeringTicks >= LINGER_DURATION_TICKS) {
            this.discard();
        }
    }

    private void applyLingeringSpit() {
        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(LINGER_RADIUS, 0.6D, LINGER_RADIUS),
                entity -> entity.isAlive() && entity != this.getOwner()
        )) {
            applyGooped(target, 30);
            target.makeStuckInBlock(Blocks.HONEY_BLOCK.defaultBlockState(), new Vec3(0.2D, 0.0D, 0.2D));
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.05D, this.getZ(), 3, 0.45D, 0.02D, 0.45D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity livingTarget) {
            Entity owner = this.getOwner();
            if (owner instanceof SpitBugEntity && this.level() instanceof ServerLevel serverLevel) {
                livingTarget.hurt(AntarchyDamageSources.spitBugDisrespect(serverLevel, this, owner), DIRECT_HIT_DAMAGE);
            } else if (owner instanceof LivingEntity livingOwner) {
                livingTarget.hurt(this.damageSources().mobProjectile(this, livingOwner), DIRECT_HIT_DAMAGE);
            } else {
                livingTarget.hurt(this.damageSources().magic(), DIRECT_HIT_DAMAGE);
            }
            applyGooped(livingTarget, GoopedEffectHooks.PROJECTILE_DURATION_TICKS);
            livingTarget.makeStuckInBlock(Blocks.HONEY_BLOCK.defaultBlockState(), new Vec3(0.25D, 0.0D, 0.25D));
        }

        this.beginLingering(result.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.beginLingering(result.getLocation());
    }

    private void beginLingering(Vec3 location) {
        if (this.isLingering()) {
            return;
        }

        this.lingering = true;
        this.entityData.set(LINGERING, true);
        this.lingeringTicks = 0;
        this.noPhysics = true;
        this.setPos(location.x, location.y + 0.05D, location.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private static void applyGooped(LivingEntity target, int durationTicks) {
        Holder<MobEffect> gooped = GoopedEffectHooks.holder();
        if (gooped != null) {
            target.addEffect(new MobEffectInstance(gooped.value(), durationTicks, 0, false, true, true));
        }
    }
}
