package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

public class MolewormEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(MolewormEntity.class, EntityDataSerializers.INT);

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_DIG = 2;
    private static final int ANIM_DEATH = 3;
    private static final int DIG_ANIMATION_TICKS = 20;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation DIG_ANIM = RawAnimation.begin().thenPlay("Dig");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("Death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private WakeUpFriendsGoal friendsGoal;
    @Nullable
    private BlockPos mergeTargetPos;
    private int mergeTicks;

    public MolewormEntity(EntityType<? extends MolewormEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @SuppressWarnings("unchecked")
    public static boolean canSpawn(EntityType<MolewormEntity> entityType, LevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return Silverfish.checkSilverfishSpawnRules((EntityType<Silverfish>) (EntityType<?>) entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
    }

    @Override
    protected void registerGoals() {
        this.friendsGoal = new WakeUpFriendsGoal(this);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
        this.goalSelector.addGoal(3, this.friendsGoal);
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new MergeWithDirtGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<MolewormEntity> state) {
        return switch (this.getAnimationState()) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_DIG -> state.setAndContinue(DIG_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return EntityDimensions.scalable(0.4F, 0.3F);
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
        this.playSound(AntarchySoundEvents.MOLEWORM_DIG.get(), 0.15F, 1.0F);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity) {
            this.playSound(AntarchySoundEvents.MOLEWORM_ATTACK.get(), 0.35F, 0.95F + this.random.nextFloat() * 0.1F);
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }

        if ((source.getEntity() != null || source.is(DamageTypeTags.ALWAYS_TRIGGERS_SILVERFISH)) && this.friendsGoal != null) {
            this.friendsGoal.notifyHurt();
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && this.isMerging()) {
            this.cancelMerge();
        }
        return hurt;
    }

    @Override
    public void tick() {
        this.yBodyRot = this.getYRot();
        super.tick();

        if (!this.level().isClientSide && this.isMerging()) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            this.mergeTicks--;
            if (this.mergeTicks <= 0) {
                this.completeMerge();
            }
        }

        if (!this.level().isClientSide) {
            this.updateAnimationState();
        }
    }

    @Override
    public void setYBodyRot(float yBodyRot) {
        this.setYRot(yBodyRot);
        super.setYBodyRot(yBodyRot);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return isMergeHost(level.getBlockState(pos.below())) ? 10.0F : super.getWalkTargetValue(pos, level);
    }

    public boolean isMerging() {
        return this.mergeTicks > 0 && this.mergeTargetPos != null;
    }

    private int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private void startMerge(BlockPos targetPos) {
        this.mergeTargetPos = targetPos.immutable();
        this.mergeTicks = DIG_ANIMATION_TICKS;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.setAnimationState(ANIM_DIG);
    }

    private void cancelMerge() {
        this.mergeTargetPos = null;
        this.mergeTicks = 0;
    }

    private void completeMerge() {
        BlockPos targetPos = this.mergeTargetPos;
        this.cancelMerge();
        if (targetPos == null || !canMobGrief(this.level())) {
            return;
        }

        BlockState targetState = this.level().getBlockState(targetPos);
        BlockState infestedState = infestedStateByHost(targetState);
        if (infestedState == null) {
            return;
        }

        this.level().setBlock(targetPos, infestedState, 3);
        this.spawnAnim();
        this.discard();
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }

        if (this.isMerging()) {
            this.setAnimationState(ANIM_DIG);
            return;
        }

        Vec3 velocity = this.getDeltaMovement();
        if (velocity.horizontalDistanceSqr() > 1.0E-4D || this.walkAnimation.speed() > 0.02F) {
            this.setAnimationState(ANIM_WALK);
            return;
        }

        this.setAnimationState(ANIM_IDLE);
    }

    private static boolean isMergeHost(BlockState state) {
        return state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT);
    }

    private static boolean canMobGrief(Level level) {
        return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    private static boolean isInfestedDirt(BlockState state) {
        return state.is(AntarchyObjects.INFESTED_COARSE_DIRT.get()) || state.is(AntarchyObjects.INFESTED_ROOTED_DIRT.get());
    }

    @Nullable
    private static BlockState infestedStateByHost(BlockState state) {
        if (state.is(Blocks.COARSE_DIRT)) {
            return AntarchyObjects.INFESTED_COARSE_DIRT.get().defaultBlockState();
        }
        if (state.is(Blocks.ROOTED_DIRT)) {
            return AntarchyObjects.INFESTED_ROOTED_DIRT.get().defaultBlockState();
        }
        return null;
    }

    @Nullable
    private static BlockState hostStateByInfested(BlockState state) {
        if (state.is(AntarchyObjects.INFESTED_COARSE_DIRT.get())) {
            return Blocks.COARSE_DIRT.defaultBlockState();
        }
        if (state.is(AntarchyObjects.INFESTED_ROOTED_DIRT.get())) {
            return Blocks.ROOTED_DIRT.defaultBlockState();
        }
        return null;
    }

    @Nullable
    private static BlockPos findEmergencePos(Level level, BlockPos origin) {
        BlockPos[] candidates = new BlockPos[] {
                origin.above(),
                origin.north(),
                origin.south(),
                origin.east(),
                origin.west(),
                origin.north().above(),
                origin.south().above(),
                origin.east().above(),
                origin.west().above()
        };

        for (BlockPos candidate : candidates) {
            BlockState state = level.getBlockState(candidate);
            BlockState above = level.getBlockState(candidate.above());
            BlockState below = level.getBlockState(candidate.below());
            if (state.isAir() && above.isAir() && below.isFaceSturdy(level, candidate.below(), Direction.UP)) {
                return candidate;
            }
        }

        return null;
    }

    private static final class MergeWithDirtGoal extends RandomStrollGoal {
        private final MolewormEntity moleworm;
        @Nullable
        private BlockPos mergePos;
        private boolean doMerge;

        private MergeWithDirtGoal(MolewormEntity moleworm) {
            super(moleworm, 1.0D, 10);
            this.moleworm = moleworm;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.moleworm.isMerging() || this.mob.getTarget() != null || !this.mob.getNavigation().isDone()) {
                return false;
            }

            RandomSource random = this.mob.getRandom();
            if (canMobGrief(this.mob.level()) && random.nextInt(reducedTickDelay(10)) == 0) {
                BlockPos candidate = this.mob.blockPosition().below();
                if (isMergeHost(this.mob.level().getBlockState(candidate))) {
                    this.mergePos = candidate;
                    this.doMerge = true;
                    return true;
                }
            }

            this.doMerge = false;
            this.mergePos = null;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.doMerge ? this.moleworm.isMerging() : super.canContinueToUse();
        }

        @Override
        public void start() {
            if (!this.doMerge || this.mergePos == null) {
                super.start();
                return;
            }

            if (isMergeHost(this.moleworm.level().getBlockState(this.mergePos))) {
                this.moleworm.startMerge(this.mergePos);
            } else {
                this.doMerge = false;
                this.mergePos = null;
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.doMerge = false;
            this.mergePos = null;
        }
    }

    private static final class WakeUpFriendsGoal extends Goal {
        private final MolewormEntity moleworm;
        private int lookForFriends;

        private WakeUpFriendsGoal(MolewormEntity moleworm) {
            this.moleworm = moleworm;
        }

        public void notifyHurt() {
            if (this.lookForFriends == 0) {
                this.lookForFriends = this.adjustedTickDelay(20);
            }
        }

        @Override
        public boolean canUse() {
            return this.lookForFriends > 0;
        }

        @Override
        public void tick() {
            this.lookForFriends--;
            if (this.lookForFriends > 0) {
                return;
            }

            Level level = this.moleworm.level();
            RandomSource random = this.moleworm.getRandom();
            BlockPos origin = this.moleworm.blockPosition();

            for (BlockPos friendPos : BlockPos.betweenClosed(origin.offset(-5, -3, -5), origin.offset(5, 3, 5))) {
                BlockState friendState = level.getBlockState(friendPos);
                if (!isInfestedDirt(friendState)) {
                    continue;
                }

                if (canMobGrief(level)) {
                    releaseFriend(level, friendPos.immutable(), friendState, this.moleworm);
                } else {
                    BlockState hostState = hostStateByInfested(friendState);
                    if (hostState != null) {
                        level.setBlock(friendPos, hostState, 3);
                    }
                }

                if (random.nextBoolean()) {
                    return;
                }
            }
        }

        private static void releaseFriend(Level level, BlockPos infestedPos, BlockState infestedState, MolewormEntity source) {
            BlockState hostState = hostStateByInfested(infestedState);
            if (hostState == null) {
                return;
            }

            BlockPos emergencePos = findEmergencePos(level, infestedPos);
            if (!(source.getType().create(level) instanceof MolewormEntity friend)) {
                level.setBlock(infestedPos, hostState, 3);
                return;
            }

            if (emergencePos == null) {
                friend.discard();
                level.setBlock(infestedPos, hostState, 3);
                return;
            }

            friend.moveTo(
                    emergencePos.getX() + 0.5D,
                    emergencePos.getY() + 0.05D,
                    emergencePos.getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F,
                    0.0F
            );

            if (!level.noCollision(friend)) {
                friend.discard();
                level.setBlock(infestedPos, hostState, 3);
                return;
            }

            level.setBlock(infestedPos, hostState, 3);
            level.addFreshEntity(friend);
            level.playSound(null, emergencePos, AntarchySoundEvents.MOLEWORM_DIG.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.45F, 0.8F + level.random.nextFloat() * 0.25F);
            level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.POOF,
                    infestedPos.getX() + 0.5D,
                    infestedPos.getY() + 0.45D,
                    infestedPos.getZ() + 0.5D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }
}
