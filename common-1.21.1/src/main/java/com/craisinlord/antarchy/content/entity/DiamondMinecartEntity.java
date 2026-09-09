package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class DiamondMinecartEntity extends Minecart {
    private static final long INPUT_TIMEOUT_TICKS = 5L;
    private static final long COLLISION_COOLDOWN_TICKS = 10L;
    private static final EntityDataAccessor<Float> SYNCED_SPEED =
            SynchedEntityData.defineId(DiamondMinecartEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SYNCED_FACING =
            SynchedEntityData.defineId(DiamondMinecartEntity.class, EntityDataSerializers.INT);

    private byte inputFlags = 0;
    private long lastInputGameTime = Long.MIN_VALUE;
    private float currentSpeed = 0.0F;
    private Direction facingDir = Direction.SOUTH;
    private BlockPos lastBlockPos = null;
    private boolean railWarningCooldown = false;
    private final Map<Integer, Long> collisionCooldowns = new HashMap<>();

    private final Item dropItem;

    private record RailPlan(RailShape currentShape, BlockPos nextPos, RailShape nextShape) {
    }

    public DiamondMinecartEntity(EntityType<? extends DiamondMinecartEntity> entityType, Level level, Item dropItem) {
        super(entityType, level);
        this.dropItem = dropItem;
    }

    public DiamondMinecartEntity(EntityType<? extends DiamondMinecartEntity> entityType, Level level,
                                 double x, double y, double z, Item dropItem) {
        super(entityType, level);
        this.setPos(x, y, z);
        this.dropItem = dropItem;
    }

    @Override
    protected Item getDropItem() {
        return this.dropItem;
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.RIDEABLE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SYNCED_SPEED, 0.0F);
        builder.define(SYNCED_FACING, 2);
    }

    public float getSyncedSpeed() {
        return this.entityData.get(SYNCED_SPEED);
    }

    public Direction getSyncedFacing() {
        int idx = this.entityData.get(SYNCED_FACING);
        return switch (idx) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.EAST;
            case 3 -> Direction.WEST;
            default -> Direction.SOUTH;
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("DiamondFacing", this.facingDir.getName());
        tag.putFloat("DiamondSpeed", this.currentSpeed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DiamondFacing")) {
            Direction direction = Direction.byName(tag.getString("DiamondFacing"));
            if (direction != null && direction.getAxis() != Direction.Axis.Y) {
                this.facingDir = direction;
            }
        }
        this.currentSpeed = tag.getFloat("DiamondSpeed");
    }

    @Override
    protected double getMaxSpeed() {
        return AntarchySettings.diamondMinecartEnabled()
                ? AntarchySettings.diamondMinecartMaxSpeed()
                : 0.4D;
    }

    public void onInputReceived(byte flags) {
        this.inputFlags = flags;
        this.lastInputGameTime = this.level().getGameTime();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.serverTick();
        }
    }

    private void serverTick() {
        if (!AntarchySettings.diamondMinecartEnabled()) {
            this.inputFlags = 0;
            this.lastInputGameTime = Long.MIN_VALUE;
            this.currentSpeed = 0.0F;
            this.collisionCooldowns.clear();
            this.entityData.set(SYNCED_SPEED, 0.0F);
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            return;
        }

        if (this.level().getGameTime() - this.lastInputGameTime > INPUT_TIMEOUT_TICKS) {
            this.inputFlags = 0;
        }

        Player rider = this.getRidingPlayer();
        boolean hasRider = rider != null;

        if (!hasRider) {
            this.inputFlags = 0;
            this.lastInputGameTime = Long.MIN_VALUE;
        }

        if (this.lastBlockPos == null) {
            if (rider != null) {
                this.facingDir = getLookFacing(rider, Direction.SOUTH);
            }
            this.lastBlockPos = this.blockPosition();
        }

        if (hasRider) {
            float cruiseSpeed = (float) AntarchySettings.diamondMinecartCruiseSpeed();
            float maxSpeed = (float) AntarchySettings.diamondMinecartMaxSpeed();
            float accel = (float) AntarchySettings.diamondMinecartAcceleration();
            float decel = (float) AntarchySettings.diamondMinecartDeceleration();
            float coast = (float) AntarchySettings.diamondMinecartCoastDeceleration();

            cruiseSpeed = Math.max(0.0F, Math.min(cruiseSpeed, maxSpeed));

            boolean forward = (this.inputFlags & 0x01) != 0;
            boolean back = (this.inputFlags & 0x02) != 0;

            float targetSpeed = forward ? maxSpeed : cruiseSpeed;
            if (back && !forward) {
                targetSpeed = Math.max(0.0F, cruiseSpeed - decel);
            }

            if (this.currentSpeed < targetSpeed) {
                this.currentSpeed = Math.min(this.currentSpeed + accel, targetSpeed);
            } else if (this.currentSpeed > targetSpeed) {
                this.currentSpeed = Math.max(this.currentSpeed - coast, targetSpeed);
            }
        } else {
            float coast = (float) AntarchySettings.diamondMinecartCoastDeceleration();
            this.currentSpeed = Math.max(this.currentSpeed - coast, 0.0F);
            this.railWarningCooldown = false;
        }

        this.entityData.set(SYNCED_SPEED, this.currentSpeed);
        this.entityData.set(SYNCED_FACING, facingDirectionIndex(this.facingDir));

        BlockPos currentPos = this.blockPosition();
        if (!currentPos.equals(this.lastBlockPos)) {
            this.onEnteredNewBlock(currentPos, rider);
            this.lastBlockPos = currentPos;
        }

        if (AntarchySettings.diamondMinecartPlacesRails() && hasRider) {
            this.ensureRailAt(currentPos, this.level().getBlockState(currentPos), getStockShape(this.facingDir), false);
        }

        if (AntarchySettings.diamondMinecartMobDamageEnabled() && this.currentSpeed > 0.15F && hasRider) {
            this.applyMobCollisionDamage();
        }

        this.applyControlledVelocity();
    }

    private void applyControlledVelocity() {
        double speed = this.currentSpeed;
        this.setDeltaMovement(
                this.facingDir.getStepX() * speed,
                this.getDeltaMovement().y,
                this.facingDir.getStepZ() * speed
        );
    }

    private void onEnteredNewBlock(BlockPos currentPos, @Nullable Player rider) {
        Direction previousFacing = this.facingDir;
        if (rider != null) {
            Direction steeredFacing = getSteeredFacing(rider, this.facingDir);
            if (steeredFacing != this.facingDir.getOpposite()) {
                this.facingDir = steeredFacing;
            }
        }

        boolean turned = this.facingDir != previousFacing;
        if (!AntarchySettings.diamondMinecartPlacesRails() || rider == null) {
            return;
        }

        RailPlan railPlan = this.buildRailPlan(currentPos, previousFacing, turned);
        if (railPlan == null) {
            return;
        }

        this.ensureRailAt(currentPos, this.level().getBlockState(currentPos), railPlan.currentShape(), false);
        this.placeRailAt(railPlan.nextPos(), railPlan.nextShape(), rider);
        this.refreshRailConnections(currentPos);
        this.refreshRailConnections(railPlan.nextPos());
    }

    @Nullable
    private RailPlan buildRailPlan(BlockPos currentPos, Direction previousFacing, boolean turned) {
        BlockPos flatAhead = currentPos.relative(this.facingDir);
        if (canPlaceRailAt(flatAhead)) {
            RailShape currentShape = turned ? getTurnShape(previousFacing, this.facingDir) : getStockShape(this.facingDir);
            return new RailPlan(currentShape, flatAhead, getStockShape(this.facingDir));
        }

        if (!turned) {
            BlockPos upAhead = flatAhead.above();
            if (canPlaceRailAt(upAhead)) {
                return new RailPlan(getAscendingShape(this.facingDir), upAhead, getStockShape(this.facingDir));
            }

            BlockPos downAhead = flatAhead.below();
            if (canPlaceRailAt(downAhead)) {
                return new RailPlan(getDescendingEntryShape(this.facingDir), downAhead, getStockShape(this.facingDir));
            }
        }

        return null;
    }

    private void placeRailAt(BlockPos pos, RailShape shape, Player rider) {
        BlockState existing = this.level().getBlockState(pos);
        if (BaseRailBlock.isRail(existing)) {
            this.setRailShapeAt(pos, shape);
            return;
        }

        if (!canPlaceRailAt(pos, existing)) {
            return;
        }

        if (!consumeRailFromInventory(rider)) {
            if (!this.railWarningCooldown) {
                rider.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("The Diamond Minecart is out of rails!"),
                        true
                );
                this.railWarningCooldown = true;
                this.currentSpeed = 0.0F;
            }
            return;
        }

        this.railWarningCooldown = false;
        this.level().setBlock(pos, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, shape), 3);
        this.refreshRailConnections(pos);
    }

    private void ensureRailAt(BlockPos pos, BlockState existing, RailShape shape, boolean consume) {
        if (BaseRailBlock.isRail(existing)) {
            this.setRailShapeAt(pos, shape);
            return;
        }

        if (!canPlaceRailAt(pos, existing)) {
            return;
        }

        Player rider = this.getRidingPlayer();
        if (consume && rider != null && !consumeRailFromInventory(rider)) {
            return;
        }

        this.level().setBlock(pos, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, shape), 3);
        this.refreshRailConnections(pos);
    }

    private void setRailShapeAt(BlockPos pos, RailShape shape) {
        BlockState state = this.level().getBlockState(pos);
        if (state.getBlock() instanceof RailBlock) {
            this.level().setBlock(pos, state.setValue(RailBlock.SHAPE, shape), 3);
            this.refreshRailConnections(pos);
        }
    }

    private void refreshRailConnections(BlockPos pos) {
        this.level().updateNeighborsAt(pos, Blocks.RAIL);
        this.level().updateNeighborsAt(pos.above(), Blocks.RAIL);
        this.level().updateNeighborsAt(pos.below(), Blocks.RAIL);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            this.level().updateNeighborsAt(pos.relative(direction), Blocks.RAIL);
            this.level().updateNeighborsAt(pos.relative(direction).above(), Blocks.RAIL);
            this.level().updateNeighborsAt(pos.relative(direction).below(), Blocks.RAIL);
        }
    }

    private boolean consumeRailFromInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.RAIL)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    public static RailShape getStockShape(Direction facing) {
        return (facing == Direction.NORTH || facing == Direction.SOUTH)
                ? RailShape.NORTH_SOUTH
                : RailShape.EAST_WEST;
    }

    public static RailShape getTurnShape(Direction oldFacing, Direction newFacing) {
        Direction entry = oldFacing.getOpposite();
        Direction exit = newFacing;

        boolean hasNorth = entry == Direction.NORTH || exit == Direction.NORTH;
        boolean hasEast = entry == Direction.EAST || exit == Direction.EAST;
        boolean hasSouth = entry == Direction.SOUTH || exit == Direction.SOUTH;
        boolean hasWest = entry == Direction.WEST || exit == Direction.WEST;

        if (hasNorth && hasEast) return RailShape.NORTH_EAST;
        if (hasNorth && hasWest) return RailShape.NORTH_WEST;
        if (hasSouth && hasEast) return RailShape.SOUTH_EAST;
        if (hasSouth && hasWest) return RailShape.SOUTH_WEST;
        return getStockShape(newFacing);
    }

    private static RailShape getAscendingShape(Direction facing) {
        return switch (facing) {
            case NORTH -> RailShape.ASCENDING_NORTH;
            case SOUTH -> RailShape.ASCENDING_SOUTH;
            case EAST -> RailShape.ASCENDING_EAST;
            case WEST -> RailShape.ASCENDING_WEST;
            default -> RailShape.NORTH_SOUTH;
        };
    }

    private static RailShape getDescendingEntryShape(Direction facing) {
        return getAscendingShape(facing.getOpposite());
    }

    private static boolean isWithinRailBuildHeight(BlockPos pos) {
        return pos.getY() >= -64 && pos.getY() < 320;
    }

    private boolean canPlaceRailAt(BlockPos pos) {
        return canPlaceRailAt(pos, this.level().getBlockState(pos));
    }

    private boolean canPlaceRailAt(BlockPos pos, BlockState existing) {
        if (!isWithinRailBuildHeight(pos)
                || !this.level().hasChunkAt(pos)
                || !this.level().getWorldBorder().isWithinBounds(pos)
                || !this.level().getBlockState(pos.below()).isSolid()) {
            return false;
        }
        return BaseRailBlock.isRail(existing) || existing.isAir() || existing.canBeReplaced();
    }

    private void applyMobCollisionDamage() {
        double maxDamage = AntarchySettings.diamondMinecartMaxMobDamage();
        double maxSpeed = AntarchySettings.diamondMinecartMaxSpeed();
        float damage = maxSpeed <= 0.0D ? 0.0F : (float) (maxDamage * (this.currentSpeed / maxSpeed));
        if (damage <= 0.0F) {
            return;
        }

        long gameTime = this.level().getGameTime();
        this.collisionCooldowns.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
        Vec3 horizontalVelocity = new Vec3(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z);
        double velocityLength = horizontalVelocity.length();
        if (velocityLength < 1.0E-4D) {
            return;
        }
        Vec3 movementDirection = horizontalVelocity.scale(1.0D / velocityLength);

        DamageSource source = this.level().damageSources().generic();
        AABB hitBox = this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, hitBox,
                entity -> !(entity instanceof Player player && this.hasPassenger(player))
                        && !entity.isInvulnerable());

        for (LivingEntity mob : nearby) {
            if (this.collisionCooldowns.containsKey(mob.getId())) {
                continue;
            }

            double dx = mob.getX() - this.getX();
            double dz = mob.getZ() - this.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 1.0E-4D) {
                Vec3 toMob = new Vec3(dx / distance, 0.0D, dz / distance);
                if (movementDirection.dot(toMob) < -0.2D) {
                    continue;
                }
            }

            if (!mob.hurt(source, damage)) {
                continue;
            }
            this.collisionCooldowns.put(mob.getId(), gameTime + COLLISION_COOLDOWN_TICKS);
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.001D) {
                mob.setDeltaMovement(mob.getDeltaMovement().add(
                        dx / len * 0.5D,
                        0.3D,
                        dz / len * 0.5D
                ));
            }
        }
    }

    @Nullable
    private Player getRidingPlayer() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    private static Direction getLookFacing(Player rider, Direction fallback) {
        Direction direction = Direction.fromYRot(rider.getYRot());
        return direction.getAxis() == Direction.Axis.Y ? fallback : direction;
    }

    private Direction getSteeredFacing(Player rider, Direction fallback) {
        boolean left = (this.inputFlags & 0x04) != 0;
        boolean right = (this.inputFlags & 0x08) != 0;
        if (left != right) {
            return left ? fallback.getCounterClockWise() : fallback.getClockWise();
        }
        if ((this.inputFlags & 0x01) != 0) {
            return getLookFacing(rider, fallback);
        }
        return fallback;
    }

    private static int facingDirectionIndex(Direction dir) {
        return switch (dir) {
            case NORTH -> 0;
            case EAST -> 1;
            case WEST -> 3;
            default -> 2;
        };
    }
}
