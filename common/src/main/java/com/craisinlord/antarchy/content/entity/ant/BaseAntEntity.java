package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
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

public abstract class BaseAntEntity extends Animal implements GeoEntity {
    private static final double ATTRIBUTE_EPSILON = 1.0E-4D;
    private static final double MAX_NEST_WANDER_DISTANCE_SQR = 576.0D;
    private static final EntityDataAccessor<ItemStack> CARRIED_FOOD = SynchedEntityData.defineId(BaseAntEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DANCING = SynchedEntityData.defineId(BaseAntEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BITE_ANIM_TICKS = SynchedEntityData.defineId(BaseAntEntity.class, EntityDataSerializers.INT);
    private static final String HAS_FOOD = "HasFood";
    private static final String CARRIED_FOOD_TAG = "CarriedFood";
    private static final String OUTSIDE_NEST_TICKS_KEY = "OutsideNestTicks";
    private static final String NEST_X = "NestX";
    private static final String NEST_Y = "NestY";
    private static final String NEST_Z = "NestZ";
    private static final String TELEPORT_ACTIVATED = "TeleportActivated";
    private static final String MARCH_LEADER_UUID = "MarchLeaderUuid";
    private static final String MARCH_ORDER_KEY = "MarchOrder";
    private static final int MIN_OUTSIDE_NEST_TICKS = 20 * 30;
    private static final int EXTRA_OUTSIDE_NEST_TICKS = 20 * 30;
    private static final double MARCH_SEARCH_RADIUS = 12.0D;
    private static final double MARCH_LEADER_LOST_DISTANCE_SQR = 400.0D;
    private static final int MARCH_FOLLOW_REPATH_TICKS = 6;
    private static final int ATTRIBUTE_SYNC_INTERVAL_TICKS = 200;
    private static final int NEST_SEARCH_RADIUS_HORIZONTAL = 16;
    private static final int NEST_SEARCH_RADIUS_VERTICAL = 4;
    private static final int NEST_SEARCH_INTERVAL_TICKS = 80;
    private static final int FOOD_NEST_SEARCH_INTERVAL_TICKS = 20;
    private static final int CHEST_SEARCH_INTERVAL_TICKS = 20;
    private static final int SHARED_CHEST_MEMORY_TICKS = 120;
    private static final double FOOD_CHEST_SEARCH_RADIUS = 20.0D;
    private static final int GROUND_FOOD_CACHE_INTERVAL_TICKS = 20;
    private static final int NEST_CACHE_INTERVAL_TICKS = 20;
    private static final int STUCK_WARN_TICKS = 60;
    private static final int STUCK_REPEAT_TICKS = 100;
    private static final int RETURN_NAV_FAILURE_STREAK = 20;
    private static final int RETURN_NAV_FAILURE_COOLDOWN = 200;
    private static final int JUKEBOX_SCAN_INTERVAL_TICKS = 40;
    private static final int CHEST_REPATH_TICKS = 12;
    private static final double GROUND_FOOD_MOVE_SPEED = 1.12D;
    private static final double CHEST_MOVE_SPEED = 1.02D;
    private static final double SHARED_CHEST_MOVE_SPEED = 1.04D;
    private static final double SHARED_GROUND_FOOD_MOVE_SPEED = 1.08D;
    private static final double MARCH_FOLLOW_SPEED = 0.98D;
    private static final double MARCH_FOLLOW_CATCHUP_SPEED = 1.14D;
    private static final double STROLL_SPEED = 1.0D;
    private static final double ESCAPE_MOVE_SPEED = 1.18D;
    private static final double NEST_RETURN_MOVE_SPEED = 1.02D;
    private static final double FOOD_RETURN_MOVE_SPEED = 1.08D;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenLoop("attack");
    private static final RawAnimation DANCE_ANIM = RawAnimation.begin().thenLoop("dance");


    @Nullable
    private BlockPos nestPos;
    @Nullable
    private BlockPos jukeboxPos;
    private int outsideNestTicks;
    private boolean teleportActivated;
    @Nullable
    private UUID marchLeaderUuid;
    private int marchOrder = -1;
    private int nextMarchFollowRepathTick;
    private int nextNestSearchTick;
    @Nullable
    private BlockPos targetFoodChestPos;
    private int nextFoodChestSearchTick;
    @Nullable
    private BlockPos recentFoodChestPos;
    private int recentFoodChestExpireTick;
    @Nullable
    private BlockPos recentGroundFoodPos;
    private int recentGroundFoodExpireTick;
    @Nullable
    private ItemEntity cachedNearestGroundFood;
    private int nextGroundFoodCacheTick;
    private int stuckDetectorTicks;
    private int returnToNestFailureCooldownTick;
    private boolean cachedHasUsableNest;
    private boolean nestCacheDirty = true;
    private int nextNestCacheTick;
    private int nextJukeboxScanTick;
    private double cachedDanceRadius = 1.0D;
    private int nextIdleSoundTick;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected BaseAntEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CARRIED_FOOD, ItemStack.EMPTY);
        builder.define(DANCING, false);
        builder.define(BITE_ANIM_TICKS, 0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 2, this::mainAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AntDanceGoal());
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, Ingredient.of(this.breedingFoodsTag()), false));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new AntSharedMarchForageGoal());
        this.goalSelector.addGoal(7, new AntMarchFollowGoal());
        this.goalSelector.addGoal(8, new AntReturnToNestGoal());
        this.goalSelector.addGoal(9, new AntForageGoal());
        this.goalSelector.addGoal(10, new AntFoodEscapeGoal());
        this.goalSelector.addGoal(11, new AntRandomStrollGoal(STROLL_SPEED));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isAlive() || this.isRemoved()) {
            return;
        }

        this.tickJukeboxDanceState();

        if (!this.level().isClientSide && (this.tickCount <= 1 || this.tickCount % ATTRIBUTE_SYNC_INTERVAL_TICKS == 0)) {
            this.syncConfiguredAttributes();
            if (this.level() instanceof ServerLevel serverLevel) {
                AntMarchManager.get(serverLevel).cleanup();
            }
        }

        if (this.level().isClientSide) {
            return;
        }

        if (this.getBiteAnimationTicks() > 0) {
            this.setBiteAnimationTicks(this.getBiteAnimationTicks() - 1);
        }

        if (this.isDancing()) {
            this.setTarget(null);
            this.leaveMarch();
            this.getNavigation().stop();
            return;
        }

        if (this.shouldSearchForNest()) {
            this.findNearestNestIfNeeded();
        }

        if (this.outsideNestTicks > 0) {
            this.outsideNestTicks--;
        }

        if (this.getTarget() != null) {
            this.leaveMarch();
            return;
        }

        if (!this.canMarch()) {
            this.leaveMarch();
        } else if (this.tickCount % 20 == 0) {
            this.refreshMarch();
        }

        if (this.tryEnterNest()) {
            return;
        }

        if (this.handlePriorityForaging()) {
            return;
        }

        this.tickLeaderStuckDetector();
        this.tickIdleSound();
    }

    @Override
    public void setRecordPlayingNearby(BlockPos songPosition, boolean isPlaying) {
        this.jukeboxPos = songPosition == null ? null : songPosition.immutable();
        this.setDancing(isPlaying && this.jukeboxPos != null);
        this.nextJukeboxScanTick = 0;
    }

    
    private void tickLeaderStuckDetector() {
        if (this.level().isClientSide || !this.isMarchLeader()) {
            return;
        }

        boolean anyMoveGoalRunning = false;
        for (var wrapped : this.goalSelector.getAvailableGoals()) {
            if (wrapped.isRunning() && wrapped.getGoal().getFlags().contains(Goal.Flag.MOVE)) {
                anyMoveGoalRunning = true;
                break;
            }
        }

        if (!anyMoveGoalRunning && this.getNavigation().isDone()) {
            this.stuckDetectorTicks++;
        } else {
            this.stuckDetectorTicks = 0;
        }
    }

    private static void logDebug(String message, Object... args) {
    }

    private static void logWarn(String message, Object... args) {
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide && this.isInMarch() && this.level() instanceof ServerLevel serverLevel) {
            AntMarchManager.get(serverLevel).unregister(this.marchLeaderUuid, this.marchOrder);
        }
        super.remove(reason);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.isFood(itemStack)) {
            return super.mobInteract(player, hand);
        }

        return AntTeleportHelper.handleInteraction(this, player, itemStack);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isDancing() && (source == this.damageSources().inWall() || source == this.damageSources().cramming())) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean attacked = super.doHurtTarget(target);
        if (attacked) {
            if (this instanceof RedAntEntity) {
                this.setBiteAnimationTicks(8);
            }
            this.playSound(AntarchySoundEvents.ANT_BITE.get(), 0.35F, 0.95F + this.random.nextFloat() * 0.1F);
        }
        return attacked;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(HAS_FOOD, this.isCarryingFood());
        if (this.isCarryingFood()) {
            tag.put(CARRIED_FOOD_TAG, this.getCarriedFood().save(this.registryAccess()));
        }
        tag.putInt(OUTSIDE_NEST_TICKS_KEY, this.outsideNestTicks);
        tag.putBoolean(TELEPORT_ACTIVATED, this.teleportActivated);
        if (this.marchLeaderUuid != null) {
            tag.putUUID(MARCH_LEADER_UUID, this.marchLeaderUuid);
            tag.putInt(MARCH_ORDER_KEY, this.marchOrder);
        }
        if (this.nestPos != null) {
            tag.putInt(NEST_X, this.nestPos.getX());
            tag.putInt(NEST_Y, this.nestPos.getY());
            tag.putInt(NEST_Z, this.nestPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(CARRIED_FOOD_TAG)) {
            this.setCarriedFood(ItemStack.parseOptional(this.registryAccess(), tag.getCompound(CARRIED_FOOD_TAG)));
        } else if (tag.getBoolean(HAS_FOOD)) {
            this.setCarriedFood(new ItemStack(Items.APPLE));
        } else {
            this.clearCarriedFood();
        }
        this.outsideNestTicks = tag.getInt(OUTSIDE_NEST_TICKS_KEY);
        this.teleportActivated = tag.getBoolean(TELEPORT_ACTIVATED);
        if (tag.hasUUID(MARCH_LEADER_UUID)) {
            this.marchLeaderUuid = tag.getUUID(MARCH_LEADER_UUID);
            this.marchOrder = Math.max(0, tag.getInt(MARCH_ORDER_KEY));
        } else {
            this.marchLeaderUuid = null;
            this.marchOrder = -1;
        }
        if (tag.contains(NEST_X) && tag.contains(NEST_Y) && tag.contains(NEST_Z)) {
            this.nestPos = new BlockPos(tag.getInt(NEST_X), tag.getInt(NEST_Y), tag.getInt(NEST_Z));
        } else {
            this.nestPos = null;
        }
        this.nextMarchFollowRepathTick = 0;
        this.nextJukeboxScanTick = 0;
        this.clearFoodChestTarget();
        this.clearRecentFoodChest();
        this.clearRecentGroundFood();
        this.nextNestSearchTick = 0;
        this.markNestCacheDirty();
        this.invalidateGroundFoodCache();
    }

    public void setNestPos(BlockPos nestPos) {
        this.nestPos = nestPos;
        this.markNestCacheDirty();
    }

    public void onEnterNest() {
        this.clearCarriedFood();
        this.outsideNestTicks = 0;
        this.clearFoodChestTarget();
        this.clearRecentFoodChest();
        this.clearRecentGroundFood();
        this.leaveMarch();
        this.markNestCacheDirty();
        this.playSound(AntarchySoundEvents.ANT_NEST.get(), 0.3F, 1.0F + this.getRandom().nextFloat() * 0.1F);
    }

    public void onExitNest() {
        this.outsideNestTicks = MIN_OUTSIDE_NEST_TICKS + this.random.nextInt(EXTRA_OUTSIDE_NEST_TICKS);
        this.clearFoodChestTarget();
        this.clearRecentFoodChest();
        this.clearRecentGroundFood();
        this.leaveMarch();
        this.markNestCacheDirty();
        this.playSound(AntarchySoundEvents.ANT_NEST.get(), 0.3F, 1.0F + this.getRandom().nextFloat() * 0.1F);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        BaseAntEntity child = this.getType().create(level) instanceof BaseAntEntity antChild ? antChild : null;
        if (child != null) {
            child.setNestPos(this.nestPos);
        }
        return child;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (this.isCarryingFood()) {
            ItemStack droppedFood = this.getCarriedFood().copy();
            this.clearCarriedFood();
            ItemEntity itemEntity = this.spawnAtLocation(droppedFood, 0.1F);
            if (itemEntity != null) {
                itemEntity.setPickUpDelay(40);
            }
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(this.breedingFoodsTag());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.ANT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.ANT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.ANT_HURT.get();
    }

    @Override
    public void playAmbientSound() {
        SoundEvent ambientSound = this.getAmbientSound();
        if (ambientSound != null) {
            this.playSound(ambientSound, 0.1F, this.getVoicePitch());
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    protected abstract ResourceKey<Level> destinationDimension();
    protected abstract TagKey<Item> activationItemsTag();
    protected abstract TagKey<Item> breedingFoodsTag();
    protected abstract String activationMessageKey();
    protected abstract String needsReagentMessageKey();
    protected abstract double configuredMaxHealth();

    protected boolean requiresActivationReagent() { return false; }
    protected double configuredAttackDamage() { return 1.0D; }
    protected boolean canForageGroundFood() { return true; }
    protected boolean canGroupWithNestmates() { return true; }
    protected boolean canMarch() { return true; }
    protected boolean canTraverseFluidFloor(FluidState fluidState) { return false; }

    
    protected boolean handlePriorityForaging() { return false; }

    private boolean shouldUseSharedMarchForageGoal() {
        return this.canRunAntMovementGoals()
                && this.canMarch()
                && this.isMarchFollower()
                && !this.isCarryingFood()
                && !this.isInLove()
                && (this.getSharedMarchFoodChestPos() != null || this.getSharedMarchGroundFoodPos() != null);
    }

    private void tickSharedMarchForageGoal() {
        if (!this.handleSharedMarchChestForaging()) {
            this.handleSharedMarchGroundFoodForaging();
        }
    }

    private boolean shouldUseMarchFollowGoal() {
        return this.canRunAntMovementGoals()
                && this.canMarch()
                && this.isMarchFollower()
                && !this.shouldUseSharedMarchForageGoal();
    }

    private void tickMarchFollowGoal() {
        this.handleMarchFollowing();
    }

    private boolean shouldUseReturnToNestGoal() {
        if (this.tickCount < this.returnToNestFailureCooldownTick) {
            return false;
        }
        return this.canRunAntMovementGoals() && this.shouldReturnToNest(this.getReturnToNestReason());
    }

    private void tickReturnToNestGoal() {
        this.navigateToNest(1.15D);
    }

    private boolean shouldUseForageGoal() {
        return this.canRunAntMovementGoals()
                && this.canForageGroundFood()
                && !this.isCarryingFood()
                && !this.isInLove()
                && this.hasForageTarget();
    }

    private void tickForageGoal() {
        this.handleGroundFoodForaging();
    }

    private void findNearestNestIfNeeded() {
        this.nextNestSearchTick = this.tickCount + (this.isCarryingFood() ? FOOD_NEST_SEARCH_INTERVAL_TICKS : NEST_SEARCH_INTERVAL_TICKS);
        if (this.hasUsableNest()) {
            return;
        }
        this.nestPos = this.findNearestNestInLoadedChunks();
        this.markNestCacheDirty();
    }

    @Nullable
    private BlockPos findNearestNestInLoadedChunks() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return this.findNearestNestInArea();
        }

        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        int bestDistance = Integer.MAX_VALUE;
        int minChunkX = (origin.getX() - NEST_SEARCH_RADIUS_HORIZONTAL) >> 4;
        int maxChunkX = (origin.getX() + NEST_SEARCH_RADIUS_HORIZONTAL) >> 4;
        int minChunkZ = (origin.getZ() - NEST_SEARCH_RADIUS_HORIZONTAL) >> 4;
        int maxChunkZ = (origin.getZ() + NEST_SEARCH_RADIUS_HORIZONTAL) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!serverLevel.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                LevelChunk chunk = serverLevel.getChunk(chunkX, chunkZ);
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof AntNestBlockEntity nestBlockEntity) || !nestBlockEntity.canAccept(this.getType())) {
                        continue;
                    }

                    BlockPos pos = blockEntity.getBlockPos();
                    if (Math.abs(pos.getX() - origin.getX()) > NEST_SEARCH_RADIUS_HORIZONTAL
                            || Math.abs(pos.getY() - origin.getY()) > NEST_SEARCH_RADIUS_VERTICAL
                            || Math.abs(pos.getZ() - origin.getZ()) > NEST_SEARCH_RADIUS_HORIZONTAL) {
                        continue;
                    }

                    int distance = pos.distManhattan(origin);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestPos = pos.immutable();
                    }
                }
            }
        }

        return bestPos;
    }

    @Nullable
    private BlockPos findNearestNestInArea() {
        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        int bestDistance = Integer.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-NEST_SEARCH_RADIUS_HORIZONTAL, -NEST_SEARCH_RADIUS_VERTICAL, -NEST_SEARCH_RADIUS_HORIZONTAL),
                origin.offset(NEST_SEARCH_RADIUS_HORIZONTAL, NEST_SEARCH_RADIUS_VERTICAL, NEST_SEARCH_RADIUS_HORIZONTAL))) {
            BlockEntity blockEntity = this.level().getBlockEntity(pos);
            if (!(blockEntity instanceof AntNestBlockEntity nestBlockEntity) || !nestBlockEntity.canAccept(this.getType())) {
                continue;
            }

            int distance = pos.distManhattan(origin);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPos = pos.immutable();
            }
        }

        return bestPos;
    }

    private boolean handleGroundFoodForaging() {
        if (!this.canForageGroundFood() || this.isCarryingFood() || this.isInLove()) {
            return false;
        }

        ItemEntity nearestFood = this.nearestGroundFood();

        if (nearestFood == null) {
            return this.handleChestFoodForaging();
        }

        if (this.distanceToSqr(nearestFood) <= 2.25D) {
            this.collectGroundFood(nearestFood);
            return true;
        }

        this.getNavigation().moveTo(nearestFood, GROUND_FOOD_MOVE_SPEED);
        return true;
    }

    private boolean handleChestFoodForaging() {
        if (!AntarchySettings.antsStealFromChests()) {
            return false;
        }

        BlockPos chestPos = this.getCurrentFoodChestPos();
        if (chestPos == null) {
            return false;
        }

        Vec3 chestCenter = Vec3.atBottomCenterOf(chestPos);
        if (this.distanceToSqr(chestCenter) <= 4.0D) {
            return this.stealFoodFromChest(chestPos);
        }

        if (this.getNavigation().isDone() || this.tickCount % CHEST_REPATH_TICKS == 0) {
            this.getNavigation().moveTo(chestCenter.x, chestCenter.y, chestCenter.z, CHEST_MOVE_SPEED);
        }
        return true;
    }

    private boolean handleSharedMarchChestForaging() {
        if (this.isCarryingFood() || this.isInLove() || !AntarchySettings.antsStealFromChests()) {
            return false;
        }

        BlockPos chestPos = this.getSharedMarchFoodChestPos();
        if (chestPos == null) {
            return false;
        }

        this.targetFoodChestPos = chestPos;
        Vec3 chestCenter = Vec3.atBottomCenterOf(chestPos);
        if (this.distanceToSqr(chestCenter) <= 4.0D) {
            return this.stealFoodFromChest(chestPos);
        }

        if (this.getNavigation().isDone() || this.tickCount % CHEST_REPATH_TICKS == 0) {
            this.getNavigation().moveTo(chestCenter.x, chestCenter.y, chestCenter.z, SHARED_CHEST_MOVE_SPEED);
        }
        return true;
    }

    private boolean handleSharedMarchGroundFoodForaging() {
        if (this.isCarryingFood() || this.isInLove()) {
            return false;
        }

        BlockPos groundFoodPos = this.getSharedMarchGroundFoodPos();
        if (groundFoodPos == null) {
            return false;
        }

        ItemEntity nearestFood = this.findNearestGroundFoodAt(groundFoodPos);
        if (nearestFood == null) {
            return false;
        }

        if (this.distanceToSqr(nearestFood) <= 2.25D) {
            this.collectGroundFood(nearestFood);
            return true;
        }

        this.getNavigation().moveTo(nearestFood, SHARED_GROUND_FOOD_MOVE_SPEED);
        return true;
    }

    @Nullable
    private BlockPos getCurrentFoodChestPos() {
        if (this.targetFoodChestPos != null && this.isValidFoodChest(this.targetFoodChestPos)) {
            return this.targetFoodChestPos;
        }

        this.targetFoodChestPos = null;
        BlockPos sharedMarchChestPos = this.getSharedMarchFoodChestPos();
        if (sharedMarchChestPos != null) {
            this.targetFoodChestPos = sharedMarchChestPos;
            return this.targetFoodChestPos;
        }
        if (this.tickCount < this.nextFoodChestSearchTick) {
            return null;
        }

        this.nextFoodChestSearchTick = this.tickCount + CHEST_SEARCH_INTERVAL_TICKS;
        this.targetFoodChestPos = this.findClosestFoodChest();
        return this.targetFoodChestPos;
    }

    @Nullable
    private BlockPos findClosestFoodChest() {
        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-(int) FOOD_CHEST_SEARCH_RADIUS, -2, -(int) FOOD_CHEST_SEARCH_RADIUS),
                origin.offset((int) FOOD_CHEST_SEARCH_RADIUS, 2, (int) FOOD_CHEST_SEARCH_RADIUS))) {
            BlockEntity blockEntity = this.level().getBlockEntity(pos);
            if (!(blockEntity instanceof ChestBlockEntity chestBlockEntity) || this.findStealableFoodSlot(chestBlockEntity) < 0) {
                continue;
            }

            double distance = pos.distSqr(origin);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPos = pos.immutable();
            }
        }

        return bestPos;
    }

    private boolean isValidFoodChest(BlockPos chestPos) {
        BlockEntity blockEntity = this.level().getBlockEntity(chestPos);
        return blockEntity instanceof ChestBlockEntity chestBlockEntity && this.findStealableFoodSlot(chestBlockEntity) >= 0;
    }

    private void clearFoodChestTarget() {
        this.targetFoodChestPos = null;
        this.nextFoodChestSearchTick = 0;
    }

    private boolean stealFoodFromChest(BlockPos chestPos) {
        BlockEntity blockEntity = this.level().getBlockEntity(chestPos);
        if (!(blockEntity instanceof ChestBlockEntity chestBlockEntity)) {
            this.clearFoodChestTarget();
            return false;
        }

        int foodSlot = this.findStealableFoodSlot(chestBlockEntity);
        if (foodSlot < 0) {
            this.clearFoodChestTarget();
            return false;
        }

        ItemStack stolenStack = chestBlockEntity.removeItem(foodSlot, 1);
        if (stolenStack.isEmpty()) {
            this.clearFoodChestTarget();
            return false;
        }

        this.playSound(AntarchySoundEvents.ANT_GATHER.get(), 0.3F, 1.0F + this.getRandom().nextFloat() * 0.1F);
        this.startReturningToNestWithFood(stolenStack, chestPos);
        return true;
    }

    private int findStealableFoodSlot(Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack itemStack = container.getItem(slot);
            if (!itemStack.isEmpty() && this.isGroundFood(itemStack)) {
                return slot;
            }
        }

        return -1;
    }

    private void refreshMarch() {
        this.validateMarchState();
        if (this.isInMarch()) {
            return;
        }

        if (this.tryJoinNearbyMarch()) {
            return;
        }

        this.startOwnMarch();
    }

    private void validateMarchState() {
        if (!this.isInMarch()) {
            return;
        }

        if (this.isMarchLeader()) {
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.leaveMarch();
            return;
        }

        BaseAntEntity leader = AntMarchManager.get(serverLevel).getLeader(this.marchLeaderUuid);
        if (leader == null || !leader.isAlive() || !this.canShareMarchWith(leader)) {
            this.leaveMarch();
            return;
        }

        if (this.distanceToSqr(leader) > MARCH_LEADER_LOST_DISTANCE_SQR) {
            this.leaveMarch();
        }
    }

    private boolean tryJoinNearbyMarch() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        AntMarchManager manager = AntMarchManager.get(serverLevel);
        List<BaseAntEntity> candidates = manager.getParticipantsNear(this, MARCH_SEARCH_RADIUS, 3.0D);

        BaseAntEntity targetMarchAnt = candidates.stream()
                .filter(ant -> ant.isInMarch() && ant.canAcceptMarchFollower(this) && this.canShareMarchWith(ant))
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);

        if (targetMarchAnt == null) {
            return false;
        }

        BaseAntEntity leader = targetMarchAnt.isMarchLeader()
                ? targetMarchAnt
                : manager.getLeader(targetMarchAnt.marchLeaderUuid);

        if (leader == null || leader == this) {
            return false;
        }

        this.joinMarch(leader, manager.getNextOrder(leader.getUUID()));
        return true;
    }

    private void startOwnMarch() {
        if (this.isInMarch()) {
            return;
        }

        this.marchLeaderUuid = this.getUUID();
        this.marchOrder = 0;
        if (this.level() instanceof ServerLevel serverLevel) {
            AntMarchManager.get(serverLevel).register(this.marchLeaderUuid, this.marchOrder, this);
        }
    }

    private void joinMarch(BaseAntEntity leader, int order) {
        if (this.isInMarch()) {
            this.leaveMarch();
        }
        this.marchLeaderUuid = leader.getUUID();
        this.marchOrder = order;
        this.nextMarchFollowRepathTick = 0;
        if (this.level() instanceof ServerLevel serverLevel) {
            AntMarchManager.get(serverLevel).register(this.marchLeaderUuid, this.marchOrder, this);
        }
    }

    private void leaveMarch() {
        if (!this.isInMarch()) {
            return;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            AntMarchManager.get(serverLevel).unregister(this.marchLeaderUuid, this.marchOrder);
        }
        this.marchLeaderUuid = null;
        this.marchOrder = -1;
        this.nextMarchFollowRepathTick = 0;
    }

    private boolean handleMarchFollowing() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        AntMarchManager manager = AntMarchManager.get(serverLevel);
        BaseAntEntity leader = manager.getLeader(this.marchLeaderUuid);
        if (leader == null) {
            this.leaveMarch();
            return false;
        }

        BaseAntEntity predecessor = manager.getPredecessor(this.marchLeaderUuid, this.marchOrder);
        if (predecessor == null) {
            predecessor = leader;
        }

        Vec3 followTarget = this.getMarchFollowTarget(predecessor);
        double distanceToTarget = this.distanceToSqr(followTarget.x, followTarget.y, followTarget.z);
        if (distanceToTarget > 0.04D
                && (this.getNavigation().isDone()
                || distanceToTarget > 4.0D
                || this.tickCount >= this.nextMarchFollowRepathTick)) {
            double followSpeed = distanceToTarget > 4.0D ? MARCH_FOLLOW_CATCHUP_SPEED : MARCH_FOLLOW_SPEED;
            this.getNavigation().moveTo(followTarget.x, followTarget.y, followTarget.z, followSpeed);
            this.nextMarchFollowRepathTick = this.tickCount + MARCH_FOLLOW_REPATH_TICKS;
        }
        return true;
    }

    private Vec3 getMarchFollowTarget(BaseAntEntity predecessor) {
        Vec3 direction = predecessor.getDeltaMovement();
        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            float yawRadians = predecessor.getYRot() * ((float) Math.PI / 180.0F);
            horizontal = new Vec3(-Mth.sin(yawRadians), 0.0D, Mth.cos(yawRadians));
        }
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        }

        return predecessor.position().subtract(horizontal.normalize().scale(0.85D)).add(0.0D, 0.05D, 0.0D);
    }

    boolean isInMarch() {
        return this.marchLeaderUuid != null && this.marchOrder >= 0;
    }

    boolean isMarchLeader() {
        return this.isInMarch() && this.marchOrder == 0 && this.getUUID().equals(this.marchLeaderUuid);
    }

    private boolean isMarchFollower() {
        return this.isInMarch() && !this.isMarchLeader();
    }

    boolean canAcceptMarchFollower(BaseAntEntity otherAnt) {
        return this.isInMarch() && this.canShareMarchWith(otherAnt);
    }

    private boolean canShareMarchWith(BaseAntEntity otherAnt) {
        if (otherAnt.getType() == this.getType()) {
            return true;
        }

        return this.canGroupWithNestmates()
                && otherAnt.canGroupWithNestmates()
                && this.sharesNestWith(otherAnt);
    }

    private boolean tryEnterNest() {
        if (this.nestPos == null
                || this.tickCount % 5 != 0
                || !this.canEnterNest()
                || !this.isNearNestEntrance()) {
            return false;
        }

        BlockEntity blockEntity = this.level().getBlockEntity(this.nestPos);
        return blockEntity instanceof AntNestBlockEntity nestBlockEntity && nestBlockEntity.tryStoreAnt(this);
    }

    private boolean shouldReturnToNest(@Nullable String returnReason) {
        return returnReason != null && this.canEnterNest();
    }

    private boolean canEnterNest() {
        return this.isCarryingFood() || this.outsideNestTicks <= 0;
    }

    private void navigateToNest(double speed) {
        if (this.nestPos == null) {
            return;
        }

        BlockPos entrancePos = this.nestPos.above();
        Vec3 entrance = Vec3.atBottomCenterOf(entrancePos);
        double horizontalDistanceSqr = Mth.lengthSquared(this.getX() - entrance.x, this.getZ() - entrance.z);
        if (horizontalDistanceSqr <= 4.0D && Math.abs(this.getY() - entrance.y) <= 1.5D) {
            this.getNavigation().stop();
            this.getMoveControl().setWantedPosition(entrance.x, entrance.y, entrance.z, Math.min(speed, NEST_RETURN_MOVE_SPEED));
            return;
        }

        if (this.tickCount % 10 == 0 || this.getNavigation().isDone()) {
            this.getNavigation().moveTo(entrance.x, entrance.y, entrance.z, Math.min(speed, NEST_RETURN_MOVE_SPEED));
        }
    }

    private boolean isNearNestEntrance() {
        if (this.nestPos == null) {
            return false;
        }

        BlockPos entrancePos = this.nestPos.above();
        if (!this.blockPosition().equals(entrancePos)) {
            return false;
        }

        Vec3 entrance = Vec3.atBottomCenterOf(entrancePos);
        return Math.abs(this.getX() - entrance.x) <= 0.35D
                && Math.abs(this.getZ() - entrance.z) <= 0.35D
                && Math.abs(this.getY() - entrance.y) <= 0.8D;
    }

    private boolean shouldSearchForNest() {
        return !this.hasUsableNest() && this.tickCount >= this.nextNestSearchTick;
    }

    private void markNestCacheDirty() {
        this.nestCacheDirty = true;
        this.nextNestCacheTick = 0;
    }

    private boolean hasUsableNest() {
        if (this.nestCacheDirty || this.tickCount >= this.nextNestCacheTick) {
            this.cachedHasUsableNest = this.nestPos != null
                    && this.level().getBlockEntity(this.nestPos) instanceof AntNestBlockEntity nestBlockEntity
                    && nestBlockEntity.canAccept(this.getType());
            this.nestCacheDirty = false;
            this.nextNestCacheTick = this.tickCount + NEST_CACHE_INTERVAL_TICKS;
        }
        return this.cachedHasUsableNest;
    }

    @Nullable
    private ItemEntity nearestGroundFood() {
        if (this.tickCount >= this.nextGroundFoodCacheTick
                || (this.cachedNearestGroundFood != null && !this.cachedNearestGroundFood.isAlive())) {
            this.cachedNearestGroundFood = this.findNearestGroundFoodRaw();
            this.nextGroundFoodCacheTick = this.tickCount + GROUND_FOOD_CACHE_INTERVAL_TICKS;
        }
        return this.cachedNearestGroundFood;
    }

    private void invalidateGroundFoodCache() {
        this.cachedNearestGroundFood = null;
        this.nextGroundFoodCacheTick = 0;
    }

    private boolean canRunAntMovementGoals() {
        return !this.level().isClientSide && this.isAlive() && !this.isRemoved() && this.getTarget() == null;
    }

    private boolean hasForageTarget() {
        return this.nearestGroundFood() != null || this.getCurrentFoodChestPos() != null;
    }

    private boolean sharesNestWith(BaseAntEntity otherAnt) {
        return this.nestPos != null && this.nestPos.equals(otherAnt.nestPos);
    }

    private boolean isGroundFood(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    private void collectGroundFood(ItemEntity itemEntity) {
        this.rememberRecentGroundFood(itemEntity.blockPosition());
        this.invalidateGroundFoodCache();
        ItemStack itemStack = itemEntity.getItem();
        ItemStack carriedStack = itemStack.copyWithCount(1);
        itemStack.shrink(1);
        if (itemStack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(itemStack);
        }

        this.playSound(AntarchySoundEvents.ANT_GATHER.get(), 0.4F, 1.0F + this.getRandom().nextFloat() * 0.1F);
        this.startReturningToNestWithFood(carriedStack);
    }

    private void tickIdleSound() {
        if (this.tickCount < this.nextIdleSoundTick) {
            return;
        }

        if (!this.onGround() || this.getTarget() != null || this.isCarryingFood() || this.isDancing()) {
            return;
        }

        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0025D) {
            return;
        }

        this.nextIdleSoundTick = this.tickCount + 60 + this.random.nextInt(120);
        this.playSound(AntarchySoundEvents.ANT_IDLE.get(), 0.1F, 0.9F + this.random.nextFloat() * 0.15F);
    }

    @Nullable
    private String getReturnToNestReason() {
        if (!this.hasUsableNest()) {
            return null;
        }

        if (this.isCarryingFood()) {
            return "has_food";
        }

        if (this.level().isNight()) {
            return "night";
        }

        if (this.level().isRaining()) {
            return "raining";
        }

        if (this.distanceToSqr(Vec3.atCenterOf(this.nestPos.above())) > MAX_NEST_WANDER_DISTANCE_SQR) {
            return "too_far_from_nest";
        }

        return null;
    }

    private void syncConfiguredAttributes() {
        this.syncAttribute(this.getAttribute(Attributes.MAX_HEALTH), this.configuredMaxHealth(), true);
        this.syncAttribute(this.getAttribute(Attributes.ATTACK_DAMAGE), this.configuredAttackDamage(), false);
    }

    public boolean isDancing() {
        return this.entityData.get(DANCING);
    }

    private void setDancing(boolean dancing) {
        this.entityData.set(DANCING, dancing);
    }

    private void tickJukeboxDanceState() {
        if (this.jukeboxPos != null) {
            if (this.tickCount % 20 == 0 && !this.isJukeboxActivelyPlayingAt(this.jukeboxPos, this.cachedDanceRadius)) {
                this.jukeboxPos = null;
                if (this.isDancing()) {
                    this.setDancing(false);
                }
            }
            if (this.jukeboxPos != null) {
                return;
            }
        }

        if (this.tickCount < this.nextJukeboxScanTick) {
            if (this.isDancing()) {
                this.setDancing(false);
            }
            return;
        }

        this.cachedDanceRadius = Math.max(1.0D, AntarchySettings.antDanceRadius());
        this.nextJukeboxScanTick = this.tickCount + JUKEBOX_SCAN_INTERVAL_TICKS;
        BlockPos nearbyJukebox = this.findNearestPlayingJukebox(this.cachedDanceRadius);
        this.jukeboxPos = nearbyJukebox;
        this.setDancing(nearbyJukebox != null);
    }

    private boolean isJukeboxActivelyPlayingAt(BlockPos jukeboxPos, double danceRadius) {
        if (!jukeboxPos.closerToCenterThan(this.position(), danceRadius)) {
            return false;
        }

        return this.isJukeboxPlayingFromState(jukeboxPos);
    }

    @Nullable
    private BlockPos findNearestPlayingJukebox(double danceRadius) {
        int radius = Mth.ceil(danceRadius);
        BlockPos center = this.blockPosition();
        BlockPos closest = null;
        double closestDistanceSqr = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius, center.getY() + radius, center.getZ() + radius)) {
            if (!pos.closerToCenterThan(this.position(), danceRadius)) {
                continue;
            }

            if (!this.level().getBlockState(pos).is(Blocks.JUKEBOX)) {
                continue;
            }

            if (!this.isJukeboxPlayingFromState(pos)) {
                continue;
            }

            double distanceSqr = this.distanceToSqr(Vec3.atCenterOf(pos));
            if (distanceSqr < closestDistanceSqr) {
                closestDistanceSqr = distanceSqr;
                closest = pos.immutable();
            }
        }

        return closest;
    }

    private boolean isJukeboxPlayingFromState(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        if (!state.is(Blocks.JUKEBOX)) {
            return false;
        }

        return state.hasProperty(BlockStateProperties.HAS_RECORD) && state.getValue(BlockStateProperties.HAS_RECORD);
    }

    private PlayState mainAnimController(AnimationState<BaseAntEntity> state) {
        if (this.isDancing()) {
            state.getController().setAnimationSpeed(1.0D);
            return state.setAndContinue(DANCE_ANIM);
        }

        if (this.shouldPlayAttackAnimation()) {
            state.getController().setAnimationSpeed(1.0D);
            return state.setAndContinue(ATTACK_ANIM);
        }

        if (this.walkAnimation.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
            state.getController().setAnimationSpeed(1.0D);
            return state.setAndContinue(WALK_ANIM);
        }

        state.getController().setAnimationSpeed(1.0D);
        return state.setAndContinue(IDLE_ANIM);
    }

    private boolean shouldPlayAttackAnimation() {
        if (!(this instanceof RedAntEntity)) {
            return false;
        }

        return this.getBiteAnimationTicks() > 0;
    }

    private int getBiteAnimationTicks() {
        return this.entityData.get(BITE_ANIM_TICKS);
    }

    private void setBiteAnimationTicks(int ticks) {
        this.entityData.set(BITE_ANIM_TICKS, Math.max(0, ticks));
    }

    private void syncAttribute(@Nullable AttributeInstance attributeInstance, double desiredValue, boolean rescaleCurrentHealth) {
        if (attributeInstance == null || Math.abs(attributeInstance.getBaseValue() - desiredValue) < ATTRIBUTE_EPSILON) {
            return;
        }

        if (rescaleCurrentHealth) {
            double previousMaxHealth = attributeInstance.getBaseValue();
            float healthRatio = previousMaxHealth > 0.0D ? this.getHealth() / (float) previousMaxHealth : 1.0F;
            attributeInstance.setBaseValue(desiredValue);
            this.setHealth((float) Mth.clamp(healthRatio * desiredValue, 0.0D, desiredValue));
            return;
        }

        attributeInstance.setBaseValue(desiredValue);
    }

    protected ServerLevel resolveReturnDestinationLevel(ServerPlayer player) {
        return AntTeleportHelper.resolveReturnDestinationLevel(player);
    }

    protected Vec3 getDestinationPosition(ServerLevel destination, ServerPlayer player) {
        return AntTeleportHelper.getDestinationPosition(player, destination);
    }

    public boolean isCarryingFood() {
        return !this.getCarriedFood().isEmpty();
    }

    public ItemStack getCarriedFood() {
        return this.entityData.get(CARRIED_FOOD);
    }

    boolean isTeleportActivatedState() {
        return this.teleportActivated;
    }

    void setTeleportActivatedState(boolean teleportActivated) {
        this.teleportActivated = teleportActivated;
    }

    private void setCarriedFood(ItemStack stack) {
        this.entityData.set(CARRIED_FOOD, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
    }

    private void clearCarriedFood() {
        this.entityData.set(CARRIED_FOOD, ItemStack.EMPTY);
    }

    private void startReturningToNestWithFood(ItemStack carriedFood, @Nullable BlockPos sourceChestPos) {
        this.setCarriedFood(carriedFood);
        this.outsideNestTicks = 0;
        if (sourceChestPos != null) {
            this.rememberRecentFoodChest(sourceChestPos);
        }
        this.clearFoodChestTarget();
        if (!this.hasUsableNest()) {
            this.findNearestNestIfNeeded();
        }
        if (this.nestPos != null) {
            if (this.isMarchLeader()) {
                logDebug(
                        "[Antarchy] March leader {} picked up {} and is heading to nest at {}.",
                        this.getUUID(), carriedFood.getItem(), this.nestPos);
            }
            Vec3 entrance = Vec3.atBottomCenterOf(this.nestPos.above());
            this.getNavigation().moveTo(entrance.x, entrance.y, entrance.z, FOOD_RETURN_MOVE_SPEED);
        } else {
            if (this.isMarchLeader()) {
                logWarn(
                        "[Antarchy] March leader {} picked up {} but has no nest — stroll fallback. pos={}",
                        this.getUUID(), carriedFood.getItem(), this.blockPosition());
            }
            this.getNavigation().stop();
        }
    }

    private void startReturningToNestWithFood(ItemStack carriedFood) {
        this.startReturningToNestWithFood(carriedFood, null);
    }

    private void rememberRecentFoodChest(BlockPos chestPos) {
        this.recentFoodChestPos = chestPos.immutable();
        this.recentFoodChestExpireTick = this.tickCount + SHARED_CHEST_MEMORY_TICKS;
    }

    private void clearRecentFoodChest() {
        this.recentFoodChestPos = null;
        this.recentFoodChestExpireTick = 0;
    }

    private void rememberRecentGroundFood(BlockPos foodPos) {
        this.recentGroundFoodPos = foodPos.immutable();
        this.recentGroundFoodExpireTick = this.tickCount + SHARED_CHEST_MEMORY_TICKS;
    }

    private void clearRecentGroundFood() {
        this.recentGroundFoodPos = null;
        this.recentGroundFoodExpireTick = 0;
    }

    @Nullable
    private BlockPos getSharedMarchFoodChestPos() {
        if (!this.isMarchFollower() || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        BaseAntEntity leader = AntMarchManager.get(serverLevel).getLeader(this.marchLeaderUuid);
        if (leader == null || leader == this) {
            return null;
        }

        if (leader.targetFoodChestPos != null && this.isValidFoodChest(leader.targetFoodChestPos)) {
            return leader.targetFoodChestPos;
        }

        if (leader.recentFoodChestPos != null
                && leader.tickCount <= leader.recentFoodChestExpireTick
                && this.isValidFoodChest(leader.recentFoodChestPos)) {
            return leader.recentFoodChestPos;
        }

        return null;
    }

    @Nullable
    private BlockPos getSharedMarchGroundFoodPos() {
        if (!this.isMarchFollower() || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        BaseAntEntity leader = AntMarchManager.get(serverLevel).getLeader(this.marchLeaderUuid);
        if (leader == null || leader == this) {
            return null;
        }

        if (leader.recentGroundFoodPos != null
                && leader.tickCount <= leader.recentGroundFoodExpireTick
                && this.findNearestGroundFoodAt(leader.recentGroundFoodPos) != null) {
            return leader.recentGroundFoodPos;
        }

        return null;
    }

    @Nullable
    private ItemEntity findNearestGroundFoodAt(BlockPos foodPos) {
        return this.level().getEntitiesOfClass(
                        ItemEntity.class,
                        new AABB(foodPos).inflate(2.0D, 1.5D, 2.0D),
                        itemEntity -> itemEntity.isAlive()
                                && !itemEntity.getItem().isEmpty()
                                && this.isGroundFood(itemEntity.getItem()))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    @Nullable
    private ItemEntity findNearestGroundFoodRaw() {
        return this.level().getEntitiesOfClass(
                        ItemEntity.class,
                        this.getBoundingBox().inflate(10.0D, 2.0D, 10.0D),
                        itemEntity -> itemEntity.isAlive()
                                && !itemEntity.getItem().isEmpty()
                                && this.isGroundFood(itemEntity.getItem()))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private abstract class AbstractAntMoveGoal extends Goal {
        protected AbstractAntMoveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }
    }

    private final class AntDanceGoal extends Goal {
        private AntDanceGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            return isDancing();
        }

        @Override
        public boolean canContinueToUse() {
            return isDancing();
        }

        @Override
        public void start() {
            setTarget(null);
            leaveMarch();
            getNavigation().stop();
        }

        @Override
        public void tick() {
            setTarget(null);
            getNavigation().stop();
            getLookControl().setLookAt(getX(), getEyeY(), getZ(), 10.0F, 10.0F);
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }
    }

    private final class AntSharedMarchForageGoal extends AbstractAntMoveGoal {
        @Override
        public boolean canUse() {
            return shouldUseSharedMarchForageGoal();
        }

        @Override
        public boolean canContinueToUse() {
            return shouldUseSharedMarchForageGoal();
        }

        @Override
        public void tick() {
            tickSharedMarchForageGoal();
        }
    }

    private final class AntMarchFollowGoal extends AbstractAntMoveGoal {
        @Override
        public boolean canUse() {
            return shouldUseMarchFollowGoal();
        }

        @Override
        public boolean canContinueToUse() {
            return shouldUseMarchFollowGoal();
        }

        @Override
        public void tick() {
            tickMarchFollowGoal();
        }
    }

    private final class AntReturnToNestGoal extends AbstractAntMoveGoal {
        
        private int navDoneStreak;

        @Override
        public boolean canUse() {
            return shouldUseReturnToNestGoal();
        }

        @Override
        public boolean canContinueToUse() {
            if (navDoneStreak >= RETURN_NAV_FAILURE_STREAK) {
                returnToNestFailureCooldownTick = tickCount + RETURN_NAV_FAILURE_COOLDOWN;
                logDebug(
                        "[Antarchy] AntReturnToNestGoal giving up — nav failed for {} ticks. "
                        + "leader={} pos={} nestPos={} reason={} cooldown={}t",
                        navDoneStreak, getUUID(), blockPosition(), nestPos,
                        getReturnToNestReason(), RETURN_NAV_FAILURE_COOLDOWN);
                return false;
            }
            return shouldUseReturnToNestGoal();
        }

        @Override
        public void start() {
            navDoneStreak = 0;
            if (isMarchLeader()) {
                logDebug(
                        "[Antarchy] AntReturnToNestGoal START — leader={} nestPos={} carrying={} reason={}",
                        getUUID(), nestPos, isCarryingFood(), getReturnToNestReason());
            }
        }

        @Override
        public void stop() {
            if (isMarchLeader()) {
                logDebug(
                        "[Antarchy] AntReturnToNestGoal STOP  — leader={} nestPos={} carrying={} navDone={}",
                        getUUID(), nestPos, isCarryingFood(), getNavigation().isDone());
            }
            navDoneStreak = 0;
        }

        @Override
        public void tick() {
            tickReturnToNestGoal();
            if (getNavigation().isDone()) {
                navDoneStreak++;
                if (isMarchLeader() && navDoneStreak == RETURN_NAV_FAILURE_STREAK) {
                    logDebug(
                            "[Antarchy] AntReturnToNestGoal nav stalled for {} ticks — will give up next canContinueToUse. "
                            + "leader={} pos={} nestPos={} carrying={} reason={}",
                            navDoneStreak, getUUID(), blockPosition(), nestPos,
                            isCarryingFood(), getReturnToNestReason());
                }
            } else {
                navDoneStreak = 0;
            }
        }
    }

    private final class AntForageGoal extends AbstractAntMoveGoal {
        @Override
        public boolean canUse() {
            return shouldUseForageGoal();
        }

        @Override
        public boolean canContinueToUse() {
            return shouldUseForageGoal();
        }

        @Override
        public void start() {
            if (isMarchLeader()) {
                logDebug(
                        "[Antarchy] AntForageGoal START — leader={} pos={} groundFood={} chest={}",
                        getUUID(), blockPosition(),
                        nearestGroundFood() != null ? nearestGroundFood().blockPosition() : "null",
                        getCurrentFoodChestPos());
            }
        }

        @Override
        public void stop() {
            if (isMarchLeader()) {
                logDebug(
                        "[Antarchy] AntForageGoal STOP  — leader={} pos={} carrying={}",
                        getUUID(), blockPosition(), isCarryingFood());
            }
        }

        @Override
        public void tick() {
            tickForageGoal();
        }
    }

    
    private final class AntFoodEscapeGoal extends AvoidEntityGoal<Player> {
        AntFoodEscapeGoal() {
            super(BaseAntEntity.this, Player.class, 12.0F, ESCAPE_MOVE_SPEED, ESCAPE_MOVE_SPEED);
        }

        @Override
        public boolean canUse() {
            return isCarryingFood() && !hasUsableNest() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return isCarryingFood() && !hasUsableNest() && super.canContinueToUse();
        }
    }

    
    private final class AntRandomStrollGoal extends WaterAvoidingRandomStrollGoal {
        private static final double FORWARD_BIAS_DISTANCE = 8.0D;
        private static final int FORWARD_SEARCH_RADIUS = 8;
        private static final int WIDE_SEARCH_RADIUS = 16;
        private static final int MIN_DISTANCE_SQR = 4;

        AntRandomStrollGoal(double speedModifier) {
            super(BaseAntEntity.this, speedModifier, 5);
        }
        @Override
        public boolean canUse() {
            if (isMarchLeader() || (isCarryingFood() && !hasUsableNest())) {
                Vec3 pos = getPosition();
                if (pos != null) {
                    wantedX = pos.x;
                    wantedY = pos.y;
                    wantedZ = pos.z;
                    return true;
                }
                return false;
            }
            return super.canUse();
        }

        @Override
        @Nullable
        protected Vec3 getPosition() {
            Vec3 heading = getStrollHeading();
            Vec3 forwardBias = position().add(heading.scale(FORWARD_BIAS_DISTANCE));
            Vec3 forward = findStrollTarget(BlockPos.containing(forwardBias), FORWARD_SEARCH_RADIUS);
            if (forward != null) return forward;
            return findStrollTarget(blockPosition(), WIDE_SEARCH_RADIUS);
        }

        @Nullable
        private Vec3 findStrollTarget(BlockPos center, int radius) {
            for (int attempt = 0; attempt < 16; attempt++) {
                int x = center.getX() + random.nextInt(radius * 2 + 1) - radius;
                int z = center.getZ() + random.nextInt(radius * 2 + 1) - radius;

                BlockPos candidate = null;
                for (int dy = 2; dy >= -4; dy--) {
                    BlockPos pos = new BlockPos(x, center.getY() + dy, z);
                    if (isValidStrollPos(pos)) {
                        candidate = pos;
                        break;
                    }
                }

                if (candidate == null) continue;
                if (candidate.distSqr(blockPosition()) < MIN_DISTANCE_SQR) continue;
                if (nestPos != null && candidate.distSqr(nestPos) > MAX_NEST_WANDER_DISTANCE_SQR) continue;

                return new Vec3(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
            }
            return null;
        }

        private Vec3 getStrollHeading() {
            Vec3 movement = getDeltaMovement();
            Vec3 horizontal = new Vec3(movement.x, 0.0D, movement.z);
            if (horizontal.lengthSqr() > 1.0E-4D) return horizontal.normalize();
            float yawRad = getYRot() * ((float) Math.PI / 180.0F);
            return new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        }

        private boolean isValidStrollPos(BlockPos pos) {
            BlockState floor = level().getBlockState(pos.below());
            FluidState fluidBelow = level().getFluidState(pos.below());
            if (!level().getBlockState(pos).isAir()) return false;
            if (!fluidBelow.isEmpty() && !BaseAntEntity.this.canTraverseFluidFloor(fluidBelow)) return false;
            if (!floor.isFaceSturdy(level(), pos.below(), Direction.UP)) return false;
            AABB box = getBoundingBox().move(
                    pos.getX() + 0.5D - getX(),
                    pos.getY() - getY(),
                    pos.getZ() + 0.5D - getZ());
            return level().noCollision(BaseAntEntity.this, box);
        }
    }
}
