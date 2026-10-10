package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PortalGunProjectileEntity extends ThrowableItemProjectile {
    private static final TagKey<Block> GLASS_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "glass_blocks"));
    private static final TagKey<Block> GLASS_PANES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "glass_panes"));
    private static final int MAX_TICKETED_PROJECTILES = 20;
    private static final int PROJECTILE_TICKET_LEVEL = 31;
    private static final int MAX_CHUNK_WAIT_TICKS = 200;
    private static final TicketType<UUID> PROJECTILE_TICKET = TicketType.create("antarchy_portal_projectile", Comparator.comparing(UUID::toString));
    private static final LinkedHashMap<UUID, ProjectileTicket> PROJECTILE_TICKETS = new LinkedHashMap<>();
    private static final EntityDataAccessor<Integer> SIDE = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOOTER_ID = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISTANCE = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> SPAWN_X = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> SPAWN_Y = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> SPAWN_Z = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> MAX_TRAVEL_DISTANCE_DATA = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> VELOCITY_X = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VELOCITY_Y = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VELOCITY_Z = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PROJECTILE_SPEED = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> PASS_THROUGH_GLASS = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PASS_THROUGH_LIQUID = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int TRAIL_PARTICLES_PER_TICK = 4;
    private static final float TRAIL_DUST_SCALE = 0.45F;
    private static final float IMPACT_DUST_SCALE = 0.8F;
    private static final int IMPACT_PARTICLES = 14;
    public static final double MIN_LAUNCH_SPEED = 4.98D;
    public static final double LAUNCH_SPEED_VARIANCE = 0.02D;
    public static final double MAX_TRAVEL_DISTANCE = 10000.0D;
    private UUID gunId;
    private boolean spawnSynced;
    private boolean ticketRegistered;
    private int chunkWaitTicks;
    public int portalWidth = 1;
    public int portalHeight = 2;

    public PortalGunProjectileEntity(EntityType<? extends PortalGunProjectileEntity> entityType, Level level) {
        super(entityType, level);
        this.configureProjectile();
    }

    public PortalGunProjectileEntity(EntityType<? extends PortalGunProjectileEntity> entityType, LivingEntity owner, Level level) {
        super(entityType, owner, level);
        this.configureProjectile();
    }

    private void configureProjectile() {
        this.setNoGravity(true);
        this.noCulling = true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(0.3F, 0.3F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SIDE, PortalGunPortalEntity.PortalSide.BLUE.ordinal());
        builder.define(SHOOTER_ID, -1);
        builder.define(DISTANCE, 0);
        builder.define(SPAWN_X, Double.doubleToRawLongBits(0.0D));
        builder.define(SPAWN_Y, Double.doubleToRawLongBits(0.0D));
        builder.define(SPAWN_Z, Double.doubleToRawLongBits(0.0D));
        builder.define(MAX_TRAVEL_DISTANCE_DATA, (int) MAX_TRAVEL_DISTANCE);
        builder.define(VELOCITY_X, 0.0F);
        builder.define(VELOCITY_Y, 0.0F);
        builder.define(VELOCITY_Z, 0.0F);
        builder.define(PROJECTILE_SPEED, 4.99F);
        builder.define(PASS_THROUGH_GLASS, false);
        builder.define(PASS_THROUGH_LIQUID, false);
    }

    public void configure(PortalGunPortalEntity.PortalSide side, UUID gunId, ItemStack gunStack) {
        AntarchyGravityApi.setAirborneGravityDirection(this, AntarchyGravityDirection.DOWN, false, AntarchyGravityTransition.INSTANT);
        this.entityData.set(SIDE, side.ordinal());
        this.gunId = gunId;
        this.setItem(gunStack.copyWithCount(1));
        this.portalWidth = PortalGunItem.getPortalWidth(gunStack);
        this.portalHeight = PortalGunItem.getPortalHeight(gunStack);
        this.entityData.set(MAX_TRAVEL_DISTANCE_DATA, AntarchySettings.portalGunMaxShootDistance());
        this.entityData.set(PASS_THROUGH_GLASS, AntarchySettings.portalGunCanFireThroughGlass());
        this.entityData.set(PASS_THROUGH_LIQUID, AntarchySettings.portalGunCanFireThroughLiquid());
        Entity owner = this.getOwner();
        this.entityData.set(SHOOTER_ID, owner == null ? -1 : owner.getId());
    }

    public void syncLaunchState() {
        this.syncSpawnPosition(this.position());
        this.syncVelocity();
    }

    public void setProjectileSpeed(double speed) {
        this.entityData.set(PROJECTILE_SPEED, (float) speed);
    }

    public double getProjectileSpeed() {
        return this.entityData.get(PROJECTILE_SPEED);
    }

    public PortalGunVariant getVariant() {
        return this.getItem().getItem() instanceof PortalGunItem portalGun ? portalGun.getVariant() : PortalGunVariant.DEFAULT;
    }

    public PortalGunPortalEntity.PortalSide getPortalSide() {
        return this.entityData.get(SIDE) == PortalGunPortalEntity.PortalSide.ORANGE.ordinal() ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
    }

    public int getShooterId() {
        return this.entityData.get(SHOOTER_ID);
    }

    public int getSyncedDistance() {
        return this.entityData.get(DISTANCE);
    }

    public int getMaxTravelDistance() {
        return this.entityData.get(MAX_TRAVEL_DISTANCE_DATA);
    }

    public Vec3 getSyncedSpawnPosition() {
        return new Vec3(
                Double.longBitsToDouble(this.entityData.get(SPAWN_X)),
                Double.longBitsToDouble(this.entityData.get(SPAWN_Y)),
                Double.longBitsToDouble(this.entityData.get(SPAWN_Z))
        );
    }

    public Vec3 getSyncedVelocity() {
        return new Vec3(this.entityData.get(VELOCITY_X), this.entityData.get(VELOCITY_Y), this.entityData.get(VELOCITY_Z));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide && !this.spawnSynced) {
            Vec3 spawnPosition = this.getSyncedSpawnPosition();
            this.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
            this.setDeltaMovement(this.getSyncedVelocity());
            this.spawnSynced = true;
        }
        if (this.level() instanceof ServerLevel serverLevel && !this.isRemoved() && !this.prepareServerStep(serverLevel)) {
            return;
        }
        this.baseTick();
        if (this.isRemoved()) {
            return;
        }
        if (!this.spawnSynced && !this.level().isClientSide) {
            this.syncSpawnPosition(this.position());
            this.syncVelocity();
            this.spawnSynced = true;
        }
        this.entityData.set(DISTANCE, this.tickCount);
        int maxTravelDistance = this.getMaxTravelDistance();
        Vec3 spawnPosition = this.getSyncedSpawnPosition();
        if (this.tickCount > maxTravelDistance / 5.0D + 10.0D
                || this.position().distanceToSqr(spawnPosition) > (maxTravelDistance + 5.0D) * (maxTravelDistance + 5.0D)) {
            this.discard();
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        Vec3 start = this.position();
        Vec3 end = start.add(motion);
        ClipContext.Fluid fluidMode = this.entityData.get(PASS_THROUGH_LIQUID) ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY;
        HitResult hit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, fluidMode, this));
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos hitBlockPos = blockHit.getBlockPos();
            net.minecraft.world.level.block.state.BlockState hitBlockState = this.level().getBlockState(hitBlockPos);
            boolean passThroughGlass = this.shouldPassThroughGlass(blockHit);
            if (!hitBlockState.isAir()) {
                hitBlockState.entityInside(this.level(), hitBlockPos, this);
                if (this.isRemoved()) {
                    return;
                }
            }
            if (!passThroughGlass) {
                this.onHit(blockHit);
                if (this.isRemoved()) {
                    return;
                }
            }
            this.setPos(blockHit.getLocation().subtract(motion.scale(0.98D)));
        }
        if (motion.lengthSqr() > 1.0E-6D) {
            double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            this.setYRot((float) (net.minecraft.util.Mth.atan2(motion.z, motion.x) * (180.0D / Math.PI)) - 90.0F);
            this.setXRot((float) (-(net.minecraft.util.Mth.atan2(motion.y, horizontal) * (180.0D / Math.PI))));
        }
        this.setPos(this.position().add(motion));
        this.checkInsideBlocks();
        if (this.isRemoved()) {
            return;
        }
        if (this.isInWater()) {
            for (int i = 0; i < 4; i++) {
                float backtrack = 0.25F;
                this.level().addParticle(
                        net.minecraft.core.particles.ParticleTypes.BUBBLE,
                        this.getX() - motion.x * backtrack,
                        this.getY() - motion.y * backtrack,
                        this.getZ() - motion.z * backtrack,
                        motion.x,
                        motion.y,
                        motion.z
                );
            }
        }
        if (this.level().isClientSide && this.tickCount > 1) {
            net.minecraft.core.particles.DustParticleOptions dust = this.trailDust(TRAIL_DUST_SCALE);
            Vec3 point = this.position();
            for (int i = 0; i < TRAIL_PARTICLES_PER_TICK; i++) {
                Vec3 trailPoint = point.subtract(motion.scale((double) i / TRAIL_PARTICLES_PER_TICK));
                this.level().addParticle(dust, trailPoint.x, trailPoint.y, trailPoint.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        this.releaseProjectileTicket();
        super.remove(reason);
    }

    private boolean prepareServerStep(ServerLevel level) {
        Vec3 position = this.position();
        Vec3 motion = this.getDeltaMovement();
        if (position.y > level.getMaxBuildHeight() + 2.0D && motion.y >= 0.0D
                || position.y < level.getMinBuildHeight() - 2.0D && motion.y <= 0.0D) {
            this.discard();
            return false;
        }
        List<ChunkPos> path = chunksAlong(position, position.add(motion));
        boolean chunkload = AntarchySettings.portalGunCanPortalProjectilesChunkload();
        if (chunkload) {
            this.updateProjectileTicket(level, path);
        } else {
            this.releaseProjectileTicket();
        }
        for (ChunkPos chunk : path) {
            if (level.getChunkSource().getChunkNow(chunk.x, chunk.z) == null) {
                if (!chunkload || ++this.chunkWaitTicks > MAX_CHUNK_WAIT_TICKS) {
                    this.discard();
                }
                return false;
            }
        }
        this.chunkWaitTicks = 0;
        return true;
    }

    private static List<ChunkPos> chunksAlong(Vec3 start, Vec3 end) {
        int minX = SectionPos.blockToSectionCoord(Math.min(start.x, end.x));
        int maxX = SectionPos.blockToSectionCoord(Math.max(start.x, end.x));
        int minZ = SectionPos.blockToSectionCoord(Math.min(start.z, end.z));
        int maxZ = SectionPos.blockToSectionCoord(Math.max(start.z, end.z));
        if (maxX - minX > 1 || maxZ - minZ > 1) {
            return List.of(new ChunkPos(minX, minZ), new ChunkPos(maxX, maxZ));
        }
        List<ChunkPos> chunks = new ArrayList<>(4);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(new ChunkPos(x, z));
            }
        }
        return chunks;
    }

    private void updateProjectileTicket(ServerLevel serverLevel, List<ChunkPos> chunks) {
        UUID ticketId = this.getUUID();
        ProjectileTicket current = PROJECTILE_TICKETS.get(ticketId);
        if (current != null && current.level == serverLevel && current.chunks.equals(chunks)) {
            return;
        }
        if (current != null) {
            removeTicket(ticketId, current);
            PROJECTILE_TICKETS.remove(ticketId);
        }
        while (PROJECTILE_TICKETS.size() >= MAX_TICKETED_PROJECTILES) {
            Map.Entry<UUID, ProjectileTicket> oldest = PROJECTILE_TICKETS.entrySet().iterator().next();
            removeTicket(oldest.getKey(), oldest.getValue());
            PROJECTILE_TICKETS.remove(oldest.getKey());
        }
        for (ChunkPos chunk : chunks) {
            serverLevel.getChunkSource().addRegionTicket(PROJECTILE_TICKET, chunk, PROJECTILE_TICKET_LEVEL, ticketId);
        }
        PROJECTILE_TICKETS.put(ticketId, new ProjectileTicket(serverLevel, List.copyOf(chunks)));
        this.ticketRegistered = true;
    }

    private static void removeTicket(UUID ticketId, ProjectileTicket ticket) {
        for (ChunkPos chunk : ticket.chunks()) {
            ticket.level().getChunkSource().removeRegionTicket(PROJECTILE_TICKET, chunk, PROJECTILE_TICKET_LEVEL, ticketId);
        }
    }

    private void releaseProjectileTicket() {
        if (!this.ticketRegistered) {
            return;
        }
        UUID ticketId = this.getUUID();
        ProjectileTicket ticket = PROJECTILE_TICKETS.remove(ticketId);
        if (ticket != null) {
            removeTicket(ticketId, ticket);
        }
        this.ticketRegistered = false;
    }

    public static void releaseAllProjectileTickets() {
        for (Map.Entry<UUID, ProjectileTicket> entry : PROJECTILE_TICKETS.entrySet()) {
            removeTicket(entry.getKey(), entry.getValue());
        }
        PROJECTILE_TICKETS.clear();
    }

    private record ProjectileTicket(ServerLevel level, List<ChunkPos> chunks) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            this.spawnImpactParticles(serverLevel, result.getLocation());
            ItemStack sourceStack = this.resolveSourceStack();
            if (this.getOwner() instanceof LivingEntity shooter && sourceStack.getItem() instanceof PortalGunItem portalGunItem) {
                portalGunItem.handlePortalImpact(serverLevel, shooter, sourceStack, this.gunId, this.getPortalSide(), result, result.getLocation(), this.portalWidth, this.portalHeight);
            }
        }
        this.discard();
    }

    private boolean shouldPassThroughGlass(BlockHitResult hit) {
        if (!this.entityData.get(PASS_THROUGH_GLASS)) {
            return false;
        }
        BlockPos blockPos = hit.getBlockPos();
        BlockState state = this.level().getBlockState(blockPos);
        Block block = state.getBlock();
        return state.is(GLASS_BLOCKS)
                || state.is(GLASS_PANES)
                || block == Blocks.GLASS
                || block == Blocks.GLASS_PANE
                || block == Blocks.TINTED_GLASS
                || block instanceof StainedGlassBlock
                || block instanceof StainedGlassPaneBlock
                || block instanceof TintedGlassBlock;
    }

    private void spawnImpactParticles(ServerLevel level, Vec3 impactPos) {
        level.sendParticles(this.trailDust(IMPACT_DUST_SCALE), impactPos.x, impactPos.y, impactPos.z, IMPACT_PARTICLES, 0.12D, 0.12D, 0.12D, 0.02D);
    }

    private net.minecraft.core.particles.DustParticleOptions trailDust(float scale) {
        int color = this.getVariant().color(this.getPortalSide());
        return new net.minecraft.core.particles.DustParticleOptions(
                new org.joml.Vector3f((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F), scale);
    }

    private void syncSpawnPosition(Vec3 pos) {
        this.entityData.set(SPAWN_X, Double.doubleToRawLongBits(pos.x));
        this.entityData.set(SPAWN_Y, Double.doubleToRawLongBits(pos.y));
        this.entityData.set(SPAWN_Z, Double.doubleToRawLongBits(pos.z));
        this.spawnSynced = true;
    }

    private void syncVelocity() {
        Vec3 velocity = this.getDeltaMovement();
        this.entityData.set(VELOCITY_X, (float) velocity.x);
        this.entityData.set(VELOCITY_Y, (float) velocity.y);
        this.entityData.set(VELOCITY_Z, (float) velocity.z);
    }

    private ItemStack resolveSourceStack() {
        ItemStack projectileStack = this.getItem();
        if (!(this.getOwner() instanceof Player player) || !(projectileStack.getItem() instanceof PortalGunItem portalGunItem) || this.gunId == null) {
            return projectileStack;
        }
        ItemStack matching = portalGunItem.findMatchingGunStack(player, this.gunId);
        return matching.isEmpty() ? projectileStack : matching;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Side", this.entityData.get(SIDE));
        tag.putInt("ShooterId", this.entityData.get(SHOOTER_ID));
        tag.putInt("Distance", this.entityData.get(DISTANCE));
        Vec3 spawnPosition = this.getSyncedSpawnPosition();
        tag.putInt("SpawnX", net.minecraft.util.Mth.floor(spawnPosition.x));
        tag.putInt("SpawnY", net.minecraft.util.Mth.floor(spawnPosition.y));
        tag.putInt("SpawnZ", net.minecraft.util.Mth.floor(spawnPosition.z));
        tag.putDouble("SpawnPosX", spawnPosition.x);
        tag.putDouble("SpawnPosY", spawnPosition.y);
        tag.putDouble("SpawnPosZ", spawnPosition.z);
        tag.putInt("MaxTravelDistance", this.entityData.get(MAX_TRAVEL_DISTANCE_DATA));
        tag.putFloat("VelocityX", this.entityData.get(VELOCITY_X));
        tag.putFloat("VelocityY", this.entityData.get(VELOCITY_Y));
        tag.putFloat("VelocityZ", this.entityData.get(VELOCITY_Z));
        tag.putFloat("ProjectileSpeed", this.entityData.get(PROJECTILE_SPEED));
        tag.putBoolean("PassThroughGlass", this.entityData.get(PASS_THROUGH_GLASS));
        tag.putBoolean("PassThroughLiquid", this.entityData.get(PASS_THROUGH_LIQUID));
        tag.putInt("PortalWidth", this.portalWidth);
        tag.putInt("PortalHeight", this.portalHeight);
        if (this.gunId != null) {
            tag.putUUID("GunId", this.gunId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SIDE, tag.getInt("Side"));
        this.entityData.set(SHOOTER_ID, tag.getInt("ShooterId"));
        this.entityData.set(DISTANCE, tag.getInt("Distance"));
        this.entityData.set(SPAWN_X, Double.doubleToRawLongBits(tag.contains("SpawnPosX") ? tag.getDouble("SpawnPosX") : tag.getInt("SpawnX")));
        this.entityData.set(SPAWN_Y, Double.doubleToRawLongBits(tag.contains("SpawnPosY") ? tag.getDouble("SpawnPosY") : tag.getInt("SpawnY")));
        this.entityData.set(SPAWN_Z, Double.doubleToRawLongBits(tag.contains("SpawnPosZ") ? tag.getDouble("SpawnPosZ") : tag.getInt("SpawnZ")));
        this.entityData.set(MAX_TRAVEL_DISTANCE_DATA, tag.contains("MaxTravelDistance")
                ? Math.max(1, tag.getInt("MaxTravelDistance"))
                : AntarchySettings.portalGunMaxShootDistance());
        this.entityData.set(VELOCITY_X, tag.getFloat("VelocityX"));
        this.entityData.set(VELOCITY_Y, tag.getFloat("VelocityY"));
        this.entityData.set(VELOCITY_Z, tag.getFloat("VelocityZ"));
        this.entityData.set(PROJECTILE_SPEED, tag.contains("ProjectileSpeed") ? tag.getFloat("ProjectileSpeed") : 4.99F);
        this.entityData.set(PASS_THROUGH_GLASS, tag.contains("PassThroughGlass") ? tag.getBoolean("PassThroughGlass") : AntarchySettings.portalGunCanFireThroughGlass());
        this.entityData.set(PASS_THROUGH_LIQUID, tag.contains("PassThroughLiquid") ? tag.getBoolean("PassThroughLiquid") : AntarchySettings.portalGunCanFireThroughLiquid());
        this.portalWidth = net.minecraft.util.Mth.clamp(tag.getInt("PortalWidth"), 1, 16);
        this.portalHeight = net.minecraft.util.Mth.clamp(tag.getInt("PortalHeight"), 2, 16);
        this.spawnSynced = true;
        if (tag.hasUUID("GunId")) {
            this.gunId = tag.getUUID("GunId");
        }
        ItemStack itemStack = this.getItem();
        if (this.gunId != null && !itemStack.isEmpty()) {
            net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, itemStack, customData -> customData.putUUID(PortalGunItem.GUN_ID_TAG, this.gunId));
        }
    }
}
