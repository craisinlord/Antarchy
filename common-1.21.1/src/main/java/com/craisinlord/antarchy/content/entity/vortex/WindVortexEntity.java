package com.craisinlord.antarchy.content.entity.vortex;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class WindVortexEntity extends Entity {
    private static final EntityDataAccessor<Float> HEIGHT =
            SynchedEntityData.defineId(WindVortexEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TOP_RADIUS =
            SynchedEntityData.defineId(WindVortexEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DAMAGING =
            SynchedEntityData.defineId(WindVortexEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.defineId(WindVortexEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AXIS_X =
            SynchedEntityData.defineId(WindVortexEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AXIS_Y =
            SynchedEntityData.defineId(WindVortexEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AXIS_Z =
            SynchedEntityData.defineId(WindVortexEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FADING_OUT =
            SynchedEntityData.defineId(WindVortexEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String AGE_KEY = "Age";
    private static final String DURATION_KEY = "Duration";
    private static final String HEIGHT_KEY = "Height";
    private static final String TOP_RADIUS_KEY = "TopRadius";
    private static final String PULL_STRENGTH_KEY = "PullStrength";
    private static final String LAUNCH_STRENGTH_KEY = "LaunchStrength";
    private static final String DAMAGING_KEY = "Damaging";
    private static final String OWNER_KEY = "Owner";
    private static final String MODE_KEY = "Mode";
    private static final String AXIS_X_KEY = "AxisX";
    private static final String AXIS_Y_KEY = "AxisY";
    private static final String AXIS_Z_KEY = "AxisZ";
    private static final String TRAVEL_X_KEY = "TravelX";
    private static final String TRAVEL_Y_KEY = "TravelY";
    private static final String TRAVEL_Z_KEY = "TravelZ";
    private static final String TRAVELLING_KEY = "Travelling";
    private static final String HOMING_KEY = "Homing";

    private static final double BASE_RADIUS = 0.35D;
    private static final double DRIFT_FRICTION = 0.995D;
    private static final double FALL_ACCELERATION = 0.006D;
    private static final double MAX_FALL_SPEED = 0.08D;
    private static final double MINIMUM_DOWNWARD_DRIFT = 0.012D;
    private static final int FADE_OUT_TICKS = 8;

    public enum VortexMode {
        UPWARD,
        LENS_PULL,
        LENS_PUSH,
        GATHER_RETURN
    }

    private int age;
    private int durationTicks = 140;
    private double pullStrength = 0.32D;
    private double launchStrength = 1.0D;
    private float damageOverride = -1.0F;
    private Vec3 travelVelocity = Vec3.ZERO;
    private boolean travelling = false;
    private boolean homing = false;
    private int travelTicksRemaining;
    private int fadeOutTicksRemaining;
    private final Map<UUID, Double> carriedProgress = new HashMap<>();
    @Nullable
    private UUID ownerUuid;

    public WindVortexEntity(EntityType<? extends WindVortexEntity> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    public static WindVortexEntity create(Level level, EntityType<? extends WindVortexEntity> type, Vec3 position,
            Vec3 drift, @Nullable LivingEntity owner, boolean damaging) {
        WindVortexEntity vortex = new WindVortexEntity(type, level);
        vortex.setPos(position.x, position.y, position.z);
        vortex.setDeltaMovement(drift);
        vortex.setVortexSize(5.0F, 1.5F);
        vortex.setAxis(Direction.UP);
        vortex.setMode(VortexMode.UPWARD);
        vortex.durationTicks = Math.max(1, AntarchySettings.windVortexDurationTicks());
        vortex.pullStrength = AntarchySettings.windVortexPullStrength();
        vortex.launchStrength = AntarchySettings.windVortexLaunchStrength();
        vortex.setDamaging(damaging);
        if (owner != null) {
            vortex.ownerUuid = owner.getUUID();
        }
        return vortex;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HEIGHT, 5.0F);
        builder.define(TOP_RADIUS, 1.5F);
        builder.define(DAMAGING, false);
        builder.define(MODE, VortexMode.UPWARD.ordinal());
        builder.define(AXIS_X, 0.0F);
        builder.define(AXIS_Y, 1.0F);
        builder.define(AXIS_Z, 0.0F);
        builder.define(FADING_OUT, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (this.fadeOutTicksRemaining > 0) {
                this.fadeOutTicksRemaining--;
            }
            return;
        }

        if (this.isFadingOut()) {
            if (--this.fadeOutTicksRemaining <= 0) {
                this.discard();
            }
            return;
        }

        boolean lensVortex = this.getMode() != VortexMode.UPWARD;
        if (!lensVortex && (this.isInLava() || this.isInsideSolidBlock())) {
            this.fadeOut();
            return;
        }

        this.age++;
        if (!lensVortex && this.age >= this.durationTicks) {
            this.fadeOut();
            return;
        }

        if (this.travelling) {
            if (this.homing) {
                this.steerTowardPrey();
            }
            Vec3 from = this.position();
            Vec3 to = from.add(this.travelVelocity);
            HitResult blockHit = this.level().clip(
                    new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (blockHit.getType() != HitResult.Type.MISS) {
                this.fadeOut();
                return;
            }
            this.setDeltaMovement(this.travelVelocity);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.travelVelocity);
            if (--this.travelTicksRemaining <= 0) {
                this.travelling = false;
                this.noPhysics = false;
                this.setDeltaMovement(this.travelVelocity);
            }
        } else if (!lensVortex) {
            Vec3 current = this.getDeltaMovement();
            double downwardSpeed = Math.min(MAX_FALL_SPEED, Math.max(MINIMUM_DOWNWARD_DRIFT,
                    -current.y + FALL_ACCELERATION));
            Vec3 drift = new Vec3(
                    current.x * DRIFT_FRICTION,
                    -downwardSpeed,
                    current.z * DRIFT_FRICTION);
            Vec3 from = this.position();
            Vec3 to = from.add(drift);
            HitResult blockHit = this.level().clip(
                    new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (blockHit.getType() != HitResult.Type.MISS) {
                this.fadeOut();
                return;
            }
            this.setDeltaMovement(drift);
            this.move(net.minecraft.world.entity.MoverType.SELF, drift);
        } else {
            Vec3 drift = this.getDeltaMovement().multiply(DRIFT_FRICTION, DRIFT_FRICTION, DRIFT_FRICTION);
            this.setDeltaMovement(drift);
            this.move(net.minecraft.world.entity.MoverType.SELF, drift);
        }

        if ((this.tickCount & 1) == 0) {
            this.applyVortexForces();
        }
    }

    private void steerTowardPrey() {
        double speed = this.travelVelocity.length();
        if (speed < 1.0E-4D) {
            return;
        }
        AABB seekArea = this.getBoundingBox().inflate(12.0D);
        Entity best = null;
        double bestDistSqr = Double.MAX_VALUE;
        for (Entity candidate : this.level().getEntities(this, seekArea, this::isSeekTarget)) {
            double distSqr = candidate.distanceToSqr(this);
            if (distSqr < bestDistSqr) {
                bestDistSqr = distSqr;
                best = candidate;
            }
        }
        if (best == null) {
            return;
        }
        Vec3 desired = best.getBoundingBox().getCenter().subtract(this.position());
        if (desired.lengthSqr() < 1.0E-6D) {
            return;
        }
        Vec3 steered = this.travelVelocity.normalize().lerp(desired.normalize(), 0.18D);
        if (steered.lengthSqr() < 1.0E-6D) {
            return;
        }
        this.travelVelocity = steered.normalize().scale(speed);
    }

    private boolean isSeekTarget(Entity entity) {
        return entity instanceof LivingEntity && !(entity instanceof Player) && !this.isOwnedBy(entity)
                && this.canAffectEntity(entity);
    }

    private boolean isInsideSolidBlock() {
        BlockPos pos = this.blockPosition();
        BlockState state = this.level().getBlockState(pos);
        return !state.getCollisionShape(this.level(), pos).isEmpty();
    }

    private void fadeOut() {
        if (!this.level().isClientSide && !this.isFadingOut()) {
            this.entityData.set(FADING_OUT, true);
            this.fadeOutTicksRemaining = FADE_OUT_TICKS;
            this.level().broadcastEntityEvent(this, (byte) 60);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 60) {
            this.fadeOutTicksRemaining = FADE_OUT_TICKS;
            return;
        }
        super.handleEntityEvent(id);
    }

    private void applyVortexForces() {
        double height = this.getVortexHeight();
        double maxRadius = this.getTopRadius();
        AABB area = this.getBoundingBox().inflate(maxRadius + height + 1.0D);
        Basis basis = this.basis();
        VortexMode mode = this.getMode();

        for (Entity entity : this.level().getEntities(this, area, this::canAffectEntity)) {
            this.captureEntity(entity, height, basis, mode);
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (ServerPlayer player : serverLevel.players()) {
            if (area.intersects(player.getBoundingBox())) {
                this.captureEntity(player, height, basis, mode);
            }
        }

        Iterator<Map.Entry<UUID, Double>> iterator = this.carriedProgress.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Double> entry = iterator.next();
            Entity entity = serverLevel.getEntity(entry.getKey());
            if (entity == null || !this.canAffectEntity(entity)) {
                iterator.remove();
                continue;
            }

            double progress = entry.getValue();
            double direction = mode == VortexMode.LENS_PULL ? -1.0D : 1.0D;
            progress += direction * 0.055D;
            if (this.travelling) {
                progress = Math.min(progress, 0.85D);
            }
            if (mode == VortexMode.LENS_PULL && progress <= 0.0D) {
                iterator.remove();
                continue;
            }

            if (mode == VortexMode.GATHER_RETURN && this.age >= this.gatherReturnAtAge()) {
                this.launchTowardOwner(entity);
                iterator.remove();
                continue;
            }

            if (mode == VortexMode.UPWARD && progress >= 0.92D && !this.travelling) {
                this.launch(entity, this.radialVectorAt(entity, progress, basis), progress, basis);
                iterator.remove();
                continue;
            }

            if (mode == VortexMode.LENS_PUSH && progress >= 0.94D) {
                this.launch(entity, this.radialVectorAt(entity, progress, basis), progress, basis);
                iterator.remove();
                continue;
            }

            double shapeProgress = mode == VortexMode.LENS_PULL ? progress : progress;
            double radius = this.radiusAt(shapeProgress);
            Vec3 relative = entity.position().subtract(this.position());
            Vec3 axisPoint = basis.axis.scale(relative.dot(basis.axis));
            Vec3 radialVector = relative.subtract(axisPoint);
            this.spin(entity, radialVector, progress, shapeProgress, radius, basis, mode);
            if (mode == VortexMode.UPWARD) {
                entity.setOnGround(false);
            }
            this.hurtCaughtEntity(entity);
            entry.setValue(progress);
        }
    }

    private Vec3 radialVectorAt(Entity entity, double progress, Basis basis) {
        Vec3 relative = entity.position().subtract(this.position());
        Vec3 axisPoint = basis.axis.scale(relative.dot(basis.axis));
        Vec3 radial = relative.subtract(axisPoint);
        return radial.lengthSqr() < 1.0E-6D ? basis.sideA.scale(this.radiusAt(progress)) : radial;
    }

    private void captureEntity(Entity entity, double height, Basis basis, VortexMode mode) {
        if (!this.canAffectEntity(entity)) {
            return;
        }
        Vec3 relative = entity.position().subtract(this.position());
        double axisDistance = relative.dot(basis.axis);
        if (axisDistance < -1.0D || axisDistance > height + 2.0D) {
            return;
        }
        double along = Mth.clamp(axisDistance, 0.0D, height);
        double currentProgress = height <= 0.0D ? 1.0D : along / height;
        double captureProgress = mode == VortexMode.LENS_PULL ? 1.0D - currentProgress : currentProgress;
        double shapeProgress = mode == VortexMode.LENS_PULL ? 1.0D - currentProgress : currentProgress;
        double radius = this.radiusAt(shapeProgress);
        Vec3 axisPoint = basis.axis.scale(axisDistance);
        Vec3 radialVector = relative.subtract(axisPoint);
        double captureRadius = radius * 1.35D + (entity instanceof Player ? 1.5D : 1.75D);
        boolean requireLos = !this.travelling && mode == VortexMode.GATHER_RETURN;

        if (radialVector.lengthSqr() > captureRadius * captureRadius
                || (requireLos && !this.hasClearPathTo(entity))) {
            return;
        }

        this.carriedProgress.putIfAbsent(entity.getUUID(), captureProgress);
    }

    private boolean canAffectEntity(Entity entity) {
        if (entity instanceof WindVortexEntity
                || !entity.isAlive()
                || entity.isSpectator()
                || entity.noPhysics
                || entity.getType().is(AntarchyTags.Entities.WIND_VORTEX_IMMUNE)) {
            return false;
        }
        return true;
    }

    private boolean hasClearPathTo(Entity entity) {
        Vec3 from = this.position().add(this.getAxis().scale(Math.min(entity.getBbHeight() * 0.5D, this.getVortexHeight())));
        Vec3 to = entity.getBoundingBox().getCenter();
        HitResult result = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS;
    }

    private void spin(Entity entity, Vec3 radialVector, double progress, double shapeProgress, double radius, Basis basis, VortexMode mode) {
        double horizontal = Math.max(0.08D, radialVector.length());
        Vec3 radial = radialVector.lengthSqr() < 1.0E-6D ? basis.sideA : radialVector.scale(1.0D / horizontal);
        Vec3 tangent = basis.axis.cross(radial).normalize();
        double targetRadius = mode == VortexMode.LENS_PULL || mode == VortexMode.GATHER_RETURN
                ? Math.max(0.05D, radius * 0.18D)
                : Mth.lerp(shapeProgress, BASE_RADIUS, radius * 0.82D);
        double radialError = targetRadius - horizontal;
        boolean lensMode = mode == VortexMode.LENS_PULL || mode == VortexMode.LENS_PUSH;
        double lensLivingBoost = lensMode && entity instanceof LivingEntity ? 1.6D : 1.0D;
        double playerScale = (entity instanceof Player ? 1.8D : 1.35D) * lensLivingBoost;
        double pull = this.pullStrength * playerScale * (entity.isShiftKeyDown() ? 0.5D : 1.0D);
        double massScale = Mth.clamp(1.35D / Math.max(0.35D, entity.getBbWidth() * entity.getBbHeight()), 0.28D, 1.0D);
        double spin = (0.24D + progress * 0.34D) * massScale * playerScale;
        double axialSpeed = switch (mode) {
            case LENS_PULL -> -(0.18D + (1.0D - progress) * 0.18D) * massScale;
            case LENS_PUSH -> (0.18D + progress * 0.22D) * massScale;
            case UPWARD -> (0.13D + progress * 0.16D) * massScale;
            case GATHER_RETURN -> (0.05D + progress * 0.08D) * massScale;
        } * playerScale;
        if (mode == VortexMode.UPWARD && entity.onGround()) {
            axialSpeed = Math.max(axialSpeed, entity instanceof Player ? 0.38D : 0.3D);
        }

        Vec3 wanted = radial.scale(radialError * pull)
                .add(tangent.scale(spin))
                .add(basis.axis.scale(axialSpeed));
        Vec3 current = entity.getDeltaMovement();
        Vec3 updatedMovement = new Vec3(
                Mth.lerp(0.72D, current.x, wanted.x),
                Mth.lerp(0.58D, current.y, wanted.y),
                Mth.lerp(0.72D, current.z, wanted.z)
        );
        if (this.travelling) {
            updatedMovement = updatedMovement.add(this.travelVelocity.x * 0.8D, 0.0D, this.travelVelocity.z * 0.8D);
        }
        if (mode == VortexMode.UPWARD && entity.onGround()) {
            double minRise = entity instanceof Player ? 0.38D : 0.3D;
            updatedMovement = new Vec3(updatedMovement.x, Math.max(updatedMovement.y, minRise), updatedMovement.z);
            entity.setOnGround(false);
        }
        if (entity instanceof Player) {
            Vec3 before = entity.position();
            Vec3 playerCarry = wanted.scale(0.65D);
            entity.move(net.minecraft.world.entity.MoverType.SELF, playerCarry);
            if (entity.position().distanceToSqr(before) < 1.0E-6D && playerCarry.lengthSqr() > 1.0E-6D) {
                entity.setPos(before.add(playerCarry));
            }
            entity.setOnGround(false);
        }
        entity.setDeltaMovement(updatedMovement);
        entity.fallDistance = 0.0F;
        entity.hasImpulse = true;
    }

    private void launch(Entity entity, Vec3 radialVector, double progress, Basis basis) {
        double horizontal = Math.max(0.08D, radialVector.length());
        Vec3 radial = radialVector.lengthSqr() < 1.0E-6D ? basis.sideA : radialVector.scale(1.0D / horizontal);
        Vec3 tangent = basis.axis.cross(radial).normalize();
        double scale = this.launchStrength * Mth.clamp(this.getVortexHeight() / 5.0D, 0.5D, 2.5D);
        entity.setDeltaMovement(tangent.scale(0.72D * scale).add(radial.scale(0.34D * scale)).add(basis.axis.scale((0.78D + progress * 0.28D) * scale)));
        entity.hasImpulse = true;
    }

    private void hurtCaughtEntity(Entity entity) {
        if (!this.isDamaging() || !(entity instanceof LivingEntity living) || living.tickCount % 20 != 0) {
            return;
        }
        if (living.getType().is(AntarchyTags.Entities.WIND_VORTEX_IMMUNE)) {
            return;
        }
        living.hurt(this.damageSource(), this.resolveDamage());
    }

    private float resolveDamage() {
        if (this.damageOverride >= 0.0F) {
            return this.damageOverride;
        }
        return (float) (2.0D * Mth.clamp(this.getTopRadius() / 1.5D, 0.5D, 3.0D));
    }

    private int gatherReturnAtAge() {
        return Math.max(10, (int) (this.durationTicks * 0.55D));
    }

    private void launchTowardOwner(Entity entity) {
        Entity owner = this.getOwnerEntity();
        Vec3 target = owner != null ? owner.getEyePosition() : this.position().add(0.0D, 1.0D, 0.0D);
        Vec3 toOwner = target.subtract(entity.position());
        double distance = toOwner.length();
        Vec3 direction = distance < 1.0E-4D ? new Vec3(0.0D, 1.0D, 0.0D) : toOwner.scale(1.0D / distance);
        double arc = Mth.clamp(distance * 0.06D, 0.25D, 1.1D);
        double power = this.launchStrength * Mth.clamp(distance / 6.0D, 0.6D, 2.0D);
        entity.setDeltaMovement(direction.scale(power).add(0.0D, arc, 0.0D));
        entity.fallDistance = 0.0F;
        entity.hasImpulse = true;
        entity.hurtMarked = true;
        if (this.isDamaging() && entity instanceof LivingEntity living
                && !living.getType().is(AntarchyTags.Entities.WIND_VORTEX_IMMUNE)) {
            living.hurt(this.damageSource(), this.resolveDamage());
        }
    }

    private DamageSource damageSource() {
        Entity owner = this.getOwnerEntity();
        if (owner instanceof LivingEntity living) {
            return this.damageSources().mobAttack(living);
        }
        return this.damageSources().magic();
    }

    @Nullable
    private Entity getOwnerEntity() {
        if (this.ownerUuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getEntity(this.ownerUuid);
    }

    private double radiusAt(double progress) {
        return Mth.lerp(progress, BASE_RADIUS, this.getTopRadius());
    }

    public void setVortexSize(float height, float topRadius) {
        this.entityData.set(HEIGHT, height);
        this.entityData.set(TOP_RADIUS, topRadius);
    }

    public float getVortexHeight() {
        return this.entityData.get(HEIGHT);
    }

    public float getTopRadius() {
        return this.entityData.get(TOP_RADIUS);
    }

    public void setMode(VortexMode mode) {
        this.entityData.set(MODE, mode.ordinal());
    }

    public VortexMode getMode() {
        int ordinal = Mth.clamp(this.entityData.get(MODE), 0, VortexMode.values().length - 1);
        return VortexMode.values()[ordinal];
    }

    public void setAxis(Direction direction) {
        this.setAxis(Vec3.atLowerCornerOf(direction.getNormal()));
    }

    public void setAxis(Vec3 axis) {
        Vec3 normalized = axis.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 1.0D, 0.0D) : axis.normalize();
        this.entityData.set(AXIS_X, (float) normalized.x);
        this.entityData.set(AXIS_Y, (float) normalized.y);
        this.entityData.set(AXIS_Z, (float) normalized.z);
    }

    public Vec3 getAxis() {
        Vec3 axis = new Vec3(this.entityData.get(AXIS_X), this.entityData.get(AXIS_Y), this.entityData.get(AXIS_Z));
        return axis.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 1.0D, 0.0D) : axis.normalize();
    }

    public int getVortexAge() {
        return this.age;
    }

    public int getVortexDurationTicks() {
        return this.durationTicks;
    }

    public void setVortexDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    public void resetVortexAge() {
        this.age = 0;
    }

    public void setTravel(Vec3 velocity) {
        this.travelVelocity = velocity;
        this.travelling = true;
        this.travelTicksRemaining = 24;
        this.noPhysics = true;
    }

    public void setTravelDuration(int ticks) {
        this.travelTicksRemaining = Math.max(1, ticks);
    }

    public void setHoming(boolean homing) {
        this.homing = homing;
    }

    public void setVortexStrengths(double pullStrength, double launchStrength) {
        this.pullStrength = Math.max(0.0D, pullStrength);
        this.launchStrength = Math.max(0.0D, launchStrength);
    }

    public void setDamageOverride(float damage) {
        this.damageOverride = damage;
    }

    public void removeFromLens() {
        this.fadeOut();
    }

    public boolean isDamaging() {
        return this.entityData.get(DAMAGING);
    }

    public boolean isOwnedBy(Entity entity) {
        return entity != null && this.ownerUuid != null && this.ownerUuid.equals(entity.getUUID());
    }

    public void setDamaging(boolean damaging) {
        this.entityData.set(DAMAGING, damaging);
    }

    public boolean isFadingOut() {
        return this.entityData.get(FADING_OUT);
    }

    public float getFadeOutProgress(float partialTick) {
        if (!this.isFadingOut()) {
            return 1.0F;
        }
        return Mth.clamp((this.fadeOutTicksRemaining - partialTick) / (float) FADE_OUT_TICKS, 0.0F, 1.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.age = tag.getInt(AGE_KEY);
        this.durationTicks = tag.getInt(DURATION_KEY);
        this.setVortexSize(tag.getFloat(HEIGHT_KEY), tag.getFloat(TOP_RADIUS_KEY));
        this.pullStrength = tag.getDouble(PULL_STRENGTH_KEY);
        this.launchStrength = tag.getDouble(LAUNCH_STRENGTH_KEY);
        this.setDamaging(tag.getBoolean(DAMAGING_KEY));
        this.setMode(VortexMode.values()[Mth.clamp(tag.getInt(MODE_KEY), 0, VortexMode.values().length - 1)]);
        this.setAxis(new Vec3(tag.getFloat(AXIS_X_KEY), tag.getFloat(AXIS_Y_KEY), tag.getFloat(AXIS_Z_KEY)));
        this.travelling = tag.getBoolean(TRAVELLING_KEY);
        this.homing = tag.getBoolean(HOMING_KEY);
        this.travelVelocity = new Vec3(tag.getDouble(TRAVEL_X_KEY), tag.getDouble(TRAVEL_Y_KEY), tag.getDouble(TRAVEL_Z_KEY));
        if (this.travelling) {
            this.noPhysics = true;
        }
        if (tag.hasUUID(OWNER_KEY)) {
            this.ownerUuid = tag.getUUID(OWNER_KEY);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(AGE_KEY, this.age);
        tag.putInt(DURATION_KEY, this.durationTicks);
        tag.putFloat(HEIGHT_KEY, this.getVortexHeight());
        tag.putFloat(TOP_RADIUS_KEY, this.getTopRadius());
        tag.putDouble(PULL_STRENGTH_KEY, this.pullStrength);
        tag.putDouble(LAUNCH_STRENGTH_KEY, this.launchStrength);
        tag.putBoolean(DAMAGING_KEY, this.isDamaging());
        tag.putInt(MODE_KEY, this.getMode().ordinal());
        tag.putFloat(AXIS_X_KEY, (float) this.getAxis().x);
        tag.putFloat(AXIS_Y_KEY, (float) this.getAxis().y);
        tag.putFloat(AXIS_Z_KEY, (float) this.getAxis().z);
        tag.putBoolean(TRAVELLING_KEY, this.travelling);
        tag.putBoolean(HOMING_KEY, this.homing);
        tag.putDouble(TRAVEL_X_KEY, this.travelVelocity.x);
        tag.putDouble(TRAVEL_Y_KEY, this.travelVelocity.y);
        tag.putDouble(TRAVEL_Z_KEY, this.travelVelocity.z);
        if (this.ownerUuid != null) {
            tag.putUUID(OWNER_KEY, this.ownerUuid);
        }
    }

    public static final class Basis {
        public final Vec3 axis;
        public final Vec3 sideA;
        public final Vec3 sideB;

        private Basis(Vec3 axis, Vec3 sideA, Vec3 sideB) {
            this.axis = axis;
            this.sideA = sideA;
            this.sideB = sideB;
        }
    }

    public Basis basis() {
        Vec3 axis = this.getAxis();
        Vec3 reference = Math.abs(axis.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 sideA = reference.cross(axis).normalize();
        Vec3 sideB = axis.cross(sideA).normalize();
        return new Basis(axis, sideA, sideB);
    }
}
