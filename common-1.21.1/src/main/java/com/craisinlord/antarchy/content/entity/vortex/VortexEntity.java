package com.craisinlord.antarchy.content.entity.vortex;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.block.state.BlockState;
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

public class VortexEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(VortexEntity.class, EntityDataSerializers.INT);

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_FLY = 1;
    private static final int ANIM_ATTACK = 2;
    private static final int ANIM_DEATH = 3;

    private static final String ATTACK_COOLDOWN_KEY = "AttackCooldown";
    private static final String ATTACK_TICKS_KEY = "AttackTicks";

    private static final int ATTACK_WINDUP_TICKS = 18;
    private static final int ATTACK_RECOVERY_TICKS = 12;
    private static final int ATTACK_COOLDOWN_TICKS = 70;
    private static final int DEATH_TICKS = 15;
    private static final double ATTACK_RANGE = 14.0D;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("wind_gust");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");

    public static Supplier<EntityType<WindVortexEntity>> windVortexTypeSupplier;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int attackCooldown;
    private int attackTicks;
    private boolean spawnedAttackVortex;

    public VortexEntity(EntityType<? extends VortexEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 2, true);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.vortexHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.vortexAttackDamage())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.vortexMovementSpeed())
                .add(Attributes.FLYING_SPEED, AntarchySettings.vortexFlyingSpeed())
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D);
    }

    public static boolean canSpawn(EntityType<VortexEntity> entityType, ServerLevelAccessor level,
            MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above())
                && level.isEmptyBlock(pos.above(2));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
            MobSpawnType spawnReason, SpawnGroupData spawnData) {
        com.craisinlord.antarchy.content.entity.ConfiguredMobSpawnUtil.applyConfiguredHealth(this, AntarchySettings.vortexHealth());
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new VortexPatrolGoal());
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 20, true, false, this::canTargetEntity));
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
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController)
                .triggerableAnim("attack", ATTACK_ANIM)
                .triggerableAnim("death", DEATH_ANIM));
    }

    private PlayState mainAnimController(AnimationState<VortexEntity> state) {
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
        if (this.attackTicks > 0) {
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
        this.faceTowards(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), 12.0F, 8.0F);
        this.getMoveControl().setWantedPosition(target.getX(), target.getY() + 2.5D, target.getZ(), 0.38D);

        if (this.attackCooldown <= 0
                && this.distanceToSqr(target) <= ATTACK_RANGE * ATTACK_RANGE
                && this.hasLineOfSight(target)
                && this.countOwnedActiveVortexes() < AntarchySettings.vortexMaxActiveVortexes()) {
            this.startAttack();
        }
    }

    private void startAttack() {
        this.attackTicks = ATTACK_WINDUP_TICKS + ATTACK_RECOVERY_TICKS;
        this.spawnedAttackVortex = false;
        this.attackCooldown = ATTACK_COOLDOWN_TICKS;
        this.getNavigation().stop();
    }

    private void tickAttack(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.84D));

        if (target == null || !this.canTargetEntity(target)) {
            this.attackTicks = 0;
            this.spawnedAttackVortex = false;
            return;
        }

        this.faceTowards(target.getEyePosition(), 16.0F, 10.0F);
        this.getMoveControl().setWantedPosition(this.getX(), Math.max(this.getY(), target.getY() + 2.5D), this.getZ(), 0.34D);

        int elapsed = ATTACK_WINDUP_TICKS + ATTACK_RECOVERY_TICKS - this.attackTicks;
        if (!this.spawnedAttackVortex && elapsed >= ATTACK_WINDUP_TICKS) {
            this.spawnAttackVortex(target);
            this.spawnedAttackVortex = true;
        }

        if (--this.attackTicks <= 0) {
            this.attackTicks = 0;
        }
    }

    private void spawnAttackVortex(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel) || windVortexTypeSupplier == null) {
            return;
        }
        Vec3 targetPos = target.position();
        Vec3 fromMob = targetPos.subtract(this.position());
        Vec3 drift = fromMob.lengthSqr() > 1.0E-6D ? fromMob.normalize().scale(0.045D) : this.getViewVector(0.0F).scale(0.045D);
        Vec3 position = targetPos.add(
                (this.random.nextDouble() - 0.5D) * 1.6D,
                0.05D,
                (this.random.nextDouble() - 0.5D) * 1.6D
        );
        WindVortexEntity vortex = WindVortexEntity.create(serverLevel, windVortexTypeSupplier.get(), position, drift, this, true);
        serverLevel.addFreshEntity(vortex);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BREEZE_SHOOT, this.getSoundSource(), 1.0F, 0.9F);
    }

    private int countOwnedActiveVortexes() {
        if (!(this.level() instanceof ServerLevel serverLevel) || windVortexTypeSupplier == null) {
            return 0;
        }
        return serverLevel.getEntities(windVortexTypeSupplier.get(), this.getBoundingBox().inflate(32.0D),
                vortex -> vortex.isAlive() && vortex.isDamaging() && vortex.isOwnedBy(this)).size();
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
            this.attackTicks = 0;
            this.spawnedAttackVortex = false;
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
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BREEZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BREEZE_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(ATTACK_COOLDOWN_KEY, this.attackCooldown);
        tag.putInt(ATTACK_TICKS_KEY, this.attackTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.attackCooldown = tag.getInt(ATTACK_COOLDOWN_KEY);
        this.attackTicks = 0;
        this.spawnedAttackVortex = false;
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int state) {
        if (this.entityData.get(ANIMATION_STATE) != state) {
            this.entityData.set(ANIMATION_STATE, state);
            String trigger = switch (state) {
                case ANIM_ATTACK -> "wind_gust";
                case ANIM_DEATH -> "death";
                default -> null;
            };
            if (trigger != null) {
                this.triggerAnim("main_controller", trigger);
            }
        }
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
        } else if (this.attackTicks > 0) {
            this.setAnimationState(ANIM_ATTACK);
        } else if (this.getTarget() != null && this.canTargetEntity(this.getTarget())) {
            this.setAnimationState(ANIM_FLY);
        } else {
            this.setAnimationState(ANIM_IDLE);
        }
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

    private final class VortexPatrolGoal extends Goal {
        private int retargetDelay;

        private VortexPatrolGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return VortexEntity.this.getTarget() == null
                    && VortexEntity.this.attackTicks <= 0
                    && VortexEntity.this.getNavigation().isDone();
        }

        @Override
        public boolean canContinueToUse() {
            return VortexEntity.this.getTarget() == null
                    && VortexEntity.this.attackTicks <= 0
                    && VortexEntity.this.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            this.retargetDelay = Mth.nextInt(VortexEntity.this.random, 40, 80);
            this.moveToNewTarget();
        }

        @Override
        public void tick() {
            if (VortexEntity.this.getTarget() != null || VortexEntity.this.attackTicks > 0) {
                VortexEntity.this.getNavigation().stop();
                return;
            }
            if (VortexEntity.this.getNavigation().isDone() && this.retargetDelay-- <= 0) {
                this.retargetDelay = Mth.nextInt(VortexEntity.this.random, 40, 80);
                this.moveToNewTarget();
            }
        }

        private void moveToNewTarget() {
            Vec3 bias = VortexEntity.this.getViewVector(0.0F);
            Vec3 hover = HoverRandomPos.getPos(VortexEntity.this, 10, 5, bias.x, bias.z, (float) Math.PI / 2.0F, 3, 1);
            if (hover != null) {
                VortexEntity.this.getNavigation().moveTo(hover.x, hover.y, hover.z, 0.18D);
            }
        }
    }
}
