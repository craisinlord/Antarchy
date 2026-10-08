package com.craisinlord.antarchy.content.portalgun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class PortalGunPortalRegistry {
    private static final Map<Level, Map<UUID, PortalGunPortalEntity>> LEVELS = Collections.synchronizedMap(new WeakHashMap<>());

    private PortalGunPortalRegistry() {
    }

    static void track(PortalGunPortalEntity portal) {
        Level level = portal.level();
        if (level == null) {
            return;
        }
        Map<UUID, PortalGunPortalEntity> portals;
        synchronized (LEVELS) {
            portals = LEVELS.computeIfAbsent(level, ignored -> new ConcurrentHashMap<>());
        }
        portals.put(portal.getUUID(), portal);
    }

    static void untrack(PortalGunPortalEntity portal) {
        Map<UUID, PortalGunPortalEntity> portals = portalsIn(portal.level());
        if (portals != null) {
            portals.remove(portal.getUUID(), portal);
        }
    }

    public static Collection<PortalGunPortalEntity> all(Level level) {
        Map<UUID, PortalGunPortalEntity> portals = portalsIn(level);
        return portals == null ? List.of() : portals.values();
    }

    public static PortalGunPortalEntity find(Level level, UUID portalId) {
        if (portalId == null) {
            return null;
        }
        Map<UUID, PortalGunPortalEntity> portals = portalsIn(level);
        if (portals == null) {
            return null;
        }
        PortalGunPortalEntity portal = portals.get(portalId);
        return portal != null && portal.isAlive() ? portal : null;
    }

    public static List<PortalGunPortalEntity> near(Level level, AABB bounds) {
        Map<UUID, PortalGunPortalEntity> portals = portalsIn(level);
        if (portals == null || portals.isEmpty()) {
            return List.of();
        }
        List<PortalGunPortalEntity> nearby = null;
        for (PortalGunPortalEntity portal : portals.values()) {
            if (portal.isAlive() && portal.getPortalInsides().intersects(bounds)) {
                if (nearby == null) {
                    nearby = new ArrayList<>(2);
                }
                nearby.add(portal);
            }
        }
        return nearby == null ? List.of() : nearby;
    }

    private static Map<UUID, PortalGunPortalEntity> portalsIn(Level level) {
        if (level == null) {
            return null;
        }
        synchronized (LEVELS) {
            return LEVELS.get(level);
        }
    }
}
