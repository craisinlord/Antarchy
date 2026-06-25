package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
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

import java.util.EnumSet;
import java.util.function.IntFunction;

public class JumpyBugEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> CAMOUFLAGED =
            SynchedEntityData.defineId(JumpyBugEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CLINGING_TO_CEILING =
            SynchedEntityData.defineId(JumpyBugEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_STATE =
            SynchedEntityData.defineId(JumpyBugEntity.class, EntityDataSerializers.INT);

    private static final String POUNCE_COOLDOWN_KEY = "PounceCooldown";
    private static final String RECOVERY_TICKS_KEY = "RecoveryTicks";
    private static final String POUNCE_TICKS_KEY = "PounceTicks";

    private static final double DEFAULT_MOVEMENT_SPEED = 0.25D;
    private static final double DEFAULT_FOLLOW_RANGE = 16.0D;
    private static final double MELEE_WALK_RANGE = 4.0D;
    private static final int DEFAULT_POUNCE_COOLDOWN = 60;
    private static final int DEFAULT_RECOVERY_TICKS = 20;
    private static final double POUNCE_HORIZONTAL_SPEED = 0.92D;
    private static final double POUNCE_UPWARD_SPEED = 0.75D;
    private static final double QUICK_MOVE_SQR = 0.14D * 0.14D;
    private static final double CLING_SEARCH_RANGE = 1.5D;
    private static final double POUNCE_LAND_DAMAGE_RANGE = 5.0D;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation JUMP_ANIM = RawAnimation.begin().thenLoop("jump");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int pounceCooldownTicks;
    private int recoveryTicks;
    private int pounceTicks;
    private float visualAlpha = 1.0F;
    private float previousVisualAlpha = 1.0F;

    public JumpyBugEntity(EntityType<? extends JumpyBugEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.jumpyBugHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.jumpyBugPounceDamage())
                .add(Attributes.MOVEMENT_SPEED, DEFAULT_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, DEFAULT_FOLLOW_RANGE);
    }

    public static boolean canSpawn(EntityType<JumpyBugEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        return level.getDifficulty() != Difficulty.PEACEFUL
                && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAMOUFLAGED, false);
        builder.define(CLINGING_TO_CEILING, false);
        builder.define(ATTACK_STATE, AttackState.GROUND.id());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PounceAttackGoal());
        this.goalSelector.addGoal(2, new CeilingClingGoal());
        this.goalSelector.addGoal(3, new ChaseTargetGoal());
        this.goalSelector.addGoal(4, new WanderGoal());
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(POUNCE_COOLDOWN_KEY, this.pounceCooldownTicks);
        tag.putInt(RECOVERY_TICKS_KEY, this.recoveryTicks);
        tag.putInt(POUNCE_TICKS_KEY, this.pounceTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.pounceCooldownTicks = tag.getInt(POUNCE_COOLDOWN_KEY);
        this.recoveryTicks = tag.getInt(RECOVERY_TICKS_KEY);
        this.pounceTicks = tag.getInt(POUNCE_TICKS_KEY);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.setHealth((float) AntarchySettings.jumpyBugHealth());
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    public void tick() {
        this.previousVisualAlpha = this.visualAlpha;
        this.visualAlpha += (this.getTargetVisualAlpha() - this.visualAlpha) * 0.25F;

        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.pounceCooldownTicks > 0) {
            this.pounceCooldownTicks--;
        }
        if (this.recoveryTicks > 0) {
            this.recoveryTicks--;
        }

        if (this.isClingingToCeiling()) {
            this.tickClinging();
        } else if (this.isPouncing()) {
            this.tickPouncing();
        } else {
            this.setNoGravity(false);
            this.noPhysics = false;
            if (this.getAttackState() != AttackState.RECOVERING || this.recoveryTicks <= 0) {
                this.setAttackState(AttackState.GROUND);
            }
        }

        this.updateCamouflage();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isClingingToCeiling()) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (!hurt) {
            return false;
        }

        this.setCamouflaged(false);
        this.recoveryTicks = Math.max(this.recoveryTicks, DEFAULT_RECOVERY_TICKS);
        if (this.isClingingToCeiling()) {
            this.stopClinging();
        }
        if (this.isPouncing()) {
            this.finishPounce(false);
        }
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.12F, 1.1F);
    }

    public boolean isCamouflaged() {
        return this.entityData.get(CAMOUFLAGED);
    }

    public boolean isClingingToCeiling() {
        return this.entityData.get(CLINGING_TO_CEILING);
    }

    public boolean isPouncing() {
        return this.getAttackState() == AttackState.POUNCING;
    }

    public float getVisualAlpha(float partialTick) {
        return Mth.lerp(partialTick, this.previousVisualAlpha, this.visualAlpha);
    }

    public AttackState getAttackState() {
        return AttackState.BY_ID.apply(this.entityData.get(ATTACK_STATE));
    }

    private void setCamouflaged(boolean value) {
        this.entityData.set(CAMOUFLAGED, value);
    }

    private void setClingingToCeiling(boolean value) {
        this.entityData.set(CLINGING_TO_CEILING, value);
    }

    private void setAttackState(AttackState state) {
        this.entityData.set(ATTACK_STATE, state.id());
    }

    private float getTargetVisualAlpha() {
        return this.isCamouflaged() ? (float) AntarchySettings.jumpyBugCamouflageAlpha() : 1.0F;
    }

    private PlayState mainAnimController(AnimationState<JumpyBugEntity> state) {
        AttackState attackState = this.getAttackState();
        if (attackState == AttackState.POUNCING || attackState == AttackState.CLINGING) {
            return state.setAndContinue(JUMP_ANIM);
        }
        if (attackState == AttackState.RECOVERING) {
            return state.setAndContinue(ATTACK_ANIM);
        }
        return state.setAndContinue(state.isMoving() ? WALK_ANIM : IDLE_ANIM);
    }

    private void tickClinging() {
        BlockPos clingPos = this.findClingBlockPos();
        if (clingPos == null) {
            this.stopClinging();
            return;
        }

        this.setAttackState(AttackState.CLINGING);
        this.setCamouflaged(true);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.move(MoverType.SELF, Vec3.ZERO);
        this.setPos(this.getX(), clingPos.getY() - this.getBbHeight() - 0.01D, this.getZ());
    }

    private void tickPouncing() {
        this.setAttackState(AttackState.POUNCING);
        this.setCamouflaged(false);
        this.pounceTicks++;

        if (this.onGround() && this.pounceTicks > 8) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && this.distanceToSqr(target) <= POUNCE_LAND_DAMAGE_RANGE * POUNCE_LAND_DAMAGE_RANGE) {
                target.hurt(this.damageSources().mobAttack(this), (float) AntarchySettings.jumpyBugPounceDamage());
            }
            this.finishPounce(false);
        }
    }

    private void updateCamouflage() {
        if (this.isPouncing()) {
            this.setCamouflaged(false);
            return;
        }

        boolean idle = this.getTarget() == null && this.getDeltaMovement().horizontalDistanceSqr() < 0.003D;
        boolean stalking = this.isClingingToCeiling();
        boolean movingQuickly = this.getDeltaMovement().lengthSqr() > QUICK_MOVE_SQR;
        boolean shouldCamouflage = !movingQuickly && (idle || stalking);
        this.setCamouflaged(shouldCamouflage);
    }

    @Nullable
    private BlockPos findClingBlockPos() {
        BlockPos origin = this.blockPosition();
        for (int dy = 1; dy <= 2; dy++) {
            BlockPos candidate = origin.above(dy);
            if (this.isCeilingBlock(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isCeilingBlock(BlockPos pos) {
        return this.level().getBlockState(pos).isFaceSturdy(this.level(), pos, net.minecraft.core.Direction.DOWN);
    }

    private boolean canClingHere() {
        return this.findClingBlockPos() != null;
    }

    private void beginClinging() {
        this.setClingingToCeiling(true);
        this.setAttackState(AttackState.CLINGING);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void stopClinging() {
        this.setClingingToCeiling(false);
        this.setAttackState(AttackState.GROUND);
        this.setNoGravity(false);
        this.noPhysics = false;
    }

    private void beginPounce(LivingEntity target) {
        this.stopClinging();
        this.setTarget(target);
        this.setAttackState(AttackState.POUNCING);
        this.setCamouflaged(false);
        this.getNavigation().stop();
        this.pounceCooldownTicks = DEFAULT_POUNCE_COOLDOWN;
        this.pounceTicks = 0;

        Vec3 from = this.position();
        Vec3 to = target.getEyePosition().subtract(from);
        Vec3 horizontal = new Vec3(to.x, 0.0D, to.z);
        Vec3 direction = horizontal.lengthSqr() > 1.0E-4D ? horizontal.normalize() : target.getLookAngle();
        double upward = POUNCE_UPWARD_SPEED + Mth.clamp(to.y * 0.08D, -0.04D, 0.22D);
        this.setDeltaMovement(direction.x * POUNCE_HORIZONTAL_SPEED, upward, direction.z * POUNCE_HORIZONTAL_SPEED);
        this.hasImpulse = true;
    }

    private void finishPounce(boolean keepMomentum) {
        this.pounceTicks = 0;
        if (!keepMomentum) {
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(movement.x * 0.25D, Math.max(0.0D, movement.y), movement.z * 0.25D);
        }
        this.setAttackState(AttackState.RECOVERING);
        this.recoveryTicks = DEFAULT_RECOVERY_TICKS;
    }

    private boolean canUseAmbush() {
        return !this.isPouncing()
                && this.recoveryTicks <= 0;
    }

    private enum AttackState implements StringRepresentable {
        GROUND(0, "ground"),
        CLINGING(1, "clinging"),
        POUNCING(2, "pouncing"),
        RECOVERING(3, "recovering");

        private static final IntFunction<AttackState> BY_ID = ByIdMap.continuous(AttackState::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<AttackState> CODEC = StringRepresentable.fromEnum(AttackState::values);

        private final int id;
        private final String name;

        AttackState(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int id() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    private final class PounceAttackGoal extends Goal {
        private PounceAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = JumpyBugEntity.this.getTarget();
            return target != null
                    && target.isAlive()
                    && JumpyBugEntity.this.canUseAmbush()
                    && JumpyBugEntity.this.pounceCooldownTicks <= 0
                    && (JumpyBugEntity.this.hasLineOfSight(target) || JumpyBugEntity.this.distanceToSqr(target) <= 9.0D);
        }

        @Override
        public boolean canContinueToUse() {
            return JumpyBugEntity.this.isPouncing();
        }

        @Override
        public void start() {
            LivingEntity target = JumpyBugEntity.this.getTarget();
            if (target != null) {
                JumpyBugEntity.this.beginPounce(target);
            }
        }
    }

    private final class CeilingClingGoal extends Goal {
        private CeilingClingGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (!JumpyBugEntity.this.canUseAmbush() || JumpyBugEntity.this.isClingingToCeiling()) {
                return false;
            }

            LivingEntity target = JumpyBugEntity.this.getTarget();
            if (target == null) {
                return JumpyBugEntity.this.random.nextInt(40) == 0 && JumpyBugEntity.this.canClingHere();
            }

            return JumpyBugEntity.this.distanceToSqr(target) <= (DEFAULT_FOLLOW_RANGE + CLING_SEARCH_RANGE) * (DEFAULT_FOLLOW_RANGE + CLING_SEARCH_RANGE)
                    && JumpyBugEntity.this.canClingHere();
        }

        @Override
        public boolean canContinueToUse() {
            return JumpyBugEntity.this.isClingingToCeiling();
        }

        @Override
        public void start() {
            JumpyBugEntity.this.beginClinging();
        }

        @Override
        public void stop() {
            if (JumpyBugEntity.this.isClingingToCeiling()) {
                JumpyBugEntity.this.stopClinging();
            }
        }

        @Override
        public void tick() {
            LivingEntity target = JumpyBugEntity.this.getTarget();
            if (target == null) {
                return;
            }

            JumpyBugEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (JumpyBugEntity.this.pounceCooldownTicks <= 0
                    && (JumpyBugEntity.this.hasLineOfSight(target) || JumpyBugEntity.this.distanceToSqr(target) <= 4.0D)) {
                JumpyBugEntity.this.stopClinging();
                JumpyBugEntity.this.beginPounce(target);
            }
        }
    }

    private final class ChaseTargetGoal extends Goal {
        private ChaseTargetGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = JumpyBugEntity.this.getTarget();
            return target != null
                    && target.isAlive()
                    && !JumpyBugEntity.this.isClingingToCeiling()
                    && !JumpyBugEntity.this.isPouncing()
                    && JumpyBugEntity.this.recoveryTicks <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = JumpyBugEntity.this.getTarget();
            if (target == null) {
                return;
            }

            JumpyBugEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            // Only walk when close; at range, pounce is the primary approach
            if (JumpyBugEntity.this.distanceToSqr(target) <= MELEE_WALK_RANGE * MELEE_WALK_RANGE) {
                JumpyBugEntity.this.getNavigation().moveTo(target, 1.05D);
            } else {
                JumpyBugEntity.this.getNavigation().stop();
            }
        }
    }

    private final class WanderGoal extends RandomStrollGoal {
        private WanderGoal() {
            super(JumpyBugEntity.this, 0.8D);
        }

        @Override
        public boolean canUse() {
            return !JumpyBugEntity.this.isClingingToCeiling()
                    && !JumpyBugEntity.this.isPouncing()
                    && JumpyBugEntity.this.getTarget() == null
                    && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !JumpyBugEntity.this.isClingingToCeiling()
                    && !JumpyBugEntity.this.isPouncing()
                    && super.canContinueToUse();
        }
    }
}
