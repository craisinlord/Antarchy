package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyAdvancements;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.movement.DreamSandLowGravityAccess;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
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

public class ReverieEntity extends PathfinderMob implements FlyingAnimal, GeoEntity {
    private static final EntityDataAccessor<Integer> MOOD =
            SynchedEntityData.defineId(ReverieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(ReverieEntity.class, EntityDataSerializers.INT);
    private static final ResourceKey<Level> THORAXIS_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis")
    );

    private static final String FOCUS_PLAYER_KEY = "FocusPlayer";
    private static final String NOTICE_PLAYER_KEY = "NoticePlayer";
    private static final String MOOD_KEY = "Mood";
    private static final String NOTICE_TICKS_KEY = "NoticeTicks";
    private static final String INTEREST_TICKS_KEY = "InterestTicks";
    private static final String DAMAGE_REACTION_TICKS_KEY = "DamageReactionTicks";
    private static final String DUPLICATION_COOLDOWN_KEY = "DuplicationCooldownTicks";
    private static final String REBIND_COOLDOWN_TICKS_KEY = "RebindCooldownTicks";
    private static final ResourceLocation HEY_LISTEN_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hey_listen");
    private static final ResourceKey<DamageType> POISON_DAMAGE_TYPE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("minecraft", "poison")
    );

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_FLY = 1;
    private static final int PURPLE_SUPPORT_SCAN_RANGE = 7;
    private static final int INVERSION_REACTION_TICKS = 200;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nullable private UUID focusPlayerUuid;
    @Nullable private Player cachedFocusPlayer;
    @Nullable private UUID noticedPlayerUuid;
    @Nullable private Player cachedNoticedPlayer;
    @Nullable private Vec3 ambientTarget;

    private int noticeTicks;
    private int interestTicks;
    private int damageReactionTicks;
    private int duplicationCooldownTicks;
    private int movementPulseCooldown;
    private int ambientTargetAge;
    private int warningCycleTicks;
    private int rebindCooldownTicks;
    private int inversionReactionTicks;
    private boolean pendingJoinSound;
    private Mood pendingMoodSound;
    private int pendingMoodSoundTicks;
    private boolean trackedFocusInverted;
    private boolean trackedFocusInvertedInitialized;

    private int nextNoticeScanTick;
    private int nextDuplicationScanTick;
    private int nextThreatScanTick;
    private int nextFallCheckTick;
    private boolean cachedIsFallingDangerously;
    @Nullable private LivingEntity cachedWarningThreat;

    private final double cfgInterestRadius;
    private final double cfgAbandonDistance;
    private final int cfgNoticeDuration;
    private final int cfgInterestDuration;
    private final int cfgRebindCooldown;
    private final int cfgDuplicationCooldown;
    private final double cfgDangerousFallSpeed;
    private final double cfgDangerousFallDistance;
    private final double cfgWarningThreatRadius;
    private final double cfgWarningThreatVerticalRange;
    private final int cfgWarningApproachTicks;
    private final int cfgWarningHoverTicks;
    private final int cfgWarningReturnTicks;
    private final int cfgWarningPlayerHoverTicks;
    private final double cfgCatchUpDistance;
    private final double cfgFollowMinDistance;
    private final double cfgFollowMaxDistance;
    private final double cfgAmbientMinRadius;
    private final double cfgAmbientMaxRadius;
    private final double cfgAmbientVerticalRange;
    private final int cfgAmbientMaxAgeTicks;
    private final int cfgAmbientPulseMinTicks;
    private final int cfgAmbientPulseMaxTicks;

    public ReverieEntity(EntityType<? extends ReverieEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.LEAVES, -1.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
        this.xpReward = 0;
        this.setNoGravity(true);
        this.noPhysics = true;
        this.cfgInterestRadius = AntarchySettings.reverieInterestRadius();
        this.cfgAbandonDistance = AntarchySettings.reverieAbandonPlayerDistance();
        this.cfgNoticeDuration = AntarchySettings.reverieNoticeDurationTicks();
        this.cfgInterestDuration = AntarchySettings.reverieInterestDurationTicks();
        this.cfgRebindCooldown = AntarchySettings.reverieRebindCooldownTicks();
        this.cfgDuplicationCooldown = AntarchySettings.reverieDuplicationCooldownTicks();
        this.cfgDangerousFallSpeed = AntarchySettings.reverieDangerousFallSpeed();
        this.cfgDangerousFallDistance = AntarchySettings.reverieDangerousFallDistance();
        this.cfgWarningThreatRadius = AntarchySettings.reverieWarningThreatRadius();
        this.cfgWarningThreatVerticalRange = AntarchySettings.reverieWarningThreatVerticalRange();
        this.cfgWarningApproachTicks = AntarchySettings.reverieWarningApproachTicks();
        this.cfgWarningHoverTicks = AntarchySettings.reverieWarningHoverTicks();
        this.cfgWarningReturnTicks = AntarchySettings.reverieWarningReturnTicks();
        this.cfgWarningPlayerHoverTicks = AntarchySettings.reverieWarningPlayerHoverTicks();
        this.cfgCatchUpDistance = AntarchySettings.reverieCatchUpDistance();
        this.cfgFollowMinDistance = AntarchySettings.reveriePreferredFollowMinDistance();
        this.cfgFollowMaxDistance = AntarchySettings.reveriePreferredFollowMaxDistance();
        this.cfgAmbientMinRadius = AntarchySettings.reverieAmbientTargetMinRadius();
        this.cfgAmbientMaxRadius = AntarchySettings.reverieAmbientTargetMaxRadius();
        this.cfgAmbientVerticalRange = AntarchySettings.reverieAmbientTargetVerticalRange();
        this.cfgAmbientMaxAgeTicks = AntarchySettings.reverieAmbientTargetMaxAgeTicks();
        this.cfgAmbientPulseMinTicks = AntarchySettings.reverieAmbientPulseIntervalMinTicks();
        this.cfgAmbientPulseMaxTicks = AntarchySettings.reverieAmbientPulseIntervalMaxTicks();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.reverieHealth())
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    public static boolean canSpawn(EntityType<ReverieEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        return level.getLevel().dimension().equals(THORAXIS_DIMENSION)
                && level.getBlockState(pos).isAir()
                && level.getFluidState(pos).isEmpty()
                && Mob.checkMobSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ReverieBoundGoal());
        this.goalSelector.addGoal(2, new ReverieAmbientGoal());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(BlockPos pos) {
                return this.level.getBlockState(pos).isAir();
            }
        };
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(false);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MOOD, Mood.WHITE.ordinal());
        builder.define(ANIMATION_STATE, ANIM_IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 2, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<ReverieEntity> state) {
        return this.getAnimationState() == ANIM_IDLE
                ? state.setAndContinue(IDLE_ANIM)
                : state.setAndContinue(FLY_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.tickClientEffects();
            return;
        }

        if (this.duplicationCooldownTicks > 0) {
            this.duplicationCooldownTicks--;
        }
        if (this.rebindCooldownTicks > 0) {
            this.rebindCooldownTicks--;
        }
        if (this.damageReactionTicks > 0) {
            this.damageReactionTicks--;
        }
        if (this.noticeTicks > 0) {
            this.noticeTicks--;
        }
        if (this.movementPulseCooldown > 0) {
            this.movementPulseCooldown--;
        }
        if (this.inversionReactionTicks > 0) {
            this.inversionReactionTicks--;
        }
        this.tickPendingMoodSound();

        this.tickNoticeAndFocus();
        this.tickInterestTimer();
        Player focus = this.getFocusPlayer();
        if (focus != null) {
            this.syncFocusInversionState(focus);
        }
        this.tickMoodAndSupport();
        this.tickLookTarget();
        this.applyPassiveDriftDamping();
        this.updateAnimationState();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isImmuneToSource(source)) {
            return false;
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.releaseFocus();
            this.clearNoticePlayer();
            this.ambientTarget = null;
            this.damageReactionTicks = 0;
            this.inversionReactionTicks = 0;
            this.transitionToMood(Mood.WHITE);
        }
        return hurt;
    }

    private boolean isImmuneToSource(DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.WITHER)
                || source.is(POISON_DAMAGE_TYPE);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.GLASS_BOTTLE)) {
            return this.captureIntoBottle(player, hand);
        }

        if (!stack.is(AntarchyTags.Items.REVERIE_DUPLICATION_ITEMS)) {
            return super.mobInteract(player, hand);
        }

        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        if (this.duplicationCooldownTicks > 0) {
            return InteractionResult.CONSUME;
        }

        ReverieEntity duplicate = AntarchyObjects.REVERIE.get().create(serverLevel);
        if (duplicate == null) {
            return InteractionResult.PASS;
        }

        duplicate.moveTo(
                this.getX() + (this.random.nextDouble() - 0.5D) * 1.5D,
                this.getY() + 0.25D,
                this.getZ() + (this.random.nextDouble() - 0.5D) * 1.5D,
                this.getYRot(),
                this.getXRot()
        );
        duplicate.setDeltaMovement(this.getDeltaMovement().scale(0.25D).add(0.0D, 0.08D, 0.0D));
        serverLevel.addFreshEntity(duplicate);
        this.duplicationCooldownTicks = this.cfgDuplicationCooldown;
        duplicate.duplicationCooldownTicks = this.cfgDuplicationCooldown;
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    private InteractionResult captureIntoBottle(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack reverieBottle = new ItemStack(AntarchyObjects.REVERIE_BOTTLE.get());
        CompoundTag tag = new CompoundTag();
        this.saveWithoutId(tag);
        tag.remove("UUID");
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Rotation");
        tag.remove("FallDistance");
        tag.remove("OnGround");
        tag.remove("Dimension");
        tag.remove("Passengers");
        tag.remove("PortalCooldown");

        CustomData.update(DataComponents.CUSTOM_DATA, reverieBottle, customData -> customData.put("StoredReverie", tag));
        if (this.hasCustomName()) {
            reverieBottle.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        }

        ItemStack heldStack = player.getItemInHand(hand);
        if (player.getAbilities().instabuild || heldStack.getCount() <= 1) {
            player.setItemInHand(hand, reverieBottle);
        } else {
            heldStack.shrink(1);
            if (!player.getInventory().add(reverieBottle)) {
                player.drop(reverieBottle, false);
            }
        }

        serverLevel.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.BOTTLE_FILL, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        this.discard();
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
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

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.getFocusPlayer() == null && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    public Mood getMood() {
        return Mood.byOrdinal(this.entityData.get(MOOD));
    }

    public boolean isPurpleMood() {
        return this.getMood() == Mood.PURPLE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.REVERIE_IDLE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * 4;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.REVERIE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(MOOD_KEY, this.getMood().ordinal());
        tag.putInt(NOTICE_TICKS_KEY, this.noticeTicks);
        tag.putInt(INTEREST_TICKS_KEY, this.interestTicks);
        tag.putInt(DAMAGE_REACTION_TICKS_KEY, this.damageReactionTicks);
        tag.putInt(DUPLICATION_COOLDOWN_KEY, this.duplicationCooldownTicks);
        tag.putInt(REBIND_COOLDOWN_TICKS_KEY, this.rebindCooldownTicks);
        if (this.focusPlayerUuid != null) {
            tag.putUUID(FOCUS_PLAYER_KEY, this.focusPlayerUuid);
        }
        if (this.noticedPlayerUuid != null) {
            tag.putUUID(NOTICE_PLAYER_KEY, this.noticedPlayerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setMoodRaw(Mood.byOrdinal(tag.getInt(MOOD_KEY)));
        this.noticeTicks = tag.getInt(NOTICE_TICKS_KEY);
        this.interestTicks = tag.getInt(INTEREST_TICKS_KEY);
        this.damageReactionTicks = tag.getInt(DAMAGE_REACTION_TICKS_KEY);
        this.duplicationCooldownTicks = tag.getInt(DUPLICATION_COOLDOWN_KEY);
        this.rebindCooldownTicks = tag.getInt(REBIND_COOLDOWN_TICKS_KEY);
        this.focusPlayerUuid = tag.hasUUID(FOCUS_PLAYER_KEY) ? tag.getUUID(FOCUS_PLAYER_KEY) : null;
        this.noticedPlayerUuid = tag.hasUUID(NOTICE_PLAYER_KEY) ? tag.getUUID(NOTICE_PLAYER_KEY) : null;
        this.cachedFocusPlayer = null;
        this.cachedNoticedPlayer = null;
    }

    private void tickNoticeAndFocus() {
        Player shardHolder = this.findDuplicationItemHolder();
        if (shardHolder != null) {
            boolean wasDifferentFocus = this.getFocusPlayer() == null || !shardHolder.getUUID().equals(this.focusPlayerUuid);
            this.setFocusPlayer(shardHolder);
            this.clearNoticePlayer();
            this.warningCycleTicks = 0;
            this.interestTicks = this.cfgInterestDuration;
            this.pendingJoinSound = wasDifferentFocus;
            if (wasDifferentFocus) {
                this.awardJoinAdvancement(shardHolder);
            }
            this.transitionToMood(Mood.YELLOW);
            return;
        }

        Player focus = this.getFocusPlayer();
        if (focus != null && !this.isValidBoundPlayer(focus)) {
            this.releaseFocus();
            focus = null;
        }

        if (focus != null) {
            this.clearNoticePlayer();
            return;
        }

        Player noticed = this.getNoticedPlayer();
        if (!this.isValidNoticePlayer(noticed)) {
            this.clearNoticePlayer();
            noticed = null;
        }

        if (noticed == null) {
            if (this.rebindCooldownTicks > 0) {
                this.clearNoticePlayer();
                return;
            }
            Player nearby = this.findNoticeCandidate();
            if (nearby != null) {
                this.setNoticedPlayer(nearby);
                this.noticeTicks = this.cfgNoticeDuration;
            }
            return;
        }

        if (this.noticeTicks <= 0) {
            boolean wasUnbound = this.getFocusPlayer() == null;
            this.setFocusPlayer(noticed);
            this.clearNoticePlayer();
            this.warningCycleTicks = 0;
            this.pendingJoinSound = wasUnbound;
            if (wasUnbound) {
                this.awardJoinAdvancement(noticed);
            }
            this.transitionToMood(Mood.YELLOW);
        }
    }

    private void tickInterestTimer() {
        if (this.getFocusPlayer() == null) {
            this.interestTicks = 0;
            return;
        }

        if (this.interestTicks > 0) {
            this.interestTicks--;
        }

        if (this.interestTicks <= 0) {
            this.releaseFocus();
            this.transitionToMood(Mood.WHITE);
        }
    }

    private void tickMoodAndSupport() {
        Player focus = this.getFocusPlayer();
        Mood desiredMood = this.getDesiredMood(focus);
        this.transitionToMood(desiredMood);

        if (desiredMood == Mood.RED) {
            this.warningCycleTicks++;
        } else {
            this.warningCycleTicks = 0;
        }

        if (desiredMood == Mood.PURPLE && focus != null && !this.shouldSuppressSlowFalling(focus)) {
            focus.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, true, true));
        }
    }

    private Mood getDesiredMood(@Nullable Player focus) {
        if (focus == null) {
            return Mood.WHITE;
        }
        if (this.shouldSuppressSlowFalling(focus)) {
            if (this.isPlayerTakingDamage(focus)) {
                return Mood.BLUE;
            }
            if (this.damageReactionTicks > 0 || this.findWarningThreat(focus) != null) {
                return Mood.RED;
            }
            return Mood.YELLOW;
        }
        if (this.inversionReactionTicks > 0) {
            return Mood.PURPLE;
        }
        if (this.isPlayerInDangerousFall(focus)) {
            return Mood.PURPLE;
        }
        if (this.isPlayerTakingDamage(focus)) {
            return Mood.BLUE;
        }
        if (this.damageReactionTicks > 0 || this.findWarningThreat(focus) != null) {
            return Mood.RED;
        }
        return Mood.YELLOW;
    }

    private boolean isPlayerTakingDamage(Player focus) {
        return focus.hurtTime > 0 || focus.invulnerableTime > 0;
    }

    private boolean shouldSuppressSlowFalling(Player focus) {
        return focus.isFallFlying()
                || focus.getAbilities().flying
                || focus.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
    }

    private boolean isPlayerCurrentlyFallingDangerously(Player focus) {
        if (this.tickCount < this.nextFallCheckTick) {
            return this.cachedIsFallingDangerously;
        }
        this.nextFallCheckTick = this.tickCount + 5;
        this.cachedIsFallingDangerously = this.checkIsFallingDangerously(focus);
        return this.cachedIsFallingDangerously;
    }

    private boolean checkIsFallingDangerously(Player focus) {
        if (focus.onGround() || focus.onClimbable() || focus.isFallFlying() || focus.isPassenger()) {
            return false;
        }
        if (this.isPlayerInsideAntiwater(focus)) {
            return false;
        }
        if (focus instanceof DreamSandLowGravityAccess access && access.antarchy$getDreamSandLandingGraceTicks() > 0) {
            return false;
        }

        AntarchyGravityDirection gravityDirection = AntarchyGravityApi.getGravityDirection(focus);
        Vec3 localMotion = AntarchyGravityRotationUtil.vecWorldToPlayer(focus.getDeltaMovement(), gravityDirection);
        double descentThreshold = Math.max(0.02D, this.cfgDangerousFallSpeed);
        boolean invertedGravity = gravityDirection.isInverted();
        if (invertedGravity ? localMotion.y < descentThreshold : localMotion.y > -descentThreshold) {
            return false;
        }

        return this.getDangerousFallSupportDistance(focus) >= this.cfgDangerousFallDistance;
    }

    private void tickLookTarget() {
        Player focus = this.getFocusPlayer();
        if (focus != null) {
            this.getLookControl().setLookAt(focus, 30.0F, 30.0F);
            return;
        }

        Player noticed = this.getNoticedPlayer();
        if (noticed != null) {
            this.getLookControl().setLookAt(noticed, 30.0F, 30.0F);
        }
    }

    private void applyPassiveDriftDamping() {
        Vec3 motion = this.getDeltaMovement();
        double damping = this.getMood() == Mood.RED ? 0.88D : 0.82D;
        if (motion.lengthSqr() < 1.0E-4D) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        this.setDeltaMovement(motion.scale(damping));
    }

    private void updateAnimationState() {
        Vec3 delta = this.getDeltaMovement();
        this.setAnimationState(delta.lengthSqr() > 0.01D ? ANIM_FLY : ANIM_IDLE);
    }

    private int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int state) {
        if (this.getAnimationState() != state) {
            this.entityData.set(ANIMATION_STATE, state);
        }
    }

    private void transitionToMood(Mood mood) {
        Mood current = this.getMood();
        if (current == mood) {
            return;
        }

        this.setMoodRaw(mood);
        if (mood == Mood.BLUE || mood == Mood.PURPLE || mood == Mood.RED) {
            this.queueMoodSound(mood);
        } else if (mood != Mood.YELLOW || this.pendingJoinSound) {
            this.playMoodSound(mood);
        }
        if (mood == Mood.YELLOW) {
            this.pendingJoinSound = false;
        }
        if (this.getFocusPlayer() != null && mood != Mood.WHITE) {
            this.interestTicks = this.cfgInterestDuration;
        }
    }

    public void syncFocusInversionState() {
        Player focus = this.getFocusPlayer();
        if (focus == null) {
            this.trackedFocusInvertedInitialized = false;
            return;
        }

        this.syncFocusInversionState(focus);
    }

    private void syncFocusInversionState(Player focus) {
        boolean inverted = focus.hasEffect(AntarchyObjects.INVERTED_EFFECT.get());
        if (!this.trackedFocusInvertedInitialized) {
            this.trackedFocusInverted = inverted;
            this.trackedFocusInvertedInitialized = true;
            return;
        }

        if (!this.trackedFocusInverted && inverted) {
            this.trackedFocusInverted = inverted;
            this.triggerInversionReaction(focus);
            return;
        }

        this.trackedFocusInverted = inverted;
    }

    private void triggerInversionReaction(Player focus) {
        if (this.shouldSuppressSlowFalling(focus)) {
            return;
        }
        this.inversionReactionTicks = INVERSION_REACTION_TICKS;
        this.transitionToMood(Mood.PURPLE);
        focus.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, INVERSION_REACTION_TICKS, 0, false, true, true));
    }

    private void playMoodSound(Mood mood) {
        SoundEvent sound = switch (mood) {
            case WHITE -> null;
            case YELLOW -> AntarchySoundEvents.REVERIE_JOIN_PLAYER.get();
            case BLUE -> AntarchySoundEvents.REVERIE_WORRY.get();
            case PURPLE -> AntarchySoundEvents.REVERIE_SAVE.get();
            case RED -> AntarchySoundEvents.REVERIE_ALERT.get();
        };
        if (sound != null) {
            this.playSound(sound, 0.9F, 0.95F + this.random.nextFloat() * 0.1F);
        }
    }

    private void queueMoodSound(Mood mood) {
        this.pendingMoodSound = mood;
        this.pendingMoodSoundTicks = 2 + this.random.nextInt(7);
    }

    private void tickPendingMoodSound() {
        if (this.pendingMoodSound == null) {
            return;
        }

        if (this.pendingMoodSoundTicks > 0) {
            this.pendingMoodSoundTicks--;
            return;
        }

        this.playMoodSound(this.pendingMoodSound);
        this.pendingMoodSound = null;
    }

    private void setMoodRaw(Mood mood) {
        this.entityData.set(MOOD, mood.ordinal());
    }

    private void tickClientEffects() {
    }

    @Nullable
    private Player getFocusPlayer() {
        if (this.cachedFocusPlayer != null && this.cachedFocusPlayer.isAlive() && this.cachedFocusPlayer.getUUID().equals(this.focusPlayerUuid)) {
            return this.cachedFocusPlayer;
        }
        if (this.focusPlayerUuid == null) {
            this.cachedFocusPlayer = null;
            return null;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            this.cachedFocusPlayer = serverLevel.getPlayerByUUID(this.focusPlayerUuid);
            return this.cachedFocusPlayer;
        }
        return null;
    }

    private void setFocusPlayer(@Nullable Player player) {
        this.cachedFocusPlayer = player;
        this.focusPlayerUuid = player != null ? player.getUUID() : null;
        this.trackedFocusInvertedInitialized = false;
    }

    private void clearFocusState() {
        this.setFocusPlayer(null);
        this.interestTicks = 0;
        this.warningCycleTicks = 0;
        this.inversionReactionTicks = 0;
        this.pendingJoinSound = false;
        this.pendingMoodSound = null;
        this.pendingMoodSoundTicks = 0;
        this.trackedFocusInvertedInitialized = false;
    }

    private void awardJoinAdvancement(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            AntarchyAdvancements.award(serverPlayer, HEY_LISTEN_ADVANCEMENT);
        }
    }

    private void releaseFocus() {
        this.clearFocusState();
        this.rebindCooldownTicks = this.cfgRebindCooldown;
    }

    @Nullable
    private Player getNoticedPlayer() {
        if (this.cachedNoticedPlayer != null && this.cachedNoticedPlayer.isAlive() && this.cachedNoticedPlayer.getUUID().equals(this.noticedPlayerUuid)) {
            return this.cachedNoticedPlayer;
        }
        if (this.noticedPlayerUuid == null) {
            this.cachedNoticedPlayer = null;
            return null;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            this.cachedNoticedPlayer = serverLevel.getPlayerByUUID(this.noticedPlayerUuid);
            return this.cachedNoticedPlayer;
        }
        return null;
    }

    private void setNoticedPlayer(@Nullable Player player) {
        this.cachedNoticedPlayer = player;
        this.noticedPlayerUuid = player != null ? player.getUUID() : null;
    }

    private void clearNoticePlayer() {
        this.setNoticedPlayer(null);
        this.noticeTicks = 0;
    }

    @Nullable
    private Player findNoticeCandidate() {
        if (this.tickCount < this.nextNoticeScanTick) {
            return null;
        }
        this.nextNoticeScanTick = this.tickCount + 20;
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(this.cfgInterestRadius)
        );

        Player nearest = null;
        double nearestDistanceSqr = Double.MAX_VALUE;
        for (Player player : nearbyPlayers) {
            if (!this.isValidNoticePlayer(player)) {
                continue;
            }
            double distanceSqr = this.distanceToSqr(player);
            if (distanceSqr < nearestDistanceSqr) {
                nearest = player;
                nearestDistanceSqr = distanceSqr;
            }
        }
        return nearest;
    }

    @Nullable
    private Player findDuplicationItemHolder() {
        if (this.tickCount < this.nextDuplicationScanTick) {
            return null;
        }
        this.nextDuplicationScanTick = this.tickCount + 20;
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(this.cfgInterestRadius)
        );

        Player nearest = null;
        double nearestDistanceSqr = Double.MAX_VALUE;
        for (Player player : nearbyPlayers) {
            if (!this.isValidBoundPlayer(player) || !this.isHoldingDuplicationItem(player)) {
                continue;
            }

            double distanceSqr = this.distanceToSqr(player);
            if (distanceSqr < nearestDistanceSqr) {
                nearest = player;
                nearestDistanceSqr = distanceSqr;
            }
        }
        return nearest;
    }

    private boolean isValidNoticePlayer(@Nullable Player player) {
        double maxDistance = this.cfgInterestRadius;
        return player != null
                && player.isAlive()
                && !player.isSpectator()
                && !player.isCreative()
                && this.distanceToSqr(player) <= maxDistance * maxDistance;
    }

    private boolean isHoldingDuplicationItem(Player player) {
        return player.getMainHandItem().is(AntarchyTags.Items.REVERIE_DUPLICATION_ITEMS)
                || player.getOffhandItem().is(AntarchyTags.Items.REVERIE_DUPLICATION_ITEMS);
    }

    private boolean isValidBoundPlayer(@Nullable Player player) {
        double maxDistance = this.cfgAbandonDistance;
        return player != null
                && player.isAlive()
                && !player.isSpectator()
                && !player.isCreative()
                && this.distanceToSqr(player) <= maxDistance * maxDistance;
    }

    @Nullable
    private LivingEntity findWarningThreat(Player focus) {
        LivingEntity lastAttacker = this.getLastHurtByMob();
        if (this.damageReactionTicks > 0 && this.isValidThreat(lastAttacker, focus)) {
            this.cachedWarningThreat = lastAttacker;
            return this.cachedWarningThreat;
        }

        if (this.tickCount < this.nextThreatScanTick) {
            return this.cachedWarningThreat != null && this.cachedWarningThreat.isAlive() ? this.cachedWarningThreat : null;
        }

        this.nextThreatScanTick = this.tickCount + 10;
        AABB threatArea = focus.getBoundingBox().inflate(this.cfgWarningThreatRadius, this.cfgWarningThreatVerticalRange, this.cfgWarningThreatRadius);
        List<LivingEntity> threats = this.level().getEntitiesOfClass(LivingEntity.class, threatArea, entity -> this.isValidThreat(entity, focus));

        LivingEntity prioritized = null;
        double prioritizedDistance = Double.MAX_VALUE;
        LivingEntity fallback = null;
        double fallbackDistance = Double.MAX_VALUE;
        for (LivingEntity threat : threats) {
            double distance = focus.distanceToSqr(threat);
            if (threat.getLastHurtMob() == focus || (threat instanceof Mob mob && mob.getTarget() == focus)) {
                if (distance < prioritizedDistance) {
                    prioritized = threat;
                    prioritizedDistance = distance;
                }
            }
            if (distance < fallbackDistance) {
                fallback = threat;
                fallbackDistance = distance;
            }
        }
        this.cachedWarningThreat = prioritized != null ? prioritized : fallback;
        return this.cachedWarningThreat;
    }

    private boolean isValidThreat(@Nullable LivingEntity entity, Player focus) {
        return entity != null
                && entity.isAlive()
                && entity != this
                && entity != focus
                && entity instanceof Enemy;
    }

    private boolean isPlayerInDangerousFall(Player focus) {
        return this.isPlayerCurrentlyFallingDangerously(focus);
    }

    private int getDangerousFallSupportDistance(Player focus) {
        int scanRange = Math.max(PURPLE_SUPPORT_SCAN_RANGE, Mth.ceil(this.cfgDangerousFallDistance) + 2);
        return this.getGravitySupportDistance(focus, scanRange);
    }

    private int getGravitySupportDistance(Player focus, int maxDistanceBlocks) {
        Direction gravityDown = AntarchyGravityRotationUtil.getGravityDownDirection(focus);
        Direction supportFace = gravityDown.getOpposite();
        BlockPos cursor = focus.blockPosition();
        for (int distance = 1; distance <= maxDistanceBlocks; distance++) {
            cursor = cursor.relative(gravityDown);
            BlockState supportState = this.level().getBlockState(cursor);
            if (supportState.isFaceSturdy(this.level(), cursor, supportFace)) {
                return distance;
            }
        }
        return -1;
    }

    private boolean isPlayerInsideAntiwater(Player focus) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos min = focus.blockPosition().offset(-1, 0, -1);
        BlockPos max = focus.blockPosition().offset(1, 2, 1);
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    if (PotentNyxiteBlock.isAntiwater(this.level().getFluidState(cursor))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Vec3 getOrbitTarget(Player focus) {
        double desiredMin = Math.min(this.cfgFollowMinDistance, this.cfgFollowMaxDistance);
        double desiredMax = Math.max(this.cfgFollowMinDistance, this.cfgFollowMaxDistance);
        double orbitTime = (this.tickCount * 0.08D) + (this.getId() * 0.53D);
        double distance = this.distanceTo(focus);
        double orbitBlend = Mth.clamp((distance - desiredMin) / Math.max(0.001D, desiredMax - desiredMin), 0.0D, 1.0D);
        double radius = Mth.lerp(1.0D - orbitBlend, desiredMin * 0.35D, desiredMax * 0.85D);
        Vec3 tetherAnchor = focus.position().add(focus.getDeltaMovement().scale(0.75D));
        double angle = orbitTime * (this.getId() % 2 == 0 ? 1.0D : -1.0D);
        double verticalOffset = AntarchyGravityApi.isGravityInverted(focus) ? -2.15D : 1.15D;
        double y = tetherAnchor.y + verticalOffset + Math.sin(orbitTime * 0.8D) * 0.2D;
        return new Vec3(
                tetherAnchor.x + Math.cos(angle) * radius,
                Mth.clamp(y, this.level().getMinBuildHeight() + 2.0D, this.level().getMaxBuildHeight() - 2.0D),
                tetherAnchor.z + Math.sin(angle) * radius
        );
    }

    private Vec3 getPurpleProtectTarget(Player focus) {
        double verticalOffset = AntarchyGravityApi.isGravityInverted(focus) ? -0.95D : 0.95D;
        Vec3 tetherAnchor = focus.position().add(focus.getDeltaMovement().scale(0.85D));
        Vec3 sideOffset = Vec3.directionFromRotation(0.0F, focus.getYRot() + 90.0F).scale(0.45D);
        return new Vec3(
                tetherAnchor.x + sideOffset.x,
                Mth.clamp(tetherAnchor.y + verticalOffset, this.level().getMinBuildHeight() + 2.0D, this.level().getMaxBuildHeight() - 2.0D),
                tetherAnchor.z + sideOffset.z
        );
    }

    private Vec3 getWarningTarget(Player focus, LivingEntity threat) {
        int approachTicks = this.cfgWarningApproachTicks;
        int hoverTicks = this.cfgWarningHoverTicks;
        int returnTicks = this.cfgWarningReturnTicks;
        int playerHoverTicks = this.cfgWarningPlayerHoverTicks;
        int phaseLength = approachTicks + hoverTicks + returnTicks + playerHoverTicks;
        int phase = this.warningCycleTicks % Math.max(1, phaseLength);
        if (phase < approachTicks) {
            return threat.position().add(0.0D, threat.getBbHeight() * 0.65D, 0.0D);
        }
        if (phase < approachTicks + hoverTicks) {
            return threat.position().add(0.0D, threat.getBbHeight() * 1.05D, 0.0D);
        }
        if (phase < approachTicks + hoverTicks + returnTicks) {
            return this.getOrbitTarget(focus);
        }
        return focus.position().add(0.0D, 1.8D, 0.0D);
    }

    private double getWarningSpeed(Player focus) {
        return this.distanceTo(focus) > this.cfgCatchUpDistance ? 0.42D : 0.34D;
    }

    private void pulseToward(Vec3 target, double impulse, double maxSpeed, int pulseInterval) {
        Vec3 toTarget = target.subtract(this.position());
        double distance = toTarget.length();
        if (distance < 0.08D) {
            return;
        }

        if (this.movementPulseCooldown > 0 && distance < 1.25D && maxSpeed < 0.5D) {
            return;
        }

        Vec3 direction = toTarget.scale(1.0D / distance);
        Vec3 nextMotion = this.getDeltaMovement().scale(0.55D).add(direction.scale(Math.min(impulse, 0.1D + distance * 0.08D)));
        if (nextMotion.lengthSqr() > maxSpeed * maxSpeed) {
            nextMotion = nextMotion.normalize().scale(maxSpeed);
        }

        this.setDeltaMovement(nextMotion);
        this.hasImpulse = true;
        this.movementPulseCooldown = pulseInterval;
    }

    private Vec3 pickAmbientTarget() {
        double angle = this.random.nextDouble() * (Math.PI * 2.0D);
        double maxRadius = Math.max(this.cfgAmbientMinRadius, this.cfgAmbientMaxRadius);
        double radius = this.cfgAmbientMinRadius + this.random.nextDouble() * Math.max(0.0D, maxRadius - this.cfgAmbientMinRadius);
        double vertical = (this.random.nextDouble() - 0.5D) * this.cfgAmbientVerticalRange;
        return new Vec3(
                this.getX() + Math.cos(angle) * radius,
                Mth.clamp(this.getY() + vertical, this.level().getMinBuildHeight() + 2.0D, this.level().getMaxBuildHeight() - 2.0D),
                this.getZ() + Math.sin(angle) * radius
        );
    }

    public enum Mood {
        YELLOW,
        BLUE,
        PURPLE,
        RED,
        WHITE;

        private static final Mood[] VALUES = values();

        private static Mood byOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length) {
                return WHITE;
            }
            return VALUES[ordinal];
        }
    }

    private final class ReverieBoundGoal extends Goal {
        private ReverieBoundGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return ReverieEntity.this.getFocusPlayer() != null;
        }

        @Override
        public boolean canContinueToUse() {
            return ReverieEntity.this.getFocusPlayer() != null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Player focus = ReverieEntity.this.getFocusPlayer();
            if (focus == null) {
                return;
            }

            Mood mood = ReverieEntity.this.getMood();
            double distance = ReverieEntity.this.distanceTo(focus);
            double catchUpDistance = ReverieEntity.this.cfgCatchUpDistance;
            boolean urgentCatchUp = distance > catchUpDistance;
            if (mood == Mood.PURPLE) {
                Vec3 target = ReverieEntity.this.getPurpleProtectTarget(focus);
                ReverieEntity.this.pulseToward(target, urgentCatchUp ? 0.72D : 0.48D, urgentCatchUp ? 1.20D : 0.85D, 1);
                return;
            }

            if (mood == Mood.RED) {
                LivingEntity threat = ReverieEntity.this.findWarningThreat(focus);
                Vec3 target = threat != null
                        ? ReverieEntity.this.getWarningTarget(focus, threat)
                        : ReverieEntity.this.getOrbitTarget(focus);
                ReverieEntity.this.pulseToward(target, urgentCatchUp ? 0.45D : 0.24D, urgentCatchUp ? 1.05D : ReverieEntity.this.getWarningSpeed(focus), urgentCatchUp ? 1 : 5);
                return;
            }

            Vec3 orbitTarget = ReverieEntity.this.getOrbitTarget(focus);
            double desiredMax = Math.max(ReverieEntity.this.cfgFollowMinDistance, ReverieEntity.this.cfgFollowMaxDistance);
            boolean worried = mood == Mood.BLUE;
            if (worried && distance <= desiredMax && !focus.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                focus.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, true, true));
            }
            double impulse = urgentCatchUp ? 0.56D : (worried ? 0.32D : (distance > desiredMax ? 0.24D : 0.18D));
            double maxSpeed = urgentCatchUp ? 1.05D : (worried ? 0.68D : (distance > desiredMax ? 0.50D : 0.32D));
            int pulseInterval = urgentCatchUp ? 1 : (worried ? 2 : (distance > desiredMax ? 3 : 5));
            ReverieEntity.this.pulseToward(orbitTarget, impulse, maxSpeed, pulseInterval);
        }
    }

    private final class ReverieAmbientGoal extends Goal {
        private ReverieAmbientGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return ReverieEntity.this.getFocusPlayer() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return ReverieEntity.this.getFocusPlayer() == null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (ReverieEntity.this.getNoticedPlayer() != null) {
                ReverieEntity.this.ambientTarget = null;
                ReverieEntity.this.ambientTargetAge = 0;
                return;
            }

            if (ReverieEntity.this.ambientTarget == null
                    || ReverieEntity.this.ambientTarget.distanceTo(ReverieEntity.this.position()) < 0.8D
                    || ReverieEntity.this.ambientTargetAge++ > ReverieEntity.this.cfgAmbientMaxAgeTicks) {
                ReverieEntity.this.ambientTarget = ReverieEntity.this.pickAmbientTarget();
                ReverieEntity.this.ambientTargetAge = 0;
            }

            int minInterval = ReverieEntity.this.cfgAmbientPulseMinTicks;
            int maxInterval = Math.max(minInterval, ReverieEntity.this.cfgAmbientPulseMaxTicks);
            ReverieEntity.this.pulseToward(
                    ReverieEntity.this.ambientTarget,
                    0.14D,
                    0.18D,
                    minInterval + ReverieEntity.this.random.nextInt(Math.max(1, maxInterval - minInterval + 1))
            );
        }
    }
}

