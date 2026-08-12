package com.craisinlord.antarchy.content.item;

public final class JumpyBootsHelper {
    public static final int CHARGE_TICKS_MAX = 30;
    public static final int PRIME_WINDOW_TICKS = 15;
    public static final int COOLDOWN_TICKS = 40;

    public static final float MAX_VERTICAL_BOOST = 2.0F;
    public static final float SPRINT_FORWARD_BOOST = 0.3F;
    public static final long FALL_PROTECTION_TICKS = 120L;
    public static final String FALL_PROTECTION_NBT_KEY = "antarchy:jumpy_boots_protection_until";

    private JumpyBootsHelper() {
    }

    public static float verticalBoostFor(int chargeTicks) {
        float ratio = Math.min(chargeTicks / (float) CHARGE_TICKS_MAX, 1.0F);
        return ratio * MAX_VERTICAL_BOOST;
    }
}
