package com.craisinlord.antarchy.content.minecart;

import com.craisinlord.antarchy.content.block.AbstractAntimetalRailBlock;
import com.craisinlord.antarchy.content.block.AntimetalActivatorRailBlock;
import com.craisinlord.antarchy.content.block.AntimetalPoweredRailBlock;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A direct mirror of {@code AbstractMinecart.moveAlongTrack}/{@code getPos}/{@code getPosOffs},
 * adapted for ceiling-mounted track. The world-Y meaning of "ascending"/"descending" does not
 * need to be sign-flipped anywhere here — lower world Y is lower world Y whether a rail rests on
 * a floor or hangs from a ceiling. What actually differs from vanilla is: the rail sits near the
 * top of its cell instead of near the bottom (see {@link AntimetalRailHelper#attachY}), support
 * is required above instead of below, and the "which cell owns this position" disambiguation
 * checks the cell above instead of the cell below. Everything else — the exit-projection math,
 * the slope acceleration constant, the player-push nudge, using real collision-checked
 * {@code Entity.move} rather than a hand-rolled collision test — is a straight port.
 */
public final class AntimetalMinecartPhysics {
    private static final double SLOPE_ACCEL = 0.0078125D;
    private static final double MAX_RAIL_SPEED = 0.4D;
    private static final double RAIL_ACCELERATION = 0.06D;
    private static final double HITBOX_DROP = 0.4625D;

    private AntimetalMinecartPhysics() {
    }

    @Nullable
    public static BlockPos findCell(Level level, double x, double y, double z) {
        int i = Mth.floor(x);
        int j = Mth.floor(y);
        int k = Mth.floor(z);
        BlockPos direct = new BlockPos(i, j, k);
        if (isAntimetalRailAt(level, direct)) {
            return direct;
        }
        BlockPos above = direct.above();
        return isAntimetalRailAt(level, above) ? above : null;
    }

    public static boolean isAntimetalRailAt(Level level, BlockPos pos) {
        return AbstractAntimetalRailBlock.isAntimetalRail(level.getBlockState(pos));
    }

    private static boolean isAntimetalRailAt(Level level, int x, int y, int z) {
        return isAntimetalRailAt(level, new BlockPos(x, y, z));
    }

    @Nullable
    private static Vec3 ascendingPosition(RailShape shape, BlockPos cell, double x, double z) {
        double baseY = AntimetalRailHelper.attachY(cell.getY(), 0);
        double centerX = cell.getX() + 0.5D;
        double centerZ = cell.getZ() + 0.5D;
        return switch (shape) {
            case ASCENDING_EAST -> {
                double t = Mth.clamp(cell.getX() + 1.0D - x, 0.0D, 1.0D);
                yield new Vec3(cell.getX() + 1.0D - t, baseY - t, centerZ);
            }
            case ASCENDING_WEST -> {
                double t = Mth.clamp(x - cell.getX(), 0.0D, 1.0D);
                yield new Vec3(cell.getX() + t, baseY - t, centerZ);
            }
            case ASCENDING_NORTH -> {
                double t = Mth.clamp(z - cell.getZ(), 0.0D, 1.0D);
                yield new Vec3(centerX, baseY - t, cell.getZ() + t);
            }
            case ASCENDING_SOUTH -> {
                double t = Mth.clamp(cell.getZ() + 1.0D - z, 0.0D, 1.0D);
                yield new Vec3(centerX, baseY - t, cell.getZ() + 1.0D - t);
            }
            default -> null;
        };
    }

    public static void moveAlongTrack(AbstractMinecart cart, BlockPos railPos, BlockState railState) {
        cart.resetFallDistance();
        double x = cart.getX();
        double y = cart.getY();
        double z = cart.getZ();
        Vec3 before = getPos(cart, x, y, z);
        double targetY = AntimetalRailHelper.attachY(railPos.getY(), 0);

        boolean poweredOn = false;
        boolean unpoweredBraking = false;
        if (railState.getBlock() instanceof AntimetalPoweredRailBlock) {
            poweredOn = railState.getValue(AntimetalPoweredRailBlock.POWERED);
            unpoweredBraking = !poweredOn;
        }

        RailShape shape = railState.getValue(((AbstractAntimetalRailBlock) railState.getBlock()).getShapeProperty());
        Vec3 motion = cart.getDeltaMovement();
        switch (shape) {
            case ASCENDING_EAST -> cart.setDeltaMovement(motion.add(-SLOPE_ACCEL, 0.0D, 0.0D));
            case ASCENDING_WEST -> cart.setDeltaMovement(motion.add(SLOPE_ACCEL, 0.0D, 0.0D));
            case ASCENDING_NORTH -> cart.setDeltaMovement(motion.add(0.0D, 0.0D, SLOPE_ACCEL));
            case ASCENDING_SOUTH -> cart.setDeltaMovement(motion.add(0.0D, 0.0D, -SLOPE_ACCEL));
            default -> {
            }
        }

        motion = cart.getDeltaMovement();
        Pair<Vec3i, Vec3i> exits = AntimetalRailHelper.getExits(shape);
        Vec3i exitA = exits.getFirst();
        Vec3i exitB = exits.getSecond();
        double dx = exitB.getX() - exitA.getX();
        double dz = exitB.getZ() - exitA.getZ();
        double dirLength = Math.sqrt(dx * dx + dz * dz);
        double alignment = motion.x * dx + motion.z * dz;
        if (alignment < 0.0D) {
            dx = -dx;
            dz = -dz;
        }

        double speed = Math.min(2.0D, motion.horizontalDistance());
        motion = new Vec3(speed * dx / dirLength, motion.y, speed * dz / dirLength);
        cart.setDeltaMovement(motion);

        Entity firstPassenger = cart.getFirstPassenger();
        if (firstPassenger instanceof Player) {
            Vec3 riderMotion = firstPassenger.getDeltaMovement();
            double riderSpeedSq = riderMotion.horizontalDistanceSqr();
            double cartSpeedSq = cart.getDeltaMovement().horizontalDistanceSqr();
            if (riderSpeedSq > 1.0E-4D && cartSpeedSq < 0.01D) {
                cart.setDeltaMovement(cart.getDeltaMovement().add(riderMotion.x * 0.1D, 0.0D, riderMotion.z * 0.1D));
                unpoweredBraking = false;
            }
        }

        if (unpoweredBraking) {
            double brakingSpeed = cart.getDeltaMovement().horizontalDistance();
            if (brakingSpeed < 0.03D) {
                cart.setDeltaMovement(Vec3.ZERO);
            } else {
                cart.setDeltaMovement(cart.getDeltaMovement().multiply(0.5D, 0.0D, 0.5D));
            }
        }

        Vec3 ascendingPos = shape.isAscending() ? ascendingPosition(shape, railPos, x, z) : null;
        if (ascendingPos != null) {
            x = ascendingPos.x;
            targetY = ascendingPos.y;
            z = ascendingPos.z;
        } else {
            double startX = railPos.getX() + 0.5D + exitA.getX() * 0.5D;
            double startZ = railPos.getZ() + 0.5D + exitA.getZ() * 0.5D;
            double endX = railPos.getX() + 0.5D + exitB.getX() * 0.5D;
            double endZ = railPos.getZ() + 0.5D + exitB.getZ() * 0.5D;
            dx = endX - startX;
            dz = endZ - startZ;
            double s;
            if (dx == 0.0D) {
                s = z - railPos.getZ();
            } else if (dz == 0.0D) {
                s = x - railPos.getX();
            } else {
                double px = x - startX;
                double pz = z - startZ;
                s = (px * dx + pz * dz) * 2.0D;
            }
            x = startX + dx * s;
            z = startZ + dz * s;
        }
        cart.setPos(x, targetY, z);
        cart.setBoundingBox(cart.getBoundingBox().move(0.0D, -HITBOX_DROP, 0.0D));

        double passengerFactor = cart.isVehicle() ? 0.75D : 1.0D;
        motion = cart.getDeltaMovement();
        Vec3 clampedMotion = new Vec3(
                Mth.clamp(passengerFactor * motion.x, -MAX_RAIL_SPEED, MAX_RAIL_SPEED),
                0.0D,
                Mth.clamp(passengerFactor * motion.z, -MAX_RAIL_SPEED, MAX_RAIL_SPEED)
        );
        if (shape.isAscending()) {
            cart.setPos(cart.getX() + clampedMotion.x, cart.getY(), cart.getZ() + clampedMotion.z);
        } else {
            cart.move(MoverType.SELF, clampedMotion);
        }

        if (exitA.getY() != 0 && Mth.floor(cart.getX()) - railPos.getX() == exitA.getX() && Mth.floor(cart.getZ()) - railPos.getZ() == exitA.getZ()) {
            cart.setPos(cart.getX(), cart.getY() + exitA.getY(), cart.getZ());
        } else if (exitB.getY() != 0 && Mth.floor(cart.getX()) - railPos.getX() == exitB.getX() && Mth.floor(cart.getZ()) - railPos.getZ() == exitB.getZ()) {
            cart.setPos(cart.getX(), cart.getY() + exitB.getY(), cart.getZ());
        }

        applyNaturalSlowdown(cart);

        Vec3 after = getPos(cart, cart.getX(), cart.getY(), cart.getZ());
        if (after != null && before != null) {
            double slopeDelta = (before.y - after.y) * 0.05D;
            Vec3 currentMotion = cart.getDeltaMovement();
            double horizontalSpeed = currentMotion.horizontalDistance();
            if (horizontalSpeed > 0.0D) {
                cart.setDeltaMovement(currentMotion.multiply((horizontalSpeed + slopeDelta) / horizontalSpeed, 1.0D, (horizontalSpeed + slopeDelta) / horizontalSpeed));
            }
            cart.setPos(cart.getX(), after.y, cart.getZ());
        }

        int cellX = Mth.floor(cart.getX());
        int cellZ = Mth.floor(cart.getZ());
        if (cellX != railPos.getX() || cellZ != railPos.getZ()) {
            Vec3 currentMotion = cart.getDeltaMovement();
            double horizontalSpeed = currentMotion.horizontalDistance();
            cart.setDeltaMovement(horizontalSpeed * (cellX - railPos.getX()), currentMotion.y, horizontalSpeed * (cellZ - railPos.getZ()));
        }

        cart.setBoundingBox(cart.getBoundingBox().move(0.0D, -HITBOX_DROP, 0.0D));

        if (poweredOn) {
            Vec3 currentMotion = cart.getDeltaMovement();
            double horizontalSpeed = currentMotion.horizontalDistance();
            if (horizontalSpeed > 0.01D) {
                cart.setDeltaMovement(currentMotion.add(currentMotion.x / horizontalSpeed * RAIL_ACCELERATION, 0.0D, currentMotion.z / horizontalSpeed * RAIL_ACCELERATION));
            } else if (shape == RailShape.EAST_WEST) {
                Vec3 currentMotion2 = cart.getDeltaMovement();
                double nudgeX = currentMotion2.x;
                if (isRedstoneConductor(cart.level(), railPos.west())) {
                    nudgeX = 0.02D;
                } else if (isRedstoneConductor(cart.level(), railPos.east())) {
                    nudgeX = -0.02D;
                }
                cart.setDeltaMovement(nudgeX, currentMotion2.y, currentMotion2.z);
            } else if (shape == RailShape.NORTH_SOUTH) {
                Vec3 currentMotion2 = cart.getDeltaMovement();
                double nudgeZ = currentMotion2.z;
                if (isRedstoneConductor(cart.level(), railPos.north())) {
                    nudgeZ = 0.02D;
                } else if (isRedstoneConductor(cart.level(), railPos.south())) {
                    nudgeZ = -0.02D;
                }
                cart.setDeltaMovement(currentMotion2.x, currentMotion2.y, nudgeZ);
            }
        }

        if (railState.getBlock() instanceof AntimetalActivatorRailBlock && railState.getValue(AntimetalActivatorRailBlock.POWERED)) {
            cart.activateMinecart(railPos.getX(), railPos.getY(), railPos.getZ(), true);
        }
    }

    private static boolean isRedstoneConductor(Level level, BlockPos pos) {
        return level.getBlockState(pos).isRedstoneConductor(level, pos);
    }

    private static void applyNaturalSlowdown(AbstractMinecart cart) {
        double drag = cart.isVehicle() ? 0.997D : 0.96D;
        Vec3 motion = cart.getDeltaMovement();
        cart.setDeltaMovement(motion.multiply(drag, 0.0D, drag));
    }

    @Nullable
    public static Vec3 getPosOffs(AbstractMinecart cart, double x, double y, double z, double offset) {
        BlockPos cell = findCell(cart.level(), x, y, z);
        if (cell == null) {
            return null;
        }
        BlockState state = cart.level().getBlockState(cell);
        RailShape shape = state.getValue(((AbstractAntimetalRailBlock) state.getBlock()).getShapeProperty());
        double targetY = AntimetalRailHelper.attachY(cell.getY(), 0);

        Pair<Vec3i, Vec3i> exits = AntimetalRailHelper.getExits(shape);
        Vec3i exitA = exits.getFirst();
        Vec3i exitB = exits.getSecond();
        double dx = exitB.getX() - exitA.getX();
        double dz = exitB.getZ() - exitA.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        dx /= length;
        dz /= length;
        x += dx * offset;
        z += dz * offset;
        if (exitA.getY() != 0 && Mth.floor(x) - cell.getX() == exitA.getX() && Mth.floor(z) - cell.getZ() == exitA.getZ()) {
            targetY += exitA.getY();
        } else if (exitB.getY() != 0 && Mth.floor(x) - cell.getX() == exitB.getX() && Mth.floor(z) - cell.getZ() == exitB.getZ()) {
            targetY += exitB.getY();
        }
        Vec3 result = getPos(cart, x, targetY, z);
        if (result != null && (shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH)) {
            double baseY = AntimetalRailHelper.attachY(cell.getY(), 0);
            double mirroredY = 2.0D * (baseY - 0.5D) - result.y;
            result = new Vec3(result.x, mirroredY, result.z);
        }
        return result;
    }

    @Nullable
    public static Vec3 getPos(AbstractMinecart cart, double x, double y, double z) {
        BlockPos cell = findCell(cart.level(), x, y, z);
        if (cell == null) {
            return null;
        }
        BlockState state = cart.level().getBlockState(cell);
        RailShape shape = state.getValue(((AbstractAntimetalRailBlock) state.getBlock()).getShapeProperty());

        Vec3 ascendingPos = shape.isAscending() ? ascendingPosition(shape, cell, x, z) : null;
        if (ascendingPos != null) {
            return ascendingPos;
        }

        Pair<Vec3i, Vec3i> exits = AntimetalRailHelper.getExits(shape);
        Vec3i exitA = exits.getFirst();
        Vec3i exitB = exits.getSecond();

        double startX = cell.getX() + 0.5D + exitA.getX() * 0.5D;
        double startY = AntimetalRailHelper.attachY(cell.getY(), 0) + exitA.getY() * 0.5D;
        double startZ = cell.getZ() + 0.5D + exitA.getZ() * 0.5D;
        double endX = cell.getX() + 0.5D + exitB.getX() * 0.5D;
        double endY = AntimetalRailHelper.attachY(cell.getY(), 0) + exitB.getY() * 0.5D;
        double endZ = cell.getZ() + 0.5D + exitB.getZ() * 0.5D;

        double dx = endX - startX;
        double dy = (endY - startY) * 2.0D;
        double dz = endZ - startZ;
        double s;
        if (dx == 0.0D) {
            s = z - cell.getZ();
        } else if (dz == 0.0D) {
            s = x - cell.getX();
        } else {
            double px = x - startX;
            double pz = z - startZ;
            s = (px * dx + pz * dz) * 2.0D;
        }

        x = startX + dx * s;
        y = startY + dy * s;
        z = startZ + dz * s;
        return new Vec3(x, y, z);
    }
}
