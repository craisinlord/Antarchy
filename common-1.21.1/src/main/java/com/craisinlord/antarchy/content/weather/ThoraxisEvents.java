package com.craisinlord.antarchy.content.weather;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.network.ThoraxisBubbleUpdatePayload;
import com.craisinlord.antarchy.content.network.ThoraxisEventPayload;
import com.craisinlord.antarchy.content.network.ThoraxisEventSync;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.craisinlord.anthology.time.TimeDilationFieldEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ThoraxisEvents extends SavedData {
    private static final String ID = "antarchy_thoraxis_events";

    private static final int ROLL_INTERVAL_TICKS = 20 * 60 * 15;
    private static final int ROLL_CHANCE_PERCENT = 60;
    private static final int ARRIVAL_DELAY_TICKS = 20 * 120;
    private static final int MIN_WARNING_TICKS = 20 * 30;
    private static final int MAX_WARNING_TICKS = 20 * 60;
    private static final int FORCED_WARNING_TICKS = 20 * 10;
    private static final int MIN_ACTIVE_TICKS = 20 * 180;
    private static final int MAX_ACTIVE_TICKS = 20 * 360;

    private static final int BUBBLE_SPAWN_INTERVAL_TICKS = 20;
    private static final int MAX_SPAWNS_PER_INTERVAL = 3;
    private static final int MAX_BUBBLES_NEAR_PLAYER = 12;
    private static final int MAX_BUBBLES_PER_PLAYER = 12;
    private static final int MAX_BUBBLES = 64;
    private static final double BUBBLE_NEAR_RADIUS = 96.0D;
    private static final double BUBBLE_RECYCLE_DISTANCE = 160.0D;
    private static final double MIN_BUBBLE_DISTANCE = 24.0D;
    private static final double MAX_BUBBLE_DISTANCE = 96.0D;
    private static final double MIN_BUBBLE_GAP = 6.0D;
    private static final int SPAWN_ATTEMPTS = 6;
    private static final double HEADING_SPREAD = 0.6D;
    private static final int HOMING_CHANCE_PERCENT = 25;
    private static final double MIN_BUBBLE_SPEED = 0.035D;
    private static final double MAX_BUBBLE_SPEED = 0.07D;
    private static final double MAX_DRIFT_SPEED = 0.1D;
    private static final double GROUND_SEARCH_OFFSET = 12.0D;
    private static final int GROUND_SCAN_UP = 24;
    private static final int GROUND_SCAN_DOWN = 64;
    private static final double MIN_SPAWN_CLEARANCE = 2.0D;
    private static final double MAX_SPAWN_CLEARANCE = 7.0D;
    private static final int MIN_BUBBLE_LIFE = 20 * 50;
    private static final int MAX_BUBBLE_LIFE = 20 * 100;
    private static final int BOUNCE_COOLDOWN_TICKS = 4;
    private static final double RESTITUTION = 0.85D;
    private static final double MIN_HOP_SPEED = 0.19D;
    private static final double MAX_HOP_SPEED = 0.27D;
    private static final double CEILING_RESTITUTION = 0.4D;
    private static final double SLOPE_PUSH = 0.025D;
    private static final float MIN_BUBBLE_RADIUS = 4.5F;
    private static final float MAX_BUBBLE_RADIUS = 8.0F;
    private static final float SLOW_BUBBLE_RATE = 0.12F;
    private static final float FAST_BUBBLE_RATE = 3.0F;

    private static final int SURGE_INTERVAL_TICKS = 100;
    private static final double SURGE_RADIUS = 40.0D;
    private static final int BLIND_SKY_MONSTER_CAP = 10;
    private static final int BLIND_SKY_SPAWN_ATTEMPTS = 2;

    private static final Set<UUID> HIDDEN_FIELDS = ConcurrentHashMap.newKeySet();
    private static final SavedData.Factory<ThoraxisEvents> FACTORY =
            new SavedData.Factory<>(ThoraxisEvents::create, ThoraxisEvents::load, null);

    private ThoraxisEvent event = ThoraxisEvent.NONE;
    private ThoraxisEvent.Phase phase = ThoraxisEvent.Phase.NONE;
    private long phaseStart;
    private long phaseEnd;
    private long seed;
    private long nextRoll = -1L;
    private final List<ThoraxisBubble> bubbles = new ArrayList<>();
    private final Map<UUID, UUID> bubbleFields = new HashMap<>();

    private int version;
    private final Map<UUID, Integer> syncedVersions = new HashMap<>();

    public static ThoraxisEvents create() {
        HIDDEN_FIELDS.clear();
        return new ThoraxisEvents();
    }

    public static ThoraxisEvents load(CompoundTag tag, HolderLookup.Provider registries) {
        HIDDEN_FIELDS.clear();
        ThoraxisEvents data = new ThoraxisEvents();
        data.event = ThoraxisEvent.byId(tag.getString("Event"));
        data.phase = data.event == ThoraxisEvent.NONE ? ThoraxisEvent.Phase.NONE
                : tag.getBoolean("Active") ? ThoraxisEvent.Phase.ACTIVE : ThoraxisEvent.Phase.WARNING;
        data.phaseStart = tag.getLong("PhaseStart");
        data.phaseEnd = tag.getLong("PhaseEnd");
        data.seed = tag.getLong("Seed");
        data.nextRoll = tag.contains("NextRoll") ? tag.getLong("NextRoll") : -1L;
        for (Tag raw : tag.getList("Bubbles", Tag.TAG_COMPOUND)) {
            CompoundTag bubbleTag = (CompoundTag) raw;
            ThoraxisBubble bubble = ThoraxisBubble.load(bubbleTag);
            data.bubbles.add(bubble);
            if (bubbleTag.hasUUID("Field")) {
                UUID field = bubbleTag.getUUID("Field");
                data.bubbleFields.put(bubble.id(), field);
                HIDDEN_FIELDS.add(field);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("Event", this.event.id());
        tag.putBoolean("Active", this.phase == ThoraxisEvent.Phase.ACTIVE);
        tag.putLong("PhaseStart", this.phaseStart);
        tag.putLong("PhaseEnd", this.phaseEnd);
        tag.putLong("Seed", this.seed);
        tag.putLong("NextRoll", this.nextRoll);
        ListTag list = new ListTag();
        for (ThoraxisBubble bubble : this.bubbles) {
            CompoundTag bubbleTag = bubble.save();
            UUID field = this.bubbleFields.get(bubble.id());
            if (field != null) {
                bubbleTag.putUUID("Field", field);
            }
            list.add(bubbleTag);
        }
        tag.put("Bubbles", list);
        return tag;
    }

    public static ThoraxisEvents get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    public static boolean isHiddenField(Entity field) {
        return HIDDEN_FIELDS.contains(field.getUUID());
    }

    public static void tick(ServerLevel level) {
        if (!ThoraxisEventMath.isThoraxis(level)) {
            return;
        }
        get(level).tickLevel(level);
    }

    public ThoraxisEvent event() {
        return this.event;
    }

    public ThoraxisEvent.Phase phase() {
        return this.phase;
    }

    public long remainingTicks(ServerLevel level) {
        return Math.max(0L, this.phaseEnd - level.getGameTime());
    }

    public void start(ServerLevel level, ThoraxisEvent next, boolean skipWarning) {
        this.stop(level);
        if (next == ThoraxisEvent.NONE) {
            return;
        }
        long time = level.getGameTime();
        this.event = next;
        this.seed = level.random.nextLong();
        this.phase = ThoraxisEvent.Phase.WARNING;
        this.phaseStart = time;
        this.phaseEnd = time + (skipWarning ? FORCED_WARNING_TICKS : Mth.nextInt(level.random, MIN_WARNING_TICKS, MAX_WARNING_TICKS));
        this.changed();
    }

    public void stop(ServerLevel level) {
        if (this.event == ThoraxisEvent.NONE) {
            return;
        }
        for (UUID field : this.bubbleFields.values()) {
            Entity entity = level.getEntity(field);
            if (entity != null) {
                entity.discard();
            }
            HIDDEN_FIELDS.remove(field);
        }
        this.bubbles.clear();
        this.bubbleFields.clear();
        this.event = ThoraxisEvent.NONE;
        this.phase = ThoraxisEvent.Phase.NONE;
        ThoraxisEventMath.setServerGravityScale(1.0F);
        this.changed();
    }

    private void tickLevel(ServerLevel level) {
        long time = level.getGameTime();
        float intensity = ThoraxisEventMath.intensity(this.phase, this.phaseStart, this.phaseEnd, time);
        ThoraxisEventMath.setServerGravityScale(this.event == ThoraxisEvent.WEIGHTLESS
                ? Mth.lerp(intensity, 1.0F, ThoraxisEventMath.WEIGHTLESS_GRAVITY) : 1.0F);

        if (this.event == ThoraxisEvent.TEMPORAL_BUBBLES) {
            this.tickBubbles(level, time);
        }
        if (time % 20L == 0L) {
            this.update(level, time);
        }
        if (time % SURGE_INTERVAL_TICKS == 0L && this.phase == ThoraxisEvent.Phase.ACTIVE) {
            this.tickSurges(level);
        }
        if (time % 20L == 0L) {
            this.sync(level);
        }
    }

    private void update(ServerLevel level, long time) {
        if (this.nextRoll < 0L) {
            this.nextRoll = time + ROLL_INTERVAL_TICKS;
            this.setDirty();
        }
        if (this.phase == ThoraxisEvent.Phase.WARNING && time >= this.phaseEnd) {
            this.phase = ThoraxisEvent.Phase.ACTIVE;
            this.phaseStart = time;
            this.phaseEnd = time + Mth.nextInt(level.random, MIN_ACTIVE_TICKS, MAX_ACTIVE_TICKS);
            this.applyBubbleRates(level);
            this.changed();
        } else if (this.phase == ThoraxisEvent.Phase.ACTIVE && time >= this.phaseEnd) {
            this.stop(level);
        }

        if (level.players().isEmpty()) {
            if (this.nextRoll < time + ARRIVAL_DELAY_TICKS) {
                this.nextRoll = time + ARRIVAL_DELAY_TICKS;
                this.setDirty();
            }
            return;
        }
        if (this.event != ThoraxisEvent.NONE || time < this.nextRoll) {
            return;
        }
        this.nextRoll = time + ROLL_INTERVAL_TICKS;
        this.setDirty();
        if (!AntarchySettings.thoraxisEvents() || level.random.nextInt(100) >= ROLL_CHANCE_PERCENT) {
            return;
        }
        this.roll(level);
    }

    private void roll(ServerLevel level) {
        List<ThoraxisEvent> candidates = new ArrayList<>();
        int total = 0;
        for (ThoraxisEvent candidate : ThoraxisEvent.values()) {
            if (candidate.weight() > 0 && this.anyPlayerInScope(level, candidate.scope())) {
                candidates.add(candidate);
                total += candidate.weight();
            }
        }
        if (total <= 0) {
            return;
        }
        int roll = level.random.nextInt(total);
        for (ThoraxisEvent candidate : candidates) {
            roll -= candidate.weight();
            if (roll < 0) {
                this.start(level, candidate, false);
                return;
            }
        }
    }

    private boolean anyPlayerInScope(ServerLevel level, ThoraxisEvent.Scope scope) {
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && inScope(level, player, scope)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inScope(ServerLevel level, ServerPlayer player, ThoraxisEvent.Scope scope) {
        return switch (scope) {
            case ANYWHERE -> true;
            case UNDERSIDE -> ThoraxisUndersideManager.isInUnderside(player);
        };
    }

    private void tickBubbles(ServerLevel level, long time) {
        boolean changed = false;
        List<ThoraxisBubble> updated = new ArrayList<>();
        for (int i = this.bubbles.size() - 1; i >= 0; i--) {
            ThoraxisBubble bubble = this.bubbles.get(i);
            UUID fieldId = this.bubbleFields.get(bubble.id());
            if (bubble.expired(time)) {
                if (fieldId != null) {
                    Entity expiredField = level.getEntity(fieldId);
                    if (expiredField != null) {
                        expiredField.discard();
                    }
                    HIDDEN_FIELDS.remove(fieldId);
                }
                this.bubbleFields.remove(bubble.id());
                this.bubbles.remove(i);
                changed = true;
                continue;
            }
            ThoraxisBubble moved = !bubble.fading(time) && farFromPlayers(level, bubble.position(time))
                    ? bubble.withLife((int) (time - bubble.born()) + ThoraxisBubble.FADE_TICKS)
                    : bounce(level, bubble, time);
            if (moved != bubble) {
                bubble = moved;
                this.bubbles.set(i, bubble);
                updated.add(bubble);
            }
            Vec3 position = bubble.position(time);
            Entity field = fieldId == null ? null : level.getEntity(fieldId);
            if (field != null) {
                field.setPos(position);
            } else if (level.isLoaded(BlockPos.containing(position))) {
                this.createField(level, bubble, position, time);
            }
        }

        boolean spawning = this.phase == ThoraxisEvent.Phase.ACTIVE
                || this.phase == ThoraxisEvent.Phase.WARNING && time >= (this.phaseStart + this.phaseEnd) / 2L;
        if (spawning && time % BUBBLE_SPAWN_INTERVAL_TICKS == 0L) {
            int cap = Math.min(MAX_BUBBLES, MAX_BUBBLES_PER_PLAYER * Math.max(1, level.players().size()));
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator()) {
                    continue;
                }
                int spawns = Math.min(MAX_SPAWNS_PER_INTERVAL, MAX_BUBBLES_NEAR_PLAYER - this.bubblesNear(player.position(), time));
                for (int i = 0; i < spawns && this.bubbles.size() < cap; i++) {
                    changed |= this.spawnBubble(level, player, time);
                }
            }
        }
        if (!updated.isEmpty()) {
            this.setDirty();
            this.sendUpdates(level, updated);
        }
        if (changed) {
            this.changed();
        }
    }

    private static boolean farFromPlayers(ServerLevel level, Vec3 position) {
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            double dx = player.getX() - position.x;
            double dz = player.getZ() - position.z;
            if (dx * dx + dz * dz < BUBBLE_RECYCLE_DISTANCE * BUBBLE_RECYCLE_DISTANCE) {
                return false;
            }
        }
        return true;
    }

    private static ThoraxisBubble bounce(ServerLevel level, ThoraxisBubble bubble, long time) {
        Vec3 center = bubble.position(time);
        if (!level.isLoaded(BlockPos.containing(center))) {
            return bubble;
        }
        double down = bubble.down();
        if (ThoraxisBubble.down(center.y) != down) {
            return bubble.rebase(time, Vec3.ZERO, bubble.vx(), bubble.verticalVelocity(time), bubble.vz(), false);
        }
        if (time - bubble.segment() < BOUNCE_COOLDOWN_TICKS) {
            return bubble;
        }
        RandomSource random = level.random;
        double radius = bubble.radius();
        double fall = bubble.verticalVelocity(time) * down;
        if (blocked(level, center)) {
            return bubble.rebase(time, new Vec3(0.0D, -down, 0.0D), bubble.vx(), -down * MAX_HOP_SPEED, bubble.vz(), false);
        }
        if (fall > 0.0D) {
            boolean hit = blocked(level, center.add(0.0D, down * radius * 0.9D, 0.0D));
            double pushX = 0.0D;
            double pushZ = 0.0D;
            for (int k = 0; k < 4; k++) {
                double angle = Math.PI * 0.25D + k * Math.PI * 0.5D;
                double ox = Math.cos(angle) * radius * 0.6D;
                double oz = Math.sin(angle) * radius * 0.6D;
                if (blocked(level, center.add(ox, down * radius * 0.75D, oz))) {
                    hit = true;
                    pushX -= ox;
                    pushZ -= oz;
                }
            }
            if (hit) {
                double hop = Mth.clamp(fall * RESTITUTION + random.nextDouble() * 0.03D, MIN_HOP_SPEED, MAX_HOP_SPEED);
                double vx = bubble.vx();
                double vz = bubble.vz();
                double push = Math.sqrt(pushX * pushX + pushZ * pushZ);
                if (push > 1.0E-4D) {
                    vx += pushX / push * SLOPE_PUSH;
                    vz += pushZ / push * SLOPE_PUSH;
                }
                double speed = Math.sqrt(vx * vx + vz * vz);
                if (speed > MAX_DRIFT_SPEED) {
                    vx *= MAX_DRIFT_SPEED / speed;
                    vz *= MAX_DRIFT_SPEED / speed;
                }
                return bubble.rebase(time, Vec3.ZERO, vx, -down * hop, vz, true);
            }
        } else if (fall < 0.0D && blocked(level, center.add(0.0D, -down * radius * 0.9D, 0.0D))) {
            return bubble.rebase(time, Vec3.ZERO, bubble.vx(), down * -fall * CEILING_RESTITUTION, bubble.vz(), false);
        }
        double speed = Math.sqrt(bubble.vx() * bubble.vx() + bubble.vz() * bubble.vz());
        if (speed > 1.0E-4D && blocked(level, center.add(bubble.vx() / speed * radius * 0.85D, -down * radius * 0.3D,
                bubble.vz() / speed * radius * 0.85D))) {
            double angle = Math.atan2(-bubble.vz(), -bubble.vx()) + (random.nextDouble() - 0.5D);
            return bubble.rebase(time, Vec3.ZERO, Math.cos(angle) * speed, bubble.verticalVelocity(time), Math.sin(angle) * speed, false);
        }
        return bubble;
    }

    private static boolean blocked(ServerLevel level, Vec3 point) {
        return blocked(level, BlockPos.containing(point));
    }

    private static boolean blocked(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.blocksMotion() || !state.getFluidState().isEmpty();
    }

    private void sendUpdates(ServerLevel level, List<ThoraxisBubble> updated) {
        ThoraxisBubbleUpdatePayload payload = new ThoraxisBubbleUpdatePayload(this.seed, List.copyOf(updated));
        for (ServerPlayer player : level.players()) {
            if (this.syncedVersions.containsKey(player.getUUID())) {
                ThoraxisEventSync.send(player, payload);
            }
        }
    }

    private int bubblesNear(Vec3 center, long time) {
        int count = 0;
        for (ThoraxisBubble bubble : this.bubbles) {
            if (bubble.position(time).distanceToSqr(center) < BUBBLE_NEAR_RADIUS * BUBBLE_NEAR_RADIUS) {
                count++;
            }
        }
        return count;
    }

    private boolean spawnBubble(ServerLevel level, ServerPlayer player, long time) {
        RandomSource random = level.random;
        double down = ThoraxisBubble.down(player.getY());
        float radius = Mth.nextFloat(random, MIN_BUBBLE_RADIUS, MAX_BUBBLE_RADIUS);
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_BUBBLE_DISTANCE + random.nextDouble() * (MAX_BUBBLE_DISTANCE - MIN_BUBBLE_DISTANCE);
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            Double ground = findGround(level, x, player.getY(), z, down);
            if (ground == null) {
                continue;
            }
            double clearance = MIN_SPAWN_CLEARANCE + random.nextDouble() * (MAX_SPAWN_CLEARANCE - MIN_SPAWN_CLEARANCE);
            Vec3 position = new Vec3(x, ground - down * (radius + clearance), z);
            if (ThoraxisBubble.down(position.y) != down || this.crowded(position, radius, time)) {
                continue;
            }
            double heading = random.nextInt(100) < HOMING_CHANCE_PERCENT
                    ? Math.atan2(player.getZ() + (random.nextDouble() - 0.5D) * 20.0D - z, player.getX() + (random.nextDouble() - 0.5D) * 20.0D - x)
                    : this.driftHeading() + random.nextGaussian() * HEADING_SPREAD;
            double speed = MIN_BUBBLE_SPEED + random.nextDouble() * (MAX_BUBBLE_SPEED - MIN_BUBBLE_SPEED);
            ThoraxisBubble bubble = ThoraxisBubble.spawn(UUID.randomUUID(), position, Math.cos(heading) * speed, Math.sin(heading) * speed,
                    time, Mth.nextInt(random, MIN_BUBBLE_LIFE, MAX_BUBBLE_LIFE), radius,
                    random.nextBoolean() ? SLOW_BUBBLE_RATE : FAST_BUBBLE_RATE);
            this.bubbles.add(bubble);
            this.createField(level, bubble, bubble.position(time), time);
            return true;
        }
        return false;
    }

    private double driftHeading() {
        return (this.seed & 0xFFFFL) / 65536.0D * Math.PI * 2.0D;
    }

    private boolean crowded(Vec3 position, float radius, long time) {
        for (ThoraxisBubble other : this.bubbles) {
            double gap = radius + other.radius() + MIN_BUBBLE_GAP;
            if (other.position(time).distanceToSqr(position) < gap * gap) {
                return true;
            }
        }
        return false;
    }

    private static Double findGround(ServerLevel level, double x, double y, double z, double down) {
        BlockPos.MutableBlockPos cursor = BlockPos.containing(x, y - down * GROUND_SEARCH_OFFSET, z).mutable();
        if (!level.isLoaded(cursor)) {
            return null;
        }
        int step = down < 0.0D ? -1 : 1;
        for (int climb = 0; blocked(level, cursor); climb++) {
            if (climb >= GROUND_SCAN_UP) {
                return null;
            }
            cursor.move(0, -step, 0);
        }
        for (int i = 0; i < GROUND_SCAN_DOWN; i++) {
            cursor.move(0, step, 0);
            if (level.isOutsideBuildHeight(cursor)) {
                return null;
            }
            if (blocked(level, cursor)) {
                return (double) (step < 0 ? cursor.getY() + 1 : cursor.getY());
            }
        }
        return null;
    }

    private void createField(ServerLevel level, ThoraxisBubble bubble, Vec3 position, long time) {
        int remaining = (int) Math.max(1L, bubble.born() + bubble.life() - time);
        double rate = this.phase == ThoraxisEvent.Phase.ACTIVE ? bubble.rate() : 1.0D;
        TimeDilationFieldEntity field = TimeDilationFieldEntity.create(level, position, bubble.radius(), rate, remaining);
        HIDDEN_FIELDS.add(field.getUUID());
        level.addFreshEntity(field);
        UUID previous = this.bubbleFields.put(bubble.id(), field.getUUID());
        if (previous != null) {
            HIDDEN_FIELDS.remove(previous);
        }
        this.setDirty();
    }

    private void applyBubbleRates(ServerLevel level) {
        for (ThoraxisBubble bubble : this.bubbles) {
            UUID fieldId = this.bubbleFields.get(bubble.id());
            if (fieldId != null && level.getEntity(fieldId) instanceof TimeDilationFieldEntity field) {
                field.setFieldRate(bubble.rate());
            }
        }
    }

    private void tickSurges(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            if (this.event == ThoraxisEvent.BLIND_SKY && ThoraxisUndersideManager.isInUnderside(player)) {
                AABB area = player.getBoundingBox().inflate(SURGE_RADIUS);
                if (level.getEntitiesOfClass(Mob.class, area, mob -> mob instanceof Enemy).size() < BLIND_SKY_MONSTER_CAP) {
                    for (int i = 0; i < BLIND_SKY_SPAWN_ATTEMPTS; i++) {
                        spawnMonsterNear(level, player);
                    }
                }
            }
        }
    }

    private static void spawnMonsterNear(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.random;
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = 16.0D + random.nextDouble() * 16.0D;
        BlockPos pos = BlockPos.containing(player.getX() + Math.cos(angle) * distance,
                player.getY() + random.nextInt(9) - 4, player.getZ() + Math.sin(angle) * distance);
        if (level.isLoaded(pos) && level.isInWorldBounds(pos)) {
            NaturalSpawner.spawnCategoryForPosition(MobCategory.MONSTER, level, pos);
        }
    }

    private void changed() {
        this.version++;
        this.setDirty();
    }

    private void sync(ServerLevel level) {
        ThoraxisEventPayload payload = null;
        int players = 0;
        for (ServerPlayer player : level.players()) {
            players++;
            Integer synced = this.syncedVersions.get(player.getUUID());
            if (synced != null && synced == this.version) {
                continue;
            }
            if (payload == null) {
                payload = new ThoraxisEventPayload(this.event.ordinal(), this.phase.ordinal(), this.phaseStart, this.phaseEnd,
                        this.seed, List.copyOf(this.bubbles));
            }
            ThoraxisEventSync.send(player, payload);
            this.syncedVersions.put(player.getUUID(), this.version);
        }
        if (this.syncedVersions.size() > players) {
            this.syncedVersions.keySet().removeIf(uuid -> level.getPlayerByUUID(uuid) == null);
        }
    }
}
