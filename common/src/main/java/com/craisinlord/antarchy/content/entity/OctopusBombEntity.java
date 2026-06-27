package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Objects;

public class OctopusBombEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ATTACK_STATE = SynchedEntityData.defineId(OctopusBombEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(OctopusBombEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CAN_FLY = SynchedEntityData.defineId(OctopusBombEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ATTACK_NONE = 0;
    private static final int ATTACK_GRAB = 1;
    private static final int ATTACK_SWIPE = 2;

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_SWIM = 2;
    private static final int ANIM_SWIPE = 3;
    private static final int ANIM_GRAB = 4;
    private static final int ANIM_DEATH = 5;

    private static final int DEATH_ANIM_TICKS = 60;
    private static final int INK_SPRAY_RANGE = 8;
    private static final int INK_SPRAY_COOLDOWN = 80;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SWIM_ANIM = RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation SWIPE_ANIM = RawAnimation.begin().thenPlay("Swipe");
    private static final RawAnimation GRAB_ANIM = RawAnimation.begin().thenPlay("Grab");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("Death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    // Squid-style wander
    private float wanderDx, wanderDy, wanderDz;
    private int wanderRetargetTicks;

    // Combat
    private int attackCooldown;
    private int actionTicks;
    private int lastAttackState;
    private int grabbedTargetId = -1;
    private int strafeRetargetTicks;
    private float orbitDirection = 1.0F;
    private int inkSprayCooldown;

    // Kraken spawn flag
    private boolean spawnedByKraken;

    public OctopusBombEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 80, 8, 0.06F, 0.12F, true);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FLYING_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    public static boolean canSpawn(EntityType<OctopusBombEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, net.minecraft.core.BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER)
                && level.getFluidState(pos.above()).is(net.minecraft.tags.FluidTags.WATER);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_STATE, ATTACK_NONE);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(CAN_FLY, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (CAN_FLY.equals(key)) {
            this.moveControl = this.canFly()
                    ? new FlyingMoveControl(this, 20, true)
                    : new SmoothSwimmingMoveControl(this, 80, 8, 0.06F, 0.12F, true);
            this.navigation = this.canFly()
                    ? new FlyingPathNavigation(this, this.level())
                    : new WaterBoundPathNavigation(this, this.level());
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Only retaliate when hit — neutral otherwise
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // When spawned by Kraken and flying, target nearest player
        this.targetSelector.addGoal(2, new KrakenSummonTargetGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnData) {
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(com.craisinlord.antarchy.config.AntarchySettings.octopusBombHealth());
        this.setHealth((float) com.craisinlord.antarchy.config.AntarchySettings.octopusBombHealth());
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(com.craisinlord.antarchy.config.AntarchySettings.octopusBombAttackDamage());
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController)
                .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>()));
    }

    private PlayState mainAnimController(AnimationState<OctopusBombEntity> state) {
        return switch (this.getAnimationState()) {
            case ANIM_GRAB -> state.setAndContinue(GRAB_ANIM);
            case ANIM_SWIPE -> state.setAndContinue(SWIPE_ANIM);
            case ANIM_SWIM -> state.setAndContinue(SWIM_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void baseTick() {
        int airSupply = this.getAirSupply();
        super.baseTick();
        if (!this.canFly()) {
            this.handleAirSupply(airSupply);
        } else {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(this.isInWater() || this.canFly());

        if (this.level().isClientSide) {
            this.updateSwimRotation();
            return;
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.inkSprayCooldown > 0) this.inkSprayCooldown--;

        // Ink spray — always active regardless of aggro state
        this.tickInkSpray();

        LivingEntity target = this.getTarget();

        if (target == null || !target.isAlive()) {
            this.resetCombatState();
            if (this.isInWater()) this.tickWanderMovement();
            this.updateSwimRotation();
            this.updateAnimationState();
            return;
        }

        this.getLookControl().setLookAt(target, 25.0F, 20.0F);

        if (this.getAttackState() != ATTACK_NONE) {
            this.tickCurrentAttack(target);
        } else {
            this.tickPursuitMovement(target);
            if (this.attackCooldown <= 0) {
                int nextAttack = this.chooseNextAttack(target);
                if (nextAttack != ATTACK_NONE) {
                    this.startAttack(nextAttack);
                }
            }
        }

        this.updateSwimRotation();
        this.updateAnimationState();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && (this.isInWater() || this.canFly())) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(this.isInWater() ? 0.92D : 0.91D));
            return;
        }
        super.travel(travelVector);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.KRAKEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.KRAKEN_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.2F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttackCooldown", this.attackCooldown);
        tag.putInt("ActionTicks", this.actionTicks);
        tag.putInt("LastAttackState", this.lastAttackState);
        tag.putBoolean("CanFly", this.canFly());
        tag.putBoolean("SpawnedByKraken", this.spawnedByKraken);
        if (this.grabbedTargetId >= 0) tag.putInt("GrabTarget", this.grabbedTargetId);
        tag.putInt("AttackState", this.getAttackState());
        tag.putInt("AnimationState", this.getAnimationState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.attackCooldown = tag.getInt("AttackCooldown");
        this.actionTicks = tag.getInt("ActionTicks");
        this.lastAttackState = tag.contains("LastAttackState") ? tag.getInt("LastAttackState") : ATTACK_NONE;
        this.setCanFly(tag.getBoolean("CanFly"));
        this.spawnedByKraken = tag.getBoolean("SpawnedByKraken");
        this.grabbedTargetId = tag.contains("GrabTarget") ? tag.getInt("GrabTarget") : -1;
        this.entityData.set(ATTACK_STATE, tag.getInt("AttackState"));
        this.entityData.set(ANIMATION_STATE, tag.contains("AnimationState") ? tag.getInt("AnimationState") : ANIM_IDLE);
    }

    public boolean canFly() {
        return this.entityData.get(CAN_FLY);
    }

    public void setCanFly(boolean canFly) {
        this.entityData.set(CAN_FLY, canFly);
    }

    public int getAttackState() {
        return this.entityData.get(ATTACK_STATE);
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAttackState(int state) {
        this.entityData.set(ATTACK_STATE, state);
    }

    private void setAnimationState(int state) {
        this.entityData.set(ANIMATION_STATE, state);
    }

    public void launchAsProjectile(net.minecraft.world.entity.Entity owner, Vec3 velocity) {
        this.spawnedByKraken = true;
        this.setCanFly(true);
        this.setDeltaMovement(velocity);
    }

    // -------------------------------------------------------------------------
    // Squid-style wander
    // -------------------------------------------------------------------------

    private void tickWanderMovement() {
        if (--this.wanderRetargetTicks <= 0) {
            this.wanderRetargetTicks = 40 + this.random.nextInt(40);
            float angle = this.random.nextFloat() * (float) (Math.PI * 2);
            this.wanderDx = Mth.cos(angle) * 0.1f;
            this.wanderDy = -0.05f + this.random.nextFloat() * 0.1f;
            this.wanderDz = Mth.sin(angle) * 0.1f;
        }
        this.setDeltaMovement(this.getDeltaMovement().add(this.wanderDx, this.wanderDy, this.wanderDz));
    }

    // -------------------------------------------------------------------------
    // Ink spray
    // -------------------------------------------------------------------------

    private void tickInkSpray() {
        if (this.inkSprayCooldown > 0 || !(this.level() instanceof ServerLevel serverLevel)) return;

        Player nearbyPlayer = this.level().getNearestPlayer(this, INK_SPRAY_RANGE);
        if (nearbyPlayer == null || nearbyPlayer.isCreative() || nearbyPlayer.isSpectator()) return;

        this.inkSprayCooldown = INK_SPRAY_COOLDOWN;
        this.playSound(AntarchySoundEvents.KRAKEN_HURT.get(), 1.0F, 1.49F);

        double cx = this.getX();
        double cy = this.getY() + this.getBbHeight() * 0.5D;
        double cz = this.getZ();
        serverLevel.sendParticles(ParticleTypes.SQUID_INK, cx, cy, cz, 120, 2.0D, 1.2D, 2.0D, 0.04D);
        serverLevel.sendParticles(ParticleTypes.SQUID_INK, cx, cy, cz,  60, 3.5D, 1.8D, 3.5D, 0.02D);
    }

    // -------------------------------------------------------------------------
    // Combat
    // -------------------------------------------------------------------------

    private void tickPursuitMovement(LivingEntity target) {
        if (this.strafeRetargetTicks-- <= 0) {
            this.strafeRetargetTicks = 10 + this.random.nextInt(12);
            if (this.random.nextFloat() < 0.4F) this.orbitDirection *= -1.0F;
        }
        Vec3 attackPoint = this.createCombatTarget(target);
        this.getMoveControl().setWantedPosition(attackPoint.x, attackPoint.y, attackPoint.z, 1.1D);
    }

    private Vec3 createCombatTarget(LivingEntity target) {
        double radius = 9.0D;
        double angle = this.tickCount * 0.16D * this.orbitDirection + this.getId() * 0.11D;
        double x = target.getX() + Math.cos(angle) * radius;
        double z = target.getZ() + Math.sin(angle) * radius;
        double y = Mth.clamp(
                target.getY() + 1.0D + Math.sin((this.tickCount + this.getId()) * 0.18D),
                target.getY() - 3.0D,
                target.getY() + 4.0D
        );
        return new Vec3(x, y, z);
    }

    private void tickCurrentAttack(LivingEntity target) {
        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 25.0F);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));

        switch (this.getAttackState()) {
            case ATTACK_GRAB -> this.tickGrabAttack(target);
            case ATTACK_SWIPE -> this.tickSwipeAttack();
            default -> this.finishAttack();
        }
    }

    private void startAttack(int attackState) {
        this.setAttackState(attackState);
        this.grabbedTargetId = -1;
        if (attackState == ATTACK_GRAB) {
            this.actionTicks = 55;
            this.playSound(AntarchySoundEvents.KRAKEN_ATTACK.get(), 1.2F, 1.07F);
        } else if (attackState == ATTACK_SWIPE) {
            this.actionTicks = 40;
            this.playSound(AntarchySoundEvents.KRAKEN_SPIN.get(), 1.1F, 1.13F);
        }
    }

    private void tickGrabAttack(LivingEntity target) {
        if (this.actionTicks == 48 && this.distanceToSqr(target) <= 42.25D) {
            this.grabbedTargetId = target.getId();
        }

        LivingEntity grabbed = this.getGrabbedTarget();
        if (grabbed != null && grabbed.isAlive()) {
            this.holdGrabbedTarget(grabbed);
            if (this.actionTicks <= 45 && this.actionTicks > 10 && this.actionTicks % 10 == 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    grabbed.hurt(AntarchyDamageSources.krakenMauling(serverLevel, this), 4.0F);
                }
            }
        }

        if (--this.actionTicks <= 0) {
            if (grabbed != null && grabbed.isAlive()) this.pullDown(grabbed, 2.5D);
            this.grabbedTargetId = -1;
            this.finishAttack();
        }
    }

    private void tickSwipeAttack() {
        if (this.actionTicks == 10) this.performSwipe();
        if (--this.actionTicks <= 0) this.finishAttack();
    }

    private void finishAttack() {
        int finished = this.getAttackState();
        this.setAttackState(ATTACK_NONE);
        this.actionTicks = 0;
        this.attackCooldown = 18;
        this.grabbedTargetId = -1;
        this.lastAttackState = finished;
    }

    private int chooseNextAttack(LivingEntity target) {
        double distSqr = this.distanceToSqr(target);
        boolean canGrab = distSqr <= 42.25D;
        boolean canSwipe = distSqr <= 81.0D && this.isTargetInFront(target, -0.15D);
        if (!canGrab && !canSwipe) return ATTACK_NONE;

        int grabWeight = canGrab ? (this.lastAttackState == ATTACK_GRAB ? 1 : 4) : 0;
        int swipeWeight = canSwipe ? (this.lastAttackState == ATTACK_SWIPE ? 1 : 5) : 0;
        int total = grabWeight + swipeWeight;
        if (total == 0) return ATTACK_NONE;

        int roll = this.random.nextInt(total);
        return roll < grabWeight ? ATTACK_GRAB : ATTACK_SWIPE;
    }

    private void performSwipe() {
        AABB hitBox = this.getBoundingBox().inflate(7.0D, 3.0D, 7.0D);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, hitBox,
                p -> p.isAlive() && this.isTargetInFront(p, -0.15D));
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (Player player : players) {
            if (this.level() instanceof ServerLevel serverLevel) {
                player.hurt(AntarchyDamageSources.krakenMauling(serverLevel, this), damage);
                Vec3 dir = player.position().subtract(this.position());
                Vec3 horizontal = new Vec3(dir.x, 0.0D, dir.z).normalize().scale(1.75D);
                player.push(horizontal.x, 0.3D, horizontal.z);
                player.hurtMarked = true;
            }
        }
    }

    private void holdGrabbedTarget(LivingEntity target) {
        Vec3 anchor = this.position().add(0.0D, -this.getBbHeight() * 0.4D, 0.0D);
        target.setDeltaMovement(anchor.subtract(target.position()).scale(0.35D));
        target.resetFallDistance();
        if (target instanceof ServerPlayer sp) {
            sp.connection.teleport(anchor.x, anchor.y, anchor.z, target.getYRot(), target.getXRot());
        } else {
            target.moveTo(anchor.x, anchor.y, anchor.z, target.getYRot(), target.getXRot());
        }
        target.hurtMarked = true;
    }

    private void pullDown(LivingEntity target, double strength) {
        target.push(0.0D, -strength, 0.0D);
        target.hurtMarked = true;
    }

    @Nullable
    private LivingEntity getGrabbedTarget() {
        if (this.grabbedTargetId < 0) return null;
        net.minecraft.world.entity.Entity e = this.level().getEntity(this.grabbedTargetId);
        return e instanceof LivingEntity le ? le : null;
    }

    private boolean isTargetInFront(LivingEntity target, double minDot) {
        Vec3 forward = this.getViewVector(1.0F);
        Vec3 toTarget = target.position().subtract(this.position()).normalize();
        return forward.dot(toTarget) >= minDot;
    }

    private void resetCombatState() {
        this.setAttackState(ATTACK_NONE);
        this.setTarget(null);
        this.actionTicks = 0;
        this.grabbedTargetId = -1;
    }

    private void handleAirSupply(int previousAirSupply) {
        if (this.isInWaterOrBubble()) {
            this.setAirSupply(this.getMaxAirSupply());
            return;
        }
        this.setAirSupply(previousAirSupply - 1);
        if (this.getAirSupply() <= -20) {
            this.setAirSupply(0);
            this.hurt(this.damageSources().drown(), 2.0F);
        }
    }

    private void updateSwimRotation() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.horizontalDistanceSqr() > 1.0E-4D) {
            float targetYaw = (float) (Mth.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(Mth.approachDegrees(this.getYRot(), targetYaw, 6.0F));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }
        if (velocity.lengthSqr() > 1.0E-4D) {
            float targetPitch = (float) (-(Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI)));
            this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, 4.0F));
        }
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }
        switch (this.getAttackState()) {
            case ATTACK_GRAB -> { this.setAnimationState(ANIM_GRAB); return; }
            case ATTACK_SWIPE -> { this.setAnimationState(ANIM_SWIPE); return; }
            default -> {}
        }
        if (!this.isInWater()) {
            this.setAnimationState(ANIM_SWIM);
            return;
        }
        Vec3 vel = this.getDeltaMovement();
        double hSqr = vel.horizontalDistanceSqr();
        double vSqr = vel.y * vel.y;
        if (hSqr > 0.03D && hSqr > vSqr * 2.5D) {
            this.setAnimationState(ANIM_SWIM);
            return;
        }
        this.setAnimationState(ANIM_IDLE);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.setAnimationState(ANIM_DEATH);
            this.setAttackState(ATTACK_NONE);
            this.setDeltaMovement(Vec3.ZERO);
            this.getNavigation().stop();
        }
        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 1) this.setAnimationState(ANIM_DEATH);
        if (this.deathTime >= DEATH_ANIM_TICKS) {
            this.remove(RemovalReason.KILLED);
            this.dropExperience(this);
        }
    }

    // -------------------------------------------------------------------------
    // Inner goal: only target players when spawned by Kraken in fly mode
    // -------------------------------------------------------------------------

    private static final class KrakenSummonTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final OctopusBombEntity octopus;

        KrakenSummonTargetGoal(OctopusBombEntity octopus) {
            super(octopus, Player.class, true);
            this.octopus = octopus;
        }

        @Override
        public boolean canUse() {
            return this.octopus.spawnedByKraken && this.octopus.canFly() && super.canUse();
        }
    }
}
