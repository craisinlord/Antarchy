package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

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
        if (!this.level().isClientSide() && this.tickCount == 6 && !this.impacted) {
            this.impacted = true;
            this.impact((ServerLevel) this.level());
        }
        if (!this.level().isClientSide() && this.tickCount > 110) {
            this.discard();
        }
        if (this.level().isClientSide() && this.tickCount < 10) {
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
                    : living.hurtServer(level, source, damage);
            if (!hit) continue;
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 60, living.getTicksFrozen() + this.customFreezeTicks));
            if (this.customSlowTicks > 0) {
                living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.SLOWNESS, this.customSlowTicks, this.customSlowAmplifier));
            }
            living.setDeltaMovement(living.getDeltaMovement().add(0.0D, this.customKnockup, 0.0D));
            living.needsSync = true;
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!this.isInvulnerableToBase(source)) {
            this.markHurt();
        }
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.impacted = input.getBooleanOr("Impacted", false);
        input.read("Owner", UUIDUtil.CODEC).ifPresent(id -> this.ownerId = id);
        if (input.read("CustomImpactDamage", com.mojang.serialization.Codec.FLOAT).isPresent()) {
            this.customImpactDamage = input.getFloatOr("CustomImpactDamage", 0.0F);
            this.customImpactRadius = input.getDoubleOr("CustomImpactRadius", 0.0D);
            this.customFreezeTicks = input.getIntOr("CustomFreezeTicks", 0);
            this.customSlowTicks = input.getIntOr("CustomSlowTicks", 0);
            this.customSlowAmplifier = input.getIntOr("CustomSlowAmplifier", 0);
            this.customKnockup = input.getDoubleOr("CustomKnockup", 0.0D);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putBoolean("Impacted", this.impacted);
        if (this.ownerId != null) {
            output.store("Owner", UUIDUtil.CODEC, this.ownerId);
        }
        if (this.customImpactDamage >= 0.0F) {
            output.putFloat("CustomImpactDamage", this.customImpactDamage);
            output.putDouble("CustomImpactRadius", this.customImpactRadius);
            output.putInt("CustomFreezeTicks", this.customFreezeTicks);
            output.putInt("CustomSlowTicks", this.customSlowTicks);
            output.putInt("CustomSlowAmplifier", this.customSlowAmplifier);
            output.putDouble("CustomKnockup", this.customKnockup);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<RoyalIceSpikeEntity>(ANIMATION_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim("emerge", SPIKE_ANIMATION));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
