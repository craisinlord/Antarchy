package com.craisinlord.antarchy.neoforge;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AntarchyMiscConfig {
    public static final ModConfigSpec SPEC;

    // Elythia
    private static final ModConfigSpec.BooleanValue ELYTHIA_FIREFLY_PARTICLES_ENABLED;

    // Dream Sand
    private static final ModConfigSpec.BooleanValue DREAM_SAND_ENABLED;
    private static final ModConfigSpec.DoubleValue  DREAM_SAND_JUMP_VELOCITY_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue  DREAM_SAND_GRAVITY_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue  DREAM_SAND_FALL_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue  DREAM_SAND_EFFECT_DURATION_SECONDS;

    // Ichor
    private static final ModConfigSpec.BooleanValue ICHOR_WITHER_ENABLED;

    // Hushweed
    private static final ModConfigSpec.DoubleValue HUSHWEED_SPORE_LIFETIME_SECONDS;

    // Infinite Dimensions
    private static final ModConfigSpec.BooleanValue DISABLE_INFINITY_BOOK_PORTAL_CREATION;
    private static final ModConfigSpec.BooleanValue RAINBOW_ANTS_LEAD_TO_INFINITY_DIMENSIONS;

    // Diamond Minecart
    private static final ModConfigSpec.BooleanValue DIAMOND_MINECART_ENABLED;
    private static final ModConfigSpec.BooleanValue DIAMOND_MINECART_PLACES_RAILS;
    private static final ModConfigSpec.DoubleValue  DIAMOND_MINECART_CRUISE_SPEED;
    private static final ModConfigSpec.DoubleValue  DIAMOND_MINECART_MAX_SPEED;
    private static final ModConfigSpec.DoubleValue  DIAMOND_MINECART_ACCELERATION;
    private static final ModConfigSpec.DoubleValue  DIAMOND_MINECART_DECELERATION;
    private static final ModConfigSpec.DoubleValue  DIAMOND_MINECART_COAST_DECELERATION;
    private static final ModConfigSpec.BooleanValue DIAMOND_MINECART_MOB_DAMAGE_ENABLED;
    private static final ModConfigSpec.DoubleValue  DIAMOND_MINECART_MAX_MOB_DAMAGE;

    // World / misc toggles
    private static final ModConfigSpec.BooleanValue DUPLICATOR_TREE_ENABLED;
    private static final ModConfigSpec.BooleanValue GLOWING_TORCHFLOWERS;
    private static final ModConfigSpec.BooleanValue GLOW_VINES_UNDER_LEAVES;
    private static final ModConfigSpec.BooleanValue ENTITY_SPECIFIC_FIRE_OVERLAY_ENABLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();


        // Elythia

        b.push("elythia");
        ELYTHIA_FIREFLY_PARTICLES_ENABLED = b
                .comment("Whether firefly ambient particles appear at night in the Elythia dimension.")
                .define("fireflyParticlesEnabled", true);
        b.pop();


        // Dream Sand

        b.push("dreamSand");
        DREAM_SAND_ENABLED                   = b.comment("If true, jumping from dream sand applies temporary low gravity until solid ground is touched.").define("enabled", true);
        DREAM_SAND_JUMP_VELOCITY_MULTIPLIER  = b.comment("Multiplier applied to upward jump velocity when leaving dream sand.")                          .defineInRange("jumpVelocityMultiplier",  1.3D, 0.0D, 16.0D);
        DREAM_SAND_GRAVITY_MULTIPLIER        = b.comment("Multiplier applied to downward velocity while low gravity is active.")                         .defineInRange("gravityMultiplier",       0.45D, 0.0D, 1.0D);
        DREAM_SAND_FALL_DAMAGE_MULTIPLIER    = b.comment("Multiplier applied to fall damage during the low-gravity phase.")                              .defineInRange("fallDamageMultiplier",    0.35D, 0.0D, 1.0D);
        DREAM_SAND_EFFECT_DURATION_SECONDS   = b.comment("Seconds the low-gravity effect remains active after leaving dream sand.")                      .defineInRange("effectDurationSeconds",   7.0D, 0.0D, 60.0D);
        b.pop();


        // Ichor

        b.push("ichor");
        ICHOR_WITHER_ENABLED = b
                .comment("If true, players submerged in ichor receive a Wither effect.")
                .define("witherEnabled", true);
        b.pop();


        // Hushweed

        b.push("hushweed");
        HUSHWEED_SPORE_LIFETIME_SECONDS = b
                .comment("How long (in seconds) a Hushweed spore projectile stays alive before disappearing.")
                .defineInRange("sporeLifetimeSeconds", 5.0D, 0.1D, 120.0D);
        b.pop();


        // Infinity Dimensions

        b.push("infinityDimensions");
        DISABLE_INFINITY_BOOK_PORTAL_CREATION = b
                .comment("If true, book and quills cannot be thrown into nether portals to create random dimensions.")
                .define("disableInfinityBookPortalCreation", true);
        RAINBOW_ANTS_LEAD_TO_INFINITY_DIMENSIONS = b
                .comment(
                    "Whether rainbow ants open randomly generated Infinity dimensions.",
                    "Only works if the Infinite Dimensions mod is installed."
                )
                .define("rainbowAntsLeadToInfinityDimensions", net.neoforged.fml.ModList.get().isLoaded("infinity"));
        b.pop();


        // Diamond Minecart

        b.push("diamondMinecart");
        DIAMOND_MINECART_ENABLED            = b.comment("Master toggle. If false, acts like a regular minecart.")                             .define("enabled",           true);
        DIAMOND_MINECART_PLACES_RAILS       = b.comment("Whether the cart automatically places rails from the rider's inventory.")             .define("placesRails",       true);
        DIAMOND_MINECART_CRUISE_SPEED       = b.comment("Cruise speed in blocks/tick while ridden (even without holding W).")                  .defineInRange("cruiseSpeed",         0.6D,  0.05D, 2.0D);
        DIAMOND_MINECART_MAX_SPEED          = b.comment("Maximum boosted speed in blocks/tick when W is held. Vanilla cap is 0.4.")            .defineInRange("maxSpeed",            1.0D,  0.05D, 2.0D);
        DIAMOND_MINECART_ACCELERATION       = b.comment("Speed gained per tick while accelerating.")                                           .defineInRange("acceleration",        0.03D, 0.001D, 0.5D);
        DIAMOND_MINECART_DECELERATION       = b.comment("Speed lost per tick while S is held (active braking).")                              .defineInRange("deceleration",        0.04D, 0.001D, 0.5D);
        DIAMOND_MINECART_COAST_DECELERATION = b.comment("Speed lost per tick when neither W nor S is held (coasting friction).")               .defineInRange("coastDeceleration",   0.01D, 0.001D, 0.5D);
        DIAMOND_MINECART_MOB_DAMAGE_ENABLED = b.comment("Whether the cart deals damage to mobs it collides with at speed.")                   .define("mobDamageEnabled",  true);
        DIAMOND_MINECART_MAX_MOB_DAMAGE     = b.comment("Maximum damage dealt at full speed. Scales linearly with current speed.")             .defineInRange("maxMobDamage",        8.0D,  0.0D, 200.0D);
        b.pop();


        // World / visual toggles

        b.push("world");
        DUPLICATOR_TREE_ENABLED              = b.comment("If false, duplicator trees do not generate and duplicator saplings produce nothing.").define("duplicatorTreeEnabled",           true);
        GLOWING_TORCHFLOWERS                 = b.comment("Whether fully-grown torchflower blocks emit light.")                                  .define("glowingTorchflowers",              true);
        GLOW_VINES_UNDER_LEAVES              = b.comment("Whether glow vines can be placed and survive when attached under leaf blocks.")       .define("glowVinesUnderLeaves",             true);
        ENTITY_SPECIFIC_FIRE_OVERLAY_ENABLED = b.comment("If true, burning entities use soul fire / dream fire overlays where appropriate.")   .define("entitySpecificFireOverlayEnabled", true);
        b.pop();

        SPEC = b.build();
    }

    private AntarchyMiscConfig() {}

    static boolean elythiaFireflyParticlesEnabled()      { return ELYTHIA_FIREFLY_PARTICLES_ENABLED.get(); }

    static boolean dreamSandEnabled()                    { return DREAM_SAND_ENABLED.get(); }
    static double  dreamSandJumpVelocityMultiplier()     { return DREAM_SAND_JUMP_VELOCITY_MULTIPLIER.get(); }
    static double  dreamSandGravityMultiplier()          { return DREAM_SAND_GRAVITY_MULTIPLIER.get(); }
    static double  dreamSandFallDamageMultiplier()       { return DREAM_SAND_FALL_DAMAGE_MULTIPLIER.get(); }
    static double  dreamSandEffectDurationSeconds()      { return DREAM_SAND_EFFECT_DURATION_SECONDS.get(); }

    static boolean ichorWitherEnabled()                  { return ICHOR_WITHER_ENABLED.get(); }

    static boolean duplicatorTreeEnabled()               { return DUPLICATOR_TREE_ENABLED.get(); }
    static boolean glowingTorchflowers()                 { return GLOWING_TORCHFLOWERS.get(); }
    static boolean glowVinesUnderLeaves()                { return GLOW_VINES_UNDER_LEAVES.get(); }
    static boolean entitySpecificFireOverlayEnabled()    { return ENTITY_SPECIFIC_FIRE_OVERLAY_ENABLED.get(); }

    static double  hushweedSporeLifetimeSeconds()        { return HUSHWEED_SPORE_LIFETIME_SECONDS.get(); }

    static boolean disableInfinityBookPortalCreation()   { return DISABLE_INFINITY_BOOK_PORTAL_CREATION.get(); }
    static boolean rainbowAntsLeadToInfinityDimensions() { return RAINBOW_ANTS_LEAD_TO_INFINITY_DIMENSIONS.get(); }

    static boolean diamondMinecartEnabled()              { return DIAMOND_MINECART_ENABLED.get(); }
    static boolean diamondMinecartPlacesRails()          { return DIAMOND_MINECART_PLACES_RAILS.get(); }
    static double  diamondMinecartCruiseSpeed()          { return DIAMOND_MINECART_CRUISE_SPEED.get(); }
    static double  diamondMinecartMaxSpeed()             { return DIAMOND_MINECART_MAX_SPEED.get(); }
    static double  diamondMinecartAcceleration()         { return DIAMOND_MINECART_ACCELERATION.get(); }
    static double  diamondMinecartDeceleration()         { return DIAMOND_MINECART_DECELERATION.get(); }
    static double  diamondMinecartCoastDeceleration()    { return DIAMOND_MINECART_COAST_DECELERATION.get(); }
    static boolean diamondMinecartMobDamageEnabled()     { return DIAMOND_MINECART_MOB_DAMAGE_ENABLED.get(); }
    static double  diamondMinecartMaxMobDamage()         { return DIAMOND_MINECART_MAX_MOB_DAMAGE.get(); }
}
