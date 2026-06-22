package com.craisinlord.antarchy.content.entity.lucid;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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
import java.util.function.Supplier;

public class LucidEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(LucidEntity.class, EntityDataSerializers.INT);

    private static final String ATTACK_COOLDOWN_KEY = "AttackCooldown";
    private static final String ATTACK_ANIMATION_TICKS_KEY = "AttackAnimationTicks";
    private static final String BURST_SHOTS_REMAINING_KEY = "BurstShotsRemaining";
    private static final String BURST_SHOT_DELAY_KEY = "BurstShotDelay";

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_FLY = 1;
    private static final int ANIM_ATTACK = 2;
    private static final int ANIM_DEATH = 3;

    public static Supplier<Holder<MobEffect>> invertedEffectSupplier;
    public static Supplier<EntityType<LucidBoltEntity>> boltEntityTypeSupplier;

    private static final int ATTACK_WINDUP_TICKS = 16;
    private static final int ATTACK_RECOVERY_TICKS = 8;
    private static final int ATTACK_BURST_SHOTS = 3;
    private static final int ATTACK_BURST_INTERVAL_TICKS = 6;
    private static final int ATTACK_COOLDOWN_TICKS = 52;
    private static final int DEATH_TICKS = 15;

    private static final double MIN_EFFECTIVE_ATTACK_RANGE = 10.0D;
    private static final double MIN_HOVER_RANGE = 6.5D;
    private static final double CLOSE_RANGE = 4.5D;
    private static final double DEFAULT_HOVER_HEIGHT = 2.75D;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int attackCooldown;
    private int attackAnimationTicks;
    private int burstShotsRemaining;
    private int burstShotDelayTicks;

    public LucidEntity(EntityType<? extends LucidEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 2, true);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.lucidHealth())
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FLYING_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    public static boolean canSpawn(EntityType<LucidEntity> entityType, ServerLevelAccessor level,
            MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && pos.getY() <= 250
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above())
                && level.isEmptyBlock(pos.above(2));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LucidPatrolGoal());
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 20, true, false, this::canTargetEntity));
    }

    @Override
    public int getMaxHeadYRot() {
        return 360;
    }

    @Override
    public int getMaxHeadXRot() {
        return 45;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<LucidEntity> state) {
        return switch (this.getAnimationState()) {
            case ANIM_FLY -> state.setAndContinue(FLY_ANIM);
            case ANIM_ATTACK -> state.setAndContinue(ATTACK_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            return;
        }

        if (this.isDeadOrDying()) {
            this.updateAnimationState();
            return;
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        LivingEntity target = this.getTarget();
        if (this.attackAnimationTicks > 0) {
            this.tickAttack(target);
            this.updateAnimationState();
            return;
        }

        if (target != null && this.canTargetEntity(target)) {
            this.tickCombatMovement(target);
        }

        this.updateAnimationState();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() || this.isNoGravity()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            return;
        }
        super.travel(travelVector);
    }

    private void tickCombatMovement(LivingEntity target) {
        double distanceSqr = this.distanceToSqr(target);

        this.faceTowards(target.getEyePosition(), 12.0F, 8.0F);
        this.getMoveControl().setWantedPosition(target.getX(), target.getY() + 2.0D, target.getZ(), 0.42D);

        if (this.attackCooldown <= 0
                && distanceSqr <= this.getAttackRange() * this.getAttackRange() * 1.1D
                && this.hasLineOfSight(target)) {
            this.startAttack();
        }
    }

    private void startAttack() {
        this.attackAnimationTicks = ATTACK_WINDUP_TICKS + ATTACK_RECOVERY_TICKS + (ATTACK_BURST_SHOTS - 1) * ATTACK_BURST_INTERVAL_TICKS;
        this.burstShotsRemaining = ATTACK_BURST_SHOTS;
        this.burstShotDelayTicks = ATTACK_WINDUP_TICKS;
        this.attackCooldown = ATTACK_COOLDOWN_TICKS;
        this.getNavigation().stop();
    }

    private void tickAttack(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.84D));

        if (target == null || !this.canTargetEntity(target)) {
            this.attackAnimationTicks = 0;
            this.burstShotsRemaining = 0;
            this.burstShotDelayTicks = 0;
            return;
        }

        this.faceTowards(target.getEyePosition(), 16.0F, 10.0F);

        double riseTargetY = Math.max(this.getY() + 1.25D, target.getY() + 3.0D);
        this.getMoveControl().setWantedPosition(this.getX(), riseTargetY, this.getZ(), 0.42D);

        if (this.burstShotDelayTicks > 0) {
            this.burstShotDelayTicks--;
        }
        if (this.burstShotsRemaining > 0 && this.burstShotDelayTicks <= 0) {
            this.fireBoltAt(target);
            this.burstShotsRemaining--;
            this.burstShotDelayTicks = this.burstShotsRemaining > 0 ? ATTACK_BURST_INTERVAL_TICKS : ATTACK_RECOVERY_TICKS;
        }

        if (--this.attackAnimationTicks <= 0) {
            this.attackAnimationTicks = 0;
        }
    }

    private void fireBoltAt(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel) || boltEntityTypeSupplier == null) {
            return;
        }

        LucidBoltEntity bolt = new LucidBoltEntity(boltEntityTypeSupplier.get(), this, serverLevel);
        Vec3 origin = this.getEyePosition();
        Vec3 lead = target.getDeltaMovement().scale(0.35D);
        Vec3 aimPoint = target.getEyePosition().add(lead);
        Vec3 toTarget = aimPoint.subtract(origin);
        if (toTarget.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 direction = toTarget.normalize();
        Vec3 launchPos = origin.add(direction.scale(0.75D));
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = (float) (Mth.atan2(direction.z, direction.x) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(direction.y, horizontal) * (180.0D / Math.PI)));

        bolt.setPos(launchPos.x, launchPos.y, launchPos.z);
        bolt.setYRot(yaw);
        bolt.setXRot(pitch);
        bolt.shoot(
                direction.x + (this.random.nextDouble() - 0.5D) * 0.18D,
                direction.y + (this.random.nextDouble() - 0.5D) * 0.12D,
                direction.z + (this.random.nextDouble() - 0.5D) * 0.18D,
                0.71F,
                0.0F
        );
        serverLevel.addFreshEntity(bolt);
    }


    private double getAttackRange() {
        return Math.max(MIN_EFFECTIVE_ATTACK_RANGE, AntarchySettings.lucidAttackRange());
    }

    private void faceTowards(Vec3 targetPos, float maxYawStep, float maxPitchStep) {
        Vec3 delta = targetPos.subtract(this.getEyePosition());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontal < 1.0E-6D && Math.abs(delta.y) < 1.0E-6D) {
            return;
        }

        float targetYaw = (float) (Mth.atan2(delta.z, delta.x) * (180.0D / Math.PI)) - 90.0F;
        float targetPitch = (float) (-(Mth.atan2(delta.y, horizontal) * (180.0D / Math.PI)));
        float nextYaw = Mth.approachDegrees(this.getYRot(), targetYaw, maxYawStep);
        float nextPitch = Mth.approachDegrees(this.getXRot(), targetPitch, maxPitchStep);

        this.setYRot(nextYaw);
        this.setYHeadRot(nextYaw);
        this.yBodyRot = nextYaw;
        this.setXRot(nextPitch);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.setAnimationState(ANIM_DEATH);
            this.attackAnimationTicks = 0;
            this.attackCooldown = 0;
            this.burstShotsRemaining = 0;
            this.burstShotDelayTicks = 0;
            this.setDeltaMovement(Vec3.ZERO);
            this.getNavigation().stop();
        }
        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;

        if (this.deathTime == 1) {
            this.setAnimationState(ANIM_DEATH);
        }

        if (this.deathTime >= DEATH_TICKS) {
            this.remove(RemovalReason.KILLED);
            this.dropExperience(this);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void lavaHurt() {
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround,
            net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.canTargetEntity(target) && super.canAttack(target);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.LUCID_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.LUCID_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.9F;
    }

    @Override
    public float getVoicePitch() {
        return 1.4F + this.random.nextFloat() * 0.2F;
    }

    public boolean shouldPlayAmbientLoop() {
        return !this.isDeadOrDying() && this.getAnimationState() != ANIM_DEATH;
    }

    public boolean shouldPlayFlyingLoop() {
        return this.shouldPlayAmbientLoop() && this.getAnimationState() != ANIM_IDLE;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(ATTACK_COOLDOWN_KEY, this.attackCooldown);
        tag.putInt(ATTACK_ANIMATION_TICKS_KEY, this.attackAnimationTicks);
        tag.putInt(BURST_SHOTS_REMAINING_KEY, this.burstShotsRemaining);
        tag.putInt(BURST_SHOT_DELAY_KEY, this.burstShotDelayTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.attackCooldown = tag.getInt(ATTACK_COOLDOWN_KEY);
        this.attackAnimationTicks = tag.getInt(ATTACK_ANIMATION_TICKS_KEY);
        this.burstShotsRemaining = tag.getInt(BURST_SHOTS_REMAINING_KEY);
        this.burstShotDelayTicks = tag.getInt(BURST_SHOT_DELAY_KEY);
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int state) {
        this.entityData.set(ANIMATION_STATE, state);
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }
        if (this.attackAnimationTicks > 0) {
            this.setAnimationState(ANIM_ATTACK);
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && this.canTargetEntity(target)) {
            this.setAnimationState(ANIM_FLY);
            return;
        }
        this.setAnimationState(ANIM_IDLE);
    }

    private boolean canTargetEntity(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this || entity.getType() == this.getType()) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator()
                    && this.level().getDifficulty() != Difficulty.PEACEFUL;
        }
        return entity.isAttackable();
    }

    private final class LucidPatrolGoal extends Goal {
        private static final int RETARGET_DELAY_MIN = 40;
        private static final int RETARGET_DELAY_MAX = 80;
        private static final double PATROL_SPEED = 0.18D;

        private int retargetDelay;

        private LucidPatrolGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return LucidEntity.this.getTarget() == null
                    && LucidEntity.this.attackAnimationTicks <= 0
                    && LucidEntity.this.getNavigation().isDone();
        }

        @Override
        public boolean canContinueToUse() {
            return LucidEntity.this.getTarget() == null
                    && LucidEntity.this.attackAnimationTicks <= 0
                    && LucidEntity.this.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            this.retargetDelay = Mth.nextInt(LucidEntity.this.random, RETARGET_DELAY_MIN, RETARGET_DELAY_MAX);
            this.moveToNewTarget();
        }

        @Override
        public void stop() {
            LucidEntity.this.getNavigation().stop();
            LucidEntity.this.setDeltaMovement(Vec3.ZERO);
            this.retargetDelay = 0;
        }

        @Override
        public void tick() {
            if (LucidEntity.this.getTarget() != null || LucidEntity.this.attackAnimationTicks > 0) {
                this.stop();
                return;
            }

            if (LucidEntity.this.getNavigation().isDone()) {
                if (this.retargetDelay-- <= 0) {
                    this.retargetDelay = Mth.nextInt(LucidEntity.this.random, RETARGET_DELAY_MIN, RETARGET_DELAY_MAX);
                    this.moveToNewTarget();
                }
            }
        }

        private void moveToNewTarget() {
            Vec3 target = this.findPatrolTarget();
            if (target == null) {
                return;
            }

            LucidEntity.this.getNavigation().moveTo(target.x, target.y, target.z, PATROL_SPEED);
        }

        @Nullable
        private Vec3 findPatrolTarget() {
            Vec3 bias = LucidEntity.this.getDeltaMovement();
            if (bias.lengthSqr() < 1.0E-4D) {
                bias = LucidEntity.this.getViewVector(0.0F);
            }
            Vec3 hover = HoverRandomPos.getPos(
                    LucidEntity.this,
                    10,
                    5,
                    bias.x,
                    bias.z,
                    (float) Math.PI / 2.0F,
                    3,
                    1
            );
            if (hover != null) {
                return hover;
            }        return new Vec3(
                    LucidEntity.this.getX() + (LucidEntity.this.random.nextDouble() - 0.5D) * 10.0D,
                    LucidEntity.this.getY() + (LucidEntity.this.random.nextDouble() - 0.5D) * 4.0D,
                    LucidEntity.this.getZ() + (LucidEntity.this.random.nextDouble() - 0.5D) * 10.0D
            );
        }
    }

}






