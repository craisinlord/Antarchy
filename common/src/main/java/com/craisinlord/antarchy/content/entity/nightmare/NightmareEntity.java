package com.craisinlord.antarchy.content.entity.nightmare;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
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

import java.util.EnumSet;
import java.util.List;

public class NightmareEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String ATTACK_COOLDOWN_KEY = "AttackCooldown";
    private static final String ATTACK_ANIMATION_TICKS_KEY = "AttackAnimationTicks";
    private static final String ATTACK_HIT_APPLIED_KEY = "AttackHitApplied";
    private static final String ROAR_TICKS_KEY = "RoarTicks";
    private static final String ROAR_COOLDOWN_KEY = "RoarCooldown";
    private static final String INTRO_ROAR_USED_KEY = "IntroRoarUsed";
    private static final String TARGETLESS_TICKS_KEY = "TargetlessTicks";
    private static final String ANIMATION_STATE_KEY = "AnimationState";

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_FLY = 2;
    private static final int ANIM_ATTACK = 3;
    private static final int ANIM_FLY_ATTACK = 4;
    private static final int ANIM_ROAR = 5;
    private static final int ANIM_DEATH = 6;
    private static final int ANIM_TAKEOFF = 7;
    private static final int ANIM_FLY_DAMAGED = 8;

    private static final int ATTACK_TOTAL_TICKS = 18;
    private static final int ATTACK_DAMAGE_TICK = 9;
    private static final int INTRO_ROAR_TICKS = 32;
    private static final int COMBAT_ROAR_TICKS = 24;
    private static final int DEATH_TICKS = 36;
    private static final int TARGET_RESET_TICKS = 60;
    private static final int DREAD_TICKS = 160;
    private static final int WEAKNESS_TICKS = 100;
    private static final int BLOCK_BREAK_TICKS = 10;
    private static final int FLIGHT_CEILING_CLEARANCE_BLOCKS = 4;

    private static final double PATROL_SPEED = 0.34D;
    private static final double COMBAT_FLIGHT_SPEED = 0.58D;
    private static final double GROUND_APPROACH_SPEED = 0.82D;
    private static final double ATTACK_START_RANGE_SQR = 42.25D;
    private static final double ATTACK_REACH_RADIUS = 2.65D;
    private static final double ATTACK_COMMIT_HORIZONTAL_RANGE = 4.2D;
    private static final double FLIGHT_ENGAGE_RANGE_SQR = 100.0D;
    private static final double FLIGHT_DISENGAGE_RANGE_SQR = 36.0D;
    private static final double ROAR_RETRY_DISTANCE_SQR = 400.0D;
    private static final double PATROL_AIR_MIN_HEIGHT = 1.15D;
    private static final double PATROL_AIR_HEIGHT_SPAN = 0.85D;
    private static final float AIR_PATROL_CHANCE = 0.2F;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation TAKEOFF_ANIM = RawAnimation.begin().thenPlay("animation").thenLoop("fly");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation FLY_DAMAGED_ANIM = RawAnimation.begin().thenLoop("fly_damaged");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack").thenLoop("Idle");
    private static final RawAnimation FLY_ATTACK_ANIM = RawAnimation.begin().thenPlay("fly_attack").thenLoop("fly");
    private static final RawAnimation ROAR_ANIM = RawAnimation.begin().thenPlay("roar").thenLoop("Idle");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death").thenLoop("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nullable private Vec3 patrolTarget;
    private int patrolRetargetTicks;
    private int attackCooldown;
    private int attackAnimationTicks;
    private int roarTicks;
    private int roarCooldown;
    private int targetlessTicks;
    private int airborneTicks;
    private int wingFlapCooldown;
    private int blockBreakCooldown;
    private boolean attackHitApplied;
    private boolean introRoarUsed;

    public NightmareEntity(EntityType<? extends NightmareEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 12, true);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FLYING_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.ARMOR, 8.0D);
    }

    public static boolean canSpawn(EntityType<NightmareEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        boolean sturdy = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        boolean posEmpty = level.isEmptyBlock(pos);
        boolean aboveEmpty = level.isEmptyBlock(pos.above());
        return level.getDifficulty() != Difficulty.PEACEFUL && sturdy && posEmpty && aboveEmpty;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(ROARING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new NightmareRoarGoal());
        this.goalSelector.addGoal(2, new NightmareAttackAnimationGoal());
        this.goalSelector.addGoal(3, new NightmareFlyToTargetGoal());
        this.goalSelector.addGoal(4, new NightmareMeleeGoal());
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new NightmareWanderGoal());
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::canTargetEntity));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::canTargetEntity));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<NightmareEntity> state) {
        state.getController().setAnimationSpeed(this.getAnimationState() == ANIM_FLY ? 0.45D : 1.0D);
        return switch (this.getAnimationState()) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_TAKEOFF -> state.setAndContinue(TAKEOFF_ANIM);
            case ANIM_FLY -> state.setAndContinue(FLY_ANIM);
            case ANIM_FLY_DAMAGED -> state.setAndContinue(FLY_DAMAGED_ANIM);
            case ANIM_ATTACK -> state.setAndContinue(ATTACK_ANIM);
            case ANIM_FLY_ATTACK -> state.setAndContinue(FLY_ATTACK_ANIM);
            case ANIM_ROAR -> state.setAndContinue(ROAR_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
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
        this.setNoGravity(this.shouldUseFlight() && !this.isTooCloseToCeiling());

        if (this.level().isClientSide) {
            this.tickClientParticles();
            this.updateFlightRotation();
            return;
        }

        this.airborneTicks = this.onGround() ? 0 : this.airborneTicks + 1;

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.roarCooldown > 0) this.roarCooldown--;
        if (this.blockBreakCooldown > 0) this.blockBreakCooldown--;

        LivingEntity target = this.getTarget();
        if (!this.canTargetEntity(target)) {
            this.setTarget(null);
            target = null;
        }

        if (target == null) {
            this.targetlessTicks++;
            if (this.targetlessTicks >= TARGET_RESET_TICKS) {
                this.introRoarUsed = false;
            }
        } else {
            this.targetlessTicks = 0;
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
        }

        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            this.updateFlightRotation();
            return;
        }

        this.updateAnimationState();
        this.updateFlightRotation();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && (this.isNoGravity() || !this.onGround())) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(this.onGround() ? 0.88D : 0.91D));
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) return false;
        boolean hurt = this.level() instanceof ServerLevel serverLevel
                ? livingTarget.hurt(AntarchyDamageSources.nightmareMauling(serverLevel, this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))
                : super.doHurtTarget(target);
        if (hurt) {
            this.playNightmareBiteSound();
            this.applyNightmareStrikeEffects(livingTarget);
        }
        return hurt;
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
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.canTargetEntity(target) && super.canAttack(target);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.NIGHTMARE_IDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.NIGHTMARE_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.25F;
    }

    @Override
    public float getVoicePitch() {
        return 0.75F + this.random.nextFloat() * 0.1F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(ATTACK_COOLDOWN_KEY, this.attackCooldown);
        tag.putInt(ATTACK_ANIMATION_TICKS_KEY, this.attackAnimationTicks);
        tag.putBoolean(ATTACK_HIT_APPLIED_KEY, this.attackHitApplied);
        tag.putInt(ROAR_TICKS_KEY, this.roarTicks);
        tag.putInt(ROAR_COOLDOWN_KEY, this.roarCooldown);
        tag.putBoolean(INTRO_ROAR_USED_KEY, this.introRoarUsed);
        tag.putInt(TARGETLESS_TICKS_KEY, this.targetlessTicks);
        tag.putInt("AirborneTicks", this.airborneTicks);
        tag.putBoolean("Roaring", this.isRoaring());
        tag.putInt(ANIMATION_STATE_KEY, this.getAnimationState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.attackCooldown = tag.getInt(ATTACK_COOLDOWN_KEY);
        this.attackAnimationTicks = tag.getInt(ATTACK_ANIMATION_TICKS_KEY);
        this.attackHitApplied = tag.getBoolean(ATTACK_HIT_APPLIED_KEY);
        this.roarTicks = tag.getInt(ROAR_TICKS_KEY);
        this.roarCooldown = tag.getInt(ROAR_COOLDOWN_KEY);
        this.introRoarUsed = tag.getBoolean(INTRO_ROAR_USED_KEY);
        this.targetlessTicks = tag.getInt(TARGETLESS_TICKS_KEY);
        this.airborneTicks = tag.getInt("AirborneTicks");
        this.entityData.set(ROARING, tag.getBoolean("Roaring"));
        this.entityData.set(ANIMATION_STATE, tag.contains(ANIMATION_STATE_KEY) ? tag.getInt(ANIMATION_STATE_KEY) : ANIM_IDLE);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.setAnimationState(ANIM_DEATH);
            this.attackAnimationTicks = 0;
            this.attackCooldown = 0;
            this.roarTicks = 0;
            this.setRoaring(false);
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

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setRoaring(boolean roaring) {
        this.entityData.set(ROARING, roaring);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private boolean shouldStartRoar(LivingEntity target) {
        if (!this.onGround() || this.roarCooldown > 0 || !this.hasLineOfSight(target)) return false;
        double distanceSqr = this.distanceToSqr(target);
        if (distanceSqr > ROAR_RETRY_DISTANCE_SQR || !this.isInFront(target.position(), -0.1D)) return false;
        boolean pressureWindow = distanceSqr > ATTACK_START_RANGE_SQR;
        boolean wounded = this.getHealth() <= this.getMaxHealth() * 0.5F;
        return pressureWindow || wounded;
    }

    private void startRoar() {
        this.roarTicks = this.introRoarUsed ? COMBAT_ROAR_TICKS : INTRO_ROAR_TICKS;
        this.introRoarUsed = true;
        this.roarCooldown = 240 + this.random.nextInt(140);
        this.attackAnimationTicks = 0;
        this.attackHitApplied = false;
        this.setRoaring(true);
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.18D));
        this.playSound(AntarchySoundEvents.NIGHTMARE_ROAR.get(), 2.2F, 0.7F + this.random.nextFloat() * 0.06F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 2.0D, this.getZ(), 20, 1.1D, 0.8D, 1.1D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 2.0D, this.getZ(), 12, 0.9D, 0.6D, 0.9D, 0.02D);
        }
    }

    private void tickRoar(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));
        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
            this.faceTowardTarget(target, 14.0F);
        }
        if (--this.roarTicks <= 0) {
            this.roarTicks = 0;
            this.setRoaring(false);
            this.attackCooldown = Math.max(this.attackCooldown, 18);
        }
    }

    private void startAttack(LivingEntity target) {
        this.attackAnimationTicks = ATTACK_TOTAL_TICKS;
        this.attackHitApplied = false;
        this.attackCooldown = ATTACK_TOTAL_TICKS + 12;
        this.getNavigation().stop();
        Vec3 lunge = target.getEyePosition().subtract(this.getEyePosition());
        if (lunge.lengthSqr() > 1.0E-4D) {
            Vec3 normalized = lunge.normalize();
            this.setDeltaMovement(this.getDeltaMovement().scale(0.3D).add(normalized.x * 0.85D, normalized.y * 0.18D, normalized.z * 0.85D));
            this.hasImpulse = true;
        }
    }

    private void tickAttack(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.93D));
        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
            this.faceTowardTarget(target, 20.0F);
        }
        int elapsed = ATTACK_TOTAL_TICKS - this.attackAnimationTicks;
        if (!this.attackHitApplied && elapsed >= ATTACK_DAMAGE_TICK) {
            this.attackHitApplied = true;
            this.playNightmareBiteSound();
            this.performAttackHit();
        }
        if (--this.attackAnimationTicks <= 0) {
            this.attackAnimationTicks = 0;
        }
    }

    private void performAttackHit() {
        AABB hitBox = this.getBoundingBox()
                .inflate(ATTACK_REACH_RADIUS, 1.5D, ATTACK_REACH_RADIUS)
                .expandTowards(this.getViewVector(1.0F).scale(1.9D));
        List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::canTargetEntity);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity victim : victims) {
            if (!this.isInFront(victim.position(), -0.25D)) continue;
            if (victim.hurt(this.nightmareDamageSource(), damage)) {
                this.applyNightmareStrikeEffects(victim);
                this.knockAway(victim, 1.15D, 0.3D);
            }
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.6D, this.getZ(), 12, 0.9D, 0.35D, 0.9D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.6D, this.getZ(), 8, 0.7D, 0.25D, 0.7D, 0.03D);
        }
    }

    private void playNightmareBiteSound() {
        this.playSound(AntarchySoundEvents.NIGHTMARE_BITE.get(), 1.3F, 0.72F + this.random.nextFloat() * 0.08F);
    }

    private void applyNightmareStrikeEffects(LivingEntity target) {
        if (target instanceof Player) {
            target.addEffect(new MobEffectInstance(AntarchyObjects.DREAD.get(), DREAD_TICKS, 0));
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS, 0));
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(0.8D), target.getZ(), 10, 0.28D, 0.22D, 0.28D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.22D, 0.16D, 0.22D, 0.02D);
        }
    }

    private void knockAway(LivingEntity target, double horizontalStrength, double verticalStrength) {
        Vec3 direction = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) direction = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) direction = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 push = direction.normalize().scale(horizontalStrength);
        target.push(push.x, verticalStrength, push.z);
        target.hurtMarked = true;
    }

    private DamageSource nightmareDamageSource() {
        return this.level() instanceof ServerLevel serverLevel
                ? AntarchyDamageSources.nightmareMauling(serverLevel, this)
                : this.damageSources().mobAttack(this);
    }

    private boolean tryBreakBlocksToTarget(LivingEntity target) {
        int startY = Mth.floor(this.getY());
        int endY = Math.max(Mth.floor(target.getY()), startY - 16);
        int x = Mth.floor(this.getX());
        int z = Mth.floor(this.getZ());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = startY; y >= endY; y--) {
            cursor.set(x, y, z);
            if (this.level().getBlockState(cursor).is(AntarchyTags.Blocks.NIGHTMARE_BREAKABLE)) {
                if (this.blockBreakCooldown <= 0) {
                    this.level().destroyBlock(cursor.immutable(), true, this);
                    this.blockBreakCooldown = BLOCK_BREAK_TICKS;
                }
                this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
                return true;
            }
        }
        return false;
    }

    private boolean canTargetEntity(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this || entity.getType() == this.getType()) return false;
        if (entity.getType().is(AntarchyTags.Entities.NIGHTMARE_NO_ATTACK)) return false;
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator() && this.level().getDifficulty() != Difficulty.PEACEFUL;
        }
        return entity instanceof Mob && entity.isAttackable();
    }

    private boolean shouldUseFlight() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            return this.distanceToSqr(target) > FLIGHT_ENGAGE_RANGE_SQR;
        }
        return this.patrolTarget != null && this.patrolTarget.y > this.getY() + 1.5D;
    }

    private boolean canStartAttackOn(LivingEntity target) {
        if (!this.hasLineOfSight(target) || this.distanceToSqr(target) > ATTACK_START_RANGE_SQR) return false;
        double hdSqr = this.horizontalDistanceToSqr(target);
        double verticalDistance = Math.abs(target.getEyeY() - this.getEyeY());
        return hdSqr <= ATTACK_COMMIT_HORIZONTAL_RANGE * ATTACK_COMMIT_HORIZONTAL_RANGE && verticalDistance <= 4.0D;
    }

    private double horizontalDistanceToSqr(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        return dx * dx + dz * dz;
    }

    private boolean isInFront(Vec3 position, double minimumDot) {
        Vec3 forward = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        Vec3 toTarget = position.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D || toTarget.lengthSqr() < 1.0E-4D) return true;
        return forward.normalize().dot(toTarget.normalize()) >= minimumDot;
    }

    private void faceTowardTarget(LivingEntity target, float maxTurnDegrees) {
        Vec3 toTarget = target.getEyePosition().subtract(this.getEyePosition());
        double horizontal = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        if (horizontal > 1.0E-4D) {
            float targetYaw = (float) (Mth.atan2(toTarget.z, toTarget.x) * (180.0D / Math.PI)) - 90.0F;
            float nextYaw = Mth.approachDegrees(this.getYRot(), targetYaw, maxTurnDegrees);
            this.setYRot(nextYaw);
            this.yBodyRot = nextYaw;
            this.yHeadRot = nextYaw;
        }
        if (toTarget.lengthSqr() > 1.0E-4D) {
            float targetPitch = (float) (-(Mth.atan2(toTarget.y, horizontal) * (180.0D / Math.PI)));
            this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, maxTurnDegrees * 0.7F));
        }
    }

    private Vec3 findPatrolTarget() {
        BlockPos origin = this.blockPosition();
        for (int attempt = 0; attempt < 18; attempt++) {
            int x = origin.getX() + this.random.nextInt(29) - 14;
            int z = origin.getZ() + this.random.nextInt(29) - 14;
            BlockPos terrainPos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, origin.getY(), z));
            boolean airPatrol = this.random.nextFloat() < AIR_PATROL_CHANCE;
            double y = airPatrol
                    ? terrainPos.getY() + PATROL_AIR_MIN_HEIGHT + this.random.nextDouble() * PATROL_AIR_HEIGHT_SPAN
                    : terrainPos.getY() + 1.0D;
            Vec3 desired = new Vec3(x + 0.5D, y, z + 0.5D);
            Vec3 openTarget = this.findNearestFlightTarget(desired);
            if (openTarget.distanceToSqr(desired) <= 16.0D) return openTarget;
        }
        BlockPos fallback = this.level().getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                origin.offset(this.random.nextInt(25) - 12, 0, this.random.nextInt(25) - 12)
        );
        return new Vec3(fallback.getX() + 0.5D, fallback.getY() + 1.0D, fallback.getZ() + 0.5D);
    }

    private Vec3 findNearestFlightTarget(Vec3 desired) {
        BlockPos desiredPos = BlockPos.containing(desired);
        Vec3 best = null;
        double bestScore = Double.MAX_VALUE;
        for (int attempt = 0; attempt < 14; attempt++) {
            BlockPos candidate = desiredPos.offset(
                    this.random.nextInt(7) - 3,
                    this.random.nextInt(5) - 2,
                    this.random.nextInt(7) - 3
            );
            if (!this.isOpenFlightSpace(candidate) || !this.isCeilingBuffered(candidate)) continue;
            Vec3 center = Vec3.atCenterOf(candidate);
            double score = center.distanceToSqr(desired) + Math.max(0.0D, center.y - desired.y) * 6.0D;
            if (score < bestScore) {
                best = center;
                bestScore = score;
            }
        }
        if (best != null) return best;
        for (int down = 0; down <= 12; down++) {
            BlockPos lowered = desiredPos.below(down);
            if (this.isOpenFlightSpace(lowered) && this.isCeilingBuffered(lowered)) return Vec3.atCenterOf(lowered);
        }
        return desired;
    }

    private boolean isOpenFlightSpace(BlockPos pos) {
        return this.level().isEmptyBlock(pos)
                && this.level().isEmptyBlock(pos.above())
                && this.level().isEmptyBlock(pos.above(2));
    }

    private boolean isCeilingBuffered(BlockPos pos) {
        for (int i = 3; i <= FLIGHT_CEILING_CLEARANCE_BLOCKS; i++) {
            if (!this.level().isEmptyBlock(pos.above(i))) return false;
        }
        return true;
    }

    private boolean isTooCloseToCeiling() {
        BlockPos current = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        for (int i = 1; i <= FLIGHT_CEILING_CLEARANCE_BLOCKS; i++) {
            if (!this.level().isEmptyBlock(current.above(i))) return true;
        }
        return false;
    }

    private void tickClientParticles() {
        if ((this.isRoaring() || this.attackAnimationTicks > 0) && this.tickCount % 3 == 0) {
            this.level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + 1.2D + this.random.nextDouble() * 1.1D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    0.0D, 0.03D, 0.0D
            );
        }
    }

    private void updateFlightRotation() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.horizontalDistanceSqr() > 1.0E-4D) {
            float targetYaw = (float) (Mth.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
            float nextYaw = Mth.approachDegrees(this.getYRot(), targetYaw, 4.0F);
            this.setYRot(nextYaw);
            this.yBodyRot = Mth.approachDegrees(this.yBodyRot, nextYaw, 2.5F);
            this.yHeadRot = Mth.approachDegrees(this.yHeadRot, nextYaw, 3.5F);
        }
        if (!this.onGround() && velocity.lengthSqr() > 1.0E-4D) {
            float targetPitch = (float) (-(Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI)));
            this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, 3.0F));
        } else {
            this.setXRot(Mth.approachDegrees(this.getXRot(), 0.0F, 2.0F));
        }
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            this.wingFlapCooldown = 0;
            return;
        }
        if (this.isRoaring()) {
            this.setAnimationState(ANIM_ROAR);
            this.wingFlapCooldown = 0;
            return;
        }
        if (this.attackAnimationTicks > 0) {
            this.setAnimationState(this.onGround() ? ANIM_ATTACK : ANIM_FLY_ATTACK);
            this.tickWingFlapSound();
            return;
        }
        if (!this.onGround() && this.airborneTicks > 0 && this.airborneTicks <= 10 && this.shouldUseFlight()) {
            this.setAnimationState(ANIM_TAKEOFF);
            this.wingFlapCooldown = 0;
            return;
        }
        if (!this.onGround() && this.hurtTime > 0) {
            this.setAnimationState(ANIM_FLY_DAMAGED);
            this.tickWingFlapSound();
            return;
        }
        if (!this.onGround()) {
            this.setAnimationState(ANIM_FLY);
            this.tickWingFlapSound();
            return;
        }
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.horizontalDistanceSqr() > 0.008D) {
            this.setAnimationState(ANIM_WALK);
            this.wingFlapCooldown = 0;
            return;
        }
        this.setAnimationState(ANIM_IDLE);
        this.wingFlapCooldown = 0;
    }

    private void tickWingFlapSound() {
        if (!(this.level() instanceof ServerLevel)) return;
        if (this.onGround() || this.isRoaring() || this.isDeadOrDying()) {
            this.wingFlapCooldown = 0;
            return;
        }
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 0.01D) return;
        if (this.wingFlapCooldown > 0) {
            this.wingFlapCooldown--;
            return;
        }
        this.playSound(AntarchySoundEvents.NIGHTMARE_FLAP.get(), 1.25F, 0.85F + this.random.nextFloat() * 0.08F);
        this.wingFlapCooldown = 5 + this.random.nextInt(4);
    }

    private final class NightmareRoarGoal extends Goal {
        NightmareRoarGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return target instanceof Player && NightmareEntity.this.shouldStartRoar(target);
        }

        @Override
        public boolean canContinueToUse() {
            return NightmareEntity.this.isRoaring();
        }

        @Override
        public void start() {
            NightmareEntity.this.startRoar();
        }

        @Override
        public void tick() {
            NightmareEntity.this.tickRoar(NightmareEntity.this.getTarget());
        }

        @Override
        public void stop() {
            NightmareEntity.this.roarTicks = 0;
            NightmareEntity.this.setRoaring(false);
        }
    }

    private final class NightmareAttackAnimationGoal extends Goal {
        NightmareAttackAnimationGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return NightmareEntity.this.attackAnimationTicks > 0;
        }

        @Override
        public boolean canContinueToUse() {
            return NightmareEntity.this.attackAnimationTicks > 0;
        }

        @Override
        public void tick() {
            NightmareEntity.this.tickAttack(NightmareEntity.this.getTarget());
        }
    }

    private final class NightmareFlyToTargetGoal extends Goal {
        NightmareFlyToTargetGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return target != null && NightmareEntity.this.distanceToSqr(target) > FLIGHT_ENGAGE_RANGE_SQR;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return target != null && NightmareEntity.this.distanceToSqr(target) > FLIGHT_DISENGAGE_RANGE_SQR;
        }

        @Override
        public void stop() {
            NightmareEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = NightmareEntity.this.getTarget();
            if (target == null) return;
            if (NightmareEntity.this.attackCooldown <= 0 && NightmareEntity.this.canStartAttackOn(target)) {
                NightmareEntity.this.startAttack(target);
                return;
            }
            NightmareEntity.this.getMoveControl().setWantedPosition(
                    target.getX(), target.getEyeY() + 0.5D, target.getZ(), COMBAT_FLIGHT_SPEED
            );
        }
    }

    private final class NightmareMeleeGoal extends MeleeAttackGoal {
        NightmareMeleeGoal() {
            super(NightmareEntity.this, GROUND_APPROACH_SPEED, true);
        }

        @Override
        public void tick() {
            LivingEntity target = NightmareEntity.this.getTarget();
            if (target != null) {
                double hdSqr = NightmareEntity.this.horizontalDistanceToSqr(target);
                double vertDelta = NightmareEntity.this.getY() - target.getY();
                if (target.onGround() && hdSqr <= 25.0D && vertDelta > 1.6D) {
                    NightmareEntity.this.tryBreakBlocksToTarget(target);
                }
            }
            super.tick();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (NightmareEntity.this.attackCooldown <= 0 && NightmareEntity.this.canStartAttackOn(target)) {
                NightmareEntity.this.startAttack(target);
            }
        }
    }

    private final class NightmareWanderGoal extends Goal {
        NightmareWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return NightmareEntity.this.getTarget() == null && !NightmareEntity.this.isDeadOrDying();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            NightmareEntity.this.patrolTarget = null;
            NightmareEntity.this.patrolRetargetTicks = 0;
        }

        @Override
        public void stop() {
            NightmareEntity.this.getNavigation().stop();
            NightmareEntity.this.patrolTarget = null;
        }

        @Override
        public void tick() {
            if (NightmareEntity.this.patrolRetargetTicks-- <= 0
                    || NightmareEntity.this.patrolTarget == null
                    || NightmareEntity.this.position().distanceToSqr(NightmareEntity.this.patrolTarget) < 4.0D) {
                NightmareEntity.this.patrolRetargetTicks = 30 + NightmareEntity.this.random.nextInt(30);
                NightmareEntity.this.patrolTarget = NightmareEntity.this.findPatrolTarget();
            }
            if (NightmareEntity.this.patrolTarget != null) {
                NightmareEntity.this.getMoveControl().setWantedPosition(
                        NightmareEntity.this.patrolTarget.x,
                        NightmareEntity.this.patrolTarget.y,
                        NightmareEntity.this.patrolTarget.z,
                        PATROL_SPEED
                );
            }
        }
    }
}
