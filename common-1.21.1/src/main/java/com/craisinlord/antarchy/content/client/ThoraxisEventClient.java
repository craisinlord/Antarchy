package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.network.ThoraxisBubbleUpdatePayload;
import com.craisinlord.antarchy.content.network.ThoraxisEventPayload;
import com.craisinlord.antarchy.content.weather.ThoraxisBubble;
import com.craisinlord.antarchy.content.weather.ThoraxisEvent;
import com.craisinlord.antarchy.content.weather.ThoraxisEventMath;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class ThoraxisEventClient {
    public static final int RAIN_RADIUS = 10;
    private static final int RAIN_SIZE = RAIN_RADIUS * 2 + 1;
    private static final int RAIN_FLOOR_SEARCH = 12;
    private static final int RAIN_HEIGHT = 28;
    private static final int RAIN_REFRESH_TICKS = 4;
    private static final int RAIN_FULL_REFRESH_TICKS = 20;
    private static final int EXPOSURE_DISTANCE = 24;
    private static final float FADE_STEP = 0.025F;
    private static final double BLIND_FOG_END = 40.0D;
    private static final DustParticleOptions ANTIWATER_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.12F, 0.12F), 1.0F);
    private static final float[] BLIND_FOG_COLOR = {0.02F, 0.0F, 0.0F};
    private static final float[] FOG_COLOR = new float[3];
    private static final int SMOOTH_TICKS = 4;
    private static final double MAX_SMOOTH_DISTANCE = 8.0D;
    private static final int SQUASH_TICKS = 8;
    private static final int IMPACT_PARTICLES = 14;
    private static final double IMPACT_SOUND_DISTANCE = 48.0D;
    private static final Map<UUID, Correction> corrections = new HashMap<>();
    private static final Map<UUID, Long> impacts = new HashMap<>();

    private record Correction(Vec3 offset, long start) {
    }

    private static ClientLevel trackedLevel;
    private static ThoraxisEvent active = ThoraxisEvent.NONE;
    private static ThoraxisEvent.Phase phase = ThoraxisEvent.Phase.NONE;
    private static long phaseStart;
    private static long phaseEnd;
    private static long activeSeed;
    private static List<ThoraxisBubble> bubbles = List.of();

    private static ThoraxisEvent shown = ThoraxisEvent.NONE;
    private static long shownSeed;
    private static float omen;
    private static float previousOmen;
    private static float strength;
    private static float previousStrength;
    private static float exposure;
    private static float previousExposure;
    private static boolean underside;

    private static final float[] rainLow = new float[RAIN_SIZE * RAIN_SIZE];
    private static final float[] rainHigh = new float[RAIN_SIZE * RAIN_SIZE];
    private static final boolean[] rainValid = new boolean[RAIN_SIZE * RAIN_SIZE];
    private static int rainCenterX;
    private static int rainCenterY;
    private static int rainCenterZ;
    private static int rainDirection = 1;
    private static int rainScanDirection;
    private static int rainAge = RAIN_FULL_REFRESH_TICKS;

    private ThoraxisEventClient() {
    }

    public static void handle(ThoraxisEventPayload payload) {
        if (payload.seed() == activeSeed) {
            correct(payload.bubbles());
        } else {
            corrections.clear();
            impacts.clear();
        }
        active = ThoraxisEvent.byOrdinal(payload.event());
        phase = ThoraxisEvent.Phase.values()[Mth.clamp(payload.phase(), 0, ThoraxisEvent.Phase.values().length - 1)];
        phaseStart = payload.phaseStart();
        phaseEnd = payload.phaseEnd();
        activeSeed = payload.seed();
        bubbles = List.copyOf(payload.bubbles());
    }

    public static void handleUpdate(ThoraxisBubbleUpdatePayload payload) {
        if (payload.seed() != activeSeed || bubbles.isEmpty()) {
            return;
        }
        correct(payload.bubbles());
        Map<UUID, ThoraxisBubble> updates = new HashMap<>();
        for (ThoraxisBubble bubble : payload.bubbles()) {
            updates.put(bubble.id(), bubble);
        }
        List<ThoraxisBubble> next = new ArrayList<>(bubbles.size());
        for (ThoraxisBubble bubble : bubbles) {
            next.add(updates.getOrDefault(bubble.id(), bubble));
        }
        bubbles = List.copyOf(next);
    }

    private static void correct(List<ThoraxisBubble> incoming) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || bubbles.isEmpty()) {
            return;
        }
        long time = level.getGameTime();
        Map<UUID, ThoraxisBubble> previous = new HashMap<>();
        for (ThoraxisBubble bubble : bubbles) {
            previous.put(bubble.id(), bubble);
        }
        for (ThoraxisBubble bubble : incoming) {
            ThoraxisBubble old = previous.get(bubble.id());
            if (old == null || old.equals(bubble)) {
                continue;
            }
            Vec3 offset = bubblePosition(old, time).subtract(bubble.position(time));
            if (offset.lengthSqr() < MAX_SMOOTH_DISTANCE * MAX_SMOOTH_DISTANCE) {
                corrections.put(bubble.id(), new Correction(offset, time));
            }
            if (bubble.impact() && bubble.segment() != old.segment()) {
                impacts.put(bubble.id(), time);
                impactEffects(minecraft, level, bubble, time);
            }
        }
    }

    private static void impactEffects(Minecraft minecraft, ClientLevel level, ThoraxisBubble bubble, long time) {
        LocalPlayer player = minecraft.player;
        float presence = bubble.presence(time) * bubbleVisibility(1.0F);
        if (player == null || presence <= 0.05F) {
            return;
        }
        Vec3 center = bubblePosition(bubble, time);
        double radius = bubble.radius();
        Vec3 contact = center.add(0.0D, bubble.down() * radius * 0.9D, 0.0D);
        if (contact.distanceToSqr(player.position()) > IMPACT_SOUND_DISTANCE * IMPACT_SOUND_DISTANCE) {
            return;
        }
        RandomSource random = level.random;
        double spread = bubble.slow() ? 0.02D : 0.07D;
        for (int i = 0; i < IMPACT_PARTICLES; i++) {
            double angle = Mth.TWO_PI * i / IMPACT_PARTICLES + random.nextDouble() * 0.3D;
            double ox = Math.cos(angle);
            double oz = Math.sin(angle);
            level.addParticle(bubble.slow() ? AntarchyObjects.TEMPORAL_GLINT_SLOW.get() : AntarchyObjects.TEMPORAL_GLINT_FAST.get(),
                    contact.x + ox * radius * 0.6D, contact.y - bubble.down() * 0.2D, contact.z + oz * radius * 0.6D,
                    ox * spread, -bubble.down() * 0.02D, oz * spread);
        }
        level.playLocalSound(contact.x, contact.y, contact.z, SoundEvents.AMETHYST_CLUSTER_FALL, SoundSource.AMBIENT,
                0.7F * presence, bubble.slow() ? 0.5F : 1.1F + random.nextFloat() * 0.2F, false);
    }

    public static Vec3 bubblePosition(ThoraxisBubble bubble, double time) {
        Vec3 position = bubble.position(time);
        Correction correction = corrections.get(bubble.id());
        if (correction == null) {
            return position;
        }
        double weight = 1.0D - (time - correction.start()) / SMOOTH_TICKS;
        return weight <= 0.0D ? position : position.add(correction.offset().scale(Math.min(1.0D, weight)));
    }

    public static float bubbleSquash(ThoraxisBubble bubble, double time) {
        Long start = impacts.get(bubble.id());
        if (start == null) {
            return 0.0F;
        }
        float progress = (float) ((time - start) / SQUASH_TICKS);
        return progress < 0.0F || progress >= 1.0F ? 0.0F : Mth.sin(progress * Mth.PI) * (1.0F - progress * 0.5F);
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level != trackedLevel) {
            trackedLevel = level;
            reset();
        }
        if (level == null || player == null || !ThoraxisEventMath.isThoraxis(level)) {
            active = ThoraxisEvent.NONE;
            bubbles = List.of();
        }
        if (level == null || player == null) {
            ThoraxisEventMath.setClientGravityScale(1.0F, 1.0F);
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }

        long time = level.getGameTime();
        corrections.values().removeIf(correction -> time - correction.start() > SMOOTH_TICKS);
        impacts.values().removeIf(start -> time - start > SQUASH_TICKS);
        float targetOmen = 0.0F;
        float targetStrength = 0.0F;
        if (active == shown && activeSeed == shownSeed && active != ThoraxisEvent.NONE) {
            targetOmen = ThoraxisEventMath.omen(phase, phaseStart, phaseEnd, time);
            targetStrength = ThoraxisEventMath.intensity(phase, phaseStart, phaseEnd, time);
        }
        previousOmen = omen;
        previousStrength = strength;
        omen = approach(omen, targetOmen, FADE_STEP);
        strength = approach(strength, targetStrength, FADE_STEP);
        if ((active != shown || activeSeed != shownSeed) && omen <= 0.0F && strength <= 0.0F) {
            shown = active;
            shownSeed = activeSeed;
        }

        underside = ThoraxisUndersideManager.isInUnderside(player);
        rainDirection = underside ? -1 : 1;
        previousExposure = exposure;
        float exposureTarget = shown != ThoraxisEvent.NONE && exposedToSky(level, player) ? 1.0F : 0.0F;
        exposure = approach(exposure, exposureTarget, 0.2F);

        float gravity = shown == ThoraxisEvent.WEIGHTLESS ? Mth.lerp(strength, 1.0F, ThoraxisEventMath.WEIGHTLESS_GRAVITY) : 1.0F;
        float localGravity = shown == ThoraxisEvent.UPWARD_RAIN ? Mth.lerp(strength * exposure, 1.0F, ThoraxisEventMath.RAIN_GRAVITY) : 1.0F;
        ThoraxisEventMath.setClientGravityScale(gravity, localGravity);

        if (shown == ThoraxisEvent.UPWARD_RAIN && (omen > 0.0F || strength > 0.0F)) {
            BlockPos center = player.blockPosition();
            rainAge++;
            boolean moved = center.getX() != rainCenterX || center.getY() != rainCenterY || center.getZ() != rainCenterZ
                    || rainDirection != rainScanDirection;
            if (rainAge >= RAIN_FULL_REFRESH_TICKS || moved && rainAge >= RAIN_REFRESH_TICKS) {
                rainAge = 0;
                refreshRainColumns(level, center);
            }
        }
        tickAmbience(minecraft, level, player, time);
    }

    private static void reset() {
        active = ThoraxisEvent.NONE;
        phase = ThoraxisEvent.Phase.NONE;
        bubbles = List.of();
        corrections.clear();
        impacts.clear();
        shown = ThoraxisEvent.NONE;
        omen = 0.0F;
        previousOmen = 0.0F;
        strength = 0.0F;
        previousStrength = 0.0F;
        exposure = 0.0F;
        previousExposure = 0.0F;
        rainAge = RAIN_FULL_REFRESH_TICKS;
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(target, value + step) : Math.max(target, value - step);
    }

    private static boolean exposedToSky(ClientLevel level, LocalPlayer player) {
        int direction = underside ? -1 : 1;
        BlockPos.MutableBlockPos cursor = BlockPos.containing(player.getEyePosition()).mutable();
        for (int i = 0; i < EXPOSURE_DISTANCE; i++) {
            cursor.move(0, direction, 0);
            if (level.isOutsideBuildHeight(cursor)) {
                return true;
            }
            BlockState state = level.getBlockState(cursor);
            if (state.blocksMotion() || state.canOcclude()) {
                return false;
            }
        }
        return true;
    }

    private static void refreshRainColumns(ClientLevel level, BlockPos center) {
        rainCenterX = center.getX();
        rainCenterY = center.getY();
        rainCenterZ = center.getZ();
        rainScanDirection = rainDirection;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RAIN_RADIUS; dx <= RAIN_RADIUS; dx++) {
            for (int dz = -RAIN_RADIUS; dz <= RAIN_RADIUS; dz++) {
                int index = (dx + RAIN_RADIUS) * RAIN_SIZE + dz + RAIN_RADIUS;
                if (dx * dx + dz * dz > RAIN_RADIUS * RAIN_RADIUS) {
                    rainValid[index] = false;
                    continue;
                }
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int floor = center.getY() - rainDirection * RAIN_FLOOR_SEARCH;
                cursor.set(x, center.getY(), z);
                for (int i = 0; i < RAIN_FLOOR_SEARCH; i++) {
                    if (level.getBlockState(cursor).blocksMotion()) {
                        floor = cursor.getY() + (rainDirection > 0 ? 1 : 0);
                        break;
                    }
                    cursor.move(0, -rainDirection, 0);
                }
                int ceiling = floor + rainDirection * RAIN_HEIGHT;
                cursor.set(x, floor, z);
                for (int i = 0; i < RAIN_HEIGHT; i++) {
                    if (level.getBlockState(cursor).blocksMotion()) {
                        ceiling = cursor.getY() + (rainDirection > 0 ? 0 : 1);
                        break;
                    }
                    cursor.move(0, rainDirection, 0);
                }
                rainValid[index] = ceiling != floor;
                rainLow[index] = Math.min(floor, ceiling);
                rainHigh[index] = Math.max(floor, ceiling);
            }
        }
    }

    private static void tickAmbience(Minecraft minecraft, ClientLevel level, LocalPlayer player, long time) {
        float presence = Math.max(omen, strength);
        if (presence <= 0.0F) {
            return;
        }
        RandomSource random = level.random;
        Vec3 origin = player.position();
        switch (shown) {
            case WEIGHTLESS -> {
                if (phase == ThoraxisEvent.Phase.WARNING && random.nextInt(80) == 0) {
                    level.playLocalSound(origin.x, origin.y, origin.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                            0.8F, 0.5F + random.nextFloat() * 0.2F, false);
                }
            }
            case TEMPORAL_BUBBLES -> {
                double now = time;
                for (ThoraxisBubble bubble : bubbles) {
                    Vec3 center = bubblePosition(bubble, now);
                    if (center.distanceToSqr(origin) > 64.0D * 64.0D || random.nextFloat() > 0.6F * bubble.presence(now)) {
                        continue;
                    }
                    double ox = random.nextGaussian();
                    double oy = random.nextGaussian();
                    double oz = random.nextGaussian();
                    double length = Math.sqrt(ox * ox + oy * oy + oz * oz);
                    if (length < 1.0E-4D) {
                        continue;
                    }
                    ox /= length;
                    oy /= length;
                    oz /= length;
                    double radius = bubble.radius();
                    double drift = bubble.slow() ? 0.005D : 0.04D;
                    level.addParticle(bubble.slow() ? AntarchyObjects.TEMPORAL_GLINT_SLOW.get() : AntarchyObjects.TEMPORAL_GLINT_FAST.get(),
                            center.x + ox * radius, center.y + oy * radius, center.z + oz * radius, ox * drift, oy * drift, oz * drift);
                }
                if (phase == ThoraxisEvent.Phase.WARNING && random.nextInt(100) == 0) {
                    level.playLocalSound(origin.x, origin.y, origin.z, SoundEvents.BEACON_AMBIENT, SoundSource.AMBIENT,
                            1.2F, 0.5F, false);
                }
            }
            case UPWARD_RAIN -> {
                if (strength > 0.0F) {
                    int splashes = Mth.ceil(strength * 4.0F);
                    for (int i = 0; i < splashes; i++) {
                        int dx = random.nextInt(RAIN_SIZE) - RAIN_RADIUS;
                        int dz = random.nextInt(RAIN_SIZE) - RAIN_RADIUS;
                        int index = (dx + RAIN_RADIUS) * RAIN_SIZE + dz + RAIN_RADIUS;
                        if (!rainValid[index]) {
                            continue;
                        }
                        double y = rainDirection > 0 ? rainHigh[index] : rainLow[index];
                        level.addParticle(ANTIWATER_DUST,
                                rainCenterX + dx + random.nextDouble(), y, rainCenterZ + dz + random.nextDouble(), 0.0D, 0.0D, 0.0D);
                    }
                    if (random.nextInt(12) == 0) {
                        level.playLocalSound(origin.x, origin.y, origin.z, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER,
                                0.3F * strength, 0.55F, false);
                    }
                } else if (random.nextInt(90) == 0) {
                    level.playLocalSound(origin.x, origin.y, origin.z, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.AMBIENT,
                            1.0F, 0.6F, false);
                }
            }
            case BLIND_SKY -> {
                if (underside && phase == ThoraxisEvent.Phase.WARNING && random.nextInt(120) == 0) {
                    level.playLocalSound(origin.x, origin.y, origin.z, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
                            0.8F, 0.6F, false);
                }
            }
            default -> {
            }
        }
    }

    public static ThoraxisEvent shown() {
        return shown;
    }

    public static long seed() {
        return shownSeed;
    }

    public static boolean underside() {
        return underside;
    }

    public static List<ThoraxisBubble> bubbles() {
        return shown == ThoraxisEvent.TEMPORAL_BUBBLES ? bubbles : List.of();
    }

    public static float omen(float partialTick) {
        return Mth.lerp(partialTick, previousOmen, omen);
    }

    public static float strength(float partialTick) {
        return Mth.lerp(partialTick, previousStrength, strength);
    }

    public static float omen(ThoraxisEvent event, float partialTick) {
        return shown == event ? omen(partialTick) : 0.0F;
    }

    public static float strength(ThoraxisEvent event, float partialTick) {
        return shown == event ? strength(partialTick) : 0.0F;
    }

    public static float exposure(float partialTick) {
        return Mth.lerp(partialTick, previousExposure, exposure);
    }

    public static float bubbleVisibility(float partialTick) {
        return Math.max(omen(ThoraxisEvent.TEMPORAL_BUBBLES, partialTick), strength(ThoraxisEvent.TEMPORAL_BUBBLES, partialTick));
    }

    public static float rainStrength(float partialTick) {
        if (shown != ThoraxisEvent.UPWARD_RAIN) {
            return 0.0F;
        }
        return Math.max(strength(partialTick), omen(partialTick) * 0.15F);
    }

    public static int rainDirection() {
        return rainDirection;
    }

    public static int rainCenterX() {
        return rainCenterX;
    }

    public static int rainCenterZ() {
        return rainCenterZ;
    }

    public static boolean rainValid(int index) {
        return rainValid[index];
    }

    public static float rainLow(int index) {
        return rainLow[index];
    }

    public static float rainHigh(int index) {
        return rainHigh[index];
    }

    public static float eyeOpenness(double time, float partialTick) {
        float open = 1.0F;
        if (shown == ThoraxisEvent.BLIND_SKY) {
            float droop = 0.55F + 0.45F * ThoraxisEventMath.eyeOpenness(shownSeed, time * 0.5D);
            open = Mth.lerp(omen(partialTick), 1.0F, droop);
            open = Mth.lerp(strength(partialTick), open, 0.0F);
        }
        return open;
    }

    public static float eyeStarBrightness(int group, double time, float partialTick) {
        if (shown == ThoraxisEvent.BLIND_SKY) {
            return Mth.lerp(Math.max(omen(partialTick) * 0.5F, strength(partialTick)), 1.0F, 0.05F);
        }
        return 1.0F;
    }

    public static float blindSkyDarkness() {
        if (!underside || shown != ThoraxisEvent.BLIND_SKY) {
            return 0.0F;
        }
        return strength(1.0F) * 0.85F;
    }

    public static double fogEnd(double end, float partialTick) {
        if (shown == ThoraxisEvent.BLIND_SKY && underside) {
            float blind = Math.max(omen(partialTick) * 0.3F, strength(partialTick));
            return end * Math.pow(BLIND_FOG_END / end, blind);
        }
        return end;
    }

    public static float[] fogColor(float red, float green, float blue, float partialTick) {
        if (shown == ThoraxisEvent.BLIND_SKY && underside) {
            return blend(red, green, blue, BLIND_FOG_COLOR, strength(partialTick));
        }
        return null;
    }

    private static float[] blend(float red, float green, float blue, float[] target, float amount) {
        FOG_COLOR[0] = Mth.lerp(amount, red, target[0]);
        FOG_COLOR[1] = Mth.lerp(amount, green, target[1]);
        FOG_COLOR[2] = Mth.lerp(amount, blue, target[2]);
        return FOG_COLOR;
    }
}
