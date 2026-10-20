package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.anthology.beam.BeamController;
import com.craisinlord.anthology.beam.BeamOwner;
import com.craisinlord.anthology.beam.BeamSettings;
import com.craisinlord.anthology.beam.BeamTerrainMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
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
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class RoyalMountEntity extends TamableAnimal implements GeoEntity, FlyingAnimal, BeamOwner {
    private static final ResourceLocation GROWTH_HEALTH_MODIFIER = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_mount_growth_health");
    private static final ResourceLocation GROWTH_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_mount_growth_damage");
    private static final ResourceLocation GROWTH_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_mount_growth_speed");
    private static final ResourceLocation GROWTH_FLYING_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_mount_growth_flying_speed");

    public static final int ANIM_IDLE = 0;
    public static final int ANIM_WALK = 1;
    public static final int ANIM_FLY = 2;
    public static final int ANIM_BITE = 3;
    public static final int ANIM_FLY_BITE = 4;
    public static final int ANIM_SHOOT = 5;
    public static final int ANIM_FLY_SHOOT = 6;

    public static final int ACTION_NONE = 0;
    public static final int ACTION_BITE = 1;
    public static final int ACTION_SPIT = 2;
    public static final int ACTION_BEAM = 3;
    public static final int ACTION_SIGNATURE = 4;

    public static final float MAX_SCALE = 5.0F;
    public static final float JUVENILE_PROGRESS = 0.25F;
    public static final float ADULT_PROGRESS = 0.75F;
    private static final float GOLDEN_APPLE_GROWTH = 0.025F;
    private static final float ENCHANTED_APPLE_GROWTH = 0.125F;

    private static final double SEAT_HEIGHT = 17.0D / 16.0D;
    private static final double SEAT_BACK = 2.0D / 16.0D;
    private static final double FLY_BOB_HEIGHT = 9.0D / 16.0D;

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

    private static final int BITE_TICKS = 12;
    private static final int BITE_HIT_TICK = 6;
    private static final int BITE_COOLDOWN_TICKS = 14;
    private static final int SPIT_TICKS = 20;
    private static final int SPIT_FIRE_TICK = 8;
    private static final int SPIT_COOLDOWN_TICKS = 50;
    private static final int BEAM_WINDUP_TICKS = 24;
    private static final int BEAM_DURATION_TICKS = 14;
    private static final int BEAM_RECOVERY_TICKS = 10;
    private static final int BEAM_COOLDOWN_TICKS = 240;
    private static final int SIGNATURE_WINDUP_TICKS = 20;
    private static final int SIGNATURE_TICKS = 30;
    private static final int ACTION_RECOVERY_TICKS = 8;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final MoveControl groundMoveControl;
    private final MoveControl flyingMoveControl;
    private final PathNavigation groundNavigation;
    private final PathNavigation flyingNavigation;
    private final BeamController beamController = new BeamController(this);

    private int action = ACTION_NONE;
    private int actionTicks;
    private boolean actionFired;
    private boolean actionRidden;
    @Nullable
    private LivingEntity actionTarget;
    @Nullable
    private Vec3 actionAimPoint;
    private int actionRecoveryTicks;
    private int lastAction = ACTION_NONE;
    private int biteCooldownTicks;
    private int spitCooldownTicks;
    private int beamCooldownTicks;
    private int signatureCooldownTicks;
    private int beamHead;

    private int quirkTimer;
    private int flapSoundTimer;
    private int flightClaimTick = -100;
    private int ownerAwayTicks;
    private boolean riderAscendPressed;
    private boolean riderDescendPressed;

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

    public enum Stage {
        HATCHLING("hatchling"),
        JUVENILE("juvenile"),
        ADULT("adult");

        private final String id;

        Stage(String id) {
            this.id = id;
        }

        public Component displayName() {
            return Component.translatable("entity.antarchy.royal_mount.stage." + this.id);
        }
    }

    protected abstract String geoName();

    protected abstract EntityType<? extends RoyalBoltEntity> boltType();

    protected abstract SoundEvent idleSound();

    protected abstract SoundEvent biteSound();

    protected abstract SoundEvent shootSound();

    protected abstract SoundEvent flySound();

    protected abstract SoundEvent stepSound();

    protected abstract int signatureCooldownTicks();

    protected abstract boolean isSignatureUseful(LivingEntity target);

    protected abstract void signatureTelegraph(ServerLevel level, int tick, @Nullable LivingEntity target, @Nullable Vec3 aimPoint);

    protected abstract boolean performSignature(ServerLevel level, @Nullable LivingEntity target, @Nullable Vec3 aimPoint, boolean ridden);

    protected abstract float beamBaseDamage();

    protected abstract ParticleOptions beamWindupParticle();

    public abstract boolean prefersAerialCombat();

    public abstract double preferredCombatRange();

    protected abstract float biteWeight();

    protected abstract float spitWeight();

    protected abstract float beamWeight();

    protected abstract float signatureWeight();

    protected int nextBeamHead(int previous) {
        return (previous + 1) % 3;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.idleSound();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.stepSound(), 0.16F * this.getGrowthScale(), 1.1F - 0.06F * this.getGrowthScale() + (this.random.nextFloat() - 0.5F) * 0.2F);
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
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new RoyalMountCombatGoal(this));
        this.goalSelector.addGoal(4, new RoyalMountFollowOwnerGoal(this));
        this.goalSelector.addGoal(5, new RoyalMountCompanionGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return RoyalMountEntity.this.canWanderFreely() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return RoyalMountEntity.this.canWanderFreely() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    private boolean canWanderFreely() {
        if (this.isVehicle() || this.isOrderedToSit() || this.isFlying() || this.isActing()) {
            return false;
        }
        if (!this.isTame()) {
            return true;
        }
        LivingEntity owner = this.getOwner();
        return owner == null || owner.level() != this.level() || this.distanceToSqr(owner) > 48.0D * 48.0D;
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
        tag.putInt("SignatureCooldown", this.signatureCooldownTicks);
        tag.putFloat("GrowthProgress", this.getGrowthProgress());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSaddled(tag.getBoolean("Saddled"));
        this.clearAction();
        this.biteCooldownTicks = 0;
        this.spitCooldownTicks = 0;
        this.actionRecoveryTicks = 0;
        this.beamCooldownTicks = Math.max(0, tag.getInt("BeamCooldown"));
        int signatureCooldown = tag.contains("SignatureCooldown")
                ? tag.getInt("SignatureCooldown")
                : Math.max(tag.getInt("CommandCooldown"), tag.getInt("TimeFieldCooldown"));
        this.signatureCooldownTicks = Math.max(0, signatureCooldown);
        this.setGrowthProgress(tag.contains("GrowthProgress") ? tag.getFloat("GrowthProgress") : 0.0F);
        if (tag.contains("Health", 99)) {
            this.setHealth(tag.getFloat("Health"));
        }
        this.setFlying(tag.getBoolean("Flying") && this.canFly());
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

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    public void setFlying(boolean flying) {
        if (this.entityData.get(FLYING) != flying) {
            this.entityData.set(FLYING, flying);
            this.moveControl = flying ? this.flyingMoveControl : this.groundMoveControl;
            this.navigation.stop();
            this.navigation = flying ? this.flyingNavigation : this.groundNavigation;
            this.setNoGravity(flying);
            if (flying && !this.level().isClientSide) {
                this.playSound(this.flySound(), 1.0F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.2F);
            }
        }
    }

    public boolean claimFlight() {
        if (!this.canFly()) {
            return false;
        }
        this.setFlying(true);
        this.flightClaimTick = this.tickCount;
        return true;
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
        return 1.0F + (MAX_SCALE - 1.0F) * this.getGrowthProgress();
    }

    public float getGrowthProgress() {
        return this.entityData.get(GROWTH_PROGRESS);
    }

    public Stage getStage() {
        float progress = this.getGrowthProgress();
        if (progress >= ADULT_PROGRESS) {
            return Stage.ADULT;
        }
        return progress >= JUVENILE_PROGRESS ? Stage.JUVENILE : Stage.HATCHLING;
    }

    public boolean canBeRidden() {
        return this.getStage() != Stage.HATCHLING;
    }

    public boolean canFly() {
        return this.getStage() == Stage.ADULT;
    }

    public boolean canSpit() {
        return this.getStage() != Stage.HATCHLING;
    }

    public boolean canUseAdultAbilities() {
        return this.getStage() == Stage.ADULT;
    }

    public void setGrowthProgress(float progress) {
        float clamped = Mth.clamp(progress, 0.0F, 1.0F);
        if (this.getGrowthProgress() != clamped) {
            float oldHealth = this.getHealth();
            this.entityData.set(GROWTH_PROGRESS, clamped);
            this.applyGrowthModifiers();
            this.setHealth(Math.min(oldHealth, this.getMaxHealth()));
            this.refreshDimensions();
        }
    }

    private void applyGrowthModifiers() {
        float bonus = this.getGrowthScale() - 1.0F;
        this.applyGrowthModifier(this.getAttribute(Attributes.MAX_HEALTH), GROWTH_HEALTH_MODIFIER, bonus);
        this.applyGrowthModifier(this.getAttribute(Attributes.ATTACK_DAMAGE), GROWTH_DAMAGE_MODIFIER, bonus * 0.5D);
        this.applyGrowthModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), GROWTH_SPEED_MODIFIER, bonus * 0.1D);
        this.applyGrowthModifier(this.getAttribute(Attributes.FLYING_SPEED), GROWTH_FLYING_SPEED_MODIFIER, bonus * 0.1D);
    }

    private void applyGrowthModifier(@Nullable AttributeInstance attribute, ResourceLocation id, double amount) {
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(id);
        if (amount > 0.0D) {
            attribute.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    private void grow(float amount, @Nullable Player feeder) {
        Stage before = this.getStage();
        this.setGrowthProgress(this.getGrowthProgress() + amount);
        Stage after = this.getStage();
        if (after != before && this.level() instanceof ServerLevel level) {
            double radius = this.getBbWidth() * 0.5D;
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + this.getBbHeight() * 0.6D, this.getZ(),
                    40, radius, this.getBbHeight() * 0.3D, radius, 0.3D);
            this.playSound(this.idleSound(), 2.0F, 0.6F);
            this.triggerQuirk();
            if (feeder != null) {
                feeder.displayClientMessage(Component.translatable("message.antarchy.royal_mount.stage_up",
                        this.getDisplayName(), after.displayName()), true);
            }
        } else if (feeder != null) {
            feeder.displayClientMessage(Component.translatable("message.antarchy.royal_mount.growth",
                    this.getDisplayName(), this.getStage().displayName(), Math.round(this.getGrowthProgress() * 100.0F)), true);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && attacker != this && (this.hasPassenger(attacker)
                || attacker != this.getOwner() && this.isFriendlyTo(attacker))) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(this.getGrowthScale());
    }

    @Override
    public float maxUpStep() {
        return Math.max(1.1F, 0.5F + 0.6F * this.getGrowthScale());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(AntarchyTags.Items.ROYAL_MOUNT_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean owned = this.isTame() && this.isOwnedBy(player);

        if (stack.is(Items.GOLDEN_APPLE) && !this.isTame()) {
            if (!this.level().isClientSide) {
                this.consume(player, stack);
                this.tame(player);
                this.setTarget(null);
                this.heal(8.0F);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (owned && (stack.is(Items.ENCHANTED_GOLDEN_APPLE) || stack.is(Items.GOLDEN_APPLE))) {
            boolean enchanted = stack.is(Items.ENCHANTED_GOLDEN_APPLE);
            boolean canGrow = this.getGrowthProgress() < 1.0F;
            if (!canGrow && this.getHealth() >= this.getMaxHealth()) {
                return InteractionResult.PASS;
            }
            if (!this.level().isClientSide) {
                this.consume(player, stack);
                this.heal(enchanted ? 20.0F : 8.0F);
                if (canGrow) {
                    this.grow(enchanted ? ENCHANTED_APPLE_GROWTH : GOLDEN_APPLE_GROWTH, player);
                }
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.triggerQuirk();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isFood(stack)) {
            if (owned && this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide) {
                    this.consume(player, stack);
                    this.heal(8.0F);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return InteractionResult.PASS;
        }

        if (owned) {
            if (!this.isSaddled() && stack.is(Items.SADDLE)) {
                if (!this.level().isClientSide) {
                    this.setSaddled(true);
                    this.consume(player, stack);
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
                    this.clearAction();
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.isEmpty() && !player.isPassenger()) {
                if (!this.level().isClientSide) {
                    this.tryMount(player);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    private void consume(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private boolean tryMount(Player player) {
        if (!this.canBeRidden()) {
            player.displayClientMessage(Component.translatable("message.antarchy.royal_mount.too_young_ride",
                    this.getDisplayName(), Stage.JUVENILE.displayName()), true);
            return false;
        }
        this.setOrderedToSit(false);
        this.setInSittingPose(false);
        this.setTarget(null);
        this.clearAction();
        return player.startRiding(this);
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
        if (target != null && (!target.isAlive() || !this.canAttack(target))) {
            this.setTarget(null);
        }

        this.tickCooldowns();
        this.tickAction();
        this.tickFlightState();
        this.tickOwnerGreeting();

        if ((this.isFlying() || !this.onGround()) && --this.flapSoundTimer <= 0) {
            this.flapSoundTimer = 24 + this.random.nextInt(12);
            this.playSound(this.flySound(), 0.6F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.3F);
        }

        this.updateAmbientAnimState();

        if (this.quirkTimer > 0) {
            this.quirkTimer--;
        }
        if (this.getAnimState() == ANIM_IDLE && this.quirkTimer == 0 && this.random.nextInt(600) == 0) {
            this.quirkTimer = 80;
        }
        this.entityData.set(QUIRKING, this.quirkTimer > 0 && this.getAnimState() == ANIM_IDLE && !this.isActing());
    }

    public void triggerQuirk() {
        if (!this.isActing()) {
            this.quirkTimer = 80;
        }
    }

    private void tickCooldowns() {
        if (this.biteCooldownTicks > 0) this.biteCooldownTicks--;
        if (this.spitCooldownTicks > 0) this.spitCooldownTicks--;
        if (this.beamCooldownTicks > 0) this.beamCooldownTicks--;
        if (this.signatureCooldownTicks > 0) this.signatureCooldownTicks--;
        if (this.actionRecoveryTicks > 0) this.actionRecoveryTicks--;
    }

    private void tickFlightState() {
        if (!this.isFlying()) {
            return;
        }
        if (this.isVehicle()) {
            if (!this.canFly()) {
                this.setFlying(false);
            } else if (this.onGround() && this.riderDescendPressed) {
                this.setFlying(false);
            }
            return;
        }
        if (this.canFly() && this.tickCount - this.flightClaimTick <= 10) {
            return;
        }
        if (this.onGround() || this.isInWater() || this.isInLava()) {
            this.setFlying(false);
            return;
        }
        this.navigation.stop();
        this.moveControl.setWantedPosition(this.getX(), this.getY() - 3.0D, this.getZ(), 0.8D);
    }

    private void tickOwnerGreeting() {
        if (!this.isTame() || this.isVehicle() || this.tickCount % 10 != 0) {
            return;
        }
        LivingEntity owner = this.getOwner();
        if (owner == null || owner.level() != this.level()) {
            this.ownerAwayTicks = Math.min(this.ownerAwayTicks + 10, 2400);
            return;
        }
        double distance = this.distanceTo(owner);
        if (distance > 24.0D + this.getBbWidth()) {
            this.ownerAwayTicks = Math.min(this.ownerAwayTicks + 10, 2400);
        } else if (distance < 8.0D + this.getBbWidth()) {
            if (this.ownerAwayTicks >= 400 && this.getTarget() == null) {
                this.triggerQuirk();
                this.playSound(this.idleSound(), 1.4F, 1.15F);
                this.lookControl.setLookAt(owner, 30.0F, 30.0F);
            }
            this.ownerAwayTicks = 0;
        }
    }

    private void updateAmbientAnimState() {
        if (this.isActing()) {
            return;
        }
        if (this.isFlying() || (!this.onGround() && !this.isInWater())) {
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

    public boolean isActing() {
        return this.action != ACTION_NONE;
    }

    public int currentAction() {
        return this.action;
    }

    public boolean isActionReady() {
        return this.action == ACTION_NONE && this.actionRecoveryTicks <= 0;
    }

    @Nullable
    public LivingEntity actionTarget() {
        return this.actionTarget;
    }

    public boolean isActionCommitted() {
        return switch (this.action) {
            case ACTION_BEAM -> this.actionTicks >= BEAM_WINDUP_TICKS - 6;
            case ACTION_SPIT -> this.actionTicks >= SPIT_FIRE_TICK - 3;
            case ACTION_SIGNATURE -> this.actionTicks >= SIGNATURE_WINDUP_TICKS - 6;
            default -> this.action != ACTION_NONE;
        };
    }

    public boolean isActionAvailable(int type) {
        return switch (type) {
            case ACTION_BITE -> this.biteCooldownTicks <= 0;
            case ACTION_SPIT -> this.canSpit() && this.spitCooldownTicks <= 0;
            case ACTION_BEAM -> this.canUseAdultAbilities() && this.beamCooldownTicks <= 0;
            case ACTION_SIGNATURE -> this.canUseAdultAbilities() && this.signatureCooldownTicks <= 0;
            default -> false;
        };
    }

    public boolean startAction(int type, @Nullable LivingEntity target, @Nullable Vec3 aimPoint, boolean ridden) {
        if (this.level().isClientSide || !this.isActionReady() || !this.isActionAvailable(type)) {
            return false;
        }
        this.action = type;
        this.actionTicks = 0;
        this.actionFired = false;
        this.actionRidden = ridden;
        this.actionTarget = target;
        this.actionAimPoint = aimPoint;
        this.lastAction = type;
        this.entityData.set(QUIRKING, false);
        this.quirkTimer = 0;
        boolean flying = this.isFlying();
        switch (type) {
            case ACTION_BITE -> {
                this.biteCooldownTicks = BITE_COOLDOWN_TICKS;
                this.setAnimState(flying ? ANIM_FLY_BITE : ANIM_BITE);
                this.triggerAnim("main", flying ? "fly_bite" : "bite");
                this.playSound(this.biteSound(), 1.2F, 0.95F - 0.04F * this.getGrowthScale() + this.random.nextFloat() * 0.2F);
            }
            case ACTION_SPIT -> {
                this.spitCooldownTicks = SPIT_COOLDOWN_TICKS;
                this.setAnimState(flying ? ANIM_FLY_SHOOT : ANIM_SHOOT);
                this.triggerAnim("main", flying ? "fly_shoot" : "shoot");
            }
            case ACTION_BEAM -> {
                this.beamCooldownTicks = BEAM_COOLDOWN_TICKS;
                this.beamHead = this.nextBeamHead(this.beamHead);
                this.entityData.set(BEAM_HEAD, this.beamHead);
                this.setAnimState(flying ? ANIM_FLY_SHOOT : ANIM_SHOOT);
                this.triggerAnim("main", flying ? "fly_shoot" : "shoot");
                this.playSound(this.shootSound(), 1.0F, 0.7F);
            }
            case ACTION_SIGNATURE -> {
                this.signatureCooldownTicks = this.signatureCooldownTicks();
                this.setAnimState(flying ? ANIM_FLY_SHOOT : ANIM_SHOOT);
                this.triggerAnim("main", flying ? "fly_shoot" : "shoot");
                this.playSound(this.idleSound(), 2.0F, 0.6F);
            }
            default -> {
                this.clearAction();
                return false;
            }
        }
        return true;
    }

    private void tickAction() {
        if (this.action == ACTION_NONE) {
            if (this.beamController.isFiring()) {
                this.stopBeam();
            }
            return;
        }
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.actionRidden && this.getControllingPassenger() == null) {
            this.actionRidden = false;
        }
        this.actionTicks++;
        if (!this.actionRidden && this.actionTarget != null && !this.isActionCommitted()) {
            this.turnTowards(this.actionTarget.position(), 14.0F);
        }
        switch (this.action) {
            case ACTION_BITE -> {
                if (!this.actionFired && this.actionTicks >= BITE_HIT_TICK) {
                    this.actionFired = true;
                    this.applyBiteDamage();
                }
                if (this.actionTicks >= BITE_TICKS) {
                    this.finishAction();
                }
            }
            case ACTION_SPIT -> {
                if (!this.actionFired && this.actionTicks >= SPIT_FIRE_TICK) {
                    this.actionFired = true;
                    this.fireSpitVolley();
                }
                if (this.actionTicks >= SPIT_TICKS) {
                    this.finishAction();
                }
            }
            case ACTION_BEAM -> this.tickBeamAction(level);
            case ACTION_SIGNATURE -> {
                if (!this.actionFired) {
                    this.signatureTelegraph(level, this.actionTicks, this.actionTarget, this.actionAimPoint);
                    if (this.actionTicks >= SIGNATURE_WINDUP_TICKS) {
                        this.actionFired = true;
                        if (this.performSignature(level, this.validActionTarget(), this.actionAimPoint, this.actionRidden)) {
                            this.playSound(this.shootSound(), 1.4F, 0.6F);
                        } else {
                            this.signatureCooldownTicks = Math.min(this.signatureCooldownTicks, 60);
                        }
                    }
                }
                if (this.actionTicks >= SIGNATURE_TICKS) {
                    this.finishAction();
                }
            }
            default -> this.finishAction();
        }
    }

    @Nullable
    private LivingEntity validActionTarget() {
        LivingEntity target = this.actionTarget;
        return target != null && target.isAlive() && target.level() == this.level() ? target : null;
    }

    private void tickBeamAction(ServerLevel level) {
        if (!this.actionFired) {
            if (this.actionTicks % 2 == 0) {
                Vec3 origin = this.beamShootFrom();
                level.sendParticles(this.beamWindupParticle(), origin.x, origin.y, origin.z, 3,
                        0.15D * this.getGrowthScale(), 0.15D * this.getGrowthScale(), 0.15D * this.getGrowthScale(), 0.02D);
            }
            if (this.actionTicks >= BEAM_WINDUP_TICKS) {
                this.actionFired = true;
                Vec3 aim = this.currentBeamAim();
                this.beamController.start(this.beamShootFrom(), aim, BEAM_DURATION_TICKS);
                this.entityData.set(BEAM_ACTIVE, true);
                this.playSound(this.shootSound(), 1.4F, 0.6F);
            }
            return;
        }
        if (this.beamController.isFiring()) {
            Vec3 from = this.beamShootFrom();
            LivingEntity target = this.actionRidden ? null : this.validActionTarget();
            Vec3 direction;
            if (this.actionRidden && this.getControllingPassenger() instanceof Player rider) {
                direction = this.riderAimPoint(rider, this.beamRange()).subtract(from);
            } else {
                direction = this.getLookAngle();
            }
            this.beamController.tick(from, direction, target, this.beamSettings(), BeamTerrainMode.NONE,
                    this::setBeamEndPosition, !this.actionRidden);
            if (!this.beamController.isFiring()) {
                this.entityData.set(BEAM_ACTIVE, false);
                this.entityData.set(HAS_BEAM_END, false);
            }
            return;
        }
        if (this.actionTicks >= BEAM_WINDUP_TICKS + BEAM_DURATION_TICKS + BEAM_RECOVERY_TICKS) {
            this.finishAction();
        }
    }

    private Vec3 currentBeamAim() {
        if (this.actionRidden && this.getControllingPassenger() instanceof Player rider) {
            return this.riderAimPoint(rider, this.beamRange());
        }
        LivingEntity target = this.validActionTarget();
        if (target != null) {
            return target.getEyePosition();
        }
        return this.actionAimPoint != null ? this.actionAimPoint : this.getEyePosition().add(this.getLookAngle().scale(20.0D));
    }

    private void finishAction() {
        this.clearAction();
        this.actionRecoveryTicks = ACTION_RECOVERY_TICKS;
    }

    public void clearAction() {
        this.action = ACTION_NONE;
        this.actionTicks = 0;
        this.actionFired = false;
        this.actionRidden = false;
        this.actionTarget = null;
        this.actionAimPoint = null;
        if (this.beamController.isFiring() || this.entityData.get(BEAM_ACTIVE)) {
            this.stopBeam();
        }
    }

    private void stopBeam() {
        this.beamController.stop();
        this.entityData.set(BEAM_ACTIVE, false);
        this.entityData.set(HAS_BEAM_END, false);
        this.entityData.set(BEAM_HEAD, -1);
    }

    public int chooseCombatAction(LivingEntity target, double gap, boolean canSee) {
        if (!this.isActionReady()) {
            return ACTION_NONE;
        }
        float bite = this.isActionAvailable(ACTION_BITE) && this.canBiteReach(target) ? this.biteWeight() : 0.0F;
        float spit = this.isActionAvailable(ACTION_SPIT) && canSee && gap > 2.0D && gap < 22.0D + this.getGrowthScale() ? this.spitWeight() : 0.0F;
        float beam = this.isActionAvailable(ACTION_BEAM) && canSee && gap > 5.0D && gap < this.beamRange() - 2.0D ? this.beamWeight() : 0.0F;
        float signature = this.isActionAvailable(ACTION_SIGNATURE) && this.isSignatureUseful(target) ? this.signatureWeight() : 0.0F;
        if (this.lastAction == ACTION_SPIT) spit *= 0.5F;
        if (this.lastAction == ACTION_BITE && spit > 0.0F) bite *= 0.7F;
        float total = bite + spit + beam + signature;
        if (total <= 0.0F) {
            return ACTION_NONE;
        }
        float roll = this.random.nextFloat() * total;
        if ((roll -= signature) < 0.0F) return ACTION_SIGNATURE;
        if ((roll -= beam) < 0.0F) return ACTION_BEAM;
        if ((roll -= bite) < 0.0F) return ACTION_BITE;
        return ACTION_SPIT;
    }

    public void turnTowards(Vec3 point, float maxTurn) {
        double dx = point.x - this.getX();
        double dz = point.z - this.getZ();
        if (dx * dx + dz * dz < 1.0E-4D) {
            return;
        }
        float desired = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float yaw = Mth.approachDegrees(this.getYRot(), desired, maxTurn);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    public double combatGap(LivingEntity target) {
        double horizontal = Math.sqrt(Mth.square(target.getX() - this.getX()) + Mth.square(target.getZ() - this.getZ()));
        double vertical = Math.max(0.0D, Math.max(target.getY() - (this.getY() + this.getBbHeight()), this.getY() - (target.getY() + target.getBbHeight())));
        double flat = Math.max(0.0D, horizontal - this.getBbWidth() * 0.5D - target.getBbWidth() * 0.5D);
        return Math.sqrt(flat * flat + vertical * vertical);
    }

    public AABB biteBox() {
        float scale = this.getGrowthScale();
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        double reach = this.getBbWidth() * 0.5D + 1.0D * scale;
        double radius = 1.1D * scale + 0.6D;
        Vec3 center = this.position().add(forward.scale(reach));
        return new AABB(center.x - radius, this.getY() - 0.75D, center.z - radius,
                center.x + radius, this.getY() + this.getBbHeight() + 0.75D * scale, center.z + radius);
    }

    public boolean canBiteReach(LivingEntity target) {
        return this.biteBox().intersects(target.getBoundingBox());
    }

    @Override
    protected AABB getAttackBoundingBox() {
        return this.getBoundingBox().minmax(this.biteBox());
    }

    private void applyBiteDamage() {
        AABB box = this.biteBox();
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        DamageSource source = this.damageSources().mobAttack(this);
        int hits = 0;
        LivingEntity primary = this.validActionTarget();
        if (primary != null && box.intersects(primary.getBoundingBox()) && this.canAttack(primary)) {
            this.biteHit(primary, damage, source);
            hits++;
        }
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, box,
                living -> living != primary && living.isAlive() && this.canAttack(living))) {
            if (hits >= 3) {
                break;
            }
            if (!this.actionRidden && !(candidate instanceof Enemy) && candidate != this.getTarget()) {
                continue;
            }
            this.biteHit(candidate, damage, source);
            hits++;
        }
    }

    private void biteHit(LivingEntity victim, float damage, DamageSource source) {
        if (victim.hurt(source, damage)) {
            Vec3 push = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (push.lengthSqr() > 1.0E-4D) {
                push = push.normalize();
                victim.knockback(0.3D + 0.1D * this.getGrowthScale(), -push.x, -push.z);
            }
        }
    }

    private void fireSpitVolley() {
        Vec3 aim;
        LivingEntity target = this.validActionTarget();
        if (this.actionRidden && this.getControllingPassenger() instanceof Player rider) {
            aim = this.riderAimPoint(rider, 48.0D).subtract(this.getEyePosition()).normalize();
        } else if (target != null) {
            aim = target.getEyePosition().subtract(this.getEyePosition()).normalize();
        } else {
            aim = this.getLookAngle();
        }
        Vec3 flat = new Vec3(-aim.z, 0.0D, aim.x);
        Vec3 right = flat.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : flat.normalize();
        float speed = 1.3F + 0.06F * this.getGrowthScale();
        for (int i = -1; i <= 1; i++) {
            Vec3 origin = this.boltOrigin(i);
            Vec3 dir;
            if (target != null && !this.actionRidden) {
                dir = target.getEyePosition().subtract(origin).normalize().add(right.scale(i * 0.05D)).normalize();
            } else {
                dir = aim.add(right.scale(i * 0.06D)).normalize();
            }
            RoyalBoltEntity bolt = new RoyalBoltEntity(this.boltType(), this.level());
            bolt.setOwner(this);
            bolt.setElement(this.boltElement(i));
            bolt.setPos(origin.x, origin.y, origin.z);
            bolt.shoot(dir.x, dir.y, dir.z, speed, 1.0F);
            this.level().addFreshEntity(bolt);
        }
        this.playSound(this.shootSound(), 1.2F, 0.95F - 0.03F * this.getGrowthScale() + this.random.nextFloat() * 0.2F);
    }

    protected Vec3 boltOrigin(int lateralIndex) {
        float scale = this.getGrowthScale();
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        return this.position()
                .add(0.0D, 1.05D * scale, 0.0D)
                .add(forward.scale(1.75D * scale))
                .add(side.scale(lateralIndex * 0.85D * scale));
    }

    public float projectileDamageMultiplier() {
        return 1.0F + 0.35F * (this.getGrowthScale() - 1.0F);
    }

    protected RoyalBeamElement boltElement(int lateralIndex) {
        return RoyalBeamElement.GENERIC;
    }

    public double beamRange() {
        return 26.0D + 2.0D * this.getGrowthScale();
    }

    private BeamSettings beamSettings() {
        return new BeamSettings(this.beamRange(), 0.08D, 4.0D, BEAM_DURATION_TICKS, 6, BEAM_COOLDOWN_TICKS,
                0.35F + 0.08F * this.getGrowthScale(), 0.45F + 0.15F * this.getGrowthScale(), this.beamBaseDamage(),
                0.45F, 3, 0.0D, 0.0F, 0.0F, 0, 0.0F, 0.0F, 0.0F, false, true);
    }

    @Override
    public float scaleBeamDamage(float damage) {
        return damage * (1.0F + 0.25F * (this.getGrowthScale() - 1.0F));
    }

    @Override
    public boolean canBeamDamage(LivingEntity target) {
        if (this.isFriendlyTo(target)) {
            return false;
        }
        if (this.actionRidden) {
            return true;
        }
        return target instanceof Enemy || target == this.getTarget() || target == this.actionTarget
                || target instanceof Mob mob && mob.getTarget() != null && this.isFriendlyTo(mob.getTarget());
    }

    public Vec3 beamShootFrom() {
        float scale = this.getGrowthScale();
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        int head = Math.max(0, this.getBeamHead());
        return this.position().add(0.0D, 1.05D * scale, 0.0D).add(forward.scale(1.75D * scale)).add(side.scale((head - 1) * 0.85D * scale));
    }

    public int getBeamHead() { return this.entityData.get(BEAM_HEAD); }
    public boolean isFiringBeam() { return this.entityData.get(BEAM_ACTIVE); }
    public Vec3 getBeamEndPosition() {
        return this.entityData.get(HAS_BEAM_END) ? new Vec3(this.entityData.get(BEAM_END_X), this.entityData.get(BEAM_END_Y), this.entityData.get(BEAM_END_Z)) : null;
    }
    public RoyalBeamElement getBeamElement() { return RoyalBeamElement.GENERIC; }
    public BeamController beamController() { return this.beamController; }
    private void setBeamEndPosition(@Nullable Vec3 position) {
        this.entityData.set(HAS_BEAM_END, position != null);
        if (position != null) {
            this.entityData.set(BEAM_END_X, (float) position.x);
            this.entityData.set(BEAM_END_Y, (float) position.y);
            this.entityData.set(BEAM_END_Z, (float) position.z);
        }
    }

    public Vec3 riderAimPoint(Player rider, double range) {
        Vec3 eye = rider.getEyePosition();
        Vec3 end = eye.add(rider.getViewVector(1.0F).scale(range));
        var blockHit = this.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, rider));
        Vec3 limit = blockHit.getLocation();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this.level(), rider, eye, limit,
                new AABB(eye, limit).inflate(1.0D),
                entity -> entity instanceof LivingEntity living && living.isAlive() && !entity.isSpectator() && !this.isFriendlyTo(entity));
        return entityHit != null ? entityHit.getLocation() : limit;
    }

    @Nullable
    public LivingEntity riderAimEntity(Player rider, double range) {
        Vec3 eye = rider.getEyePosition();
        Vec3 end = eye.add(rider.getViewVector(1.0F).scale(range));
        var blockHit = this.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, rider));
        Vec3 limit = blockHit.getLocation();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this.level(), rider, eye, limit,
                new AABB(eye, limit).inflate(1.0D),
                entity -> entity instanceof LivingEntity living && living.isAlive() && !entity.isSpectator() && !this.isFriendlyTo(entity));
        return entityHit != null && entityHit.getEntity() instanceof LivingEntity living ? living : null;
    }

    public boolean isFriendlyTo(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity == this || this.hasPassenger(entity)) {
            return true;
        }
        if (entity instanceof RoyalMountEntity other && this.isTame() && other.isTame()) {
            return true;
        }
        if (this.isAlliedTo(entity) || entity.isAlliedTo(this)) {
            return true;
        }
        if (!this.isTame()) {
            return false;
        }
        LivingEntity owner = this.getOwner();
        if (owner != null && (entity == owner || entity.isAlliedTo(owner) || owner.isAlliedTo(entity))) {
            return true;
        }
        return entity instanceof OwnableEntity ownable && this.getOwnerUUID() != null
                && this.getOwnerUUID().equals(ownable.getOwnerUUID());
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.isFriendlyTo(target) && super.canAttack(target);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Creeper || this.isFriendlyTo(target)) {
            return false;
        }
        if (target instanceof Player victim && owner instanceof Player ownerPlayer && !ownerPlayer.canHarmPlayer(victim)) {
            return false;
        }
        return super.wantsToAttack(target, owner);
    }

    public boolean toggleMountedFlight(ServerPlayer player) {
        if (!this.canControl(player)) {
            return false;
        }
        if (!this.isFlying() && !this.canFly()) {
            this.notifyLocked(player, "message.antarchy.royal_mount.too_young_fly", Stage.ADULT);
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
            this.startAction(ACTION_BITE, null, null, true);
        }
    }

    public void handleMountedSpit(ServerPlayer player) {
        if (!this.canControl(player)) {
            return;
        }
        if (!this.canSpit()) {
            this.notifyLocked(player, "message.antarchy.royal_mount.too_young_ability", Stage.JUVENILE);
            return;
        }
        this.startAction(ACTION_SPIT, null, null, true);
    }

    public void handleMountedBeam(ServerPlayer player) {
        if (!this.canControl(player)) {
            return;
        }
        if (!this.canUseAdultAbilities()) {
            this.notifyLocked(player, "message.antarchy.royal_mount.too_young_ability", Stage.ADULT);
            return;
        }
        this.startAction(ACTION_BEAM, null, this.riderAimPoint(player, this.beamRange()), true);
    }

    public void handleMountedSignature(ServerPlayer player) {
        if (!this.canControl(player)) {
            return;
        }
        if (!this.canUseAdultAbilities()) {
            this.notifyLocked(player, "message.antarchy.royal_mount.too_young_ability", Stage.ADULT);
            return;
        }
        LivingEntity aimed = this.riderAimEntity(player, 32.0D);
        this.startAction(ACTION_SIGNATURE, aimed, this.riderAimPoint(player, 32.0D), true);
    }

    private void notifyLocked(Player player, String key, Stage required) {
        player.displayClientMessage(Component.translatable(key, this.getDisplayName(), required.displayName()), true);
    }

    public void setRiderAscend(boolean pressed) {
        this.riderAscendPressed = pressed;
        if (pressed && !this.isFlying() && this.canFly() && this.getControllingPassenger() != null) {
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
        for (RoyalMountEntity mount : player.level().getEntitiesOfClass(RoyalMountEntity.class, player.getBoundingBox().inflate(40.0D),
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
        return mount != null && mount.tryMount(player);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player player && this.isOwnedBy(player) ? player : null;
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && this.canBeRidden();
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.riderAscendPressed = false;
        this.riderDescendPressed = false;
        if (this.actionRidden) {
            this.clearAction();
        }
    }

    public double seatHeight() {
        double bob = this.getAnimState() == ANIM_FLY || this.getAnimState() == ANIM_FLY_BITE || this.getAnimState() == ANIM_FLY_SHOOT
                ? FLY_BOB_HEIGHT : 0.0D;
        return (SEAT_HEIGHT + bob) * this.getGrowthScale();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        float scale = this.getGrowthScale();
        Vec3 back = Vec3.directionFromRotation(0.0F, this.yBodyRot).scale(-SEAT_BACK * scale);
        double attach = passenger.getVehicleAttachmentPoint(this).y;
        moveFunction.accept(passenger, this.getX() + back.x, this.getY() + this.seatHeight() - attach, this.getZ() + back.z);
    }

    public float thirdPersonCameraBonus() {
        return 1.5F + 2.6F * (this.getGrowthScale() - 1.0F);
    }

    public float thirdPersonCameraLift() {
        return 0.3F * this.getGrowthScale();
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

        float yaw = this.isActionCommittedForRider() ? Mth.approachDegrees(this.getYRot(), rider.getYRot(), 6.0F) : rider.getYRot();
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        this.setXRot(rider.getXRot() * 0.5F);

        Vec3 input = this.getRiddenInput(rider, travelVector);
        float speed = this.getRiddenSpeed(rider);
        if (this.action == ACTION_BEAM || this.action == ACTION_SIGNATURE) {
            speed *= 0.45F;
        }
        this.setSpeed(speed);

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

    private boolean isActionCommittedForRider() {
        return this.action == ACTION_BEAM && this.actionFired;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public static boolean isAirborne(LivingEntity entity) {
        if (entity.isFallFlying()) {
            return true;
        }
        if (entity instanceof Player player && player.getAbilities().flying) {
            return true;
        }
        Entity vehicle = entity.getVehicle();
        return vehicle != null && !vehicle.onGround() && !vehicle.isInWater() && vehicle.isNoGravity();
    }

    public double findGroundY(double x, double startY, double z, int maxDepth) {
        BlockPos.MutableBlockPos cursor = BlockPos.containing(x, startY, z).mutable();
        for (int i = 0; i < maxDepth; i++) {
            BlockState state = this.level().getBlockState(cursor);
            if (!state.getCollisionShape(this.level(), cursor).isEmpty() || !state.getFluidState().isEmpty()) {
                return cursor.getY() + 1.0D;
            }
            cursor.move(0, -1, 0);
            if (cursor.getY() < this.level().getMinBuildHeight()) {
                break;
            }
        }
        return Double.NaN;
    }

    public boolean canOccupy(double x, double y, double z) {
        AABB box = this.getBoundingBox().move(x - this.getX(), y - this.getY(), z - this.getZ());
        return this.level().noCollision(this, box) && !this.level().containsAnyLiquid(box);
    }

    public boolean tryTeleportNear(LivingEntity anchor) {
        boolean allowAir = this.canFly() && isAirborne(anchor);
        double base = this.getBbWidth() * 0.5D + 1.5D;
        float startAngle = this.random.nextFloat() * Mth.TWO_PI;
        for (int ring = 0; ring < 3; ring++) {
            double radius = base + ring * (1.5D + this.getBbWidth() * 0.25D);
            for (int step = 0; step < 12; step++) {
                double angle = startAngle + step * (Mth.TWO_PI / 12.0D);
                double x = anchor.getX() + Math.cos(angle) * radius;
                double z = anchor.getZ() + Math.sin(angle) * radius;
                if (allowAir) {
                    double y = anchor.getY() + 1.0D;
                    if (this.canOccupy(x, y, z)) {
                        this.finishTeleport(x, y, z, true);
                        return true;
                    }
                    continue;
                }
                double ground = this.findGroundY(x, anchor.getY() + 3.0D, z, 8);
                if (Double.isNaN(ground) || !this.level().getBlockState(BlockPos.containing(x, ground - 1.0D, z)).getFluidState().isEmpty()) {
                    continue;
                }
                if (this.canOccupy(x, ground, z)) {
                    this.finishTeleport(x, ground, z, false);
                    return true;
                }
            }
        }
        return false;
    }

    private void finishTeleport(double x, double y, double z, boolean flying) {
        this.navigation.stop();
        this.setFlying(flying && this.canFly());
        if (flying) {
            this.flightClaimTick = this.tickCount;
        }
        this.teleportTo(x, y, z);
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
