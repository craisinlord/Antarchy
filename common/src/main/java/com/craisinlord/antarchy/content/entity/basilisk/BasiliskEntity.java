package com.craisinlord.antarchy.content.entity.basilisk;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
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
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class BasiliskEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(BasiliskEntity.class, EntityDataSerializers.INT);
    private static final byte ATTACK_ENTITY_EVENT = 4;
    private static final int DEATH_TICKS = 25;

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_ATTACK = 2;
    private static final int ANIM_DEATH = 3;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");

    private static final float TURN_RATE_DEG_PER_TICK = 10.0F;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int attackAnimTicks = 0;
    private boolean attackDamagePending = false;

    private int hissCooldown = AntarchySettings.basiliskHissCooldownTicks();
    private int hissChargeTimer = 0;
    @Nullable private UUID pendingTargetId;

    private int preyPetrifyCooldown = AntarchySettings.basiliskPreyPetrifyCooldownTicks();

    public BasiliskEntity(EntityType<? extends BasiliskEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = AntarchySettings.basiliskXpReward();
        this.moveControl = new BasiliskMoveControl(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.basiliskHealth())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.basiliskMovementSpeed())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.basiliskAttackDamage())
                .add(Attributes.ARMOR, AntarchySettings.basiliskArmor())
                .add(Attributes.KNOCKBACK_RESISTANCE, AntarchySettings.basiliskKnockbackResistance())
                .add(Attributes.FOLLOW_RANGE, AntarchySettings.basiliskFollowRange());
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
        }

        return hurt;
    }

    private void performAoeAttack() {
        if (this.level().isClientSide) return;
        // Start the animation; damage fires later in tick() when attackAnimTicks reaches the damage threshold
        this.attackAnimTicks = AntarchySettings.basiliskAttackAnimTicks();
        this.attackDamagePending = true;
        this.level().broadcastEntityEvent(this, ATTACK_ENTITY_EVENT);
    }

    private void tickAoeDamage() {
        if (!this.attackDamagePending) return;
        if (this.attackAnimTicks > AntarchySettings.basiliskAttackDamageTick()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        this.attackDamagePending = false;

        Vec3 forward = Vec3.directionFromRotation(0, this.yBodyRot);
        Vec3 right = new Vec3(-forward.z, 0, forward.x);
        Vec3 origin = this.position();
        double attackReach = AntarchySettings.basiliskAttackReach();

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(Math.max(8.0D, attackReach + 3.5D)),
                e -> e != this && e.isAlive()
        );

        boolean hitAnything = false;
        for (LivingEntity entity : candidates) {
            Vec3 toEntity = entity.position().subtract(origin);
            double fwdDist = toEntity.dot(forward);
            double sideDist = toEntity.dot(right);
            double upDist = toEntity.y;
            if (fwdDist >= 2.0 && fwdDist <= 7.0 && Math.abs(sideDist) <= 2.25 && upDist >= -1.0 && upDist <= 3.0) {
                if (entity.hurt(AntarchyDamageSources.basiliskBite(serverLevel, this),
                        (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                    hitAnything = true;
                }
            }
        }

        if (hitAnything) {
            this.playSound(AntarchySoundEvents.BASILISK_BITE.get(), 1.1F, 0.95F + this.random.nextFloat() * 0.08F);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackAnimTicks > 0) {
            this.attackAnimTicks--;
        }

        if (this.isDeadOrDying()) {
            this.updateAnimationState();
            return;
        }

        if (this.level().isClientSide) {
            return;
        }

        this.tickPlayerPetrification();
        this.tickPreyPetrification();
        this.tickAoeDamage();
        this.updateAnimationState();
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
            this.hissCooldown = AntarchySettings.basiliskHissCooldownTicks();
            return;
        }

        Player target = this.findGazingPlayer();
        if (target == null) {
            this.hissCooldown = AntarchySettings.basiliskHissCooldownTicks();
            return;
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                AntarchySoundEvents.BASILISK_HISS.get(), this.getSoundSource(), 1.4F, 0.92F);
        this.pendingTargetId = target.getUUID();
        this.hissChargeTimer = AntarchySettings.basiliskHissChargeTicks();
        this.hissCooldown = AntarchySettings.basiliskHissCooldownTicks();
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

        this.preyPetrifyCooldown = AntarchySettings.basiliskPreyPetrifyCooldownTicks();
        LivingEntity prey = this.findNearestPrey();
        if (prey == null) {
            return;
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                AntarchySoundEvents.BASILISK_HISS.get(), this.getSoundSource(), 1.35F, 0.95F);
        this.getLookControl().setLookAt(prey, 30.0F, 30.0F);
        prey.addEffect(new MobEffectInstance(
                AntarchyObjects.PARALYZED_EFFECT.get(),
                AntarchySettings.basiliskPreyPetrifyTicks(),
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
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(AntarchySettings.basiliskGazeRange()))) {
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
        if (player.distanceToSqr(this) > AntarchySettings.basiliskGazeRange() * AntarchySettings.basiliskGazeRange()) {
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
        return player.getLookAngle().dot(toMe) >= AntarchySettings.basiliskGazeDotThreshold();
    }

    private boolean isFacingPlayerForGaze(Player player) {
        Vec3 toPlayer = player.getEyePosition().subtract(this.getEyePosition());
        if (toPlayer.lengthSqr() < 1.0E-6D) {
            return true;
        }

        return this.getViewVector(1.0F).normalize().dot(toPlayer.normalize()) >= AntarchySettings.basiliskGazeFacingThreshold();
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
                this.getBoundingBox().inflate(AntarchySettings.basiliskPreyPetrifyRange()))) {
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
        if (this.isDeadOrDying()) {
            return state.setAndContinue(DEATH_ANIM);
        }
        if (this.attackAnimTicks > 0) {
            return state.setAndContinue(ATTACK_ANIM);
        }
        if (this.getAnimationState() == ANIM_WALK || state.isMoving()) {
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
            this.attackAnimTicks = AntarchySettings.basiliskAttackAnimTicks();
            this.setAnimationState(ANIM_ATTACK);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.setAnimationState(ANIM_DEATH);
            this.attackAnimTicks = 0;
            this.attackDamagePending = false;
            this.hissChargeTimer = 0;
            this.pendingTargetId = null;
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
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
        }
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }
        if (this.attackAnimTicks > 0) {
            this.setAnimationState(ANIM_ATTACK);
            return;
        }
        if (this.getTarget() != null || this.hissChargeTimer > 0 || this.pendingTargetId != null) {
            this.setAnimationState(ANIM_WALK);
            return;
        }
        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
            this.setAnimationState(ANIM_WALK);
            return;
        }
        this.setAnimationState(ANIM_IDLE);
    }

    private int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int state) {
        this.entityData.set(ANIMATION_STATE, state);
    }

    @Override
    public int getMaxHeadYRot() {
        return 20;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player player) {
            return this.canTargetPlayer(player) && super.canAttack(target);
        }
        return super.canAttack(target);
    }

    @Override
    public float maxUpStep() {
        return 1.5F;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        Holder<MobEffect> effect = effectInstance.getEffect();
        if (effect == MobEffects.POISON) return false;
        if (effect == AntarchyObjects.PARALYZED_EFFECT.get()) return false;
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        double reach = AntarchySettings.basiliskAttackReach() + 4.0;
        return this.distanceToSqr(target) <= reach * reach;
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

    /** Limits body rotation to TURN_RATE_DEG_PER_TICK so the long snake turns gradually. */
    private static final class BasiliskMoveControl extends MoveControl {
        private BasiliskMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                this.operation = MoveControl.Operation.WAIT;
                double dx = this.wantedX - this.mob.getX();
                double dz = this.wantedZ - this.mob.getZ();
                double dy = this.wantedY - this.mob.getY();
                if (dx * dx + dy * dy + dz * dz < 2.5E-7) {
                    this.mob.setZza(0.0F);
                    return;
                }
                float targetYaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), targetYaw, TURN_RATE_DEG_PER_TICK));
                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                this.mob.setZza(1.0F);
            }
        }
    }

    private final class PersistentNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        PersistentNearestAttackableTargetGoal(Class<T> targetType, int randomInterval, Predicate<LivingEntity> predicate) {
            super(BasiliskEntity.this, targetType, randomInterval, true, false, predicate);
            this.unseenMemoryTicks = 200;
        }
    }

    private final class BasiliskMeleeAttackGoal extends MeleeAttackGoal {
        private BasiliskMeleeAttackGoal() {
            super(BasiliskEntity.this, 1.2D, true);
        }

        @Override
        public void tick() {
            LivingEntity target = BasiliskEntity.this.getTarget();
            if (target != null) {
                BasiliskEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            if (BasiliskEntity.this.hissChargeTimer > 0 || BasiliskEntity.this.attackAnimTicks > 0) {
                this.stopMovement();
                return;
            }
            
            super.tick();

            // If we're in range after super ran, kill movement so the basilisk stops and bites
            if (target != null && BasiliskEntity.this.isWithinMeleeAttackRange(target)) {
                this.stopMovement();
            }
        }

        private void stopMovement() {
            BasiliskEntity.this.getNavigation().stop();
            BasiliskEntity.this.setSpeed(0.0F);
            BasiliskEntity.this.setZza(0.0F);
        }

        @Override
        public boolean canContinueToUse() {
            return BasiliskEntity.this.attackAnimTicks > 0 || super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (BasiliskEntity.this.attackAnimTicks > 0) return;
            if (!this.canPerformAttack(target)) return;

            this.resetAttackCooldown();
            BasiliskEntity.this.performAoeAttack();
        }
    }
}
