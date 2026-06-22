package com.craisinlord.antarchy.content.entity.ant;

import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.WeakHashMap;

final class AntMarchManager {
    private static final WeakHashMap<ServerLevel, AntMarchManager> INSTANCES = new WeakHashMap<>();

    
    private final Map<UUID, TreeMap<Integer, BaseAntEntity>> marches = new HashMap<>();

    private AntMarchManager() {}

    static AntMarchManager get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, l -> new AntMarchManager());
    }

    
    void register(UUID leaderUuid, int marchOrder, BaseAntEntity ant) {
        marches.computeIfAbsent(leaderUuid, k -> new TreeMap<>()).put(marchOrder, ant);
    }

    
    void unregister(UUID leaderUuid, int marchOrder) {
        TreeMap<Integer, BaseAntEntity> march = marches.get(leaderUuid);
        if (march == null) return;
        march.remove(marchOrder);
        if (march.isEmpty()) marches.remove(leaderUuid);
    }

    
    @Nullable
    BaseAntEntity getLeader(UUID leaderUuid) {
        return getByOrder(leaderUuid, 0);
    }

    
    @Nullable
    BaseAntEntity getPredecessor(UUID leaderUuid, int order) {
        return order > 0 ? getByOrder(leaderUuid, order - 1) : null;
    }

    
    int getNextOrder(UUID leaderUuid) {
        TreeMap<Integer, BaseAntEntity> march = marches.get(leaderUuid);
        if (march == null || march.isEmpty()) return 1;
        return march.lastKey() + 1;
    }

    
    List<BaseAntEntity> getParticipantsNear(BaseAntEntity seeker, double radius, double verticalRange) {
        double radiusSqr = radius * radius;
        List<BaseAntEntity> result = new ArrayList<>();
        for (TreeMap<Integer, BaseAntEntity> march : marches.values()) {
            for (BaseAntEntity ant : march.values()) {
                if (ant == seeker || !ant.isAlive()) continue;
                double dy = Math.abs(ant.getY() - seeker.getY());
                if (dy > verticalRange) continue;
                double dx = ant.getX() - seeker.getX();
                double dz = ant.getZ() - seeker.getZ();
                if (dx * dx + dz * dz <= radiusSqr) {
                    result.add(ant);
                }
            }
        }
        return result;
    }

    
    void cleanup() {
        Iterator<Map.Entry<UUID, TreeMap<Integer, BaseAntEntity>>> marchIter = marches.entrySet().iterator();
        while (marchIter.hasNext()) {
            Map.Entry<UUID, TreeMap<Integer, BaseAntEntity>> entry = marchIter.next();
            entry.getValue().entrySet().removeIf(e -> !e.getValue().isAlive());
            if (entry.getValue().isEmpty()) {
                marchIter.remove();
            }
        }
    }

    @Nullable
    private BaseAntEntity getByOrder(UUID leaderUuid, int order) {
        TreeMap<Integer, BaseAntEntity> march = marches.get(leaderUuid);
        if (march == null) return null;
        BaseAntEntity ant = march.get(order);
        if (ant == null) return null;
        if (!ant.isAlive()) {
            march.remove(order);
            if (march.isEmpty()) marches.remove(leaderUuid);
            return null;
        }
        return ant;
    }
}
