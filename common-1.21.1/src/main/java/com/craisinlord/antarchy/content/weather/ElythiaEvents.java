package com.craisinlord.antarchy.content.weather;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.OuranwoodDeerMigration;
import com.craisinlord.antarchy.content.network.ElythiaEventPayload;
import com.craisinlord.antarchy.content.network.ElythiaEventSync;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

public final class ElythiaEvents extends SavedData {
    private static final String ID = "antarchy_elythia_events";
    private static final String EVENT_KEY = "Event";
    private static final String END_KEY = "End";
    private static final String SEED_KEY = "Seed";
    private static final String FORCED_KEY = "Forced";
    private static final String LAST_NIGHT_KEY = "LastNight";
    private static final String LAST_DAY_KEY = "LastDay";
    private static final String WAS_RAINING_KEY = "WasRaining";

    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final long DAY_TICKS = 24000L;
    private static final long DUSK_TICKS = 12000L;
    private static final int MIGRATION_TICKS = 20 * 300;
    private static final int FORCED_TICKS = 20 * 300;
    private static final int RAIN_WEATHER_TICKS = 20 * 600;
    private static final SavedData.Factory<ElythiaEvents> FACTORY =
            new SavedData.Factory<>(ElythiaEvents::create, ElythiaEvents::load, null);

    private ElythiaEvent event = ElythiaEvent.NONE;
    private long endTime;
    private long seed;
    private boolean forced;
    private long lastNight = Long.MIN_VALUE;
    private long lastDay = Long.MIN_VALUE;
    private boolean wasRaining;

    private int version;
    private final Map<UUID, Integer> syncedVersions = new HashMap<>();

    public static ElythiaEvents create() {
        return new ElythiaEvents();
    }

    public static ElythiaEvents load(CompoundTag tag, HolderLookup.Provider registries) {
        ElythiaEvents data = new ElythiaEvents();
        data.event = ElythiaEvent.byId(tag.getString(EVENT_KEY));
        data.endTime = tag.getLong(END_KEY);
        data.seed = tag.getLong(SEED_KEY);
        data.forced = tag.getBoolean(FORCED_KEY);
        data.lastNight = tag.contains(LAST_NIGHT_KEY) ? tag.getLong(LAST_NIGHT_KEY) : Long.MIN_VALUE;
        data.lastDay = tag.contains(LAST_DAY_KEY) ? tag.getLong(LAST_DAY_KEY) : Long.MIN_VALUE;
        data.wasRaining = tag.getBoolean(WAS_RAINING_KEY);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString(EVENT_KEY, this.event.id());
        tag.putLong(END_KEY, this.endTime);
        tag.putLong(SEED_KEY, this.seed);
        tag.putBoolean(FORCED_KEY, this.forced);
        tag.putLong(LAST_NIGHT_KEY, this.lastNight);
        tag.putLong(LAST_DAY_KEY, this.lastDay);
        tag.putBoolean(WAS_RAINING_KEY, this.wasRaining);
        return tag;
    }

    public static ElythiaEvents get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    public static boolean isElythia(ServerLevel level) {
        return level.dimension().equals(ThrowbackZones.ELYTHIA);
    }

    public static ElythiaEvent current(ServerLevel level) {
        return isElythia(level) ? get(level).event : ElythiaEvent.NONE;
    }

    public static boolean isActive(ServerLevel level, ElythiaEvent event) {
        return current(level) == event;
    }

    public static void tick(ServerLevel level) {
        if (!isElythia(level) || level.getGameTime() % CHECK_INTERVAL_TICKS != 0L) {
            return;
        }
        ElythiaEvents data = get(level);
        data.update(level);
        data.sync(level);
    }

    public ElythiaEvent event() {
        return this.event;
    }

    public long remainingTicks(ServerLevel level) {
        return this.endTime == Long.MAX_VALUE ? -1L : Math.max(0L, this.endTime - level.getGameTime());
    }

    public Vec3 heading() {
        float angle = (float) ((this.seed >>> 11) * 0x1.0p-53 * Math.PI * 2.0D);
        return new Vec3(Mth.cos(angle), 0.0D, Mth.sin(angle));
    }

    public void start(ServerLevel level, ElythiaEvent next, boolean forced) {
        if (next == ElythiaEvent.NONE) {
            this.stop();
            return;
        }
        long timeOfDay = Math.floorMod(level.getDayTime(), DAY_TICKS);
        long duration = switch (next.window()) {
            case NIGHT -> forced ? FORCED_TICKS : DAY_TICKS - timeOfDay;
            case DAY -> forced ? MIGRATION_TICKS : Math.min(MIGRATION_TICKS, DUSK_TICKS - timeOfDay);
            case RAIN, NONE -> -1L;
        };
        if (next == ElythiaEvent.LUMEN_RAIN && !level.getLevelData().isRaining()) {
            level.getServer().overworld().setWeatherParameters(0, RAIN_WEATHER_TICKS, true, false);
        }
        this.event = next;
        this.endTime = duration < 0L ? Long.MAX_VALUE : level.getGameTime() + duration;
        this.seed = level.random.nextLong();
        this.forced = forced;
        this.changed();
        if (next == ElythiaEvent.DEER_MIGRATION) {
            OuranwoodDeerMigration.begin(level, this.heading());
        }
    }

    public void stop() {
        if (this.event == ElythiaEvent.NONE) {
            return;
        }
        this.event = ElythiaEvent.NONE;
        this.forced = false;
        this.changed();
    }

    private void update(ServerLevel level) {
        if (this.event != ElythiaEvent.NONE && this.shouldEnd(level)) {
            this.stop();
        }

        long dayTime = level.getDayTime();
        long timeOfDay = Math.floorMod(dayTime, DAY_TICKS);
        boolean night = timeOfDay >= DUSK_TICKS;
        boolean raining = level.getLevelData().isRaining();
        boolean enabled = AntarchySettings.elythiaEvents();
        if (!enabled && this.event != ElythiaEvent.NONE && !this.forced) {
            this.stop();
        }

        if (night) {
            long nightIndex = Math.floorDiv(dayTime + DUSK_TICKS, DAY_TICKS);
            if (nightIndex != this.lastNight) {
                this.lastNight = nightIndex;
                this.setDirty();
                if (enabled && this.event == ElythiaEvent.NONE) {
                    this.roll(level, ElythiaEvent.Window.NIGHT);
                }
            }
        } else {
            long dayIndex = Math.floorDiv(dayTime, DAY_TICKS);
            if (dayIndex != this.lastDay) {
                this.lastDay = dayIndex;
                this.setDirty();
                if (enabled && this.event == ElythiaEvent.NONE) {
                    this.roll(level, ElythiaEvent.Window.DAY);
                }
            }
        }

        if (raining != this.wasRaining) {
            this.wasRaining = raining;
            this.setDirty();
            if (raining && enabled && (this.event == ElythiaEvent.NONE || this.event.window() == ElythiaEvent.Window.NIGHT)) {
                this.roll(level, ElythiaEvent.Window.RAIN);
            }
        }
    }

    private boolean shouldEnd(ServerLevel level) {
        if (level.getGameTime() >= this.endTime) {
            return true;
        }
        if (this.event == ElythiaEvent.LUMEN_RAIN) {
            return !level.getLevelData().isRaining();
        }
        if (this.forced) {
            return false;
        }
        boolean night = Math.floorMod(level.getDayTime(), DAY_TICKS) >= DUSK_TICKS;
        return switch (this.event.window()) {
            case NIGHT -> !night;
            case DAY -> night;
            default -> false;
        };
    }

    private void roll(ServerLevel level, ElythiaEvent.Window window) {
        int roll = level.random.nextInt(100);
        for (ElythiaEvent candidate : ElythiaEvent.values()) {
            if (candidate.window() != window) {
                continue;
            }
            roll -= candidate.chance();
            if (roll < 0) {
                this.start(level, candidate, false);
                return;
            }
        }
    }

    private void changed() {
        this.version++;
        this.setDirty();
    }

    private void sync(ServerLevel level) {
        ElythiaEventPayload payload = null;
        int players = 0;
        for (ServerPlayer player : level.players()) {
            players++;
            Integer synced = this.syncedVersions.get(player.getUUID());
            if (synced != null && synced == this.version) {
                continue;
            }
            if (payload == null) {
                payload = new ElythiaEventPayload(this.event.ordinal(), this.seed);
            }
            ElythiaEventSync.send(player, payload);
            this.syncedVersions.put(player.getUUID(), this.version);
        }
        if (this.syncedVersions.size() > players) {
            this.syncedVersions.keySet().removeIf(uuid -> level.getPlayerByUUID(uuid) == null);
        }
    }
}
