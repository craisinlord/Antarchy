package com.craisinlord.antarchy.content.entity.cloud_shark;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class SharknadoManager {
    public static final double[] TIER_RADII          = { 2.5D,  5.0D,  8.0D  };
    public static final double[] TIER_HEIGHT_OFFSETS = { 8.0D,  5.0D,  1.5D  };
    public static final int[]    TIER_MAX_SLOTS      = {    2,     4,     6   };
    
    public static final double[] TIER_ROTATION_SPEEDS= { 0.06D, 0.045D, 0.03D };
    private static final int    MIN_SHARKS_TO_FORM     = 3;
    private static final double FORM_SEARCH_RADIUS     = 20.0D;
    private static final double FORM_SEARCH_RADIUS_SQR = FORM_SEARCH_RADIUS * FORM_SEARCH_RADIUS;
    private static final int    MIN_SHARKS_TO_PERSIST  = 2;
    private static final int   ATTACK_COOLDOWN_MIN = 80;
    private static final int   ATTACK_COOLDOWN_MAX = 160;
    private static final float VOLLEY_CHANCE       = 0.15F;
    
    private static final double DRIFT_SPEED     = 0.06D;
    
    private static final double MAX_DRIFT_DIST  = 10.0D;
    private static final WeakHashMap<ServerLevel, SharknadoManager> INSTANCES = new WeakHashMap<>();

    
    private final Map<UUID, SharknadoEntry> tornadoes = new HashMap<>();
    
    private final Map<UUID, UUID> sharkToTornado = new HashMap<>();
    private long lastCleanupGameTime = Long.MIN_VALUE;

    private SharknadoManager() {}

    public static SharknadoManager get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, l -> new SharknadoManager());
    }

    
    public boolean tryFormOrJoin(CloudSharkEntity shark, ServerLevel level) {
        if (tryJoinExisting(shark)) return true;

        List<CloudSharkEntity> soloNearby = getSoloSharksNear(shark, level);
        if (soloNearby.size() + 1 < MIN_SHARKS_TO_FORM) return false;
        UUID myUuid = shark.getUUID();
        for (CloudSharkEntity other : soloNearby) {
            if (other.getUUID().compareTo(myUuid) < 0) return false;
        }

        List<CloudSharkEntity> all = new ArrayList<>();
        all.add(shark);
        all.addAll(soloNearby);
        formTornado(all, level);
        return true;
    }

    private void formTornado(List<CloudSharkEntity> sharks, ServerLevel level) {
        UUID id = UUID.randomUUID();
        SharknadoEntry entry = new SharknadoEntry(id);
        int idx = 0;
        outer:
        for (int tier = 2; tier >= 0; tier--) {
            for (int slot = 0; slot < TIER_MAX_SLOTS[tier]; slot++) {
                if (idx >= sharks.size()) break outer;
                CloudSharkEntity s = sharks.get(idx++);
                entry.add(s, tier, slot);
                sharkToTornado.put(s.getUUID(), id);
                s.onJoinSharknado(id, tier, slot);
            }
        }

        entry.updateCentroid();
        entry.initializeFormation(level);
        entry.state = TornadoState.ACTIVE;
        entry.attackCooldown = ATTACK_COOLDOWN_MIN
                + level.getRandom().nextInt(ATTACK_COOLDOWN_MAX - ATTACK_COOLDOWN_MIN);
        tornadoes.put(id, entry);
    }

    private boolean tryJoinExisting(CloudSharkEntity shark) {
        UUID bestId = null;
        double bestDistSqr = FORM_SEARCH_RADIUS_SQR;

        for (Map.Entry<UUID, SharknadoEntry> e : tornadoes.entrySet()) {
            SharknadoEntry entry = e.getValue();
            if (entry.state == TornadoState.DISPERSING) continue;
            if (!entry.hasOpenSlot()) continue;
            Vec3 c = entry.centroid;
            if (c == null) continue;
            double dx = shark.getX() - c.x;
            double dz = shark.getZ() - c.z;
            double dSqr = dx * dx + dz * dz;
            if (dSqr < bestDistSqr) {
                bestDistSqr = dSqr;
                bestId = e.getKey();
            }
        }

        if (bestId == null) return false;
        SharknadoEntry entry = tornadoes.get(bestId);
        int[] openSlot = entry.getNextOpenSlot();
        if (openSlot == null) return false;

        entry.add(shark, openSlot[0], openSlot[1]);
        sharkToTornado.put(shark.getUUID(), bestId);
        shark.onJoinSharknado(bestId, openSlot[0], openSlot[1]);
        entry.updateCentroid();
        entry.syncOrbitCenter();
        return true;
    }

    
    public void tickSharknado(UUID sharknadoId, ServerLevel level) {
        SharknadoEntry entry = tornadoes.get(sharknadoId);
        if (entry == null) return;
        long now = level.getGameTime();
        if (entry.lastTickedGameTime == now) return;
        entry.lastTickedGameTime = now;
        this.cleanup(level);

        entry.updateCentroid();
        if (entry.centroid == null) return;
        entry.updateOrbitCenter();
        entry.advanceRotation();
        Player nearestPlayer = level.getNearestPlayer(
                entry.getOrbitCenter().x, entry.getOrbitCenter().y, entry.getOrbitCenter().z, 48.0D, false);
        entry.driftOffset = entry.driftOffset.scale(0.92D);
        if (nearestPlayer != null) {
            Vec3 toPlayer = nearestPlayer.position().subtract(entry.getOrbitCenter());
            double len = toPlayer.length();
            if (len > 0.001D) {
                entry.driftOffset = entry.driftOffset.add(toPlayer.scale(DRIFT_SPEED / len));
                double driftLen = entry.driftOffset.length();
                if (driftLen > MAX_DRIFT_DIST) {
                    entry.driftOffset = entry.driftOffset.scale(MAX_DRIFT_DIST / driftLen);
                }
            }
        }

        if (entry.getAliveCount() < MIN_SHARKS_TO_PERSIST) {
            disperseTornado(sharknadoId);
            return;
        }

        if (entry.state != TornadoState.ACTIVE) return;

        if (--entry.attackCooldown <= 0) {
            triggerAttack(entry, level);
            entry.attackCooldown = ATTACK_COOLDOWN_MIN
                    + level.getRandom().nextInt(ATTACK_COOLDOWN_MAX - ATTACK_COOLDOWN_MIN);
        }
    }

    private void triggerAttack(SharknadoEntry entry, ServerLevel level) {
        Vec3 orbitCenter = entry.getOrbitCenter();
        if (orbitCenter == null) return;
        Player target = level.getNearestPlayer(
                orbitCenter.x, orbitCenter.y, orbitCenter.z, 48.0D, false);
        if (target == null) return;

        boolean volley = level.getRandom().nextFloat() < VOLLEY_CHANCE;
        if (volley) {
            int launched = 0;
            for (SharkSlot s : entry.slots) {
                if (s.tier == 2 && s.shark.isAlive() && s.shark.isOrbiting()) {
                    s.shark.launchFromSharknado(target);
                    if (++launched >= 2) break;
                }
            }
        } else {
            SharkSlot best = null;
            double bestDist = Double.MAX_VALUE;
            for (SharkSlot s : entry.slots) {
                if (s.tier == 2 && s.shark.isAlive() && s.shark.isOrbiting()) {
                    double d = s.shark.distanceToSqr(target);
                    if (d < bestDist) { bestDist = d; best = s; }
                }
            }
            if (best == null) {
                for (SharkSlot s : entry.slots) {
                    if (s.shark.isAlive() && s.shark.isOrbiting()) {
                        double d = s.shark.distanceToSqr(target);
                        if (d < bestDist) { bestDist = d; best = s; }
                    }
                }
            }
            if (best != null) best.shark.launchFromSharknado(target);
        }
    }

    
    public void disperseTornado(UUID sharknadoId) {
        SharknadoEntry entry = tornadoes.remove(sharknadoId);
        if (entry == null) return;
        entry.state = TornadoState.DISPERSING;
        for (SharkSlot s : entry.slots) {
            sharkToTornado.remove(s.shark.getUUID());
            if (s.shark.isAlive()) s.shark.onLeaveSharknado();
        }
    }

    
    public void unregisterShark(CloudSharkEntity shark) {
        UUID tornadoId = sharkToTornado.remove(shark.getUUID());
        if (tornadoId == null) return;
        SharknadoEntry entry = tornadoes.get(tornadoId);
        if (entry == null) return;
        entry.removeShark(shark);
        if (entry.getAliveCount() < MIN_SHARKS_TO_PERSIST) {
            disperseTornado(tornadoId);
        }
    }

    
    public void cleanup(ServerLevel level) {
        long now = level.getGameTime();
        if (this.lastCleanupGameTime == now || now % 200L != 0L) return;
        this.lastCleanupGameTime = now;
        Iterator<Map.Entry<UUID, SharknadoEntry>> iter = tornadoes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, SharknadoEntry> e = iter.next();
            SharknadoEntry entry = e.getValue();
            Iterator<SharkSlot> slotIter = entry.slots.iterator();
            while (slotIter.hasNext()) {
                SharkSlot s = slotIter.next();
                if (!s.shark.isAlive()) {
                    sharkToTornado.remove(s.shark.getUUID());
                    slotIter.remove();
                }
            }
            if (entry.getAliveCount() < MIN_SHARKS_TO_PERSIST) {
                for (SharkSlot s : entry.slots) {
                    sharkToTornado.remove(s.shark.getUUID());
                    if (s.shark.isAlive()) s.shark.onLeaveSharknado();
                }
                iter.remove();
            }
        }
    }

    
    @Nullable
    public Vec3 getOrbitTarget(UUID sharknadoId, int tier, int slot) {
        SharknadoEntry entry = tornadoes.get(sharknadoId);
        if (entry == null || entry.getOrbitCenter() == null) return null;

        Vec3 center = entry.getOrbitCenter().add(entry.driftOffset);
        double baseAngle = (2.0 * Math.PI * slot) / TIER_MAX_SLOTS[tier];
        double currentAngle = baseAngle + entry.rotationPhases[tier];

        double x = center.x + Math.cos(currentAngle) * TIER_RADII[tier];
        double z = center.z + Math.sin(currentAngle) * TIER_RADII[tier];
        double y = center.y + TIER_HEIGHT_OFFSETS[tier];
        return new Vec3(x, y, z);
    }

    @Nullable
    public Vec3 getCentroid(UUID sharknadoId) {
        SharknadoEntry entry = tornadoes.get(sharknadoId);
        return entry == null ? null : entry.getOrbitCenter();
    }

    public boolean isInSharknado(UUID sharkUuid) {
        return sharkToTornado.containsKey(sharkUuid);
    }

    
    private List<CloudSharkEntity> getSoloSharksNear(CloudSharkEntity seeker, ServerLevel level) {
        List<CloudSharkEntity> result = new ArrayList<>();
        for (CloudSharkEntity other : level.getEntitiesOfClass(
                CloudSharkEntity.class, seeker.getBoundingBox().inflate(FORM_SEARCH_RADIUS))) {
            if (other == seeker) continue;
            if (sharkToTornado.containsKey(other.getUUID())) continue;
            double dx = other.getX() - seeker.getX();
            double dz = other.getZ() - seeker.getZ();
            if (dx * dx + dz * dz <= FORM_SEARCH_RADIUS_SQR) result.add(other);
        }
        return result;
    }

    public enum TornadoState { FORMING, ACTIVE, DISPERSING }

    private static final class SharknadoEntry {
        final UUID id;
        final List<SharkSlot> slots = new ArrayList<>();
        @Nullable Vec3 centroid;
        @Nullable Vec3 orbitCenter;
        Vec3 driftOffset = Vec3.ZERO;
        final double[] rotationPhases = new double[TIER_ROTATION_SPEEDS.length];
        TornadoState state = TornadoState.FORMING;
        int attackCooldown;
        long lastTickedGameTime = -1L;

        SharknadoEntry(UUID id) { this.id = id; }

        void add(CloudSharkEntity shark, int tier, int slot) {
            slots.add(new SharkSlot(shark, tier, slot));
        }

        void removeShark(CloudSharkEntity shark) {
            slots.removeIf(s -> s.shark == shark);
        }

        void updateCentroid() {
            double x = 0, y = 0, z = 0;
            int count = 0;
            for (SharkSlot s : slots) {
                if (s.shark.isAlive()) {
                    x += s.shark.getX();
                    y += s.shark.getY();
                    z += s.shark.getZ();
                    count++;
                }
            }
            centroid = count > 0 ? new Vec3(x / count, y / count, z / count) : null;
        }

        void initializeFormation(ServerLevel level) {
            this.syncOrbitCenter();
            for (int tier = 0; tier < this.rotationPhases.length; tier++) {
                this.rotationPhases[tier] = level.getRandom().nextDouble() * (Math.PI * 2.0D);
            }
        }

        void syncOrbitCenter() {
            this.orbitCenter = this.centroid;
        }

        void updateOrbitCenter() {
            if (this.centroid == null) {
                this.orbitCenter = null;
                return;
            }
            if (this.orbitCenter == null) {
                this.orbitCenter = this.centroid;
                return;
            }
            this.orbitCenter = this.orbitCenter.lerp(this.centroid, 0.2D);
        }

        void advanceRotation() {
            for (int tier = 0; tier < this.rotationPhases.length; tier++) {
                this.rotationPhases[tier] += TIER_ROTATION_SPEEDS[tier];
            }
        }

        @Nullable Vec3 getOrbitCenter() {
            return this.orbitCenter != null ? this.orbitCenter : this.centroid;
        }

        int getAliveCount() {
            int c = 0;
            for (SharkSlot s : slots) if (s.shark.isAlive()) c++;
            return c;
        }

        boolean hasOpenSlot() {
            for (int tier = 2; tier >= 0; tier--) {
                int used = 0;
                for (SharkSlot s : slots) if (s.tier == tier) used++;
                if (used < TIER_MAX_SLOTS[tier]) return true;
            }
            return false;
        }

        @Nullable int[] getNextOpenSlot() {
            for (int tier = 2; tier >= 0; tier--) {
                Set<Integer> usedSlots = new HashSet<>();
                for (SharkSlot s : slots) if (s.tier == tier) usedSlots.add(s.slot);
                for (int slot = 0; slot < TIER_MAX_SLOTS[tier]; slot++) {
                    if (!usedSlots.contains(slot)) return new int[]{tier, slot};
                }
            }
            return null;
        }
    }

    private static final class SharkSlot {
        final CloudSharkEntity shark;
        final int tier;
        final int slot;

        SharkSlot(CloudSharkEntity shark, int tier, int slot) {
            this.shark = shark;
            this.tier = tier;
            this.slot = slot;
        }
    }
}
