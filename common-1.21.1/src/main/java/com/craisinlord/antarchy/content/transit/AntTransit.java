package com.craisinlord.antarchy.content.transit;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.entity.ant.AntTeleportHelper;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.craisinlord.antarchy.content.network.AntTransitPayload;
import com.craisinlord.antarchy.content.network.AntTransitSync;
import com.craisinlord.antarchy.content.network.TransitStyle;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class AntTransit {
    public static final ResourceKey<Level> THORAXIS = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis"));
    public static final ResourceKey<Level> CAVARYN = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn"));

    private static final int TELEPORT_TICK = 45;
    private static final int SEARCH_TIMEOUT_TICKS = 450;
    private static final int PREPARE_TIMEOUT_TICKS = 1200;
    private static final int ARRIVAL_PROTECTION_TICKS = 40;
    private static final int WAKE_UP_DELAY_TICKS = 30;
    private static final int LANDING_GRACE_TICKS = 15;
    private static final int MAX_GLIDE_TICKS = 1200;
    private static final int DROP_Y = 236;
    private static final int MIN_DROP_HEIGHT = 48;
    private static final int GROUND_SCAN = 14;
    private static final int POOF_HEIGHT = 4;
    private static final double HOLD_DISTANCE_SQR = 0.09D;
    private static final double DISC_RADIUS = 2.5D;
    private static final String ARRIVAL_FAILED = "message.antarchy.teleport_arrival_failed";
    private static final String PREPARING = "message.antarchy.transit_preparing";
    private static final String WARP_FAILED ="message.antarchy.rainbow_ant_failed_dimension_create";
    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private AntTransit() {
    }

    public enum Kind {
        ELYTHIA,
        THORAXIS,
        CAVARYN,
        PRISM
    }

    public static boolean start(ServerPlayer player, ServerLevel destination, Kind kind) {
        if (kind == null || !available(player)) {
            return false;
        }
        TransitStyle style = styleFor(player.level().dimension(), destination.dimension(), kind);
        if (style == null) {
            return false;
        }
        State state = new State(style, player.serverLevel(), destination, player.position(), null);
        state.waiting = true;
        STATES.put(player.getUUID(), state);
        player.displayClientMessage(Component.translatable(PREPARING), true);
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.6F, 0.8F);
        AntTeleportHelper.prepareArrival(player, destination, arrival -> {
            state.arrival = arrival;
            state.searchDone = true;
        });
        return true;
    }

    public static boolean startWarp(ServerPlayer player, BooleanSupplier warp, @Nullable String failureKey) {
        if (!AntarchySettings.rainbowAntPrism() || !available(player)) {
            return false;
        }
        State state = new State(TransitStyle.PRISM_IN, player.serverLevel(), null, player.position(), warp);
        STATES.put(player.getUUID(), state);
        begin(player, state);
        state.searchDone = true;
        state.warpFailureKey = failureKey;
        return true;
    }

    public static boolean isActive(ServerPlayer player) {
        return STATES.containsKey(player.getUUID());
    }

    public static boolean isProtected(ServerPlayer player, DamageSource source) {
        State state = STATES.get(player.getUUID());
        return state != null && !state.waiting && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
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
            if (player == null) {
                iterator.remove();
                continue;
            }
            if (!player.isAlive() || !tick(player, state)) {
                if (!state.teleported && !state.waiting) {
                    AntTransitSync.send(player, payload(player, state, AntTransitPayload.PHASE_CANCEL, state.origin, false));
                }
                iterator.remove();
            }
        }
    }

    public static void clear() {
        STATES.clear();
    }

    private static boolean available(ServerPlayer player) {
        return !player.isSpectator() && !STATES.containsKey(player.getUUID());
    }

    private static TransitStyle styleFor(ResourceKey<Level> from, ResourceKey<Level> to, Kind kind) {
        return switch (kind) {
            case ELYTHIA -> {
                if (!AntarchySettings.brownAntDescent() || !AntarchySettings.brownAntDestinationDimension().equals(ThrowbackZones.ELYTHIA)) {
                    yield null;
                }
                if (to.equals(ThrowbackZones.ELYTHIA)) {
                    yield TransitStyle.ELYTHIA_IN;
                }
                yield from.equals(ThrowbackZones.ELYTHIA) && to.equals(Level.OVERWORLD) ? TransitStyle.ELYTHIA_HOME : null;
            }
            case THORAXIS -> {
                if (!AntarchySettings.redAntFlip() || !AntarchySettings.redAntDestinationDimension().equals(THORAXIS)) {
                    yield null;
                }
                yield to.equals(THORAXIS) ? TransitStyle.THORAXIS_IN : from.equals(THORAXIS) ? TransitStyle.THORAXIS_OUT : null;
            }
            case CAVARYN -> {
                if (!AntarchySettings.termiteBurrow() || !AntarchySettings.termiteDestinationDimension().equals(CAVARYN)) {
                    yield null;
                }
                yield to.equals(CAVARYN) ? TransitStyle.CAVARYN_IN : from.equals(CAVARYN) ? TransitStyle.CAVARYN_OUT : null;
            }
            case PRISM -> AntarchySettings.rainbowAntPrism() ? TransitStyle.PRISM_OUT : null;
        };
    }

    private static void begin(ServerPlayer player, State state) {
        if (player.isPassenger()) {
            player.stopRiding();
        }
        state.waiting = false;
        state.tick = 0;
        state.origin = settle(player);
        AntTransitSync.sendToSelfAndTracking(player, payload(player, state, AntTransitPayload.PHASE_START, state.origin, false));
        playAt(player, state.origin, startSound(state.style, player), 1.0F, startPitch(state.style));
        if (state.style == TransitStyle.CAVARYN_IN) {
            player.playNotifySound(AntarchySoundEvents.CAVARYN_HEARTBEAT.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }

    private static boolean grounded(ServerPlayer player) {
        return player.onGround() || player.isPassenger() || player.getAbilities().flying
                || player.isInWater() || player.isInLava() || player.onClimbable();
    }

    private static Vec3 settle(ServerPlayer player) {
        Vec3 pos = player.position();
        if (grounded(player)) {
            return pos;
        }
        AABB box = player.getBoundingBox();
        double drop = Shapes.collide(Direction.Axis.Y, box,
                player.serverLevel().getBlockCollisions(player, box.expandTowards(0.0D, -GROUND_SCAN, 0.0D)), -GROUND_SCAN);
        return drop > -GROUND_SCAN ? pos.add(0.0D, drop, 0.0D) : pos;
    }

    private static SoundEvent startSound(TransitStyle style, ServerPlayer player) {
        return switch (style) {
            case ELYTHIA_IN, ELYTHIA_HOME -> SoundEvents.AMETHYST_BLOCK_RESONATE;
            case THORAXIS_IN, THORAXIS_OUT -> AntarchySoundEvents.THORAXIS_DREAM_DUNES_ADDITIONS.get();
            case CAVARYN_IN, CAVARYN_OUT -> groundBelow(player.serverLevel(), player.position()).getSoundType().getBreakSound();
            case PRISM_IN, PRISM_OUT -> SoundEvents.BEACON_ACTIVATE;
        };
    }

    private static float startPitch(TransitStyle style) {
        return switch (style) {
            case ELYTHIA_IN, ELYTHIA_HOME -> 1.3F;
            case THORAXIS_IN, THORAXIS_OUT -> 1.0F;
            case CAVARYN_IN, CAVARYN_OUT -> 0.6F;
            case PRISM_IN, PRISM_OUT -> 1.6F;
        };
    }

    private static boolean tick(ServerPlayer player, State state) {
        state.tick++;
        if (state.waiting) {
            if (player.level() != state.originLevel) {
                return false;
            }
            if (state.tick > PREPARE_TIMEOUT_TICKS || (state.searchDone && state.arrival == null)) {
                player.displayClientMessage(Component.translatable(ARRIVAL_FAILED), true);
                return false;
            }
            if (state.searchDone) {
                if (grounded(player) || ++state.landingTicks > LANDING_GRACE_TICKS) {
                    begin(player, state);
                }
            } else if (state.tick % 4 == 0) {
                gathering(player, state);
            }
            return true;
        }
        if (state.teleported && player.isChangingDimension()) {
            return true;
        }
        if (!state.teleported) {
            if (player.level() != state.originLevel) {
                if (state.warpRequested) {
                    arrived(player, state, false);
                    return true;
                }
                return false;
            }
            hold(player, state);
            effects(player, state);
            if (state.warpRequested) {
                if (state.tick > SEARCH_TIMEOUT_TICKS) {
                    fail(player, state, WARP_FAILED);
                    return false;
                }
                return true;
            }
            if (state.tick > SEARCH_TIMEOUT_TICKS || (state.tick >= TELEPORT_TICK && state.searchDone && state.warp == null && state.arrival == null)) {
                fail(player, state, ARRIVAL_FAILED);
                return false;
            }
            if (state.tick >= TELEPORT_TICK && state.searchDone) {
                if (state.warp != null) {
                    if (!state.warp.getAsBoolean()) {
                        fail(player, state, state.warpFailureKey);
                        return false;
                    }
                    state.warpRequested = true;
                    if (player.level() != state.originLevel) {
                        arrived(player, state, false);
                    }
                    return true;
                }
                arrive(player, state);
            }
            return true;
        }
        if (state.destination != null && player.level() != state.destination) {
            return false;
        }
        return switch (state.style) {
            case ELYTHIA_IN -> glide(player, state);
            case ELYTHIA_HOME -> wake(player, state);
            default -> state.tick < ARRIVAL_PROTECTION_TICKS;
        };
    }

    private static void fail(ServerPlayer player, State state, String messageKey) {
        AntTransitSync.sendToSelfAndTracking(player, payload(player, state, AntTransitPayload.PHASE_CANCEL, state.origin, false));
        if (messageKey != null) {
            player.displayClientMessage(Component.translatable(messageKey), true);
        }
    }

    private static void hold(ServerPlayer player, State state) {
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        if (player.position().distanceToSqr(state.origin) > HOLD_DISTANCE_SQR) {
            player.connection.teleport(state.origin.x, state.origin.y, state.origin.z, player.getYRot(), player.getXRot());
        }
    }

    private static void effects(ServerPlayer player, State state) {
        if (state.tick > TELEPORT_TICK || state.tick % 2 != 0) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Vec3 origin = state.origin;
        double radius = DISC_RADIUS * Math.min(1.0D, state.tick / 20.0D);
        for (int i = 0; i < 4; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double distance = radius * (0.6D + level.random.nextDouble() * 0.6D);
            double x = origin.x + Math.cos(angle) * distance;
            double z = origin.z + Math.sin(angle) * distance;
            switch (state.style) {
                case ELYTHIA_IN, ELYTHIA_HOME -> level.sendParticles(ParticleTypes.END_ROD, x, origin.y + 0.1D, z, 0, 0.0D, 1.0D, 0.0D, 0.06D);
                case THORAXIS_IN, THORAXIS_OUT -> level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, origin.y + 0.1D, z, 0, 0.0D, 1.0D, 0.0D, 0.2D);
                case CAVARYN_IN, CAVARYN_OUT -> level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundBelow(level, origin)),
                        origin.x + (x - origin.x) * 0.5D, origin.y + 0.1D, origin.z + (z - origin.z) * 0.5D, 3, 0.2D, 0.1D, 0.2D, 0.15D);
                case PRISM_IN, PRISM_OUT -> level.sendParticles(new DustParticleOptions(rainbow((float) (angle / (Math.PI * 2.0D)) + state.tick * 0.02F), 1.2F),
                        x, origin.y + 0.1D, z, 0, 0.0D, 1.0D, 0.0D, 0.08D);
            }
        }
        if (state.style.cavaryn() && state.tick % 6 == 0) {
            BlockState ground = groundBelow(level, origin);
            playAt(player, origin, ground.getSoundType().getBreakSound(), 0.8F, 0.6F + level.random.nextFloat() * 0.3F);
        }
    }

    private static void gathering(ServerPlayer player, State state) {
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();
        double angle = state.tick * 0.35D;
        for (int i = 0; i < 2; i++) {
            double a = angle + i * Math.PI;
            double x = pos.x + Math.cos(a) * 0.9D;
            double z = pos.z + Math.sin(a) * 0.9D;
            switch (state.style) {
                case ELYTHIA_IN, ELYTHIA_HOME -> level.sendParticles(ParticleTypes.END_ROD, x, pos.y + 0.1D, z, 0, 0.0D, 1.0D, 0.0D, 0.03D);
                case THORAXIS_IN, THORAXIS_OUT -> level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, pos.y + 0.1D, z, 0, 0.0D, 1.0D, 0.0D, 0.1D);
                case CAVARYN_IN, CAVARYN_OUT -> level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundBelow(level, pos)),
                        x, pos.y + 0.1D, z, 2, 0.1D, 0.05D, 0.1D, 0.1D);
                case PRISM_IN, PRISM_OUT -> level.sendParticles(new DustParticleOptions(rainbow(state.tick * 0.02F + i * 0.5F), 1.0F),
                        x, pos.y + 0.1D, z, 0, 0.0D, 1.0D, 0.0D, 0.05D);
            }
        }
    }

    private static void arrive(ServerPlayer player, State state) {
        AntTeleportHelper.teleportPlayerWithCompanions(player, state.destination, state.arrival);
        boolean glide = false;
        switch (state.style) {
            case ELYTHIA_HOME -> {
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
            }
            case ELYTHIA_IN -> {
                if (player.isPassenger()) {
                    player.stopRiding();
                }
                glide = lift(player);
                state.gliding = glide;
            }
            default -> {
            }
        }
        arrived(player, state, glide);
    }

    private static void arrived(ServerPlayer player, State state, boolean glide) {
        state.teleported = true;
        state.tick = 0;
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();
        switch (state.style) {
            case ELYTHIA_IN -> player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
            case ELYTHIA_HOME -> player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.6F, 0.5F);
            case THORAXIS_IN, THORAXIS_OUT -> {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y + 1.0D, pos.z, 40, 0.6D, 1.0D, 0.6D, 0.05D);
                player.playNotifySound(AntarchySoundEvents.THORAXIS_DREAM_DUNES_ADDITIONS.get(), SoundSource.PLAYERS, 1.0F, 1.2F);
            }
            case CAVARYN_IN -> {
                level.sendParticles(ParticleTypes.POOF, pos.x, pos.y + 0.2D, pos.z, 20, 0.6D, 0.1D, 0.6D, 0.02D);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundBelow(level, pos)), pos.x, pos.y + 0.1D, pos.z, 30, 0.6D, 0.1D, 0.6D, 0.2D);
                player.playNotifySound(SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.0F, 0.5F);
                player.playNotifySound(AntarchySoundEvents.CAVARYN_HEARTBEAT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            case CAVARYN_OUT -> {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), pos.x, pos.y + 0.5D, pos.z, 40, 0.5D, 0.5D, 0.5D, 0.3D);
                level.sendParticles(ParticleTypes.POOF, pos.x, pos.y + 0.5D, pos.z, 15, 0.4D, 0.4D, 0.4D, 0.05D);
                player.playNotifySound(SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);
            }
            case PRISM_IN, PRISM_OUT -> {
                for (int i = 0; i < 24; i++) {
                    float hue = i / 24.0F;
                    level.sendParticles(new DustParticleOptions(rainbow(hue), 1.4F), pos.x, pos.y + 1.0D, pos.z, 2, 0.7D, 0.8D, 0.7D, 0.0D);
                }
                player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 2.0F);
            }
        }
        AntTransitSync.send(player, payload(player, state, AntTransitPayload.PHASE_ARRIVED, pos, glide));
    }

    private static boolean lift(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (player.getAbilities().flying) {
            return false;
        }
        AABB box = player.getBoundingBox();
        int top = Math.max(level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(box.minX), Mth.floor(box.minZ)),
                level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(box.maxX), Mth.floor(box.maxZ)));
        double floor = Math.max(player.getY(), top);
        double ceiling = Math.max(DROP_Y, floor + MIN_DROP_HEIGHT);
        if (ceiling > level.getMaxBuildHeight() - 4) {
            return false;
        }
        AABB column = new AABB(box.minX, floor + 1.0D, box.minZ, box.maxX, ceiling + box.getYsize(), box.maxZ);
        if (!level.noCollision(player, column) || level.containsAnyLiquid(column)) {
            return false;
        }
        player.connection.teleport(player.getX(), ceiling, player.getZ(), player.getYRot(), player.getXRot());
        player.resetFallDistance();
        return true;
    }

    private static boolean wake(ServerPlayer player, State state) {
        if (!state.sleeping) {
            return state.tick < ARRIVAL_PROTECTION_TICKS;
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
        if (!state.gliding) {
            return state.tick < ARRIVAL_PROTECTION_TICKS;
        }
        if (state.tick > MAX_GLIDE_TICKS || player.isInWater()
                || AntarchyFluidChecks.isLumen(player.level().getFluidState(player.blockPosition()))
                || player.getAbilities().flying
                || player.isFallFlying() || (state.tick > 5 && player.onGround())) {
            return false;
        }
        player.resetFallDistance();
        if (!state.poofed && heightAboveGround(player) <= POOF_HEIGHT) {
            state.poofed = true;
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() - heightAboveGround(player) + 0.2D, player.getZ(),
                    20, 0.6D, 0.05D, 0.6D, 0.08D);
        }
        return true;
    }

    private static int heightAboveGround(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos.MutableBlockPos cursor = player.blockPosition().mutable();
        int startY = cursor.getY();
        for (int depth = 0; depth <= GROUND_SCAN; depth++) {
            cursor.setY(startY - depth);
            if (cursor.getY() < level.getMinBuildHeight()
                    || !level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()
                    || !level.getFluidState(cursor).isEmpty()) {
                return depth;
            }
        }
        return GROUND_SCAN + 1;
    }

    private static BlockState groundBelow(ServerLevel level, Vec3 pos) {
        BlockState state = level.getBlockState(BlockPos.containing(pos.x, pos.y - 0.5D, pos.z));
        return state.isAir() ? Blocks.DIRT.defaultBlockState() : state;
    }

    private static void playAt(ServerPlayer player, Vec3 pos, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static Vector3f rainbow(float hue) {
        int rgb = Mth.hsvToRgb(hue - (float) Math.floor(hue), 0.85F, 1.0F);
        return new Vector3f((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
    }

    private static AntTransitPayload payload(ServerPlayer player, State state, byte phase, Vec3 position, boolean glide) {
        return new AntTransitPayload(player.getId(), state.style, phase, position.x, position.y, position.z, glide);
    }

    private static final class State {
        private final TransitStyle style;
        private final ServerLevel originLevel;
        private final ServerLevel destination;
        private final BooleanSupplier warp;
        private Vec3 origin;
        private Vec3 arrival;
        private boolean searchDone;
        private boolean waiting;
        private String warpFailureKey;
        private boolean warpRequested;
        private boolean teleported;
        private boolean sleeping;
        private boolean gliding;
        private boolean poofed;
        private int tick;
        private int landingTicks;

        private State(TransitStyle style, ServerLevel originLevel, ServerLevel destination, Vec3 origin, BooleanSupplier warp) {
            this.style = style;
            this.originLevel = originLevel;
            this.destination = destination;
            this.origin = origin;
            this.warp = warp;
        }
    }
}
