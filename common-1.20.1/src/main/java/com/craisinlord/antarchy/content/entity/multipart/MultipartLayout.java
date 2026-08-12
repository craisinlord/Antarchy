package com.craisinlord.antarchy.content.entity.multipart;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface MultipartLayout {
    MultipartPartDefinition[] parts();

    default MultipartPartDefinition part(int index) {
        return this.parts()[index];
    }

    default Vec3 getPartPosition(Entity parent, MultipartPartDefinition spec) {
        double yawRadians = Math.toRadians(parent.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);

        return new Vec3(
                parent.getX() + forward.x * spec.forwardOffset() + right.x * spec.lateralOffset(),
                parent.getY() + spec.yOffset(),
                parent.getZ() + forward.z * spec.forwardOffset() + right.z * spec.lateralOffset()
        );
    }
}
