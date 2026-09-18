package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RoyalElementalProjectileEntity extends ThrowableProjectile implements GeoEntity {
    private static final EntityDataAccessor<Integer> ATTACK_TYPE =
            SynchedEntityData.defineId(RoyalElementalProjectileEntity.class, EntityDataSerializers.INT);
    private static final int FIREBALL = 0;
    private static final int ICEBALL = 1;
    private static final int DREAM_FIREBALL = 2;
    private static final int MAX_LIFETIME = 100;
    private static final float FIREBALL_SPEED = 1.5F;
    private static final float ICEBALL_SPEED = 1.2F;
    private static final float DREAM_FIREBALL_SPEED = 1.05F;
    private static final double PROJECTILE_INERTIA = 0.985D;
    private static final float DREAM_FIREBALL_INERTIA = 0.96F;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RoyalElementalProjectileEntity(EntityType<? extends RoyalElementalProjectileEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public static RoyalElementalProjectileEntity create(Level level, LivingEntity owner, RoyalBeamElement element,
                                                        Vec3 position, Vec3 direction) {
        RoyalElementalProjectileEntity projectile = new RoyalElementalProjectileEntity(
                AntarchyObjects.ROYAL_ELEMENTAL_PROJECTILE.get(), level);
        projectile.setOwner(owner);
        projectile.setPos(position.x, position.y, position.z);
        projectile.setAttackType(element == RoyalBeamElement.ICE ? ICEBALL
                : element == RoyalBeamElement.DREAM_FIRE ? DREAM_FIREBALL : FIREBALL);
        float speed = element == RoyalBeamElement.ICE ? ICEBALL_SPEED
                : element == RoyalBeamElement.DREAM_FIRE ? DREAM_FIREBALL_SPEED : FIREBALL_SPEED;
        projectile.shoot(direction.x, direction.y, direction.z, speed, 0.0F);
        return projectile;
    }

    /** Solves a discrete ballistic lead against the projectile's drag and gravity. */
    public static Vec3 aimDirection(Vec3 origin, LivingEntity target, RoyalBeamElement element) {
        double speed = element == RoyalBeamElement.ICE ? ICEBALL_SPEED : FIREBALL_SPEED;
        double gravity = element == RoyalBeamElement.ICE ? 0.006D : 0.018D;
        double inertia = PROJECTILE_INERTIA;
        Vec3 bestDirection = target.getEyePosition().subtract(origin).normalize();
        double bestError = Double.MAX_VALUE;

        for (int ticks = 1; ticks < MAX_LIFETIME; ticks++) {
            double dragDistance = (1.0D - Math.pow(inertia, ticks)) / (1.0D - inertia);
            Vec3 predictedTarget = target.getEyePosition().add(target.getDeltaMovement().scale(ticks));
            Vec3 displacement = predictedTarget.subtract(origin);
            double gravityCompensation = gravity * (ticks - dragDistance) / (1.0D - inertia);
            Vec3 initialVelocity = new Vec3(
                    displacement.x / dragDistance,
                    (displacement.y + gravityCompensation) / dragDistance,
                    displacement.z / dragDistance);
            double error = Math.abs(initialVelocity.length() - speed);
            if (error < bestError) {
                bestError = error;
                bestDirection = initialVelocity.normalize();
            }
        }
        return bestDirection;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ATTACK_TYPE, FIREBALL);
    }

    public boolean isIceball() {
        return this.entityData.get(ATTACK_TYPE) == ICEBALL;
    }

    public boolean isDreamFireball() {
        return this.entityData.get(ATTACK_TYPE) == DREAM_FIREBALL;
    }

    private void setAttackType(int type) {
        this.entityData.set(ATTACK_TYPE, type);
    }

    @Override
    protected double getDefaultGravity() {
        return this.isIceball() ? 0.006D : 0.018D;
    }

    protected float getInertia() {
        return this.isDreamFireball() ? DREAM_FIREBALL_INERTIA : (float) PROJECTILE_INERTIA;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.tickCount > MAX_LIFETIME) {
            this.discard();
            return;
        }
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(this.getTrailParticle(),
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
            this.impact(level, result.getLocation());
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
            this.impact(level, result.getLocation());
        }
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && result.getType() == HitResult.Type.MISS) {
            this.discard();
        }
    }

    private void impact(ServerLevel level, Vec3 center) {
        if (this.isIceball()) {
            impactIce(level, center);
        } else {
            impactFire(level, center);
        }
    }

    private void impactFire(ServerLevel level, Vec3 center) {
        DamageSource source = this.damageSources().mobProjectile(this, this.getOwner() instanceof LivingEntity living ? living : null);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(5.0D),
                this::canDamageWithImpact)) {
            double distance = Math.max(0.5D, living.position().distanceTo(center));
            float falloff = (float) Math.max(0.25D, 1.0D - distance / 5.0D);
            double damage = AntarchySettings.kingFireballDamage() * falloff;
            living.hurt(source, this.getOwner() instanceof RoyalBossEntity royalBoss
                    ? royalBoss.scaleRoyalDamage(damage) : (float) damage);
            living.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), 120));
        }
        igniteImpact(level, center, this.isDreamFireball());
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 4, 1.5D, 1.5D, 1.5D, 0.0D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, this.isDreamFireball() ? 1.15F : 1.0F,
                this.isDreamFireball() ? 0.78F : 0.92F);
    }

    private void igniteImpact(ServerLevel level, Vec3 center, boolean dream) {
        double radius = AntarchySettings.kingFireballRadius();
        int minX = (int) Math.floor(center.x - radius);
        int minY = (int) Math.floor(center.y - radius);
        int minZ = (int) Math.floor(center.z - radius);
        int maxX = (int) Math.floor(center.x + radius);
        int maxY = (int) Math.floor(center.y + radius);
        int maxZ = (int) Math.floor(center.z + radius);
        var fire = dream
                ? BuiltInRegistries.BLOCK.getOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        "antarchy", "dream_fire")).orElse(Blocks.FIRE)
                : Blocks.FIRE;
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (pos.distToCenterSqr(center.x, center.y, center.z) > radius * radius || !level.isEmptyBlock(pos)) {
                continue;
            }
            BlockState state = fire.defaultBlockState();
            if (state.canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, state);
            }
        }
    }

    private void impactIce(ServerLevel level, Vec3 center) {
        DamageSource source = this.damageSources().mobProjectile(this, this.getOwner() instanceof LivingEntity living ? living : null);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(5.0D),
                this::canDamageWithImpact)) {
            double damage = AntarchySettings.kingIceballDamage();
            living.hurt(source, this.getOwner() instanceof RoyalBossEntity royalBoss
                    ? royalBoss.scaleRoyalDamage(damage) : (float) damage);
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 80, living.getTicksFrozen() + 180));
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 120, 3));
        }
        coverInIce(level, center, AntarchySettings.kingIceballRadius(), AntarchySettings.kingElementalTerrainCap());
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, 80, 4.0D, 2.0D, 4.0D, 0.08D);
    }

    private boolean canDamageWithImpact(LivingEntity target) {
        Entity owner = this.getOwner();
        if (!target.isAlive() || target == owner) {
            return false;
        }
        return !(owner instanceof RoyalBossEntity royalBoss) || royalBoss.canDamageWithRoyalAttack(target);
    }

    private void coverInIce(ServerLevel level, Vec3 center, double radius, int cap) {
        int changed = 0;
        int minX = (int) Math.floor(center.x - radius);
        int minY = (int) Math.floor(center.y - radius);
        int minZ = (int) Math.floor(center.z - radius);
        int maxX = (int) Math.floor(center.x + radius);
        int maxY = (int) Math.floor(center.y + radius);
        int maxZ = (int) Math.floor(center.z + radius);
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (changed >= cap || pos.distToCenterSqr(center.x, center.y, center.z) > radius * radius) continue;
            BlockState existing = level.getBlockState(pos);
            if (existing.isAir() || !existing.getFluidState().isEmpty() || existing.hasBlockEntity()) continue;
            if (existing.getDestroySpeed(level, pos) < 0.0F || existing.getBlock().getExplosionResistance() > 80.0F) continue;
            BlockState frozen = pos.getY() >= center.y + 1.0D ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
            if (existing != frozen && level.setBlockAndUpdate(pos, frozen)) changed++;
        }
    }

    protected net.minecraft.core.particles.ParticleOptions getTrailParticle() {
        if (this.isIceball()) {
            return ParticleTypes.SNOWFLAKE;
        }
        if (this.isDreamFireball()
                && BuiltInRegistries.PARTICLE_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        "antarchy", "dream_fire_flame")) instanceof net.minecraft.core.particles.ParticleOptions dreamFlame) {
            return dreamFlame;
        }
        return ParticleTypes.FLAME;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttackType", this.entityData.get(ATTACK_TYPE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttackType(tag.getInt("AttackType"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "elemental_projectile", 0, this::animation));
    }

    private PlayState animation(AnimationState<RoyalElementalProjectileEntity> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop(this.isIceball() ? "animation.ice_ball" : "animation.fire_ball"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
