package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.config.AntarchySettings;

import java.util.UUID;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
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

public class CaterpillarEntity extends Animal implements GeoEntity {
    private static final ResourceKey<Level> ELYTHIA_DIMENSION = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    private static final TagKey<Biome> CAN_SPAWN_BIOMES = TagKey.create(
            net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "caterpillar_can_spawn_biomes")
    );
    private static final EntityDataAccessor<Integer> LIFE_STAGE =
            SynchedEntityData.defineId(CaterpillarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EAT_ANIMATION_TICKS =
            SynchedEntityData.defineId(CaterpillarEntity.class, EntityDataSerializers.INT);

    private static final String GROWTH_TICKS_KEY = "GrowthTicks";
    private static final String STAGE_TICKS_KEY = "StageTicks";
    private static final String EAT_ANIM_TICKS_KEY = "EatAnimTicks";
    private static final String PUPATION_TARGET_X_KEY = "PupationTargetX";
    private static final String PUPATION_TARGET_Y_KEY = "PupationTargetY";
    private static final String PUPATION_TARGET_Z_KEY = "PupationTargetZ";

    private static final int FOOD_SEARCH_INTERVAL_TICKS = 20;
    private static final int FOOD_REPATH_INTERVAL_TICKS = 12;
    private static final int EAT_SEARCH_INTERVAL_TICKS = 5;
    private static final double FOOD_SEARCH_RANGE = 10.0D;
    private static final double FOOD_EAT_REACH = 1.4D;
    private static final int GROWTH_TICKS_TO_PUPATE = 4800;
    private static final int FOOD_GROWTH_BONUS = 600;
    private static final int EAT_ANIM_TICKS = 30;
    private static final int EAT_CONSUME_TICK = 16;
    private static final int PUPATION_ANIM_TICKS = 25;
    private static final int PUPATION_SEARCH_INTERVAL_TICKS = 20;
    private static final int PUPATION_SEARCH_RADIUS_HORIZONTAL = 8;
    private static final int PUPATION_SEARCH_RADIUS_VERTICAL = 8;
    private static final double PUPATION_BEGIN_DISTANCE_SQR = 0.35D;
    private static final double FOOD_SPEED = 1.0D;
    private static final double PLAYER_FOLLOW_SPEED = 0.95D;
    private static final double PUPATION_SPEED = 0.9D;

    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation EAT_ANIM = RawAnimation.begin().thenPlay("eat");
    private static final RawAnimation PUPATION_ANIM = RawAnimation.begin().thenPlay("Coocon");
    private static final RawAnimation CHRYSALIS_ANIM = RawAnimation.begin().thenLoop("coocon");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private ItemEntity targetFoodItem;
    @Nullable
    private BlockPos pupationStandPos;
    @Nullable
    private UUID eatingFoodEntityId;
    private boolean hasConsumedCurrentFood;
    private int growthTicks;
    private int stageTicks;
    private int nextFoodSearchTick;
    private int nextFoodRepathTick;
    private int nextEatSearchTick;
    private int nextPupationSearchTick;

    public CaterpillarEntity(EntityType<? extends CaterpillarEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.LEAVES, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.156D)
                .add(Attributes.FOLLOW_RANGE, 14.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(BlockPos pos) {
                return CaterpillarEntity.this.isWalkableStandPos(pos);
            }
        };
        navigation.setCanFloat(false);
        navigation.setCanOpenDoors(false);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    public static boolean canSpawn(EntityType<CaterpillarEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        return (level.getLevel().dimension().equals(ELYTHIA_DIMENSION) || level.getBiome(pos).is(CAN_SPAWN_BIOMES))
                && Animal.checkAnimalSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TemptGoal(this, PLAYER_FOLLOW_SPEED, Ingredient.of(AntarchyTags.Items.CATERPILLAR_FOODS), false));
        this.goalSelector.addGoal(2, new PupationSeekGoal());
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public float maxUpStep() {
        return 1.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LIFE_STAGE, LifeStage.CRAWLING.ordinal());
        builder.define(EAT_ANIMATION_TICKS, 0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 2, this::mainAnimController)
                .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>()));
    }

    private PlayState mainAnimController(AnimationState<CaterpillarEntity> state) {
        if (this.getLifeStage() == LifeStage.PUPATING) {
            return state.setAndContinue(PUPATION_ANIM);
        }

        if (this.getLifeStage() == LifeStage.CHRYSALIS) {
            return state.setAndContinue(CHRYSALIS_ANIM);
        }

        if (this.getEatAnimationTicks() > 0) {
            return state.setAndContinue(EAT_ANIM);
        }

        return state.isMoving() ? state.setAndContinue(WALK_ANIM) : state.setAndContinue(IDLE_ANIM);
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

        LifeStage stage = this.getLifeStage();
        if (stage == LifeStage.PUPATING) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            if (this.stageTicks > 0) {
                this.stageTicks--;
            }
            if (this.stageTicks <= 0) {
                this.enterChrysalis();
            }
            return;
        }

        if (stage == LifeStage.CHRYSALIS) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            if (this.stageTicks > 0) {
                this.stageTicks--;
            }
            if (this.stageTicks <= 0) {
                this.metamorphose();
            }
            return;
        }

        if (this.getEatAnimationTicks() > 0) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            this.tickEatingFood();
            this.setEatAnimationTicks(this.getEatAnimationTicks() - 1);
            if (this.getEatAnimationTicks() <= 1) {
                this.eatingFoodEntityId = null;
                this.hasConsumedCurrentFood = false;
            }
            return;
        }

        if (this.isNoGravity()) {
            this.setNoGravity(false);
        }

        if (!this.isBaby()) {
            this.growthTicks++;
        }

        if (this.targetFoodItem != null && !this.isValidFoodItem(this.targetFoodItem)) {
            this.targetFoodItem = null;
        }

        if (this.pupationStandPos != null && !this.isValidPupationStandPos(this.pupationStandPos)) {
            this.pupationStandPos = null;
        }

        if (this.readyToPupate()) {
            this.setLifeStage(LifeStage.SEEKING_PUPATION);
            this.handlePupationSeeking();
            return;
        }

        if (this.getLifeStage() != LifeStage.CRAWLING) {
            this.setLifeStage(LifeStage.CRAWLING);
        }
        this.handleFoodForaging();
    }

    private void handleFoodForaging() {
        if (this.tryEatNearbyFoodItem()) {
            this.targetFoodItem = null;
            this.getNavigation().stop();
            return;
        }

        ItemEntity targetItem = this.getCurrentFoodTarget();
        if (targetItem == null) {
            return;
        }

        if (this.getNavigation().isDone() || this.tickCount >= this.nextFoodRepathTick) {
            this.getNavigation().moveTo(targetItem, FOOD_SPEED);
            this.nextFoodRepathTick = this.tickCount + FOOD_REPATH_INTERVAL_TICKS;
        }
    }

    private boolean tryEatNearbyFoodItem() {
        if (this.tickCount < this.nextEatSearchTick) {
            return false;
        }
        this.nextEatSearchTick = this.tickCount + EAT_SEARCH_INTERVAL_TICKS;

        for (ItemEntity itemEntity : this.level().getEntitiesOfClass(
                ItemEntity.class,
                this.getBoundingBox().inflate(FOOD_EAT_REACH, 0.6D, FOOD_EAT_REACH),
                this::isValidFoodItem)) {
            this.startEatingFood(itemEntity);
            this.setEatAnimationTicks(EAT_ANIM_TICKS);
            return true;
        }

        return false;
    }

    @Nullable
    private ItemEntity getCurrentFoodTarget() {
        if (this.targetFoodItem != null && this.isValidFoodItem(this.targetFoodItem)) {
            return this.targetFoodItem;
        }

        if (this.tickCount < this.nextFoodSearchTick) {
            return null;
        }

        this.nextFoodSearchTick = this.tickCount + FOOD_SEARCH_INTERVAL_TICKS;
        this.targetFoodItem = this.findClosestFoodItem();
        return this.targetFoodItem;
    }

    @Nullable
    private ItemEntity findClosestFoodItem() {
        ItemEntity bestMatch = null;
        double bestDistance = Double.MAX_VALUE;

        for (ItemEntity itemEntity : this.level().getEntitiesOfClass(
                ItemEntity.class,
                this.getBoundingBox().inflate(FOOD_SEARCH_RANGE),
                this::isValidFoodItem)) {
            double distance = this.distanceToSqr(itemEntity);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestMatch = itemEntity;
            }
        }

        return bestMatch;
    }

    private boolean isValidFoodItem(ItemEntity itemEntity) {
        return itemEntity.isAlive()
                && !itemEntity.getItem().isEmpty()
                && itemEntity.getItem().is(AntarchyTags.Items.CATERPILLAR_FOODS);
    }

    private void startEatingFood(ItemEntity itemEntity) {
        this.eatingFoodEntityId = itemEntity.getUUID();
        this.hasConsumedCurrentFood = false;
    }

    private void tickEatingFood() {
        if (this.hasConsumedCurrentFood || this.eatingFoodEntityId == null || this.getEatAnimationTicks() > EAT_CONSUME_TICK) {
            return;
        }

        ItemEntity itemEntity = this.findFoodEntityByUuid(this.eatingFoodEntityId);
        if (itemEntity != null) {
            ItemStack stack = itemEntity.getItem();
            if (stack.is(AntarchyTags.Items.CATERPILLAR_FOODS)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(stack);
                }
                this.growthTicks = Math.min(GROWTH_TICKS_TO_PUPATE, this.growthTicks + FOOD_GROWTH_BONUS);
            }
        }

        this.hasConsumedCurrentFood = true;
    }

    @Nullable
    private ItemEntity findFoodEntityByUuid(UUID uuid) {
        for (ItemEntity itemEntity : this.level().getEntitiesOfClass(
                ItemEntity.class,
                this.getBoundingBox().inflate(3.0D),
                entity -> entity.getUUID().equals(uuid))) {
            return itemEntity;
        }
        return null;
    }

    private void handlePupationSeeking() {
        BlockPos standPos = this.getCurrentPupationStandPos();
        if (standPos == null) {
            return;
        }

        Vec3 targetCenter = Vec3.atBottomCenterOf(standPos);
        if (this.distanceToSqr(targetCenter) <= PUPATION_BEGIN_DISTANCE_SQR) {
            this.beginPupation(standPos);
            return;
        }

        if (this.getNavigation().isDone() || this.tickCount >= this.nextFoodRepathTick) {
            this.getNavigation().moveTo(targetCenter.x, targetCenter.y, targetCenter.z, PUPATION_SPEED);
            this.nextFoodRepathTick = this.tickCount + FOOD_REPATH_INTERVAL_TICKS;
        }
    }

    @Nullable
    private BlockPos getCurrentPupationStandPos() {
        if (this.pupationStandPos != null && this.isValidPupationStandPos(this.pupationStandPos)) {
            return this.pupationStandPos;
        }

        if (this.tickCount < this.nextPupationSearchTick) {
            return null;
        }

        this.nextPupationSearchTick = this.tickCount + PUPATION_SEARCH_INTERVAL_TICKS;
        this.pupationStandPos = this.findClosestPupationStandPos();
        return this.pupationStandPos;
    }

    @Nullable
    private BlockPos findClosestPupationStandPos() {
        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-PUPATION_SEARCH_RADIUS_HORIZONTAL, -PUPATION_SEARCH_RADIUS_VERTICAL, -PUPATION_SEARCH_RADIUS_HORIZONTAL),
                origin.offset(PUPATION_SEARCH_RADIUS_HORIZONTAL, PUPATION_SEARCH_RADIUS_VERTICAL, PUPATION_SEARCH_RADIUS_HORIZONTAL))) {
            if (!this.isValidPupationStandPos(pos)) {
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

    private boolean isValidPupationStandPos(BlockPos pos) {
        BlockPos ceilingPos = pos.above(3);
        if (!this.level().getBlockState(pos).isAir()
                || !this.level().getBlockState(pos.above()).isAir()
                || !this.level().getBlockState(pos.above(2)).isAir()) {
            return false;
        }

        BlockState supportState = this.level().getBlockState(ceilingPos);
        return !supportState.isAir() && supportState.isFaceSturdy(this.level(), ceilingPos, Direction.DOWN);
    }

    private boolean isWalkableStandPos(BlockPos pos) {
        BlockState floorState = this.level().getBlockState(pos.below());
        return this.level().getBlockState(pos).isAir()
                && this.level().getBlockState(pos.above()).isAir()
                && floorState.isFaceSturdy(this.level(), pos.below(), Direction.UP);
    }

    private void beginPupation(BlockPos standPos) {
        this.pupationStandPos = standPos.immutable();
        this.setLifeStage(LifeStage.PUPATING);
        this.stageTicks = PUPATION_ANIM_TICKS;
        this.targetFoodItem = null;
        this.getNavigation().stop();
        this.setNoGravity(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.moveTo(standPos.getX() + 0.5D, standPos.getY() + 0.85D, standPos.getZ() + 0.5D, this.getYRot(), this.getXRot());
    }

    private void enterChrysalis() {
        this.setLifeStage(LifeStage.CHRYSALIS);
        this.stageTicks = AntarchySettings.caterpillarPupationTimeTicks();
        this.setNoGravity(true);
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void spawnAsChrysalis() {
        this.growthTicks = GROWTH_TICKS_TO_PUPATE;
        this.setLifeStage(LifeStage.CHRYSALIS);
        this.stageTicks = AntarchySettings.caterpillarPupationTimeTicks();
        this.setNoGravity(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.setPersistenceRequired();
    }

    private void metamorphose() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ButterflyEntity butterfly = AntarchyObjects.BUTTERFLY.get().create(serverLevel);
        if (butterfly == null) {
            return;
        }

        butterfly.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        butterfly.assignRandomTextureVariant();
        butterfly.setEmergingFromChrysalis();
        if (this.hasCustomName()) {
            butterfly.setCustomName(this.getCustomName());
            butterfly.setCustomNameVisible(this.isCustomNameVisible());
        }
        if (this.isPersistenceRequired()) {
            butterfly.setPersistenceRequired();
        }
        serverLevel.addFreshEntity(butterfly);
        this.discard();
    }

    private boolean readyToPupate() {
        return !this.isBaby() && this.growthTicks >= GROWTH_TICKS_TO_PUPATE;
    }

    public void forcePupation() {
        this.growthTicks = GROWTH_TICKS_TO_PUPATE;
        this.pupationStandPos = null;
        this.stageTicks = 0;
        this.nextPupationSearchTick = 0;
        this.setLifeStage(LifeStage.SEEKING_PUPATION);
        this.getNavigation().stop();
        this.setNoGravity(false);
    }

    private int getEatAnimationTicks() {
        return this.entityData.get(EAT_ANIMATION_TICKS);
    }

    private void setEatAnimationTicks(int ticks) {
        this.entityData.set(EAT_ANIMATION_TICKS, Math.max(0, ticks));
    }

    private void setLifeStage(LifeStage stage) {
        this.entityData.set(LIFE_STAGE, stage.ordinal());
    }

    private LifeStage getLifeStage() {
        int index = this.entityData.get(LIFE_STAGE);
        if (index < 0 || index >= LifeStage.VALUES.length) {
            return LifeStage.CRAWLING;
        }
        return LifeStage.VALUES[index];
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(AntarchyTags.Items.CATERPILLAR_FOODS);
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(GROWTH_TICKS_KEY, this.growthTicks);
        tag.putInt(STAGE_TICKS_KEY, this.stageTicks);
        tag.putInt(EAT_ANIM_TICKS_KEY, this.getEatAnimationTicks());
        tag.putInt("LifeStage", this.getLifeStage().ordinal());
        if (this.pupationStandPos != null) {
            tag.putInt(PUPATION_TARGET_X_KEY, this.pupationStandPos.getX());
            tag.putInt(PUPATION_TARGET_Y_KEY, this.pupationStandPos.getY());
            tag.putInt(PUPATION_TARGET_Z_KEY, this.pupationStandPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.growthTicks = tag.getInt(GROWTH_TICKS_KEY);
        this.stageTicks = tag.getInt(STAGE_TICKS_KEY);
        this.setEatAnimationTicks(tag.getInt(EAT_ANIM_TICKS_KEY));
        this.setLifeStage(LifeStage.VALUES[Math.max(0, Math.min(LifeStage.VALUES.length - 1, tag.getInt("LifeStage")))]);
        this.pupationStandPos = tag.contains(PUPATION_TARGET_X_KEY)
                ? new BlockPos(tag.getInt(PUPATION_TARGET_X_KEY), tag.getInt(PUPATION_TARGET_Y_KEY), tag.getInt(PUPATION_TARGET_Z_KEY))
                : null;
        this.targetFoodItem = null;
        this.eatingFoodEntityId = null;
        this.hasConsumedCurrentFood = false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.CATERPILLAR_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.CATERPILLAR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.CATERPILLAR_HURT.get();
    }

    private enum LifeStage {
        CRAWLING,
        SEEKING_PUPATION,
        PUPATING,
        CHRYSALIS;

        private static final LifeStage[] VALUES = LifeStage.values();
    }

    private final class PupationSeekGoal extends Goal {
        private PupationSeekGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !CaterpillarEntity.this.isBaby() && CaterpillarEntity.this.readyToPupate();
        }

        @Override
        public boolean canContinueToUse() {
            return !CaterpillarEntity.this.isBaby()
                    && (CaterpillarEntity.this.readyToPupate()
                    || CaterpillarEntity.this.getLifeStage() == LifeStage.SEEKING_PUPATION
                    || CaterpillarEntity.this.getLifeStage() == LifeStage.PUPATING
                    || CaterpillarEntity.this.getLifeStage() == LifeStage.CHRYSALIS);
        }

        @Override
        public void start() {
            CaterpillarEntity.this.setLifeStage(LifeStage.SEEKING_PUPATION);
            CaterpillarEntity.this.nextPupationSearchTick = 0;
        }

        @Override
        public void stop() {
            CaterpillarEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (CaterpillarEntity.this.getLifeStage() == LifeStage.PUPATING
                    || CaterpillarEntity.this.getLifeStage() == LifeStage.CHRYSALIS) {
                return;
            }

            CaterpillarEntity.this.setLifeStage(LifeStage.SEEKING_PUPATION);
            CaterpillarEntity.this.handlePupationSeeking();
        }
    }
}
