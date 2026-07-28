package com.craisinlord.antarchy.content.client;

public final class BloodglassClientState {
    private BloodglassClientState() {}

    private static int shieldsActive = 0;
    private static int shieldsMax = 0;

    public static void update(int active, int max) {
        shieldsActive = active;
        shieldsMax = max;
    }

    public static int getShieldsActive() { return shieldsActive; }
    public static int getShieldsMax()    { return shieldsMax; }
}
