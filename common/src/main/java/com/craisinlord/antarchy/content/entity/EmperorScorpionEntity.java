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
    private static final byte SNAP_ATTACK_ANIM_EVENT = 4;
    private static final byte STING_ATTACK_ANIM_EVENT = 5;
    private static final byte AGRO_ANIM_EVENT = 6;

    private static final int SNAP_ATTACK_ANIM_TICKS = 23;
    private static final int SNAP_ATTACK_HIT_TICK = 12;
    private static final int SNAP_ATTACK_COOLDOWN_TICKS = 28;
    private static final int STING_ATTACK_ANIM_TICKS = 25;
    private static final int STING_ATTACK_HIT_TICK = 13;
    private static final int STING_ATTACK_COOLDOWN_TICKS = 60;
    private static final int AGRO_ANIM_TICKS = 30;
    private static final int DEATH_ANIM_TICKS = 40;
    private static final float STING_DAMAGE = 18.0F;
    private static final double STING_AOE_RADIUS = 5.0D;
    private static final int POISON_DURATION_TICKS = 300;
    private static final int WEAKNESS_DURATION_TICKS = 120;
    private static final int SUMMON_INTERVAL_TICKS = 140;
    private static final int MAX_SUMMONED_SCORPIONS = 8;
    private static final String EMPEROR_SUMMON_TAG = "antarchy_emperor_summoned";

    private static final ResourceKey<Level> THORAXIS_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis"));

    private static final RawAnimation IDLE_ANIM  = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM  = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SNAP_ANIM  = RawAnimation.begin().thenPlay("snap");
    private static final RawAnimation STING_ANIM = RawAnimation.begin().thenPlay("sting");
    private static final RawAnimation AGRO_ANIM  = RawAnimation.begin().thenPlay("agro");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private int attackAnimTicks = 0;
    private int attackHitTick = 0;
    private int attackCooldownTicks = 0;
    private int stingCooldownTicks = 0;
    private int agroAnimTicks = 0;
    private int summonCooldown = 0;
    private boolean attackDamageApplied = false;
    private boolean stingAttackActive = false;
    private boolean hasAgroed = false;
    @Nullable private LivingEntity attackTarget;

    public EmperorScorpionEntity(EntityType<? extends EmperorScorpionEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 25;
        this.bossEvent.setDarkenScreen(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 28.0D)
                .add(Attributes.ARMOR, 25.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
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
        if (this.agroAnimTicks > 0) {
            this.agroAnimTicks--;
        }
        if (this.summonCooldown > 0) {
            this.summonCooldown--;
        }

        if (!this.level().isClientSide) {
            if (this.getTarget() != null) {
                if (!this.hasAgroed) {
                    this.hasAgroed = true;
                    this.agroAnimTicks = AGRO_ANIM_TICKS;
                    this.getNavigation().stop();
                    this.level().broadcastEntityEvent(this, AGRO_ANIM_EVENT);
                    this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ROAR.get(), 2.0F, 0.7F);
                }
            } else {
                this.hasAgroed = false;
            }
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

        int toSpawn = 2 + this.random.nextInt(2);
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
        return this.performSnapAttack(target);
    }

    private boolean performSnapAttack(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ATTACK.get(), 0.9F, 0.95F + this.random.nextFloat() * 0.08F);
        }
        return hurt;
    }

    private boolean performStingAttack(Entity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        boolean hurtAny = false;
        for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(STING_AOE_RADIUS),
                entity -> entity != this
                        && entity.isAlive()
                        && !(entity instanceof EmperorScorpionEntity)
                        && !(entity instanceof ScorpionEntity))) {
            if (living.hurt(AntarchyDamageSources.emperorScorpionSting(serverLevel, this), STING_DAMAGE)) {
                hurtAny = true;
                int poisonDuration = this.level().getDifficulty() == Difficulty.HARD
                        ? POISON_DURATION_TICKS * 2 : POISON_DURATION_TICKS;
                living.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 1));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION_TICKS, 0));
            }
        }
        if (hurtAny) {
            this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ATTACK.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.08F);
        }
        return hurtAny;
    }

    private void beginSnapAttack(LivingEntity target) {
        this.attackTarget = target;
        this.attackAnimTicks = SNAP_ATTACK_ANIM_TICKS;
        this.attackHitTick = SNAP_ATTACK_HIT_TICK;
        this.attackCooldownTicks = SNAP_ATTACK_COOLDOWN_TICKS;
        this.attackDamageApplied = false;
        this.stingAttackActive = false;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, SNAP_ATTACK_ANIM_EVENT);
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
        if (!this.attackDamageApplied && this.attackAnimTicks == this.attackHitTick) {
            if (this.stingAttackActive) {
                this.attackDamageApplied = true;
                this.performStingAttack(this.attackTarget);
            } else if (this.distanceToSqr(this.attackTarget) <= this.getAttackReachSqr(this.attackTarget)) {
                this.attackDamageApplied = true;
                this.performSnapAttack(this.attackTarget);
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
        return this.attackAnimTicks > 0 || this.agroAnimTicks > 0;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == SNAP_ATTACK_ANIM_EVENT) {
            this.attackAnimTicks = SNAP_ATTACK_ANIM_TICKS;
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
        if (id == AGRO_ANIM_EVENT) {
            this.agroAnimTicks = AGRO_ANIM_TICKS;
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, this::mainController));
    }

    private PlayState mainController(AnimationState<EmperorScorpionEntity> state) {
        if (this.isDeadOrDying()) {
            return state.setAndContinue(DEATH_ANIM);
        }
        if (this.attackAnimTicks > 0) {
            return state.setAndContinue(this.stingAttackActive ? STING_ANIM : SNAP_ANIM);
        }
        if (this.agroAnimTicks > 0) {
            return state.setAndContinue(AGRO_ANIM);
        }
        if (state.isMoving()) {
            return state.setAndContinue(WALK_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= DEATH_ANIM_TICKS && !this.level().isClientSide && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
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
                    EmperorScorpionEntity.this.beginSnapAttack(enemy);
                }
                return;
            }
            super.checkAndPerformAttack(enemy);
        }
    }
}
