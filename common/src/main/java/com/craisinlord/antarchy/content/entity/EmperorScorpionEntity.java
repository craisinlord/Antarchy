package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.levelgen.Heightmap;
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

public class EmperorScorpionEntity extends Monster implements GeoEntity {
    private static final byte CLAW_ATTACK_ANIM_EVENT = 4;
    private static final byte STING_ATTACK_ANIM_EVENT = 5;

    private static final int CLAW_ATTACK_ANIM_TICKS = 12;
    private static final int CLAW_ATTACK_HIT_TICK = 6;
    private static final int CLAW_ATTACK_COOLDOWN_TICKS = 16;
    private static final int STING_ATTACK_ANIM_TICKS = 18;
    private static final int STING_ATTACK_HIT_TICK = 9;
    private static final int STING_ATTACK_COOLDOWN_TICKS = 42;
    private static final int POISON_DURATION_TICKS = 100;
    private static final int WEAKNESS_DURATION_TICKS = 80;
    private static final int SUMMON_INTERVAL_TICKS = 180;
    private static final int MAX_SUMMONED_SCORPIONS = 4;
    private static final String EMPEROR_SUMMON_TAG = "antarchy_emperor_summoned";

    private static final ResourceKey<Level> THORAXIS_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis"));

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private int attackAnimTicks = 0;
    private int attackHitTick = 0;
    private int attackCooldownTicks = 0;
    private int stingCooldownTicks = 0;
    private int summonCooldown = 0;
    private boolean attackDamageApplied = false;
    private boolean stingAttackActive = false;
    @Nullable private LivingEntity attackTarget;

    public EmperorScorpionEntity(EntityType<? extends EmperorScorpionEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 25;
        this.bossEvent.setDarkenScreen(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    public static boolean canSpawn(EntityType<EmperorScorpionEntity> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG
                || spawnReason == MobSpawnType.SPAWNER
                || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }
        if (!level.getLevel().dimension().equals(THORAXIS_KEY)) {
            return false;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EmperorScorpionAttackGoal());
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public float maxUpStep() {
        return 1.5F;
    }

    @Override
    public int getMaxHeadYRot() {
        return 35;
    }

    @Override
    public void tick() {
        super.tick();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.attackCooldownTicks > 0) {
            this.attackCooldownTicks--;
        }
        if (this.stingCooldownTicks > 0) {
            this.stingCooldownTicks--;
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
        if (this.summonCooldown > 0) {
            this.summonCooldown--;
        }

        if (this.getTarget() != null
                && this.summonCooldown <= 0
                && !this.isAttackLocked()
                && this.getHealth() <= this.getMaxHealth() * 0.75F
                && this.level() instanceof ServerLevel serverLevel) {
            this.trySummonScorpions(serverLevel);
        }
    }

    private void trySummonScorpions(ServerLevel serverLevel) {
        long nearby = serverLevel.getEntitiesOfClass(ScorpionEntity.class, this.getBoundingBox().inflate(16.0D),
                scorpion -> scorpion.getTags().contains(EMPEROR_SUMMON_TAG)).size();
        this.summonCooldown = SUMMON_INTERVAL_TICKS;
        if (nearby >= MAX_SUMMONED_SCORPIONS) {
            return;
        }

        int toSpawn = 1 + this.random.nextInt(2);
        EntityType<ScorpionEntity> scorpionType = AntarchyObjects.SCORPION.get();
        for (int i = 0; i < toSpawn; i++) {
            double offsetX = (this.random.nextDouble() - 0.5D) * 6.0D;
            double offsetZ = (this.random.nextDouble() - 0.5D) * 6.0D;
            BlockPos xzPos = BlockPos.containing(this.getX() + offsetX, 0, this.getZ() + offsetZ);
            BlockPos spawnPos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xzPos);
            @Nullable ScorpionEntity scorpion = scorpionType.create(serverLevel);
            if (scorpion == null) continue;
            scorpion.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    this.getYRot(), 0.0F);
            scorpion.addTag(EMPEROR_SUMMON_TAG);
            scorpion.setTarget(this.getTarget());
            scorpion.finalizeSpawn(serverLevel,
                    serverLevel.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.MOB_SUMMONED, null);
            serverLevel.addFreshEntity(scorpion);
        }
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ROAR.get(), 1.2F, 0.75F);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return this.performStingAttack(target);
    }

    private boolean performClawAttack(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ATTACK.get(), 0.9F, 0.95F + this.random.nextFloat() * 0.08F);
        }
        return hurt;
    }

    private boolean performStingAttack(Entity target) {
        boolean hurt;
        if (this.level() instanceof ServerLevel serverLevel) {
            hurt = target.hurt(AntarchyDamageSources.emperorScorpionSting(serverLevel, this),
                    (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        } else {
            hurt = super.doHurtTarget(target);
        }
        if (hurt && target instanceof LivingEntity living) {
            int poisonDuration = this.level().getDifficulty() == Difficulty.HARD
                    ? POISON_DURATION_TICKS * 2 : POISON_DURATION_TICKS;
            living.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 0));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION_TICKS, 0));
            this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ATTACK.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.08F);
        }
        return hurt;
    }

    private void beginClawAttack(LivingEntity target) {
        this.attackTarget = target;
        this.attackAnimTicks = CLAW_ATTACK_ANIM_TICKS;
        this.attackHitTick = CLAW_ATTACK_HIT_TICK;
        this.attackCooldownTicks = CLAW_ATTACK_COOLDOWN_TICKS;
        this.attackDamageApplied = false;
        this.stingAttackActive = false;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, CLAW_ATTACK_ANIM_EVENT);
        this.commitLunge(target, 0.42D, 0.10D);
    }

    private void beginStingAttack(LivingEntity target) {
        this.attackTarget = target;
        this.attackAnimTicks = STING_ATTACK_ANIM_TICKS;
        this.attackHitTick = STING_ATTACK_HIT_TICK;
        this.attackCooldownTicks = STING_ATTACK_COOLDOWN_TICKS;
        this.stingCooldownTicks = STING_ATTACK_COOLDOWN_TICKS;
        this.attackDamageApplied = false;
        this.stingAttackActive = true;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, STING_ATTACK_ANIM_EVENT);
        this.commitLunge(target, 0.55D, 0.14D);
    }

    private void tickAttackWindup() {
        if (this.attackTarget == null || !this.attackTarget.isAlive()) {
            this.resetAttackState();
            return;
        }

        this.getLookControl().setLookAt(this.attackTarget, 30.0F, 30.0F);
        if (!this.attackDamageApplied
                && this.attackAnimTicks == this.attackHitTick
                && this.distanceToSqr(this.attackTarget) <= this.getAttackReachSqr(this.attackTarget)) {
            this.attackDamageApplied = true;
            if (this.stingAttackActive) {
                this.performStingAttack(this.attackTarget);
            } else {
                this.performClawAttack(this.attackTarget);
            }
        }

        if (this.attackAnimTicks <= 0) {
            this.resetAttackState();
        }
    }

    private double getAttackReachSqr(LivingEntity target) {
        double reach = this.getBbWidth() * 1.9D + target.getBbWidth();
        return reach * reach + 1.5D;
    }

    private void resetAttackState() {
        this.attackAnimTicks = 0;
        this.attackHitTick = 0;
        this.attackDamageApplied = false;
        this.stingAttackActive = false;
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

    private boolean shouldUseSting(LivingEntity target) {
        return this.stingCooldownTicks <= 0
                && this.distanceToSqr(target) <= this.getAttackReachSqr(target) + 2.0D
                && (this.getHealth() <= this.getMaxHealth() * 0.6F || this.random.nextFloat() < 0.45F);
    }

    private boolean isAttackLocked() {
        return this.attackAnimTicks > 0;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == CLAW_ATTACK_ANIM_EVENT) {
            this.attackAnimTicks = CLAW_ATTACK_ANIM_TICKS;
            this.attackDamageApplied = false;
            this.stingAttackActive = false;
            return;
        }
        if (id == STING_ATTACK_ANIM_EVENT) {
            this.attackAnimTicks = STING_ATTACK_ANIM_TICKS;
            this.attackDamageApplied = false;
            this.stingAttackActive = true;
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, this::mainController));
    }

    private PlayState mainController(AnimationState<EmperorScorpionEntity> state) {
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
        return AntarchySoundEvents.EMPEROR_SCORPION_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return AntarchySoundEvents.EMPEROR_SCORPION_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.EMPEROR_SCORPION_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public void setCustomName(@Nullable net.minecraft.network.chat.Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        this.bossEvent.addPlayer(serverPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        this.bossEvent.removePlayer(serverPlayer);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
    }

    private final class EmperorScorpionAttackGoal extends MeleeAttackGoal {
        private EmperorScorpionAttackGoal() {
            super(EmperorScorpionEntity.this, 1.15D, true);
        }

        @Override
        public void tick() {
            if (EmperorScorpionEntity.this.attackTarget != null) {
                EmperorScorpionEntity.this.getLookControl().setLookAt(
                        EmperorScorpionEntity.this.attackTarget, 30.0F, 30.0F);
            }
            super.tick();
        }

        @Override
        public boolean canContinueToUse() {
            return EmperorScorpionEntity.this.isAttackLocked() || super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy) {
            if (EmperorScorpionEntity.this.isAttackLocked()) {
                return;
            }
            if (this.canPerformAttack(enemy)
                    && this.isTimeToAttack()
                    && EmperorScorpionEntity.this.attackCooldownTicks <= 0) {
                this.resetAttackCooldown();
                if (EmperorScorpionEntity.this.shouldUseSting(enemy)) {
                    EmperorScorpionEntity.this.beginStingAttack(enemy);
                } else {
                    EmperorScorpionEntity.this.beginClawAttack(enemy);
                }
                return;
            }
            super.checkAndPerformAttack(enemy);
        }
    }
}
