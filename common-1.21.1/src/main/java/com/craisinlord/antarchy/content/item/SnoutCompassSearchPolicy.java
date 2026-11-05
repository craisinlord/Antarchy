package com.craisinlord.antarchy.content.item;

public final class SnoutCompassSearchPolicy {
    private SnoutCompassSearchPolicy() {
    }

    public static boolean shouldRestart(boolean running, double movementSquared) {
        return movementSquared > (running ? 16384.0 : 64.0);
    }

    public static int refreshDelay(double movementSquared) {
        return movementSquared > 64.0 ? 1 : 100;
    }
}
