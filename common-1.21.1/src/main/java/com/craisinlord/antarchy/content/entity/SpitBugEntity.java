package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class SpitBugEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> SPIT_ANIM_TICKS =
            SynchedEntityData.defineId(SpitBugEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SPIT_ANIM = RawAnimation.begin().thenPlay("spit");

    private static final double MAX_HEALTH = 50.0D;
    private static final double MOVEMENT_SPEED = 0.23D;
    private static final double ATTACK_DAMAGE = 5.0D;
    private static final double FOLLOW_RANGE = 24.0D;
    private static final double SPIT_RANGE_SQR = 14.0D * 14.0D;
    private static final int SPIT_COOLDOWN_TICKS = 55;
    private static final int SPIT_ANIMATION_TICKS = 18;
    private static final int SPIT_RELEASE_TICKS = 8;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int spitCooldownTicks;
    private int spitAnimationTicks;
    private int spitReleaseTicks;

    public SpitBugEntity(EntityType<? extends SpitBugEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    public static boolean canSpawn(EntityType<SpitBugEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above())
                && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPIT_ANIM_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpitAttackGoal());
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 2, this::mainAnimController)
                .triggerableAnim("spit", SPIT_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        CavarynBurrowingMobBehavior.moveOutOfBlocks(this);
        super.tick();
        CavarynBurrowingMobBehavior.moveOutOfBlocks(this);
        if (this.level().isClientSide) {
            return;
        }

        if (this.spitCooldownTicks > 0) {
            this.spitCooldownTicks--;
        }

        if (this.spitAnimationTicks > 0) {
            this.spitAnimationTicks--;
            this.entityData.set(SPIT_ANIM_TICKS, this.spitAnimationTicks);
            if (this.spitReleaseTicks > 0 && --this.spitReleaseTicks == 0) {
                this.fireSpitProjectile();
            }
        }
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = target instanceof LivingEntity livingTarget && this.level() instanceof ServerLevel serverLevel
                ? livingTarget.hurt(AntarchyDamageSources.spitBugDisrespect(serverLevel, this, this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))
                : super.doHurtTarget(target);
        if (hurt) {
            this.playSound(SoundEvents.SPIDER_HURT, 0.7F, 0.8F + this.random.nextFloat() * 0.2F);
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.SPIT_BUG_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.SPIT_BUG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.SPIT_BUG_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.18F, 0.95F);
    }

    private PlayState mainAnimController(AnimationState<SpitBugEntity> state) {
        if (this.entityData.get(SPIT_ANIM_TICKS) > 0) {
            return state.setAndContinue(SPIT_ANIM);
        }
        return state.setAndContinue(state.isMoving() ? WALK_ANIM : IDLE_ANIM);
    }

    private boolean canSpitAt(LivingEntity target) {
        return this.spitCooldownTicks <= 0
                && this.spitAnimationTicks <= 0
                && this.hasLineOfSight(target)
                && this.distanceToSqr(target) <= SPIT_RANGE_SQR;
    }

    private void startSpitAttack() {
        this.spitCooldownTicks = SPIT_COOLDOWN_TICKS;
        this.spitAnimationTicks = SPIT_ANIMATION_TICKS;
        this.spitReleaseTicks = SPIT_RELEASE_TICKS;
        this.entityData.set(SPIT_ANIM_TICKS, this.spitAnimationTicks);
        this.triggerAnim("main_controller", "spit");
    }

    @SuppressWarnings("unchecked")
    private void fireSpitProjectile() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        EntityType<SpitBugProjectileEntity> projectileType = (EntityType<SpitBugProjectileEntity>) BuiltInRegistries.ENTITY_TYPE.getOptional(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "spit_bug_projectile")
        ).orElse(null);
        if (projectileType == null) {
            return;
        }

        SpitBugProjectileEntity projectile = new SpitBugProjectileEntity(projectileType, this, serverLevel);
        Vec3 origin = this.position()
                .add(0.0D, this.getBbHeight() * 0.55D, 0.0D)
                .add(this.getViewVector(0.0F).scale(1.35D));
        Vec3 aimPoint = target.getEyePosition().add(target.getDeltaMovement().scale(0.45D));
        Vec3 direction = aimPoint.subtract(origin).normalize();

        projectile.setPos(origin.x, origin.y, origin.z);
        projectile.shoot(direction.x, direction.y, direction.z, 1.05F, 0.0F);
        serverLevel.addFreshEntity(projectile);
        this.playSound(AntarchySoundEvents.SPIT_BUG_SPIT.get(), 1.0F, 0.85F + this.random.nextFloat() * 0.15F);
    }

    private final class SpitAttackGoal extends Goal {
        SpitAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = SpitBugEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = SpitBugEntity.this.getTarget();
            if (target == null) {
                return;
            }

            SpitBugEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (SpitBugEntity.this.spitAnimationTicks > 0) {
                SpitBugEntity.this.getNavigation().stop();
                return;
            }

            if (SpitBugEntity.this.canSpitAt(target)) {
                SpitBugEntity.this.getNavigation().stop();
                SpitBugEntity.this.startSpitAttack();
                return;
            }

            if (SpitBugEntity.this.distanceToSqr(target) > 36.0D || !SpitBugEntity.this.hasLineOfSight(target)) {
                SpitBugEntity.this.getNavigation().moveTo(target, 1.0D);
            } else {
                SpitBugEntity.this.getNavigation().stop();
            }
        }
    }
}
