package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatWaterStepMixin extends Entity {
    @Unique
    private static final float ANTARCHY$WATER_FRICTION = 0.9F;
    @Unique
    private static final double ANTARCHY$RISE_SPEED = 0.25D;
    @Unique
    private static final double ANTARCHY$SURFACE_OFFSET = 0.101D;

    @Shadow
    private Boat.Status status;
    @Shadow
    private Boat.Status oldStatus;
    @Shadow
    private float invFriction;
    @Shadow
    private float deltaRotation;

    protected BoatWaterStepMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "floatBoat", at = @At("HEAD"), cancellable = true)
    private void antarchy$climbWaterStep(CallbackInfo ci) {
        if (this.status != Boat.Status.UNDER_WATER && this.status != Boat.Status.UNDER_FLOWING_WATER) {
            return;
        }
        if (this.oldStatus == Boat.Status.IN_AIR || !AntarchySettings.boatsClimbWaterSteps()) {
            return;
        }
        double rise = this.antarchy$waterStepRise();
        if (rise <= 0.0D) {
            return;
        }
        this.invFriction = ANTARCHY$WATER_FRICTION;
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(
                motion.x * ANTARCHY$WATER_FRICTION,
                Math.max(motion.y, Math.min(ANTARCHY$RISE_SPEED, rise)),
                motion.z * ANTARCHY$WATER_FRICTION);
        this.deltaRotation *= ANTARCHY$WATER_FRICTION;
        ci.cancel();
    }

    @Unique
    private double antarchy$waterStepRise() {
        double maxStep = AntarchySettings.boatWaterStepMaxHeight();
        if (maxStep <= 0.0D) {
            return 0.0D;
        }
        AABB box = this.getBoundingBox();
        int minX = Mth.floor(box.minX);
        int maxX = Mth.ceil(box.maxX);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.ceil(box.maxZ);
        double offset = ANTARCHY$SURFACE_OFFSET - this.getBbHeight();
        double limit = this.getY() + maxStep - offset;
        int topY = Mth.floor(limit);
        Level level = this.level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = Mth.floor(box.maxY); y <= topY; y++) {
            float height = 0.0F;
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    pos.set(x, y, z);
                    FluidState fluid = level.getFluidState(pos);
                    if (fluid.is(FluidTags.WATER)) {
                        height = Math.max(height, fluid.getHeight(level, pos));
                    }
                }
            }
            if (height < 1.0F) {
                double surface = y + height;
                return surface > limit ? 0.0D : surface + offset - this.getY();
            }
        }
        return 0.0D;
    }
}
