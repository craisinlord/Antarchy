package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MolevoreEntity extends Monster implements GeoEntity {
    private static final ResourceKey<Biome> MOLEWORM_CAVES = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "moleworm_caves")
    );
    private static final ResourceKey<Biome> CAVARYN_MOLEWORM_CAVES = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_moleworm_caves")
    );
    private static final EntityDataAccessor<Integer> ACTION_STATE = SynchedEntityData.defineId(MolevoreEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SUBMERGED = SynchedEntityData.defineId(MolevoreEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ACTION_SERIAL = SynchedEntityData.defineId(MolevoreEntity.class, EntityDataSerializers.INT);

    private static final int ACTION_NONE = 0;
    private static final int ACTION_DIG_UP = 1;
    private static final int ACTION_CLAW_SWIPE = 2;
    private static final int ACTION_SPIN_CHARGE = 3;
    private static final int ACTION_HURT = 4;
    private static final int ACTION_DEATH = 5;
    private static final int ACTION_IDLE_DIG = 6;
    private static final int ACTION_DIG_DOWN = 7;
    private static final int ACTION_TUNNEL_DIG = 8;

    private static final int DIG_UP_TICKS = 60;
    private static final int DIG_UP_SUBMERGED_TICKS = 10;
    private static final int DIG_DOWN_TICKS = 50;
    private static final int DIG_DOWN_SUBMERGE_TICK = 37;
    private static final int SWIPE_TICKS = 31;
    private static final int SWIPE_COMMIT_TICK = 12;
    private static final int SWIPE_FIRST_HIT_TICK = 18;
    private static final int SWIPE_SECOND_HIT_TICK = 25;
    private static final float SWIPE_SECOND_HIT_MULTIPLIER = 0.6F;
    private static final int SWIPE_COOLDOWN_TICKS = 20;
    private static final int SPIN_TOTAL_TICKS = 50;
    private static final int SPIN_LOCK_TICK = 10;
    private static final int SPIN_LAUNCH_TICK = 15;
    private static final int SPIN_RECOVERY_TICK = 32;
    private static final int SPIN_COOLDOWN_TICKS = 42;
    private static final int WALL_STAGGER_TICKS = 17;
    private static final int WALL_STAGGER_COOLDOWN_TICKS = 60;
    private static final int HURT_TICKS = 10;
    private static final int FLINCH_LOCKOUT_TICKS = 40;
    private static final int DEATH_TICKS = 44;
    private static final int NO_PATH_RESPONSE_TICKS = 100;
    private static final int TUNNEL_RETRY_NO_PATH_TICKS = 70;
    private static final int BURROW_COOLDOWN_TICKS = 300;
    private static final int BURROW_RETRY_TICKS = 100;
    private static final int BURROW_SEARCH_ATTEMPTS = 16;
    private static final int ANIMATION_TRANSITION_TICKS = 4;

    private static final float TRACKING_TURN_DEGREES = 12.0F;
    private static final float SPIN_ALIGN_TURN_DEGREES = 36.0F;
    private static final double SWIPE_MAX_TARGET_ABOVE = 2.4D;
    private static final double SWIPE_MAX_TARGET_BELOW = 2.0D;
    private static final double SPIN_MAX_TARGET_ELEVATION = 2.0D;
    private static final double BURROW_MIN_TARGET_DISTANCE = 5.0D;
    private static final double BURROW_EMERGE_MIN_RADIUS = 3.0D;
    private static final double BURROW_EMERGE_RADIUS_RANGE = 5.0D;
    private static final double IDLE_DIG_PLAYER_RANGE = 24.0D;
    private static final double CHASE_WALK_ANIMATION_SPEED = 1.6D;

    private static final double CHASE_SPEED = 1.05D;
    private static final double WANDER_SPEED = 0.8D;
    private static final double SPIN_SPEED = 1.15D;
    private static final double SWIPE_RANGE = 3.6D;
    private static final double SPIN_RANGE_MIN = 4.0D;
    private static final double SPIN_RANGE_MAX = 14.0D;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");
    private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("hurt");
    private static final RawAnimation DIG_UP_ANIM = RawAnimation.begin().thenPlay("digging up");
    private static final RawAnimation DIG_DOWN_ANIM = RawAnimation.begin().thenPlay("digging down");
    private static final RawAnimation CLAW_SWIPE_ANIM = RawAnimation.begin().thenPlay("claw swiping");
    private static final RawAnimation SPINNING_ANIM = RawAnimation.begin().thenPlay("spinning");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Set<UUID> actionHitEntities = new HashSet<>();

    private int actionTicks;
    private int animatedActionSerial;
    private int attackCooldown;
    private int idleDigCooldown;
    private int flinchLockout;
    private int noPathTicks;
    private int burrowCooldown;
    private int pacifiedTicks;
    private int swipeHitsApplied;
    private float committedYaw;
    private Vec3 spinDirection = Vec3.ZERO;
    private Vec3 tunnelDirection = Vec3.ZERO;
    @Nullable
    private Vec3 burrowEmergeSpot;
    private int nextChaseRepathTick;
    @Nullable
    private Vec3 lastChaseTargetPos;

    public MolevoreEntity(EntityType<? extends MolevoreEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 20;
        this.idleDigCooldown = 100;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.molevoreHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.molevoreAttackDamage())
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    public static boolean canSpawn(EntityType<MolevoreEntity> entityType, LevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return hasSpawnPocket(level, pos);
    }

    private static boolean hasSpawnPocket(LevelAccessor level, BlockPos pos) {
        if (!level.getBiome(pos).is(MOLEWORM_CAVES) && !level.getBiome(pos).is(CAVARYN_MOLEWORM_CAVES)) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos airPos = pos.offset(dx, 0, dz);
                if (!level.getBlockState(airPos).isAir() || !level.getBlockState(airPos.above()).isAir()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION_STATE, ACTION_NONE);
        builder.define(SUBMERGED, false);
        builder.define(ACTION_SERIAL, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new TruffleLureGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, WANDER_SPEED) {
            @Override
            public boolean canUse() {
                return MolevoreEntity.this.canStroll() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return MolevoreEntity.this.canStroll() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (target, serverLevel) -> !MoleTruffles.lures(target)));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main_controller", ANIMATION_TRANSITION_TICKS, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationTest<MolevoreEntity> state) {
        int action = this.getActionState();
        int actionSerial = this.entityData.get(ACTION_SERIAL);
        if (actionSerial != this.animatedActionSerial) {
            this.animatedActionSerial = actionSerial;
            state.controller().reset();
        }
        int transitionTicks = action == ACTION_DIG_UP ? 0 : ANIMATION_TRANSITION_TICKS;
        if (state.controller().getTransitionTicks() != transitionTicks) {
            state.controller().setTransitionTicks(transitionTicks);
            return state.controller().getCurrentRawAnimation() == null ? PlayState.STOP : PlayState.CONTINUE;
        }
        state.controller().setAnimationSpeed(action == ACTION_NONE && state.isMoving() && this.isAggressive() ? CHASE_WALK_ANIMATION_SPEED : 1.0D);
        return switch (action) {
            case ACTION_DIG_UP -> state.setAndContinue(DIG_UP_ANIM);
            case ACTION_DIG_DOWN -> state.setAndContinue(DIG_DOWN_ANIM);
            case ACTION_CLAW_SWIPE, ACTION_IDLE_DIG, ACTION_TUNNEL_DIG -> state.setAndContinue(CLAW_SWIPE_ANIM);
            case ACTION_SPIN_CHARGE -> state.setAndContinue(SPINNING_ANIM);
            case ACTION_HURT -> state.setAndContinue(HURT_ANIM);
            case ACTION_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> state.setAndContinue(state.isMoving() ? WALK_ANIM : IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData finalized = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        ConfiguredMobSpawnUtil.applyConfiguredHealth(this, AntarchySettings.molevoreHealth());
        if (spawnReason != EntitySpawnReason.SPAWN_ITEM_USE) {
            this.startDigUp();
        }
        return finalized;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (this.isBodyLockedToYaw()) {
                this.yBodyRot = Mth.approachDegrees(this.yBodyRot, this.getYRot(), SPIN_ALIGN_TURN_DEGREES);
            }
            return;
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.idleDigCooldown > 0) {
            this.idleDigCooldown--;
        }
        if (this.flinchLockout > 0) {
            this.flinchLockout--;
        }
        if (this.burrowCooldown > 0) {
            this.burrowCooldown--;
        }
        if (this.pacifiedTicks > 0) {
            this.pacifiedTicks--;
            MoleTruffles.tickCalm(this);
        }

        if (this.getActionState() == ACTION_DEATH) {
            return;
        }

        switch (this.getActionState()) {
            case ACTION_DIG_UP -> this.tickDigUp();
            case ACTION_DIG_DOWN -> this.tickDigDown();
            case ACTION_TUNNEL_DIG -> this.tickTunnelDig();
            case ACTION_CLAW_SWIPE -> this.tickClawSwipe();
            case ACTION_SPIN_CHARGE -> this.tickSpinCharge();
            case ACTION_HURT -> this.tickHurtAnimation();
            case ACTION_IDLE_DIG -> this.tickIdleDig();
            default -> this.tickCombat();
        }
    }

    @Override
    public boolean hasLineOfSight(Entity entity) {
        Vec3 start = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 end = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        Vec3 delta = end.subtract(start);
        double distance = delta.length();
        if (distance < 1.0E-6D) {
            return true;
        }
        int steps = Math.max(1, Mth.ceil(distance / 0.5D));
        Vec3 step = delta.scale(1.0D / steps);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int lastX = Integer.MIN_VALUE;
        int lastY = Integer.MIN_VALUE;
        int lastZ = Integer.MIN_VALUE;

        for (int i = 0; i <= steps; i++) {
            Vec3 sample = start.add(step.scale(i));
            int bx = Mth.floor(sample.x);
            int by = Mth.floor(sample.y);
            int bz = Mth.floor(sample.z);
            if (bx == lastX && by == lastY && bz == lastZ) {
                continue;
            }
            lastX = bx;
            lastY = by;
            lastZ = bz;
            cursor.set(bx, by, bz);

            BlockState state = this.level().getBlockState(cursor);
            if (state.isAir()
                    || state.is(AntarchyTags.Blocks.MOLEVORE_SEE_THROUGH_BLOCKS)
                    || state.getCollisionShape(this.level(), cursor).isEmpty()) {
                continue;
            }

            return false;
        }

        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }

        boolean hurt = super.hurtServer(level, source, amount);
        if (!hurt || this.level().isClientSide()) {
            return hurt;
        }

        if (source.getEntity() instanceof LivingEntity) {
            this.pacifiedTicks = 0;
        }

        if (!this.isDeadOrDying()
                && this.getActionState() == ACTION_NONE
                && source.getEntity() != null
                && this.flinchLockout <= 0) {
            this.setActionState(ACTION_HURT);
            this.actionTicks = HURT_TICKS;
            this.flinchLockout = FLINCH_LOCKOUT_TICKS;
        }

        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.pacifiedTicks <= 0 && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return super.isInvulnerableTo(level, source)
                || (this.isSubmerged() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY));
    }

    @Override
    public boolean isPickable() {
        return !this.isSubmerged() && super.isPickable();
    }

    @Override
    public boolean isPushable() {
        return !this.isSubmerged() && super.isPushable();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.pacifiedTicks <= 0 && target instanceof Player player && !player.isCreative() && !player.isSpectator() && super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(this.pacifiedTicks > 0 ? null : target);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!MoleTruffles.isTruffle(player.getItemInHand(hand)) || this.isDeadOrDying()) {
            return super.mobInteract(player, hand);
        }
        if (this.level().isClientSide()) {
            return InteractionResult.CONSUME;
        }
        if (this.getActionState() != ACTION_NONE) {
            return InteractionResult.CONSUME;
        }
        this.setAggressive(false);
        this.pacifiedTicks = MoleTruffles.feed(this, player, hand, this.pacifiedTicks);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(MoleTruffles.PACIFIED_KEY, this.pacifiedTicks);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.pacifiedTicks = input.getIntOr(MoleTruffles.PACIFIED_KEY, 0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        int action = this.getActionState();
        return action == ACTION_DIG_UP || action == ACTION_DIG_DOWN ? null : AntarchySoundEvents.MOLEVORE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.MOLEVORE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.MOLEVORE_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(AntarchySoundEvents.MOLEVORE_DIG.get(), 0.18F, 0.9F);
    }

    @Override
    protected float getSoundVolume() {
        return 0.9F;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos.below()).is(AntarchyTags.Blocks.MOLEVORE_SEE_THROUGH_BLOCKS)
                ? 8.0F
                : super.getWalkTargetValue(pos, level);
    }

    @Override
    public float maxUpStep() {
        return 1.0F;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.level().isClientSide() || this.getActionState() != ACTION_SPIN_CHARGE) {
            super.travel(travelVector);
            return;
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.96D));
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide() && this.getActionState() != ACTION_DEATH) {
            this.setActionState(ACTION_DEATH);
            this.actionTicks = DEATH_TICKS;
            this.attackCooldown = 0;
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 1 && !this.level().isClientSide()) {
            this.setActionState(ACTION_DEATH);
            this.setSubmerged(false);
        }
        if (this.deathTime >= DEATH_TICKS) {
            this.remove(RemovalReason.KILLED);
        }
    }

    private void tickCombat() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.setAggressive(false);
            this.noPathTicks = 0;
            this.dampHorizontalMovement(0.8D);
            if (this.idleDigCooldown <= 0 && this.random.nextFloat() < 0.01F) {
                this.startIdleDig();
            }
            return;
        }

        this.setAggressive(true);
        this.getLookControl().setLookAt(target, 30.0F, 20.0F);
        double distanceToTarget = this.distanceToSqr(target);

        if (this.attackCooldown <= 0) {
            if (distanceToTarget <= SWIPE_RANGE * SWIPE_RANGE && this.canSwipeTarget(target)) {
                this.startClawSwipe();
                return;
            }

            if (distanceToTarget >= SPIN_RANGE_MIN * SPIN_RANGE_MIN
                    && distanceToTarget <= SPIN_RANGE_MAX * SPIN_RANGE_MAX
                    && this.random.nextFloat() < 0.05F
                    && this.canSpinAt(target)) {
                this.startSpinCharge(target);
                return;
            }
        }

        if (this.tickCount >= this.nextChaseRepathTick
                || this.lastChaseTargetPos == null
                || target.distanceToSqr(this.lastChaseTargetPos) > 1.0D) {
            this.lastChaseTargetPos = target.position();
            this.nextChaseRepathTick = this.tickCount + (this.getNavigation().moveTo(target, CHASE_SPEED) ? 4 : 10);
        }

        if (this.horizontalCollision && this.canBreakBlocks()) {
            this.breakTaggedBlocksInFront(1.7D, 2.0D, 1.0D);
        }

        this.tickNoPathResponse(target);
    }

    private void tickNoPathResponse(LivingEntity target) {
        Path path = this.getNavigation().getPath();
        if (path != null && path.canReach()) {
            this.noPathTicks = 0;
            return;
        }

        if (++this.noPathTicks < NO_PATH_RESPONSE_TICKS) {
            return;
        }

        Vec3 toTarget = this.toLocalPlane(target.position().subtract(this.position()));
        if (toTarget.lengthSqr() < BURROW_MIN_TARGET_DISTANCE * BURROW_MIN_TARGET_DISTANCE || this.burrowCooldown > 0) {
            return;
        }

        Vec3 direction = this.toWorld(toTarget.normalize());
        if (this.canBreakBlocks() && this.hasTaggedBlocksAhead(direction)) {
            this.noPathTicks = TUNNEL_RETRY_NO_PATH_TICKS;
            this.startTunnelDig(direction);
            return;
        }

        this.noPathTicks = 0;
        if (!this.canStartBurrow()) {
            this.burrowCooldown = BURROW_RETRY_TICKS;
            return;
        }

        Vec3 emergeSpot = this.findEmergeSpot(target);
        if (emergeSpot == null) {
            this.burrowCooldown = BURROW_RETRY_TICKS;
            return;
        }

        this.burrowEmergeSpot = emergeSpot;
        this.startDigDown();
    }

    private boolean canSwipeTarget(LivingEntity target) {
        double elevation = this.toLocal(target.position().subtract(this.position())).y;
        return elevation <= SWIPE_MAX_TARGET_ABOVE
                && elevation >= -SWIPE_MAX_TARGET_BELOW
                && this.hasLineOfSight(target);
    }

    private boolean canSpinAt(LivingEntity target) {
        Vec3 delta = target.position().subtract(this.position());
        if (this.isInWater()
                || Math.abs(this.toLocal(delta).y) > SPIN_MAX_TARGET_ELEVATION
                || !this.hasLineOfSight(target)) {
            return false;
        }

        Vec3 flat = this.toLocalPlane(delta);
        double distance = flat.length();
        if (distance < 1.0E-4D) {
            return false;
        }

        Vec3 forward = flat.scale(1.0D / distance);
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        boolean canBreak = this.canBreakBlocks();
        for (double travelled = 1.0D; travelled < distance; travelled += 1.0D) {
            for (double lateral = -0.6D; lateral <= 0.6D; lateral += 0.6D) {
                Vec3 local = forward.scale(travelled).add(side.scale(lateral)).add(0.0D, 1.5D, 0.0D);
                BlockPos pos = BlockPos.containing(this.position().add(this.toWorld(local)));
                BlockState state = this.level().getBlockState(pos);
                if (state.getCollisionShape(this.level(), pos).isEmpty()) {
                    continue;
                }
                if (!canBreak || !state.is(AntarchyTags.Blocks.MOLEVORE_BREAKABLE_BLOCKS)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasTaggedBlocksAhead(Vec3 worldDirection) {
        Vec3 forward = this.toLocalPlane(worldDirection).normalize();
        for (double distance = 1.5D; distance <= 2.5D; distance += 1.0D) {
            for (double height = 0.5D; height <= 1.5D; height += 1.0D) {
                Vec3 local = forward.scale(distance).add(0.0D, height, 0.0D);
                BlockState state = this.level().getBlockState(BlockPos.containing(this.position().add(this.toWorld(local))));
                if (state.is(AntarchyTags.Blocks.MOLEVORE_BREAKABLE_BLOCKS)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canStartBurrow() {
        return this.onGround()
                && !this.isInWater()
                && !this.isInLava()
                && !this.isPassenger()
                && !this.isLeashed()
                && this.gravityDirection() == GravityDirection.DOWN;
    }

    @Nullable
    private Vec3 findEmergeSpot(LivingEntity target) {
        double angleStart = this.random.nextDouble() * Math.PI * 2.0D;
        for (int attempt = 0; attempt < BURROW_SEARCH_ATTEMPTS; attempt++) {
            double angle = angleStart + attempt * Math.PI * 2.0D / BURROW_SEARCH_ATTEMPTS;
            double radius = BURROW_EMERGE_MIN_RADIUS + this.random.nextDouble() * BURROW_EMERGE_RADIUS_RANGE;
            int blockX = Mth.floor(target.getX() + Math.cos(angle) * radius);
            int blockZ = Mth.floor(target.getZ() + Math.sin(angle) * radius);
            for (int offsetY = 2; offsetY >= -3; offsetY--) {
                Vec3 candidate = new Vec3(blockX + 0.5D, target.getBlockY() + offsetY, blockZ + 0.5D);
                if (this.isValidEmergeSpot(candidate) && this.emergeSpotSees(candidate, target)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private boolean isValidEmergeSpot(Vec3 spot) {
        BlockPos feet = BlockPos.containing(spot);
        if (!this.level().hasChunkAt(feet)) {
            return false;
        }

        BlockPos floor = feet.below();
        if (!this.level().getBlockState(floor).entityCanStandOn(this.level(), floor, this)) {
            return false;
        }

        AABB box = this.getDimensions(this.getPose()).makeBoundingBox(spot).inflate(0.25D, 0.0D, 0.25D).expandTowards(0.0D, 1.0D, 0.0D);
        if (!this.level().noCollision(this, box) || this.level().containsAnyLiquid(box)) {
            return false;
        }

        PathType pathType = WalkNodeEvaluator.getPathTypeStatic(this, feet);
        return this.getPathfindingMalus(pathType) == 0.0F;
    }

    private boolean emergeSpotSees(Vec3 spot, LivingEntity target) {
        Vec3 start = spot.add(0.0D, 1.0D, 0.0D);
        return this.level().clip(new ClipContext(start, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private void startDigUp() {
        this.setActionState(ACTION_DIG_UP);
        this.actionTicks = DIG_UP_TICKS;
        this.attackCooldown = 30;
        this.setSubmerged(true);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void tickDigUp() {
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        if (this.actionTicks == DIG_UP_TICKS) {
            this.playSound(AntarchySoundEvents.MOLEVORE_DIG.get(), 0.9F, 0.75F);
        }
        this.setSubmerged(DIG_UP_TICKS - this.actionTicks < DIG_UP_SUBMERGED_TICKS);
        this.spawnDigParticles();
        if (--this.actionTicks <= 0) {
            this.setSubmerged(false);
            this.setActionState(ACTION_NONE);
        }
    }

    private void startDigDown() {
        this.setActionState(ACTION_DIG_DOWN);
        this.actionTicks = DIG_DOWN_TICKS;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.playSound(AntarchySoundEvents.MOLEVORE_DIG.get(), 0.9F, 0.6F);
    }

    private void tickDigDown() {
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.setSubmerged(DIG_DOWN_TICKS - this.actionTicks >= DIG_DOWN_SUBMERGE_TICK);
        this.spawnDigParticles();
        if (--this.actionTicks > 0) {
            return;
        }

        Vec3 emergeSpot = this.resolveEmergeSpot();
        if (emergeSpot != null) {
            this.teleportTo(emergeSpot.x, emergeSpot.y, emergeSpot.z);
        }
        this.burrowEmergeSpot = null;
        this.noPathTicks = 0;
        this.burrowCooldown = BURROW_COOLDOWN_TICKS;
        this.startDigUp();
    }

    @Nullable
    private Vec3 resolveEmergeSpot() {
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && target.level() == this.level()) {
            Vec3 nearTarget = this.findEmergeSpot(target);
            if (nearTarget != null) {
                return nearTarget;
            }
        }
        return this.burrowEmergeSpot != null && this.isValidEmergeSpot(this.burrowEmergeSpot) ? this.burrowEmergeSpot : null;
    }

    private void spawnDigParticles() {
        if (this.tickCount % 4 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    this.getX(),
                    this.getY() + 0.3D,
                    this.getZ(),
                    8,
                    this.getBbWidth() * 0.25D,
                    0.12D,
                    this.getBbWidth() * 0.25D,
                    0.02D
            );
        }
    }

    private void startClawSwipe() {
        this.setActionState(ACTION_CLAW_SWIPE);
        this.actionTicks = SWIPE_TICKS;
        this.swipeHitsApplied = 0;
        this.actionHitEntities.clear();
        this.committedYaw = this.getYRot();
        this.getNavigation().stop();
        this.dampHorizontalMovement(0.35D);
        this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 0.9F, 0.65F);
    }

    private void tickClawSwipe() {
        this.getNavigation().stop();
        this.dampHorizontalMovement(0.6D);
        int elapsed = SWIPE_TICKS - this.actionTicks;
        LivingEntity target = this.getTarget();
        if (elapsed < SWIPE_COMMIT_TICK && target != null) {
            this.turnBodyToward(target.position(), TRACKING_TURN_DEGREES);
            this.committedYaw = this.getYRot();
        } else {
            this.holdBodyYaw(this.committedYaw);
        }

        if (this.swipeHitsApplied == 0 && elapsed >= SWIPE_FIRST_HIT_TICK) {
            this.performClawSwipe(1.0F, false);
            if (this.canBreakBlocks()) {
                this.breakTaggedBlocksInDirection(this.committedForwardWorld(), 2.8D, 2.2D, 1.15D);
            }
            this.swipeHitsApplied = 1;
        } else if (this.swipeHitsApplied == 1 && elapsed >= SWIPE_SECOND_HIT_TICK) {
            this.performClawSwipe(SWIPE_SECOND_HIT_MULTIPLIER, true);
            this.swipeHitsApplied = 2;
        }

        if (--this.actionTicks <= 0) {
            this.actionHitEntities.clear();
            this.setActionState(ACTION_NONE);
            this.attackCooldown = SWIPE_COOLDOWN_TICKS;
        }
    }

    private void performClawSwipe(float damageMultiplier, boolean followUp) {
        AABB hitBox = this.getBoundingBox().inflate(3.0D, 1.3D, 3.0D);
        Vec3 forward = this.committedForwardLocal();
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::isValidSwipeTarget)) {
            Vec3 toEntity = this.toLocalPlane(entity.position().subtract(this.position()));
            if (toEntity.lengthSqr() > 1.0E-4D && forward.dot(toEntity.normalize()) < 0.05D) {
                continue;
            }
            if (!this.hasLineOfSight(entity)) {
                continue;
            }

            if (followUp && this.actionHitEntities.contains(entity.getUUID())) {
                entity.setInvulnerableTime(0);
            }
            Vec3 preHitMovement = entity.getDeltaMovement();
            if (this.level() instanceof ServerLevel serverLevel && entity.hurtServer(serverLevel, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier)) {
                this.actionHitEntities.add(entity.getUUID());
                if (followUp) {
                    Vec3 push = toEntity.lengthSqr() < 1.0E-4D ? forward : toEntity.normalize();
                    Vec3 worldPush = this.toWorld(push.x * 0.8D, 0.25D, push.z * 0.8D);
                    entity.push(worldPush.x, worldPush.y, worldPush.z);
                } else {
                    entity.setDeltaMovement(preHitMovement);
                }
                entity.syncVelocity = true;
            }
        }

        this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 1.0F, followUp ? 1.0F : 0.85F);
    }

    private void startSpinCharge(LivingEntity target) {
        Vec3 horizontal = this.toLocalPlane(target.position().subtract(this.position()));
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return;
        }

        this.spinDirection = this.toWorld(horizontal.normalize());
        this.actionHitEntities.clear();
        this.setActionState(ACTION_SPIN_CHARGE);
        this.actionTicks = SPIN_TOTAL_TICKS;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 1.2F, 0.75F);
    }

    private void tickSpinCharge() {
        this.getNavigation().stop();
        int elapsed = SPIN_TOTAL_TICKS - this.actionTicks;

        if (elapsed < SPIN_LAUNCH_TICK) {
            if (elapsed < SPIN_LOCK_TICK) {
                LivingEntity target = this.getTarget();
                if (target != null) {
                    Vec3 horizontal = this.toLocalPlane(target.position().subtract(this.position()));
                    if (horizontal.lengthSqr() > 1.0E-4D) {
                        this.spinDirection = this.toWorld(horizontal.normalize());
                    }
                }
                this.turnBodyToward(this.position().add(this.spinDirection), TRACKING_TURN_DEGREES);
            } else {
                this.turnBodyToward(this.position().add(this.spinDirection), SPIN_ALIGN_TURN_DEGREES);
            }
            this.committedYaw = this.getYRot();
            this.setSpinMovement(this.currentHorizontalMovement().scale(0.2D));
        } else if (elapsed < SPIN_RECOVERY_TICK) {
            this.committedYaw = this.yawOf(this.spinDirection);
            this.holdBodyYaw(this.committedYaw);
            this.setSpinMovement(this.spinDirection.scale(SPIN_SPEED));
            this.needsSync = true;
            this.performSpinChargeHit();

            if (this.horizontalCollision) {
                boolean brokeDirt = this.canBreakBlocks()
                        && this.breakTaggedBlocksInDirection(this.spinDirection, 1.8D, 2.2D, 1.1D);
                if (!brokeDirt) {
                    this.startWallStagger();
                    return;
                }
            }
            if (this.isInWater()) {
                this.actionTicks = SPIN_TOTAL_TICKS - SPIN_RECOVERY_TICK;
            }
        } else {
            this.holdBodyYaw(this.committedYaw);
            this.setSpinMovement(this.currentHorizontalMovement().scale(0.5D));
        }

        if (--this.actionTicks <= 0) {
            this.actionHitEntities.clear();
            this.setActionState(ACTION_NONE);
            this.attackCooldown = SPIN_COOLDOWN_TICKS;
        }
    }

    private void startWallStagger() {
        this.actionHitEntities.clear();
        this.setDeltaMovement(this.currentVerticalMovement());
        this.setActionState(ACTION_HURT);
        this.actionTicks = WALL_STAGGER_TICKS;
        this.attackCooldown = WALL_STAGGER_COOLDOWN_TICKS;
        this.playSound(AntarchySoundEvents.MOLEVORE_DIG.get(), 1.0F, 0.6F);
    }

    private void setSpinMovement(Vec3 horizontal) {
        double localVertical = this.toLocal(this.getDeltaMovement()).y;
        double nextVertical;
        if (this.isInWater()) {
            nextVertical = 0.0D;
        } else if (this.onGround()) {
            nextVertical = -0.08D;
        } else {
            nextVertical = Math.max(localVertical - 0.08D, -2.0D);
        }
        this.setDeltaMovement(horizontal.add(this.toWorld(0.0D, nextVertical, 0.0D)));
    }

    private Vec3 currentHorizontalMovement() {
        return this.toWorld(this.toLocalPlane(this.getDeltaMovement()));
    }

    private Vec3 currentVerticalMovement() {
        return this.toWorld(0.0D, this.toLocal(this.getDeltaMovement()).y, 0.0D);
    }

    private void dampHorizontalMovement(double factor) {
        Vec3 local = this.toLocal(this.getDeltaMovement());
        this.setDeltaMovement(this.toWorld(local.x * factor, local.y, local.z * factor));
    }

    private void turnBodyToward(Vec3 worldPoint, float maxDegrees) {
        Vec3 flat = this.toLocalPlane(worldPoint.subtract(this.position()));
        if (flat.lengthSqr() < 1.0E-4D) {
            return;
        }
        float yaw = (float) (Mth.atan2(flat.z, flat.x) * Mth.RAD_TO_DEG) - 90.0F;
        this.holdBodyYaw(Mth.approachDegrees(this.getYRot(), yaw, maxDegrees));
    }

    private void holdBodyYaw(float yaw) {
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private float yawOf(Vec3 worldDirection) {
        Vec3 flat = this.toLocalPlane(worldDirection);
        return (float) (Mth.atan2(flat.z, flat.x) * Mth.RAD_TO_DEG) - 90.0F;
    }

    private Vec3 committedForwardLocal() {
        float radians = this.committedYaw * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(radians), 0.0D, Mth.cos(radians));
    }

    private Vec3 committedForwardWorld() {
        return this.toWorld(this.committedForwardLocal());
    }

    private void performSpinChargeHit() {
        AABB hitBox = this.getBoundingBox().inflate(0.55D, 0.35D, 0.55D);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::isValidSwipeTarget)) {
            if (!this.actionHitEntities.add(entity.getUUID())) {
                continue;
            }

            if (this.level() instanceof ServerLevel serverLevel && entity.hurtServer(serverLevel, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.65F)) {
                Vec3 push = this.spinDirection.lengthSqr() > 0.0D
                        ? this.toLocalPlane(this.spinDirection)
                        : this.toLocalPlane(entity.position().subtract(this.position()));
                if (push.lengthSqr() < 1.0E-4D) {
                    push = this.toLocalPlane(this.getViewVector(1.0F));
                }
                push = push.normalize();
                Vec3 worldPush = this.toWorld(push.x * 1.15D, 0.35D, push.z * 1.15D);
                entity.push(worldPush.x, worldPush.y, worldPush.z);
                entity.syncVelocity = true;
                this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 1.0F, 0.65F + this.random.nextFloat() * 0.1F);
            }
        }
    }

    private void startIdleDig() {
        this.setActionState(ACTION_IDLE_DIG);
        this.actionTicks = SWIPE_TICKS;
        this.swipeHitsApplied = 0;
        this.getNavigation().stop();
        this.dampHorizontalMovement(0.35D);
        this.playSound(AntarchySoundEvents.MOLEVORE_DIG.get(), 0.9F, 0.55F);
    }

    private void tickIdleDig() {
        this.getNavigation().stop();
        this.dampHorizontalMovement(0.6D);

        int elapsed = SWIPE_TICKS - this.actionTicks;
        if (this.swipeHitsApplied == 0 && elapsed >= SWIPE_FIRST_HIT_TICK) {
            this.performIdleDig();
            this.swipeHitsApplied = 1;
        }

        if (--this.actionTicks <= 0) {
            this.setActionState(ACTION_NONE);
            this.idleDigCooldown = 120 + this.random.nextInt(80);
        }
    }

    private void performIdleDig() {
        if (!this.canBreakBlocks()
                || !this.level().hasNearbyAlivePlayer(this.getX(), this.getY(), this.getZ(), IDLE_DIG_PLAYER_RANGE)) {
            return;
        }

        Vec3 forward = this.toLocalPlane(this.getViewVector(1.0F));
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }

        forward = forward.normalize();
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        for (int across = -1; across <= 1; across++) {
            for (int along = 2; along <= 3; along++) {
                Vec3 local = forward.scale(along).add(side.scale(across)).add(0.0D, -0.5D, 0.0D);
                BlockPos pos = BlockPos.containing(this.position().add(this.toWorld(local)));
                BlockPos support = BlockPos.containing(Vec3.atCenterOf(pos).add(this.toWorld(0.0D, -1.0D, 0.0D)));
                if (this.level().getBlockState(pos).is(AntarchyTags.Blocks.MOLEVORE_BREAKABLE_BLOCKS)
                        && !this.level().getBlockState(support).getCollisionShape(this.level(), support).isEmpty()) {
                    this.level().destroyBlock(pos, false, this);
                }
            }
        }

        this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.1F);
    }

    private void startTunnelDig(Vec3 direction) {
        this.tunnelDirection = direction;
        this.setActionState(ACTION_TUNNEL_DIG);
        this.actionTicks = SWIPE_TICKS;
        this.swipeHitsApplied = 0;
        this.getNavigation().stop();
        this.dampHorizontalMovement(0.35D);
        this.playSound(AntarchySoundEvents.MOLEVORE_DIG.get(), 0.9F, 0.55F);
    }

    private void tickTunnelDig() {
        this.getNavigation().stop();
        this.dampHorizontalMovement(0.6D);
        this.turnBodyToward(this.position().add(this.tunnelDirection), TRACKING_TURN_DEGREES);

        int elapsed = SWIPE_TICKS - this.actionTicks;
        if (this.swipeHitsApplied == 0 && elapsed >= SWIPE_FIRST_HIT_TICK) {
            if (this.canBreakBlocks()) {
                this.breakTaggedBlocksInDirection(this.tunnelDirection, 2.8D, 2.2D, 1.15D);
            }
            this.playSound(AntarchySoundEvents.MOLEVORE_DIG.get(), 1.0F, 0.75F);
            this.swipeHitsApplied = 1;
        }

        if (--this.actionTicks <= 0) {
            this.lastChaseTargetPos = null;
            this.setActionState(ACTION_NONE);
        }
    }

    private void tickHurtAnimation() {
        this.getNavigation().stop();
        this.dampHorizontalMovement(0.5D);
        if (--this.actionTicks <= 0) {
            this.setActionState(ACTION_NONE);
        }
    }

    private boolean isValidSwipeTarget(LivingEntity entity) {
        return entity.isAlive() && entity != this && this.canAttack(entity);
    }

    private boolean breakTaggedBlocksInFront(double range, double verticalRange, double halfWidth) {
        return this.breakTaggedBlocksInDirection(this.getViewVector(1.0F).normalize(), range, verticalRange, halfWidth);
    }

    private boolean breakTaggedBlocksInDirection(Vec3 direction, double range, double verticalRange, double halfWidth) {
        if (!canMobGrief(this.level())) {
            return false;
        }

        Vec3 forward = direction.normalize();
        Vec3 center = this.position().add(forward.scale(range * 0.5D)).add(this.toWorld(0.0D, verticalRange * 0.5D + 0.05D, 0.0D));
        BlockPos min = BlockPos.containing(center.x - halfWidth, center.y - verticalRange * 0.5D, center.z - halfWidth);
        BlockPos max = BlockPos.containing(center.x + halfWidth, center.y + verticalRange * 0.5D, center.z + halfWidth);

        boolean broke = false;
        Vec3 hForward = this.toLocalPlane(forward);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = this.level().getBlockState(pos);
            if (!state.is(AntarchyTags.Blocks.MOLEVORE_BREAKABLE_BLOCKS) || state.isAir()) {
                continue;
            }

            Vec3 blockCenter = Vec3.atCenterOf(pos);
            Vec3 toBlock = this.toLocalPlane(blockCenter.subtract(this.position()));
            if (hForward.lengthSqr() > 1.0E-4D && toBlock.lengthSqr() > 1.0E-4D
                    && hForward.normalize().dot(toBlock.normalize()) < -0.15D) {
                continue;
            }

            this.level().destroyBlock(pos, false, this);
            broke = true;
        }
        return broke;
    }

    private GravityDirection gravityDirection() {
        return GravityApi.getGravityDirection(this);
    }

    private Vec3 toWorld(double x, double y, double z) {
        return GravityRotationUtil.vecPlayerToWorld(x, y, z, this.gravityDirection());
    }

    private Vec3 toWorld(Vec3 localVector) {
        return GravityRotationUtil.vecPlayerToWorld(localVector, this.gravityDirection());
    }

    private Vec3 toLocal(Vec3 worldVector) {
        return GravityRotationUtil.vecWorldToPlayer(worldVector, this.gravityDirection());
    }

    private Vec3 toLocalPlane(Vec3 worldVector) {
        Vec3 local = GravityRotationUtil.vecWorldToPlayer(worldVector, this.gravityDirection());
        return new Vec3(local.x, 0.0D, local.z);
    }

    private int getActionState() {
        return this.entityData.get(ACTION_STATE);
    }

    private void setActionState(int actionState) {
        if (this.entityData.get(ACTION_STATE) == actionState) {
            return;
        }
        this.entityData.set(ACTION_STATE, actionState);
        if (actionState != ACTION_NONE) {
            this.entityData.set(ACTION_SERIAL, this.entityData.get(ACTION_SERIAL) + 1);
        }
    }

    private boolean isSubmerged() {
        return this.entityData.get(SUBMERGED);
    }

    private void setSubmerged(boolean submerged) {
        this.entityData.set(SUBMERGED, submerged);
    }

    private boolean canStroll() {
        return this.getTarget() == null && this.getActionState() == ACTION_NONE;
    }

    private boolean isBodyLockedToYaw() {
        int action = this.getActionState();
        return action == ACTION_CLAW_SWIPE || action == ACTION_SPIN_CHARGE || action == ACTION_TUNNEL_DIG;
    }

    public boolean isHeadTrackingActive() {
        return this.getActionState() == ACTION_NONE && !this.isDeadOrDying();
    }

    private boolean canBreakBlocks() {
        return canMobGrief(this.level());
    }

    private static final class TruffleLureGoal extends Goal {
        private static final double RANGE = 16.0D;
        private static final double STOP_DISTANCE = 3.0D;
        private static final int REPATH_TICKS = 10;
        private final MolevoreEntity molevore;
        private final TargetingConditions conditions = TargetingConditions.forNonCombat().range(RANGE).ignoreLineOfSight().selector((candidate, serverLevel) -> MoleTruffles.lures(candidate));
        @Nullable
        private Player player;
        private int repathDelay;

        private TruffleLureGoal(MolevoreEntity molevore) {
            this.molevore = molevore;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.molevore.canStroll()) {
                return false;
            }
            this.player = this.molevore.level() instanceof ServerLevel serverLevel ? serverLevel.getNearestPlayer(this.conditions, this.molevore) : null;
            return this.player != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.player != null
                    && this.player.isAlive()
                    && !this.player.isSpectator()
                    && MoleTruffles.lures(this.player)
                    && this.molevore.canStroll()
                    && this.molevore.distanceToSqr(this.player) <= RANGE * RANGE;
        }

        @Override
        public void start() {
            this.repathDelay = 0;
        }

        @Override
        public void stop() {
            this.player = null;
            this.molevore.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.player == null) {
                return;
            }
            this.molevore.getLookControl().setLookAt(this.player, 30.0F, 20.0F);
            if (this.molevore.distanceToSqr(this.player) <= STOP_DISTANCE * STOP_DISTANCE) {
                this.molevore.getNavigation().stop();
            } else if (--this.repathDelay <= 0) {
                this.repathDelay = REPATH_TICKS;
                this.molevore.getNavigation().moveTo(this.player, WANDER_SPEED);
            }
        }
    }

    private static boolean canMobGrief(Level level) {
        return level instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING);
    }
}
