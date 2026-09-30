package com.craisinlord.antarchy.content.client;

public final class PortalGunGrabClientState {
    private static boolean active;

    private PortalGunGrabClientState() {
    }

    public static void update(boolean grabActive) {
        active = grabActive;
    }

    public static boolean isActive() {
        return active;
    }

    public static void clear() {
        active = false;
    }
}
