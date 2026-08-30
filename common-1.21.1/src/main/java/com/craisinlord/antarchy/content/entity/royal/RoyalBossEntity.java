package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import com.craisinlord.antarchy.content.entity.multipart.MultipartLayout;
import com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamController;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

public abstract class RoyalBossEntity extends Monster implements GeoEntity, MultipartEntityOwner {
    public static final float MODEL_RENDER_SCALE = 2.0F;
    public static final float GAMEPLAY_WIDTH = 30.0F;
    public static final float GAMEPLAY_HEIGHT = 30.0F;

    public enum Phase {
        ONE(1, 1.00F),
        TWO(2, 0.72F),
        THREE(3, 0.48F);

        private final int maxConcurrentHeadAttacks;
        private final float cooldownScale;

        Phase(int maxConcurrentHeadAttacks, float cooldownScale) {
            this.maxConcurrentHeadAttacks = maxConcurrentHeadAttacks;
            this.cooldownScale = cooldownScale;
        }

        public int maxConcurrentHeadAttacks() {
            return this.maxConcurrentHeadAttacks;
        }

        public float cooldownScale() {
            return this.cooldownScale;
        }
    }

    private static final float PHASE_TWO_THRESHOLD = 0.70F;
    private static final float PHASE_THREE_THRESHOLD = 0.35F;

    private static final int BITE_DURATION_TICKS = 16;
    private static final int BITE_HIT_TICK = 6;
    private static final int DEATH_TICKS = 60;
    private static final double CRUSH_RADIUS = 3.5D;
    private static final int CRUSH_MAX_BLOCKS = 24;
    private static final double CRUSH_MAX_RESISTANCE = 60.0D;
    private static final float CRUSH_DROP_CHANCE = 0.1F;

    private static final EntityDataAccessor<Boolean> FIRING =
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_BEAM_END =
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BEAM_END_X =
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_END_Y =
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_END_Z =
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected final RoyalBeamController beamController = new RoyalBeamController(this);
    private final ServerBossEvent bossEvent;
    private final RoyalHead[] heads = {
            new RoyalHead(RoyalHead.Slot.LEFT),
            new RoyalHead(RoyalHead.Slot.CENTER),
            new RoyalHead(RoyalHead.Slot.RIGHT)
    };

    @Nullable
    private Entity[] multipartParts;

    protected RoyalBossEntity(EntityType<? extends RoyalBossEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
        if (this.isFlyingBoss()) {
            this.moveControl = new FlyingMoveControl(this, 20, true);
        }
        this.bossEvent = new com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent(
                this.getUUID(),
                Component.translatable(entityType.getDescriptionId()),
                this.bossBarColor(),
                BossEvent.BossBarOverlay.PROGRESS);
    }

    protected abstract BossEvent.BossBarColor bossBarColor();

    protected abstract SoundEvent royalIdleSound();

    protected abstract SoundEvent royalHurtSound();

    protected abstract SoundEvent royalDeathSound();

    protected abstract SoundEvent royalBiteSound();

    public static AttributeSupplier.Builder createBaseAttributes(double health, double attackDamage) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.ARMOR, AntarchySettings.royalBossArmor())
                .add(Attributes.FOLLOW_RANGE, AntarchySettings.royalBossFollowRange())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.royalBossMovementSpeed())
                .add(Attributes.FLYING_SPEED, AntarchySettings.royalBossMovementSpeed())
                .add(Attributes.KNOCKBACK_RESISTANCE, AntarchySettings.royalBossKnockbackResistance())
                .add(Attributes.STEP_HEIGHT, AntarchySettings.royalBossStepHeight());
    }

    protected abstract String geoName();

    protected abstract boolean isFlyingBoss();

    public String geoNameForRender() {
        return this.geoName();
    }

    @Override
    public MultipartLayout antarchy$getMultipartLayout() {
        return RoyalBossMultipartLayout.INSTANCE;
    }

    @Override
    @Nullable
    public Entity[] antarchy$getMultipartParts() {
        return this.multipartParts;
    }

    @Override
    public void antarchy$setMultipartParts(@Nullable Entity[] parts) {
        this.multipartParts = parts;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        if (this.isFlyingBoss()) {
            FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
            navigation.setCanOpenDoors(false);
            navigation.setCanFloat(true);
            return navigation;
        }
        return super.createNavigation(level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 48.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FIRING, false);
        builder.define(HAS_BEAM_END, false);
        builder.define(BEAM_END_X, 0.0F);
        builder.define(BEAM_END_Y, 0.0F);
        builder.define(BEAM_END_Z, 0.0F);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.royalIdleSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.royalHurtSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.royalDeathSound();
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isFlyingBoss() && this.isEffectiveAi()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isFlyingBoss()) {
            this.setNoGravity(true);
        }
        if (this.level().isClientSide) {
            return;
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        for (RoyalHead head : this.heads) {
            head.tick();
        }

        if (this.isDeadOrDying()) {
            return;
        }

        LivingEntity primaryTarget = this.getTarget();
        if (primaryTarget == null || !primaryTarget.isAlive() || !this.canAttack(primaryTarget)) {
            for (RoyalHead head : this.heads) {
                head.setTarget(null);
            }
            return;
        }

        this.getLookControl().setLookAt(primaryTarget, 30.0F, 30.0F);
        this.assignHeadTargets(primaryTarget);
        this.steerTowardTarget(primaryTarget);
        this.tickBodyCrush();

        for (RoyalHead head : this.heads) {
            if (head.tickBiteHit()) {
                this.applyBiteHit(head);
            }
        }

        Phase phase = this.phase();
        int activeHeadAttacks = 0;
        for (RoyalHead head : this.heads) {
            if (head.busy()) {
                activeHeadAttacks++;
            }
        }

        for (RoyalHead head : this.heads) {
            if (activeHeadAttacks >= phase.maxConcurrentHeadAttacks()) {
                break;
            }
            if (!head.readyToAttack()) {
                continue;
            }
            LivingEntity headTarget = head.target(this.level());
            if (headTarget != null && this.headWithinBiteReach(head, headTarget)) {
                this.startBite(head, phase);
                activeHeadAttacks++;
            }
        }
    }

    public Phase phase() {
        float fraction = this.getMaxHealth() <= 0.0F ? 1.0F : this.getHealth() / this.getMaxHealth();
        if (fraction > PHASE_TWO_THRESHOLD) {
            return Phase.ONE;
        }
        if (fraction > PHASE_THREE_THRESHOLD) {
            return Phase.TWO;
        }
        return Phase.THREE;
    }

    private void assignHeadTargets(LivingEntity fallback) {
        List<Player> candidates = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(AntarchySettings.royalBossFollowRange()),
                player -> player.isAlive() && this.canAttack(player));
        if (candidates.isEmpty()) {
            for (RoyalHead head : this.heads) {
                head.setTarget(fallback);
            }
            return;
        }

        int[] assignedCounts = new int[candidates.size()];
        for (RoyalHead head : this.heads) {
            Vec3 anchor = this.headAnchor(head);
            int bestIndex = 0;
            int bestCount = Integer.MAX_VALUE;
            double bestDistance = Double.MAX_VALUE;
            for (int i = 0; i < candidates.size(); i++) {
                double distance = candidates.get(i).position().distanceToSqr(anchor);
                if (assignedCounts[i] < bestCount || (assignedCounts[i] == bestCount && distance < bestDistance)) {
                    bestIndex = i;
                    bestCount = assignedCounts[i];
                    bestDistance = distance;
                }
            }
            assignedCounts[bestIndex]++;
            head.setTarget(candidates.get(bestIndex));
        }
    }

    private void steerTowardTarget(LivingEntity target) {
        double reach = this.getBbWidth() * 0.5D + this.biteReach() * 0.5D;
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        if ((dx * dx + dz * dz) > reach * reach) {
            double wantedY = this.isFlyingBoss() ? target.getY() + 3.0D : target.getY();
            this.getMoveControl().setWantedPosition(target.getX(), wantedY, target.getZ(), this.isFlyingBoss() ? 1.1D : 1.0D);
        } else {
            this.getNavigation().stop();
        }
    }

    private void tickBodyCrush() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.tickCount % 10 != 0) {
            return;
        }
        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0016D) {
            return;
        }
        double yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        Vec3 center = new Vec3(
                this.getX() + forward.x * (this.getBbWidth() * 0.45D),
                this.getY() + (this.isFlyingBoss() ? 0.0D : 1.0D),
                this.getZ() + forward.z * (this.getBbWidth() * 0.45D));
        RoyalBlockDestruction.destroySphere(serverLevel, this, center, CRUSH_RADIUS, CRUSH_MAX_BLOCKS, CRUSH_MAX_RESISTANCE, CRUSH_DROP_CHANCE);
    }

    private void startBite(RoyalHead head, Phase phase) {
        int cooldown = Math.max(10, Mth.floor(AntarchySettings.royalBossBiteCooldownTicks() * phase.cooldownScale()));
        head.startBite(BITE_DURATION_TICKS, BITE_HIT_TICK, cooldown);
        this.triggerAnim(head.slot().controllerName(), "bite");
        this.playSound(this.royalBiteSound(), 3.0F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    private void applyBiteHit(RoyalHead head) {
        if (this.level().isClientSide) {
            return;
        }
        Vec3 anchor = this.headAnchor(head);
        double reach = this.biteReach();
        float damage = (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * AntarchySettings.royalBossBiteDamageMultiplier());
        DamageSource damageSource = this.damageSources().mobAttack(this);
        AABB box = new AABB(anchor, anchor).inflate(reach);
        for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, box, entity -> entity.isAlive() && entity != this)) {
            if (living instanceof RoyalBossEntity || living.getType() == this.getType()) {
                continue;
            }
            if (living.position().distanceToSqr(anchor) > reach * reach) {
                continue;
            }
            if (living.hurt(damageSource, damage)) {
                Vec3 push = living.position().subtract(anchor).multiply(1.0D, 0.0D, 1.0D);
                if (push.lengthSqr() < 1.0E-4D) {
                    push = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
                }
                push = push.normalize().scale(1.4D);
                living.push(push.x, 0.42D, push.z);
                living.hurtMarked = true;
            }
        }
    }

    private boolean headWithinBiteReach(RoyalHead head, LivingEntity target) {
        Vec3 anchor = this.headAnchor(head);
        double reach = this.biteReach() + target.getBbWidth();
        return target.position().distanceToSqr(anchor) <= reach * reach;
    }

    private double biteReach() {
        return AntarchySettings.royalBossBiteReach();
    }

    private Vec3 headAnchor(RoyalHead head) {
        int partIndex = head.slot().partIndex();
        Entity[] parts = this.multipartParts;
        if (parts != null && partIndex < parts.length && parts[partIndex] != null) {
            Entity part = parts[partIndex];
            return part.position().add(0.0D, part.getBbHeight() * 0.5D, 0.0D);
        }

        MultipartPartDefinition spec = RoyalBossMultipartLayout.INSTANCE.parts()[partIndex];
        double yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        return new Vec3(
                this.getX() + forward.x * spec.forwardOffset() + right.x * spec.lateralOffset(),
                this.getY() + spec.yOffset(),
                this.getZ() + forward.z * spec.forwardOffset() + right.z * spec.lateralOffset());
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            for (RoyalHead head : this.heads) {
                head.reset();
            }
            this.beamController.stop();
            this.setFiringRoyalBeam(false);
            this.setRoyalBeamEndPosition(null);
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 20 && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
        }
        if (this.deathTime >= DEATH_TICKS) {
            this.remove(RemovalReason.KILLED);
            this.dropExperience(this);
        }
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
    public boolean hurt(DamageSource source, float amount) {
        float cappedAmount = (float) Math.min(amount, AntarchySettings.royalBossMaxSingleHitDamage());
        return super.hurt(source, cappedAmount);
    }

    public boolean isFiringRoyalBeam() {
        return this.entityData.get(FIRING);
    }

    public void setFiringRoyalBeam(boolean firing) {
        this.entityData.set(FIRING, firing);
    }

    public RoyalBeamController beamController() {
        return this.beamController;
    }

    public void setRoyalBeamEndPosition(@Nullable Vec3 position) {
        this.entityData.set(HAS_BEAM_END, position != null);
        if (position != null) {
            this.entityData.set(BEAM_END_X, (float) position.x);
            this.entityData.set(BEAM_END_Y, (float) position.y);
            this.entityData.set(BEAM_END_Z, (float) position.z);
        }
    }

    @Nullable
    public Vec3 getRoyalBeamEndPosition() {
        if (!this.entityData.get(HAS_BEAM_END)) {
            return null;
        }
        return new Vec3(this.entityData.get(BEAM_END_X), this.entityData.get(BEAM_END_Y), this.entityData.get(BEAM_END_Z));
    }

    public Vec3 getRoyalBeamShootFrom(float partialTicks) {
        double x = Mth.lerp(partialTicks, this.xo, this.getX());
        double y = Mth.lerp(partialTicks, this.yo, this.getY()) + this.getBbHeight() * 0.72D;
        double z = Mth.lerp(partialTicks, this.zo, this.getZ());
        return new Vec3(x, y, z);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 4, this::locomotionPredicate));
        controllers.add(new AnimationController<>(this, "head_left", 0, RoyalBossEntity::headIdle)
                .triggerableAnim("bite", RawAnimation.begin().thenPlay("bite_1"))
                .triggerableAnim("shoot", RawAnimation.begin().thenLoop("shoot_1")));
        controllers.add(new AnimationController<>(this, "head_center", 0, RoyalBossEntity::headIdle)
                .triggerableAnim("bite", RawAnimation.begin().thenPlay("bite_2"))
                .triggerableAnim("shoot", RawAnimation.begin().thenLoop("shoot_2")));
        controllers.add(new AnimationController<>(this, "head_right", 0, RoyalBossEntity::headIdle)
                .triggerableAnim("bite", RawAnimation.begin().thenPlay("bite_3"))
                .triggerableAnim("shoot", RawAnimation.begin().thenLoop("shoot_3")));
        controllers.add(new AnimationController<>(this, "body_action", 0, this::bodyActionPredicate)
                .triggerableAnim("stomp", RawAnimation.begin().thenPlay("stomp"))
                .triggerableAnim("wing_gust", RawAnimation.begin().thenPlay("wing_gust"))
                .triggerableAnim("minion_spawn", RawAnimation.begin().thenPlay("minion_spawn")));
    }

    private PlayState locomotionPredicate(AnimationState<RoyalBossEntity> state) {
        if (this.isDeadOrDying()) {
            return PlayState.STOP;
        }
        if (this.isFlyingBoss()) {
            state.setAndContinue(FLY_ANIM);
        } else if (state.isMoving()) {
            state.setAndContinue(WALK_ANIM);
        } else {
            state.setAndContinue(IDLE_ANIM);
        }
        return PlayState.CONTINUE;
    }

    private PlayState bodyActionPredicate(AnimationState<RoyalBossEntity> state) {
        if (this.isDeadOrDying()) {
            return state.setAndContinue(DEATH_ANIM);
        }
        return PlayState.STOP;
    }

    private static PlayState headIdle(AnimationState<RoyalBossEntity> state) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
