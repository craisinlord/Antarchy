package com.craisinlord.antarchy.content.entity.basilisk;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
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
import software.bernie.geckolib.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;
import java.util.function.Predicate;

public class BasiliskEntity extends Monster implements GeoEntity {
    private static final byte ATTACK_ENTITY_EVENT = 4;
    private static final int ATTACK_ANIM_TICKS = 18;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    private static final int HISS_COOLDOWN_TICKS = 600;
    private static final double GAZE_RANGE = 12.0D;
    private static final double GAZE_RANGE_SQR = GAZE_RANGE * GAZE_RANGE;
    private static final double GAZE_DOT_THRESHOLD = 0.64D;
    private static final double BASILISK_GAZE_FACING_THRESHOLD = 0.45D;
    private static final int HISS_CHARGE_TICKS = 30;

    private static final int PREY_PETRIFY_COOLDOWN_TICKS = 500;
    private static final double PREY_PETRIFY_RANGE = 8.0D;
    private static final int PREY_PETRIFY_DURATION_TICKS = 200;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int attackAnimTicks = 0;

    private int hissCooldown = 100;
    private int hissChargeTimer = 0;
    @Nullable private UUID pendingTargetId;

    private int preyPetrifyCooldown = 200;

    public BasiliskEntity(EntityType<? extends BasiliskEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    public static boolean canSpawn(EntityType<BasiliskEntity> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG
                || spawnReason == MobSpawnType.SPAWNER
                || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        return level.getDifficulty() != Difficulty.PEACEFUL
                && pos.getY() >= 60 && pos.getY() <= 90
                && level.getMaxLocalRawBrightness(pos) <= AntarchySettings.basiliskSpawnMaxLightLevel()
                && level.getEntitiesOfClass(BasiliskEntity.class, new AABB(pos).inflate(40.0D), Entity::isAlive).isEmpty()
                && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BasiliskMeleeAttackGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new PersistentNearestAttackableTargetGoal<>(Player.class, 5,
                entity -> entity instanceof Player player && this.canTargetPlayer(player)));
        this.targetSelector.addGoal(3, new PersistentNearestAttackableTargetGoal<>(LivingEntity.class, 5,
                entity -> entity != this && entity.getType().is(AntarchyTags.Entities.BASILISK_PREY)));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt;
        if (this.level() instanceof ServerLevel serverLevel) {
            hurt = target.hurt(
                    AntarchyDamageSources.basiliskBite(serverLevel, this),
                    (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE)
            );
        } else {
            hurt = super.doHurtTarget(target);
        }

        if (hurt) {
            this.level().broadcastEntityEvent(this, ATTACK_ENTITY_EVENT);
            this.playSound(AntarchySoundEvents.BASILISK_BITE.get(), 1.1F, 0.95F + this.random.nextFloat() * 0.08F);
        }

        return hurt;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackAnimTicks > 0) {
            this.attackAnimTicks--;
        }

        if (this.level().isClientSide) {
            return;
        }

        this.tickPlayerPetrification();
        this.tickPreyPetrification();
    }

    private void tickPlayerPetrification() {
        if (this.hissChargeTimer > 0) {
            Player target = this.getPendingPlayerTarget();
            if (!this.canMaintainGazeOn(target)) {
                this.pendingTargetId = null;
                this.hissChargeTimer = 0;
                return;
            }

            this.getNavigation().stop();
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.hissChargeTimer--;
            if (this.hissChargeTimer == 0) {
                this.pendingTargetId = null;
                target.addEffect(new MobEffectInstance(
                        AntarchyObjects.PARALYZED_EFFECT.get(),
                        AntarchySettings.basiliskPlayerParalyzeTicks(),
                        0,
                        false,
                        true,
                        true
                ));
            }
            return;
        }

        if (this.hissCooldown > 0) {
            this.hissCooldown--;
            return;
        }

        if (!AntarchySettings.basiliskPetrifyingGazeEnabled()) {
            this.hissCooldown = HISS_COOLDOWN_TICKS;
            return;
        }

        Player target = this.findGazingPlayer();
        if (target == null) {
            this.hissCooldown = 20;
            return;
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                AntarchySoundEvents.BASILISK_HISS.get(), this.getSoundSource(), 1.4F, 0.92F);
        this.pendingTargetId = target.getUUID();
        this.hissChargeTimer = HISS_CHARGE_TICKS;
        this.hissCooldown = HISS_COOLDOWN_TICKS;
        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    private void tickPreyPetrification() {
        if (this.preyPetrifyCooldown > 0) {
            this.preyPetrifyCooldown--;
            return;
        }

        if (this.getTarget() != null || this.hissChargeTimer > 0) {
            return;
        }

        this.preyPetrifyCooldown = PREY_PETRIFY_COOLDOWN_TICKS;
        LivingEntity prey = this.findNearestPrey();
        if (prey == null) {
            return;
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                AntarchySoundEvents.BASILISK_HISS.get(), this.getSoundSource(), 1.35F, 0.95F);
        this.getLookControl().setLookAt(prey, 30.0F, 30.0F);
        prey.addEffect(new MobEffectInstance(
                AntarchyObjects.PARALYZED_EFFECT.get(),
                PREY_PETRIFY_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));
    }

    @Nullable
    private Player getPendingPlayerTarget() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.pendingTargetId == null) {
            return null;
        }

        return serverLevel.getPlayerByUUID(this.pendingTargetId);
    }

    @Nullable
    private Player findGazingPlayer() {
        LivingEntity currentTarget = this.getTarget();
        if (currentTarget instanceof Player player && this.canMaintainGazeOn(player)) {
            return player;
        }

        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GAZE_RANGE))) {
            if (!this.canMaintainGazeOn(player)) {
                continue;
            }

            double distance = player.distanceToSqr(this);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }

    private boolean canMaintainGazeOn(@Nullable Player player) {
        if (!this.canTargetPlayer(player)) {
            return false;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN)) {
            return false;
        }
        if (player.distanceToSqr(this) > GAZE_RANGE_SQR) {
            return false;
        }
        if (!this.hasLineOfSight(player)) {
            return false;
        }

        return this.isPlayerGazingAtMe(player) && this.isFacingPlayerForGaze(player);
    }

    private boolean isPlayerGazingAtMe(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 toMe = this.getEyePosition().subtract(eye).normalize();
        return player.getLookAngle().dot(toMe) >= GAZE_DOT_THRESHOLD;
    }

    private boolean isFacingPlayerForGaze(Player player) {
        Vec3 toPlayer = player.getEyePosition().subtract(this.getEyePosition());
        if (toPlayer.lengthSqr() < 1.0E-6D) {
            return true;
        }

        return this.getViewVector(1.0F).normalize().dot(toPlayer.normalize()) >= BASILISK_GAZE_FACING_THRESHOLD;
    }

    private boolean canTargetPlayer(@Nullable Player player) {
        return player != null
                && player.isAlive()
                && !player.isDeadOrDying()
                && !player.isSpectator()
                && !player.isCreative();
    }

    @Nullable
    private LivingEntity findNearestPrey() {
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(PREY_PETRIFY_RANGE))) {
            if (entity == this || entity instanceof Player || entity.isDeadOrDying()) {
                continue;
            }
            if (!entity.getType().is(AntarchyTags.Entities.BASILISK_PREY)) {
                continue;
            }
            if (entity.getType().is(AntarchyTags.Entities.PARALYSIS_IMMUNE)) {
                continue;
            }
            if (!this.hasLineOfSight(entity)) {
                continue;
            }

            double distance = entity.distanceToSqr(this);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, this::mainController)
                .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>()));
    }

    private PlayState mainController(AnimationState<BasiliskEntity> state) {
        if (this.attackAnimTicks > 0) {
            return state.setAndContinue(ATTACK_ANIM);
        }
        if (this.getTarget() != null || this.hissChargeTimer > 0 || this.pendingTargetId != null) {
            return state.setAndContinue(WALK_ANIM);
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
    public void handleEntityEvent(byte id) {
        if (id == ATTACK_ENTITY_EVENT) {
            this.attackAnimTicks = ATTACK_ANIM_TICKS;
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player player) {
            return this.canTargetPlayer(player) && super.canAttack(target);
        }
        return super.canAttack(target);
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
    protected SoundEvent getHurtSound(DamageSource source) {
        return AntarchySoundEvents.BASILISK_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.BASILISK_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }

    private final class PersistentNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        PersistentNearestAttackableTargetGoal(Class<T> targetType, int randomInterval, Predicate<LivingEntity> predicate) {
            super(BasiliskEntity.this, targetType, randomInterval, true, false, predicate);
            this.unseenMemoryTicks = 200;
        }
    }

    private final class BasiliskMeleeAttackGoal extends MeleeAttackGoal {
        private BasiliskMeleeAttackGoal() {
            super(BasiliskEntity.this, 1.1D, true);
        }

        @Override
        public void tick() {
            if (BasiliskEntity.this.hissChargeTimer > 0) {
                BasiliskEntity.this.getNavigation().stop();
                return;
            }
            super.tick();
        }

        @Override
        public boolean canContinueToUse() {
            return BasiliskEntity.this.attackAnimTicks > 0 || super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy) {
            if (BasiliskEntity.this.attackAnimTicks > 0) {
                return;
            }
            if (this.canPerformAttack(enemy) && this.isTimeToAttack()) {
                this.resetAttackCooldown();
                BasiliskEntity.this.doHurtTarget(enemy);
            }
        }
    }
}
