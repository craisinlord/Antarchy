package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.anthology.portal.PortalAppearance;
import com.craisinlord.anthology.portal.PortalEntity;
import com.craisinlord.anthology.portal.PortalShape;
import com.craisinlord.anthology.portal.PortalSounds;
import com.craisinlord.anthology.portal.PortalTransitMath;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalBaseBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalCellAccess;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalFaceRecord;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalMasterBlockEntity;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.craisinlord.antarchy.content.entity.BlockSuction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PortalGunPortalEntity extends PortalEntity {
    public enum PortalSide {
        BLUE,
        ORANGE
    }

    private static final EntityDataAccessor<Integer> SIDE = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> OWNER_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> GUN_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> MOON = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final Identifier BLUE_RIM_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_blue.png");
    private static final Identifier ORANGE_RIM_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_orange.png");
    private static final PortalSounds SOUNDS = new PortalSounds(
            () -> sound("portal_enter"),
            () -> sound("portal_exit"),
            () -> sound("portal_ambient"),
            80,
            0.18F
    );
    private static final double MOON_RANGE = 14.0D;
    private static final double MOON_RAY_MIN_OUTWARD = -0.2D;
    private static final double MOON_PULL_STRENGTH = 0.15D;
    private static final double MOON_MAX_SPEED = 1.8D;
    private static final double MOON_MOUTH_DEPTH = 0.9D;
    private static final double MOON_HOLD_DEPTH = 0.65D;
    private static final int MOON_BLOCK_INTERVAL = 1;
    private static final int MOON_BLOCKS_PER_PULL = 4;
    private static final int MOON_BLOCK_RAYS = 16;
    private static final double MOON_RAY_STEP = 0.4D;
    private static final double MOON_BLOCK_SPEED = 0.6D;
    private static final int MOON_VACUUM_INTERVAL = 10;
    private static final float MOON_VACUUM_DAMAGE = 2.0F;
    private static final int MOON_WIND_INTERVAL = 40;
    private static final int MOON_BLOCK_MAX_FLIGHT_TICKS = 80;
    private static final double MOON_BLOCK_SWALLOW_RADIUS = 2.0D;
    private final PortalAppearance appearance = new GunAppearance();
    private UUID ownerId;
    private UUID gunId;
    private int moonTicks;
    private int moonSuckedBlocks;
    private final Map<UUID, Integer> moonLiftedBlocks = new HashMap<>();
    private BlockPos supportOrigin = BlockPos.ZERO;
    private BlockPos masterPos = BlockPos.ZERO;
    private BlockPos basePos = BlockPos.ZERO;
    private BlockPos[] portalSpots = new BlockPos[] {BlockPos.ZERO, BlockPos.ZERO};
    private Set<BlockPos> compensatedSpots = Set.of();

    public PortalGunPortalEntity(EntityType<? extends PortalGunPortalEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SIDE, PortalSide.BLUE.ordinal());
        builder.define(OWNER_ID, "");
        builder.define(GUN_ID, "");
        builder.define(VARIANT, PortalGunVariant.DEFAULT.id());
        builder.define(MOON, false);
    }

    private void setSyncedUuid(EntityDataAccessor<String> accessor, UUID value) {
        this.entityData.set(accessor, value == null ? "" : value.toString());
    }

    private Optional<UUID> getSyncedUuid(EntityDataAccessor<String> accessor) {
        String value = this.entityData.get(accessor);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public void configure(UUID ownerId, UUID gunId, PortalGunVariant variant, PortalSide side, PortalGunPlacement placement) {
        this.ownerId = ownerId;
        this.gunId = gunId;
        this.setSyncedUuid(OWNER_ID, ownerId);
        this.setSyncedUuid(GUN_ID, gunId);
        this.entityData.set(VARIANT, (variant == null ? PortalGunVariant.DEFAULT : variant).id());
        this.entityData.set(SIDE, side.ordinal());
        this.setGeometry(placement.facing(), placement.upAxis(), placement.width(), placement.height());
        this.supportOrigin = placement.supportOrigin().immutable();
        this.masterPos = placement.masterPos().immutable();
        this.basePos = placement.basePos().immutable();
        this.portalSpots = java.util.Arrays.stream(placement.portalSpots()).map(BlockPos::immutable).toArray(BlockPos[]::new);
        this.compensatedSpots = Set.copyOf(placement.compensatedSpots());
    }

    public void openToMoon() {
        this.clearLink();
        this.entityData.set(MOON, true);
        this.moonTicks = 0;
        this.moonSuckedBlocks = 0;
    }

    public boolean isMoonPortal() {
        return this.entityData.get(MOON);
    }

    public PortalSide getPortalSide() {
        int ordinal = this.entityData.get(SIDE);
        return ordinal == PortalSide.ORANGE.ordinal() ? PortalSide.ORANGE : PortalSide.BLUE;
    }

    public BlockPos getMasterPos() {
        return this.masterPos;
    }

    public UUID getOwnerId() {
        return this.getSyncedUuid(OWNER_ID).orElse(this.ownerId);
    }

    public UUID getGunId() {
        return this.getSyncedUuid(GUN_ID).orElse(this.gunId);
    }

    public PortalGunVariant getVariant() {
        return PortalGunVariant.byId(this.entityData.get(VARIANT));
    }

    public int getPortalColor() {
        return this.getVariant().color(this.getPortalSide());
    }

    public Identifier getRimTexture() {
        return this.getPortalSide() == PortalSide.BLUE ? BLUE_RIM_TEXTURE : ORANGE_RIM_TEXTURE;
    }

    public BlockPos getBasePos() {
        return this.basePos;
    }

    public BlockPos[] getPortalSpots() {
        return this.portalSpots.clone();
    }

    public boolean containsPortalSpot(BlockPos pos) {
        for (BlockPos portalSpot : this.portalSpots) {
            if (portalSpot.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    public Set<BlockPos> getCompensatedSpots() {
        return Set.copyOf(this.compensatedSpots);
    }

    @Override
    public PortalAppearance appearance() {
        return this.appearance;
    }

    @Override
    public boolean hasCustomView() {
        return this.isMoonPortal();
    }

    @Override
    protected PortalSounds sounds() {
        return SOUNDS;
    }

    @Override
    protected float ambientPitch() {
        return this.getPortalSide() == PortalSide.BLUE ? 1.05F : 0.92F;
    }

    @Override
    protected void clientTick() {
        if (this.isMoonPortal()) {
            this.spawnMoonParticles();
        }
    }

    @Override
    protected boolean isStillValid(ServerLevel level) {
        return this.gunId != null && this.isStillRegistered(level) && this.hasValidSurface();
    }

    @Override
    protected boolean tickBeforeTransit(ServerLevel level) {
        if (this.isMoonPortal()) {
            this.tickMoon(level);
            return true;
        }
        return false;
    }

    private void tickMoon(ServerLevel level) {
        this.moonTicks++;
        if (this.moonTicks >= AntarchySettings.portalGunMoonPortalSeconds() * 20) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), sound("portal_fizzle"), SoundSource.BLOCKS, 1.0F, 0.6F);
            this.discard();
            return;
        }
        if (this.moonTicks % MOON_WIND_INTERVAL == 1) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ELYTRA_FLYING, SoundSource.BLOCKS, 1.4F, 0.55F);
        }
        PortalShape shape = this.getWorldPortalShape();
        Player owner = this.getOwnerId() == null ? null : level.getPlayerByUUID(this.getOwnerId());
        this.pullMoonEntities(level, shape, owner);
        this.tickMoonLiftedBlocks(level, shape);
        if (AntarchySettings.portalGunMoonPullsBlocks() && this.moonTicks % MOON_BLOCK_INTERVAL == 0) {
            this.pullMoonBlocks(level, shape, owner);
        }
    }

    private void pullMoonEntities(ServerLevel level, PortalShape shape, Player owner) {
        Vec3 center = shape.center();
        AABB area = new AABB(center, center).inflate(MOON_RANGE);
        Vec3 target = center.add(shape.normal().scale(0.3D));
        boolean vacuumTick = this.moonTicks % MOON_VACUUM_INTERVAL == 0;
        for (Entity entity : level.getEntities(this, area, this::canMoonAffect)) {
            Vec3 position = entity.getBoundingBox().getCenter();
            PortalShape.PortalLocalCoords local = shape.localCoords(position);
            double depth = local.depth();
            double distance = position.distanceTo(center);
            if (depth < -0.5D || distance > MOON_RANGE) {
                continue;
            }
            boolean atMouth = depth <= MOON_MOUTH_DEPTH + entity.getBbWidth() * 0.5D
                    && Math.abs(local.horizontal()) <= shape.halfWidth() + 0.25D
                    && Math.abs(local.vertical()) <= shape.halfHeight() + 0.25D;
            if (atMouth) {
                if (!this.swallowIntoSpace(level, entity, owner)) {
                    this.holdInVacuum(level, entity, shape, owner, vacuumTick);
                }
                continue;
            }
            double falloff = 1.0D - distance / MOON_RANGE;
            double strength = MOON_PULL_STRENGTH * (0.35D + falloff * falloff * 1.6D);
            Vec3 worldMotion = PortalTransitMath.entityVelocityToWorld(entity, entity.getDeltaMovement())
                    .scale(0.92D)
                    .add(target.subtract(position).normalize().scale(strength));
            if (worldMotion.length() > MOON_MAX_SPEED) {
                worldMotion = worldMotion.normalize().scale(MOON_MAX_SPEED);
            }
            applyMoonMotion(entity, worldMotion);
        }
    }

    private boolean canMoonAffect(Entity entity) {
        if (!entity.isAlive() || entity.isPassenger() || entity.isSpectator()
                || entity instanceof PortalEntity || entity instanceof PortalGunProjectileEntity) {
            return false;
        }
        if (entity instanceof Player player && player.isCreative()) {
            return false;
        }
        return entity instanceof LivingEntity || entity instanceof ItemEntity || entity instanceof ExperienceOrb
                || entity instanceof FallingBlockEntity || entity instanceof Projectile
                || entity instanceof VehicleEntity || entity instanceof PrimedTnt;
    }

    private boolean swallowIntoSpace(ServerLevel level, Entity entity, Player owner) {
        if (entity instanceof Player || entity instanceof VehicleEntity) {
            return false;
        }
        if (entity instanceof LivingEntity living) {
            living.hurtServer(level, AntarchyDamageSources.moonVacuum(level, owner), Float.MAX_VALUE);
            return !living.isAlive();
        }
        entity.discard();
        return true;
    }

    private void holdInVacuum(ServerLevel level, Entity entity, PortalShape shape, Player owner, boolean vacuumTick) {
        Vec3 hold = shape.center().add(shape.normal().scale(MOON_HOLD_DEPTH + entity.getBbWidth() * 0.5D));
        applyMoonMotion(entity, hold.subtract(entity.getBoundingBox().getCenter()).scale(0.45D));
        if (vacuumTick && entity instanceof LivingEntity living) {
            living.hurtServer(level, AntarchyDamageSources.moonVacuum(level, owner == entity ? null : owner), MOON_VACUUM_DAMAGE);
        }
    }

    private static void applyMoonMotion(Entity entity, Vec3 worldMotion) {
        entity.setDeltaMovement(PortalTransitMath.entityVelocityFromWorld(entity, worldMotion));
        entity.needsSync = true;
        entity.fallDistance = 0.0F;
        if (entity instanceof LivingEntity) {
            entity.syncVelocity = true;
        }
    }

    private void pullMoonBlocks(ServerLevel level, PortalShape shape, Player owner) {
        int cap = AntarchySettings.portalGunMoonBlockCap();
        int pulled = 0;
        for (int ray = 0; ray < MOON_BLOCK_RAYS && pulled < MOON_BLOCKS_PER_PULL && this.moonSuckedBlocks < cap; ray++) {
            Vec3 direction = new Vec3(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian());
            if (direction.lengthSqr() < 1.0E-6D) {
                continue;
            }
            direction = direction.normalize();
            double outward = direction.dot(shape.normal());
            if (outward < MOON_RAY_MIN_OUTWARD) {
                direction = direction.subtract(shape.normal().scale(outward - MOON_RAY_MIN_OUTWARD - this.random.nextDouble() * 0.3D)).normalize();
            }
            Vec3 start = shape.center()
                    .add(shape.right().scale((this.random.nextDouble() * 2.0D - 1.0D) * shape.halfWidth()))
                    .add(shape.up().scale((this.random.nextDouble() * 2.0D - 1.0D) * shape.halfHeight()))
                    .add(shape.normal().scale(0.3D));
            Vec3 end = start.add(direction.scale(MOON_RANGE));
            BlockPos hit = this.firstBlockAlong(level, shape, start, end);
            if (hit == null) {
                continue;
            }
            BlockState state = loadedState(level, hit);
            if (state == null || !BlockSuction.canLift(level, hit, state, true)
                    || owner != null && !level.mayInteract(owner, hit)) {
                continue;
            }
            FallingBlockEntity falling = BlockSuction.lift(level, hit, state, shape.center().add(shape.normal().scale(1.2D)), MOON_BLOCK_SPEED);
            if (falling == null) {
                continue;
            }
            falling.setNoGravity(true);
            this.moonLiftedBlocks.put(falling.getUUID(), this.moonTicks);
            pulled++;
            this.moonSuckedBlocks++;
        }
    }

    private void tickMoonLiftedBlocks(ServerLevel level, PortalShape shape) {
        Iterator<Map.Entry<UUID, Integer>> iterator = this.moonLiftedBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            if (!(level.getEntity(entry.getKey()) instanceof FallingBlockEntity falling) || !falling.isAlive()) {
                iterator.remove();
                continue;
            }
            if (this.moonTicks - entry.getValue() > MOON_BLOCK_MAX_FLIGHT_TICKS
                    || falling.getBoundingBox().getCenter().distanceTo(shape.center()) <= MOON_BLOCK_SWALLOW_RADIUS) {
                falling.discard();
                iterator.remove();
            }
        }
    }

    private void releaseMoonLiftedBlocks(ServerLevel level) {
        for (UUID id : this.moonLiftedBlocks.keySet()) {
            if (level.getEntity(id) instanceof FallingBlockEntity falling && falling.isAlive()) {
                falling.setNoGravity(false);
                falling.setDeltaMovement(falling.getDeltaMovement().scale(0.2D));
                falling.syncVelocity = true;
            }
        }
        this.moonLiftedBlocks.clear();
    }

    private BlockPos firstBlockAlong(ServerLevel level, PortalShape shape, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        int steps = Math.max(1, (int) Math.ceil(delta.length() / MOON_RAY_STEP));
        BlockPos previous = null;
        for (int step = 0; step <= steps; step++) {
            BlockPos pos = BlockPos.containing(start.add(delta.scale((double) step / steps)));
            if (pos.equals(previous)) {
                continue;
            }
            previous = pos;
            if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
                return null;
            }
            BlockState state = loadedState(level, pos);
            if (state == null) {
                return null;
            }
            if (state.isAir() || this.containsPortalSpot(pos)) {
                continue;
            }
            PortalShape.PortalLocalCoords local = shape.localCoords(Vec3.atCenterOf(pos));
            if (local.depth() < -1.0D) {
                return null;
            }
            if (local.depth() < 0.5D
                    && Math.abs(local.horizontal()) <= shape.halfWidth() + 1.0D
                    && Math.abs(local.vertical()) <= shape.halfHeight() + 1.0D) {
                return null;
            }
            return pos;
        }
        return null;
    }

    private static BlockState loadedState(ServerLevel level, BlockPos pos) {
        net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource()
                .getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        return chunk == null ? null : chunk.getBlockState(pos);
    }

    private void spawnMoonParticles() {
        PortalShape shape = this.getWorldPortalShape();
        Vec3 center = shape.center();
        for (int i = 0; i < 6; i++) {
            Vec3 direction = new Vec3(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian());
            if (direction.lengthSqr() < 1.0E-6D) {
                continue;
            }
            direction = direction.normalize();
            if (direction.dot(shape.normal()) < 0.1D) {
                direction = direction.add(shape.normal().scale(0.1D - direction.dot(shape.normal()))).normalize();
            }
            Vec3 start = center.add(direction.scale(1.5D + this.random.nextDouble() * MOON_RANGE * 0.6D));
            Vec3 velocity = center.subtract(start).normalize().scale(0.35D + this.random.nextDouble() * 0.3D);
            this.level().addParticle(ParticleTypes.CLOUD, start.x, start.y, start.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private boolean isStillRegistered(ServerLevel level) {
        return PortalGunSavedData.isRegistered(level, this.gunId, this.getPortalSide(), this.getUUID());
    }

    private boolean hasValidSurface() {
        Direction facing = this.getFacingDirection();
        if (!PortalGunPlacement.isRectangularFootprint(this.portalSpots, this.getFacingDirection(), this.getUpAxis(), this.getPortalWidth(), this.getPortalHeight())) {
            return false;
        }
        for (int i = 0; i < this.portalSpots.length; i++) {
            BlockPos portalSpot = this.portalSpots[i];
            BlockPos supportPos = portalSpot.relative(facing.getOpposite());
            BlockState supportState = this.level().getBlockState(supportPos);
            if (!supportState.isFaceSturdy(this.level(), supportPos, facing)) {
                return false;
            }
            BlockEntity blockEntity = this.level().getBlockEntity(portalSpot);
            if (!(blockEntity instanceof PortalGunPortalCellAccess cell)) {
                return false;
            }
            PortalGunPortalFaceRecord record = cell.portalGun$getFaceRecord(facing, this.getUUID());
            if (record != null) {
                if (this.ownerId == null || !this.ownerId.equals(record.ownerId()) || record.side() != this.getPortalSide() || record.master() != (i == 0)) {
                    return false;
                }
            } else if (i == 0) {
                if (!(blockEntity instanceof PortalGunPortalMasterBlockEntity master) || !master.matches(this.ownerId, this.getUUID(), this.getPortalSide())) {
                    return false;
                }
            } else if (!(blockEntity instanceof PortalGunPortalBaseBlockEntity base) || !base.matches(this.ownerId, this.getUUID(), this.getPortalSide())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void saveExtra(ValueOutput tag) {
        if (this.ownerId != null) {
            tag.store("OwnerId", UUIDUtil.CODEC, this.ownerId);
        }
        if (this.gunId != null) {
            tag.store("GunId", UUIDUtil.CODEC, this.gunId);
        }
        if (this.isMoonPortal()) {
            tag.putBoolean("Moon", true);
            tag.putInt("MoonTicks", this.moonTicks);
            tag.putInt("MoonSuckedBlocks", this.moonSuckedBlocks);
            ValueOutput.TypedOutputList<UUID> lifted = tag.list("MoonLiftedBlocks", UUIDUtil.CODEC);
            for (UUID id : this.moonLiftedBlocks.keySet()) {
                lifted.add(id);
            }
        }
        tag.putInt("Side", this.getPortalSide().ordinal());
        tag.putInt("Variant", this.getVariant().id());
        tag.putInt("SupportX", this.supportOrigin.getX());
        tag.putInt("SupportY", this.supportOrigin.getY());
        tag.putInt("SupportZ", this.supportOrigin.getZ());
        tag.putInt("MasterX", this.masterPos.getX());
        tag.putInt("MasterY", this.masterPos.getY());
        tag.putInt("MasterZ", this.masterPos.getZ());
        tag.putInt("BaseX", this.basePos.getX());
        tag.putInt("BaseY", this.basePos.getY());
        tag.putInt("BaseZ", this.basePos.getZ());
        writeSpots(tag.list("PortalSpots", CompoundTag.CODEC), java.util.Arrays.asList(this.portalSpots));
        writeSpots(tag.list("CompensatedSpots", CompoundTag.CODEC), this.compensatedSpots);
    }

    @Override
    protected void loadExtra(ValueInput tag) {
        tag.read("OwnerId", UUIDUtil.CODEC).ifPresent(id -> this.ownerId = id);
        this.gunId = tag.read("GunId", UUIDUtil.CODEC).orElse(null);
        this.setSyncedUuid(OWNER_ID, this.ownerId);
        this.setSyncedUuid(GUN_ID, this.gunId);
        this.entityData.set(MOON, tag.getBooleanOr("Moon", false));
        this.moonTicks = tag.getIntOr("MoonTicks", 0);
        this.moonSuckedBlocks = tag.getIntOr("MoonSuckedBlocks", 0);
        this.moonLiftedBlocks.clear();
        for (UUID lifted : tag.listOrEmpty("MoonLiftedBlocks", UUIDUtil.CODEC)) {
            this.moonLiftedBlocks.put(lifted, this.moonTicks);
        }
        this.entityData.set(SIDE, tag.getIntOr("Side", 0));
        this.entityData.set(VARIANT, PortalGunVariant.byId(tag.getIntOr("Variant", 0)).id());
        this.supportOrigin = new BlockPos(tag.getIntOr("SupportX", 0), tag.getIntOr("SupportY", 0), tag.getIntOr("SupportZ", 0));
        this.masterPos = new BlockPos(tag.getIntOr("MasterX", 0), tag.getIntOr("MasterY", 0), tag.getIntOr("MasterZ", 0));
        this.basePos = new BlockPos(tag.getIntOr("BaseX", 0), tag.getIntOr("BaseY", 0), tag.getIntOr("BaseZ", 0));
        List<BlockPos> spots = readSpots(tag.listOrEmpty("PortalSpots", CompoundTag.CODEC));
        if (!spots.isEmpty()) {
            this.portalSpots = spots.toArray(BlockPos[]::new);
        }
        this.compensatedSpots = new HashSet<>(readSpots(tag.listOrEmpty("CompensatedSpots", CompoundTag.CODEC)));
    }

    private static void writeSpots(ValueOutput.TypedOutputList<CompoundTag> list, Iterable<BlockPos> positions) {
        for (BlockPos position : positions) {
            CompoundTag spot = new CompoundTag();
            spot.putInt("X", position.getX());
            spot.putInt("Y", position.getY());
            spot.putInt("Z", position.getZ());
            list.add(spot);
        }
    }

    private static List<BlockPos> readSpots(Iterable<CompoundTag> list) {
        List<BlockPos> positions = new java.util.ArrayList<>();
        for (CompoundTag spot : list) {
            positions.add(new BlockPos(spot.getIntOr("X", 0), spot.getIntOr("Y", 0), spot.getIntOr("Z", 0)));
        }
        return positions;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.isMoonPortal() && this.level() instanceof ServerLevel moonLevel) {
            this.releaseMoonLiftedBlocks(moonLevel);
        }
        Level level = this.level();
        UUID ownerId = this.ownerId;
        boolean destroyPortal = reason.shouldDestroy();
        if (destroyPortal && !level.isClientSide() && ownerId != null && level instanceof ServerLevel serverLevel) {
            PortalGunSavedData.clearPortal(serverLevel.getServer(), this.gunId, this.getPortalSide(), this.getUUID(), serverLevel.dimension().identifier());
        }
        super.remove(reason);
        if (destroyPortal && !level.isClientSide() && ownerId != null && level instanceof ServerLevel serverLevel) {
            this.clearPortalBlocks(serverLevel);
        }
    }

    private void clearPortalBlocks(ServerLevel level) {
        for (BlockPos portalSpot : this.portalSpots) {
            if (level.getBlockEntity(portalSpot) instanceof PortalGunPortalCellAccess cell) {
                PortalGunPortalFaceRecord removed = cell.portalGun$removeFaceRecord(this.getFacingDirection(), this.getUUID());
                if (!cell.portalGun$getFaceRecords().isEmpty()) {
                    continue;
                }
                if (cell instanceof PortalGunPortalMasterBlockEntity master && this.getUUID().equals(master.getPortalId())) {
                    level.removeBlock(portalSpot, false);
                } else if (cell instanceof PortalGunPortalBaseBlockEntity base && this.getUUID().equals(base.getPortalId())) {
                    level.removeBlock(portalSpot, false);
                } else if (removed != null) {
                    level.removeBlock(portalSpot, false);
                }
            }
        }
    }

    public static SoundEvent sound(String path) {
        Identifier soundId = Identifier.fromNamespaceAndPath(Antarchy.MODID, path);
        return BuiltInRegistries.SOUND_EVENT.getOptional(soundId)
                .orElseThrow(() -> new IllegalStateException("Missing portal gun sound event: " + soundId));
    }

    private final class GunAppearance implements PortalAppearance {
        @Override
        public int color() {
            return PortalGunPortalEntity.this.getPortalColor();
        }

        @Override
        public Identifier rimTexture() {
            return PortalGunPortalEntity.this.getRimTexture();
        }

        @Override
        public Identifier surfaceTexture(boolean fancy) {
            return fancy
                    ? PortalGunPortalEntity.this.getVariant().vortexTexture(PortalGunPortalEntity.this.getPortalSide())
                    : PortalGunPortalEntity.this.getRimTexture();
        }
    }
}
