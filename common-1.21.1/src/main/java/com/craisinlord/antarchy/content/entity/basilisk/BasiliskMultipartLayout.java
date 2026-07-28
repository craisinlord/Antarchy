package com.craisinlord.antarchy.content.entity.basilisk;

import com.craisinlord.antarchy.content.entity.multipart.MultipartLayout;
import com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class BasiliskMultipartLayout implements MultipartLayout {
    public static final BasiliskMultipartLayout INSTANCE = new BasiliskMultipartLayout();

    public static final MultipartPartDefinition HEAD = new MultipartPartDefinition(
            "head",
            1.58F,
            1.50F,
            1.0F,
            3.15D,
            1.62D,
            0.00D,
            false
    );

    public static final MultipartPartDefinition NECK = new MultipartPartDefinition(
            "neck",
            1.43F,
            1.35F,
            1.15F,
            2.15D,
            1.25D,
            0.06D,
            false
    );

    public static final MultipartPartDefinition BODY_1 = new MultipartPartDefinition(
            "body_1",
            1.43F,
            1.20F,
            1.0F,
            0.95D,
            0.82D,
            -0.03D,
            false
    );

    public static final MultipartPartDefinition BODY_2 = new MultipartPartDefinition(
            "body_2",
            1.35F,
            1.13F,
            1.0F,
            -0.10D,
            0.70D,
            0.06D,
            false
    );

    public static final MultipartPartDefinition BODY_3 = new MultipartPartDefinition(
            "body_3",
            1.28F,
            1.09F,
            1.0F,
            -1.35D,
            0.56D,
            -0.05D,
            false
    );

    public static final MultipartPartDefinition BODY_4 = new MultipartPartDefinition(
            "body_4",
            1.20F,
            1.01F,
            1.0F,
            -2.55D,
            0.44D,
            0.04D,
            false
    );

    public static final MultipartPartDefinition TAIL = new MultipartPartDefinition(
            "tail",
            1.05F,
            0.86F,
            1.0F,
            -4.15D,
            0.34D,
            0.00D,
            false
    );

    private static final MultipartPartDefinition[] PARTS = {
            HEAD,
            NECK,
            BODY_1,
            BODY_2,
            BODY_3,
            BODY_4,
            TAIL
    };

    private static final double[] IDLE_ARC_ANGLE_DEG = { 20.0D, 50.0D, 90.0D, 130.0D, 170.0D, 220.0D, 260.0D };
    private static final double[] IDLE_ARC_RADIUS    = { 2.6D,  2.2D,  1.8D,  1.5D,   1.3D,   1.2D,   1.1D  };
    private static final double[] IDLE_Y_OFFSET      = { 1.40D, 1.10D, 0.70D, 0.50D,  0.40D,  0.30D,  0.20D };

    private BasiliskMultipartLayout() {
    }

    @Override
    public MultipartPartDefinition[] parts() {
        return PARTS;
    }

    @Override
    public Vec3 getPartPosition(Entity parent, MultipartPartDefinition spec) {
        boolean idle = parent.getDeltaMovement().horizontalDistanceSqr() < 0.0025D;
        if (!idle) {
            return MultipartLayout.super.getPartPosition(parent, spec);
        }

        int index = partIndex(spec.name());
        if (index < 0) {
            return MultipartLayout.super.getPartPosition(parent, spec);
        }

        double yawRad = Math.toRadians(parent.getYRot());
        double arcAngleRad = Math.toRadians(IDLE_ARC_ANGLE_DEG[index]);

        double fwdX = -Math.sin(yawRad);
        double fwdZ =  Math.cos(yawRad);
        double rightX =  fwdZ;
        double rightZ = -fwdX;

        double cosA = Math.cos(arcAngleRad);
        double sinA = Math.sin(arcAngleRad);
        double r = IDLE_ARC_RADIUS[index];

        double offsetX = (fwdX * cosA + rightX * sinA) * r;
        double offsetZ = (fwdZ * cosA + rightZ * sinA) * r;

        return new Vec3(
                parent.getX() + offsetX,
                parent.getY() + IDLE_Y_OFFSET[index],
                parent.getZ() + offsetZ
        );
    }

    private static int partIndex(String name) {
        return switch (name) {
            case "head"   -> 0;
            case "neck"   -> 1;
            case "body_1" -> 2;
            case "body_2" -> 3;
            case "body_3" -> 4;
            case "body_4" -> 5;
            case "tail"   -> 6;
            default       -> -1;
        };
    }
}
