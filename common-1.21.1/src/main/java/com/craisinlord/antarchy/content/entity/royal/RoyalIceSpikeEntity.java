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
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(this.blockPosition()).inflate(2.2D), entity -> entity.isAlive())) {
            Entity owner = this.ownerId == null ? null : level.getEntity(this.ownerId);
            living.hurt(source, owner instanceof RoyalBossEntity royalBoss
                    ? royalBoss.scaleRoyalDamage(AntarchySettings.kingIceSpikeDamage())
                    : (float) AntarchySettings.kingIceSpikeDamage());
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 60, living.getTicksFrozen() + 100));
            living.setDeltaMovement(living.getDeltaMovement().add(0.0D, 0.35D, 0.0D));
            living.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.5D, this.getZ(), 25, 1.5D, 1.0D, 1.5D, 0.04D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.impacted = tag.getBoolean("Impacted");
        if (tag.hasUUID("Owner")) {
            this.ownerId = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Impacted", this.impacted);
        if (this.ownerId != null) {
            tag.putUUID("Owner", this.ownerId);
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
