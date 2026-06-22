package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import org.jetbrains.annotations.Nullable;

public class ScorpionEntity extends Monster implements GeoEntity {
    private static final byte ATTACK_ANIM_EVENT = 4;

    private static final int ATTACK_ANIM_TICKS = 12;
    private static final int ATTACK_HIT_TICK = 6;
    private static final int ATTACK_COOLDOWN_TICKS = 16;
    private static final int POISON_DURATION_TICKS = 80;

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int attackAnimTicks = 0;
    private int attackCooldownTicks = 0;
    private boolean attackDamageApplied = false;
    @Nullable private LivingEntity attackTarget;

    public ScorpionEntity(EntityType<? extends ScorpionEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    public static boolean canSpawn(EntityType<ScorpionEntity> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG
                || spawnReason == MobSpawnType.SPAWNER
                || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ScorpionMeleeAttackGoal());
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.attackCooldownTicks > 0) {
            this.attackCooldownTicks--;
        }
        if (this.attackAnimTicks > 0) {
            if (!this.level().isClientSide) {
                this.tickAttackWindup();
            }
            this.attackAnimTicks--;
            if (this.attackAnimTicks <= 0) {
                this.resetAttackState();
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            int duration = this.level().getDifficulty() == Difficulty.HARD
                    ? POISON_DURATION_TICKS * 2
                    : POISON_DURATION_TICKS;
            living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0));
            this.playSound(AntarchySoundEvents.SCORPION_ATTACK.get(), 0.65F, 0.95F + this.random.nextFloat() * 0.1F);
        }
        return hurt;
    }

    private void beginAttack(LivingEntity target) {
        this.attackTarget = target;
        this.attackAnimTicks = ATTACK_ANIM_TICKS;
        this.attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
        this.attackDamageApplied = false;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, ATTACK_ANIM_EVENT);
        this.commitLunge(target, 0.48D, 0.12D);
    }

    private void tickAttackWindup() {
        if (this.attackTarget == null || !this.attackTarget.isAlive()) {
            this.resetAttackState();
            return;
        }

        this.getLookControl().setLookAt(this.attackTarget, 30.0F, 30.0F);
        if (!this.attackDamageApplied
                && this.attackAnimTicks == ATTACK_HIT_TICK
                && this.distanceToSqr(this.attackTarget) <= this.getAttackReachSqr(this.attackTarget)) {
            this.attackDamageApplied = true;
            this.doHurtTarget(this.attackTarget);
        }

        if (this.attackAnimTicks <= 0) {
            this.resetAttackState();
        }
    }

    private double getAttackReachSqr(LivingEntity target) {
        double reach = this.getBbWidth() * 1.7D + target.getBbWidth();
        return reach * reach + 1.0D;
    }

    private void resetAttackState() {
        this.attackAnimTicks = 0;
        this.attackDamageApplied = false;
        this.attackTarget = null;
    }

    private void commitLunge(LivingEntity target, double horizontalSpeed, double verticalSpeed) {
        Vec3 lunge = target.position().subtract(this.position());
        Vec3 horizontal = new Vec3(lunge.x, 0.0D, lunge.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return;
        }
        horizontal = horizontal.normalize().scale(horizontalSpeed);
        this.setDeltaMovement(this.getDeltaMovement().add(horizontal.x, verticalSpeed, horizontal.z));
        this.hasImpulse = true;
    }

    private boolean isAttackLocked() {
        return this.attackAnimTicks > 0;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == ATTACK_ANIM_EVENT) {
            this.attackAnimTicks = ATTACK_ANIM_TICKS;
            this.attackDamageApplied = false;
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, this::mainController));
    }

    private PlayState mainController(AnimationState<ScorpionEntity> state) {
        if (this.attackAnimTicks > 0) {
            return state.setAndContinue(ATTACK_ANIM);
        }
        if (state.isMoving()) {
            return state.setAndContinue(WALK_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.SCORPION_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return AntarchySoundEvents.SCORPION_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.SCORPION_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    private final class ScorpionMeleeAttackGoal extends MeleeAttackGoal {
        private ScorpionMeleeAttackGoal() {
            super(ScorpionEntity.this, 1.15D, true);
        }

        @Override
        public void tick() {
            if (ScorpionEntity.this.attackTarget != null) {
                ScorpionEntity.this.getLookControl().setLookAt(ScorpionEntity.this.attackTarget, 30.0F, 30.0F);
            }
            super.tick();
        }

        @Override
        public boolean canContinueToUse() {
            return ScorpionEntity.this.isAttackLocked() || super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy) {
            if (ScorpionEntity.this.isAttackLocked()) {
                return;
            }
            if (this.canPerformAttack(enemy) && this.isTimeToAttack() && ScorpionEntity.this.attackCooldownTicks <= 0) {
                this.resetAttackCooldown();
                ScorpionEntity.this.beginAttack(enemy);
                return;
            }
            super.checkAndPerformAttack(enemy);
        }
    }
}
