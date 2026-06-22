package com.craisinlord.antarchy.content.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.Shearable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public abstract class AppleCowEntity extends Cow implements GeoEntity, Shearable {
    private static final EntityDataAccessor<Boolean> SLEEPING =
            SynchedEntityData.defineId(AppleCowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> QUIRK_TICKS =
            SynchedEntityData.defineId(AppleCowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SHEARED =
            SynchedEntityData.defineId(AppleCowEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String SLEEPING_KEY = "Sleeping";
    private static final String SLEEP_TICKS_KEY = "SleepTicks";
    private static final String NAP_COOLDOWN_KEY = "NapCooldown";
    private static final String QUIRK_TICKS_KEY = "QuirkTicks";
    private static final String SHEARED_KEY = "Sheared";
    private static final String GRASS_EAT_TICKS_KEY = "GrassEatTicks";
    private static final String MAIN_CONTROLLER = "main_controller";
    private static final String SLEEP_TRANSITION_CONTROLLER = "sleep_transition_controller";
    private static final String START_SLEEP_TRIGGER = "start_sleep";
    private static final String WAKE_UP_TRIGGER = "wake_up";

    private static final int IDLE_ANIMATION_CYCLE_TICKS = 40;
    private static final int QUIRK_ANIMATION_TICKS = 35;
    private static final int QUIRK_CHANCE = 50;
    private static final int DAY_NAP_MIN_TICKS = 20 * 60;
    private static final int DAY_NAP_MAX_TICKS = 20 * 60 * 2;
    private static final int DAY_NAP_COOLDOWN_TICKS = 20 * 60 * 10;
    private static final int NIGHT_NAP_COOLDOWN_TICKS = 20 * 60 * 8;
    private static final int GRASS_REGROW_TICKS = 20 * 60 * 3;

    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SLEEP_ANIM = RawAnimation.begin().thenLoop("sleep");
    private static final RawAnimation QUIRK_ANIM = RawAnimation.begin().thenPlay("quirk");
    private static final RawAnimation START_SLEEP_ANIM = RawAnimation.begin().thenPlay("start sleep");
    private static final RawAnimation WAKE_UP_ANIM = RawAnimation.begin().thenPlay("wake up");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int sleepTicksRemaining;
    private int napCooldownTicks;
    private int idleAnimationCycleTicks;
    private int grassEatTicks;

    protected AppleCowEntity(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
        this.napCooldownTicks = randomNapCooldown();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLEEPING, false);
        builder.define(QUIRK_TICKS, 0);
        builder.define(SHEARED, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new AppleCowSleepGoal());
        this.goalSelector.addGoal(1, new AppleCowGrazeGoal());
    }

    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return this.getType().create(level) instanceof Cow offspring ? offspring : null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, MAIN_CONTROLLER, 0, this::mainAnimController));
        controllers.add(new AnimationController<>(this, SLEEP_TRANSITION_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim(START_SLEEP_TRIGGER, START_SLEEP_ANIM)
                .triggerableAnim(WAKE_UP_TRIGGER, WAKE_UP_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        int quirkTicks = this.getQuirkTicks();
        if (quirkTicks > 0) {
            this.setQuirkTicks(quirkTicks - 1);
        }

        if (this.napCooldownTicks > 0) {
            this.napCooldownTicks--;
        }

        if (this.isSleeping()) {
            this.applySleepStillness();
            this.idleAnimationCycleTicks = 0;

            if (this.shouldWakeUp()) {
                this.stopCowSleeping();
                return;
            }

            if (this.sleepTicksRemaining > 0) {
                this.sleepTicksRemaining--;
                if (this.sleepTicksRemaining <= 0) {
                    this.stopCowSleeping();
                }
            }
            return;
        }

        if (!this.isSheared()) {
            this.grassEatTicks = 0;
        }

        if (this.shouldStartNap()) {
            this.startSleeping(Mth.nextInt(this.random, DAY_NAP_MIN_TICKS, DAY_NAP_MAX_TICKS));
            return;
        }

        this.tickIdleQuirk();
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        this.stopCowSleeping();
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(Items.SHEARS) && this.readyForShearing()) {
            if (!this.level().isClientSide) {
                this.shear(player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS);
                heldItem.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            this.stopCowSleeping();
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        this.stopCowSleeping();
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(SLEEPING_KEY, this.isSleeping());
        tag.putInt(SLEEP_TICKS_KEY, this.sleepTicksRemaining);
        tag.putInt(NAP_COOLDOWN_KEY, this.napCooldownTicks);
        tag.putInt(QUIRK_TICKS_KEY, this.getQuirkTicks());
        tag.putBoolean(SHEARED_KEY, this.isSheared());
        tag.putInt(GRASS_EAT_TICKS_KEY, this.grassEatTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SLEEPING, tag.getBoolean(SLEEPING_KEY));
        this.sleepTicksRemaining = tag.getInt(SLEEP_TICKS_KEY);
        this.napCooldownTicks = tag.contains(NAP_COOLDOWN_KEY) ? tag.getInt(NAP_COOLDOWN_KEY) : randomNapCooldown();
        this.setQuirkTicks(tag.getInt(QUIRK_TICKS_KEY));
        this.setSheared(tag.getBoolean(SHEARED_KEY));
        this.grassEatTicks = tag.getInt(GRASS_EAT_TICKS_KEY);
    }

    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    public int getQuirkTicks() {
        return this.entityData.get(QUIRK_TICKS);
    }

    public boolean isSheared() {
        return this.entityData.get(SHEARED);
    }

    private void setQuirkTicks(int ticks) {
        this.entityData.set(QUIRK_TICKS, Math.max(0, ticks));
    }

    private void setSheared(boolean sheared) {
        this.entityData.set(SHEARED, sheared);
    }

    private PlayState mainAnimController(AnimationState<AppleCowEntity> state) {
        if (this.isSleeping()) {
            state.getController().setAnimationSpeed(1.0D);
            return state.setAndContinue(SLEEP_ANIM);
        }

        if (state.isMoving()) {
            state.getController().setAnimationSpeed(1.0D);
            return state.setAndContinue(WALK_ANIM);
        }

        if (this.getQuirkTicks() > 0) {
            state.getController().setAnimationSpeed(1.0D);
            return state.setAndContinue(QUIRK_ANIM);
        }

        state.getController().setAnimationSpeed(1.0D);
        return state.setAndContinue(IDLE_ANIM);
    }

    private void tickIdleQuirk() {
        if (this.getQuirkTicks() > 0 || !this.isEligibleForSleep() || this.isMovingForAnimation()) {
            this.idleAnimationCycleTicks = 0;
            return;
        }

        this.idleAnimationCycleTicks++;
        if (this.idleAnimationCycleTicks < IDLE_ANIMATION_CYCLE_TICKS) {
            return;
        }

        this.idleAnimationCycleTicks = 0;
        if (this.random.nextInt(QUIRK_CHANCE) == 0) {
            this.setQuirkTicks(QUIRK_ANIMATION_TICKS);
        }
    }

    private boolean shouldStartNap() {
        return this.napCooldownTicks <= 0
                && this.isEligibleForSleep();
    }

    private boolean shouldWakeUp() {
        if (!this.isSleeping()) {
            return false;
        }

        if (!this.isAlive() || this.isInWaterOrBubble() || this.isOnFire() || this.hurtTime > 0 || this.isLeashed()
                || this.isPassenger() || this.isVehicle() || this.isInLove() || !this.onGround()) {
            return true;
        }

        return false;
    }

    private boolean isEligibleForSleep() {
        return this.onGround()
                && !this.isInWaterOrBubble()
                && !this.isOnFire()
                && !this.isLeashed()
                && !this.isPassenger()
                && !this.isVehicle()
                && !this.isInLove()
                && this.getTarget() == null
                && !this.isPanicking()
                && !this.isMovingForAnimation();
    }

    private boolean isMovingForAnimation() {
        return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D || this.getNavigation().isInProgress();
    }

    private void startSleeping(int sleepTicks) {
        this.entityData.set(SLEEPING, true);
        this.sleepTicksRemaining = sleepTicks;
        this.idleAnimationCycleTicks = 0;
        this.setQuirkTicks(0);
        this.applySleepStillness();
        if (!this.level().isClientSide) {
            this.triggerAnim(SLEEP_TRANSITION_CONTROLLER, START_SLEEP_TRIGGER);
        }
    }

    private void stopCowSleeping() {
        if (!this.isSleeping()) {
            return;
        }

        this.entityData.set(SLEEPING, false);
        this.sleepTicksRemaining = 0;
        this.napCooldownTicks = randomNapCooldown();
        if (!this.level().isClientSide) {
            this.triggerAnim(SLEEP_TRANSITION_CONTROLLER, WAKE_UP_TRIGGER);
        }
    }

    private void applySleepStillness() {
        Vec3 motion = this.getDeltaMovement();
        this.getNavigation().stop();
        this.setJumping(false);
        this.setSprinting(false);
        this.zza = 0.0F;
        this.xxa = 0.0F;
        this.setDeltaMovement(0.0D, motion.y < 0.0D ? motion.y : 0.0D, 0.0D);
    }

    private int randomNapCooldown() {
        return this.level().isNight() ? NIGHT_NAP_COOLDOWN_TICKS : DAY_NAP_COOLDOWN_TICKS;
    }

    @Override
    public boolean readyForShearing() {
        return !this.isBaby() && !this.isSleeping() && !this.isSheared();
    }

    @Override
    public void shear(SoundSource source) {
        if (this.level().isClientSide || !this.readyForShearing()) {
            return;
        }

        this.setSheared(true);
        this.grassEatTicks = 0;
        this.stopCowSleeping();
        this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
        this.spawnAtLocation(new ItemStack(this.shearDropItem(), 2), this.getBbHeight() * 0.66F);
    }

    private net.minecraft.world.item.Item shearDropItem() {
        if (this instanceof AppleCowEntityVariants.EnchantedGoldenAppleCow) {
            return Items.ENCHANTED_GOLDEN_APPLE;
        }

        if (this instanceof AppleCowEntityVariants.GoldenAppleCow) {
            return Items.GOLDEN_APPLE;
        }

        return Items.APPLE;
    }

    private final class AppleCowSleepGoal extends Goal {
        private AppleCowSleepGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return AppleCowEntity.this.isSleeping();
        }

        @Override
        public boolean canContinueToUse() {
            return AppleCowEntity.this.isSleeping();
        }

        @Override
        public void start() {
            AppleCowEntity.this.applySleepStillness();
        }

        @Override
        public void tick() {
            AppleCowEntity.this.applySleepStillness();
        }
    }

    private final class AppleCowGrazeGoal extends Goal {
        private AppleCowGrazeGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return AppleCowEntity.this.isSheared()
                    && AppleCowEntity.this.isOnGrassBlock()
                    && AppleCowEntity.this.isEligibleForSleep()
                    && AppleCowEntity.this.random.nextInt(20) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return AppleCowEntity.this.isSheared()
                    && AppleCowEntity.this.isOnGrassBlock()
                    && AppleCowEntity.this.grassEatTicks < GRASS_REGROW_TICKS;
        }

        @Override
        public void start() {
            AppleCowEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            AppleCowEntity.this.getNavigation().stop();
            if (!AppleCowEntity.this.level().isClientSide && AppleCowEntity.this.isSheared() && AppleCowEntity.this.isOnGrassBlock()) {
                AppleCowEntity.this.grassEatTicks++;
                if (AppleCowEntity.this.grassEatTicks >= GRASS_REGROW_TICKS) {
                    AppleCowEntity.this.setSheared(false);
                    AppleCowEntity.this.grassEatTicks = 0;
                    AppleCowEntity.this.playSound(SoundEvents.GENERIC_EAT, 0.7F, 1.0F + AppleCowEntity.this.random.nextFloat() * 0.2F);
                } else if (AppleCowEntity.this.grassEatTicks % 40 == 0) {
                    AppleCowEntity.this.playSound(SoundEvents.GENERIC_EAT, 0.4F, 1.0F + AppleCowEntity.this.random.nextFloat() * 0.15F);
                }
            }
        }

        @Override
        public void stop() {
            if (!AppleCowEntity.this.isSheared()) {
                AppleCowEntity.this.grassEatTicks = 0;
            }
        }
    }

    private boolean isOnGrassBlock() {
        BlockPos below = this.blockPosition().below();
        return this.level().getBlockState(below).is(Blocks.GRASS_BLOCK);
    }
}
