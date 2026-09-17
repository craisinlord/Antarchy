package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RoyalIceSpikeEntity extends Entity implements GeoEntity {
    private static final RawAnimation SPIKE_ANIMATION = RawAnimation.begin().thenPlay("animation");
    private static final String ANIMATION_CONTROLLER = "ice_spike";
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private boolean impacted;
    @Nullable
    private UUID ownerId;
    private float customImpactDamage = -1.0F;
    private double customImpactRadius = 2.2D;
    private int customFreezeTicks = 100;
    private int customSlowTicks;
    private int customSlowAmplifier;
    private double customKnockup = 0.35D;

    public RoyalIceSpikeEntity(EntityType<? extends RoyalIceSpikeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static RoyalIceSpikeEntity create(ServerLevel level, Vec3 position) {
        RoyalIceSpikeEntity spike = new RoyalIceSpikeEntity(AntarchyObjects.ROYAL_ICE_SPIKE.get(), level);
        spike.setPos(position.x, position.y, position.z);
        return spike;
    }

    public static RoyalIceSpikeEntity create(ServerLevel level, Vec3 position, LivingEntity owner) {
        RoyalIceSpikeEntity spike = create(level, position);
        spike.ownerId = owner.getUUID();
        return spike;
    }

    public static RoyalIceSpikeEntity create(ServerLevel level, Vec3 position, LivingEntity owner,
            float damage, double radius, int freezeTicks, int slowTicks, int slowAmplifier) {
        RoyalIceSpikeEntity spike = create(level, position, owner);
        spike.customImpactDamage = damage;
        spike.customImpactRadius = radius;
        spike.customFreezeTicks = freezeTicks;
        spike.customSlowTicks = slowTicks;
        spike.customSlowAmplifier = slowAmplifier;
        spike.customKnockup = 0.1D;
        return spike;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount == 1) {
            this.triggerAnim(ANIMATION_CONTROLLER, "emerge");
        }
        if (!this.level().isClientSide && this.tickCount == 6 && !this.impacted) {
            this.impacted = true;
            this.impact((ServerLevel) this.level());
        }
        if (!this.level().isClientSide && this.tickCount > 110) {
            this.discard();
        }
        if (this.level().isClientSide && this.tickCount < 10) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + this.tickCount * 0.4D, this.getZ(), 0.0D, 0.03D, 0.0D);
        }
    }

    private void impact(ServerLevel level) {
        DamageSource source = this.damageSources().magic();
        Entity owner = this.ownerId == null ? null : level.getEntity(this.ownerId);
        if (this.ownerId != null && owner == null) return;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(this.blockPosition()).inflate(this.customImpactRadius), entity -> entity.isAlive()
                        && entity.distanceToSqr(this.position()) <= this.customImpactRadius * this.customImpactRadius
                        && canHitOwnerTarget(owner, entity))) {
            float damage = this.customImpactDamage >= 0.0F ? this.customImpactDamage
                    : owner instanceof RoyalBossEntity royalBoss
                            ? royalBoss.scaleRoyalDamage(AntarchySettings.kingIceSpikeDamage())
                            : (float) AntarchySettings.kingIceSpikeDamage();
            boolean hit = owner instanceof net.minecraft.world.entity.player.Player player && this.customImpactDamage >= 0.0F
                    ? com.craisinlord.antarchy.content.item.RoyalGuardianSwordAbilities.applySecondaryDamage(player, living, damage)
                    : living.hurt(source, damage);
            if (!hit) continue;
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 60, living.getTicksFrozen() + this.customFreezeTicks));
            if (this.customSlowTicks > 0) {
                living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, this.customSlowTicks, this.customSlowAmplifier));
            }
            living.setDeltaMovement(living.getDeltaMovement().add(0.0D, this.customKnockup, 0.0D));
            living.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.5D, this.getZ(), 25, 1.5D, 1.0D, 1.5D, 0.04D);
    }

    private boolean canHitOwnerTarget(@Nullable Entity owner, LivingEntity target) {
        if (owner == null) return this.ownerId == null;
        if (target == owner || target.isAlliedTo(owner) || owner.isAlliedTo(target)) return false;
        if (owner instanceof RoyalBossEntity royalBoss) return royalBoss.canDamageWithRoyalAttack(target);
        if (owner instanceof net.minecraft.world.entity.player.Player player) {
            return com.craisinlord.antarchy.content.item.RoyalGuardianSwordAbilities.canDamageTarget(player, target, false);
        }
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.impacted = tag.getBoolean("Impacted");
        if (tag.hasUUID("Owner")) {
            this.ownerId = tag.getUUID("Owner");
        }
        if (tag.contains("CustomImpactDamage")) {
            this.customImpactDamage = tag.getFloat("CustomImpactDamage");
            this.customImpactRadius = tag.getDouble("CustomImpactRadius");
            this.customFreezeTicks = tag.getInt("CustomFreezeTicks");
            this.customSlowTicks = tag.getInt("CustomSlowTicks");
            this.customSlowAmplifier = tag.getInt("CustomSlowAmplifier");
            this.customKnockup = tag.getDouble("CustomKnockup");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Impacted", this.impacted);
        if (this.ownerId != null) {
            tag.putUUID("Owner", this.ownerId);
        }
        if (this.customImpactDamage >= 0.0F) {
            tag.putFloat("CustomImpactDamage", this.customImpactDamage);
            tag.putDouble("CustomImpactRadius", this.customImpactRadius);
            tag.putInt("CustomFreezeTicks", this.customFreezeTicks);
            tag.putInt("CustomSlowTicks", this.customSlowTicks);
            tag.putInt("CustomSlowAmplifier", this.customSlowAmplifier);
            tag.putDouble("CustomKnockup", this.customKnockup);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, ANIMATION_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim("emerge", SPIKE_ANIMATION));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
