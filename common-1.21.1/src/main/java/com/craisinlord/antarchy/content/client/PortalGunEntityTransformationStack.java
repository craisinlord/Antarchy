package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransformUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class PortalGunEntityTransformationStack {
    private final Entity entity;
    private final Deque<EntityTransformation> stack = new ArrayDeque<>();

    public PortalGunEntityTransformationStack(Entity entity) {
        this.entity = entity;
    }

    public void push() {
        this.stack.push(EntityTransformation.capture(this.entity));
    }

    public void pop() {
        if (!this.stack.isEmpty()) {
            this.stack.pop().restore(this.entity);
        }
    }

    public Vec3 moveEntity(PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal, float partialTick) {
        double eyeHeight = this.entity.getEyeHeight();
        Vec3 interpolatedEye = new Vec3(
                net.minecraft.util.Mth.lerp(partialTick, this.entity.xo, this.entity.getX()),
                net.minecraft.util.Mth.lerp(partialTick, this.entity.yo, this.entity.getY()) + eyeHeight,
                net.minecraft.util.Mth.lerp(partialTick, this.entity.zo, this.entity.getZ())
        );
        Vec3 previousEye = new Vec3(this.entity.xo, this.entity.yo + eyeHeight, this.entity.zo);
        Vec3 transformedEye = destinationPortal.position().add(PortalGunTransformUtil.transformPosition(sourcePortal, destinationPortal, interpolatedEye.subtract(sourcePortal.position())));
        Vec3 transformedPreviousEye = destinationPortal.position().add(PortalGunTransformUtil.transformPosition(sourcePortal, destinationPortal, previousEye.subtract(sourcePortal.position())));
        Vec3 transformed = transformedEye.subtract(0.0D, eyeHeight, 0.0D);
        Vec3 transformedPrevious = transformedPreviousEye.subtract(0.0D, eyeHeight, 0.0D);
        Vec3 look = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, this.entity.getLookAngle()).normalize();
        Vec3 previousLook = PortalGunTransformUtil.transformVector(
                sourcePortal,
                destinationPortal,
                Vec3.directionFromRotation(this.entity.xRotO, this.entity.yRotO)
        ).normalize();
        Vec3 velocity = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, this.entity.getDeltaMovement());
        float yaw = PortalGunTransformUtil.yawFromLook(look);
        float pitch = PortalGunTransformUtil.pitchFromLook(look);
        float previousYaw = PortalGunTransformUtil.yawFromLook(previousLook);
        float previousPitch = PortalGunTransformUtil.pitchFromLook(previousLook);
        float yawDelta = net.minecraft.util.Mth.wrapDegrees(yaw - this.entity.getYRot());
        this.entity.setPos(transformed.x, transformed.y, transformed.z);
        this.entity.xo = transformedPrevious.x;
        this.entity.yo = transformedPrevious.y;
        this.entity.zo = transformedPrevious.z;
        this.entity.setYRot(yaw);
        this.entity.setXRot(pitch);
        this.entity.yRotO = previousYaw;
        this.entity.xRotO = previousPitch;
        this.entity.setDeltaMovement(velocity);
        if (this.entity instanceof LivingEntity living) {
            living.setYHeadRot(living.getYHeadRot() + yawDelta);
            living.setYBodyRot(living.yBodyRot + yawDelta);
            living.yHeadRotO += yawDelta;
            living.yBodyRotO += yawDelta;
        }
        return transformed;
    }

    public void reset() {
        while (!this.stack.isEmpty()) {
            this.pop();
        }
    }

    private record EntityTransformation(
            double x,
            double y,
            double z,
            double xo,
            double yo,
            double zo,
            float yRot,
            float xRot,
            float yRotO,
            float xRotO,
            float yHeadRot,
            float yHeadRotO,
            float yBodyRot,
            float yBodyRotO,
            Vec3 deltaMovement
    ) {
        private static EntityTransformation capture(Entity entity) {
            float yHeadRot = entity instanceof LivingEntity living ? living.getYHeadRot() : entity.getYRot();
            float yHeadRotO = entity instanceof LivingEntity living ? living.yHeadRotO : entity.yRotO;
            float yBodyRot = entity instanceof LivingEntity living ? living.yBodyRot : entity.getYRot();
            float yBodyRotO = entity instanceof LivingEntity living ? living.yBodyRotO : entity.yRotO;
            return new EntityTransformation(entity.getX(), entity.getY(), entity.getZ(), entity.xo, entity.yo, entity.zo, entity.getYRot(), entity.getXRot(), entity.yRotO, entity.xRotO, yHeadRot, yHeadRotO, yBodyRot, yBodyRotO, entity.getDeltaMovement());
        }

        private void restore(Entity entity) {
            entity.setPos(this.x, this.y, this.z);
            entity.xo = this.xo;
            entity.yo = this.yo;
            entity.zo = this.zo;
            entity.setYRot(this.yRot);
            entity.setXRot(this.xRot);
            entity.yRotO = this.yRotO;
            entity.xRotO = this.xRotO;
            entity.setDeltaMovement(this.deltaMovement);
            if (entity instanceof LivingEntity living) {
                living.setYHeadRot(this.yHeadRot);
                living.yHeadRotO = this.yHeadRotO;
                living.setYBodyRot(this.yBodyRot);
                living.yBodyRotO = this.yBodyRotO;
            }
        }
    }
}
