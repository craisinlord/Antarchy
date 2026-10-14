package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload;
import com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class PortalGunIndicatorClientState {
    private static final Map<UUID, PortalGunIndicatorPayload> STATES = new HashMap<>();
    private static ClientLevel level;
    private static long lastRequestTick = Long.MIN_VALUE;
    private static UUID lastRequestedGun;
    private static Consumer<PortalGunIndicatorRequestPayload> requestSender = payload -> {};

    private PortalGunIndicatorClientState() {
    }

    public static void setRequestSender(Consumer<PortalGunIndicatorRequestPayload> sender) {
        requestSender = sender;
    }

    public static void requestIfNeeded(Minecraft minecraft, UUID gunId) {
        ClientLevel currentLevel = minecraft.level;
        if (currentLevel == null || gunId == null || minecraft.player == null) {
            return;
        }
        if (level != currentLevel) {
            level = currentLevel;
            STATES.clear();
            lastRequestedGun = null;
            lastRequestTick = Long.MIN_VALUE;
        }
        long currentTick = minecraft.player.tickCount;
        if (!gunId.equals(lastRequestedGun) || currentTick - lastRequestTick >= 20L) {
            lastRequestedGun = gunId;
            lastRequestTick = currentTick;
            requestSender.accept(new PortalGunIndicatorRequestPayload(gunId));
        }
    }

    public static void update(PortalGunIndicatorPayload payload) {
        STATES.put(payload.gunId(), payload);
    }

    public static PortalGunIndicatorPayload get(UUID gunId) {
        return gunId == null ? null : STATES.get(gunId);
    }

    public static void clear() {
        STATES.clear();
        level = null;
        lastRequestedGun = null;
        lastRequestTick = Long.MIN_VALUE;
    }
}
