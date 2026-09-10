package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WormEntity extends Monster implements GeoEntity {
    public enum GrowthStage {
        SMALL(0, EntityDimensions.scalable(0.55F, 0.8F), 8.0D, 0.29D, 2.0D, 20.0D, 0.05D, 2),
        MEDIUM(1, EntityDimensions.scalable(0.9F, 1.5F), 20.0D, 0.25D, 4.0D, 24.0D, 0.2D, 5),
        LARGE(2, EntityDimensions.scalable(1.35F, 2.75F), 36.0D, 0.22D, 7.0D, 28.0D, 0.45D, 8);

        private final int id;
        private final EntityDimensions dimensions;
        private final double health;
        private final double speed;
        private final double damage;
        private final double followRange;
        private final double knockbackResistance;
        private final int xp;

        GrowthStage(int id, EntityDimensions dimensions, double health, double speed, double damage,
                    double followRange, double knockbackResistance, int xp) {
            this.id = id;
            this.dimensions = dimensions;
            this.health = health;
            this.speed = speed;
            this.damage = damage;
            this.followRange = followRange;
            this.knockbackResistance = knockbackResistance;
            this.xp = xp;
        }

        public static GrowthStage byId(int id) {
            for (GrowthStage stage : values()) {
                if (stage.id == id) return stage;
            }
            return LARGE;
        }
    }

    private static final int SMALL_GROWTH_TICKS = 20 * 60 * 2;
    private static final int MEDIUM_GROWTH_TICKS = 20 * 60 * 3;
    private static final ResourceKey<Biome> WORMY_CAVES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "wormy_caves")
    );
    private static final ResourceLocation LOAM_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "loam");
    private static final ResourceLocation MUCUS_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "mucus");
    private static final int MUCUS_TRAIL_INTERVAL = 6;
    private static final double MUCUS_TRAIL_MIN_DISTANCE_SQR = 0.36D;
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(WormEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GROWTH_STAGE = SynchedEntityData.defineId(WormEntity.class, EntityDataSerializers.INT);
    private static final int ANIM_INGROUND = 0;
    private static final int ANIM_CRAWL = 1;
    private static final int ANIM_ATTACK = 2;
    private static final RawAnimation INGROUND_ANIM = RawAnimation.begin().thenLoop("inground");
    private static final RawAnimation CRAWL_ANIM = RawAnimation.begin().thenLoop("crawl");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int attackAnimationTicks;
    private int mucusTrailCooldown;
    private Vec3 lastMucusTrailPosition;
    private int growthAge;

    public WormEntity(EntityType<? extends WormEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D);
    }

    public static boolean canSpawn(EntityType<WormEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if ((spawnReason == MobSpawnType.NATURAL || spawnReason == MobSpawnType.CHUNK_GENERATION)
                && !level.getBiome(pos).is(WORMY_CAVES)) {
            return false;
        }

        Optional<Block> loam = BuiltInRegistries.BLOCK.getOptional(LOAM_ID);
        BlockState floor = level.getBlockState(pos.below());
        if (!floor.is(Blocks.MUD) && (loam.isEmpty() || !floor.is(loam.get()))) {
            return false;
        }

        return level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getBlockState(pos.above(2)).getCollisionShape(level, pos.above(2)).isEmpty()
                && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_INGROUND);
        builder.define(GROWTH_STAGE, GrowthStage.LARGE.id);
    }

    public GrowthStage getGrowthStage() {
        return GrowthStage.byId(this.entityData.get(GROWTH_STAGE));
    }

    public void setGrowthStage(GrowthStage stage) {
        if (this.entityData.get(GROWTH_STAGE) == stage.id) return;
        this.entityData.set(GROWTH_STAGE, stage.id);
        this.refreshDimensions();
        this.applyStageAttributes();
    }

    private void applyStageAttributes() {
        GrowthStage stage = this.getGrowthStage();
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stage.health);
        if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(stage.speed);
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stage.damage);
        if (this.getAttribute(Attributes.FOLLOW_RANGE) != null) this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(stage.followRange);
        if (this.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(stage.knockbackResistance);
        this.xpReward = stage.xp;
        if (!this.level().isClientSide) this.setHealth(Math.min(this.getHealth(), (float) stage.health));
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.getGrowthStage().dimensions;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (GROWTH_STAGE.equals(key)) this.refreshDimensions();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
        if (spawnReason == MobSpawnType.NATURAL || spawnReason == MobSpawnType.CHUNK_GENERATION || spawnReason == MobSpawnType.SPAWN_EGG) {
            float roll = this.random.nextFloat();
            this.setGrowthStage(roll < 0.4F ? GrowthStage.SMALL : roll < 0.6F ? GrowthStage.MEDIUM : GrowthStage.LARGE);
            this.growthAge = 0;
        }
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GrowthStage", this.getGrowthStage().id);
        tag.putInt("GrowthAge", this.growthAge);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(GROWTH_STAGE, tag.contains("GrowthStage") ? tag.getInt("GrowthStage") : GrowthStage.LARGE.id);
        this.growthAge = tag.getInt("GrowthAge");
        this.refreshDimensions();
        this.applyStageAttributes();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController)
                .triggerableAnim("attack", ATTACK_ANIM));
    }

    private PlayState mainAnimController(AnimationState<WormEntity> state) {
        return switch (this.entityData.get(ANIMATION_STATE)) {
            case ANIM_CRAWL -> state.setAndContinue(CRAWL_ANIM);
            case ANIM_ATTACK -> state.setAndContinue(ATTACK_ANIM);
            default -> state.setAndContinue(INGROUND_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity) {
            this.attackAnimationTicks = 45;
            this.entityData.set(ANIMATION_STATE, ANIM_ATTACK);
            this.triggerAnim("main_controller", "attack");
            this.playSound(AntarchySoundEvents.MOLEWORM_ATTACK.get(), 0.45F, 0.85F + this.random.nextFloat() * 0.2F);
        }
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        this.tickGrowth();
        this.tickMucusTrail();
        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks--;
        }
        this.updateAnimationState();
    }

    private void tickGrowth() {
        GrowthStage stage = this.getGrowthStage();
        if (stage == GrowthStage.LARGE) return;
        this.growthAge++;
        if (stage == GrowthStage.SMALL && this.growthAge >= SMALL_GROWTH_TICKS) {
            this.growthAge = 0;
            this.setGrowthStage(GrowthStage.MEDIUM);
        } else if (stage == GrowthStage.MEDIUM && this.growthAge >= MEDIUM_GROWTH_TICKS) {
            this.growthAge = 0;
            this.setGrowthStage(GrowthStage.LARGE);
        }
    }

    private void tickMucusTrail() {
        if (this.getGrowthStage() == GrowthStage.SMALL) return;
        if (this.mucusTrailCooldown > 0) {
            this.mucusTrailCooldown--;
        }

        Vec3 velocity = this.getDeltaMovement();
        if (this.mucusTrailCooldown > 0
                || !this.isAlive()
                || !this.onGround()
                || this.isInWaterOrBubble()
                || this.isPassenger()
                || velocity.horizontalDistanceSqr() < 0.0025D) {
            return;
        }

        Vec3 currentPosition = this.position();
        if (this.lastMucusTrailPosition != null
                && this.lastMucusTrailPosition.distanceToSqr(currentPosition) < MUCUS_TRAIL_MIN_DISTANCE_SQR) {
            return;
        }

        this.lastMucusTrailPosition = currentPosition;
        this.mucusTrailCooldown = MUCUS_TRAIL_INTERVAL;

        Vec3 horizontalDirection = new Vec3(velocity.x, 0.0D, velocity.z).normalize();
        BlockPos trailPos = BlockPos.containing(
                this.getX() - horizontalDirection.x * 0.45D,
                this.getBoundingBox().minY + 0.01D,
                this.getZ() - horizontalDirection.z * 0.45D
        );
        boolean placed = this.placeFloorMucus(trailPos);

        if (placed && this.random.nextFloat() < 0.35F) {
            Vec3 side = new Vec3(-horizontalDirection.z, 0.0D, horizontalDirection.x);
            double sideOffset = this.random.nextBoolean() ? 0.55D : -0.55D;
            this.placeFloorMucus(BlockPos.containing(
                    trailPos.getX() + 0.5D + side.x * sideOffset,
                    trailPos.getY(),
                    trailPos.getZ() + 0.5D + side.z * sideOffset
            ));
        }

    }

    private boolean placeFloorMucus(BlockPos pos) {
        Optional<Block> mucus = BuiltInRegistries.BLOCK.getOptional(MUCUS_ID);
        if (mucus.isEmpty() || !(mucus.get() instanceof MultifaceBlock mucusBlock)) {
            return false;
        }

        BlockState existing = this.level().getBlockState(pos);
        var floorFace = MultifaceBlock.getFaceProperty(Direction.DOWN);
        if (existing.is(mucusBlock)) {
            if (existing.getValue(floorFace)) {
                return true;
            }
            BlockState merged = existing.setValue(floorFace, true);
            if (merged.canSurvive(this.level(), pos)) {
                this.level().setBlock(pos, merged, Block.UPDATE_ALL);
                return true;
            }
            return false;
        }

        if (!existing.isAir()) {
            return false;
        }

        BlockPos supportPos = pos.below();
        BlockState support = this.level().getBlockState(supportPos);
        if (!support.isFaceSturdy(this.level(), supportPos, Direction.UP)) {
            return false;
        }

        BlockState mucusState = mucusBlock.defaultBlockState().setValue(floorFace, true);
        if (!mucusState.canSurvive(this.level(), pos)) {
            return false;
        }

        this.level().setBlock(pos, mucusState, Block.UPDATE_ALL);
        return true;
    }

    private void updateAnimationState() {
        if (this.attackAnimationTicks > 0) {
            this.entityData.set(ANIMATION_STATE, ANIM_ATTACK);
            return;
        }
        Vec3 velocity = this.getDeltaMovement();
        if (this.getTarget() != null || velocity.horizontalDistanceSqr() > 1.0E-4D || this.walkAnimation.speed() > 0.03F) {
            this.entityData.set(ANIMATION_STATE, ANIM_CRAWL);
            return;
        }
        this.entityData.set(ANIMATION_STATE, ANIM_INGROUND);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.MOLEWORM_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.MOLEWORM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.MOLEWORM_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(AntarchySoundEvents.MOLEWORM_DIG.get(), 0.15F, 0.9F + this.random.nextFloat() * 0.2F);
    }
}
