package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.network.PortalGunMoonAimPayload;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;

public final class PortalGunMoonAimSync {
    private static final long MAX_REPORT_AGE_TICKS = 10L;
    private static final Map<UUID, Report> REPORTS = new ConcurrentHashMap<>();
    private static Runnable clientReporter = () -> {};

    private PortalGunMoonAimSync() {
    }

    public static void setClientReporter(Runnable reporter) {
        clientReporter = reporter;
    }

    public static void reportFromClient() {
        clientReporter.run();
    }

    public static void handle(ServerPlayer player, PortalGunMoonAimPayload payload) {
        REPORTS.put(player.getUUID(), new Report(payload.tracked(), payload.aiming(), player.serverLevel().getGameTime()));
    }

    public static Boolean clientAim(ServerPlayer player) {
        Report report = REPORTS.get(player.getUUID());
        if (report == null || !report.tracked()) {
            return null;
        }
        long age = player.serverLevel().getGameTime() - report.gameTime();
        return age < 0L || age > MAX_REPORT_AGE_TICKS ? null : report.aiming();
    }

    public static void forget(UUID playerId) {
        REPORTS.remove(playerId);
    }

    public static void clear() {
        REPORTS.clear();
    }

    private record Report(boolean tracked, boolean aiming, long gameTime) {
    }
}
