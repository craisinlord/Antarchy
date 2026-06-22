package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MantisEntity extends Monster implements GeoEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(MantisEntity.class);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation FLY_ATTACK_ANIM = RawAnimation.begin().thenPlay("flyattack");

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_FLY = 2;
    private static final int ANIM_ATTACK = 3;
    private static final int ANIM_FLY_ATTACK = 4;
    private static final double WANDER_SPEED = 0.48D;
    private static final double COMBAT_SPEED = 0.75D;

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> ANIMATION_STATE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(MantisEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int attackAnimationTicks;
    private int flyBurstTicks;
    private int flyCooldownTicks;
    private int debugLogTicks = 0;

    public MantisEntity(EntityType<? extends MantisEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.LEAVES, -1.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.mantisHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.mantisAttackDamage())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.mantisMovementSpeed())
                .add(Attributes.FLYING_SPEED, AntarchySettings.mantisFlyingSpeed())
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    public static boolean canSpawn(EntityType<MantisEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (spawnReason != MobSpawnType.NATURAL) {
            return false;
        }

        boolean atSurface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() <= pos.getY();
        boolean posOpen = level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above()) && level.getFluidState(pos).isEmpty();
        boolean onSolidGround = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                && !(level.getBlockState(pos.below()).getBlock() instanceof LeavesBlock);

        if (!posOpen || !onSolidGround) {
            return false;
        }

        if (!hasNearbyMantisCapacity(level, pos)) {
            return false;
        }

        boolean ignoreLightLevel = AntarchySettings.mantisIgnoreLightLevel();

        if (level.getLevel().isDay() && level.getBiome(pos).is(AntarchyTags.Biomes.MANTIS_OVERWORLD_SPAWN_BIOMES)) {
            return atSurface && level.canSeeSky(pos) && (ignoreLightLevel || Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random));
        }

        // Elythia meadow (and any future mantis_spawn_biomes): nighttime, no sky-access requirement
        if (level.getLevel().isNight() && level.getBiome(pos).is(AntarchyTags.Biomes.MANTIS_SPAWN_BIOMES)) {
            return atSurface && (ignoreLightLevel || Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random));
        }

        return false;
    }

    private static boolean hasNearbyMantisCapacity(ServerLevelAccessor level, BlockPos pos) {
        return level.getLevel().getEntitiesOfClass(
                MantisEntity.class,
                new AABB(pos).inflate(64.0D),
                Entity::isAlive
        ).size() < 3;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, COMBAT_SPEED, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, WANDER_SPEED));

        HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this);
        this.targetSelector.addGoal(1, hurtByTargetGoal);
        // randomInterval lowered from default 10 to 5 for faster re-acquisition
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 5, true, false, null));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanPassDoors(true);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<MantisEntity> state) {
        return switch (this.getAnimationState()) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_FLY -> state.setAndContinue(FLY_ANIM);
            case ANIM_ATTACK -> state.setAndContinue(ATTACK_ANIM);
            case ANIM_FLY_ATTACK -> state.setAndContinue(FLY_ATTACK_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.attackAnimationTicks > 0) {
                this.attackAnimationTicks--;
            }

            if (this.flyBurstTicks > 0) {
                this.flyBurstTicks--;
                if (this.flyBurstTicks % 8 == 0) {
                    this.playSound(AntarchySoundEvents.MANTIS_FLY_LOOP.get(), 0.45F, 0.95F + this.random.nextFloat() * 0.1F);
                }
            }

            if (this.flyCooldownTicks > 0) {
                this.flyCooldownTicks--;
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            } else if (this.flyBurstTicks <= 0 && this.flyCooldownTicks <= 0 && this.onGround() && this.random.nextInt(180) == 0) {
                this.startFlightBurst();
            }

            this.setNoGravity(this.isFlyingNow());
            this.updateAnimationState();
            this.tickDebugLog();
        }

        this.updateFlightRotation();
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.attackAnimationTicks = 10;
            this.playSound(AntarchySoundEvents.MANTIS_ATTACK.get(), 1.0F, 0.95F + this.random.nextFloat() * 0.1F);
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.MANTIS_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.MANTIS_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.MANTIS_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.1F;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public void setYBodyRot(float yBodyRot) {
        // Only sync yRot from body rotation during flight — on the ground this
        // conflicted with navigation and contributed to post-attack spinning
        if (this.isFlyingNow()) {
            this.setYRot(yBodyRot);
        }
        super.setYBodyRot(yBodyRot);
    }

    private void startFlightBurst() {
        this.flyBurstTicks = 40 + this.random.nextInt(30);
        this.flyCooldownTicks = 160 + this.random.nextInt(80);
        Vec3 lift = Vec3.directionFromRotation(0.0F, this.random.nextFloat() * 360.0F).scale(0.18D);
        this.setNoGravity(true);
        this.setDeltaMovement(this.getDeltaMovement().add(lift.x, 0.28D, lift.z));
        this.navigation.stop();
    }

    private int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private void updateAnimationState() {
        if (this.attackAnimationTicks > 0) {
            this.setAnimationState(this.isFlyingNow() ? ANIM_FLY_ATTACK : ANIM_ATTACK);
            return;
        }

        Vec3 velocity = this.getDeltaMovement();
        if (this.isFlyingNow() || (!this.onGround() && velocity.lengthSqr() > 1.0E-4D)) {
            this.setAnimationState(ANIM_FLY);
        } else if (this.onGround()) {
            this.setAnimationState(velocity.horizontalDistanceSqr() > 1.0E-4D ? ANIM_WALK : ANIM_IDLE);
        } else {
            this.setAnimationState(ANIM_IDLE);
        }
    }

    private void updateFlightRotation() {
        // Only override rotation during actual flight — on the ground this fought the
        // navigation system, causing the mantis to spin 180° after each attack
        if (!this.isFlyingNow()) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-5D) {
            return;
        }

        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        this.setYRot((float) (Math.atan2(motion.z, motion.x) * (180.0D / Math.PI)) - 90.0F);
        this.setXRot((float) (-(Math.atan2(motion.y, horizontal) * (180.0D / Math.PI))));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }


    private boolean isFlyingNow() {
        return this.flyBurstTicks > 0;
    }

    private void tickDebugLog() {
        if (++this.debugLogTicks < 100) {
            return;
        }
        this.debugLogTicks = 0;
        LivingEntity target = this.getTarget();
        int animState = this.getAnimationState();
        LOGGER.debug(
            "[Mantis id={}] pos={},{},{} target={} anim={} attackTicks={} flyBurst={} flyCooldown={} onGround={} navDone={}",
            this.getId(),
            (int) this.getX(), (int) this.getY(), (int) this.getZ(),
            target != null ? target.getName().getString() : "none",
            animState,
            this.attackAnimationTicks,
            this.flyBurstTicks,
            this.flyCooldownTicks,
            this.onGround(),
            this.getNavigation().isDone()
        );
    }




}



