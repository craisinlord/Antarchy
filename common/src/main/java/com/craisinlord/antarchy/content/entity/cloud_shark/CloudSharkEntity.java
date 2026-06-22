package com.craisinlord.antarchy.content.entity.cloud_shark;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
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

import java.util.EnumSet;
import java.util.UUID;

public class CloudSharkEntity extends Monster implements GeoEntity {
    private static final double PATROL_SPEED   = 0.95D;
    private static final double STRAFE_SPEED   = 1.2D;
    private static final double DIVE_SPEED     = 1.9D;
    private static final double RECOVERY_SPEED = 1.15D;
    private static final double ORBIT_SPEED    = 1.1D;
    private static final double REJOIN_SPEED   = 1.25D;
    private static final int STRAFE_MIN_TICKS  = 14;
    private static final int STRAFE_MAX_TICKS  = 28;
    private static final int DIVE_RECOVERY_TICKS = 12;
    private static final int DIVE_TOTAL_TICKS  = 26;
    private static final int PATROL_MIN_TICKS  = 20;
    private static final int PATROL_MAX_TICKS  = 40;
    private static final int FLY_SOUND_INTERVAL_TICKS = 80;
    
    private static final int  SHARKNADO_REFRESH_INTERVAL = 20;
    
    private static final double REJOIN_SNAP_DIST_SQR = 9.0D;
    private static final int ANIM_IDLE   = 0;
    private static final int ANIM_SWIM   = 1;
    private static final int ANIM_ATTACK = 2;
    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SWIM_ANIM   = RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> ANIMATION_STATE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    CloudSharkEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    @Nullable private Vec3 patrolTarget;
    @Nullable private Vec3 recoveryTarget;
    @Nullable private Vec3 strafeTarget;
    @Nullable private Vec3 diveStrikeTarget;
    private int patrolRetargetTicks;
    private int currentStrafeDuration = this.nextStrafeDuration();
    private int strafeTicks    = this.currentStrafeDuration;
    private int attackTicks;
    private int diveCooldown   = 20;
    private float orbitDirection = 1.0F;
    private boolean dealtDamageThisDive;
    private double currentStrafeRadius = 4.75D;
    private double currentStrafeHeightOffset = 2.6D;
    private double currentStrafeAngleOffset;
    private int flySoundCooldown;
    
    @Nullable private UUID sharknadoId;
    private int sharknadoTier      = -1;
    private int sharknadoSlotIndex = -1;
    private SharknadoMobState sharknadoMobState = SharknadoMobState.SOLO;
    
    private int nextSharknadoRefreshTick = 0;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public CloudSharkEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FLYING_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    public static boolean canSpawn(EntityType<CloudSharkEntity> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above())
                && level.getFluidState(pos).isEmpty();
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SharknadoOrbitGoal(this));

        HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this);
        hurtByTargetGoal.setAlertOthers(CloudSharkEntity.class);
        this.targetSelector.addGoal(1, hurtByTargetGoal);
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            this.tickClientParticles();
            this.updateFlightRotation();
            return;
        }

        if (this.diveCooldown > 0) this.diveCooldown--;
        if (this.tickCount >= this.nextSharknadoRefreshTick) {
            this.nextSharknadoRefreshTick = this.tickCount + SHARKNADO_REFRESH_INTERVAL;
            if (this.sharknadoMobState == SharknadoMobState.SOLO) {
                this.refreshSharknado();
            } else {
                this.validateSharknadoMembership();
            }
        }
        if (this.tickCount % 200 == 0 && this.level() instanceof ServerLevel serverLevel) {
            SharknadoManager.get(serverLevel).cleanup(serverLevel);
        }
        switch (this.sharknadoMobState) {
            case ORBITING -> {
                if (this.sharknadoId != null && this.level() instanceof ServerLevel serverLevel) {
                    SharknadoManager.get(serverLevel).tickSharknado(this.sharknadoId, serverLevel);
                }
                this.updateFlightRotation();
                this.updateAnimationState();
                return;
            }
            case REJOINING -> {
                this.tickRejoin();
                this.updateFlightRotation();
                this.updateAnimationState();
                return;
            }
            default -> {
            }
        }
        LivingEntity target = this.getTarget();
        if ((target == null || !target.isAlive()) && this.tickCount % 10 == 0) {
            Player nearbyPlayer = this.level().getNearestPlayer(this, 32.0D);
            if (nearbyPlayer != null && this.canAttack(nearbyPlayer)) {
                this.setTarget(nearbyPlayer);
                target = nearbyPlayer;
            }
        }

        if (target != null && target.isAlive()) {
            this.tickCombatMovement(target);
        } else {
            this.tickPatrolMovement();
        }

        this.tickFlySound();

        this.updateFlightRotation();
        this.updateAnimationState();
    }

    
    private void refreshSharknado() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (this.getTarget() != null && this.getTarget().isAlive()) return;
        SharknadoManager.get(serverLevel).tryFormOrJoin(this, serverLevel);
    }

    
    private void validateSharknadoMembership() {
        if (this.sharknadoId == null) {
            this.forceResetToSolo();
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (!SharknadoManager.get(serverLevel).isInSharknado(this.getUUID())) {
            this.forceResetToSolo();
        }
    }

    
    void onJoinSharknado(UUID id, int tier, int slot) {
        this.sharknadoId       = id;
        this.sharknadoTier     = tier;
        this.sharknadoSlotIndex = slot;
        this.sharknadoMobState = SharknadoMobState.ORBITING;
        this.patrolTarget      = null;
        this.strafeTarget      = null;
        this.diveStrikeTarget  = null;
        this.setTarget(null);
    }

    
    void onLeaveSharknado() {
        this.forceResetToSolo();
    }

    
    void launchFromSharknado(Player target) {
        this.sharknadoMobState = SharknadoMobState.LAUNCHED;
        this.setTarget(target);
        this.attackTicks = 0;
        this.diveCooldown = 0;
        this.dealtDamageThisDive = false;
        this.recoveryTarget = null;
        this.diveStrikeTarget = null;
        this.startNewStrafeCycle();
    }

    
    boolean isOrbiting() {
        return this.sharknadoMobState == SharknadoMobState.ORBITING;
    }

    private void forceResetToSolo() {
        this.sharknadoId        = null;
        this.sharknadoTier      = -1;
        this.sharknadoSlotIndex = -1;
        this.sharknadoMobState  = SharknadoMobState.SOLO;
        this.recoveryTarget     = null;
        this.strafeTarget       = null;
        this.diveStrikeTarget   = null;
        this.nextSharknadoRefreshTick = this.tickCount + SHARKNADO_REFRESH_INTERVAL;
    }

    
    private void tickRejoin() {
        if (this.sharknadoId == null || !(this.level() instanceof ServerLevel serverLevel)) {
            this.forceResetToSolo();
            return;
        }
        Vec3 orbitTarget = SharknadoManager.get(serverLevel)
                .getOrbitTarget(this.sharknadoId, this.sharknadoTier, this.sharknadoSlotIndex);
        if (orbitTarget == null) {
            this.forceResetToSolo();
            return;
        }
        this.getMoveControl().setWantedPosition(orbitTarget.x, orbitTarget.y, orbitTarget.z, REJOIN_SPEED);
        if (this.distanceToSqr(orbitTarget) < REJOIN_SNAP_DIST_SQR) {
            this.sharknadoMobState = SharknadoMobState.ORBITING;
            this.setTarget(null);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity livingTarget) {
            Vec3 push = livingTarget.position().subtract(this.position());
            Vec3 horizontal = new Vec3(push.x, 0.0D, push.z);
            if (horizontal.lengthSqr() < 1.0E-4D) horizontal = this.getForward().multiply(1.0D, 0.0D, 1.0D);
            if (horizontal.lengthSqr() < 1.0E-4D) horizontal = new Vec3(1.0D, 0.0D, 0.0D);
            horizontal = horizontal.normalize();
            Vec3 sideways = new Vec3(-horizontal.z, 0.0D, horizontal.x);
            if (this.random.nextBoolean()) sideways = sideways.scale(-1.0D);
            Vec3 knockback = horizontal.scale(0.7D).add(sideways.scale(0.45D));
            livingTarget.push(knockback.x, 0.28D, knockback.z);
            this.playSound(AntarchySoundEvents.CLOUD_SHARK_BITE.get(), 1.0F, 0.85F + this.random.nextFloat() * 0.2F);
        }
        return hurt;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround,
                                   net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {}

    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) { return AntarchySoundEvents.CLOUD_SHARK_HURT.get(); }

    @Override
    protected SoundEvent getDeathSound() { return AntarchySoundEvents.CLOUD_SHARK_DEATH.get(); }

    @Override
    protected float getSoundVolume() { return 0.9F; }

    @Override
    protected boolean shouldDespawnInPeaceful() { return true; }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide
                && this.sharknadoId != null
                && this.level() instanceof ServerLevel serverLevel) {
            SharknadoManager.get(serverLevel).unregisterShark(this);
        }
        super.remove(reason);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.geoCache; }

    private void tickFlySound() {
        if (this.flySoundCooldown > 0) {
            this.flySoundCooldown--;
        }

        boolean isFlyingAnim = this.getAnimationState() == ANIM_SWIM;
        boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 0.0125D;
        if (!isFlyingAnim || !isMoving) {
            this.flySoundCooldown = 0;
            return;
        }

        if (this.flySoundCooldown > 0) {
            return;
        }

        this.playSound(AntarchySoundEvents.CLOUD_SHARK_FLY.get(), 0.7F, 1.0F);
        this.flySoundCooldown = FLY_SOUND_INTERVAL_TICKS;
    }

    private void tickPatrolMovement() {
        this.resetCombatFlightState();

        if (this.patrolRetargetTicks-- <= 0
                || this.patrolTarget == null
                || this.position().distanceToSqr(this.patrolTarget) < 5.0D) {
            this.patrolRetargetTicks = PATROL_MIN_TICKS
                    + this.random.nextInt(PATROL_MAX_TICKS - PATROL_MIN_TICKS + 1);
            this.patrolTarget = this.findPatrolTarget();
        }
        if (this.patrolTarget != null) {
            this.getMoveControl().setWantedPosition(
                    this.patrolTarget.x, this.patrolTarget.y, this.patrolTarget.z, PATROL_SPEED);
        }
    }

    private void tickCombatMovement(LivingEntity target) {
        this.patrolTarget = null;
        if (this.strafeTarget == null || this.strafeTicks <= 0) {
            this.startNewStrafeCycle();
        }

        if (this.attackTicks > 0) {
            this.tickDive(target);
            return;
        }

        this.strafeTarget = this.getStrafePoint(target);
        this.getMoveControl().setWantedPosition(
                this.strafeTarget.x, this.strafeTarget.y, this.strafeTarget.z, STRAFE_SPEED);

        if (--this.strafeTicks <= 0) {
            if (this.random.nextFloat() < 0.6F) this.orbitDirection *= -1.0F;
            this.startNewStrafeCycle();
        }

        if (this.canStartDive(target)) this.startDive(target);
    }

    private void tickDive(LivingEntity target) {
        boolean recovering = this.attackTicks <= DIVE_RECOVERY_TICKS;
        if (!recovering) {
            Vec3 strikeTarget = this.diveStrikeTarget != null
                    ? this.diveStrikeTarget
                    : this.createDiveStrikeTarget(target);
            this.getMoveControl().setWantedPosition(
                    strikeTarget.x, strikeTarget.y - 0.15D, strikeTarget.z, DIVE_SPEED);
            if (!this.dealtDamageThisDive && this.canConnectDive(target)) {
                this.doHurtTarget(target);
                this.dealtDamageThisDive = true;
                this.recoveryTarget = this.createRecoveryTarget(strikeTarget);
                this.attackTicks = DIVE_RECOVERY_TICKS;
            }
        } else {
            Vec3 retreat = this.recoveryTarget != null
                    ? this.recoveryTarget : this.createRecoveryTarget(
                            this.diveStrikeTarget != null ? this.diveStrikeTarget : target.position());
            this.getMoveControl().setWantedPosition(retreat.x, retreat.y, retreat.z, RECOVERY_SPEED);
        }

        if (--this.attackTicks <= 0) {
            this.resetCombatFlightState();
            this.diveCooldown = 18 + this.random.nextInt(16);
            if (this.sharknadoMobState == SharknadoMobState.LAUNCHED) {
                this.sharknadoMobState = SharknadoMobState.REJOINING;
                this.setTarget(null);
            }
        }
    }

    private void startDive(LivingEntity target) {
        this.attackTicks = DIVE_TOTAL_TICKS;
        this.dealtDamageThisDive = false;
        this.recoveryTarget = null;
        this.diveStrikeTarget = this.createDiveStrikeTarget(target);
        this.setAnimationState(ANIM_ATTACK);
    }

    private boolean canStartDive(LivingEntity target) {
        if (this.diveCooldown > 0 || !this.hasLineOfSight(target)) return false;
        if (this.strafeTicks > Math.max(6, this.currentStrafeDuration / 2)) return false;
        double distanceToTarget = this.distanceToSqr(target);
        return distanceToTarget >= 9.0D
                && distanceToTarget <= 144.0D
                && this.getY() > target.getY() + 0.5D;
    }

    private boolean canConnectDive(LivingEntity target) {
        return this.hasLineOfSight(target)
                && this.distanceToSqr(target) <= this.getDiveReachSqr(target);
    }

    private double getDiveReachSqr(LivingEntity target) {
        double reach = this.getBbWidth() * 1.8D + target.getBbWidth();
        return reach * reach + 1.0D;
    }

    private Vec3 getStrafePoint(LivingEntity target) {
        double angle = this.tickCount * 0.18D * this.orbitDirection + this.currentStrafeAngleOffset;
        double x = target.getX() + Math.cos(angle) * this.currentStrafeRadius;
        double z = target.getZ() + Math.sin(angle) * this.currentStrafeRadius;
        double y = target.getY() + this.currentStrafeHeightOffset
                + Math.sin((this.tickCount + this.getId()) * 0.14D) * 0.5D;
        return new Vec3(x, y, z);
    }

    private Vec3 createRecoveryTarget(Vec3 strikeTarget) {
        double radius = this.currentStrafeRadius + 1.5D + this.random.nextDouble() * 1.2D;
        double angle  = this.currentStrafeAngleOffset + (Math.PI / 2.0D) * this.orbitDirection;
        double x = strikeTarget.x + Math.cos(angle) * radius;
        double z = strikeTarget.z + Math.sin(angle) * radius;
        double y = strikeTarget.y + 2.0D + this.random.nextDouble() * 1.2D;
        return new Vec3(x, y, z);
    }

    private Vec3 createDiveStrikeTarget(LivingEntity target) {
        Vec3 predictedOffset = target.getDeltaMovement().scale(5.0D);
        return target.position()
                .add(predictedOffset)
                .add(0.0D, target.getBbHeight() * 0.45D, 0.0D);
    }

    private Vec3 findPatrolTarget() {
        BlockPos origin = this.blockPosition();
        for (int attempt = 0; attempt < 18; attempt++) {
            BlockPos candidate = origin.offset(
                    this.random.nextInt(25) - 12,
                    this.random.nextInt(11) - 4,
                    this.random.nextInt(25) - 12);
            if (!this.level().isEmptyBlock(candidate) || !this.level().isEmptyBlock(candidate.above())) continue;
            BlockPos terrainPos = this.level().getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(candidate.getX(), origin.getY(), candidate.getZ()));
            double terrainBandY = terrainPos.getY() + 6.0D + this.random.nextDouble() * 5.0D;
            double y = Math.max(candidate.getY() + 0.5D, terrainBandY);
            return new Vec3(candidate.getX() + 0.5D, y, candidate.getZ() + 0.5D);
        }
        return this.position().add(
                (this.random.nextDouble() - 0.5D) * 10.0D,
                this.random.nextDouble() * 4.0D - 1.0D,
                (this.random.nextDouble() - 0.5D) * 10.0D);
    }

    private void tickClientParticles() {
        boolean emitParticles = this.getAnimationState() == ANIM_ATTACK
                || this.getTarget() != null
                || this.sharknadoMobState == SharknadoMobState.ORBITING;

        if (emitParticles && this.tickCount % 3 == 0) {
            this.level().addParticle(
                    ParticleTypes.CLOUD,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + 0.35D + this.random.nextDouble() * 0.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    0.0D, 0.02D, 0.0D);
        }
    }

    private PlayState mainAnimController(AnimationState<CloudSharkEntity> state) {
        return switch (this.getAnimationState()) {
            case ANIM_ATTACK -> state.setAndContinue(ATTACK_ANIM);
            case ANIM_SWIM   -> state.setAndContinue(SWIM_ANIM);
            default          -> state.setAndContinue(IDLE_ANIM);
        };
    }

    private int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private void updateAnimationState() {
        if (this.attackTicks > 0) {
            this.setAnimationState(ANIM_ATTACK);
            return;
        }
        Vec3 velocity = this.getDeltaMovement();
        if (this.getTarget() != null
                || this.sharknadoMobState != SharknadoMobState.SOLO
                || velocity.lengthSqr() > 1.0E-4D) {
            this.setAnimationState(ANIM_SWIM);
            return;
        }
        this.setAnimationState(ANIM_IDLE);
    }

    private void updateFlightRotation() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.horizontalDistanceSqr() > 1.0E-4D) {
            float targetYaw = (float) (Mth.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(Mth.approachDegrees(this.getYRot(), targetYaw, 8.0F));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }
        if (velocity.lengthSqr() > 1.0E-4D) {
            float targetPitch = (float) (-(Mth.atan2(velocity.y, velocity.horizontalDistance())
                    * (180.0D / Math.PI)));
            this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, 6.0F));
        }
    }

    private int nextStrafeDuration() {
        return STRAFE_MIN_TICKS + this.random.nextInt(STRAFE_MAX_TICKS - STRAFE_MIN_TICKS + 1);
    }

    private void startNewStrafeCycle() {
        this.currentStrafeDuration = this.nextStrafeDuration();
        this.strafeTicks = this.currentStrafeDuration;
        this.currentStrafeRadius = 4.25D + this.random.nextDouble() * 1.25D;
        this.currentStrafeHeightOffset = 2.2D + this.random.nextDouble() * 0.9D;
        this.currentStrafeAngleOffset = this.random.nextDouble() * (Math.PI * 2.0D);
        this.strafeTarget = null;
    }

    private void resetCombatFlightState() {
        this.attackTicks = 0;
        this.dealtDamageThisDive = false;
        this.recoveryTarget = null;
        this.diveStrikeTarget = null;
        this.startNewStrafeCycle();
    }

    
    enum SharknadoMobState {
        
        SOLO,
        
        ORBITING,
        
        LAUNCHED,
        
        REJOINING
    }

    
    private static final class SharknadoOrbitGoal extends Goal {

        private final CloudSharkEntity shark;

        SharknadoOrbitGoal(CloudSharkEntity shark) {
            this.shark = shark;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return shark.sharknadoMobState == SharknadoMobState.ORBITING;
        }

        @Override
        public boolean canContinueToUse() {
            return shark.sharknadoMobState == SharknadoMobState.ORBITING;
        }

        @Override
        public void tick() {
            if (shark.sharknadoId == null) return;
            if (!(shark.level() instanceof ServerLevel serverLevel)) return;

            SharknadoManager manager = SharknadoManager.get(serverLevel);
            Vec3 orbitTarget = manager.getOrbitTarget(
                    shark.sharknadoId,
                    shark.sharknadoTier,
                    shark.sharknadoSlotIndex);

            if (orbitTarget == null) {
                return;
            }

            shark.getMoveControl().setWantedPosition(
                    orbitTarget.x, orbitTarget.y, orbitTarget.z, ORBIT_SPEED);
        }
    }
}
