package com.craisinlord.antarchy.content.time;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalBlackHoleEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public class TimeDilationFieldEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(TimeDilationFieldEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RATE =
            SynchedEntityData.defineId(TimeDilationFieldEntity.class, EntityDataSerializers.FLOAT);

    private static final String RADIUS_KEY = "Radius";
    private static final String RATE_KEY = "Rate";
    private static final String DURATION_KEY = "DurationTicks";
    private static final String AGE_KEY = "Age";

    private int durationTicks = -1;
    private int age;
    private UUID ownerId;
    private UUID anchorId;
    private int missingAnchorTicks;
    private boolean visual = true;

    public TimeDilationFieldEntity(EntityType<? extends TimeDilationFieldEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static TimeDilationFieldEntity create(Level level, Vec3 center, double radius, double rate, int durationTicks) {
        return create(level, center, radius, rate, durationTicks, null);
    }

    public static TimeDilationFieldEntity create(Level level, Vec3 center, double radius, double rate, int durationTicks, UUID ownerId) {
        return create(level, center, radius, rate, durationTicks, ownerId, true);
    }

    public static TimeDilationFieldEntity create(Level level, Vec3 center, double radius, double rate, int durationTicks, UUID ownerId, boolean visual) {
        TimeDilationFieldEntity field = new TimeDilationFieldEntity(AntarchyObjects.TIME_DILATION_FIELD.get(), level);
        field.setPos(center.x, center.y, center.z);
        field.setFieldRadius(radius);
        field.setFieldRate(rate);
        field.durationTicks = durationTicks;
        field.ownerId = ownerId;
        field.visual = visual;
        return field;
    }

    public boolean isOwnedBy(Entity entity) {
        return this.ownerId != null && entity != null && this.ownerId.equals(entity.getUUID());
    }

    public void attachTo(Entity anchor) {
        this.anchorId = anchor == null ? null : anchor.getUUID();
        this.missingAnchorTicks = 0;
    }

    public boolean affects(Entity entity) {
        if (entity instanceof RoyalBlackHoleEntity || this.isOwnedBy(entity)
                || this.anchorId != null && this.anchorId.equals(entity.getUUID())) {
            return false;
        }
        if (this.ownerId != null && this.level() instanceof ServerLevel level
                && level.getEntity(this.ownerId) instanceof QueenEntity) {
            return !entity.getType().is(AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK);
        }
        return true;
    }

    public boolean isVisual() {
        return this.visual;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 8.0F);
        builder.define(RATE, (float) TimeDilationMath.NORMAL_RATE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.anchorId != null && this.level() instanceof ServerLevel level) {
            Entity anchor = level.getEntity(this.anchorId);
            if (anchor != null && anchor.isAlive()) {
                Vec3 center = anchor instanceof RoyalBlackHoleEntity blackHole
                        ? blackHole.effectCenter()
                        : anchor.position();
                this.setPos(center.x, center.y, center.z);
                this.missingAnchorTicks = 0;
            } else if (++this.missingAnchorTicks > 20) {
                this.discard();
                return;
            }
        }
        this.age++;
        if (this.durationTicks >= 0 && this.age >= this.durationTicks) {
            this.discard();
        }
    }

    public void setFieldRadius(double radius) {
        this.entityData.set(RADIUS, (float) Math.max(0.5D, radius));
    }

    public double fieldRadius() {
        return this.entityData.get(RADIUS);
    }

    public double fieldRadiusSqr() {
        double radius = this.fieldRadius();
        return radius * radius;
    }

    public void setFieldRate(double rate) {
        this.entityData.set(RATE, (float) TimeDilationMath.clampRate(rate));
    }

    public double fieldRate() {
        return TimeDilationMath.clampRate(this.entityData.get(RATE));
    }

    public double effectiveFieldRate() {
        return TimeDilationMath.effectiveFieldRate(this.fieldRate(), this.age, this.durationTicks);
    }

    public int fieldDurationTicks() {
        return this.durationTicks;
    }

    public int fieldAge() {
        return this.age;
    }

    public boolean isInfinite() {
        return this.durationTicks < 0;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setFieldRadius(tag.getDouble(RADIUS_KEY));
        this.setFieldRate(tag.getDouble(RATE_KEY));
        this.durationTicks = tag.contains(DURATION_KEY) ? tag.getInt(DURATION_KEY) : -1;
        this.age = tag.getInt(AGE_KEY);
        this.ownerId = tag.hasUUID("OwnerUuid") ? tag.getUUID("OwnerUuid") : null;
        this.anchorId = tag.hasUUID("AnchorUuid") ? tag.getUUID("AnchorUuid") : null;
        this.visual = !tag.contains("Visual") || tag.getBoolean("Visual");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble(RADIUS_KEY, this.fieldRadius());
        tag.putDouble(RATE_KEY, this.fieldRate());
        tag.putInt(DURATION_KEY, this.durationTicks);
        tag.putInt(AGE_KEY, this.age);
        if (this.ownerId != null) {
            tag.putUUID("OwnerUuid", this.ownerId);
        }
        if (this.anchorId != null) {
            tag.putUUID("AnchorUuid", this.anchorId);
        }
        tag.putBoolean("Visual", this.visual);
    }
}
