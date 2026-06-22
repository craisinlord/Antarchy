package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
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
import software.bernie.geckolib.util.GeckoLibUtil;

public class ButterflyEntity extends Animal implements FlyingAnimal, GeoEntity {
    private static final ResourceKey<Level> ELYTHIA_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    private static final TagKey<Biome> CAN_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "butterfly_can_spawn_biomes")
    );

    private static final EntityDataAccessor<Integer> ACTIVITY_STATE =
            SynchedEntityData.defineId(ButterflyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT =
            SynchedEntityData.defineId(ButterflyEntity.class, EntityDataSerializers.INT);

    private static final String ACTIVITY_STATE_KEY = "ActivityState";
    private static final String STAGE_TICKS_KEY = "StageTicks";
    private static final String TEXTURE_VARIANT_KEY = "TextureVariant";
    private static final String SAVED_FLOWER_X_KEY = "SavedFlowerX";
    private static final String SAVED_FLOWER_Y_KEY = "SavedFlowerY";
    private static final String SAVED_FLOWER_Z_KEY = "SavedFlowerZ";
    private static final String HAS_NECTAR_KEY = "HasNectar";

    private static final int EMERGE_TICKS = 34;
    private static final int FLOWER_SEARCH_COOLDOWN_MIN = 80;
    private static final int FLOWER_SEARCH_COOLDOWN_MAX = 200;
    private static final int FLOWER_RETRY_COOLDOWN = 200;
    private static final int MAX_CROPS_GROWN_PER_POLLINATION = 10;
    private static final String[] TEXTURE_VARIANT_NAMES = {
            "brown",
            "blue",
            "green",
            "magenta",
            "yellow",
            "cyan",
            "lavender",
            "light_blue",
            "orange",
            "purple"
    };

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation EMERGE_ANIM = RawAnimation.begin().thenPlay("coocon2");
    private static final RawAnimation POLLINATE_ANIM = RawAnimation.begin().thenLoop("eat");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    BlockPos savedFlowerPos;
    int remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(this.random, FLOWER_SEARCH_COOLDOWN_MIN, FLOWER_SEARCH_COOLDOWN_MAX);
    private boolean hasNectar;
    private int cropsGrownSincePollination;
    private int stageTicks;
    @Nullable
    private ButterflyPollinateGoal butterflyPollinateGoal;

    public ButterflyEntity(EntityType<? extends ButterflyEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.lookControl = new ButterflyLookControl(this);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.LEAVES, -1.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.FLYING_SPEED, 0.55D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public static boolean canSpawn(EntityType<ButterflyEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        return (level.getLevel().dimension().equals(ELYTHIA_DIMENSION) || level.getBiome(pos).is(CAN_SPAWN_BIOMES))
                && Animal.checkAnimalSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVITY_STATE, ActivityState.NORMAL.ordinal());
        builder.define(TEXTURE_VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TemptGoal(this, 1.25D, Ingredient.of(AntarchyTags.Items.BUTTERFLY_BREEDING_FOODS), false));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0D));
        this.butterflyPollinateGoal = new ButterflyPollinateGoal();
        this.goalSelector.addGoal(2, this.butterflyPollinateGoal);
        this.goalSelector.addGoal(3, new ButterflyGrowCropGoal());
        this.goalSelector.addGoal(4, new ButterflyWanderGoal());
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(BlockPos pos) {
                return !this.level.getBlockState(pos.below()).isAir();
            }

            @Override
            public void tick() {
                if (!ButterflyEntity.this.isPollinating()) {
                    super.tick();
                }
            }
        };
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(false);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 2, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<ButterflyEntity> state) {
        if (this.getActivityState() == ActivityState.EMERGING) {
            return state.setAndContinue(EMERGE_ANIM);
        }

        if (this.isPollinating()) {
            return state.setAndContinue(POLLINATE_ANIM);
        }

        if (this.onGround()) {
            return state.setAndContinue(IDLE_ANIM);
        }

        state.getController().setAnimationSpeed(0.78D);
        return state.setAndContinue(FLY_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.assignRandomTextureVariant();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.getActivityState() == ActivityState.EMERGING) {
            if (this.stageTicks > 0) {
                this.stageTicks--;
            }
            if (this.stageTicks <= 0) {
                this.setActivityState(ActivityState.NORMAL);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getActivityState() == ActivityState.EMERGING && source.is(DamageTypes.IN_WALL)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
                this.remainingCooldownBeforeLocatingNewFlower--;
            }
        }
    }

    public void setEmergingFromChrysalis() {
        this.setActivityState(ActivityState.EMERGING);
        this.stageTicks = EMERGE_TICKS;
        this.setDeltaMovement(Vec3.ZERO);
    }

    public boolean isPollinating() {
        return this.getActivityState() == ActivityState.POLLINATING;
    }

    private void setPollinating(boolean pollinating) {
        if (this.getActivityState() == ActivityState.EMERGING) {
            return;
        }

        this.setActivityState(pollinating ? ActivityState.POLLINATING : ActivityState.NORMAL);
    }

    boolean hasNectar() {
        return this.hasNectar;
    }

    private void setHasNectar(boolean hasNectar) {
        this.hasNectar = hasNectar;
        if (hasNectar) {
            this.cropsGrownSincePollination = 0;
        }
    }

    void dropOffNectar() {
        this.hasNectar = false;
        this.cropsGrownSincePollination = 0;
    }

    public boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    public boolean isFlowerValid(BlockPos pos) {
        return this.level().isLoaded(pos) && this.isValidPollinationBlock(this.level().getBlockState(pos));
    }

    private boolean isValidPollinationBlock(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && Boolean.TRUE.equals(state.getValue(BlockStateProperties.WATERLOGGED))) {
            return false;
        }

        if (!state.is(BlockTags.FLOWERS)) {
            return false;
        }

        if (state.is(Blocks.SUNFLOWER)) {
            return state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
        }

        return true;
    }

    boolean isTooFarAway(BlockPos pos) {
        return !pos.closerThan(this.blockPosition(), 32.0D);
    }

    boolean closerThan(BlockPos pos, int distance) {
        return pos.closerThan(this.blockPosition(), (double) distance);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(AntarchyTags.Items.BUTTERFLY_BREEDING_FOODS);
    }

    public String getTextureVariantName() {
        int variant = this.getTextureVariant();
        return TEXTURE_VARIANT_NAMES[Mth.clamp(variant, 0, TEXTURE_VARIANT_NAMES.length - 1)];
    }

    public void assignRandomTextureVariant() {
        this.setTextureVariant(this.random.nextInt(TEXTURE_VARIANT_NAMES.length));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        CaterpillarEntity caterpillar = AntarchyObjects.CATERPILLAR.get().create(level);
        if (caterpillar != null) {
            caterpillar.setBaby(true);
        }
        return caterpillar;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(ACTIVITY_STATE_KEY, this.getActivityState().ordinal());
        tag.putInt(STAGE_TICKS_KEY, this.stageTicks);
        tag.putInt(TEXTURE_VARIANT_KEY, this.getTextureVariant());
        tag.putBoolean(HAS_NECTAR_KEY, this.hasNectar);
        if (this.savedFlowerPos != null) {
            tag.putInt(SAVED_FLOWER_X_KEY, this.savedFlowerPos.getX());
            tag.putInt(SAVED_FLOWER_Y_KEY, this.savedFlowerPos.getY());
            tag.putInt(SAVED_FLOWER_Z_KEY, this.savedFlowerPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ActivityState savedState = ActivityState.byOrdinal(tag.getInt(ACTIVITY_STATE_KEY));
        this.setActivityState(savedState == ActivityState.POLLINATING ? ActivityState.NORMAL : savedState);
        this.stageTicks = tag.getInt(STAGE_TICKS_KEY);
        if (tag.contains(TEXTURE_VARIANT_KEY)) {
            this.setTextureVariant(tag.getInt(TEXTURE_VARIANT_KEY));
        } else {
            this.assignRandomTextureVariant();
        }
        this.hasNectar = tag.getBoolean(HAS_NECTAR_KEY);
        this.savedFlowerPos = tag.contains(SAVED_FLOWER_X_KEY)
                ? new BlockPos(tag.getInt(SAVED_FLOWER_X_KEY), tag.getInt(SAVED_FLOWER_Y_KEY), tag.getInt(SAVED_FLOWER_Z_KEY))
                : null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.BUTTERFLY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    protected void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    protected void jumpInLiquid(net.minecraft.tags.TagKey<Fluid> fluidTag) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.01D, 0.0D));
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    private void setActivityState(ActivityState state) {
        this.entityData.set(ACTIVITY_STATE, state.ordinal());
    }

    private ActivityState getActivityState() {
        return ActivityState.byOrdinal(this.entityData.get(ACTIVITY_STATE));
    }

    private int getTextureVariant() {
        return this.entityData.get(TEXTURE_VARIANT);
    }

    private void setTextureVariant(int variant) {
        this.entityData.set(TEXTURE_VARIANT, Mth.clamp(variant, 0, TEXTURE_VARIANT_NAMES.length - 1));
    }

    private enum ActivityState {
        NORMAL,
        EMERGING,
        POLLINATING;

        private static final ActivityState[] VALUES = values();

        private static ActivityState byOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length) {
                return NORMAL;
            }
            return VALUES[ordinal];
        }
    }

    private abstract class BaseButterflyGoal extends Goal {
        @Override
        public boolean canUse() {
            return ButterflyEntity.this.getActivityState() != ActivityState.EMERGING && this.canButterflyUse();
        }

        @Override
        public boolean canContinueToUse() {
            return ButterflyEntity.this.getActivityState() != ActivityState.EMERGING && this.canButterflyContinueToUse();
        }

        protected abstract boolean canButterflyUse();

        protected abstract boolean canButterflyContinueToUse();
    }

    private final class ButterflyLookControl extends LookControl {
        private ButterflyLookControl(Mob mob) {
            super(mob);
        }

        @Override
        protected boolean resetXRotOnTick() {
            return !ButterflyEntity.this.isPollinating();
        }
    }

    private final class ButterflyPollinateGoal extends BaseButterflyGoal {
        private static final int MIN_SUCCESSFUL_POLLINATION_TICKS = 24;
        private static final int MAX_POLLINATING_TICKS = 70;
        private static final int POSITION_CHANGE_CHANCE = 20;
        private static final double ARRIVAL_THRESHOLD = 0.18D;
        private static final double APPROACH_SPEED = 1.1D;
        private static final double HOVER_SPEED = 0.52D;
        private static final float HOVER_HEIGHT_WITHIN_FLOWER = 0.7F;
        private static final float HOVER_POS_OFFSET = 0.45F;
        private static final double SEARCH_RADIUS = 5.0D;
        private static final int FLOWER_SEARCH_INTERVAL = 40;

        private final Predicate<BlockState> validFlowerPredicate = ButterflyEntity.this::isValidPollinationBlock;
        @Nullable
        private Vec3 hoverPos;
        private int successfulPollinatingTicks;
        private int totalPollinatingTicks;
        @Nullable
        private BlockPos lastSearchResult;
        private int ticksSinceLastSearch = FLOWER_SEARCH_INTERVAL;

        private ButterflyPollinateGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        protected boolean canButterflyUse() {
            if (ButterflyEntity.this.remainingCooldownBeforeLocatingNewFlower > 0 || ButterflyEntity.this.hasNectar() || ButterflyEntity.this.level().isRaining()) {
                return false;
            }

            BlockPos targetFlower = this.resolveFlowerTarget();
            if (targetFlower == null) {
                ButterflyEntity.this.remainingCooldownBeforeLocatingNewFlower =
                        Mth.nextInt(ButterflyEntity.this.random, FLOWER_SEARCH_COOLDOWN_MIN, FLOWER_SEARCH_COOLDOWN_MAX);
                return false;
            }

            ButterflyEntity.this.savedFlowerPos = targetFlower;
            return true;
        }

        @Override
        protected boolean canButterflyContinueToUse() {
            if (ButterflyEntity.this.savedFlowerPos == null || ButterflyEntity.this.level().isRaining()) {
                return false;
            }

            if (this.totalPollinatingTicks >= MAX_POLLINATING_TICKS) {
                return false;
            }

            if (ButterflyEntity.this.tickCount % 20 == 0 && !ButterflyEntity.this.isFlowerValid(ButterflyEntity.this.savedFlowerPos)) {
                ButterflyEntity.this.savedFlowerPos = null;
                return false;
            }

            if (this.successfulPollinatingTicks > MIN_SUCCESSFUL_POLLINATION_TICKS) {
                return ButterflyEntity.this.random.nextFloat() < 0.2F;
            }

            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.hoverPos = null;
            this.successfulPollinatingTicks = 0;
            this.totalPollinatingTicks = 0;
            ButterflyEntity.this.setPollinating(false);
            this.moveTowardFlowerCenter();
        }

        @Override
        public void stop() {
            if (this.successfulPollinatingTicks > MIN_SUCCESSFUL_POLLINATION_TICKS) {
                ButterflyEntity.this.setHasNectar(true);
            }

            ButterflyEntity.this.setPollinating(false);
            ButterflyEntity.this.getNavigation().stop();
            ButterflyEntity.this.remainingCooldownBeforeLocatingNewFlower = FLOWER_RETRY_COOLDOWN;
        }

        @Override
        public void tick() {
            if (ButterflyEntity.this.savedFlowerPos == null) {
                return;
            }

            this.totalPollinatingTicks++;
            Vec3 flowerHoverPos = Vec3.atBottomCenterOf(ButterflyEntity.this.savedFlowerPos)
                    .add(0.0D, HOVER_HEIGHT_WITHIN_FLOWER, 0.0D);

            if (ButterflyEntity.this.position().distanceTo(flowerHoverPos) > 1.1D) {
                ButterflyEntity.this.setPollinating(false);
                this.hoverPos = flowerHoverPos;
                this.moveTowardFlowerCenter();
                return;
            }

            ButterflyEntity.this.setPollinating(true);
            ButterflyEntity.this.getNavigation().stop();
            if (this.hoverPos == null) {
                this.hoverPos = flowerHoverPos;
            }

            boolean atHoverPos = ButterflyEntity.this.position().distanceTo(this.hoverPos) <= ARRIVAL_THRESHOLD;
            boolean shouldMove = true;
            if (atHoverPos) {
                if (ButterflyEntity.this.random.nextInt(POSITION_CHANGE_CHANCE) == 0) {
                    this.hoverPos = new Vec3(
                            flowerHoverPos.x + this.getOffset(),
                            flowerHoverPos.y + (ButterflyEntity.this.random.nextDouble() - 0.5D) * 0.12D,
                            flowerHoverPos.z + this.getOffset()
                    );
                } else {
                    shouldMove = false;
                }

                ButterflyEntity.this.getLookControl().setLookAt(flowerHoverPos.x, flowerHoverPos.y, flowerHoverPos.z);
            }

            if (shouldMove && this.hoverPos != null) {
                ButterflyEntity.this.getMoveControl().setWantedPosition(
                        this.hoverPos.x,
                        this.hoverPos.y,
                        this.hoverPos.z,
                        HOVER_SPEED
                );
            }

            this.successfulPollinatingTicks++;
        }

        @Nullable
        private BlockPos resolveFlowerTarget() {
            if (ButterflyEntity.this.savedFlowerPos != null) {
                if (ButterflyEntity.this.isFlowerValid(ButterflyEntity.this.savedFlowerPos) && !ButterflyEntity.this.isTooFarAway(ButterflyEntity.this.savedFlowerPos)) {
                    return ButterflyEntity.this.savedFlowerPos;
                }
                ButterflyEntity.this.savedFlowerPos = null;
            }

            this.ticksSinceLastSearch++;
            if (this.ticksSinceLastSearch >= FLOWER_SEARCH_INTERVAL) {
                this.ticksSinceLastSearch = 0;
                this.lastSearchResult = this.findNearbyFlower().orElse(null);
            }
            return this.lastSearchResult;
        }

        private void moveTowardFlowerCenter() {
            if (ButterflyEntity.this.savedFlowerPos == null) {
                return;
            }

            ButterflyEntity.this.getNavigation().moveTo(
                    ButterflyEntity.this.savedFlowerPos.getX() + 0.5D,
                    ButterflyEntity.this.savedFlowerPos.getY() + 0.5D,
                    ButterflyEntity.this.savedFlowerPos.getZ() + 0.5D,
                    APPROACH_SPEED
            );
        }

        private float getOffset() {
            return (ButterflyEntity.this.random.nextFloat() * 2.0F - 1.0F) * HOVER_POS_OFFSET;
        }

        private Optional<BlockPos> findNearbyFlower() {
            BlockPos origin = ButterflyEntity.this.blockPosition();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            for (int y = 0; (double) y <= SEARCH_RADIUS; y = y > 0 ? -y : 1 - y) {
                for (int ring = 0; (double) ring < SEARCH_RADIUS; ++ring) {
                    for (int x = 0; x <= ring; x = x > 0 ? -x : 1 - x) {
                        for (int z = x < ring && x > -ring ? ring : 0; z <= ring; z = z > 0 ? -z : 1 - z) {
                            cursor.setWithOffset(origin, x, y - 1, z);
                            if (origin.closerThan(cursor, SEARCH_RADIUS)
                                    && this.validFlowerPredicate.test(ButterflyEntity.this.level().getBlockState(cursor))) {
                                return Optional.of(cursor.immutable());
                            }
                        }
                    }
                }
            }

            return Optional.empty();
        }
    }

    private final class ButterflyGrowCropGoal extends BaseButterflyGoal {
        private static final int GROW_CHANCE = 30;

        @Override
        protected boolean canButterflyUse() {
            if (!ButterflyEntity.this.hasNectar() || ButterflyEntity.this.cropsGrownSincePollination >= MAX_CROPS_GROWN_PER_POLLINATION) {
                return false;
            }

            return ButterflyEntity.this.random.nextFloat() >= 0.3F;
        }

        @Override
        protected boolean canButterflyContinueToUse() {
            return this.canButterflyUse();
        }

        @Override
        public void stop() {
            if (ButterflyEntity.this.cropsGrownSincePollination >= MAX_CROPS_GROWN_PER_POLLINATION) {
                ButterflyEntity.this.dropOffNectar();
            }
        }

        @Override
        public void tick() {
            if (!(ButterflyEntity.this.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            if (ButterflyEntity.this.random.nextInt(this.adjustedTickDelay(GROW_CHANCE)) != 0) {
                return;
            }

            for (int depth = 1; depth <= 2; depth++) {
                BlockPos cropPos = ButterflyEntity.this.blockPosition().below(depth);
                BlockState cropState = ButterflyEntity.this.level().getBlockState(cropPos);
                Block block = cropState.getBlock();
                BlockState updatedState = null;

                if (!cropState.is(BlockTags.BEE_GROWABLES)) {
                    continue;
                }

                if (block instanceof CropBlock cropBlock) {
                    if (!cropBlock.isMaxAge(cropState)) {
                        updatedState = cropBlock.getStateForAge(cropBlock.getAge(cropState) + 1);
                    }
                } else if (block instanceof StemBlock) {
                    int age = cropState.getValue(StemBlock.AGE);
                    if (age < 7) {
                        updatedState = cropState.setValue(StemBlock.AGE, age + 1);
                    }
                } else if (cropState.is(Blocks.SWEET_BERRY_BUSH)) {
                    int age = cropState.getValue(SweetBerryBushBlock.AGE);
                    if (age < 3) {
                        updatedState = cropState.setValue(SweetBerryBushBlock.AGE, age + 1);
                    }
                } else if (cropState.is(Blocks.CAVE_VINES) || cropState.is(Blocks.CAVE_VINES_PLANT)) {
                    ((BonemealableBlock) cropState.getBlock()).performBonemeal(serverLevel, ButterflyEntity.this.random, cropPos, cropState);
                    ButterflyEntity.this.level().levelEvent(2011, cropPos, 15);
                    ButterflyEntity.this.cropsGrownSincePollination++;
                }

                if (updatedState != null) {
                    ButterflyEntity.this.level().levelEvent(2011, cropPos, 15);
                    ButterflyEntity.this.level().setBlockAndUpdate(cropPos, updatedState);
                    ButterflyEntity.this.cropsGrownSincePollination++;
                }

                if (ButterflyEntity.this.cropsGrownSincePollination >= MAX_CROPS_GROWN_PER_POLLINATION) {
                    ButterflyEntity.this.dropOffNectar();
                    return;
                }
            }
        }
    }

    private final class ButterflyWanderGoal extends BaseButterflyGoal {
        private ButterflyWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        protected boolean canButterflyUse() {
            return ButterflyEntity.this.getNavigation().isDone()
                    && ButterflyEntity.this.random.nextInt(16) == 0;
        }

        @Override
        protected boolean canButterflyContinueToUse() {
            return ButterflyEntity.this.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            Vec3 target = this.findPos();
            if (target != null) {
                ButterflyEntity.this.getNavigation().moveTo(target.x, target.y, target.z, 1.0D);
            }
        }

        @Nullable
        private Vec3 findPos() {
            Vec3 direction;
            if (ButterflyEntity.this.savedFlowerPos != null && !ButterflyEntity.this.closerThan(ButterflyEntity.this.savedFlowerPos, 10)) {
                direction = Vec3.atCenterOf(ButterflyEntity.this.savedFlowerPos).subtract(ButterflyEntity.this.position()).normalize();
            } else {
                direction = ButterflyEntity.this.getViewVector(0.0F);
            }

            Vec3 hoverTarget = HoverRandomPos.getPos(
                    ButterflyEntity.this,
                    8,
                    7,
                    direction.x,
                    direction.z,
                    (float) Math.PI / 2.0F,
                    3,
                    1
            );
            if (hoverTarget != null) {
                return hoverTarget;
            }

            return AirAndWaterRandomPos.getPos(
                    ButterflyEntity.this,
                    8,
                    4,
                    -2,
                    direction.x,
                    direction.z,
                    (double) ((float) Math.PI / 2.0F)
            );
        }
    }
}
