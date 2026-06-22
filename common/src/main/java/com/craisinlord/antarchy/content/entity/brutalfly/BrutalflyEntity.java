package com.craisinlord.antarchy.content.entity.brutalfly;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import java.util.EnumSet;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BrutalflyEntity extends Monster implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation SPIT_ANIM = RawAnimation.begin().thenPlay("spit");
    private static final RawAnimation SWIPE_ANIM = RawAnimation.begin().thenPlay("swipe");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");
    private static final RawAnimation COOCON_ANIM = RawAnimation.begin().thenPlay("Coocon");
    private static final RawAnimation COOCON_HIT_ANIM = RawAnimation.begin().thenPlay("Coocon_hit");
    private static final RawAnimation COOCON_IDLE_ANIM = RawAnimation.begin().thenLoop("Coocon_idle");
    private static final RawAnimation COOCON_HATCH_ANIM = RawAnimation.begin().thenPlay("Coocon_hatch");

    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(BrutalflyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_COCOONED =
            SynchedEntityData.defineId(BrutalflyEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_FLY = 1;
    private static final int ANIM_SPIT = 2;
    private static final int ANIM_SWIPE = 3;
    private static final int ANIM_DEATH = 4;
    private static final int ANIM_COCOON = 5;
    private static final int ANIM_COCOON_HIT = 6;
    private static final int ANIM_COCOON_IDLE = 7;
    private static final int ANIM_COCOON_HATCH = 8;

    private static final int DEATH_ANIM_TICKS = 60;
    private static final int SPIT_WINDUP_TICKS = 16;
    private static final int SWIPE_TICKS = 18;
    private static final float SWIPE_KNOCKBACK = 2.8F;
    private static final int COCOON_HIT_THRESHOLD = 3;
    private static final int COCOON_HIT_ANIM_TICKS = 30;
    private static final int COCOON_HATCH_ANIM_TICKS = 40;
    private static final int COCOON_IDLE_ANIM_TICKS = 70;
    private static final int POST_COCOON_INVULNERABLE_TICKS = 100;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    @Nullable
    private Vec3 patrolTarget;
    private int animationTicks;
    private int spitWindupTicks;
    private int spitCooldown = 50;
    private int meleeCooldown = 35;
    private int deathAnimationTicks;
    private int currentStrafeDuration = 24;
    private int strafeTicks = this.currentStrafeDuration;
    private float orbitDirection = 1.0F;
    private double currentStrafeRadius = 7.5D;
    private double currentFlightHeight = 4.0D;
    private double currentStrafeAngleOffset = this.random.nextDouble() * (Math.PI * 2.0D);
    private boolean spawnedDeathButterflies;

    // Cocoon state (server-side)
    @Nullable
    BlockPos anchorPos;
    private int cocoonHits;
    private boolean isHatching;
    private int cocoonAnimTicks;
    private boolean pendingHatchAnim;
    private int postCocoonInvulnerableTicks;

    public BrutalflyEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.brutalflyHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.brutalflySwipeDamage())
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FLYING_SPEED, 0.55D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnData) {
        double maxHealth = AntarchySettings.brutalflyHealth();
        double swipeDamage = AntarchySettings.brutalflySwipeDamage();
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(maxHealth);
        this.setHealth((float) maxHealth);
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(swipeDamage);
        this.setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(IS_COCOONED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BrutalflyHoverGoal(this));

        HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this);
        hurtByTargetGoal.setAlertOthers(BrutalflyEntity.class);
        this.targetSelector.addGoal(1, hurtByTargetGoal);
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        navigation.setCanOpenDoors(false);
        return navigation;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isCocooned()) return;
        super.customServerAiStep();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, state -> switch (this.getAnimationState()) {
            case ANIM_SPIT -> state.setAndContinue(SPIT_ANIM);
            case ANIM_SWIPE -> state.setAndContinue(SWIPE_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            case ANIM_FLY -> state.setAndContinue(FLY_ANIM);
            case ANIM_COCOON -> state.setAndContinue(COOCON_ANIM);
            case ANIM_COCOON_HIT -> state.setAndContinue(COOCON_HIT_ANIM);
            case ANIM_COCOON_IDLE -> state.setAndContinue(COOCON_IDLE_ANIM);
            case ANIM_COCOON_HATCH -> state.setAndContinue(COOCON_HATCH_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            this.updateFlightRotation();
            return;
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }

        if (this.isCocooned()) {
            this.tickCocoon();
            return;
        }

        if (this.animationTicks > 0) {
            this.animationTicks--;
        }
        if (this.spitCooldown > 0) {
            this.spitCooldown--;
        }
        if (this.meleeCooldown > 0) {
            this.meleeCooldown--;
        }
        if (this.postCocoonInvulnerableTicks > 0) {
            this.postCocoonInvulnerableTicks--;
        }
        if (this.spitWindupTicks > 0 && --this.spitWindupTicks == 0) {
            this.fireCurrentSpitPattern();
        }

        LivingEntity target = this.getTarget();
        if ((target == null || !target.isAlive()) && this.tickCount % 10 == 0) {
            Player nearbyPlayer = this.level().getNearestPlayer(this, 40.0D);
            if (nearbyPlayer != null && this.canAttack(nearbyPlayer)
                    && !nearbyPlayer.isCreative() && !nearbyPlayer.isSpectator()) {
                this.setTarget(nearbyPlayer);
                target = nearbyPlayer;
            }
        }

        if (target != null && target.isAlive()) {
            this.tickCombat(target);
        } else {
            this.tickPatrol();
        }

        this.updateBaseAnimationState();
        this.updateFlightRotation();
    }

    private void tickCocoon() {
        // Pin position to hang from anchor
        if (this.anchorPos != null) {
            this.setPos(this.anchorPos.getX() + 0.5, this.anchorPos.getY() - 1.5, this.anchorPos.getZ() + 0.5);
            this.setDeltaMovement(Vec3.ZERO);

            // Hatch immediately if anchor log was broken
            if (!this.isHatching && !this.level().getBlockState(this.anchorPos).is(BlockTags.LOGS)) {
                this.startHatching();
                return;
            }
        }

        if (this.isHatching) {
            if (this.cocoonAnimTicks > 0) {
                this.cocoonAnimTicks--;
            }
            if (this.pendingHatchAnim && this.cocoonAnimTicks <= 0) {
                this.pendingHatchAnim = false;
                this.setAnimationState(ANIM_COCOON_HATCH);
                this.cocoonAnimTicks = COCOON_HATCH_ANIM_TICKS;
            } else if (!this.pendingHatchAnim && this.cocoonAnimTicks <= 0) {
                this.finishHatch();
            }
            return;
        }

        // Count down hit animation
        if (this.cocoonAnimTicks > 0) {
            this.cocoonAnimTicks--;
            if (this.cocoonAnimTicks <= 0) {
                this.setAnimationState(ANIM_COCOON);
            }
            return;
        }

        // Occasionally play idle animation
        if (this.getAnimationState() == ANIM_COCOON_IDLE) {
            // Will be reset to COCOON after idle ticks counted down
        } else if (this.tickCount % 200 == 17 && this.random.nextFloat() < 0.4F) {
            this.setAnimationState(ANIM_COCOON_IDLE);
            this.cocoonAnimTicks = COCOON_IDLE_ANIM_TICKS;
        } else if (this.getAnimationState() != ANIM_COCOON) {
            this.setAnimationState(ANIM_COCOON);
        }
    }

    private void startHatching() {
        this.isHatching = true;
        this.pendingHatchAnim = true;
        this.postCocoonInvulnerableTicks = POST_COCOON_INVULNERABLE_TICKS;
        this.setAnimationState(ANIM_COCOON_HIT);
        this.cocoonAnimTicks = COCOON_HIT_ANIM_TICKS;
    }

    private void finishHatch() {
        this.entityData.set(IS_COCOONED, false);
        this.isHatching = false;
        this.anchorPos = null;
        this.refreshDimensions();
        this.setAnimationState(ANIM_FLY);
        Player nearbyPlayer = this.level().getNearestPlayer(this, 48.0D);
        if (nearbyPlayer != null && !nearbyPlayer.isCreative() && !nearbyPlayer.isSpectator()) {
            this.setTarget(nearbyPlayer);
        }
    }

    public boolean isCocooned() {
        return this.entityData.get(IS_COCOONED);
    }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose) {
        if (isCocooned()) {
            return net.minecraft.world.entity.EntityDimensions.scalable(2.0F, 5.0F);
        }
        return super.getDefaultDimensions(pose);
    }

    public void setCocooned(boolean cocooned, @Nullable BlockPos anchor) {
        this.entityData.set(IS_COCOONED, cocooned);
        this.anchorPos = anchor;
        this.refreshDimensions();
        if (cocooned) {
            this.setAnimationState(ANIM_COCOON);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.postCocoonInvulnerableTicks > 0) {
            return false;
        }

        if (this.isCocooned() && !this.isHatching) {
            if (this.level().isClientSide || this.hurtTime > 0) return false;
            this.hurtTime = this.hurtDuration;
            this.cocoonHits++;
            if (this.cocoonHits >= COCOON_HIT_THRESHOLD) {
                this.startHatching();
            } else {
                this.setAnimationState(ANIM_COCOON_HIT);
                this.cocoonAnimTicks = COCOON_HIT_ANIM_TICKS;
            }
            SoundEvent hurtSound = this.getHurtSound(source);
            if (hurtSound != null) {
                this.playSound(hurtSound, this.getSoundVolume(), 1.0F);
            }
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return this.postCocoonInvulnerableTicks > 0 || super.isInvulnerableTo(source);
    }

    private void tickCombat(LivingEntity target) {
        FightPhase phase = this.getFightPhase();

        if (this.getAnimationState() == ANIM_SWIPE && this.animationTicks > 0) {
            this.tickSwipe(target);
            return;
        }

        if (this.getAnimationState() == ANIM_SPIT && this.animationTicks > 0) {
            this.tickStrafeMovement(target, phase, 0.72D);
            return;
        }

        this.tickStrafeMovement(target, phase, phase.moveSpeed);

        if (this.shouldSwipe(target, phase) && this.meleeCooldown <= 0) {
            this.startSwipe();
            return;
        }

        if (this.canStartSpit(target)) {
            this.startSpit();
        }
    }

    private void tickPatrol() {
        if (this.patrolTarget == null || this.distanceToSqr(this.patrolTarget) < 4.0D || this.tickCount % 40 == 0) {
            double targetX = this.getX() + this.random.nextDouble() * 14.0D - 7.0D;
            double targetZ = this.getZ() + this.random.nextDouble() * 14.0D - 7.0D;
            double targetY = this.getTerrainFollowY(targetX, targetZ, 3.5D, 5.0D);
            this.patrolTarget = new Vec3(targetX, targetY, targetZ);
        }

        this.getMoveControl().setWantedPosition(this.patrolTarget.x, this.patrolTarget.y, this.patrolTarget.z, 0.8D);
    }

    private void tickStrafeMovement(LivingEntity target, FightPhase phase, double speed) {
        if (this.strafeTicks <= 0) {
            this.startNewStrafeCycle(phase);
        }

        this.strafeTicks--;
        Vec3 strafePoint = this.getStrafePoint(target);
        this.getMoveControl().setWantedPosition(strafePoint.x, strafePoint.y, strafePoint.z, speed);

        if (this.random.nextFloat() < 0.02F) {
            this.orbitDirection *= -1.0F;
        }
    }

    private Vec3 getStrafePoint(LivingEntity target) {
        double angle = this.tickCount * 0.13D * this.orbitDirection + this.currentStrafeAngleOffset;
        double x = target.getX() + Math.cos(angle) * this.currentStrafeRadius;
        double z = target.getZ() + Math.sin(angle) * this.currentStrafeRadius;
        double y = this.getTerrainFollowY(
                x,
                z,
                this.currentFlightHeight + Mth.sin((float) (angle * 1.5D)) * 0.7D,
                this.currentFlightHeight + 1.1D
        );
        return new Vec3(x, y, z);
    }

    private double getTerrainFollowY(double x, double z, double minOffset, double maxOffset) {
        int groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
        double clampedMinOffset = Math.max(2.75D, minOffset);
        double clampedMaxOffset = Math.max(clampedMinOffset + 0.8D, maxOffset);
        double desiredY = groundY + clampedMinOffset + this.random.nextDouble() * (clampedMaxOffset - clampedMinOffset);
        return Math.min(desiredY, groundY + 7.0D);
    }

    private void startNewStrafeCycle(FightPhase phase) {
        this.currentStrafeDuration = phase.minStrafeTicks + this.random.nextInt(phase.maxStrafeTicks - phase.minStrafeTicks + 1);
        this.strafeTicks = this.currentStrafeDuration;
        this.currentStrafeRadius = phase.minRadius + this.random.nextDouble() * (phase.maxRadius - phase.minRadius);
        this.currentFlightHeight = phase.minHeight + this.random.nextDouble() * (phase.maxHeight - phase.minHeight);
        this.currentStrafeAngleOffset = this.random.nextDouble() * (Math.PI * 2.0D);
    }

    private boolean canStartSpit(LivingEntity target) {
        return this.spitCooldown <= 0
                && this.hasLineOfSight(target)
                && this.distanceToSqr(target) >= 16.0D
                && this.distanceToSqr(target) <= 400.0D;
    }

    private void startSpit() {
        this.setAnimationState(ANIM_SPIT);
        this.animationTicks = 20;
        this.spitWindupTicks = SPIT_WINDUP_TICKS;
        this.spitCooldown = this.getFightPhase().nextSpitCooldown(this.random);
        this.gameEvent(GameEvent.PROJECTILE_SHOOT);
    }

    private void fireCurrentSpitPattern() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 origin = this.getSpitOrigin();
        switch (this.getFightPhase()) {
            case PHASE_THREE -> {
                this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.POISON, -8.0F);
                this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.STICKY, 8.0F);
            }
            default -> {
                BrutalflyOrbEntity.OrbVariant variant = this.random.nextBoolean()
                        ? BrutalflyOrbEntity.OrbVariant.POISON
                        : BrutalflyOrbEntity.OrbVariant.STICKY;
                this.launchOrb(serverLevel, origin, target, variant, 0.0F);
            }
        }
    }

    private void launchOrb(ServerLevel serverLevel, Vec3 origin, LivingEntity target, BrutalflyOrbEntity.OrbVariant variant, float yawOffsetDegrees) {
        BrutalflyOrbEntity orb = AntarchyObjects.BRUTALFLY_ORB.get().create(serverLevel);
        if (orb == null) {
            return;
        }

        orb.setOwner(this);
        orb.setOrbVariant(variant);
        orb.moveTo(origin.x, origin.y, origin.z, this.getYRot(), this.getXRot());

        Vec3 shot = this.createShotVector(origin, target, yawOffsetDegrees);
        orb.shoot(shot.x, shot.y, shot.z, 0.95F, 0.0F);
        serverLevel.addFreshEntity(orb);
    }

    private Vec3 createShotVector(Vec3 origin, LivingEntity target, float yawOffsetDegrees) {
        Vec3 leadTarget = target.position().add(target.getDeltaMovement().scale(6.0D));
        Vec3 direction = leadTarget.subtract(origin).normalize();
        if (yawOffsetDegrees == 0.0F) {
            return direction;
        }

        double radians = Math.toRadians(yawOffsetDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(
                direction.x * cos - direction.z * sin,
                direction.y,
                direction.x * sin + direction.z * cos
        ).normalize();
    }

    private Vec3 getSpitOrigin() {
        return this.position().add(0.0D, this.getBbHeight() * 0.58D, 0.0D).add(this.getViewVector(0.0F).scale(2.3D));
    }

    private boolean shouldSwipe(LivingEntity target, FightPhase phase) {
        double horizontalDistanceSqr = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ()).lengthSqr();
        boolean beneathBoss = horizontalDistanceSqr <= 16.0D && target.getY() <= this.getY() + 2.0D;
        return beneathBoss || this.distanceToSqr(target) <= phase.swipeRangeSqr;
    }

    private void startSwipe() {
        this.setAnimationState(ANIM_SWIPE);
        this.animationTicks = SWIPE_TICKS;
        this.meleeCooldown = this.getFightPhase().nextMeleeCooldown(this.random);
    }

    private void tickSwipe(LivingEntity target) {
        Vec3 divePoint = target.position().add(0.0D, 1.0D, 0.0D);
        this.getMoveControl().setWantedPosition(divePoint.x, divePoint.y, divePoint.z, 1.45D);

        if (this.animationTicks == 8) {
            this.performSwipeHit();
        }
    }

    private void performSwipeHit() {
        float swipeDamage = (float) AntarchySettings.brutalflySwipeDamage();
        AABB attackBox = this.getBoundingBox().inflate(4.8D, 2.4D, 4.8D).expandTowards(0.0D, -2.5D, 0.0D);
        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                attackBox,
                entity -> entity.isAlive() && entity != this
        )) {
            if (!this.hasLineOfSight(target)) {
                continue;
            }

            target.hurt(this.damageSources().mobAttack(this), swipeDamage);

            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            if (dx * dx + dz * dz < 1.0E-4D) {
                dx = this.random.nextDouble() - 0.5D;
                dz = this.random.nextDouble() - 0.5D;
            }
            target.knockback(SWIPE_KNOCKBACK, -dx, -dz);
            target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.3D, 0.0D));
        }
    }

    private FightPhase getFightPhase() {
        float healthRatio = this.getHealth() / this.getMaxHealth();
        if (healthRatio > 0.70F) {
            return FightPhase.PHASE_ONE;
        }
        if (healthRatio > 0.35F) {
            return FightPhase.PHASE_TWO;
        }
        return FightPhase.PHASE_THREE;
    }

    private int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private void updateBaseAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }

        if (this.animationTicks > 0 && (this.getAnimationState() == ANIM_SPIT || this.getAnimationState() == ANIM_SWIPE)) {
            return;
        }

        if (this.onGround()) {
            this.setAnimationState(ANIM_IDLE);
        } else {
            this.setAnimationState(ANIM_FLY);
        }
    }

    private void updateFlightRotation() {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-5D) {
            return;
        }

        double horizontal = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
        this.setYRot((float) (Mth.atan2(movement.z, movement.x) * (180.0F / Math.PI)) - 90.0F);
        this.setXRot((float) (-(Mth.atan2(movement.y, horizontal) * (180.0F / Math.PI))));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    @Override
    protected void tickDeath() {
        this.deathAnimationTicks++;
        this.setAnimationState(ANIM_DEATH);
        this.setDeltaMovement(Vec3.ZERO);

        if (!this.spawnedDeathButterflies && this.deathAnimationTicks >= DEATH_ANIM_TICKS - 10) {
            this.spawnDeathButterflies();
            this.spawnedDeathButterflies = true;
        }

        if (this.deathAnimationTicks >= DEATH_ANIM_TICKS) {
            this.remove(Entity.RemovalReason.KILLED);
            this.gameEvent(GameEvent.ENTITY_DIE);
        }
    }

    private void spawnDeathButterflies() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int count = 5 + this.random.nextInt(6);
        for (int i = 0; i < count; i++) {
            var butterfly = AntarchyObjects.BUTTERFLY.get().create(serverLevel);
            if (butterfly == null) {
                continue;
            }

            butterfly.moveTo(
                    this.getX() + (this.random.nextDouble() - 0.5D) * 2.5D,
                    this.getY() + 1.0D + this.random.nextDouble() * 1.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 2.5D,
                    this.random.nextFloat() * 360.0F,
                    0.0F
            );
            butterfly.setDeltaMovement(
                    (this.random.nextDouble() - 0.5D) * 0.35D,
                    0.12D + this.random.nextDouble() * 0.15D,
                    (this.random.nextDouble() - 0.5D) * 0.35D
            );
            serverLevel.addFreshEntity(butterfly);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        this.bossEvent.addPlayer(serverPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        this.bossEvent.removePlayer(serverPlayer);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsCocooned", this.isCocooned());
        tag.putInt("CocoonHits", this.cocoonHits);
        if (this.anchorPos != null) {
            tag.putInt("AnchorX", this.anchorPos.getX());
            tag.putInt("AnchorY", this.anchorPos.getY());
            tag.putInt("AnchorZ", this.anchorPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("IsCocooned")) {
            BlockPos anchor = null;
            if (tag.contains("AnchorX")) {
                anchor = new BlockPos(tag.getInt("AnchorX"), tag.getInt("AnchorY"), tag.getInt("AnchorZ"));
            }
            this.setCocooned(true, anchor);
            this.cocoonHits = tag.getInt("CocoonHits");
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.BRUTALFLY_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.BRUTALFLY_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.4F;
    }

    private enum FightPhase {
        PHASE_ONE(0.95D, 24, 34, 7.0D, 9.0D, 3.5D, 4.6D, 22.0D),
        PHASE_TWO(1.12D, 18, 28, 5.3D, 7.0D, 2.4D, 3.2D, 19.0D),
        PHASE_THREE(1.28D, 14, 22, 3.8D, 5.4D, 1.3D, 2.1D, 26.0D);

        final double moveSpeed;
        final int minStrafeTicks;
        final int maxStrafeTicks;
        final double minRadius;
        final double maxRadius;
        final double minHeight;
        final double maxHeight;
        final double swipeRangeSqr;

        FightPhase(double moveSpeed, int minStrafeTicks, int maxStrafeTicks, double minRadius, double maxRadius, double minHeight, double maxHeight, double swipeRange) {
            this.moveSpeed = moveSpeed;
            this.minStrafeTicks = minStrafeTicks;
            this.maxStrafeTicks = maxStrafeTicks;
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
            this.swipeRangeSqr = swipeRange;
        }

        int nextSpitCooldown(RandomSource random) {
            return switch (this) {
                case PHASE_ONE -> 70 + random.nextInt(26);
                case PHASE_TWO -> 42 + random.nextInt(19);
                case PHASE_THREE -> 26 + random.nextInt(15);
            };
        }

        int nextMeleeCooldown(RandomSource random) {
            return switch (this) {
                case PHASE_ONE -> 42 + random.nextInt(10);
                case PHASE_TWO -> 30 + random.nextInt(8);
                case PHASE_THREE -> 20 + random.nextInt(7);
            };
        }
    }

    private static final class BrutalflyHoverGoal extends Goal {
        private final BrutalflyEntity brutalfly;

        private BrutalflyHoverGoal(BrutalflyEntity brutalfly) {
            this.brutalfly = brutalfly;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.brutalfly.getTarget();
            if (target != null) {
                this.brutalfly.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }
}
