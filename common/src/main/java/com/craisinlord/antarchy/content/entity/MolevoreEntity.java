package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
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

public class MolevoreEntity extends Monster implements GeoEntity {
    private static final ResourceKey<Biome> MOLEWORM_CAVES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "moleworm_caves")
    );
    private static final EntityDataAccessor<Integer> ACTION_STATE = SynchedEntityData.defineId(MolevoreEntity.class, EntityDataSerializers.INT);

    private static final int ACTION_NONE = 0;
    private static final int ACTION_DIG_UP = 1;
    private static final int ACTION_CLAW_SWIPE = 2;
    private static final int ACTION_SPIN_CHARGE = 3;
    private static final int ACTION_HURT = 4;
    private static final int ACTION_DEATH = 5;
    private static final int ACTION_IDLE_DIG = 6;

    private static final int DIG_UP_TICKS = 60;
    private static final int SWIPE_TICKS = 18;
    private static final int SWIPE_DAMAGE_TICK = 8;
    private static final int SPIN_TOTAL_TICKS = 32;
    private static final int SPIN_WINDUP_TICKS = 10;
    private static final int HURT_TICKS = 10;
    private static final int DEATH_TICKS = 34;

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
    private static final RawAnimation CLAW_SWIPE_ANIM = RawAnimation.begin().thenPlay("claw swiping");
    private static final RawAnimation SPINNING_ANIM = RawAnimation.begin().thenPlay("spinning");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Set<UUID> spinHitEntities = new HashSet<>();

    private int actionTicks;
    private int attackCooldown;
    private int idleDigCooldown;
    private boolean swipeDamageApplied;
    private Vec3 spinDirection = Vec3.ZERO;

    public MolevoreEntity(EntityType<? extends MolevoreEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 20;
        this.idleDigCooldown = 100;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    public static boolean canSpawn(EntityType<MolevoreEntity> entityType, LevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return hasSpawnPocket(level, pos);
    }

    private static boolean hasSpawnPocket(LevelAccessor level, BlockPos pos) {
        if (!level.getBiome(pos).is(MOLEWORM_CAVES)) {
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
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, WANDER_SPEED));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<MolevoreEntity> state) {
        return switch (this.getActionState()) {
            case ACTION_DIG_UP -> state.setAndContinue(DIG_UP_ANIM);
            case ACTION_CLAW_SWIPE -> state.setAndContinue(CLAW_SWIPE_ANIM);
            case ACTION_SPIN_CHARGE -> state.setAndContinue(SPINNING_ANIM);
            case ACTION_HURT -> state.setAndContinue(HURT_ANIM);
            case ACTION_DEATH -> state.setAndContinue(DEATH_ANIM);
            case ACTION_IDLE_DIG -> state.setAndContinue(CLAW_SWIPE_ANIM);
            default -> state.setAndContinue(state.isMoving() ? WALK_ANIM : IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData finalized = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        if (spawnReason != MobSpawnType.SPAWN_EGG) {
            this.startDigUp();
        }
        return finalized;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.idleDigCooldown > 0) {
            this.idleDigCooldown--;
        }

        if (this.getActionState() == ACTION_DEATH) {
            return;
        }

        switch (this.getActionState()) {
            case ACTION_DIG_UP -> this.tickDigUp();
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
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }

        boolean hurt = super.hurt(source, amount);
        if (!hurt || this.level().isClientSide) {
            return hurt;
        }

        if (!this.isDeadOrDying() && this.getActionState() != ACTION_DIG_UP && this.getActionState() != ACTION_DEATH) {
            if (this.getActionState() == ACTION_NONE) {
                this.setActionState(ACTION_HURT);
                this.actionTicks = HURT_TICKS;
            }
        }

        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.getActionState() != ACTION_DIG_UP && super.canBeCollidedWith();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target instanceof Player player && !player.isCreative() && !player.isSpectator() && super.canAttack(target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getActionState() == ACTION_DIG_UP ? null : AntarchySoundEvents.MOLEVORE_AMBIENT.get();
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
    public float getWalkTargetValue(BlockPos pos, net.minecraft.world.level.LevelReader level) {
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
        if (this.level().isClientSide || this.getActionState() != ACTION_SPIN_CHARGE) {
            super.travel(travelVector);
            return;
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.96D));
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide && this.getActionState() != ACTION_DEATH) {
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
        if (this.deathTime == 1) {
            this.setActionState(ACTION_DEATH);
        }
        if (this.deathTime >= DEATH_TICKS) {
            this.remove(RemovalReason.KILLED);
        }
    }

    private void tickCombat() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            if (this.idleDigCooldown <= 0 && this.random.nextFloat() < 0.01F) {
                this.startIdleDig();
            }
            return;
        }

        this.getLookControl().setLookAt(target, 30.0F, 20.0F);
        double distanceToTarget = this.distanceToSqr(target);

        if (this.attackCooldown <= 0) {
            if (distanceToTarget <= SWIPE_RANGE * SWIPE_RANGE) {
                this.startClawSwipe();
                return;
            }

            if (distanceToTarget >= SPIN_RANGE_MIN * SPIN_RANGE_MIN
                    && distanceToTarget <= SPIN_RANGE_MAX * SPIN_RANGE_MAX
                    && this.random.nextFloat() < 0.05F) {
                this.startSpinCharge(target);
                return;
            }
        }

        this.getNavigation().moveTo(target, CHASE_SPEED);

        if (this.horizontalCollision && this.canBreakBlocks()) {
            this.breakTaggedBlocksInFront(1.7D, 2.0D, 1.0D);
        }
    }

    private void startDigUp() {
        this.setActionState(ACTION_DIG_UP);
        this.actionTicks = DIG_UP_TICKS;
        this.attackCooldown = 30;
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void tickDigUp() {
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        if (this.actionTicks == DIG_UP_TICKS) {
            this.playSound(AntarchySoundEvents.MOLEVORE_DIG.get(), 0.9F, 0.75F);
        }
        if (this.tickCount % 4 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.POOF,
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
        if (--this.actionTicks <= 0) {
            this.setActionState(ACTION_NONE);
        }
    }

    private void startClawSwipe() {
        this.setActionState(ACTION_CLAW_SWIPE);
        this.actionTicks = SWIPE_TICKS;
        this.swipeDamageApplied = false;
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.35D));
        this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 0.9F, 0.65F);
    }

    private void tickClawSwipe() {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
        }

        int elapsed = SWIPE_TICKS - this.actionTicks;
        if (!this.swipeDamageApplied && elapsed >= SWIPE_DAMAGE_TICK) {
            this.performClawSwipe();
            this.swipeDamageApplied = true;
        }

        if (--this.actionTicks <= 0) {
            this.setActionState(ACTION_NONE);
            this.attackCooldown = 20;
        }
    }

    private void performClawSwipe() {
        AABB hitBox = this.getBoundingBox().inflate(3.0D, 1.3D, 3.0D);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::isValidSwipeTarget)) {
            if (!this.isInFront(entity.position(), 0.05D)) {
                continue;
            }

            if (entity.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                Vec3 push = entity.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
                if (push.lengthSqr() < 1.0E-4D) {
                    push = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
                }
                push = push.normalize().scale(0.8D);
                entity.push(push.x, 0.25D, push.z);
                entity.hurtMarked = true;
            }
        }

        if (this.canBreakBlocks()) {
            this.breakTaggedBlocksInFront(2.8D, 2.2D, 1.15D);
        }

        this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 1.0F, 0.85F);
    }

    private void startSpinCharge(LivingEntity target) {
        Vec3 horizontal = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return;
        }

        this.spinDirection = horizontal.normalize();
        this.spinHitEntities.clear();
        this.setActionState(ACTION_SPIN_CHARGE);
        this.actionTicks = SPIN_TOTAL_TICKS;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 1.2F, 0.75F);
    }

    private void tickSpinCharge() {
        this.getNavigation().stop();
        LivingEntity target = this.getTarget();
        boolean inWindup = this.actionTicks > SPIN_TOTAL_TICKS - SPIN_WINDUP_TICKS;

        if (target != null && inWindup) {
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
            Vec3 horizontal = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (horizontal.lengthSqr() > 1.0E-4D) {
                this.spinDirection = horizontal.normalize();
            }
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2D));
        } else {
            Vec3 lookTarget = this.position().add(this.spinDirection);
            this.getLookControl().setLookAt(lookTarget.x, this.getEyeY(), lookTarget.z, 360.0F, 0.0F);
            Vec3 motion = this.spinDirection.scale(SPIN_SPEED).add(0.0D, this.onGround() ? 0.08D : 0.0D, 0.0D);
            this.setDeltaMovement(motion);
            this.hasImpulse = true;
            this.performSpinChargeHit();
        }
        if (this.horizontalCollision && !inWindup) {
            boolean brokeDirt = this.canBreakBlocks()
                    && this.breakTaggedBlocksInDirection(this.spinDirection, 1.8D, 2.2D, 1.1D);
            if (!brokeDirt) {
                this.actionTicks = Math.min(this.actionTicks, 3);
            }
        } else if (this.horizontalCollision) {
            this.actionTicks = Math.min(this.actionTicks, 3);
        }

        if (--this.actionTicks <= 0) {
            this.spinHitEntities.clear();
            this.setActionState(ACTION_NONE);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.25D));
            this.attackCooldown = 60;
        }
    }

    private void performSpinChargeHit() {
        AABB hitBox = this.getBoundingBox().inflate(0.55D, 0.35D, 0.55D);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::isValidSwipeTarget)) {
            if (!this.spinHitEntities.add(entity.getUUID())) {
                continue;
            }

            if (entity.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.65F)) {
                Vec3 push = this.spinDirection.lengthSqr() > 0.0D ? this.spinDirection : entity.position().subtract(this.position()).normalize();
                entity.push(push.x * 1.15D, 0.35D, push.z * 1.15D);
                entity.hurtMarked = true;
                this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 1.0F, 0.65F + this.random.nextFloat() * 0.1F);
            }
        }
    }

    private void startIdleDig() {
        this.setActionState(ACTION_IDLE_DIG);
        this.actionTicks = SWIPE_TICKS;
        this.swipeDamageApplied = false;
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.35D));
        this.playSound(AntarchySoundEvents.MOLEVORE_DIG.get(), 0.9F, 0.55F);
    }

    private void tickIdleDig() {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));

        int elapsed = SWIPE_TICKS - this.actionTicks;
        if (!this.swipeDamageApplied && elapsed >= SWIPE_DAMAGE_TICK) {
            this.performIdleDig();
            this.swipeDamageApplied = true;
        }

        if (--this.actionTicks <= 0) {
            this.setActionState(ACTION_NONE);
            this.idleDigCooldown = 120 + this.random.nextInt(80);
        }
    }

    
    private void performIdleDig() {
        if (!this.canBreakBlocks()) return;

        Vec3 forward = this.getViewVector(1.0F).normalize();
        Vec3 digCenter = this.position()
                .add(forward.scale(1.0D))
                .add(0.0D, -0.5D, 0.0D);

        double halfWidth = 0.9D;
        double digDepth = 1.5D;
        BlockPos min = BlockPos.containing(digCenter.x - halfWidth, digCenter.y - digDepth, digCenter.z - halfWidth);
        BlockPos max = BlockPos.containing(digCenter.x + halfWidth, digCenter.y + 0.4D, digCenter.z + halfWidth);

        Vec3 hForward = forward.multiply(1.0D, 0.0D, 1.0D);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = this.level().getBlockState(pos);
            if (!state.is(AntarchyTags.Blocks.MOLEVORE_BREAKABLE_BLOCKS) || state.isAir()) {
                continue;
            }
            if (hForward.lengthSqr() > 1.0E-4D) {
                Vec3 toBlock = Vec3.atCenterOf(pos).subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
                if (toBlock.lengthSqr() > 1.0E-4D && hForward.normalize().dot(toBlock.normalize()) < -0.2D) {
                    continue;
                }
            }
            this.level().destroyBlock(pos, false, this);
        }

        this.playSound(AntarchySoundEvents.MOLEVORE_ATTACK.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.1F);
    }

    private void tickHurtAnimation() {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
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
        Vec3 center = this.position().add(forward.scale(range * 0.5D)).add(0.0D, this.getBbHeight() * 0.4D, 0.0D);
        BlockPos min = BlockPos.containing(center.x - halfWidth, center.y - verticalRange * 0.5D, center.z - halfWidth);
        BlockPos max = BlockPos.containing(center.x + halfWidth, center.y + verticalRange * 0.5D, center.z + halfWidth);

        boolean broke = false;
        Vec3 hForward = forward.multiply(1.0D, 0.0D, 1.0D);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = this.level().getBlockState(pos);
            if (!state.is(AntarchyTags.Blocks.MOLEVORE_BREAKABLE_BLOCKS) || state.isAir()) {
                continue;
            }

            Vec3 blockCenter = Vec3.atCenterOf(pos);
            Vec3 toBlock = blockCenter.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (hForward.lengthSqr() > 1.0E-4D && toBlock.lengthSqr() > 1.0E-4D
                    && hForward.normalize().dot(toBlock.normalize()) < -0.15D) {
                continue;
            }

            this.level().destroyBlock(pos, false, this);
            broke = true;
        }
        return broke;
    }

    private boolean isInFront(Vec3 position, double minimumDot) {
        Vec3 forward = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        Vec3 toTarget = position.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D || toTarget.lengthSqr() < 1.0E-4D) {
            return true;
        }
        return forward.normalize().dot(toTarget.normalize()) >= minimumDot;
    }

    private int getActionState() {
        return this.entityData.get(ACTION_STATE);
    }

    private void setActionState(int actionState) {
        this.entityData.set(ACTION_STATE, actionState);
    }

    private boolean canBreakBlocks() {
        return canMobGrief(this.level());
    }

    private static boolean canMobGrief(Level level) {
        return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }
}
