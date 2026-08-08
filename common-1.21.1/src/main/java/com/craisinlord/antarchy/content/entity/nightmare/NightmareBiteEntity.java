package com.craisinlord.antarchy.content.entity.nightmare;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class NightmareBiteEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> PHASE_TWO = SynchedEntityData.defineId(NightmareBiteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation BITE_ANIM = RawAnimation.begin().thenPlay("bite");
    private static final int LIFETIME_TICKS = 12;
    private static final int SOUND_TICK = 7;
    private static final int DAMAGE_TICK = 8;
    private static final double START_SIDE_OFFSET = 1.35D;
    private static final double START_BACK_OFFSET = 1.4D;
    private static final double END_SIDE_OFFSET = 0.15D;
    private static final double END_BACK_OFFSET = 0.2D;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int ageTicks;
    private int approachSide = 1;
    private boolean biteSoundPlayed;
    private boolean biteDamageApplied;
    @Nullable
    private UUID targetId;

    public NightmareBiteEntity(EntityType<? extends NightmareBiteEntity> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static NightmareBiteEntity spawnAt(ServerLevel level, Player target, boolean phaseTwo) {
        NightmareBiteEntity bite = new NightmareBiteEntity(AntarchyObjects.NIGHTMARE_BITE.get(), level);
        bite.targetId = target.getUUID();
        bite.setPhaseTwo(phaseTwo);
        bite.approachSide = target.getRandom().nextBoolean() ? 1 : -1;
        bite.updateTrackingPosition(target, 0.0D);
        level.addFreshEntity(bite);
        return bite;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PHASE_TWO, false);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void lavaHurt() {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public boolean isPhaseTwo() {
        return this.entityData.get(PHASE_TWO);
    }

    private void setPhaseTwo(boolean phaseTwo) {
        this.getEntityData().set(PHASE_TWO, phaseTwo);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        LivingEntity target = this.resolveTarget();
        if (target == null || !target.isAlive() || !target.hasEffect(AntarchyObjects.DREAD.get())) {
            this.discard();
            return;
        }
        int nextAge = ++this.ageTicks;
        double progress = Math.min(1.0D, nextAge / (double) DAMAGE_TICK);
        this.updateTrackingPosition(target, progress);
        if (!this.biteSoundPlayed && nextAge >= SOUND_TICK) {
            this.biteSoundPlayed = true;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), AntarchySoundEvents.NIGHTMARE_BITE.get(), SoundSource.HOSTILE, 0.75F, 0.86F + this.random.nextFloat() * 0.1F);
        }
        if (!this.biteDamageApplied && nextAge >= DAMAGE_TICK) {
            this.biteDamageApplied = true;
            this.performBite(target);
        }
        if (nextAge >= LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Nullable
    private LivingEntity resolveTarget() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.targetId == null) {
            return null;
        }
        Entity entity = serverLevel.getEntity(this.targetId);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private void updateTrackingPosition(LivingEntity target, double progress) {
        Vec3 forward = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        double sideOffset = START_SIDE_OFFSET + (END_SIDE_OFFSET - START_SIDE_OFFSET) * progress;
        double backOffset = START_BACK_OFFSET + (END_BACK_OFFSET - START_BACK_OFFSET) * progress;
        Vec3 desiredPos = target.position()
                .add(0.0D, target.getBbHeight() * 0.55D, 0.0D)
                .add(right.scale(this.approachSide * sideOffset))
                .subtract(forward.scale(backOffset));
        Vec3 toTarget = target.getEyePosition().subtract(desiredPos);
        this.setPos(desiredPos.x, desiredPos.y, desiredPos.z);
        if (toTarget.lengthSqr() > 1.0E-4D) {
            float yaw = (float) Math.toDegrees(Math.atan2(toTarget.z, toTarget.x)) - 90.0F;
            this.setYRot(yaw);
        }
    }

    private void performBite(LivingEntity target) {
        if (target.distanceToSqr(this) > 6.25D) {
            return;
        }
        float damage = (float) Math.max(3.0D, AntarchySettings.nightmareAttackDamage() * 0.35D);
        target.hurt(this.damageSources().magic(), damage);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("PhaseTwo", this.isPhaseTwo());
        tag.putInt("AgeTicks", this.ageTicks);
        tag.putInt("ApproachSide", this.approachSide);
        tag.putBoolean("BiteSoundPlayed", this.biteSoundPlayed);
        tag.putBoolean("BiteDamageApplied", this.biteDamageApplied);
        if (this.targetId != null) {
            tag.putUUID("TargetId", this.targetId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.setPhaseTwo(tag.getBoolean("PhaseTwo"));
        this.ageTicks = tag.getInt("AgeTicks");
        this.approachSide = tag.getInt("ApproachSide");
        this.biteSoundPlayed = tag.getBoolean("BiteSoundPlayed");
        this.biteDamageApplied = tag.getBoolean("BiteDamageApplied");
        if (tag.hasUUID("TargetId")) {
            this.targetId = tag.getUUID("TargetId");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "bite_controller", 0, this::biteController));
    }

    private PlayState biteController(AnimationState<NightmareBiteEntity> state) {
        return state.setAndContinue(BITE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
