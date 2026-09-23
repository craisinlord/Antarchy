package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamController;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamSettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamTerrainMode;
import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class RoyalMountEntity extends TamableAnimal implements GeoEntity, FlyingAnimal {
    private static final ResourceLocation GROWTH_HEALTH_MODIFIER = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_mount_growth_health");
    public static final int ANIM_IDLE = 0;
    public static final int ANIM_WALK = 1;
    public static final int ANIM_FLY = 2;
    public static final int ANIM_BITE = 3;
    public static final int ANIM_FLY_BITE = 4;
    public static final int ANIM_SHOOT = 5;
    public static final int ANIM_FLY_SHOOT = 6;

    private static final EntityDataAccessor<Integer> ANIM_STATE =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLYING =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SADDLED =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BEAM_ACTIVE =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BEAM_HEAD =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_BEAM_END =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BEAM_END_X =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_END_Y =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_END_Z =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> GROWTH_PROGRESS =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> QUIRKING =
            SynchedEntityData.defineId(RoyalMountEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation QUIRK_ANIM = RawAnimation.begin().thenLoop("quirk");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation BITE_ANIM = RawAnimation.begin().then("bite", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation FLY_BITE_ANIM = RawAnimation.begin().then("fly_bite", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().then("shoot", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation FLY_SHOOT_ANIM = RawAnimation.begin().then("fly_shoot", Animation.LoopType.PLAY_ONCE);

    private static final int ACTION_TICKS = 12;
    private static final int BITE_HIT_TICK = 6;
    private static final int BEAM_WINDUP_TICKS = 24;
    private static final int BEAM_COOLDOWN_TICKS = 240;
    private static final int BEAM_DURATION_TICKS = 12;
    private static final int COMMAND_COOLDOWN_TICKS = 360;
    private static final int TIME_FIELD_COOLDOWN_TICKS = 260;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final MoveControl groundMoveControl;
    private final MoveControl flyingMoveControl;
    private final PathNavigation groundNavigation;
    private final PathNavigation flyingNavigation;

    private int actionTicks;
    private boolean actionHit;
    private int spitCooldown;
    private int quirkTimer;
    private int flapSoundTimer;
    private boolean riderAscendPressed;
    private boolean riderDescendPressed;
    private final RoyalBeamController beamController = new RoyalBeamController(this);
    private int beamWindupTicks;
    private int beamCooldownTicks;
    private int beamHead;
    private int commandCooldownTicks;
    private int timeFieldCooldownTicks;

    protected RoyalMountEntity(EntityType<? extends RoyalMountEntity> entityType, Level level) {
        super(entityType, level);
        this.groundMoveControl = new MoveControl(this);
        this.flyingMoveControl = new FlyingMoveControl(this, 20, true);
        this.groundNavigation = new GroundPathNavigation(this, level);
        FlyingPathNavigation flying = new FlyingPathNavigation(this, level);
        flying.setCanFloat(true);
        this.flyingNavigation = flying;
        this.moveControl = this.groundMoveControl;
        this.navigation = this.groundNavigation;
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createBaseAttributes(
            double health, double attackDamage, double movementSpeed, double flyingSpeed,
            double armor, double knockbackResistance, double followRange) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.MOVEMENT_SPEED, movementSpeed)
                .add(Attributes.FLYING_SPEED, flyingSpeed)
                .add(Attributes.FOLLOW_RANGE, followRange)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.STEP_HEIGHT, 1.5D);
    }

    protected abstract String geoName();

    protected abstract EntityType<? extends RoyalBoltEntity> boltType();

    protected abstract SoundEvent idleSound();

    protected abstract SoundEvent biteSound();

    protected abstract SoundEvent shootSound();

    protected abstract SoundEvent flySound();

    protected abstract SoundEvent stepSound();

    @Override
    protected SoundEvent getAmbientSound() {
        return this.idleSound();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.stepSound(), 0.16F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.2F);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new RoyalMountSpitGoal(this));
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.2D, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.getTicksUntilNextAttack() <= 0 && this.canPerformAttack(target)) {
                    this.resetAttackCooldown();
                    RoyalMountEntity.this.startBite();
                }
            }

            @Override
            public boolean canUse() {
                return !RoyalMountEntity.this.isVehicle() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new RoyalMountFollowOwnerGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return !RoyalMountEntity.this.isVehicle()
                        && !RoyalMountEntity.this.isOrderedToSit() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse();
            }
        }.setAlertOthers());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIM_STATE, ANIM_IDLE);
        builder.define(FLYING, false);
        builder.define(SADDLED, false);
        builder.define(BEAM_ACTIVE, false);
        builder.define(BEAM_HEAD, -1);
        builder.define(HAS_BEAM_END, false);
        builder.define(BEAM_END_X, 0.0F);
        builder.define(BEAM_END_Y, 0.0F);
        builder.define(BEAM_END_Z, 0.0F);
        builder.define(GROWTH_PROGRESS, 0.0F);
        builder.define(QUIRKING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Saddled", this.isSaddled());
        tag.putBoolean("Flying", this.isFlying());
        tag.putInt("BeamCooldown", this.beamCooldownTicks);
        tag.putInt("CommandCooldown", this.commandCooldownTicks);
        tag.putInt("TimeFieldCooldown", this.timeFieldCooldownTicks);
        tag.putFloat("GrowthProgress", this.getGrowthProgress());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSaddled(tag.getBoolean("Saddled"));
        this.setFlying(tag.getBoolean("Flying"));
        this.actionTicks = 0;
        this.actionHit = false;
        this.spitCooldown = 0;
        this.beamCooldownTicks = Math.max(0, tag.getInt("BeamCooldown"));
        this.commandCooldownTicks = Math.max(0, tag.getInt("CommandCooldown"));
        this.timeFieldCooldownTicks = Math.max(0, tag.getInt("TimeFieldCooldown"));
        this.setGrowthProgress(tag.contains("GrowthProgress") ? tag.getFloat("GrowthProgress") : 0.0F);
    }

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FLYING.equals(key) && this.groundMoveControl != null) {
            boolean flying = this.entityData.get(FLYING);
            this.moveControl = flying ? this.flyingMoveControl : this.groundMoveControl;
            this.navigation = flying ? this.flyingNavigation : this.groundNavigation;
            this.setNoGravity(flying);
        }
        if (GROWTH_PROGRESS.equals(key)) {
            this.refreshDimensions();
        }
    }

    public void setFlying(boolean flying) {
        if (this.entityData.get(FLYING) != flying) {
            this.entityData.set(FLYING, flying);
            this.moveControl = flying ? this.flyingMoveControl : this.groundMoveControl;
            this.navigation = flying ? this.flyingNavigation : this.groundNavigation;
            this.setNoGravity(flying);
            if (flying && !this.level().isClientSide) {
                this.playSound(this.flySound(), 1.0F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.2F);
            }
        }
    }

    public boolean isSaddled() {
        return this.entityData.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(SADDLED, saddled);
    }

    public int getAnimState() {
        return this.entityData.get(ANIM_STATE);
    }

    private void setAnimState(int state) {
        this.entityData.set(ANIM_STATE, state);
    }

    public float getGrowthScale() {
        return 1.0F + 14.0F * this.getGrowthProgress();
    }

    public float getGrowthProgress() {
        return this.entityData.get(GROWTH_PROGRESS);
    }

    public void setGrowthProgress(float progress) {
        float clamped = Mth.clamp(progress, 0.0F, 1.0F);
        if (this.getGrowthProgress() != clamped) {
            float oldHealth = this.getHealth();
            this.entityData.set(GROWTH_PROGRESS, clamped);
            var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(GROWTH_HEALTH_MODIFIER);
                if (clamped > 0.0F) {
                    maxHealth.addTransientModifier(new AttributeModifier(GROWTH_HEALTH_MODIFIER,
                            this.getGrowthScale() - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
                this.setHealth(Math.min(oldHealth, this.getMaxHealth()));
            }
            this.refreshDimensions();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && this.hasPassenger(attacker)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(this.getGrowthScale());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(AntarchyTags.Items.ROYAL_MOUNT_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.GOLDEN_APPLE) && !this.isTame()) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.tame(player);
                this.heal(8.0F);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE) && this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(8.0F);
                this.setGrowthProgress(this.getGrowthProgress() + (1.0F - this.getGrowthProgress()) * 0.1F);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isFood(stack)) {
            if (!this.level().isClientSide && this.isTame() && this.isOwnedBy(player) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(8.0F);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            if (!this.isSaddled() && stack.is(Items.SADDLE)) {
                if (!this.level().isClientSide) {
                    this.setSaddled(true);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.isEmpty() && player.isSecondaryUseActive()) {
                if (!this.level().isClientSide) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.setInSittingPose(this.isOrderedToSit());
                    this.navigation.stop();
                    this.setTarget(null);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.isEmpty() && !player.isPassenger()) {
                if (!this.level().isClientSide) {
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public boolean canMate(net.minecraft.world.entity.animal.Animal other) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null && !this.canAttack(target)) {
            this.setTarget(null);
        }

        if (this.spitCooldown > 0) {
            this.spitCooldown--;
        }
        this.tickRoyalAbilities();

        if ((this.isFlying() || !this.onGround()) && --this.flapSoundTimer <= 0) {
            this.flapSoundTimer = 24 + this.random.nextInt(12);
            this.playSound(this.flySound(), 0.6F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.3F);
        }

        if (this.actionTicks > 0) {
            this.entityData.set(QUIRKING, false);
            this.actionTicks--;
            if (!this.actionHit && this.actionTicks <= ACTION_TICKS - BITE_HIT_TICK
                    && (this.getAnimState() == ANIM_BITE || this.getAnimState() == ANIM_FLY_BITE)) {
                this.actionHit = true;
                this.applyBiteDamage();
            }
            if (this.actionTicks == 0) {
                this.updateAmbientAnimState();
            }
            return;
        }

        this.tickFlightState();
        this.updateAmbientAnimState();

        if (this.quirkTimer > 0) {
            this.quirkTimer--;
        }
        if (this.getAnimState() == ANIM_IDLE && this.quirkTimer == 0 && this.random.nextInt(600) == 0) {
            this.quirkTimer = 80;
        }
        this.entityData.set(QUIRKING, this.quirkTimer > 0 && this.getAnimState() == ANIM_IDLE);
    }

    private void tickRoyalAbilities() {
        if (this.beamCooldownTicks > 0) this.beamCooldownTicks--;
        if (this.commandCooldownTicks > 0) this.commandCooldownTicks--;
        if (this.timeFieldCooldownTicks > 0) this.timeFieldCooldownTicks--;
        if (this.beamWindupTicks > 0) {
            this.beamWindupTicks--;
            if (this.beamWindupTicks == 0) {
                this.beamController.start(this.beamShootFrom(), this.beamTargetPosition(), BEAM_DURATION_TICKS);
                this.entityData.set(BEAM_ACTIVE, true);
            }
        }
        if (this.beamController.isFiring()) {
            this.beamController.tick(this.beamShootFrom(), this.getTarget(), this.beamSettings(), RoyalBeamTerrainMode.NONE, this::setBeamEndPosition);
            if (!this.beamController.isFiring()) {
                this.entityData.set(BEAM_ACTIVE, false);
                this.entityData.set(BEAM_HEAD, -1);
                this.beamCooldownTicks = BEAM_COOLDOWN_TICKS;
            }
            return;
        }
        if (this.isOrderedToSit()) return;
        LivingEntity target = this.getTarget();
        if (target == null && this.getControllingPassenger() instanceof Player) {
            target = this.frontTarget();
            if (target != null) this.setTarget(target);
        }
        if (target == null || !target.isAlive() || !this.hasLineOfSight(target)) return;
        double targetDistance = this.distanceTo(target);
        if (this.beamCooldownTicks <= 0 && this.beamWindupTicks <= 0 && targetDistance > 7.0D && targetDistance < 28.0D && this.tickCount % 20 == 0) {
            this.beamHead = (this.beamHead + 1) % 3;
            this.entityData.set(BEAM_HEAD, this.beamHead);
            this.beamWindupTicks = BEAM_WINDUP_TICKS;
            this.setAnimState(this.isFlying() ? ANIM_FLY_SHOOT : ANIM_SHOOT);
            this.triggerAnim("main", this.isFlying() ? "fly_shoot" : "shoot");
            this.playSound(this.shootSound(), 0.9F, 0.75F);
        }
        if (this instanceof PrinceEntity && this.commandCooldownTicks <= 0 && this.tickCount % 20 == 0) {
            this.performPrinceCommand();
        }
        if (this instanceof PrincessEntity && this.timeFieldCooldownTicks <= 0 && this.tickCount % 20 == 0) {
            this.performPrincessTimeField(target);
        }
    }

    private void performPrinceCommand() {
        if (!(this.level() instanceof ServerLevel level) || RoyalEffectHooks.commandedHolder() == null) return;
        int applied = 0;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(16.0D), mob ->
                mob != this && mob.isAlive() && mob instanceof Enemy && !mob.isAlliedTo(this)
                        && !(mob instanceof RoyalBossEntity) && RoyalEffectEligibility.canApplyCommanded(mob))) {
            if (applied >= 5) break;
            if (mob.addEffect(new MobEffectInstance(RoyalEffectHooks.commandedHolder(), 300), this)) {
                applied++;
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.02D);
            }
        }
        if (applied > 0) {
            this.commandCooldownTicks = COMMAND_COOLDOWN_TICKS;
            this.playSound(this.shootSound(), 1.0F, 0.65F);
        }
    }

    private void performPrincessTimeField(@Nullable LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) return;
        Vec3 center = target != null && target.isAlive() ? target.position() : this.position();
        TimeDilationApi.createField(level, center, 8.0D, 0.45D, 100, this.getUUID());
        this.timeFieldCooldownTicks = TIME_FIELD_COOLDOWN_TICKS;
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.5D, center.z, 28, 1.2D, 0.6D, 1.2D, 0.03D);
    }

    private RoyalBeamSettings beamSettings() {
        float damage = this instanceof PrincessEntity ? 10.0F : 12.0F;
        return new RoyalBeamSettings(28.0D, 0.08D, 4.0D, BEAM_DURATION_TICKS, 6, BEAM_COOLDOWN_TICKS,
                0.35F, 0.45F, damage, 0.45F, 3, 0.0D, 0.0F, 0.0F, 0, 0.0F, 0.0F, 0.0F, false, true);
    }

    private Vec3 beamTargetPosition() {
        LivingEntity target = this.getTarget();
        return target != null ? target.getEyePosition() : this.getEyePosition().add(this.getLookAngle().scale(20.0D));
    }

    public Vec3 beamShootFrom() {
        float scale = this.getGrowthScale();
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        return this.position().add(0.0D, 1.0D * scale, 0.0D).add(forward.scale(1.05D * scale)).add(side.scale((this.getBeamHead() - 1) * 0.72D * scale));
    }

    public int getBeamHead() { return this.entityData.get(BEAM_HEAD); }
    public boolean isFiringBeam() { return this.entityData.get(BEAM_ACTIVE); }
    public Vec3 getBeamEndPosition() {
        return this.entityData.get(HAS_BEAM_END) ? new Vec3(this.entityData.get(BEAM_END_X), this.entityData.get(BEAM_END_Y), this.entityData.get(BEAM_END_Z)) : null;
    }
    public RoyalBeamElement getBeamElement() { return RoyalBeamElement.GENERIC; }
    public RoyalBeamController beamController() { return this.beamController; }
    private void setBeamEndPosition(@Nullable Vec3 position) {
        this.entityData.set(HAS_BEAM_END, position != null);
        if (position != null) {
            this.entityData.set(BEAM_END_X, (float) position.x);
            this.entityData.set(BEAM_END_Y, (float) position.y);
            this.entityData.set(BEAM_END_Z, (float) position.z);
        }
    }

    private void tickFlightState() {
        if (this.isVehicle()) {
            return;
        }
        if (this.isFlying() && this.onGround() && this.getTarget() == null) {
            this.setFlying(false);
        }
    }

    private void updateAmbientAnimState() {
        if (this.actionTicks > 0) {
            return;
        }
        if (this.isFlying() || !this.onGround()) {
            this.setAnimState(ANIM_FLY);
            return;
        }
        boolean moving;
        if (this.getControllingPassenger() instanceof Player rider) {
            moving = Math.abs(rider.zza) > 0.05F || Math.abs(rider.xxa) > 0.05F;
        } else {
            moving = this.getDeltaMovement().horizontalDistanceSqr() > 0.0025D;
        }
        this.setAnimState(moving ? ANIM_WALK : ANIM_IDLE);
    }

    public void startBite() {
        if (this.actionTicks > 0) {
            return;
        }
        this.actionTicks = ACTION_TICKS;
        this.actionHit = false;
        this.setAnimState(this.isFlying() ? ANIM_FLY_BITE : ANIM_BITE);
        this.triggerAnim("main", this.isFlying() ? "fly_bite" : "bite");
        this.playSound(this.biteSound(), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    private void applyBiteDamage() {
        LivingEntity target = this.getTarget();
        if (target == null && this.getControllingPassenger() instanceof Player) {
            target = this.frontTarget();
        }
        if (target != null && this.canAttack(target) && target.distanceTo(this) < 5.0D) {
            target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof RoyalMountEntity other && this.isTame() && other.isTame())
                && super.canAttack(target);
    }

    @Nullable
    private LivingEntity frontTarget() {
        Vec3 look = this.getLookAngle();
        Vec3 eye = this.getEyePosition();
        LivingEntity best = null;
        double bestDot = 0.3D;
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D))) {
            if (candidate == this || candidate == this.getControllingPassenger() || candidate == this.getOwner()) {
                continue;
            }
            if (!this.canAttack(candidate)) {
                continue;
            }
            Vec3 to = candidate.getEyePosition().subtract(eye);
            double dist = to.length();
            if (dist > 6.0D || dist < 1.0E-3D) {
                continue;
            }
            double dot = to.scale(1.0D / dist).dot(look);
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }
        return best;
    }

    public void startSpitVolley(@Nullable LivingEntity target) {
        if (this.spitCooldown > 0 || this.level().isClientSide) {
            return;
        }
        this.spitCooldown = 40;
        this.actionTicks = ACTION_TICKS;
        this.actionHit = true;
        this.setAnimState(this.isFlying() ? ANIM_FLY_SHOOT : ANIM_SHOOT);
        this.triggerAnim("main", this.isFlying() ? "fly_shoot" : "shoot");

        Vec3 aim;
        if (target != null) {
            aim = target.getEyePosition().subtract(this.getEyePosition()).normalize();
        } else if (this.getControllingPassenger() instanceof Player rider) {
            aim = rider.getLookAngle();
        } else {
            aim = this.getLookAngle();
        }

        Vec3 right = new Vec3(-aim.z, 0.0D, aim.x).normalize();
        for (int i = -1; i <= 1; i++) {
            Vec3 dir = aim.add(right.scale(i * 0.14D)).normalize();
            RoyalBoltEntity bolt = new RoyalBoltEntity(this.boltType(), this.level());
            bolt.setOwner(this);
            bolt.setElement(this.boltElement(i));
            Vec3 spawn = this.getEyePosition().add(aim.scale(1.6D)).add(right.scale(i * 0.9D));
            bolt.setPos(spawn.x, spawn.y, spawn.z);
            bolt.shoot(dir.x, dir.y, dir.z, 1.4F, 1.0F);
            this.level().addFreshEntity(bolt);
        }
        this.playSound(this.shootSound(), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    protected RoyalBeamElement boltElement(int lateralIndex) {
        return RoyalBeamElement.GENERIC;
    }

    public boolean toggleMountedFlight(ServerPlayer player) {
        if (!this.canControl(player)) {
            return false;
        }
        this.setFlying(!this.isFlying());
        if (this.isFlying()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.4D, 0.0D));
            this.hasImpulse = true;
        }
        return true;
    }

    public void handleMountedBite(ServerPlayer player) {
        if (this.canControl(player)) {
            this.startBite();
        }
    }

    public void handleMountedSpit(ServerPlayer player) {
        if (this.canControl(player)) {
            this.startSpitVolley(null);
        }
    }

    public void setRiderAscend(boolean pressed) {
        this.riderAscendPressed = pressed;
        if (pressed && !this.isFlying() && this.getControllingPassenger() != null) {
            this.setFlying(true);
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.35D, 0.0D));
            this.hasImpulse = true;
        }
    }

    public void setRiderDescend(boolean pressed) {
        this.riderDescendPressed = pressed;
    }

    private boolean canControl(ServerPlayer player) {
        return this.isTame() && this.isOwnedBy(player) && this.getControllingPassenger() == player;
    }

    public AABB assistedMountBox() {
        float scale = this.getGrowthScale();
        double half = Math.max(this.getBbWidth() * 0.5D, 2.2D * scale);
        double height = Math.max(this.getBbHeight(), 1.4D * scale);
        return new AABB(this.getX() - half, this.getY(), this.getZ() - half,
                this.getX() + half, this.getY() + height, this.getZ() + half);
    }

    @Nullable
    public static RoyalMountEntity findAssistedMountTarget(Player player, double extraReach) {
        if (player.isPassenger() || player.isSpectator() || !player.getMainHandItem().isEmpty()) {
            return null;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.entityInteractionRange() + extraReach));
        RoyalMountEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (RoyalMountEntity mount : player.level().getEntitiesOfClass(RoyalMountEntity.class, player.getBoundingBox().inflate(80.0D),
                mount -> mount.isAlive() && mount.isTame() && mount.isOwnedBy(player) && !mount.isVehicle())) {
            AABB box = mount.assistedMountBox();
            double distance;
            if (box.contains(eye)) {
                distance = 0.0D;
            } else {
                var hit = box.clip(eye, end);
                if (hit.isEmpty()) {
                    continue;
                }
                distance = hit.get().distanceToSqr(eye);
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                best = mount;
            }
        }
        return best;
    }

    public static boolean tryAssistedMount(ServerPlayer player) {
        RoyalMountEntity mount = findAssistedMountTarget(player, 2.0D);
        if (mount == null) {
            return false;
        }
        mount.setOrderedToSit(false);
        mount.setInSittingPose(false);
        return player.startRiding(mount);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player player && this.isOwnedBy(player) ? player : null;
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        float scale = this.getGrowthScale();
        double yOffset = 1.3D * scale;
        double backDistance = 0.35D * scale;
        Vec3 back = Vec3.directionFromRotation(0.0F, this.getYRot()).scale(-backDistance);
        moveFunction.accept(passenger, this.getX() + back.x, this.getY() + yOffset, this.getZ() + back.z);
    }

    @Override
    protected Vec3 getRiddenInput(Player rider, Vec3 travelVector) {
        double vertical = 0.0D;
        if (this.isFlying()) {
            if (this.riderAscendPressed) {
                vertical += 0.4D;
            }
            if (this.riderDescendPressed || rider.isSecondaryUseActive()) {
                vertical -= 0.4D;
            }
            if (rider.zza > 0.0F) {
                vertical -= Math.sin(Math.toRadians(rider.getXRot())) * 0.6D;
            }
        }
        return new Vec3(rider.xxa * 0.35D, vertical, rider.zza);
    }

    @Override
    protected float getRiddenSpeed(Player rider) {
        return (float) this.getAttributeValue(this.isFlying() ? Attributes.FLYING_SPEED : Attributes.MOVEMENT_SPEED)
                * (this.isFlying() ? 0.4F : 1.0F);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (!(this.getControllingPassenger() instanceof Player rider) || !this.isVehicle()) {
            super.travel(travelVector);
            return;
        }

        this.setYRot(rider.getYRot());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
        this.setXRot(rider.getXRot() * 0.5F);

        Vec3 input = this.getRiddenInput(rider, travelVector);
        this.setSpeed(this.getRiddenSpeed(rider));

        if (this.isFlying()) {
            this.moveRelative(this.getSpeed(), new Vec3(input.x, 0.0D, input.z));
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, (motion.y + input.y) * 0.9D, motion.z);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            this.fallDistance = 0.0F;
        } else {
            super.travel(new Vec3(input.x, travelVector.y, input.z));
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public float maxUpStep() {
        return 1.5F;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, this::animController)
                .triggerableAnim("bite", BITE_ANIM)
                .triggerableAnim("fly_bite", FLY_BITE_ANIM)
                .triggerableAnim("shoot", SHOOT_ANIM)
                .triggerableAnim("fly_shoot", FLY_SHOOT_ANIM));
    }

    private PlayState animController(AnimationState<RoyalMountEntity> state) {
        return switch (this.getAnimState()) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_FLY, ANIM_FLY_BITE, ANIM_FLY_SHOOT -> state.setAndContinue(FLY_ANIM);
            default -> state.setAndContinue(this.entityData.get(QUIRKING) ? QUIRK_ANIM : IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public String geoNameForRender() {
        return this.geoName();
    }
}
