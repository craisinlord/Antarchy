package com.craisinlord.antarchy.content.elythia;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.ant.AntTeleportHelper;
import com.craisinlord.antarchy.content.network.AntDescentPayload;
import com.craisinlord.antarchy.content.network.AntDescentSync;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AntDescent {
    private static final int TELEPORT_TICK = 45;
    private static final int SEARCH_TIMEOUT_TICKS = 450;
    private static final int WAKE_UP_DELAY_TICKS = 30;
    private static final int MAX_GLIDE_TICKS = 1200;
    private static final int DROP_Y = 236;
    private static final int MIN_DROP_HEIGHT = 48;
    private static final int SLOW_FALL_HEIGHT = 14;
    private static final int SLOW_FALL_TICKS = 60;
    private static final double HOLD_DISTANCE_SQR = 0.09D;
    private static final double DISC_RADIUS = 2.5D;
    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private AntDescent() {
    }

    public static boolean start(ServerPlayer player, ServerLevel destination) {
        if (!AntarchySettings.brownAntDescent()
                || !AntarchySettings.brownAntDestinationDimension().equals(ThrowbackZones.ELYTHIA)
                || player.isSpectator()
                || STATES.containsKey(player.getUUID())) {
            return false;
        }
        boolean home;
        if (destination.dimension().equals(ThrowbackZones.ELYTHIA)) {
            home = false;
        } else if (player.level().dimension().equals(ThrowbackZones.ELYTHIA) && destination.dimension().equals(Level.OVERWORLD)) {
            home = true;
        } else {
            return false;
        }
        if (player.isPassenger()) {
            player.stopRiding();
        }
        State state = new State(home, player.serverLevel(), destination, player.position());
        STATES.put(player.getUUID(), state);
        AntTeleportHelper.prepareArrival(player, destination, arrival -> {
            state.arrival = arrival;
            state.searchDone = true;
        });
        AntDescentSync.sendToSelfAndTracking(player, payload(player, state, AntDescentPayload.PHASE_START, state.origin, false));
        if (home) {
            player.level().playSound(null, state.origin.x, state.origin.y, state.origin.z,
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.3F);
        } else {
            player.level().playSound(null, state.origin.x, state.origin.y, state.origin.z,
                    SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.6F, 0.5F);
        }
        return true;
    }

    public static boolean isProtected(ServerPlayer player, DamageSource source) {
        return STATES.containsKey(player.getUUID()) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    public static void tick(MinecraftServer server) {
        if (STATES.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, State>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, State> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            State state = entry.getValue();
            if (player == null || !player.isAlive() || !tick(player, state)) {
                iterator.remove();
            }
        }
    }

    public static void clear() {
        STATES.clear();
    }

    private static boolean tick(ServerPlayer player, State state) {
        state.tick++;
        if (!state.teleported) {
            if (player.level() != state.originLevel) {
                return false;
            }
            hold(player, state);
            particles(player, state);
            if (state.tick > SEARCH_TIMEOUT_TICKS || (state.tick >= TELEPORT_TICK && state.searchDone && state.arrival == null)) {
                AntDescentSync.sendToSelfAndTracking(player, payload(player, state, AntDescentPayload.PHASE_CANCEL, state.origin, false));
                AntTeleportHelper.failArrival(player);
                return false;
            }
            if (state.tick >= TELEPORT_TICK && state.searchDone) {
                arrive(player, state);
            }
            return true;
        }
        if (player.level() != state.destination) {
            return false;
        }
        return state.home ? wake(player, state) : glide(player, state);
    }

    private static void hold(ServerPlayer player, State state) {
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        if (player.position().distanceToSqr(state.origin) > HOLD_DISTANCE_SQR) {
            player.connection.teleport(state.origin.x, state.origin.y, state.origin.z, player.getYRot(), player.getXRot());
        }
    }

    private static void particles(ServerPlayer player, State state) {
        if (state.tick > TELEPORT_TICK || state.tick % 2 != 0) {
            return;
        }
        ServerLevel level = player.serverLevel();
        double radius = DISC_RADIUS * Math.min(1.0D, state.tick / 20.0D);
        for (int i = 0; i < 4; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double distance = radius * (0.6D + level.random.nextDouble() * 0.6D);
            double dx = Math.cos(angle) * distance;
            double dz = Math.sin(angle) * distance;
            if (state.home) {
                level.sendParticles(ParticleTypes.END_ROD, state.origin.x + dx, state.origin.y + 0.1D, state.origin.z + dz,
                        0, 0.0D, 1.0D, 0.0D, 0.06D);
            } else {
                level.sendParticles(ParticleTypes.PORTAL, state.origin.x, state.origin.y + 0.05D, state.origin.z,
                        0, dx, 0.6D, dz, 1.0D);
            }
        }
    }

    private static void arrive(ServerPlayer player, State state) {
        AntTeleportHelper.teleportPlayerWithCompanions(player, state.destination, state.arrival);
        state.teleported = true;
        state.tick = 0;
        boolean glide = false;
        if (state.home) {
            BlockPos bed = player.getRespawnPosition();
            if (bed != null
                    && player.getRespawnDimension().equals(state.destination.dimension())
                    && state.destination.getBlockState(bed).getBlock() instanceof BedBlock) {
                if (player.isPassenger()) {
                    player.stopRiding();
                }
                player.startSleeping(bed);
                state.sleeping = true;
            }
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.6F, 0.5F);
        } else {
            if (player.isPassenger()) {
                player.stopRiding();
            }
            glide = lift(player);
            state.gliding = glide;
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
        }
        AntDescentSync.send(player, payload(player, state, AntDescentPayload.PHASE_ARRIVED, player.position(), glide));
    }

    private static boolean lift(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (player.getAbilities().flying) {
            return false;
        }
        double ceiling = Math.max(DROP_Y, player.getY() + MIN_DROP_HEIGHT);
        if (ceiling > level.getMaxBuildHeight() - 4) {
            return false;
        }
        AABB box = player.getBoundingBox();
        AABB column = new AABB(box.minX, box.maxY, box.minZ, box.maxX, ceiling + box.getYsize(), box.maxZ);
        if (!level.noCollision(player, column) || level.containsAnyLiquid(column)) {
            return false;
        }
        player.connection.teleport(player.getX(), ceiling, player.getZ(), player.getYRot(), player.getXRot());
        player.resetFallDistance();
        return true;
    }

    private static boolean wake(ServerPlayer player, State state) {
        if (!state.sleeping) {
            return false;
        }
        if (state.tick < WAKE_UP_DELAY_TICKS) {
            return true;
        }
        if (player.isSleeping()) {
            player.stopSleepInBed(true, true);
        }
        return false;
    }

    private static boolean glide(ServerPlayer player, State state) {
        if (!state.gliding || state.tick > MAX_GLIDE_TICKS || player.isInWater() || player.getAbilities().flying
                || player.isFallFlying() || (state.tick > 5 && player.onGround())) {
            return false;
        }
        player.resetFallDistance();
        if (heightAboveGround(player) <= SLOW_FALL_HEIGHT) {
            MobEffectInstance current = player.getEffect(MobEffects.SLOW_FALLING);
            if (current == null || current.getDuration() < SLOW_FALL_TICKS / 2) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SLOW_FALL_TICKS, 0, true, false, true));
            }
        }
        return true;
    }

    private static int heightAboveGround(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos.MutableBlockPos cursor = player.blockPosition().mutable();
        int startY = cursor.getY();
        for (int depth = 0; depth <= SLOW_FALL_HEIGHT; depth++) {
            cursor.setY(startY - depth);
            if (cursor.getY() < level.getMinBuildHeight()
                    || !level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()
                    || !level.getFluidState(cursor).isEmpty()) {
                return depth;
            }
        }
        return SLOW_FALL_HEIGHT + 1;
    }

    private static AntDescentPayload payload(ServerPlayer player, State state, byte phase, Vec3 position, boolean glide) {
        return new AntDescentPayload(player.getId(), state.home, phase, position.x, position.y, position.z, glide);
    }

    private static final class State {
        private final boolean home;
        private final ServerLevel originLevel;
        private final ServerLevel destination;
        private final Vec3 origin;
        private volatile Vec3 arrival;
        private volatile boolean searchDone;
        private boolean teleported;
        private boolean sleeping;
        private boolean gliding;
        private int tick;

        private State(boolean home, ServerLevel originLevel, ServerLevel destination, Vec3 origin) {
            this.home = home;
            this.originLevel = originLevel;
            this.destination = destination;
            this.origin = origin;
        }
    }
}
