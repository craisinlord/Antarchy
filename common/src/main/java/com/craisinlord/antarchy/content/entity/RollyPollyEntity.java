package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.AntarchySoundEvents;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
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

public class RollyPollyEntity extends TamableAnimal implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ROLL_UP_ANIM = RawAnimation.begin().thenPlayAndHold("wheel_mode");
    private static final RawAnimation ROLLING_ANIM = RawAnimation.begin().thenLoop("roll");
    private static final RawAnimation UNROLL_ANIM = RawAnimation.begin().thenPlay("normal_mode");

    private static final int ANIM_NONE = 0;
    private static final int ANIM_ROLL_UP = 2;
    private static final int ANIM_UNROLL = 4;

    // wheel_mode is 0.75s, normal_mode is ~0.42s
    private static final int ROLL_UP_TICKS = 15;
    private static final int UNROLL_TICKS = 9;
    private static final int DEFENSIVE_CURL_TICKS = 100;
    private static final int TUMBLE_DAMAGE_INTERVAL_TICKS = 30;

    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(RollyPollyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ROLLED =
            SynchedEntityData.defineId(RollyPollyEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int rollTransitionTicks;
    private boolean unrolling;
    private int defensiveCurlTicks;
    private int tumbleTicks;
    private boolean hasCompost;

    public RollyPollyEntity(EntityType<? extends RollyPollyEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean canSpawn(EntityType<RollyPollyEntity> entityType, net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType spawnReason, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (spawnReason == net.minecraft.world.entity.MobSpawnType.SPAWN_EGG || spawnReason == net.minecraft.world.entity.MobSpawnType.SPAWNER || spawnReason == net.minecraft.world.entity.MobSpawnType.COMMAND) {
            return true;
        }

        return Mob.checkMobSpawnRules(entityType, level, spawnReason, pos, random);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.rollyPollyHealth())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.rollyPollyMovementSpeed())
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_NONE);
        builder.define(ROLLED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D) {
            @Override
            public boolean canUse() {
                // Curled-up or tamed rolly pollies stand their ground
                return !RollyPollyEntity.this.isRolled() && !RollyPollyEntity.this.isTame() && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new FertilizeCropGoal());
        this.goalSelector.addGoal(3, new EatCompostItemGoal());
        this.goalSelector.addGoal(4, new SnuggleOnBedGoal());
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.1D, Ingredient.of(AntarchyTags.Items.ROLLY_POLLY_FOOD), false) {
            @Override
            public boolean canUse() {
                return !RollyPollyEntity.this.isRolled() && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new RollingStrollGoal());
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !RollyPollyEntity.this.isRolled() && !RollyPollyEntity.this.isVehicle() && super.canUse();
            }
        });
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(AntarchyTags.Items.ROLLY_POLLY_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.isFood(stack) && (!this.isTame() || this.getHealth() < this.getMaxHealth())) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (!this.isTame()) {
                    if (this.random.nextInt(AntarchySettings.rollyPollyTameChance()) == 0) {
                        this.setTame(true, true);
                        this.setOwnerUUID(player.getUUID());
                        if (player instanceof ServerPlayer serverPlayer) {
                            CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayer, this);
                        }
                        this.stopDefensiveCurl();
                        this.setTarget(null);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                } else {
                    this.heal(4.0F);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.hasCompost && isCompostable(stack)) {
            if (!this.level().isClientSide) {
                ItemStack eaten = stack.copyWithCount(1);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.eatCompost(eaten);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isFood(stack)) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && stack.isEmpty() && !player.isShiftKeyDown() && !player.isPassengerOfSameVehicle(this)) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    private static boolean isCompostable(ItemStack stack) {
        return !stack.isEmpty() && ComposterBlock.COMPOSTABLES.containsKey(stack.getItem());
    }

    private void eatCompost(ItemStack eaten) {
        this.hasCompost = true;
        this.playSound(this.getEatingSound(eaten), 0.9F, 1.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, eaten),
                    this.getX(), this.getY(0.6D), this.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.05D);
        }
    }

    public boolean isRolled() {
        return this.entityData.get(ROLLED);
    }

    private void setRolled(boolean rolled) {
        this.entityData.set(ROLLED, rolled);
    }

    /** Toggles wheel mode; called from the ridden-roll keybind payload. */
    public void handleRollToggle(ServerPlayer player) {
        if (!this.isTame() || this.getControllingPassenger() != player) {
            return;
        }
        if (this.rollTransitionTicks > 0) {
            return;
        }
        if (this.isRolled()) {
            this.startUnroll();
        } else {
            this.startRollUp();
        }
    }

    private void startRollUp() {
        this.setRolled(true);
        this.unrolling = false;
        this.rollTransitionTicks = ROLL_UP_TICKS;
        this.setAnimationState(ANIM_ROLL_UP);
        this.playSound(SoundEvents.ARMADILLO_ROLL, 0.8F, 1.0F);
    }

    private void startUnroll() {
        this.unrolling = true;
        this.rollTransitionTicks = UNROLL_TICKS;
        this.setAnimationState(ANIM_UNROLL);
        this.playSound(SoundEvents.ARMADILLO_UNROLL_START, 0.8F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.ROLLY_POLLY_IDLE.get();
    }

    private void stopDefensiveCurl() {
        this.defensiveCurlTicks = 0;
        if (this.isRolled() && !this.isVehicle()) {
            this.startUnroll();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isRolled()) {
            amount *= 0.25F;
        }
        boolean hurt = super.hurt(source, amount);
        // Wild rolly pollies curl up defensively when attacked
        if (hurt && !this.level().isClientSide && !this.isTame() && this.isAlive()
                && !this.isRolled() && this.rollTransitionTicks <= 0) {
            this.defensiveCurlTicks = DEFENSIVE_CURL_TICKS;
            this.startRollUp();
        }
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        if (this.rollTransitionTicks > 0) {
            this.rollTransitionTicks--;
            if (this.rollTransitionTicks <= 0) {
                if (this.unrolling) {
                    this.setRolled(false);
                    this.unrolling = false;
                }
                this.setAnimationState(ANIM_NONE);
            }
        }

        if (this.defensiveCurlTicks > 0) {
            this.defensiveCurlTicks--;
            this.getNavigation().stop();
            if (this.defensiveCurlTicks <= 0 && this.isRolled() && !this.isVehicle()) {
                this.startUnroll();
            }
        }

        this.tickTumbleDamage();
    }

    private void tickTumbleDamage() {
        if (!this.isRolled() || this.rollTransitionTicks > 0
                || !(this.getControllingPassenger() instanceof Player rider)) {
            this.tumbleTicks = 0;
            return;
        }
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 0.003D;
        boolean helmeted = !rider.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        if (!moving || helmeted) {
            return;
        }
        if (++this.tumbleTicks >= TUMBLE_DAMAGE_INTERVAL_TICKS) {
            this.tumbleTicks = 0;
            rider.hurt(this.damageSources().generic(), (float) AntarchySettings.rollyPollyTumbleDamage());
        }
    }

    private int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int state) {
        this.entityData.set(ANIMATION_STATE, state);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        if (!this.isTame()) {
            return null;
        }
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof LivingEntity living ? living : null;
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            // When rolled the rider tucks into the center of the ball; otherwise sits on the shell
            double yOffset = this.isRolled() ? -0.25D : 0.45D;
            moveFunction.accept(passenger, this.getX(), this.getY() + yOffset, this.getZ());
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.defensiveCurlTicks > 0 && !this.isVehicle()) {
            super.travel(Vec3.ZERO);
            return;
        }
        super.travel(travelVector);
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        this.setRot(player.getYHeadRot(), player.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        if (this.rollTransitionTicks > 0) {
            // Hold still while rolling up / unrolling
            return Vec3.ZERO;
        }
        float forward = player.zza;
        float strafe = player.xxa * 0.5F;
        if (forward < 0.0F) {
            forward *= 0.4F;
        }
        if (this.isRolled()) {
            // Rolling is fast but drifty
            strafe *= 0.35F;
        }
        return new Vec3(strafe, 0.0D, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED)
                * (this.isRolled() ? AntarchySettings.rollyPollyRollSpeedMultiplier() : 1.0D));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        if (this.isRolled()) {
            return false;
        }
        return super.causeFallDamage(fallDistance, multiplier, damageSource);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HasCompost", this.hasCompost);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hasCompost = tag.getBoolean("HasCompost");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 2, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<RollyPollyEntity> state) {
        AnimationController<RollyPollyEntity> controller = state.getController();
        int transition = this.getAnimationState();

        if (transition == ANIM_ROLL_UP) {
            controller.setAnimationSpeed(1.0D);
            return state.setAndContinue(ROLL_UP_ANIM);
        }
        if (transition == ANIM_UNROLL) {
            controller.setAnimationSpeed(1.0D);
            return state.setAndContinue(UNROLL_ANIM);
        }

        if (this.isRolled()) {
            if (state.isMoving()) {
                controller.setAnimationSpeed(1.0D);
                return state.setAndContinue(ROLLING_ANIM);
            }
            if (controller.getCurrentAnimation() == null) {
                controller.setAnimationSpeed(1.0D);
                return state.setAndContinue(ROLL_UP_ANIM);
            }
            controller.setAnimationSpeed(0.0D);
            return PlayState.CONTINUE;
        }

        controller.setAnimationSpeed(1.0D);
        return state.setAndContinue(state.isMoving() ? WALK_ANIM : IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private class EatCompostItemGoal extends Goal {
        @Nullable
        private ItemEntity targetItem;
        private int scanCooldown;

        EatCompostItemGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (RollyPollyEntity.this.hasCompost
                    || RollyPollyEntity.this.isRolled()
                    || RollyPollyEntity.this.isVehicle()
                    || RollyPollyEntity.this.defensiveCurlTicks > 0) {
                return false;
            }
            if (this.scanCooldown > 0) {
                this.scanCooldown--;
                return false;
            }
            this.scanCooldown = 20;
            this.targetItem = this.findNearestCompostItem();
            return this.targetItem != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !RollyPollyEntity.this.hasCompost
                    && !RollyPollyEntity.this.isRolled()
                    && !RollyPollyEntity.this.isVehicle()
                    && this.targetItem != null
                    && this.targetItem.isAlive()
                    && isCompostable(this.targetItem.getItem());
        }

        @Override
        public void start() {
            if (this.targetItem != null) {
                RollyPollyEntity.this.getNavigation().moveTo(this.targetItem, 1.1D);
            }
        }

        @Override
        public void stop() {
            this.targetItem = null;
            RollyPollyEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.targetItem == null) {
                return;
            }
            RollyPollyEntity.this.getLookControl().setLookAt(this.targetItem);
            if (RollyPollyEntity.this.tickCount % 10 == 0) {
                RollyPollyEntity.this.getNavigation().moveTo(this.targetItem, 1.1D);
            }
            if (RollyPollyEntity.this.distanceToSqr(this.targetItem) < 2.0D) {
                ItemStack stack = this.targetItem.getItem();
                ItemStack eaten = stack.copyWithCount(1);
                stack.shrink(1);
                if (stack.isEmpty()) {
                    this.targetItem.discard();
                } else {
                    this.targetItem.setItem(stack.copy());
                }
                RollyPollyEntity.this.eatCompost(eaten);
            }
        }

        @Nullable
        private ItemEntity findNearestCompostItem() {
            ItemEntity nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (ItemEntity item : RollyPollyEntity.this.level().getEntitiesOfClass(ItemEntity.class,
                    RollyPollyEntity.this.getBoundingBox().inflate(8.0D, 4.0D, 8.0D),
                    item -> item.isAlive() && isCompostable(item.getItem()))) {
                double dist = RollyPollyEntity.this.distanceToSqr(item);
                if (dist < nearestDist) {
                    nearest = item;
                    nearestDist = dist;
                }
            }
            return nearest;
        }
    }

    private class FertilizeCropGoal extends MoveToBlockGoal {
        private static final int ROLL_ON_CROP_TICKS = 20;

        private int rollTicks;

        FertilizeCropGoal() {
            super(RollyPollyEntity.this, 1.1D, 8, 2);
        }

        @Override
        public boolean canUse() {
            return RollyPollyEntity.this.hasCompost
                    && !RollyPollyEntity.this.isRolled()
                    && !RollyPollyEntity.this.isVehicle()
                    && RollyPollyEntity.this.defensiveCurlTicks <= 0
                    && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return RollyPollyEntity.this.hasCompost
                    && !RollyPollyEntity.this.isVehicle()
                    && super.canContinueToUse();
        }

        @Override
        protected int nextStartTick(PathfinderMob mob) {
            return reducedTickDelay(40 + mob.getRandom().nextInt(40));
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            return state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state);
        }

        @Override
        protected BlockPos getMoveToTarget() {
            return this.blockPos;
        }

        @Override
        public double acceptedDistance() {
            return 1.75D;
        }

        @Override
        public void start() {
            this.rollTicks = 0;
            super.start();
        }

        @Override
        public void stop() {
            super.stop();
            if (RollyPollyEntity.this.isRolled() && RollyPollyEntity.this.rollTransitionTicks <= 0) {
                RollyPollyEntity.this.startUnroll();
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.isReachedTarget()) {
                this.rollTicks = 0;
                return;
            }
            RollyPollyEntity.this.getNavigation().stop();
            if (!RollyPollyEntity.this.isRolled()) {
                if (RollyPollyEntity.this.rollTransitionTicks <= 0) {
                    RollyPollyEntity.this.startRollUp();
                }
                return;
            }
            if (RollyPollyEntity.this.rollTransitionTicks > 0) {
                return;
            }
            if (++this.rollTicks >= ROLL_ON_CROP_TICKS) {
                this.fertilize();
            }
        }

        private void fertilize() {
            if (RollyPollyEntity.this.level() instanceof ServerLevel serverLevel) {
                BlockState state = serverLevel.getBlockState(this.blockPos);
                if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
                    crop.performBonemeal(serverLevel, RollyPollyEntity.this.random, this.blockPos, state);
                    serverLevel.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, this.blockPos, 15);
                }
            }
            RollyPollyEntity.this.hasCompost = false;
            RollyPollyEntity.this.startUnroll();
        }
    }

    private class RollingStrollGoal extends Goal {
        @Nullable
        private Vec3 targetPos;
        private int rollingTicks;
        private int maxRollingTicks;
        private boolean startedMoving;
        private boolean finished;

        RollingStrollGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (RollyPollyEntity.this.isRolled()
                    || RollyPollyEntity.this.isVehicle()
                    || RollyPollyEntity.this.defensiveCurlTicks > 0
                    || RollyPollyEntity.this.hasCompost
                    || RollyPollyEntity.this.rollTransitionTicks > 0) {
                return false;
            }
            if (RollyPollyEntity.this.getRandom().nextInt(reducedTickDelay(240)) != 0) {
                return false;
            }
            this.targetPos = DefaultRandomPos.getPos(RollyPollyEntity.this, 16, 4);
            return this.targetPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.finished
                    && this.rollingTicks < this.maxRollingTicks
                    && !RollyPollyEntity.this.isVehicle()
                    && !RollyPollyEntity.this.hasCompost;
        }

        @Override
        public void start() {
            this.rollingTicks = 0;
            this.maxRollingTicks = this.adjustedTickDelay(140 + RollyPollyEntity.this.getRandom().nextInt(100));
            this.startedMoving = false;
            this.finished = false;
            RollyPollyEntity.this.startRollUp();
        }

        @Override
        public void stop() {
            this.targetPos = null;
            RollyPollyEntity.this.getNavigation().stop();
            if (RollyPollyEntity.this.isRolled() && RollyPollyEntity.this.rollTransitionTicks <= 0) {
                RollyPollyEntity.this.startUnroll();
            }
        }

        @Override
        public void tick() {
            if (RollyPollyEntity.this.rollTransitionTicks > 0 || this.targetPos == null) {
                return;
            }
            this.rollingTicks++;
            if (RollyPollyEntity.this.getNavigation().isDone()) {
                if (this.startedMoving) {
                    this.finished = true;
                } else {
                    RollyPollyEntity.this.getNavigation().moveTo(
                            this.targetPos.x, this.targetPos.y, this.targetPos.z,
                            AntarchySettings.rollyPollyRollSpeedMultiplier());
                    this.startedMoving = true;
                }
            }
        }
    }

    private class SnuggleOnBedGoal extends Goal {
        @Nullable
        private Player ownerPlayer;
        @Nullable
        private BlockPos bedPos;
        private int onBedTicks;

        SnuggleOnBedGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (!RollyPollyEntity.this.isTame() || RollyPollyEntity.this.isVehicle()) {
                return false;
            }
            if (!(RollyPollyEntity.this.getOwner() instanceof Player player) || !player.isSleeping()) {
                return false;
            }
            if (RollyPollyEntity.this.distanceToSqr(player) > 100.0D) {
                return false;
            }
            BlockPos pos = player.blockPosition();
            BlockState stateAt = RollyPollyEntity.this.level().getBlockState(pos);
            if (!stateAt.is(BlockTags.BEDS)) {
                return false;
            }
            this.ownerPlayer = player;
            this.bedPos = stateAt.getOptionalValue(BedBlock.FACING)
                    .map(direction -> pos.relative(direction.getOpposite()))
                    .orElse(pos);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return RollyPollyEntity.this.isTame()
                    && !RollyPollyEntity.this.isVehicle()
                    && this.ownerPlayer != null
                    && this.ownerPlayer.isSleeping()
                    && this.bedPos != null;
        }

        @Override
        public void start() {
            this.onBedTicks = 0;
        }

        @Override
        public void stop() {
            this.ownerPlayer = null;
            this.bedPos = null;
            this.onBedTicks = 0;
            RollyPollyEntity.this.getNavigation().stop();
            if (RollyPollyEntity.this.isRolled() && RollyPollyEntity.this.rollTransitionTicks <= 0) {
                RollyPollyEntity.this.startUnroll();
            }
        }

        @Override
        public void tick() {
            if (this.ownerPlayer == null || this.bedPos == null) {
                return;
            }
            if (RollyPollyEntity.this.distanceToSqr(this.ownerPlayer) > 2.5D) {
                this.onBedTicks = 0;
                if (RollyPollyEntity.this.isRolled()) {
                    if (RollyPollyEntity.this.rollTransitionTicks <= 0) {
                        RollyPollyEntity.this.startUnroll();
                    }
                } else {
                    RollyPollyEntity.this.getNavigation().moveTo(
                            this.bedPos.getX() + 0.5D, this.bedPos.getY() + 1.0D, this.bedPos.getZ() + 0.5D, 1.1D);
                }
                return;
            }
            RollyPollyEntity.this.getNavigation().stop();
            if (++this.onBedTicks > this.adjustedTickDelay(16)
                    && !RollyPollyEntity.this.isRolled()
                    && RollyPollyEntity.this.rollTransitionTicks <= 0) {
                RollyPollyEntity.this.startRollUp();
            }
        }
    }
}
