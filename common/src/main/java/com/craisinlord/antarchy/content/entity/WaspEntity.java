package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.block.entity.WaspNestBlockEntity;
import java.util.EnumSet;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
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

public class WaspEntity extends Monster implements GeoEntity, FlyingAnimal {
    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT =
            SynchedEntityData.defineId(WaspEntity.class, EntityDataSerializers.INT);
    private static final byte ATTACK_ANIM_EVENT = 4;
    private static final int ATTACK_ANIM_TICKS = 10;
    private static final int POISON_DURATION_TICKS = 100;
    private static final double CHASE_SPEED = 0.36D;
    private static final double WANDER_SPEED = 0.34D;
    private static final String TEXTURE_VARIANT_KEY = "TextureVariant";
    private static final int TEXTURE_VARIANT_DEFAULT = 0;
    private static final int TEXTURE_VARIANT_PANDA = 1;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int attackAnimTicks;
    private boolean configuredAttributesSynced;
    @Nullable private BlockPos hivePos;

    public WaspEntity(EntityType<? extends WaspEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 30, true);
        this.setNoGravity(true);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.waspHealth())
                .add(Attributes.FLYING_SPEED, AntarchySettings.waspMovementSpeed())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.waspMovementSpeed())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.waspAttackDamage())
                .add(Attributes.ARMOR, 1.0D);
    }

    public static boolean canSpawn(EntityType<WaspEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE_VARIANT, TEXTURE_VARIANT_DEFAULT);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, CHASE_SPEED, true));
        this.goalSelector.addGoal(3, new WaspGoToNestGoal());
        this.goalSelector.addGoal(4, new WaspWanderGoal());
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            } else {
                this.moveRelative(this.getSpeed(), travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            }
        }
        this.calculateEntityAnimation(false);
    }

    public void setHivePos(@Nullable BlockPos pos) {
        this.hivePos = pos;
    }

    @Nullable
    public BlockPos getHivePos() {
        return this.hivePos;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.hivePos != null) {
            tag.put("HivePos", NbtUtils.writeBlockPos(this.hivePos));
        }
        tag.putInt(TEXTURE_VARIANT_KEY, this.getTextureVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hivePos = tag.contains("HivePos")
                ? NbtUtils.readBlockPos(tag, "HivePos").orElse(null)
                : null;
        if (tag.contains(TEXTURE_VARIANT_KEY)) {
            this.setTextureVariant(tag.getInt(TEXTURE_VARIANT_KEY));
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.realignHitbox();
        if (!this.level().isClientSide && !this.configuredAttributesSynced) {
            this.syncConfiguredAttributes();
            this.configuredAttributesSynced = true;
        }
        this.setNoGravity(true);
        this.setOnGround(false);

        if (this.attackAnimTicks > 0) {
            this.attackAnimTicks--;
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.syncConfiguredAttributes();
        this.configuredAttributesSynced = true;
        this.setHealth((float) AntarchySettings.waspHealth());
        if (spawnReason == MobSpawnType.SPAWN_EGG && this.random.nextInt(20) == 0) {
            this.setPandaVariant(true);
        }
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    public void refreshDimensions() {
        super.refreshDimensions();
        this.realignHitbox();
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return super.doHurtTarget(target);
        }

        this.beginAttackAnimation();
        boolean hurt = livingTarget.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hurt) {
            int poisonDuration = this.level().getDifficulty() == Difficulty.HARD
                    ? POISON_DURATION_TICKS * 2
                    : POISON_DURATION_TICKS;
            livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 0));
            this.playSound(AntarchySoundEvents.WASP_ATTACK.get(), 0.9F, 0.9F + this.random.nextFloat() * 0.2F);
        }

        return hurt;
    }

    private void beginAttackAnimation() {
        this.attackAnimTicks = ATTACK_ANIM_TICKS;
        this.swing(InteractionHand.MAIN_HAND);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, ATTACK_ANIM_EVENT);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.WASP_IDLE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 20;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.WASP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.WASP_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }

    @Override
    public float getVoicePitch() {
        return 1.15F + this.random.nextFloat() * 0.15F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private PlayState mainAnimController(AnimationState<WaspEntity> state) {
        if (this.attackAnimTicks > 0) {
            return state.setAndContinue(ATTACK_ANIM);
        }

        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == ATTACK_ANIM_EVENT) {
            this.attackAnimTicks = ATTACK_ANIM_TICKS;
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0D, 0.2D, 0.0D);
    }

    public boolean isPandaVariant() {
        return this.getTextureVariant() == TEXTURE_VARIANT_PANDA;
    }

    public int getTextureVariant() {
        return this.entityData.get(TEXTURE_VARIANT);
    }

    public void setTextureVariant(int variant) {
        this.entityData.set(TEXTURE_VARIANT, Mth.clamp(variant, TEXTURE_VARIANT_DEFAULT, TEXTURE_VARIANT_PANDA));
    }

    public void setPandaVariant(boolean pandaVariant) {
        this.setTextureVariant(pandaVariant ? TEXTURE_VARIANT_PANDA : TEXTURE_VARIANT_DEFAULT);
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    private void realignHitbox() {
        double halfWidth = this.getBbWidth() * 0.5D;
        double height = this.getBbHeight();
        this.setBoundingBox(new AABB(
                this.getX() - halfWidth,
                this.getY(),
                this.getZ() - halfWidth,
                this.getX() + halfWidth,
                this.getY() + height,
                this.getZ() + halfWidth
        ));
    }

    private void syncConfiguredAttributes() {
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(AntarchySettings.waspHealth());
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(AntarchySettings.waspAttackDamage());
        Objects.requireNonNull(this.getAttribute(Attributes.FLYING_SPEED)).setBaseValue(AntarchySettings.waspMovementSpeed());
        Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(AntarchySettings.waspMovementSpeed());
    }

    private boolean hasValidHive() {
        if (this.hivePos == null || !this.level().isLoaded(this.hivePos)) {
            return false;
        }

        BlockEntity blockEntity = this.level().getBlockEntity(this.hivePos);
        return blockEntity instanceof WaspNestBlockEntity;
    }

    private boolean closerThan(BlockPos pos, int distance) {
        return pos.closerThan(this.blockPosition(), distance);
    }

    private final class WaspWanderGoal extends Goal {
        private static final int WANDER_THRESHOLD = 22;

        private WaspWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            boolean hasHive = WaspEntity.this.hasValidHive();
            return WaspEntity.this.getTarget() == null
                    && (!hasHive || (!WaspEntity.this.level().isNight() && !WaspEntity.this.level().isRaining()))
                    && WaspEntity.this.navigation.isDone()
                    && WaspEntity.this.random.nextInt(10) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return WaspEntity.this.navigation.isInProgress();
        }

        @Override
        public void start() {
            Vec3 targetPos = this.findPos();
            if (targetPos != null) {
                WaspEntity.this.navigation.moveTo(
                        WaspEntity.this.navigation.createPath(BlockPos.containing(targetPos), 1),
                        WANDER_SPEED
                );
            }
        }

        @Nullable
        private Vec3 findPos() {
            Vec3 bias;
            if (WaspEntity.this.hasValidHive() && !WaspEntity.this.closerThan(WaspEntity.this.hivePos, WANDER_THRESHOLD)) {
                bias = Vec3.atCenterOf(WaspEntity.this.hivePos).subtract(WaspEntity.this.position()).normalize();
            } else {
                bias = WaspEntity.this.getViewVector(0.0F);
            }

            Vec3 hover = HoverRandomPos.getPos(
                    WaspEntity.this,
                    8,
                    7,
                    bias.x,
                    bias.z,
                    (float) Math.PI / 2.0F,
                    3,
                    1
            );
            if (hover != null) {
                return hover;
            }

            return AirAndWaterRandomPos.getPos(
                    WaspEntity.this,
                    8,
                    4,
                    -2,
                    bias.x,
                    bias.z,
                    (double) ((float) Math.PI / 2.0F)
            );
        }
    }

    private class WaspGoToNestGoal extends Goal {
        private static final int TIMEOUT_TICKS = 600;
        private static final double NEST_REACH_DISTANCE_SQ = 4.0;
        private static final double MAX_HIVE_DISTANCE_SQ = 2048;
        private int travelTicks;

        WaspGoToNestGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            BlockPos hive = WaspEntity.this.hivePos;
            return hive != null
                    && WaspEntity.this.getTarget() == null
                    && (WaspEntity.this.level().isNight() || WaspEntity.this.level().isRaining())
                    && hive.distSqr(WaspEntity.this.blockPosition()) <= MAX_HIVE_DISTANCE_SQ;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && this.travelTicks < TIMEOUT_TICKS;
        }

        @Override
        public void start() {
            this.travelTicks = 0;
            BlockPos hive = WaspEntity.this.hivePos;
            WaspEntity.this.navigation.moveTo(hive.getX() + 0.5, hive.getY() + 0.5, hive.getZ() + 0.5, WANDER_SPEED);
        }

        @Override
        public void tick() {
            this.travelTicks++;
            BlockPos hive = WaspEntity.this.hivePos;
            if (hive == null) {
                return;
            }

            if (WaspEntity.this.position().distanceToSqr(Vec3.atCenterOf(hive)) <= NEST_REACH_DISTANCE_SQ) {
                Level level = WaspEntity.this.level();
                if (level instanceof ServerLevel serverLevel) {
                    BlockEntity be = serverLevel.getBlockEntity(hive);
                    if (be instanceof WaspNestBlockEntity nest) {
                        nest.tryStoreWasp(WaspEntity.this);
                    } else {
                        WaspEntity.this.hivePos = null;
                    }
                }
            }
        }
    }
}
