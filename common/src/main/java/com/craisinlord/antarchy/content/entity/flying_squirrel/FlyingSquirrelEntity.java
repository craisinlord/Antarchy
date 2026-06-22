package com.craisinlord.antarchy.content.entity.flying_squirrel;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.block.OuranwoodAcornBlock;
import com.craisinlord.antarchy.content.block.OuranwoodLeavesBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.UUID;

public class FlyingSquirrelEntity extends TamableAnimal implements GeoEntity {
    private static final ResourceKey<Level> ELYTHIA_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );

    private static final EntityDataAccessor<Boolean> GLIDING =
            SynchedEntityData.defineId(FlyingSquirrelEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CLIMBING =
            SynchedEntityData.defineId(FlyingSquirrelEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PICKING_UP_NUT =
            SynchedEntityData.defineId(FlyingSquirrelEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT =
            SynchedEntityData.defineId(FlyingSquirrelEntity.class, EntityDataSerializers.INT);

    private static final String PICKUP_ANIM_TICKS_KEY = "PickupAnimTicks";
    private static final String TEXTURE_VARIANT_KEY = "TextureVariant";
    private static final String GLIDE_DIR_X_KEY = "GlideDirX";
    private static final String GLIDE_DIR_Y_KEY = "GlideDirY";
    private static final String GLIDE_DIR_Z_KEY = "GlideDirZ";
    private static final int MAX_GLIDE_TICKS = 160;
    private static final int PICKUP_ANIM_TICKS = 60;
    private static final double MIN_GLIDE_DISTANCE = 10.0D;
    private static final double MAX_GLIDE_DISTANCE = 64.0D;
    private static final int MIN_CLIMB_HEIGHT = 5;
    private static final int MAX_CLIMB_HEIGHT = 18;
    private static final int GROUND_ESCAPE_MIN_CLIMB_HEIGHT = 12;
    private static final double NUT_SEARCH_RANGE = 16.0D;
    private static final float FALL_GLIDE_TRIGGER_DISTANCE = 0.75F;
    private static final double FALL_GLIDE_TRIGGER_SPEED = -0.08D;
    private static final int EMERGENCY_GLIDE_REPEAT_COOLDOWN = 60;
    private static final int RECENT_GLIDE_REPEAT_COOLDOWN = 80;
    private static final double CLIMB_START_DISTANCE_SQR = 4.0D;
    private static final int CLIMB_APPROACH_TIMEOUT_TICKS = 120;
    private static final int CLIMB_NO_PROGRESS_LIMIT = 25;
    private static final int FAILED_CLIMB_TARGET_COOLDOWN = 80;
    private static final int GROUND_STALL_FORCE_WANDER_TICKS = 100;
    private static final int SHOULDER_CLIMB_TICKS = 20;
    private static final int SHOULDER_SNEAK_GRACE_TICKS = 10;
    private static final int SHOULDER_INTERACT_COOLDOWN_TICKS = 8;
    private static final int TEXTURE_VARIANT_REGULAR = 0;
    private static final int TEXTURE_VARIANT_BROWN = 1;
    private static final int TEXTURE_VARIANT_ALBINO = 2;
    private static final String[] TEXTURE_VARIANT_NAMES = {
            "regular",
            "brown",
            "albino"
    };
    private static final double SHOULDER_X_OFFSET = 0.0D;
    private static final double SHOULDER_FORWARD_OFFSET = 0.0475D;
    private static final double SHOULDER_HEAD_Y_OFFSET = 0.26D;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walking");
    private static final RawAnimation CLIMB_ANIM = RawAnimation.begin().thenLoop("climbing");
    private static final RawAnimation GLIDE_ANIM = RawAnimation.begin().thenLoop("gliding");
    private static final RawAnimation PICKUP_ANIM = RawAnimation.begin().thenPlay("item_pickup");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private TemptGoal temptGoal;
    private int wanderCooldown;
    private int glideTicks;
    private int pickupAnimTicks;
    private int glideRecoveryTicks;
    private int glideLoopSoundCooldown;
    @Nullable
    private BlockPos glideTarget;
    @Nullable
    private Vec3 glideDirection;
    @Nullable
    private BlockPos recentEmergencyGlideTarget;
    @Nullable
    private BlockPos currentGlideLaunch;
    @Nullable
    private BlockPos recentGlideLaunch;
    @Nullable
    private BlockPos recentGlideTarget;
    @Nullable
    private String currentGlideReason;
    private int recentEmergencyGlideCooldown;
    private int recentGlideCooldown;
    private int idleStallTicks;
    @Nullable
    private BlockPos recentFailedClimbTarget;
    private int recentFailedClimbCooldown;
    @Nullable
    private UUID shoulderPlayerId;
    @Nullable
    private Vec3 shoulderClimbStartPos;
    private int climbingToShoulderTicks;
    private int shoulderSneakGraceTicks;
    private int shoulderInteractCooldownTicks;
    private int nextFallGlideScanTick;
    @Nullable
    private BlockPos cachedFallGlideTarget;
    private int nextBestGlideScanTick;
    @Nullable
    private BlockPos cachedBestGlideTarget;
    private int nextNutScanTick;
    @Nullable
    private ItemEntity cachedNutItem;
    @Nullable
    private BlockPos cachedHarvestableAcorn;

    public FlyingSquirrelEntity(EntityType<? extends FlyingSquirrelEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.LEAVES, -2.0F);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingSquirrelNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public static boolean canSpawn(EntityType<FlyingSquirrelEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        return level.getLevel().dimension().equals(ELYTHIA_DIMENSION)
                && pos.getY() >= 80
                && !level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above())
                && hasNearbyCanopy(level, pos);
    }

    @Override
    protected void registerGoals() {
        this.temptGoal = new TemptGoal(this, 1.05D, Ingredient.of(AntarchyTags.Items.FLYING_SQUIRREL_NUTS), false);

        this.goalSelector.addGoal(0, new ShoulderMountGoal());
        this.goalSelector.addGoal(1, this.temptGoal);
        this.goalSelector.addGoal(2, new FloatGoal(this));
        this.goalSelector.addGoal(3, new TamableAnimal.TamableAnimalPanicGoal(1.5D));
        this.goalSelector.addGoal(4, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0D, 10.0F, 5.0F));
        this.goalSelector.addGoal(6, new BreedGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new FindNutGoal());
        this.goalSelector.addGoal(8, new ClimbGoal());
        this.goalSelector.addGoal(9, new AvoidEntityGoal<>(this, Player.class, 5.0F, 1.0D, 1.2D) {
            @Override
            public boolean canUse() {
                return !FlyingSquirrelEntity.this.isTame() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !FlyingSquirrelEntity.this.isTame() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(10, new ArborealRandomStrollGoal());
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GLIDING, false);
        builder.define(CLIMBING, false);
        builder.define(PICKING_UP_NUT, false);
        builder.define(TEXTURE_VARIANT, TEXTURE_VARIANT_REGULAR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 5, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<FlyingSquirrelEntity> state) {
        if (this.isPickingUpNut()) {
            return state.setAndContinue(PICKUP_ANIM);
        }

        if (this.isOnShoulder()) {
            return state.setAndContinue(PICKUP_ANIM);
        }

        if (this.isGliding()) {
            state.getController().setAnimationSpeed(1.1D);
            return state.setAndContinue(GLIDE_ANIM);
        }

        if (this.isClimbingToShoulder() || this.isClimbingTrunk()) {
            return state.setAndContinue(CLIMB_ANIM);
        }

        Vec3 motion = this.getDeltaMovement();
        double horizontalSpeed = motion.horizontalDistanceSqr();
        if (!this.isGliding() && !this.isClimbingTrunk() && motion.horizontalDistanceSqr() > 1.0E-4D) {
            float moveYaw = (float)(Mth.atan2(motion.z, motion.x) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(Mth.approachDegrees(this.getYRot(), moveYaw, 10.0F));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }
        if (horizontalSpeed > 0.001D && this.onGround() && !this.isClimbingTrunk()) {
            state.getController().setAnimationSpeed(1.35D);
            return state.setAndContinue(WALK_ANIM);
        }
        state.getController().setAnimationSpeed(1.0D);
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(PICKUP_ANIM_TICKS_KEY, this.pickupAnimTicks);
        tag.putInt(TEXTURE_VARIANT_KEY, this.getTextureVariant());
        if (this.glideDirection != null) {
            tag.putDouble(GLIDE_DIR_X_KEY, this.glideDirection.x);
            tag.putDouble(GLIDE_DIR_Y_KEY, this.glideDirection.y);
            tag.putDouble(GLIDE_DIR_Z_KEY, this.glideDirection.z);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.pickupAnimTicks = Math.max(0, tag.getInt(PICKUP_ANIM_TICKS_KEY));
        this.setPickingUpNut(this.pickupAnimTicks > 0);
        if (tag.contains(TEXTURE_VARIANT_KEY)) {
            this.setTextureVariant(tag.getInt(TEXTURE_VARIANT_KEY));
        } else {
            this.assignRandomTextureVariant();
        }
        this.stopGliding("loaded_from_nbt");
        if (tag.contains(GLIDE_DIR_X_KEY) && tag.contains(GLIDE_DIR_Y_KEY) && tag.contains(GLIDE_DIR_Z_KEY)) {
            this.glideDirection = new Vec3(
                    tag.getDouble(GLIDE_DIR_X_KEY),
                    tag.getDouble(GLIDE_DIR_Y_KEY),
                    tag.getDouble(GLIDE_DIR_Z_KEY)
            );
        } else {
            this.glideDirection = null;
        }
        this.shoulderPlayerId = null;
        this.shoulderClimbStartPos = null;
        this.climbingToShoulderTicks = 0;
        this.shoulderSneakGraceTicks = 0;
        this.shoulderInteractCooldownTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.temptGoal != null && this.temptGoal.isRunning() && !this.isTame() && this.tickCount % 100 == 0) {
            this.playSound(AntarchySoundEvents.FLYING_SQUIRREL_BEG.get(), 1.0F, 1.0F);
        }
        if (this.wanderCooldown > 0) {
            this.wanderCooldown--;
        }
        if (this.glideRecoveryTicks > 0) {
            this.glideRecoveryTicks--;
        }
        if (this.recentEmergencyGlideCooldown > 0) {
            this.recentEmergencyGlideCooldown--;
            if (this.recentEmergencyGlideCooldown == 0) {
                this.recentEmergencyGlideTarget = null;
            }
        }
        if (this.recentGlideCooldown > 0) {
            this.recentGlideCooldown--;
            if (this.recentGlideCooldown == 0) {
                this.recentGlideLaunch = null;
                this.recentGlideTarget = null;
            }
        }
        if (this.recentFailedClimbCooldown > 0) {
            this.recentFailedClimbCooldown--;
            if (this.recentFailedClimbCooldown == 0) {
                this.recentFailedClimbTarget = null;
            }
        }
        if (this.shoulderSneakGraceTicks > 0) {
            this.shoulderSneakGraceTicks--;
        }
        if (this.shoulderInteractCooldownTicks > 0) {
            this.shoulderInteractCooldownTicks--;
        }
        Vec3 motion = this.getDeltaMovement();
        double horizontalSpeed = motion.horizontalDistanceSqr();

        if (!this.isGliding() && !this.isClimbingTrunk() && !this.isClimbingToShoulder() && horizontalSpeed > 1.0E-4D) {
            float moveYaw = (float)(Mth.atan2(motion.z, motion.x) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(Mth.approachDegrees(this.getYRot(), moveYaw, 10.0F));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }

        if (!this.level().isClientSide) {
            this.tickPickupAnimation();

            if (this.isClimbingToShoulder()) {
                Player shoulderPlayer = this.shoulderPlayerId != null
                        ? this.level().getPlayerByUUID(this.shoulderPlayerId)
                        : null;
                if (shoulderPlayer == null || !shoulderPlayer.isAlive()) {
                    this.dismountShoulder();
                } else {
                    this.tickClimbingToShoulder(shoulderPlayer);
                }
                this.setNoGravity(true);
                return;
            }

            if (this.isOnShoulder()) {
                if (!(this.getVehicle() instanceof Player shoulderPlayer)
                        || !shoulderPlayer.isAlive()) {
                    this.dismountShoulder();
                } else if (this.shoulderSneakGraceTicks <= 0 && shoulderPlayer.isShiftKeyDown()) {
                    this.glideOffShoulder();
                } else {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.fallDistance = 0.0F;
                }
                this.setNoGravity(true);
                return;
            }

            if (!this.isGliding()
                    && !this.onGround()
                    && !this.isInWaterOrBubble()
                    && !this.isClimbingTrunk()
                    && this.glideRecoveryTicks <= 0
                    && this.fallDistance > FALL_GLIDE_TRIGGER_DISTANCE
                    && this.getDeltaMovement().y < FALL_GLIDE_TRIGGER_SPEED) {
                if (this.tickCount >= this.nextFallGlideScanTick) {
                    this.nextFallGlideScanTick = this.tickCount + 10;
                    this.cachedFallGlideTarget = this.findFallGlideTarget();
                }
                BlockPos rescueTarget = this.cachedFallGlideTarget;
                if (rescueTarget == null) {
                    if (this.tickCount >= this.nextBestGlideScanTick) {
                        this.nextBestGlideScanTick = this.tickCount + 10;
                        this.cachedBestGlideTarget = this.findBestGlideTarget();
                    }
                    rescueTarget = this.cachedBestGlideTarget;
                }
                if (rescueTarget == null) {
                    rescueTarget = this.findForcedFallLanding();
                }
                if (rescueTarget != null && this.isRecentEmergencyGlideTarget(rescueTarget)) {
                    rescueTarget = null;
                }
                if (rescueTarget != null) {
                    this.cachedFallGlideTarget = null;
                    this.cachedBestGlideTarget = null;
                    this.startGliding(rescueTarget, "emergency_fall_glide");
                }
            }

            if (this.isGliding()) {
                this.tickGliding();
                this.fallDistance = 0.0F;
            } else {
                this.glideTicks = 0;
                this.glideTarget = null;
                if (this.isClimbingTrunk()) {
                    this.fallDistance = 0.0F;
                }
            }

            this.tickIdleStallLogging(horizontalSpeed);
        }
        this.setNoGravity(this.isGliding() || this.isClimbingTrunk() || this.isClimbingToShoulder() || this.isOnShoulder());
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof Player player) {
            Vec3 shoulderPos = this.getShoulderRidePos(player);
            this.setPos(shoulderPos.x, shoulderPos.y, shoulderPos.z);
            this.setDeltaMovement(Vec3.ZERO);
            this.setYRot(player.getYHeadRot());
            this.setXRot(player.getXRot());
            this.yBodyRot = player.getYHeadRot();
            this.yHeadRot = player.getYHeadRot();
            this.fallDistance = 0.0F;
        }
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbingTrunk();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isClimbingTrunk() && source.is(DamageTypes.IN_WALL)) {
            return false;
        }

        if ((this.isOnShoulder() || this.isClimbingToShoulder()) && !this.level().isClientSide) {
            this.dismountShoulder();
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isTame() && this.isOwnedBy(player) && stack.isEmpty()) {
            if (!this.level().isClientSide) {
                if (player.isShiftKeyDown()) {
                    this.handleOwnerShoulderInteract(player);
                } else {
                    this.toggleFollowState(player);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.isFood(stack)) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide) {
            boolean canUseFood = !this.isTame() || this.isBaby() || this.canFallInLove() || this.getHealth() < this.getMaxHealth();
            return canUseFood ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }

        if (!this.isTame()) {
            this.usePlayerItem(player, hand, stack);
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.wanderCooldown = 0;
                this.setOrderedToSit(false);
                this.cancelTravel();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.CONSUME;
        }

        if (this.isBaby()) {
            this.usePlayerItem(player, hand, stack);
            this.ageUp((int) ((float) (-this.getAge() / 20) * 0.1F), true);
            this.playSound(AntarchySoundEvents.FLYING_SQUIRREL_NUT.get(), 0.35F, 1.0F + this.random.nextFloat() * 0.2F);
            return InteractionResult.CONSUME;
        }

        if (this.canFallInLove()) {
            this.usePlayerItem(player, hand, stack);
            this.setInLove(player);
            this.playSound(AntarchySoundEvents.FLYING_SQUIRREL_NUT.get(), 0.35F, 1.0F + this.random.nextFloat() * 0.2F);
            return InteractionResult.CONSUME;
        }

        if (this.getHealth() < this.getMaxHealth()) {
            this.usePlayerItem(player, hand, stack);
            this.heal(2.0F);
            this.playSound(AntarchySoundEvents.FLYING_SQUIRREL_NUT.get(), 0.35F, 1.0F + this.random.nextFloat() * 0.2F);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public boolean handleOwnerShoulderInteract(Player player) {
        if (!this.isTame() || !this.isOwnedBy(player)) {
            return false;
        }

        if (this.isOnShoulder()) {
            if (!this.level().isClientSide) {
                this.glideOffShoulder();
            }
            player.displayClientMessage(Component.literal("Flying squirrel glided off your shoulder."), true);
            return true;
        }

        if (this.isClimbingToShoulder()) {
            this.dismountShoulder();
            player.displayClientMessage(Component.literal("Flying squirrel left your shoulder."), true);
            return true;
        }

        if (this.shoulderInteractCooldownTicks > 0) {
            return true;
        }

        this.startShoulderMountSequence(player);
        player.displayClientMessage(Component.literal("Flying squirrel is climbing onto your shoulder."), true);
        return true;
    }

    private void toggleFollowState(Player player) {
        boolean shouldSit = !this.isOrderedToSit();
        this.setOrderedToSit(shouldSit);
        this.setTarget(null);
        this.cancelTravel();
        if (shouldSit) {
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            this.wanderCooldown = 0;
        }
        player.displayClientMessage(Component.literal(shouldSit ? "Flying squirrel set to sit." : "Flying squirrel set to follow."), true);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(AntarchyTags.Items.FLYING_SQUIRREL_NUTS);
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return otherAnimal instanceof FlyingSquirrelEntity
                && otherAnimal != this
                && this.isTame()
                && ((FlyingSquirrelEntity) otherAnimal).isTame()
                && this.isInLove()
                && otherAnimal.isInLove();
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        if (!(this.getType().create(level) instanceof FlyingSquirrelEntity flyingSquirrel)) {
            return null;
        }
        flyingSquirrel.setTextureVariant(this.getTextureVariant());
        return flyingSquirrel;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData finalized = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.assignRandomTextureVariant();
        this.stopGliding("spawn_initialized");
        this.wanderCooldown = 40 + this.random.nextInt(60);
        this.glideRecoveryTicks = 0;
        this.setClimbingTrunk(false);
        this.setPickingUpNut(false);
        this.pickupAnimTicks = 0;
        return finalized;
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.FLYING_SQUIRREL_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.FLYING_SQUIRREL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.FLYING_SQUIRREL_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.35F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        SoundType soundType = state.getSoundType();
        this.playSound(soundType.getStepSound(), 0.12F, 1.15F);
    }

    public boolean isGliding() {
        return this.entityData.get(GLIDING);
    }

    public boolean isClimbingTrunk() {
        return this.entityData.get(CLIMBING);
    }

    public boolean isPickingUpNut() {
        return this.entityData.get(PICKING_UP_NUT);
    }

    public String getTextureVariantName() {
        int variant = this.getTextureVariant();
        return TEXTURE_VARIANT_NAMES[Mth.clamp(variant, 0, TEXTURE_VARIANT_NAMES.length - 1)];
    }

    public boolean shouldMirrorIdleAnimation() {
        return false;
    }

    public int getWanderCooldown() {
        return this.wanderCooldown;
    }

    public void setWanderCooldown(int wanderCooldown) {
        this.wanderCooldown = wanderCooldown;
    }

    public void setGliding(boolean gliding) {
        this.entityData.set(GLIDING, gliding);
    }

    public void setClimbingTrunk(boolean climbing) {
        this.entityData.set(CLIMBING, climbing);
    }

    public void setPickingUpNut(boolean pickingUpNut) {
        this.entityData.set(PICKING_UP_NUT, pickingUpNut);
    }

    private void assignRandomTextureVariant() {
        double roll = this.random.nextDouble();
        if (roll < 0.005D) {
            this.setTextureVariant(TEXTURE_VARIANT_ALBINO);
        } else if (roll < 0.205D) {
            this.setTextureVariant(TEXTURE_VARIANT_BROWN);
        } else {
            this.setTextureVariant(TEXTURE_VARIANT_REGULAR);
        }
    }

    private int getTextureVariant() {
        return this.entityData.get(TEXTURE_VARIANT);
    }

    private void setTextureVariant(int variant) {
        this.entityData.set(TEXTURE_VARIANT, Mth.clamp(variant, 0, TEXTURE_VARIANT_NAMES.length - 1));
    }

    public boolean isOnShoulder() {
        return this.getVehicle() instanceof Player;
    }

    public boolean isClimbingToShoulder() {
        return this.climbingToShoulderTicks > 0 && this.shoulderPlayerId != null;
    }

    private void startShoulderMountSequence(Player player) {
        this.shoulderPlayerId = player.getUUID();
        this.shoulderClimbStartPos = this.position();
        this.climbingToShoulderTicks = SHOULDER_CLIMB_TICKS;
        this.shoulderSneakGraceTicks = SHOULDER_SNEAK_GRACE_TICKS;
        this.setOrderedToSit(false);
        this.setClimbingTrunk(false);
        this.stopGliding("start_shoulder_mount");
        this.setNoGravity(true);
        this.cancelTravel();
        this.setTarget(null);
        this.stopRiding();
    }

    private void dismountShoulder() {
        Entity vehicle = this.getVehicle();
        if (vehicle != null) {
            this.stopRiding();
            Vec3 dismountPos = vehicle.position().add(0.0D, 0.1D, 0.0D);
            this.setPos(dismountPos.x, dismountPos.y, dismountPos.z);
        }
        this.clearShoulderMountState();
        this.setNoGravity(false);
        this.wanderCooldown = 20 + this.random.nextInt(30);
    }

    private void clearShoulderMountState() {
        this.shoulderPlayerId = null;
        this.shoulderClimbStartPos = null;
        this.climbingToShoulderTicks = 0;
        this.shoulderSneakGraceTicks = 0;
        this.shoulderInteractCooldownTicks = SHOULDER_INTERACT_COOLDOWN_TICKS;
        this.setClimbingTrunk(false);
    }

    private void glideOffShoulder() {
        Entity vehicle = this.getVehicle();
        if (!(vehicle instanceof Player player)) {
            this.dismountShoulder();
            return;
        }

        Vec3 launchPos = this.getShoulderRidePos(player);
        this.stopRiding();
        this.setPos(launchPos.x, launchPos.y, launchPos.z);
        this.clearShoulderMountState();
        this.fallDistance = 0.0F;

        BlockPos glideTarget = this.findFallGlideTarget();
        if (glideTarget == null) {
            glideTarget = this.findBestGlideTarget();
        }
        if (glideTarget == null) {
            glideTarget = this.findForcedFallLanding();
        }

        if (glideTarget != null) {
            this.startGliding(glideTarget, "shoulder_dismount_glide");
            return;
        }

        this.setNoGravity(false);
        this.wanderCooldown = 20 + this.random.nextInt(30);
    }

    private void tickClimbingToShoulder(Player player) {
        if (this.shoulderClimbStartPos == null) {
            this.shoulderClimbStartPos = this.position();
        }

        this.cancelTravel();
        this.setTarget(null);
        this.setClimbingTrunk(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.fallDistance = 0.0F;

        int ticksRemaining = this.climbingToShoulderTicks - 1;
        double progress = 1.0D - (double)Math.max(ticksRemaining, 0) / (double)SHOULDER_CLIMB_TICKS;
        Vec3 midClimbPos = this.getShoulderClimbMidpoint(player);
        Vec3 targetPos = this.getShoulderRidePos(player);
        Vec3 nextPos;
        if (progress < 0.65D) {
            nextPos = this.shoulderClimbStartPos.lerp(midClimbPos, progress / 0.65D);
        } else {
            nextPos = midClimbPos.lerp(targetPos, (progress - 0.65D) / 0.35D);
        }
        this.setPos(nextPos.x, nextPos.y, nextPos.z);
        float climbYaw = player.getYHeadRot() + 180.0F;
        this.setYRot(climbYaw);
        this.setXRot(player.getXRot());
        this.yBodyRot = climbYaw;
        this.yHeadRot = climbYaw;
        this.climbingToShoulderTicks = ticksRemaining;

        if (this.climbingToShoulderTicks <= 0) {
            this.finishShoulderMount(player);
        }
    }

    private void finishShoulderMount(Player player) {
        this.climbingToShoulderTicks = 0;
        this.shoulderClimbStartPos = null;
        this.shoulderPlayerId = null;
        this.shoulderSneakGraceTicks = SHOULDER_SNEAK_GRACE_TICKS;
        this.setClimbingTrunk(false);
        this.startRiding(player, true);
        this.setDeltaMovement(Vec3.ZERO);
        this.fallDistance = 0.0F;
    }

    private Vec3 getShoulderRidePos(Player player) {
        float headYaw = player.getYHeadRot();
        Vec3 horizontalForward = Vec3.directionFromRotation(0.0F, headYaw).normalize();
        Vec3 headForward = Vec3.directionFromRotation(player.getXRot(), headYaw).normalize();
        Vec3 shoulderSide = new Vec3(-horizontalForward.z, 0.0D, horizontalForward.x).normalize();
        Vec3 headUp = shoulderSide.cross(headForward).normalize();
        Vec3 headBase = player.position().add(0.0D, player.getEyeHeight(), 0.0D);

        return headBase
                .add(shoulderSide.scale(SHOULDER_X_OFFSET))
                .add(headForward.scale(SHOULDER_FORWARD_OFFSET))
                .add(headUp.scale(SHOULDER_HEAD_Y_OFFSET));
    }

    private Vec3 getShoulderClimbMidpoint(Player player) {
        float headYaw = player.getYHeadRot();
        Vec3 forward = Vec3.directionFromRotation(0.0F, headYaw).normalize();
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x).normalize();
        Vec3 front = forward.scale(0.08D);
        Vec3 lateral = side.scale(SHOULDER_X_OFFSET);
        return player.position()
                .add(front)
                .add(lateral)
                .add(0.0D, player.getBbHeight() * 0.32D, 0.0D);
    }

    private void startPickupAnimation() {
        this.setPickingUpNut(true);
        this.pickupAnimTicks = PICKUP_ANIM_TICKS;
        this.cancelTravel();
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void tickPickupAnimation() {
        if (this.pickupAnimTicks <= 0) {
            return;
        }

        this.pickupAnimTicks--;
        this.cancelTravel();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.pickupAnimTicks <= 0) {
            this.pickupAnimTicks = 0;
            this.setPickingUpNut(false);
        }
    }

    private void tickGlideLoopSound() {
        if (this.level().isClientSide || !this.isGliding()) {
            return;
        }

        if (this.glideLoopSoundCooldown > 0) {
            this.glideLoopSoundCooldown--;
            return;
        }

        this.playSound(AntarchySoundEvents.FLYING_SQUIRREL_GLIDE_LOOP.get(), 0.2F, 0.92F + this.random.nextFloat() * 0.10F);
        this.glideLoopSoundCooldown = 8 + this.random.nextInt(6);
    }

    void startGliding(BlockPos targetStandPos, String reason) {
        this.currentGlideLaunch = this.blockPosition().immutable();
        this.glideTarget = targetStandPos.immutable();
        this.currentGlideReason = reason;
        this.glideTicks = 0;
        this.glideLoopSoundCooldown = 0;
        this.setGliding(true);
        this.setClimbingTrunk(false);
        this.cancelTravel();
        this.setNoGravity(true);

        Vec3 launch = Vec3.atBottomCenterOf(targetStandPos).subtract(this.position());
        Vec3 horizontal = new Vec3(launch.x, 0.0D, launch.z);

        if (horizontal.lengthSqr() > 1.0E-4D) {
            this.glideDirection = horizontal.normalize();
        } else {
            float yawRad = this.getYRot() * ((float)Math.PI / 180.0F);
            this.glideDirection = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad)).normalize();
        }

        this.setDeltaMovement(
                this.glideDirection.x * 0.38D,
                -0.06D,
                this.glideDirection.z * 0.38D
        );

        float targetYaw = (float)(Mth.atan2(this.glideDirection.z, this.glideDirection.x) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(targetYaw);
        this.yBodyRot = targetYaw;
        this.yHeadRot = targetYaw;

        this.hasImpulse = true;
    }

    void stopGliding(String reason) {
        if (this.isGliding()) {
            if (this.currentGlideLaunch != null && this.glideTarget != null) {
                this.recentGlideLaunch = this.currentGlideLaunch.immutable();
                this.recentGlideTarget = this.glideTarget.immutable();
                this.recentGlideCooldown = RECENT_GLIDE_REPEAT_COOLDOWN;
            }
            if ("emergency_fall_glide".equals(this.currentGlideReason) && this.glideTarget != null) {
                this.recentEmergencyGlideTarget = this.glideTarget.immutable();
                this.recentEmergencyGlideCooldown = EMERGENCY_GLIDE_REPEAT_COOLDOWN;
            }
        }
        this.setGliding(false);
        this.glideTarget = null;
        this.glideTicks = 0;
        this.glideDirection = null;
        this.currentGlideLaunch = null;
        this.currentGlideReason = null;
        this.glideLoopSoundCooldown = 0;
        this.glideRecoveryTicks = 16;
    }

    private void tickGliding() {
        this.tickGlideLoopSound();

        if (this.glideTarget == null || this.isInWaterOrBubble()) {
            this.stopGliding(
                    "hit_ground_or_collision"
                            + ", onGround=" + this.onGround()
                            + ", inWater=" + this.isInWaterOrBubble()
            );
            return;
        }

        if (this.onGround() && this.glideTicks > 1) {
            this.stopGliding("hit_ground_or_collision, onGround=true, inWater=false");
            return;
        }

        if (this.glideDirection == null) {
            this.stopGliding("missing_glide_direction");
            return;
        }

        if (++this.glideTicks > MAX_GLIDE_TICKS) {
            this.stopGliding("glide_timeout");
            return;
        }

        Vec3 targetCenter = Vec3.atBottomCenterOf(this.glideTarget).add(0.0D, 0.08D, 0.0D);
        Vec3 toTarget = targetCenter.subtract(this.position());

        double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        if (horizontalDistance < 0.85D && Math.abs(toTarget.y) <= 1.0D) {
            this.stopGliding("reached_glide_target");
            return;
        }

        Vec3 targetDirection = new Vec3(toTarget.x, 0.0D, toTarget.z);
        if (targetDirection.lengthSqr() > 1.0E-4D) {
            targetDirection = targetDirection.normalize();
            this.glideDirection = this.glideDirection.scale(0.92D)
                    .add(targetDirection.scale(0.08D))
                    .normalize();
        }

        Vec3 motion = this.getDeltaMovement();

        double horizontalSpeed = Mth.clamp(horizontalDistance * 0.05D, 0.30D, 0.48D);
        double verticalSpeed = Mth.clamp(toTarget.y * 0.04D, -0.20D, -0.02D);

        this.setDeltaMovement(
                this.glideDirection.x * horizontalSpeed,
                verticalSpeed,
                this.glideDirection.z * horizontalSpeed
        );

        this.hasImpulse = true;

        float targetYaw = (float)(Mth.atan2(this.glideDirection.z, this.glideDirection.x) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(Mth.approachDegrees(this.getYRot(), targetYaw, 4.0F));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    boolean requestTravelTo(BlockPos targetPos, double speed, String purpose) {
        if (this.getNavigation() instanceof FlyingSquirrelNavigation squirrelNavigation) {
            return squirrelNavigation.requestTravelTo(targetPos, speed, purpose);
        }

        double x = targetPos.getX() + 0.5D;
        double y = targetPos.getY();
        double z = targetPos.getZ() + 0.5D;
        boolean moved = this.getNavigation().moveTo(x, y, z, speed);
        this.getNavigation().setSpeedModifier(speed);
        return moved;
    }

    private void cancelTravel() {
        if (this.getNavigation() instanceof FlyingSquirrelNavigation squirrelNavigation) {
            squirrelNavigation.cancelTraversal();
            return;
        }

        this.getNavigation().stop();
    }

    private boolean isRecentEmergencyGlideTarget(BlockPos targetStandPos) {
        return this.recentEmergencyGlideTarget != null
                && this.recentEmergencyGlideCooldown > 0
                && this.recentEmergencyGlideTarget.closerThan(targetStandPos, 4.0D);
    }

    boolean isRecentGlidePath(Vec3 launchFrom, BlockPos targetStandPos) {
        if (this.recentGlideCooldown <= 0 || this.recentGlideLaunch == null || this.recentGlideTarget == null) {
            return false;
        }

        BlockPos launchPos = BlockPos.containing(launchFrom);
        return this.recentGlideLaunch.closerThan(launchPos, 4.0D)
                && this.recentGlideTarget.closerThan(targetStandPos, 4.0D);
    }

    boolean isAtStandPos(BlockPos standPos) {
        return Vec3.atBottomCenterOf(standPos).distanceToSqr(this.position()) <= 1.45D;
    }

    @Nullable BlockPos findClosestHarvestableAcorn() {
        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -6, -16), origin.offset(16, 8, 16))) {
            BlockState state = this.level().getBlockState(pos);
            if (!isHarvestableAcorn(state)) {
                continue;
            }

            double distance = origin.distSqr(pos);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPos = pos.immutable();
            }
        }

        return bestPos;
    }

    @Nullable ItemEntity findClosestNutItem() {
        ItemEntity bestMatch = null;
        double bestDistance = Double.MAX_VALUE;
        for (ItemEntity itemEntity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(NUT_SEARCH_RANGE))) {
            if (!itemEntity.isAlive() || !itemEntity.getItem().is(AntarchyTags.Items.FLYING_SQUIRREL_NUTS)) {
                continue;
            }

            double distance = this.distanceToSqr(itemEntity);
            if (distance > NUT_SEARCH_RANGE * NUT_SEARCH_RANGE || distance >= bestDistance) {
                continue;
            }

            bestDistance = distance;
            bestMatch = itemEntity;
        }

        return bestMatch;
    }

    @Nullable BlockPos findAcornHarvestStandPos(BlockPos acornPos) {
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos standPos = acornPos.offset(dx, dy, dz);
                    if (!isOpenStandPos(this.level(), standPos)) {
                        continue;
                    }

                    double distanceToAcorn = Vec3.atBottomCenterOf(standPos).distanceToSqr(Vec3.atCenterOf(acornPos));
                    if (distanceToAcorn > 9.0D) {
                        continue;
                    }

                    double score = distanceToAcorn;
                    if (isAdjacentToLog(this.level(), standPos)) {
                        score -= 1.0D;
                    }
                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = standPos.immutable();
                    }
                }
            }
        }

        return bestPos;
    }

    public void collectNut(ItemEntity itemEntity) {
        this.startPickupAnimation();

        ItemStack particleStack = itemEntity.getItem().copy();
        particleStack.setCount(1);
        ItemStack itemStack = itemEntity.getItem();
        itemStack.shrink(1);
        if (itemStack.isEmpty()) {
            itemEntity.discard();
        }
        this.wanderCooldown = 30 + this.random.nextInt(80);
        this.playSound(AntarchySoundEvents.FLYING_SQUIRREL_NUT.get(), 0.35F, 0.95F + this.random.nextFloat() * 0.2F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                    this.getX(),
                    this.getEyeY() - 0.15D,
                    this.getZ(),
                    6,
                    0.18D,
                    0.08D,
                    0.18D,
                    0.01D
            );
        }
    }

    boolean harvestAcorn(BlockPos acornPos) {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }

        BlockState state = this.level().getBlockState(acornPos);
        if (!isHarvestableAcorn(state)) {
            return false;
        }

        SoundType soundType = state.getSoundType();
        boolean destroyed = this.level().destroyBlock(acornPos, true, this);
        if (destroyed) {
            this.wanderCooldown = 10 + this.random.nextInt(15);
            this.playSound(soundType.getBreakSound(), 0.25F, 1.1F);
        }
        return destroyed;
    }

    @Nullable
    private BlockPos findFallGlideTarget() {
        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        Vec3 motion = this.getDeltaMovement();
        Vec3 preferredDirection = new Vec3(motion.x, 0.0D, motion.z);
        if (preferredDirection.lengthSqr() <= 1.0E-4D) {
            float yawRad = this.getYRot() * ((float)Math.PI / 180.0F);
            preferredDirection = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        }
        if (preferredDirection.lengthSqr() > 1.0E-4D) {
            preferredDirection = preferredDirection.normalize();
        }

        for (int dx = -20; dx <= 20; dx++) {
            for (int dz = -20; dz <= 20; dz++) {
                for (int dy = -28; dy <= -2; dy++) {
                    BlockPos supportPos = origin.offset(dx, dy, dz);
                    BlockState supportState = this.level().getBlockState(supportPos);
                    if (!isStandableSurface(supportState)) {
                        continue;
                    }

                    BlockPos standPos = supportPos.above();
                    if (!isEmergencyLandingPos(this.level(), standPos)) {
                        continue;
                    }
                    if (this.isRecentGlidePath(this.position(), standPos)) {
                        continue;
                    }

                    Vec3 standCenter = Vec3.atBottomCenterOf(standPos);
                    double horizontalDistance = Math.sqrt((standCenter.x - this.getX()) * (standCenter.x - this.getX())
                            + (standCenter.z - this.getZ()) * (standCenter.z - this.getZ()));
                    if (horizontalDistance < 3.0D || horizontalDistance > 28.0D) {
                        continue;
                    }

                    double verticalDrop = origin.getY() - standPos.getY();
                    double score = horizontalDistance * 1.6D;
                    score += Math.min(verticalDrop, 18.0D) * 0.8D;
                    if (preferredDirection.lengthSqr() > 1.0E-4D) {
                        Vec3 toTarget = new Vec3(standCenter.x - this.getX(), 0.0D, standCenter.z - this.getZ());
                        if (toTarget.lengthSqr() > 1.0E-4D) {
                            double alignment = preferredDirection.dot(toTarget.normalize());
                            score += alignment * 10.0D;
                        }
                    }
                    if (isAdjacentToLog(this.level(), standPos)) {
                        score += 4.0D;
                    }
                    if (supportState.getBlock() instanceof OuranwoodLeavesBlock) {
                        score += 6.0D;
                    } else if (supportState.blocksMotion()) {
                        score += 2.0D;
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        bestPos = standPos.immutable();
                    }
                }
            }
        }

        if (bestPos == null) {
            bestPos = this.findFallbackFallLanding(preferredDirection);
        }

        return bestPos;
    }

    @Nullable
    BlockPos findBestGlideTarget() {
        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int dx = -24; dx <= 24; dx++) {
            for (int dz = -24; dz <= 24; dz++) {
                for (int dy = -32; dy <= -1; dy++) {
                    BlockPos supportPos = origin.offset(dx, dy, dz);
                    if (!isStandableSurface(this.level().getBlockState(supportPos))) {
                        continue;
                    }

                    BlockPos standPos = supportPos.above();
                    if (!isOpenStandPos(this.level(), standPos)) {
                        continue;
                    }
                    if (this.isRecentGlidePath(this.position(), standPos)) {
                        continue;
                    }

                    double score = this.scoreGlideTarget(standPos);
                    if (score > bestScore) {
                        bestScore = score;
                        bestPos = standPos.immutable();
                    }
                }
            }
        }

        return bestPos;
    }

    @Nullable
    private BlockPos findFallbackFallLanding(Vec3 preferredDirection) {
        if (preferredDirection.lengthSqr() <= 1.0E-4D) {
            return null;
        }

        BlockPos origin = this.blockPosition();
        int minY = Math.max(this.level().getMinBuildHeight() + 1, origin.getY() - 40);

        for (int step = 4; step <= 18; step += 2) {
            int x = Mth.floor(this.getX() + preferredDirection.x * step);
            int z = Mth.floor(this.getZ() + preferredDirection.z * step);

            for (int y = origin.getY() - 2; y >= minY; y--) {
                BlockPos supportPos = new BlockPos(x, y, z);
                if (!isStandableSurface(this.level().getBlockState(supportPos))) {
                    continue;
                }

                BlockPos standPos = supportPos.above();
                if (isEmergencyLandingPos(this.level(), standPos)) {
                    return standPos;
                }
            }
        }

        return null;
    }

    @Nullable
    private BlockPos findForcedFallLanding() {
        Vec3 preferredDirection = this.getPreferredGlideDirection();
        BlockPos origin = this.blockPosition();
        int minY = Math.max(this.level().getMinBuildHeight() + 1, origin.getY() - 96);

        for (int forward = 4; forward <= 20; forward += 2) {
            double sampleX = this.getX() + preferredDirection.x * forward;
            double sampleZ = this.getZ() + preferredDirection.z * forward;

            BlockPos landing = this.findLandingInColumn(Mth.floor(sampleX), Mth.floor(sampleZ), origin.getY() - 1, minY);
            if (landing != null && !this.isRecentGlidePath(this.position(), landing)) {
                return landing;
            }

            Vec3 side = new Vec3(-preferredDirection.z, 0.0D, preferredDirection.x);
            for (int lateral = 2; lateral <= 4; lateral += 2) {
                BlockPos leftLanding = this.findLandingInColumn(
                        Mth.floor(sampleX + side.x * lateral),
                        Mth.floor(sampleZ + side.z * lateral),
                        origin.getY() - 1,
                        minY
                );
                if (leftLanding != null && !this.isRecentGlidePath(this.position(), leftLanding)) {
                    return leftLanding;
                }

                BlockPos rightLanding = this.findLandingInColumn(
                        Mth.floor(sampleX - side.x * lateral),
                        Mth.floor(sampleZ - side.z * lateral),
                        origin.getY() - 1,
                        minY
                );
                if (rightLanding != null && !this.isRecentGlidePath(this.position(), rightLanding)) {
                    return rightLanding;
                }
            }
        }

        BlockPos directLanding = this.findLandingInColumn(origin.getX(), origin.getZ(), origin.getY() - 1, minY);
        return directLanding != null && !this.isRecentGlidePath(this.position(), directLanding) ? directLanding : null;
    }

    @Nullable
    private BlockPos findLandingInColumn(int x, int z, int startY, int minY) {
        for (int y = startY; y >= minY; y--) {
            BlockPos supportPos = new BlockPos(x, y, z);
            if (!isStandableSurface(this.level().getBlockState(supportPos))) {
                continue;
            }

            BlockPos standPos = supportPos.above();
            if (isEmergencyLandingPos(this.level(), standPos)) {
                return standPos.immutable();
            }
        }

        return null;
    }

    static boolean hasNearbyCanopy(BlockGetter level, BlockPos pos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (isCanopySupport(level.getBlockState(pos.offset(dx, dy, dz)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isCanopySupport(BlockState state) {
        return state.getBlock() instanceof OuranwoodLeavesBlock || state.is(net.minecraft.tags.BlockTags.LOGS);
    }

    private static boolean isClimbableTrunk(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.LOGS);
    }

    static boolean isStandableSurface(BlockState state) {
        return state.blocksMotion() || isCanopySupport(state);
    }

    static boolean isClimbSurface(BlockState state) {
        return state.blocksMotion() || state.is(BlockTags.LEAVES);
    }

    static boolean isClimbPassable(BlockState state) {
        return state.isAir() || state.is(net.minecraft.tags.BlockTags.LEAVES);
    }

    static boolean isOpenStandPos(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && isStandableSurface(level.getBlockState(pos.below()));
    }

    private static boolean isEmergencyLandingPos(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && isStandableSurface(level.getBlockState(pos.below()));
    }

    static boolean isOpenAir(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir();
    }

    static boolean isOpenClimbPos(BlockGetter level, BlockPos pos) {
        return isClimbPassable(level.getBlockState(pos))
                && isClimbPassable(level.getBlockState(pos.above()));
    }

    static boolean isAdjacentToLog(BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(direction)).is(net.minecraft.tags.BlockTags.LOGS)
                    || level.getBlockState(pos.above().relative(direction)).is(net.minecraft.tags.BlockTags.LOGS)) {
                return true;
            }
        }
        return false;
    }

    static boolean isHarvestableAcorn(BlockState state) {
        return state.getBlock() instanceof OuranwoodAcornBlock
                && state.hasProperty(OuranwoodAcornBlock.HANGING)
                && state.getValue(OuranwoodAcornBlock.HANGING);
    }

    static boolean isGroundEscapeSurface(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.MOSS_CARPET);
    }

    private Vec3 getPreferredGlideDirection() {
        Vec3 motion = this.getDeltaMovement();
        Vec3 preferredDirection = new Vec3(motion.x, 0.0D, motion.z);
        if (preferredDirection.lengthSqr() > 1.0E-4D) {
            return preferredDirection.normalize();
        }

        float yawRad = this.getYRot() * ((float)Math.PI / 180.0F);
        return new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad)).normalize();
    }

    private double getClimbFaceOffset() {
        return Mth.clamp(0.5D - this.getBbWidth() * 0.5D + 0.04D, 0.16D, 0.30D);
    }

    private void tickIdleStallLogging(double horizontalSpeed) {
        if (this.isOrderedToSit() || this.isGliding() || this.isClimbingTrunk() || this.isClimbingToShoulder() || this.isPickingUpNut() || this.isOnShoulder()) {
            this.idleStallTicks = 0;
            return;
        }

        boolean mostlyStill = horizontalSpeed < 1.0E-4D && Math.abs(this.getDeltaMovement().y) < 0.02D;
        if (!this.onGround() || !mostlyStill || !this.getNavigation().isDone()) {
            this.idleStallTicks = 0;
            return;
        }

        this.idleStallTicks++;

        if (this.idleStallTicks == GROUND_STALL_FORCE_WANDER_TICKS) {
            this.forceGroundStallWander();
        }
    }

    private void forceGroundStallWander() {
        BlockPos origin = this.blockPosition();
        for (int attempt = 0; attempt < 16; attempt++) {
            int dx = this.random.nextIntBetweenInclusive(-8, 8);
            int dz = this.random.nextIntBetweenInclusive(-8, 8);
            for (int dy = 2; dy >= -2; dy--) {
                BlockPos candidate = origin.offset(dx, dy, dz);
                if (isOpenStandPos(this.level(), candidate)) {
                    this.wanderCooldown = 0;
                    this.requestTravelTo(candidate, 1.0D, "stall_recovery");
                    this.idleStallTicks = 0;
                    return;
                }
            }
        }
        BlockPos glideTarget = this.findBestGlideTarget();
        if (glideTarget == null) {
            glideTarget = this.findFallGlideTarget();
        }
        if (glideTarget != null && !this.isRecentEmergencyGlideTarget(glideTarget)) {
            this.startGliding(glideTarget, "stall_recovery_glide");
            this.idleStallTicks = 0;
            return;
        }
        BlockPos edge = this.findNearestEdge();
        if (edge != null) {
            this.setDeltaMovement(
                    (edge.getX() + 0.5D - this.getX()) * 0.15D,
                    0.12D,
                    (edge.getZ() + 0.5D - this.getZ()) * 0.15D
            );
            this.hasImpulse = true;
            this.idleStallTicks = 0;
            return;
        }
        this.idleStallTicks = GROUND_STALL_FORCE_WANDER_TICKS - 40;
    }

    @Nullable
    private BlockPos findNearestEdge() {
        BlockPos origin = this.blockPosition();
        for (int r = 1; r <= 5; r++) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = origin.relative(dir, r);
                if (this.level().getBlockState(candidate).isAir()
                        && this.level().getBlockState(candidate.below()).isAir()) {
                    return candidate.immutable();
                }
            }
        }
        return null;
    }

    private void refreshNutCache() {
        if (this.tickCount < this.nextNutScanTick) {
            return;
        }
        this.nextNutScanTick = this.tickCount + 30;
        this.cachedNutItem = this.findClosestNutItem();
        this.cachedHarvestableAcorn = this.cachedNutItem == null ? this.findClosestHarvestableAcorn() : null;
    }

    double scoreGlideTarget(BlockPos standPos) {
        return this.scoreGlideTargetFrom(this.position(), standPos);
    }

    double scoreGlideTargetFrom(Vec3 from, BlockPos standPos) {
        Vec3 to = Vec3.atBottomCenterOf(standPos);

        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double verticalDrop = from.y - to.y;

        if (horizontalDistance < MIN_GLIDE_DISTANCE || horizontalDistance > MAX_GLIDE_DISTANCE) {
            return Double.NEGATIVE_INFINITY;
        }

        if (verticalDrop <= MIN_GLIDE_DISTANCE || verticalDrop > 32.0D) {
            return Double.NEGATIVE_INFINITY;
        }

        return horizontalDistance * 1.5D + verticalDrop * 2.0D;
    }

    private class FindNutGoal extends Goal {
        @Nullable
        private ItemEntity targetNut;
        @Nullable
        private BlockPos targetAcorn;
        @Nullable
        private BlockPos acornStandPos;
        private int repathDelay;
        private int searchCooldown;

        private FindNutGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }

            if (FlyingSquirrelEntity.this.isGliding()
                    || FlyingSquirrelEntity.this.isClimbingTrunk()
                    || FlyingSquirrelEntity.this.isPickingUpNut()
                    || FlyingSquirrelEntity.this.isOrderedToSit()) {
                this.searchCooldown = 10;
                return false;
            }

            FlyingSquirrelEntity.this.refreshNutCache();
            this.targetNut = FlyingSquirrelEntity.this.cachedNutItem;
            this.targetAcorn = null;
            this.acornStandPos = null;

            if (this.targetNut != null) {
                return true;
            }

            if (!FlyingSquirrelEntity.this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                this.searchCooldown = 20;
                return false;
            }

            this.targetAcorn = FlyingSquirrelEntity.this.cachedHarvestableAcorn;
            this.acornStandPos = this.targetAcorn == null ? null : FlyingSquirrelEntity.this.findAcornHarvestStandPos(this.targetAcorn);

            if (this.targetAcorn != null && this.acornStandPos != null) {
                return true;
            }

            this.searchCooldown = 20;
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (FlyingSquirrelEntity.this.isGliding()
                    || FlyingSquirrelEntity.this.isClimbingTrunk()
                    || FlyingSquirrelEntity.this.isOrderedToSit()) {
                return false;
            }

            if (this.targetNut != null) {
                return this.targetNut.isAlive();
            }

            return this.targetAcorn != null
                    && this.acornStandPos != null
                    && FlyingSquirrelEntity.isHarvestableAcorn(FlyingSquirrelEntity.this.level().getBlockState(this.targetAcorn));
        }

        @Override
        public void start() {
            this.repathDelay = 0;
            this.searchCooldown = 0;
        }

        @Override
        public void stop() {
            this.targetNut = null;
            this.targetAcorn = null;
            this.acornStandPos = null;
            this.repathDelay = 0;
            this.searchCooldown = 10;
            if (!FlyingSquirrelEntity.this.isGliding()) {
                FlyingSquirrelEntity.this.cancelTravel();
            }
        }

        @Override
        public void tick() {
            if (this.targetNut != null) {
                FlyingSquirrelEntity.this.getLookControl().setLookAt(this.targetNut, 30.0F, 30.0F);

                if (--this.repathDelay <= 0 && !FlyingSquirrelEntity.this.isGliding()) {
                    boolean moved = FlyingSquirrelEntity.this.requestTravelTo(this.targetNut.blockPosition(), 1.15D, "forage_item");
                    this.repathDelay = moved ? 10 : 0;
                }

                if (FlyingSquirrelEntity.this.distanceToSqr(this.targetNut) <= 2.1D) {
                    FlyingSquirrelEntity.this.collectNut(this.targetNut);
                    this.targetNut = null;
                }
                return;
            }

            if (this.targetAcorn == null || this.acornStandPos == null) {
                return;
            }

            FlyingSquirrelEntity.this.getLookControl().setLookAt(
                    this.targetAcorn.getX() + 0.5D,
                    this.targetAcorn.getY() + 0.5D,
                    this.targetAcorn.getZ() + 0.5D
            );

            if (FlyingSquirrelEntity.this.position().distanceTo(Vec3.atCenterOf(this.targetAcorn)) <= 2.2D) {
                if (FlyingSquirrelEntity.this.harvestAcorn(this.targetAcorn)) {
                    this.targetAcorn = null;
                    this.acornStandPos = null;
                }
                return;
            }

            if (--this.repathDelay <= 0 && !FlyingSquirrelEntity.this.isGliding()) {
                boolean moved = FlyingSquirrelEntity.this.requestTravelTo(this.acornStandPos, 1.1D, "forage_acorn");
                this.repathDelay = moved ? 10 : 0;
            }
        }
    }

    private class ShoulderMountGoal extends Goal {
        private ShoulderMountGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return FlyingSquirrelEntity.this.isClimbingToShoulder();
        }

        @Override
        public boolean canContinueToUse() {
            return FlyingSquirrelEntity.this.isClimbingToShoulder();
        }

        @Override
        public void start() {
            FlyingSquirrelEntity.this.cancelTravel();
            FlyingSquirrelEntity.this.setTarget(null);
        }

        @Override
        public void stop() {
            if (FlyingSquirrelEntity.this.isClimbingToShoulder()) {
                FlyingSquirrelEntity.this.dismountShoulder();
            }
        }

        @Override
        public void tick() {
            FlyingSquirrelEntity.this.cancelTravel();
            FlyingSquirrelEntity.this.setTarget(null);
        }
    }

    private double getClimbBias() {
        if (!this.onGround()) {
            return 0.0D;
        }

        BlockState groundState = this.level().getBlockState(this.blockPosition().below());
        if (!isGroundEscapeSurface(groundState)) {
            return 0.0D;
        }

        if (this.getY() >= 200.0D) {
            return 0.0D;
        }

        double yBias = Mth.clampedMap(this.getY(), 80.0D, 180.0D, 1.0D, 0.0D);
        return Math.min(1.0D, yBias + 0.20D);
    }

    private boolean shouldFavorClimbingFromGround() {
        return this.getClimbBias() > 0.08D;
    }

    private void rememberFailedClimbTarget(BlockPos topStandPos) {
        this.recentFailedClimbTarget = topStandPos.immutable();
        this.recentFailedClimbCooldown = FAILED_CLIMB_TARGET_COOLDOWN;
    }

    @Nullable
    private ClimbTarget findBestClimbTarget() {
        BlockPos origin = this.blockPosition();
        boolean favorClimbing = this.shouldFavorClimbingFromGround();
        int horizontalRange = favorClimbing ? 24 : 10;
        ClimbTarget bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        int currentY = Mth.floor(this.getY());
        int minimumTargetY = currentY + (favorClimbing ? Math.max(MIN_CLIMB_HEIGHT, GROUND_ESCAPE_MIN_CLIMB_HEIGHT) : MIN_CLIMB_HEIGHT);
        int maximumTargetY = currentY + MAX_CLIMB_HEIGHT;

        for (BlockPos baseStandPos : BlockPos.betweenClosed(origin.offset(-horizontalRange, -1, -horizontalRange), origin.offset(horizontalRange, 1, horizontalRange))) {
            if (!isOpenStandPos(this.level(), baseStandPos)) {
                continue;
            }

            Direction attachDirection = this.findClimbAttachmentDirection(baseStandPos, null);
            if (attachDirection == null || !this.isValidClimbColumnPos(baseStandPos, attachDirection)) {
                continue;
            }

            int reachableTopY = this.findHighestReachableClimbY(baseStandPos, maximumTargetY, attachDirection);
            if (reachableTopY < minimumTargetY) {
                continue;
            }

            BlockPos topStandPos = this.findBestClimbPerch(baseStandPos, minimumTargetY, reachableTopY + 2, attachDirection);
            if (topStandPos == null) {
                continue;
            }

            if (this.recentFailedClimbTarget != null
                    && this.recentFailedClimbCooldown > 0
                    && this.recentFailedClimbTarget.closerThan(topStandPos, 4.0D)) {
                continue;
            }

            int climbHeight = topStandPos.getY() - currentY;
            if (climbHeight < MIN_CLIMB_HEIGHT || climbHeight > MAX_CLIMB_HEIGHT) {
                continue;
            }

            double walkDistance = Vec3.atBottomCenterOf(baseStandPos).distanceTo(this.position());
            double perchOffset = Vec3.atBottomCenterOf(topStandPos).distanceTo(Vec3.atBottomCenterOf(baseStandPos));
            double score = climbHeight * (favorClimbing ? 10.0D : 4.5D) - walkDistance - perchOffset * 0.6D;
            if (favorClimbing) {
                score += 20.0D;
            }
            if (score > bestScore) {
                bestScore = score;
                bestTarget = new ClimbTarget(
                        baseStandPos.immutable(),
                        topStandPos.immutable(),
                        attachDirection
                );
            }
        }

        return bestTarget;
    }

    private int findHighestReachableClimbY(BlockPos baseStandPos, int maxTargetY, Direction preferredDirection) {
        int highestOpenY = baseStandPos.getY();
        for (int y = baseStandPos.getY(); y <= maxTargetY; y++) {
            BlockPos columnPos = new BlockPos(baseStandPos.getX(), y, baseStandPos.getZ());
            if (!this.isValidClimbColumnPos(columnPos, preferredDirection)) {
                break;
            }
            highestOpenY = y;
        }

        return highestOpenY;
    }

    private boolean hasSideClimbSupportAt(BlockPos pos) {
        return this.findClimbAttachmentDirection(pos, null) != null;
    }

    private boolean hasClimbSupportAt(BlockPos pos) {
        return this.hasSideClimbSupportAt(pos) || isStandableSurface(this.level().getBlockState(pos.above()));
    }

    private boolean isValidClimbColumnPos(BlockPos pos, @Nullable Direction preferredDirection) {
        return isOpenClimbPos(this.level(), pos) && this.findClimbAttachmentDirection(pos, preferredDirection) != null;
    }

    private Vec3 getClimbAttachPoint(BlockPos climbColumnPos, @Nullable Direction preferredDirection) {
        Direction attachDirection = this.findClimbAttachmentDirection(climbColumnPos, preferredDirection);
        double x = climbColumnPos.getX() + 0.5D;
        double y = climbColumnPos.getY();
        double z = climbColumnPos.getZ() + 0.5D;
        if (attachDirection == null) {
            return new Vec3(x, y, z);
        }
        double faceOffset = this.getClimbFaceOffset();
        switch (attachDirection) {
            case NORTH -> z -= faceOffset;
            case SOUTH -> z += faceOffset;
            case WEST -> x -= faceOffset;
            case EAST -> x += faceOffset;
        }
        return new Vec3(x, y, z);
    }

    @Nullable
    private Direction findClimbAttachmentDirection(BlockPos pos, @Nullable Direction preferredDirection) {
        if (preferredDirection != null
                && preferredDirection.getAxis().isHorizontal()
                && this.isValidClimbSupport(pos.relative(preferredDirection))) {
            return preferredDirection;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState supportState = this.level().getBlockState(pos.relative(direction));
            if (supportState.is(BlockTags.LOGS)) {
                return direction;
            }
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState supportState = this.level().getBlockState(pos.relative(direction));
            if (supportState.blocksMotion()) {
                return direction;
            }
        }
        return null;
    }

    private boolean isValidClimbSupport(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        return state.is(BlockTags.LOGS)
                || (state.blocksMotion() && !state.is(BlockTags.LEAVES))
                || state.is(BlockTags.LEAVES);
    }

    @Nullable
    private BlockPos findBestClimbPerch(BlockPos baseStandPos, int minimumY, int maximumY, Direction preferredDirection) {
        BlockPos bestStandPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int y = maximumY; y >= minimumY; y--) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > 3) {
                        continue;
                    }

                    BlockPos standPos = new BlockPos(baseStandPos.getX() + dx, y, baseStandPos.getZ() + dz);
                    if (!isOpenStandPos(this.level(), standPos)) {
                        continue;
                    }

                    BlockPos climbColumnPos = new BlockPos(baseStandPos.getX(), y, baseStandPos.getZ());
                    if (!this.isValidClimbColumnPos(climbColumnPos, preferredDirection)) {
                        continue;
                    }

                    if (!this.hasSideClimbSupportAt(standPos) && !isStandableSurface(this.level().getBlockState(standPos.above()))) {
                        continue;
                    }

                    double horizontalOffset = Math.sqrt(dx * dx + dz * dz);
                    double score = y * 4.0D - horizontalOffset * 1.5D;
                    if (isAdjacentToLog(this.level(), standPos)) {
                        score += 2.0D;
                    }
                    if (score > bestScore) {
                        bestScore = score;
                        bestStandPos = standPos.immutable();
                    }
                }
            }
        }

        return bestStandPos;
    }

    private class ArborealRandomStrollGoal extends WaterAvoidingRandomStrollGoal {
        private static final int INTERVAL = 80;

        private ArborealRandomStrollGoal() {
            super(FlyingSquirrelEntity.this, 1.0D, INTERVAL);
        }

        @Override
        public boolean canUse() {
            if (FlyingSquirrelEntity.this.isOrderedToSit()) {
                return false;
            }

            if (!FlyingSquirrelEntity.this.onGround()
                    && !FlyingSquirrelEntity.this.isClimbingTrunk()
                    && !FlyingSquirrelEntity.this.isAtStandPos(FlyingSquirrelEntity.this.blockPosition())
                    && !FlyingSquirrelEntity.hasNearbyCanopy(
                    FlyingSquirrelEntity.this.level(),
                    FlyingSquirrelEntity.this.blockPosition())) {
                return false;
            }
            if (FlyingSquirrelEntity.this.isGliding()) {
                return false;
            }

            if (FlyingSquirrelEntity.this.isClimbingTrunk()) {
                return false;
            }

            if (FlyingSquirrelEntity.this.isPickingUpNut()) {
                return false;
            }

            if (FlyingSquirrelEntity.this.getWanderCooldown() > 0) {
                return false;
            }

            return super.canUse();
        }

        @Override
        @Nullable
        protected Vec3 getPosition() {
            BlockPos target = this.findBestArborealDestination();
            if (target == null) {
                return null;
            }
            return Vec3.atBottomCenterOf(target);
        }

        @Override
        public void start() {
            BlockPos target = BlockPos.containing(this.wantedX, this.wantedY, this.wantedZ);
            FlyingSquirrelEntity.this.requestTravelTo(target, this.speedModifier, "arboreal_wander");
        }

        @Override
        public void stop() {
            super.stop();

            if (!FlyingSquirrelEntity.this.isGliding()) {
                FlyingSquirrelEntity.this.setWanderCooldown(
                        25 + FlyingSquirrelEntity.this.getRandom().nextInt(35)
                );
            }
        }

        @Nullable
        private BlockPos findBestArborealDestination() {
            BlockPos origin = FlyingSquirrelEntity.this.blockPosition();
            double heightBias = this.getHeightBias(origin);

            int horizontalRange = Mth.floor(Mth.lerp(heightBias, 24.0D, 30.0D));
            int verticalDown = Mth.floor(Mth.lerp(heightBias, 8.0D, 4.0D));
            int verticalUp = Mth.floor(Mth.lerp(heightBias, 30.0D, 40.0D));

            BlockPos bestPos = null;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (BlockPos candidate : BlockPos.betweenClosed(
                    origin.offset(-horizontalRange, -verticalDown, -horizontalRange),
                    origin.offset(horizontalRange, verticalUp, horizontalRange))) {

                if (!FlyingSquirrelEntity.isOpenStandPos(FlyingSquirrelEntity.this.level(), candidate)) {
                    continue;
                }

                if (FlyingSquirrelEntity.this.isAtStandPos(candidate)) {
                    continue;
                }

                boolean nearCanopy = FlyingSquirrelEntity.hasNearbyCanopy(
                        FlyingSquirrelEntity.this.level(), candidate
                );
                boolean nearLog = FlyingSquirrelEntity.isAdjacentToLog(
                        FlyingSquirrelEntity.this.level(), candidate
                );

                if (!nearCanopy && !nearLog) {
                    continue;
                }

                double dx = candidate.getX() + 0.5D - FlyingSquirrelEntity.this.getX();
                double dy = candidate.getY() - FlyingSquirrelEntity.this.getY();
                double dz = candidate.getZ() + 0.5D - FlyingSquirrelEntity.this.getZ();
                double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

                if (horizontalDistance < 6.0D) {
                    continue;
                }

                double minPreferredRise = Mth.lerp(heightBias, 0.0D, 8.0D);
                if (heightBias > 0.15D && dy < minPreferredRise) {
                    continue;
                }

                double score = 0.0D;

                if (nearCanopy) score += 10.0D;
                if (nearLog) score += 8.0D;

                score += Math.max(dy, 0.0D) * Mth.lerp(heightBias, 3.0D, 5.5D);
                score += Math.min(horizontalDistance, 24.0D) * Mth.lerp(heightBias, 1.1D, 0.9D);

                if (dy <= -4.0D && horizontalDistance >= 10.0D) {
                    score += 18.0D * (1.0D - heightBias);
                }

                if (dy >= 12.0D) {
                    score += Mth.lerp(heightBias, 10.0D, 18.0D);
                }
                if (horizontalDistance >= 12.0D) {
                    score += 12.0D;
                }
                if (horizontalDistance >= 18.0D) {
                    score += 10.0D;
                }

                if (dy < -2.0D) {
                    score += dy * Mth.lerp(heightBias, 0.75D, 0.15D);
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestPos = candidate.immutable();
                }
            }

            return bestPos;
        }

        private double getHeightBias(BlockPos origin) {
            double yBias = Mth.clampedMap(origin.getY(), 80.0D, 170.0D, 1.0D, 0.0D);
            if (origin.getY() >= 190) {
                yBias = 0.0D;
            }

            if (FlyingSquirrelEntity.isGroundEscapeSurface(
                    FlyingSquirrelEntity.this.level().getBlockState(origin.below()))) {
                yBias = Math.min(1.0D, yBias + 0.15D);
            }

            return yBias;
        }
    }

    private static final class ClimbTarget {
        private final BlockPos baseStandPos;
        private final BlockPos topStandPos;
        @Nullable
        private final Direction preferredAttachDirection;

        private ClimbTarget(BlockPos baseStandPos, BlockPos topStandPos, @Nullable Direction preferredAttachDirection) {
            this.baseStandPos = baseStandPos;
            this.topStandPos = topStandPos;
            this.preferredAttachDirection = preferredAttachDirection;
        }
    }

    private class ClimbGoal extends Goal {
        @Nullable
        private ClimbTarget target;
        @Nullable
        private Direction activeAttachDirection;
        private int repathDelay;
        private int climbTicks;
        private int approachTicks;
        private int searchCooldown;
        private int noProgressTicks;
        private double lastDistanceToBase;
        private boolean climbing;
        private int attachDirectionRefreshTick;

        private ClimbGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            boolean favorClimbing = FlyingSquirrelEntity.this.shouldFavorClimbingFromGround();

            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }

            if (FlyingSquirrelEntity.this.isOrderedToSit()
                    || FlyingSquirrelEntity.this.isGliding()
                    || FlyingSquirrelEntity.this.isPickingUpNut()
                    || !FlyingSquirrelEntity.this.onGround()
                    || (!favorClimbing && FlyingSquirrelEntity.this.getWanderCooldown() > 30)) {
                return false;
            }

            this.target = FlyingSquirrelEntity.this.findBestClimbTarget();
            if (this.target != null) {
                return true;
            }

            this.searchCooldown = favorClimbing ? 10 : 20;
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.target == null
                    || FlyingSquirrelEntity.this.isGliding()
                    || FlyingSquirrelEntity.this.isOrderedToSit()) {
                return false;
            }

            if (this.climbing) {
                return this.climbTicks < 200;
            }

            return !FlyingSquirrelEntity.this.isAtStandPos(this.target.topStandPos);
        }

        @Override
        public void start() {
            this.repathDelay = 0;
            this.climbTicks = 0;
            this.approachTicks = 0;
            this.searchCooldown = 0;
            this.noProgressTicks = 0;
            this.lastDistanceToBase = Double.MAX_VALUE;
            this.climbing = false;
            this.activeAttachDirection = null;
        }

        @Override
        public void stop() {
            if (this.climbing) {
                FlyingSquirrelEntity.this.setClimbingTrunk(false);
                FlyingSquirrelEntity.this.setNoGravity(false);
            }

            this.target = null;
            this.repathDelay = 0;
            this.climbTicks = 0;
            this.approachTicks = 0;
            this.noProgressTicks = 0;
            this.lastDistanceToBase = Double.MAX_VALUE;
            this.climbing = false;
            this.activeAttachDirection = null;
            this.searchCooldown = 10;

            if (!FlyingSquirrelEntity.this.isGliding()) {
                FlyingSquirrelEntity.this.getNavigation().stop();
            }
        }

        @Override
        public void tick() {
            if (this.target == null) {
                return;
            }

            if (!this.climbing) {
                FlyingSquirrelEntity.this.getLookControl().setLookAt(
                        this.target.topStandPos.getX() + 0.5D,
                        this.target.topStandPos.getY(),
                        this.target.topStandPos.getZ() + 0.5D,
                        20.0F,
                        20.0F
                );

                this.approachTicks++;

                double distanceToBase = Vec3.atBottomCenterOf(this.target.baseStandPos)
                        .distanceTo(FlyingSquirrelEntity.this.position());

                boolean closeEnoughToStart = FlyingSquirrelEntity.this.isAtStandPos(this.target.baseStandPos)
                        || FlyingSquirrelEntity.this.position().distanceToSqr(Vec3.atBottomCenterOf(this.target.baseStandPos)) <= CLIMB_START_DISTANCE_SQR;

                if (closeEnoughToStart) {
                    this.climbing = true;
                    this.climbTicks = 0;
                    this.activeAttachDirection = this.target.preferredAttachDirection;
                    FlyingSquirrelEntity.this.setClimbingTrunk(true);
                    return;
                }

                if (distanceToBase + 0.15D < this.lastDistanceToBase) {
                    this.noProgressTicks = 0;
                    this.lastDistanceToBase = distanceToBase;
                } else if (++this.noProgressTicks > CLIMB_NO_PROGRESS_LIMIT
                        || this.approachTicks > CLIMB_APPROACH_TIMEOUT_TICKS) {
                    FlyingSquirrelEntity.this.rememberFailedClimbTarget(this.target.topStandPos);
                    this.target = null;
                    return;
                }

                if (--this.repathDelay <= 0) {
                    this.repathDelay = 10;
                    FlyingSquirrelEntity.this.requestTravelTo(
                            this.target.baseStandPos,
                            1.0D,
                            "climb_approach"
                    );
                }

                return;
            }

            if (++this.climbTicks > 200) {
                FlyingSquirrelEntity.this.rememberFailedClimbTarget(this.target.topStandPos);
                FlyingSquirrelEntity.this.setClimbingTrunk(false);
                FlyingSquirrelEntity.this.setNoGravity(false);
                this.target = null;
                this.climbing = false;
                return;
            }

            FlyingSquirrelEntity.this.setClimbingTrunk(true);
            FlyingSquirrelEntity.this.setNoGravity(true);
            FlyingSquirrelEntity.this.fallDistance = 0.0F;

            double nextY = Math.min(
                    FlyingSquirrelEntity.this.getY() + 0.24D,
                    this.target.topStandPos.getY()
            );

            BlockPos climbColumnPos = new BlockPos(
                    this.target.baseStandPos.getX(),
                    Mth.floor(nextY),
                    this.target.baseStandPos.getZ()
            );

            if (!FlyingSquirrelEntity.this.isValidClimbColumnPos(
                    climbColumnPos,
                    this.target.preferredAttachDirection
            )) {
                FlyingSquirrelEntity.this.rememberFailedClimbTarget(this.target.topStandPos);
                FlyingSquirrelEntity.this.setClimbingTrunk(false);
                FlyingSquirrelEntity.this.setNoGravity(false);
                this.target = null;
                this.climbing = false;
                return;
            }

            Vec3 attachPoint = FlyingSquirrelEntity.this.getClimbAttachPoint(
                    climbColumnPos,
                    this.activeAttachDirection
            );

            double nextX = Mth.lerp(0.45D, FlyingSquirrelEntity.this.getX(), attachPoint.x);
            double nextZ = Mth.lerp(0.45D, FlyingSquirrelEntity.this.getZ(), attachPoint.z);

            FlyingSquirrelEntity.this.setPos(nextX, nextY, nextZ);
            FlyingSquirrelEntity.this.setDeltaMovement(Vec3.ZERO);
            FlyingSquirrelEntity.this.hasImpulse = true;

            if (this.climbTicks >= this.attachDirectionRefreshTick) {
                this.attachDirectionRefreshTick = this.climbTicks + 5;
                Direction newDir = FlyingSquirrelEntity.this.findClimbAttachmentDirection(
                        climbColumnPos,
                        this.activeAttachDirection
                );
                if (newDir != null) {
                    this.activeAttachDirection = newDir;
                }
            }
            Direction attachDirection = this.activeAttachDirection;
            if (attachDirection != null) {
                float targetYaw = attachDirection.toYRot();
                FlyingSquirrelEntity.this.setYRot(targetYaw);
                FlyingSquirrelEntity.this.yRotO = targetYaw;
                FlyingSquirrelEntity.this.yBodyRot = targetYaw;
                FlyingSquirrelEntity.this.yBodyRotO = targetYaw;
                FlyingSquirrelEntity.this.yHeadRot = targetYaw;
                FlyingSquirrelEntity.this.yHeadRotO = targetYaw;
            }

            if (FlyingSquirrelEntity.this.getY() >= this.target.topStandPos.getY() - 0.15D) {
                FlyingSquirrelEntity.this.setPos(
                        this.target.topStandPos.getX() + 0.5D,
                        this.target.topStandPos.getY(),
                        this.target.topStandPos.getZ() + 0.5D
                );
                FlyingSquirrelEntity.this.setDeltaMovement(Vec3.ZERO);
                FlyingSquirrelEntity.this.setClimbingTrunk(false);
                FlyingSquirrelEntity.this.setNoGravity(false);

                this.target = null;
                this.climbing = false;
                FlyingSquirrelEntity.this.setWanderCooldown(
                        20 + FlyingSquirrelEntity.this.getRandom().nextInt(25)
                );
            }
        }
    }
}
