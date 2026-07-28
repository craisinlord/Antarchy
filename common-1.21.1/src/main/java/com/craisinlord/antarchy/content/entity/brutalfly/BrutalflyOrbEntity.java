package com.craisinlord.antarchy.content.entity.brutalfly;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

public class BrutalflyOrbEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> ORB_VARIANT =
            SynchedEntityData.defineId(BrutalflyOrbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> LINGERING =
            SynchedEntityData.defineId(BrutalflyOrbEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String ORB_VARIANT_KEY = "OrbVariant";
    private static final String LINGERING_KEY = "Lingering";
    private static final String LINGER_TICKS_KEY = "LingerTicks";

    private static final int MAX_FLIGHT_TICKS = 200;
    private static final int MAX_LIFETIME_TICKS = 100;
    private static final float AREA_RADIUS = 2.75F;

    private int lingeringTicks;

    public BrutalflyOrbEntity(EntityType<? extends BrutalflyOrbEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BrutalflyOrbEntity(EntityType<? extends BrutalflyOrbEntity> entityType, LivingEntity owner, Level level) {
        super(entityType, owner, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ORB_VARIANT, OrbVariant.POISON.ordinal());
        builder.define(LINGERING, false);
    }

    public void setOrbVariant(OrbVariant variant) {
        this.entityData.set(ORB_VARIANT, variant.ordinal());
    }

    public OrbVariant getOrbVariant() {
        int index = Mth.clamp(this.entityData.get(ORB_VARIANT), 0, OrbVariant.values().length - 1);
        return OrbVariant.values()[index];
    }

    public boolean isLingeringOrb() {
        return this.entityData.get(LINGERING);
    }

    public float getOrbRadius(float partialTick) {
        if (!this.isLingeringOrb()) {
            return 0.55F;
        }
        float age = Math.min(1.0F, (this.lingeringTicks + partialTick) / 12.0F);
        return 0.7F + AREA_RADIUS * age;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void tick() {
        if (this.isLingeringOrb()) {
            this.tickLingeringOrb();
            return;
        }

        super.tick();

        if (this.tickCount > MAX_FLIGHT_TICKS) {
            this.discard();
            return;
        }

        if (this.level().isClientSide) {
            this.spawnTravelParticles();
        }
    }

    private void tickLingeringOrb() {
        this.lingeringTicks++;

        if (this.level().isClientSide) {
            this.spawnLingeringParticles();
            return;
        }

        if (this.lingeringTicks % 5 == 0) {
            this.applyAreaEffects();
        }

        if (this.lingeringTicks >= MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    private void applyAreaEffects() {
        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(AREA_RADIUS, 1.75D, AREA_RADIUS),
                entity -> entity.isAlive() && entity != this.getOwner()
        )) {
            switch (this.getOrbVariant()) {
                case POISON -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
                case STICKY -> {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 3, false, true));
                    target.makeStuckInBlock(Blocks.HONEY_BLOCK.defaultBlockState(), new Vec3(0.35D, 0.2D, 0.35D));
                }
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            double radius = this.getOrbRadius(0.0F) * 0.3D;
            if (this.getOrbVariant() == OrbVariant.POISON) {
                serverLevel.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.15D, this.getZ(), 3, radius, 0.1D, radius, 0.02D);
                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.25D, this.getZ(), 2, radius, 0.15D, radius, 0.0D);
            } else {
                serverLevel.sendParticles(ParticleTypes.FALLING_HONEY, this.getX(), this.getY() + 0.2D, this.getZ(), 4, radius, 0.15D, radius, 0.0D);
                serverLevel.sendParticles(ParticleTypes.DRIPPING_HONEY, this.getX(), this.getY() + 0.1D, this.getZ(), 2, radius, 0.1D, radius, 0.0D);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity livingTarget) {
            float directHitDamage = (float) AntarchySettings.brutalflySpitDamage();
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                livingTarget.hurt(this.damageSources().mobProjectile(this, livingOwner), directHitDamage);
            } else {
                livingTarget.hurt(this.damageSources().magic(), directHitDamage);
            }

            if (this.getOrbVariant() == OrbVariant.POISON) {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0, false, true));
            } else {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 4, false, true));
                livingTarget.makeStuckInBlock(Blocks.HONEY_BLOCK.defaultBlockState(), new Vec3(0.3D, 0.2D, 0.3D));
            }
        }

        this.beginLingering(result.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.beginLingering(result.getLocation());
    }

    private void beginLingering(Vec3 location) {
        if (this.isLingeringOrb()) {
            return;
        }

        this.setPos(location.x, location.y + 0.1D, location.z);
        this.entityData.set(LINGERING, true);
        this.noPhysics = true;
        this.lingeringTicks = 0;
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void spawnTravelParticles() {
        if (this.getOrbVariant() == OrbVariant.POISON) {
            this.level().addParticle(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ParticleTypes.ITEM_SLIME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        } else {
            this.level().addParticle(ParticleTypes.FALLING_HONEY, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    private void spawnLingeringParticles() {
        double radius = this.getOrbRadius(0.0F) * 0.28D;
        if (this.getOrbVariant() == OrbVariant.POISON) {
            this.level().addParticle(ParticleTypes.WITCH, this.getX() + (this.random.nextDouble() - 0.5D) * radius, this.getY() + 0.15D, this.getZ() + (this.random.nextDouble() - 0.5D) * radius, 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ParticleTypes.ITEM_SLIME, this.getX() + (this.random.nextDouble() - 0.5D) * radius, this.getY() + 0.2D, this.getZ() + (this.random.nextDouble() - 0.5D) * radius, 0.0D, 0.0D, 0.0D);
        } else {
            this.level().addParticle(ParticleTypes.FALLING_HONEY, this.getX() + (this.random.nextDouble() - 0.5D) * radius, this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) * radius, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(ORB_VARIANT_KEY, this.getOrbVariant().ordinal());
        tag.putBoolean(LINGERING_KEY, this.isLingeringOrb());
        tag.putInt(LINGER_TICKS_KEY, this.lingeringTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setOrbVariant(OrbVariant.values()[Mth.clamp(tag.getInt(ORB_VARIANT_KEY), 0, OrbVariant.values().length - 1)]);
        this.entityData.set(LINGERING, tag.getBoolean(LINGERING_KEY));
        this.lingeringTicks = Math.max(0, tag.getInt(LINGER_TICKS_KEY));
        this.noPhysics = this.isLingeringOrb();
        if (this.isLingeringOrb()) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    public enum OrbVariant {
        POISON,
        STICKY
    }
}
