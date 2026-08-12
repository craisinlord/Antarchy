package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.BlockPos;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ElkaEntity extends Animal implements GeoEntity {
    private static final EntityDataAccessor<Boolean> SITTING =
            SynchedEntityData.defineId(ElkaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> QUIRK_TICKS =
            SynchedEntityData.defineId(ElkaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> QUIRK_VARIANT =
            SynchedEntityData.defineId(ElkaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TICKS =
            SynchedEntityData.defineId(ElkaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CRY_TICKS =
            SynchedEntityData.defineId(ElkaEntity.class, EntityDataSerializers.INT);

    private static final String SITTING_KEY = "Sitting";
    private static final String SIT_TICKS_KEY = "SitTicks";
    private static final String SIT_COOLDOWN_KEY = "SitCooldown";
    private static final String QUIRK_TICKS_KEY = "QuirkTicks";
    private static final String QUIRK_VARIANT_KEY = "QuirkVariant";

    private static final String MAIN_CONTROLLER = "main_controller";
    private static final String SIT_TRANSITION_CONTROLLER = "sit_transition_controller";
    private static final String START_SIT_TRIGGER = "start_sit";
    private static final String GET_UP_TRIGGER = "get_up";

    private static final int IDLE_ANIMATION_CYCLE_TICKS = 60;
    private static final int QUIRK_ANIMATION_TICKS = 40;
    private static final int QUIRK_CHANCE = 60;
    private static final int SIT_MIN_TICKS = 20 * 60;
    private static final int SIT_MAX_TICKS = 20 * 60 * 3;
    private static final int SIT_COOLDOWN_TICKS = 20 * 60 * 6;
    private static final int SIT_CHECK_CHANCE_SHADE = 300;
    private static final int SIT_CHECK_CHANCE_OPEN = 1200;
    private static final int ATTACK_ANIM_TICKS = 20;
    private static final int CRY_ANIM_TICKS = 60;

    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SIT_IDLE_ANIM = RawAnimation.begin().thenLoop("sit_idle");
    private static final RawAnimation QUIRK_ANIM = RawAnimation.begin().thenPlay("quirk");
    private static final RawAnimation QUIRK_2_ANIM = RawAnimation.begin().thenPlay("quirk_2");
    private static final RawAnimation QUIRK_3_ANIM = RawAnimation.begin().thenPlay("quirk_3");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation CRY_ANIM = RawAnimation.begin().thenPlay("cry");
    private static final RawAnimation START_SIT_ANIM = RawAnimation.begin().thenPlay("sit");
    private static final RawAnimation GET_UP_ANIM = RawAnimation.begin().thenPlay("get_up");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int sitTicksRemaining;
    private int sitCooldownTicks;
    private int idleAnimationCycleTicks;

    public ElkaEntity(EntityType<? extends ElkaEntity> entityType, net.minecraft.world.level.Level level) {
        super(entityType, level);
        this.sitCooldownTicks = SIT_COOLDOWN_TICKS;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SITTING, false);
        this.entityData.define(QUIRK_TICKS, 0);
        this.entityData.define(QUIRK_VARIANT, 0);
        this.entityData.define(ATTACK_TICKS, 0);
        this.entityData.define(CRY_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        if (!this.isBaby()) {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        }
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, Ingredient.of(AntarchyObjects.PEACH.get()), false));
        this.goalSelector.addGoal(4, new ElkaSitGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new ElkaHurtByTargetGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(AntarchyObjects.PEACH.get());
    }

    @Override
    public ElkaEntity getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return AntarchyObjects.ELKA.get().create(level);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }

    private void startSitting() {
        this.entityData.set(SITTING, true);
        this.sitTicksRemaining = Mth.nextInt(this.random, SIT_MIN_TICKS, SIT_MAX_TICKS);
        this.getNavigation().stop();
        if (!this.level().isClientSide) {
            this.triggerAnim(SIT_TRANSITION_CONTROLLER, START_SIT_TRIGGER);
        }
    }

    private void stopSitting() {
        if (!this.isSitting()) {
            return;
        }
        this.entityData.set(SITTING, false);
        this.sitTicksRemaining = 0;
        this.sitCooldownTicks = SIT_COOLDOWN_TICKS;
        if (!this.level().isClientSide) {
            this.triggerAnim(SIT_TRANSITION_CONTROLLER, GET_UP_TRIGGER);
        }
    }

    private boolean isEligibleToSit() {
        return this.onGround()
                && !this.isInWaterOrBubble()
                && !this.isOnFire()
                && !this.isLeashed()
                && !this.isPassenger()
                && !this.isVehicle()
                && !this.isInLove()
                && !this.isBaby()
                && this.getTarget() == null
                && this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4D
                && this.getNavigation().isDone();
    }

    private boolean isNearShade() {
        BlockPos base = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-3, -1, -3), base.offset(3, 3, 3))) {
            if (this.level().getBlockState(pos).is(BlockTags.LEAVES)) {
                return true;
            }
        }
        return false;
    }

    private void tickSitDecision() {
        if (this.isSitting()) {
            if (this.getTarget() != null || this.isInWaterOrBubble() || this.isOnFire() || this.hurtTime > 0
                    || this.isLeashed() || this.isPassenger() || this.isVehicle() || this.isInLove() || !this.onGround()) {
                this.stopSitting();
                return;
            }
            if (this.sitTicksRemaining > 0) {
                this.sitTicksRemaining--;
                if (this.sitTicksRemaining <= 0) {
                    this.stopSitting();
                }
            }
            return;
        }

        if (this.sitCooldownTicks > 0) {
            this.sitCooldownTicks--;
            return;
        }

        if (!this.isEligibleToSit()) {
            return;
        }

        int chance = this.isNearShade() ? SIT_CHECK_CHANCE_SHADE : SIT_CHECK_CHANCE_OPEN;
        if (this.random.nextInt(chance) == 0) {
            this.startSitting();
        }
    }

    private void tickIdleQuirk() {
        if (this.entityData.get(QUIRK_TICKS) > 0 || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D
                || this.getTarget() != null) {
            this.idleAnimationCycleTicks = 0;
            return;
        }

        this.idleAnimationCycleTicks++;
        if (this.idleAnimationCycleTicks < IDLE_ANIMATION_CYCLE_TICKS) {
            return;
        }
        this.idleAnimationCycleTicks = 0;

        if (this.random.nextInt(QUIRK_CHANCE) != 0) {
            return;
        }

        this.entityData.set(QUIRK_TICKS, QUIRK_ANIMATION_TICKS);
        if (this.isBaby()) {
            this.entityData.set(QUIRK_VARIANT, 0);
        } else if (this.isSitting()) {
            this.entityData.set(QUIRK_VARIANT, 2);
        } else {
            this.entityData.set(QUIRK_VARIANT, this.random.nextBoolean() ? 0 : 1);
        }
    }

    public void triggerCry() {
        this.entityData.set(CRY_TICKS, CRY_ANIM_TICKS);
    }

    public void triggerAttackAnim() {
        this.entityData.set(ATTACK_TICKS, ATTACK_ANIM_TICKS);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.triggerAttackAnim();
        }
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        int quirkTicks = this.entityData.get(QUIRK_TICKS);
        if (quirkTicks > 0) {
            this.entityData.set(QUIRK_TICKS, quirkTicks - 1);
        }

        int attackTicks = this.entityData.get(ATTACK_TICKS);
        if (attackTicks > 0) {
            this.entityData.set(ATTACK_TICKS, attackTicks - 1);
        }

        int cryTicks = this.entityData.get(CRY_TICKS);
        if (cryTicks > 0) {
            this.entityData.set(CRY_TICKS, cryTicks - 1);
        }

        this.tickSitDecision();
        this.tickIdleQuirk();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        this.stopSitting();
        return super.hurt(source, amount);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.isBaby()
                ? EntityDimensions.scalable(1.0F, 1.5F)
                : EntityDimensions.scalable(2.2F, 2.75F);
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        this.refreshDimensions();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(SITTING_KEY, this.isSitting());
        tag.putInt(SIT_TICKS_KEY, this.sitTicksRemaining);
        tag.putInt(SIT_COOLDOWN_KEY, this.sitCooldownTicks);
        tag.putInt(QUIRK_TICKS_KEY, this.entityData.get(QUIRK_TICKS));
        tag.putInt(QUIRK_VARIANT_KEY, this.entityData.get(QUIRK_VARIANT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SITTING, tag.getBoolean(SITTING_KEY));
        this.sitTicksRemaining = tag.getInt(SIT_TICKS_KEY);
        this.sitCooldownTicks = tag.contains(SIT_COOLDOWN_KEY) ? tag.getInt(SIT_COOLDOWN_KEY) : SIT_COOLDOWN_TICKS;
        this.entityData.set(QUIRK_TICKS, tag.getInt(QUIRK_TICKS_KEY));
        this.entityData.set(QUIRK_VARIANT, tag.getInt(QUIRK_VARIANT_KEY));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PANDA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PANDA_BITE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PANDA_DEATH;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, MAIN_CONTROLLER, 0, this::mainAnimController));
        if (!this.isBaby()) {
            controllers.add(new AnimationController<>(this, SIT_TRANSITION_CONTROLLER, 0, state -> PlayState.STOP)
                    .triggerableAnim(START_SIT_TRIGGER, START_SIT_ANIM)
                    .triggerableAnim(GET_UP_TRIGGER, GET_UP_ANIM));
        }
    }

    private PlayState mainAnimController(AnimationState<ElkaEntity> state) {
        if (this.entityData.get(ATTACK_TICKS) > 0) {
            return state.setAndContinue(ATTACK_ANIM);
        }
        if (this.entityData.get(CRY_TICKS) > 0) {
            return state.setAndContinue(CRY_ANIM);
        }
        if (state.isMoving()) {
            return state.setAndContinue(WALK_ANIM);
        }
        if (this.entityData.get(QUIRK_TICKS) > 0) {
            return switch (this.entityData.get(QUIRK_VARIANT)) {
                case 1 -> state.setAndContinue(QUIRK_2_ANIM);
                case 2 -> state.setAndContinue(QUIRK_3_ANIM);
                default -> state.setAndContinue(QUIRK_ANIM);
            };
        }
        if (this.isSitting()) {
            return state.setAndContinue(SIT_IDLE_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private final class ElkaSitGoal extends Goal {
        private ElkaSitGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return ElkaEntity.this.isSitting();
        }

        @Override
        public boolean canContinueToUse() {
            return ElkaEntity.this.isSitting();
        }

        @Override
        public void start() {
            ElkaEntity.this.getNavigation().stop();
            ElkaEntity.this.setDeltaMovement(0.0D, ElkaEntity.this.getDeltaMovement().y, 0.0D);
        }

        @Override
        public void tick() {
            ElkaEntity.this.getNavigation().stop();
            ElkaEntity.this.setZza(0.0F);
            ElkaEntity.this.setXxa(0.0F);
        }
    }

    private static final class ElkaHurtByTargetGoal extends HurtByTargetGoal {
        private ElkaHurtByTargetGoal(ElkaEntity elka) {
            super(elka);
        }

        @Override
        public void start() {
            super.start();
            if (this.mob instanceof ElkaEntity elka && elka.isBaby()) {
                this.alertOthers();
                this.stop();
            }
        }

        @Override
        protected void alertOther(Mob mob, LivingEntity target) {
            if (mob instanceof ElkaEntity elka && !elka.isBaby()) {
                elka.triggerCry();
                super.alertOther(mob, target);
            }
        }
    }
}
