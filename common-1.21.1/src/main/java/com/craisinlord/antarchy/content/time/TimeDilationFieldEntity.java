package com.craisinlord.antarchy.content.time;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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

    public TimeDilationFieldEntity(EntityType<? extends TimeDilationFieldEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static TimeDilationFieldEntity create(Level level, Vec3 center, double radius, double rate, int durationTicks) {
        TimeDilationFieldEntity field = new TimeDilationFieldEntity(AntarchyObjects.TIME_DILATION_FIELD.get(), level);
        field.setPos(center.x, center.y, center.z);
        field.setFieldRadius(radius);
        field.setFieldRate(rate);
        field.durationTicks = durationTicks;
        return field;
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble(RADIUS_KEY, this.fieldRadius());
        tag.putDouble(RATE_KEY, this.fieldRate());
        tag.putInt(DURATION_KEY, this.durationTicks);
        tag.putInt(AGE_KEY, this.age);
    }
}
