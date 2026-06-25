package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ToreterrorEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(ToreterrorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PHASE_2 = SynchedEntityData.defineId(ToreterrorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> JUMP_SHAKING = SynchedEntityData.defineId(ToreterrorEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_SHOOT = 2;
    private static final int ANIM_JUMP_ATTACK = 3;
    private static final int ANIM_SPIN = 4;
    private static final int ANIM_DEATH = 5;
    private static final int ANIM_RAPID_SHOOT = 6;

    private static final int ATTACK_RAPID_FIRE = 0;
    private static final int ATTACK_HUGE_BOMB = 1;
    private static final int ATTACK_BOMBERS = 2;

    private static final int DEATH_TICKS = 60;
    private static final int SHOOT_TOTAL_TICKS = 75;
    private static final int SHOOT_FIRE_START_TICK = 12;
    private static final int RAPID_FIRE_INTERVAL = 4;
    private static final int RAPID_FIRE_END_TICK = 60;
    private static final int SHOOT_BOMBER_TICK = 12;
    private static final int SHOOT_BOMBER_SECOND_TICK = 32;
    private static final int JUMP_TOTAL_TICKS = 40;
    private static final int JUMP_LAUNCH_TICK = 10;
    private static final int JUMP_SHAKE_TICKS = 25;
    private static final int SPIN_TOTAL_TICKS = 40;
    private static final int SPIN_START_TICK = 3;
    private static final int SPIN_END_TICK = 22;
    private static final int SPIN_HIT_INTERVAL = 5;

    private static final double CLOSE_RANGE_SQR = 64.0D;
    private static final double JUMP_RANGE_SQR = 400.0D;
    private static final double RANGED_MAX_RANGE_SQR = 625.0D;
    private static final double SPIN_RADIUS = 4.5D;
    private static final double JUMP_SHOCKWAVE_RADIUS = 6.0D;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().thenPlay("shoot").thenLoop("Idle");
    private static final RawAnimation RAPID_SHOOT_ANIM = RawAnimation.begin().thenLoop("shoot");
    private static final RawAnimation JUMP_ANIM = RawAnimation.begin().thenPlay("jump_attack").thenLoop("Idle");
    private static final RawAnimation SPIN_ANIM = RawAnimation.begin().thenPlay("spin_attack").thenLoop("Idle");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.antarchy.toreterror"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    private int shootAnimTicks;
    private int jumpAnimTicks;
    private int spinAnimTicks;
    private int shootCooldown;
    private int jumpCooldown;
    private int spinCooldown;
    private boolean jumpLaunched;
    private boolean shockwaveApplied;
    private int jumpShakeTicks;
    private int shootAttackType;
    private boolean shootBomberFired;
    private boolean shootBomberFiredSecond;
    private int rapidFireNextTick;
    private final Map<UUID, Integer> spinHitCooldowns = new HashMap<>();

    public ToreterrorEntity(EntityType<? extends ToreterrorEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.toreterrorHealth())
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.325D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.ARMOR, 10.0D);
    }

    public static boolean canSpawn(EntityType<ToreterrorEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, net.minecraft.util.RandomSource random) {
        boolean sturdy = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        return level.getDifficulty() != Difficulty.PEACEFUL && sturdy;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(PHASE_2, false);
        builder.define(JUMP_SHAKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ToreterrorRangedGoal());
        this.goalSelector.addGoal(2, new ToreterrorJumpGoal());
        this.goalSelector.addGoal(3, new ToreterrorSpinGoal());
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 1.0D, 64.0F));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::canTargetEntity));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<ToreterrorEntity> state) {
        int animState = this.getAnimationState();
        return switch (animState) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_SHOOT -> state.setAndContinue(SHOOT_ANIM);
            case ANIM_RAPID_SHOOT -> state.setAndContinue(RAPID_SHOOT_ANIM);
            case ANIM_JUMP_ATTACK -> state.setAndContinue(JUMP_ANIM);
            case ANIM_SPIN -> state.setAndContinue(SPIN_ANIM);
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
        if (this.level().isClientSide) return;

        if (this.shootCooldown > 0) this.shootCooldown--;
        if (this.jumpCooldown > 0) this.jumpCooldown--;
        if (this.spinCooldown > 0) this.spinCooldown--;
        if (this.jumpShakeTicks > 0) {
            this.jumpShakeTicks--;
            if (this.jumpShakeTicks <= 0) {
                this.entityData.set(JUMP_SHAKING, false);
            }
        }

        this.spinHitCooldowns.replaceAll((uuid, cd) -> cd - 1);
        this.spinHitCooldowns.entrySet().removeIf(e -> e.getValue() <= 0);

        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }

        if (this.shootAnimTicks > 0) {
            this.tickShootAnimation();
        } else if (this.jumpAnimTicks > 0) {
            this.tickJumpAnimation();
        } else if (this.spinAnimTicks > 0) {
            this.tickSpinAnimation();
        } else {
            this.updateIdleAnimation();
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) return false;
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            amount *= (float) AntarchySettings.toreterrorProjectileDamageMultiplier();
        }
        if (!this.isPhase2()) {
            float halfHealth = this.getMaxHealth() * 0.5F;
            if (this.getHealth() - amount <= halfHealth) {
                float capDamage = this.getHealth() - halfHealth;
                if (capDamage <= 0.0F) {
                    this.enterPhase2();
                    return false;
                }
                boolean result = super.hurt(source, capDamage);
                if (result) this.enterPhase2();
                return result;
            }
        }
        return super.hurt(source, amount);
    }

    private void enterPhase2() {
        this.entityData.set(PHASE_2, true);
        this.bossEvent.setColor(BossEvent.BossBarColor.RED);
    }

    public boolean isPhase2() {
        return this.entityData.get(PHASE_2);
    }

    public boolean isJumpShaking() {
        return this.entityData.get(JUMP_SHAKING);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AnimationState", this.getAnimationState());
        tag.putInt("ShootCooldown", this.shootCooldown);
        tag.putInt("JumpCooldown", this.jumpCooldown);
        tag.putInt("SpinCooldown", this.spinCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(ANIMATION_STATE, tag.contains("AnimationState") ? tag.getInt("AnimationState") : ANIM_IDLE);
        this.shootCooldown = tag.getInt("ShootCooldown");
        this.jumpCooldown = tag.getInt("JumpCooldown");
        this.spinCooldown = tag.getInt("SpinCooldown");
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.setAnimationState(ANIM_DEATH);
            this.shootAnimTicks = 0;
            this.jumpAnimTicks = 0;
            this.spinAnimTicks = 0;
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

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int state) {
        if (this.entityData.get(ANIMATION_STATE) != state) {
            this.entityData.set(ANIMATION_STATE, state);
        }
    }

    private void updateIdleAnimation() {
        Vec3 vel = this.getDeltaMovement();
        boolean moving = (vel.x * vel.x + vel.z * vel.z) > 0.0004D;
        this.setAnimationState(moving ? ANIM_WALK : ANIM_IDLE);
    }

    private boolean isInAttackAnimation() {
        return this.shootAnimTicks > 0 || this.jumpAnimTicks > 0 || this.spinAnimTicks > 0;
    }

    private void tickShootAnimation() {
        int elapsed = SHOOT_TOTAL_TICKS - this.shootAnimTicks;
        switch (this.shootAttackType) {
            case ATTACK_RAPID_FIRE -> {
                this.setAnimationState(ANIM_RAPID_SHOOT);
                if (elapsed >= this.rapidFireNextTick && elapsed < RAPID_FIRE_END_TICK) {
                    this.rapidFireNextTick += RAPID_FIRE_INTERVAL;
                    this.fireRapidBomb();
                }
            }
            case ATTACK_HUGE_BOMB -> {
                this.setAnimationState(ANIM_SHOOT);
                if (elapsed == SHOOT_FIRE_START_TICK) {
                    this.fireHugeBomb();
                }
            }
            default -> {
                this.setAnimationState(ANIM_SHOOT);
                if (!this.shootBomberFired && elapsed >= SHOOT_BOMBER_TICK) {
                    this.shootBomberFired = true;
                    this.fireBomberBurst();
                }
                if (this.isPhase2() && !this.shootBomberFiredSecond && elapsed >= SHOOT_BOMBER_SECOND_TICK) {
                    this.shootBomberFiredSecond = true;
                    this.fireBomberBurst();
                }
            }
        }
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.85D));
        if (this.getTarget() != null) {
            this.getLookControl().setLookAt(this.getTarget(), 30.0F, 20.0F);
        }
        this.shootAnimTicks--;
        if (this.shootAnimTicks <= 0) {
            this.shootCooldown = 100 + this.random.nextInt(60);
        }
    }

    private void tickJumpAnimation() {
        this.setAnimationState(ANIM_JUMP_ATTACK);
        int elapsed = JUMP_TOTAL_TICKS - this.jumpAnimTicks;
        if (!this.jumpLaunched && elapsed >= JUMP_LAUNCH_TICK) {
            this.jumpLaunched = true;
            LivingEntity target = this.getTarget();
            if (target != null) {
                Vec3 toTarget = target.position().subtract(this.position()).normalize();
                this.setDeltaMovement(toTarget.x * 0.6D, 0.85D, toTarget.z * 0.6D);
                this.hasImpulse = true;
            }
        }
        if (this.jumpLaunched && this.onGround() && elapsed > JUMP_LAUNCH_TICK + 3 && !this.shockwaveApplied) {
            this.shockwaveApplied = true;
            this.applyJumpShockwave();
            this.jumpShakeTicks = JUMP_SHAKE_TICKS;
            this.entityData.set(JUMP_SHAKING, true);
        }
        this.jumpAnimTicks--;
        if (this.jumpAnimTicks <= 0) {
            this.jumpCooldown = 140 + this.random.nextInt(80);
        }
    }

    private void tickSpinAnimation() {
        this.setAnimationState(ANIM_SPIN);
        int elapsed = SPIN_TOTAL_TICKS - this.spinAnimTicks;
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, 1.0D, 0.7D));
        if (elapsed >= SPIN_START_TICK && elapsed <= SPIN_END_TICK && elapsed % SPIN_HIT_INTERVAL == 0) {
            this.applySpinDamage();
        }
        this.spinAnimTicks--;
        if (this.spinAnimTicks <= 0) {
            this.spinCooldown = 160 + this.random.nextInt(80);
            this.spinHitCooldowns.clear();
        }
    }

    private Vec3 calcLaunchVelocity(Vec3 from, Vec3 to, double minSpeed) {
        Vec3 diff = to.subtract(from);
        double dh = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        double dy = diff.y;
        double gravity = AntarchySettings.waterBombGravity();
        // Choose travel time based on horizontal distance at minSpeed, minimum 8 ticks
        double t = Math.max(8.0, dh / minSpeed);
        double vx = diff.x / t;
        double vz = diff.z / t;
        // Compensate for gravity accumulating over t ticks
        double vy = dy / t + 0.5 * gravity * t;
        return new Vec3(vx, vy, vz);
    }

    private void fireRapidBomb() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        LivingEntity target = this.getTarget();
        if (target == null) return;
        Vec3 from = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 to = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        Vec3 baseVel = calcLaunchVelocity(from, to, 1.5);
        int count = this.isPhase2() ? 2 : 1;
        for (int i = 0; i < count; i++) {
            double spread = count > 1 ? (i - 0.5) * 0.15 : 0.0;
            Vec3 vel = new Vec3(baseVel.x + spread, baseVel.y, baseVel.z - spread);
            WaterBombEntity bomb = new WaterBombEntity(this.level(), this);
            bomb.setPos(from.x, from.y, from.z);
            bomb.setDeltaMovement(vel);
            bomb.hasImpulse = true;
            serverLevel.addFreshEntity(bomb);
        }
    }

    private void fireHugeBomb() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        LivingEntity target = this.getTarget();
        if (target == null) return;
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        Vec3 from = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 to = new Vec3(target.getX(), target.getY(), target.getZ());
        Vec3 vel = calcLaunchVelocity(from, to, 0.9);
        WaterBombEntity bomb = new WaterBombEntity(this.level(), this, true);
        bomb.setPos(from.x, from.y, from.z);
        bomb.setDeltaMovement(vel);
        bomb.hasImpulse = true;
        serverLevel.addFreshEntity(bomb);
    }


    private void fireBomberBurst() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        LivingEntity target = this.getTarget();
        if (target == null) return;
        int count = 3;
        for (int i = 0; i < count; i++) {
            BomberEntity bomber = AntarchyObjects.BOMBER.get().create(this.level());
            if (bomber == null) continue;
            double spreadAngle = (i - count / 2.0D) * 20.0D;
            double yaw = Math.toRadians(this.getYRot() + spreadAngle);
            double vx = -Math.sin(yaw) * 1.2D;
            double vy = 0.4D;
            double vz = Math.cos(yaw) * 1.2D;
            bomber.moveTo(this.getX() + vx * 0.5D, this.getEyeY(), this.getZ() + vz * 0.5D, this.getYRot(), 0);
            bomber.setDeltaMovement(vx, vy, vz);
            bomber.setTarget(target);
            serverLevel.addFreshEntity(bomber);
        }
    }

    private void applyJumpShockwave() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        AABB shockArea = this.getBoundingBox().inflate(JUMP_SHOCKWAVE_RADIUS, 2.0D, JUMP_SHOCKWAVE_RADIUS);
        List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, shockArea, this::canTargetEntity);
        float damage = (float) AntarchySettings.toreterrorJumpAttackDamage();
        for (LivingEntity victim : victims) {
            victim.hurt(AntarchyDamageSources.toreterrorJump(serverLevel, this), damage);
            Vec3 knockDir = victim.position().subtract(this.position()).normalize().add(0, 0.4D, 0);
            double knockback = AntarchySettings.toreterrorJumpAttackKnockback();
            victim.push(knockDir.x * knockback, 0.5D, knockDir.z * knockback);
            victim.hurtMarked = true;
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 0.75F);
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5D, this.getZ(), 3, 1.0D, 0.3D, 1.0D, 0.0D);
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH, this.getX(), this.getY() + 0.5D, this.getZ(), 40, 2.0D, 0.5D, 2.0D, 0.2D);
    }

    private void applySpinDamage() {
        if (!(this.level() instanceof ServerLevel)) return;
        AABB spinArea = this.getBoundingBox().inflate(SPIN_RADIUS, SPIN_RADIUS, SPIN_RADIUS);
        List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, spinArea, this::canTargetEntity);
        float damage = (float) AntarchySettings.toreterrorSpinDamage();
        double knockback = AntarchySettings.toreterrorSpinKnockback();
        for (LivingEntity victim : victims) {
            UUID id = victim.getUUID();
            if (this.spinHitCooldowns.getOrDefault(id, 0) > 0) continue;
            if (victim.hurt(AntarchyDamageSources.toreterrorSpin((ServerLevel) this.level(), this), damage)) {
                this.spinHitCooldowns.put(id, 20);
                Vec3 knockDir = victim.position().subtract(this.position()).normalize();
                victim.push(knockDir.x * knockback, 0.35D, knockDir.z * knockback);
                victim.hurtMarked = true;
            }
        }
    }

    private boolean canTargetEntity(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this || entity.getType() == this.getType()) return false;
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator() && this.level().getDifficulty() != Difficulty.PEACEFUL;
        }
        return entity instanceof Mob && entity.isAttackable();
    }

    private final class ToreterrorRangedGoal extends Goal {
        ToreterrorRangedGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ToreterrorEntity.this.getTarget();
            if (target == null || ToreterrorEntity.this.isInAttackAnimation()) return false;
            if (ToreterrorEntity.this.shootCooldown > 0) return false;
            double distSqr = ToreterrorEntity.this.distanceToSqr(target);
            return distSqr > CLOSE_RANGE_SQR && distSqr <= RANGED_MAX_RANGE_SQR
                    && ToreterrorEntity.this.hasLineOfSight(target);
        }

        @Override
        public void start() {
            ToreterrorEntity.this.shootAnimTicks = SHOOT_TOTAL_TICKS;
            ToreterrorEntity.this.shootBomberFired = false;
            ToreterrorEntity.this.shootBomberFiredSecond = false;
            ToreterrorEntity.this.rapidFireNextTick = 0;
            float roll = ToreterrorEntity.this.random.nextFloat();
            if (ToreterrorEntity.this.isPhase2()) {
                if (roll < 0.5F) ToreterrorEntity.this.shootAttackType = ATTACK_RAPID_FIRE;
                else if (roll < 0.65F) ToreterrorEntity.this.shootAttackType = ATTACK_HUGE_BOMB;
                else ToreterrorEntity.this.shootAttackType = ATTACK_BOMBERS;
            } else {
                if (roll < 0.5F) ToreterrorEntity.this.shootAttackType = ATTACK_RAPID_FIRE;
                else if (roll < 0.7F) ToreterrorEntity.this.shootAttackType = ATTACK_HUGE_BOMB;
                else ToreterrorEntity.this.shootAttackType = ATTACK_BOMBERS;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return ToreterrorEntity.this.shootAnimTicks > 0;
        }

        @Override
        public void stop() {
            ToreterrorEntity.this.shootAnimTicks = 0;
        }
    }

    private final class ToreterrorJumpGoal extends Goal {
        ToreterrorJumpGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ToreterrorEntity.this.getTarget();
            if (target == null || ToreterrorEntity.this.isInAttackAnimation()) return false;
            if (ToreterrorEntity.this.jumpCooldown > 0) return false;
            double distSqr = ToreterrorEntity.this.distanceToSqr(target);
            return distSqr <= JUMP_RANGE_SQR && ToreterrorEntity.this.onGround();
        }

        @Override
        public void start() {
            ToreterrorEntity.this.jumpAnimTicks = JUMP_TOTAL_TICKS;
            ToreterrorEntity.this.jumpLaunched = false;
            ToreterrorEntity.this.shockwaveApplied = false;
        }

        @Override
        public boolean canContinueToUse() {
            return ToreterrorEntity.this.jumpAnimTicks > 0;
        }

        @Override
        public void stop() {
            ToreterrorEntity.this.jumpAnimTicks = 0;
        }
    }

    private final class ToreterrorSpinGoal extends Goal {
        ToreterrorSpinGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ToreterrorEntity.this.getTarget();
            if (target == null || ToreterrorEntity.this.isInAttackAnimation()) return false;
            if (ToreterrorEntity.this.spinCooldown > 0) return false;
            return ToreterrorEntity.this.distanceToSqr(target) <= CLOSE_RANGE_SQR && ToreterrorEntity.this.onGround();
        }

        @Override
        public void start() {
            ToreterrorEntity.this.spinAnimTicks = SPIN_TOTAL_TICKS;
        }

        @Override
        public boolean canContinueToUse() {
            return ToreterrorEntity.this.spinAnimTicks > 0;
        }

        @Override
        public void stop() {
            ToreterrorEntity.this.spinAnimTicks = 0;
        }
    }
}
