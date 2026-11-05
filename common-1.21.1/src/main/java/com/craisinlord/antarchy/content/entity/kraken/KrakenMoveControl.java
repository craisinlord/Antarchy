package com.craisinlord.antarchy.content.entity.kraken;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

final class KrakenMoveControl extends MoveControl {
    private static final double SPEED_SCALE = 1.55D;
    private static final double MAX_ACCELERATION = 0.06D;
    private static final double ARRIVAL_GAIN = 0.08D;

    KrakenMoveControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        this.mob.setSpeed(0.0F);
        this.mob.setXxa(0.0F);
        this.mob.setYya(0.0F);
        this.mob.setZza(0.0F);
        if (this.operation != Operation.MOVE_TO) {
            return;
        }
        this.operation = Operation.WAIT;

        Vec3 toWanted = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
        double distance = toWanted.length();
        double topSpeed = this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED) * SPEED_SCALE;
        double wantedSpeed = Math.min(topSpeed, distance * ARRIVAL_GAIN);
        Vec3 wantedVelocity = distance > 1.0E-3D ? toWanted.scale(wantedSpeed / distance) : Vec3.ZERO;

        Vec3 velocity = this.mob.getDeltaMovement();
        Vec3 steering = wantedVelocity.subtract(velocity);
        double steeringLength = steering.length();
        if (steeringLength > MAX_ACCELERATION) {
            steering = steering.scale(MAX_ACCELERATION / steeringLength);
        }
        this.mob.setDeltaMovement(velocity.add(steering));
    }
}
