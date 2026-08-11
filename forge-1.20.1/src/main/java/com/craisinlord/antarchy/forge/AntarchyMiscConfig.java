package com.craisinlord.antarchy.forge;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AntarchyMiscConfig {
    public static final ForgeConfigSpec SPEC;

    // Elythia
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_FIREFLY_PARTICLES_ENABLED;

    // Dream Sand
    private static final ForgeConfigSpec.BooleanValue DREAM_SAND_ENABLED;
    private static final ForgeConfigSpec.DoubleValue  DREAM_SAND_JUMP_VELOCITY_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue  DREAM_SAND_GRAVITY_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue  DREAM_SAND_EFFECT_DURATION_SECONDS;
    private static final ForgeConfigSpec.DoubleValue  DREAM_SAND_FALLING_BLOCK_GRAVITY_MULTIPLIER;

    // Ichor
    private static final ForgeConfigSpec.BooleanValue ICHOR_WITHER_ENABLED;

    // Hushweed
    private static final ForgeConfigSpec.DoubleValue HUSHWEED_SPORE_LIFETIME_SECONDS;

    // Infinite Dimensions
    private static final ForgeConfigSpec.BooleanValue DISABLE_INFINITY_BOOK_PORTAL_CREATION;
    private static final ForgeConfigSpec.BooleanValue RAINBOW_ANTS_LEAD_TO_INFINITY_DIMENSIONS;
    private static final ForgeConfigSpec.DoubleValue RAINBOW_ANT_INFINITY_EASTER_EGG_CHANCE;

    // Permanent portals
    private static final ForgeConfigSpec.BooleanValue PERMANENT_PORTALS_ENABLED;
    private static final ForgeConfigSpec.BooleanValue PERMANENT_PORTALS_FLINT_AND_STEEL_ENABLED;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_PORTAL_ENABLED;
    private static final ForgeConfigSpec.BooleanValue THORAXIS_PORTAL_ENABLED;
    private static final ForgeConfigSpec.BooleanValue CAVARYN_PORTAL_ENABLED;

    // Diamond Minecart
    private static final ForgeConfigSpec.BooleanValue DIAMOND_MINECART_ENABLED;
    private static final ForgeConfigSpec.BooleanValue DIAMOND_MINECART_PLACES_RAILS;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_CRUISE_SPEED;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_MAX_SPEED;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_ACCELERATION;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_DECELERATION;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_COAST_DECELERATION;
    private static final ForgeConfigSpec.BooleanValue DIAMOND_MINECART_MOB_DAMAGE_ENABLED;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_MAX_MOB_DAMAGE;

    // World / misc toggles
    private static final ForgeConfigSpec.BooleanValue DUPLICATOR_TREE_ENABLED;
    private static final ForgeConfigSpec.BooleanValue GLOW_VINES_UNDER_LEAVES;
    private static final ForgeConfigSpec.BooleanValue SWING_THROUGH_GRASS_ENABLED;
    private static final ForgeConfigSpec.BooleanValue FABRIC_KEYBINDING_CONFLICT_FIX_ENABLED;
    private static final ForgeConfigSpec.BooleanValue EXPERIMENTAL_SETTINGS_POPUP_DISABLED;
    private static final ForgeConfigSpec.BooleanValue ENTITY_SPECIFIC_FIRE_OVERLAY_ENABLED;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();


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
        DREAM_SAND_EFFECT_DURATION_SECONDS   = b.comment("Seconds the low-gravity effect remains active after leaving dream sand.")                      .defineInRange("effectDurationSeconds",   7.0D, 0.0D, 60.0D);
        DREAM_SAND_FALLING_BLOCK_GRAVITY_MULTIPLIER = b.comment("Multiplier applied to dream sand's own falling-block gravity (how slowly it falls as a block, distinct from the player low-gravity effect).").defineInRange("fallingBlockGravityMultiplier", 0.4D, 0.0D, 16.0D);
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
                .define("rainbowAntsLeadToInfinityDimensions", true);
        RAINBOW_ANT_INFINITY_EASTER_EGG_CHANCE = b
                .comment(
                    "Chance for a rainbow ant Infinity destination roll to select an Infinity easter egg dimension instead of a normal generated one.",
                    "0.01 = 1%, 0 disables easter egg rolls."
                )
                .defineInRange("rainbowAntInfinityEasterEggChance", 0.01D, 0.0D, 1.0D);
        b.pop();


        // Permanent Portals

        b.push("permanentPortals");
        PERMANENT_PORTALS_ENABLED = b.comment("Master toggle for sacrifice-activated permanent portals.").define("permanentPortalsEnabled", true);
        PERMANENT_PORTALS_FLINT_AND_STEEL_ENABLED = b.comment("Whether flint and steel can ignite permanent portal frames.").define("permanentPortalsFlintAndSteelEnabled", false);
        ELYTHIA_PORTAL_ENABLED = b.comment("Whether mossy ouranwood wood portals to Elythia can activate and function.").define("elythiaPortalEnabled", true);
        THORAXIS_PORTAL_ENABLED = b.comment("Whether Nyxite portals to Thoraxis can activate and function.").define("thoraxisPortalEnabled", true);
        CAVARYN_PORTAL_ENABLED = b.comment("Whether Myrmite portals to Cavaryn can activate and function.").define("cavarynPortalEnabled", true);
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
        GLOW_VINES_UNDER_LEAVES              = b.comment("Whether glow vines can be placed and survive when attached under leaf blocks.")       .define("glowVinesUnderLeaves",             true);
        SWING_THROUGH_GRASS_ENABLED          = b.comment("If true, empty collision plants like grass do not block melee target picking.")      .define("swingThroughGrassEnabled",         true);
        FABRIC_KEYBINDING_CONFLICT_FIX_ENABLED = b.define("fabricKeybindingConflictFixEnabled", true);
        EXPERIMENTAL_SETTINGS_POPUP_DISABLED = b.define("experimentalSettingsPopupDisabled", true);
        ENTITY_SPECIFIC_FIRE_OVERLAY_ENABLED = b.comment("If true, burning entities use soul fire / dream fire overlays where appropriate.")   .define("entitySpecificFireOverlayEnabled", true);
        b.pop();

        SPEC = b.build();
    }

    private AntarchyMiscConfig() {}

    static boolean elythiaFireflyParticlesEnabled()      { return ELYTHIA_FIREFLY_PARTICLES_ENABLED.get(); }

    static boolean dreamSandEnabled()                    { return DREAM_SAND_ENABLED.get(); }
    static double  dreamSandJumpVelocityMultiplier()     { return DREAM_SAND_JUMP_VELOCITY_MULTIPLIER.get(); }
    static double  dreamSandGravityMultiplier()          { return DREAM_SAND_GRAVITY_MULTIPLIER.get(); }
    static double  dreamSandEffectDurationSeconds()      { return DREAM_SAND_EFFECT_DURATION_SECONDS.get(); }
    static double  dreamSandFallingBlockGravityMultiplier() { return DREAM_SAND_FALLING_BLOCK_GRAVITY_MULTIPLIER.get(); }

    static boolean ichorWitherEnabled()                  { return ICHOR_WITHER_ENABLED.get(); }

    static boolean duplicatorTreeEnabled()               { return DUPLICATOR_TREE_ENABLED.get(); }
    static boolean glowVinesUnderLeaves()                { return GLOW_VINES_UNDER_LEAVES.get(); }
    static boolean swingThroughGrassEnabled()            { return SWING_THROUGH_GRASS_ENABLED.get(); }
    static boolean fabricKeybindingConflictFixEnabled()  { return FABRIC_KEYBINDING_CONFLICT_FIX_ENABLED.get(); }
    static boolean experimentalSettingsPopupDisabled()   { return EXPERIMENTAL_SETTINGS_POPUP_DISABLED.get(); }
    static boolean entitySpecificFireOverlayEnabled()    { return ENTITY_SPECIFIC_FIRE_OVERLAY_ENABLED.get(); }

    static double  hushweedSporeLifetimeSeconds()        { return HUSHWEED_SPORE_LIFETIME_SECONDS.get(); }

    static boolean disableInfinityBookPortalCreation()   { return DISABLE_INFINITY_BOOK_PORTAL_CREATION.get(); }
    static boolean rainbowAntsLeadToInfinityDimensions() { return RAINBOW_ANTS_LEAD_TO_INFINITY_DIMENSIONS.get(); }
    static double  rainbowAntInfinityEasterEggChance()   { return RAINBOW_ANT_INFINITY_EASTER_EGG_CHANCE.get(); }
    public static boolean permanentPortalsEnabled()      { return PERMANENT_PORTALS_ENABLED.get(); }
    public static boolean permanentPortalsFlintAndSteelEnabled() { return PERMANENT_PORTALS_FLINT_AND_STEEL_ENABLED.get(); }
    public static boolean elythiaPortalEnabled()         { return ELYTHIA_PORTAL_ENABLED.get(); }
    public static boolean thoraxisPortalEnabled()        { return THORAXIS_PORTAL_ENABLED.get(); }
    public static boolean cavarynPortalEnabled()         { return CAVARYN_PORTAL_ENABLED.get(); }

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
