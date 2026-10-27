package com.craisinlord.antarchy.content.item;

import net.minecraft.util.Mth;

public final class BrutalflyElytraFlightHelper {
    public static final int FLAP_CHARGE_TICKS_MAX = 20;
    public static final int IDEAL_RELEASE_TICK = 18;
    public static final int PERFECT_WINDOW_TICKS = 2;
    public static final int GOOD_WINDOW_TICKS = 5;
    public static final int FLAP_COOLDOWN_TICKS = 12;
    public static final float WEAK_LIFT = 0.12F;
    public static final float BASE_LIFT = 0.25F;
    public static final float GOOD_LIFT = 0.45F;
    public static final float PERFECT_LIFT = 0.70F;
    public static final float FORWARD_BOOST = 0.15F;
    public static final float GOOD_FORWARD_BOOST = 0.25F;
    public static final float PERFECT_FORWARD_BOOST = 0.35F;
    public static final int ANIMATION_TICKS = 10;

    private BrutalflyElytraFlightHelper() {
    }

    public static int clampChargeTicks(int chargeTicks) {
        return Mth.clamp(chargeTicks, 0, FLAP_CHARGE_TICKS_MAX);
    }

    public static FlapTier resolveFlapTier(int chargeTicks) {
        if (chargeTicks <= 0) {
            return FlapTier.FAIL;
        }

        int clampedCharge = clampChargeTicks(chargeTicks);
        int distance = Math.abs(clampedCharge - IDEAL_RELEASE_TICK);
        if (distance <= PERFECT_WINDOW_TICKS) {
            return FlapTier.PERFECT;
        }
        if (distance <= GOOD_WINDOW_TICKS) {
            return FlapTier.GOOD;
        }
        return FlapTier.WEAK;
    }

    public static float liftFor(FlapTier tier) {
        return switch (tier) {
            case PERFECT -> PERFECT_LIFT;
            case GOOD -> GOOD_LIFT;
            case WEAK -> BASE_LIFT;
            case FAIL -> 0.0F;
        };
    }

    public static float forwardBoostFor(FlapTier tier) {
        return switch (tier) {
            case PERFECT -> PERFECT_FORWARD_BOOST;
            case GOOD -> GOOD_FORWARD_BOOST;
            case WEAK -> FORWARD_BOOST;
            case FAIL -> 0.0F;
        };
    }

    public static int animationTicksFor(FlapTier tier) {
        return switch (tier) {
            case PERFECT -> ANIMATION_TICKS;
            case GOOD -> 9;
            case WEAK -> 7;
            case FAIL -> 0;
        };
    }

    public enum FlapTier {
        FAIL,
        WEAK,
        GOOD,
        PERFECT
    }
}
