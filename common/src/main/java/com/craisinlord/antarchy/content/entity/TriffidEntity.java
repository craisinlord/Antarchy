package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
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

public class TriffidEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ACTION_STATE = SynchedEntityData.defineId(TriffidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> AWAKE = SynchedEntityData.defineId(TriffidEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int ACTION_NONE = 0;
    private static final int ACTION_SWEEP = 1;
    private static final int ACTION_GRAB = 2;
    private static final int ACTION_DEATH = 3;

    private static final int SWEEP_TOTAL_TICKS = 20;
    private static final int SWEEP_ACTIVE_START = 7;
    private static final int SWEEP_ACTIVE_END = 14;
    private static final int SWEEP_COOLDOWN_TICKS = 24;
    private static final int GRAB_TOTAL_TICKS = 24;
    private static final int GRAB_PULL_START = 6;
    private static final int GRAB_PULL_END = 18;
    private static final int GRAB_DAMAGE_TICK = 19;
    private static final int GRAB_COOLDOWN_TICKS = 42;
    private static final int DEATH_TICKS = 32;

    private static final double WAKE_RANGE = 12.0D;
    private static final double SWEEP_RANGE = 4.25D;
    private static final double GRAB_RANGE = 5.25D;
    private static final double MIN_GRAB_RANGE = 2.0D;
    private static final double FOLLOW_RANGE = 14.0D;
    private static final double TURN_SPEED_DEGREES = 5.0D;

    private static final RawAnimation DORMANT_ANIM = RawAnimation.begin().thenLoop("dormant");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SWEEP_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation GRAB_ANIM = RawAnimation.begin().thenPlay("grab");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Set<UUID> sweepHitEntities = new HashSet<>();

    private int actionTicks;
    private int attackCooldown;
    private float actionStartYaw;
    private int grabTargetId = -1;
    private boolean grabDamageApplied;
    private int preySearchCooldown = 0;

    public TriffidEntity(EntityType<? extends TriffidEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.triffidHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.triffidAttackDamage())
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    public static boolean canSpawn(EntityType<TriffidEntity> entityType, LevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() + 7 >= level.getMaxBuildHeight()) {
            return false;
        }

        BlockPos[] footprint = new BlockPos[] {
                pos,
                pos.east(),
                pos.south(),
                pos.east().south()
        };

        for (BlockPos footprintPos : footprint) {
            if (!level.getBlockState(footprintPos).isAir()) {
                return false;
            }

            BlockPos below = footprintPos.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                return false;
            }

            for (int y = 1; y <= 7; y++) {
                if (!level.getBlockState(footprintPos.above(y)).isAir()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION_STATE, ACTION_NONE);
        builder.define(AWAKE, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, TriffidEntity::isValidPreyTarget));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<TriffidEntity> state) {
        return switch (this.getActionState()) {
            case ACTION_SWEEP -> state.setAndContinue(SWEEP_ANIM);
            case ACTION_GRAB -> state.setAndContinue(GRAB_ANIM);
            case ACTION_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> state.setAndContinue(this.isAwake() ? IDLE_ANIM : DORMANT_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();
        this.getNavigation().stop();
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(0.0D, movement.y, 0.0D);
        this.zza = 0.0F;
        this.xxa = 0.0F;

        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        if (this.level().isClientSide) {
            return;
        }

        this.syncConfiguredAttributes();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (this.getActionState() == ACTION_DEATH) {
            return;
        }

        if (this.preySearchCooldown > 0) {
            this.preySearchCooldown--;
        } else {
            this.preySearchCooldown = 20;
            this.setAwake(this.hasNearbyPrey());
            if (!isValidLivePrey(this.getTarget())) {
                this.setTarget(this.findNearestPrey());
            }
        }

        LivingEntity target = this.getTarget();

        if (this.getActionState() == ACTION_SWEEP) {
            this.tickSweepAttack();
            return;
        }

        if (this.getActionState() == ACTION_GRAB) {
            this.tickGrabAttack();
            return;
        }

        if (target != null) {
            this.rotateToward(target, TURN_SPEED_DEGREES);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.tryStartAttack(target);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide && this.getActionState() != ACTION_DEATH) {
            this.setActionState(ACTION_DEATH);
            this.actionTicks = DEATH_TICKS;
            this.attackCooldown = 0;
            this.grabTargetId = -1;
            this.setTarget(null);
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= DEATH_TICKS) {
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getActionState() != ACTION_NONE) {
            return null;
        }

        if (!this.isAwake()) {
            return this.random.nextInt(24) == 0 ? AntarchySoundEvents.TRIFFID_HISS.get() : null;
        }

        return this.getTarget() != null ? AntarchySoundEvents.TRIFFID_GROWL.get() : null;
    }

    @Override
    public int getAmbientSoundInterval() {
        if (!this.isAwake()) {
            return 200;
        }

        return this.getTarget() != null ? 80 : 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.TRIFFID_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.TRIFFID_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return isValidPreyTarget(target);
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    private void tryStartAttack(LivingEntity target) {
        if (this.attackCooldown > 0 || !this.hasLineOfSight(target)) {
            return;
        }

        double distance = this.distanceTo(target);
        boolean inSweepRange = distance <= SWEEP_RANGE;
        boolean inGrabRange = distance >= MIN_GRAB_RANGE && distance <= GRAB_RANGE;

        if (inSweepRange) {
            if (inGrabRange && isLowHealthPrey(target) && this.random.nextFloat() < 0.25F) {
                this.startGrabAttack(target);
            } else {
                this.startSweepAttack(target);
            }
            return;
        }

        if (inGrabRange && (isLowHealthPrey(target) || this.random.nextFloat() < 0.08F)) {
            this.startGrabAttack(target);
        }
    }

    private void startSweepAttack(LivingEntity target) {
        this.setActionState(ACTION_SWEEP);
        this.actionTicks = SWEEP_TOTAL_TICKS;
        this.attackCooldown = SWEEP_COOLDOWN_TICKS;
        this.actionStartYaw = this.getAngleTo(target) - 100.0F;
        this.sweepHitEntities.clear();
        this.setYRot(this.actionStartYaw);
        this.playSound(AntarchySoundEvents.TRIFFID_ATTACK.get(), 0.9F, 0.75F);
    }

    private void tickSweepAttack() {
        int elapsed = SWEEP_TOTAL_TICKS - this.actionTicks;
        float progress = elapsed / (float) SWEEP_TOTAL_TICKS;
        this.setYRot(this.actionStartYaw + 200.0F * progress);

        if (elapsed >= SWEEP_ACTIVE_START && elapsed <= SWEEP_ACTIVE_END) {
            this.applySweepDamage();
        }

        this.actionTicks--;
        if (this.actionTicks <= 0) {
            this.setActionState(ACTION_NONE);
            this.sweepHitEntities.clear();
        }
    }

    private void applySweepDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(SWEEP_RANGE, 1.0D, SWEEP_RANGE),
                TriffidEntity::isValidLivePrey
        );

        for (LivingEntity candidate : candidates) {
            if (candidate == this || this.sweepHitEntities.contains(candidate.getUUID())) {
                continue;
            }
            if (this.distanceTo(candidate) > SWEEP_RANGE) {
                continue;
            }

            float relativeYaw = Mth.wrapDegrees(this.getAngleTo(candidate) - this.getYRot());
            if (Math.abs(relativeYaw) > 70.0F) {
                continue;
            }

            if (candidate.hurt(AntarchyDamageSources.triffidMauling(serverLevel, this), (float) AntarchySettings.triffidAttackDamage())) {
                this.sweepHitEntities.add(candidate.getUUID());
            }
        }
    }

    private void startGrabAttack(LivingEntity target) {
        this.setActionState(ACTION_GRAB);
        this.actionTicks = GRAB_TOTAL_TICKS;
        this.attackCooldown = GRAB_COOLDOWN_TICKS;
        this.grabTargetId = target.getId();
        this.grabDamageApplied = false;
        this.setYRot(this.getAngleTo(target));
        this.playSound(AntarchySoundEvents.TRIFFID_GRAB.get(), 0.8F, 0.85F);
    }

    private void tickGrabAttack() {
        LivingEntity grabTarget = this.getGrabTarget();
        if (!isValidLivePrey(grabTarget)) {
            this.finishGrabAttack();
            return;
        }

        this.rotateToward(grabTarget, TURN_SPEED_DEGREES * 1.25D);
        int elapsed = GRAB_TOTAL_TICKS - this.actionTicks;
        if (elapsed >= GRAB_PULL_START && elapsed <= GRAB_PULL_END) {
            this.pullTargetTowardMouth(grabTarget);
        }

        if (!this.grabDamageApplied && elapsed >= GRAB_DAMAGE_TICK) {
            this.grabDamageApplied = true;
            if (this.level() instanceof ServerLevel serverLevel) {
                grabTarget.hurt(AntarchyDamageSources.triffidSwallow(serverLevel, this), (float) AntarchySettings.triffidGrabDamage());
            } else {
                grabTarget.hurt(this.damageSources().mobAttack(this), (float) AntarchySettings.triffidGrabDamage());
            }
        }

        this.actionTicks--;
        if (this.actionTicks <= 0) {
            this.finishGrabAttack();
        }
    }

    private void pullTargetTowardMouth(LivingEntity target) {
        Vec3 mouthPos = this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot()).scale(1.45D)).add(0.0D, 2.3D, 0.0D);
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 offset = mouthPos.subtract(targetCenter);
        double distance = offset.length();
        if (distance < 1.0E-4D) {
            return;
        }

        Vec3 pull = offset.normalize().scale(Math.min(1.0D, 0.2D + distance * 0.18D));
        target.setDeltaMovement(target.getDeltaMovement().scale(0.35D).add(pull));
        target.hurtMarked = true;
        target.fallDistance = 0.0F;
    }

    private void finishGrabAttack() {
        this.setActionState(ACTION_NONE);
        this.actionTicks = 0;
        this.grabTargetId = -1;
        this.grabDamageApplied = false;
    }

    private void rotateToward(LivingEntity target, double maxStep) {
        float targetYaw = this.getAngleTo(target);
        this.setYRot(this.rotateTowards(this.getYRot(), targetYaw, (float) maxStep));
    }

    private float getAngleTo(Entity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        return (float) (Mth.atan2(dz, dx) * (180.0F / Math.PI)) - 90.0F;
    }

    private float rotateTowards(float currentYaw, float targetYaw, float maxStep) {
        return currentYaw + Mth.clamp(Mth.wrapDegrees(targetYaw - currentYaw), -maxStep, maxStep);
    }

    private boolean hasNearbyPrey() {
        return !this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(WAKE_RANGE, 4.0D, WAKE_RANGE),
                TriffidEntity::isValidLivePrey
        ).isEmpty();
    }

    @Nullable
    private LivingEntity findNearestPrey() {
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(FOLLOW_RANGE, 6.0D, FOLLOW_RANGE),
                TriffidEntity::isValidLivePrey
        );
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double distance = this.distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    @Nullable
    private LivingEntity getGrabTarget() {
        Entity entity = this.level().getEntity(this.grabTargetId);
        return entity instanceof LivingEntity living ? living : null;
    }

    private void syncConfiguredAttributes() {
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(AntarchySettings.triffidHealth());
        }
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(AntarchySettings.triffidAttackDamage());
        }
    }

    private static boolean isLowHealthPrey(LivingEntity target) {
        return target.getHealth() <= Math.max((float) AntarchySettings.triffidGrabDamage(), target.getMaxHealth() * 0.35F);
    }

    private int getActionState() {
        return this.entityData.get(ACTION_STATE);
    }

    private void setActionState(int state) {
        this.entityData.set(ACTION_STATE, state);
    }

    private boolean isAwake() {
        return this.entityData.get(AWAKE);
    }

    private void setAwake(boolean awake) {
        this.entityData.set(AWAKE, awake);
    }

    private static boolean isValidLivePrey(@Nullable LivingEntity target) {
        return target != null && target.isAlive() && isValidPreyTarget(target);
    }

    private static boolean isValidPreyTarget(LivingEntity target) {
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return target.getType().is(AntarchyTags.Entities.TRIFFID_PREY);
    }
}
