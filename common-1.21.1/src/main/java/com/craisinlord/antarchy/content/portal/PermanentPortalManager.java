package com.craisinlord.antarchy.content.portal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class PermanentPortalManager {
    private static final int TELEPORT_DELAY_TICKS = 20;
    private static final int ACTIVATION_SEARCH_RADIUS = 2;
    private static final Map<UUID, PortalWarmup> WARMUPS = new HashMap<>();

    private PermanentPortalManager() {
    }

    public static void handleSacrifice(LivingEntity entity) {
        PermanentPortalType type = PermanentPortalType.fromSacrifice(entity);
        if (type == null || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        PermanentPortalShape shape = findInactiveShape(serverLevel, entity.getBoundingBox(), type);
        if (shape == null) {
            return;
        }

        activatePortal(serverLevel, type, shape, SoundEvents.END_PORTAL_SPAWN, 0.9F, 0.9F);
    }

    public static boolean tryIgnitePortal(Level level, BlockPos triggerPos, PermanentPortalType type) {
        PermanentPortalShape shape = PermanentPortalShape.findInactiveNear(level, triggerPos, type);
        if (shape == null) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            activatePortal(serverLevel, type, shape, SoundEvents.FLINTANDSTEEL_USE, 0.9F, 1.0F);
        }
        return true;
    }

    public static void handleEntityInsidePortal(Entity entity, PermanentPortalType type) {
        if (!(entity.level() instanceof ServerLevel serverLevel) || !type.isEnabled() || entity.isPassenger() || entity.isVehicle()) {
            return;
        }
        if (entity.isOnPortalCooldown()) {
            WARMUPS.remove(entity.getUUID());
            return;
        }

        long gameTime = serverLevel.getGameTime();
        PortalWarmup warmup = WARMUPS.computeIfAbsent(entity.getUUID(), ignored -> new PortalWarmup());
        if (warmup.type != type || warmup.lastSeenTick < gameTime - 1) {
            warmup.ticks = 0;
        }
        warmup.type = type;
        if (warmup.lastSeenTick != gameTime) {
            warmup.ticks++;
            warmup.lastSeenTick = gameTime;
        }

        if (warmup.ticks < TELEPORT_DELAY_TICKS) {
            return;
        }

        WARMUPS.remove(entity.getUUID());
        PermanentPortalTeleporter.teleport(entity, type);
        if (entity.level() instanceof ServerLevel currentLevel) {
            currentLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }

    public static boolean isPortalStillValid(ServerLevel level, BlockPos pos, PermanentPortalType type, Direction.Axis axis) {
        PermanentPortalShape shape = PermanentPortalShape.findActive(level, pos, type, axis);
        return shape != null && shape.contains(pos);
    }

    @Nullable
    private static PermanentPortalShape findInactiveShape(ServerLevel level, AABB bounds, PermanentPortalType type) {
        int minX = (int) Math.floor(bounds.minX + 0.001D) - ACTIVATION_SEARCH_RADIUS;
        int minY = (int) Math.floor(bounds.minY + 0.001D) - ACTIVATION_SEARCH_RADIUS;
        int minZ = (int) Math.floor(bounds.minZ + 0.001D) - ACTIVATION_SEARCH_RADIUS;
        int maxX = (int) Math.floor(bounds.maxX - 0.001D) + ACTIVATION_SEARCH_RADIUS;
        int maxY = (int) Math.floor(bounds.maxY - 0.001D) + ACTIVATION_SEARCH_RADIUS;
        int maxZ = (int) Math.floor(bounds.maxZ - 0.001D) + ACTIVATION_SEARCH_RADIUS;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    PermanentPortalShape xShape = PermanentPortalShape.findInactive(level, candidate, type, Direction.Axis.X);
                    if (xShape != null && xShape.intersects(bounds)) {
                        return xShape;
                    }
                    PermanentPortalShape zShape = PermanentPortalShape.findInactive(level, candidate, type, Direction.Axis.Z);
                    if (zShape != null && zShape.intersects(bounds)) {
                        return zShape;
                    }
                }
            }
        }

        return null;
    }

    private static void spawnActivationParticles(ServerLevel level, PermanentPortalType type, Vec3 center) {
        switch (type) {
            case ELYTHIA -> {
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y, center.z, 24, 0.8D, 1.2D, 0.8D, 0.02D);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y, center.z, 12, 0.6D, 1.0D, 0.6D, 0.02D);
            }
            case THORAXIS -> {
                level.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 32, 0.8D, 1.2D, 0.8D, 0.15D);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 10, 0.5D, 0.8D, 0.5D, 0.01D);
            }
            case CAVARYN -> {
                level.sendParticles(ParticleTypes.MYCELIUM, center.x, center.y, center.z, 24, 0.8D, 1.2D, 0.8D, 0.01D);
                level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, center.x, center.y, center.z, 16, 0.6D, 1.0D, 0.6D, 0.01D);
            }
        }
    }

    private static void activatePortal(ServerLevel level, PermanentPortalType type, PermanentPortalShape shape, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        shape.fill(level);
        Vec3 center = shape.center();
        level.playSound(null, center.x, center.y, center.z, sound, SoundSource.BLOCKS, volume, pitch);
        spawnActivationParticles(level, type, center);
    }

    private static final class PortalWarmup {
        private PermanentPortalType type;
        private long lastSeenTick;
        private int ticks;
    }
}
