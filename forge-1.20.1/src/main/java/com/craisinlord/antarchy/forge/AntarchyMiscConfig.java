package com.craisinlord.antarchy.forge;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AntarchyMiscConfig {
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue ELYTHIA_FIREFLY_PARTICLES_ENABLED;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_FOG_ENABLED;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_GROUND_MIST_ENABLED;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_GRASS_WIND_ENABLED;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_WIND_SOUNDS_ENABLED;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_WIND_LINES_ENABLED;
    private static final ForgeConfigSpec.IntValue     PRIMORDIAL_GRASS_DRAW_DISTANCE;
    private static final ForgeConfigSpec.BooleanValue PRIMORDIAL_GRASS_DYNAMIC;
    private static final ForgeConfigSpec.IntValue     PRIMORDIAL_GRASS_QUALITY;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_SOFT_CLOUDS_ENABLED;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_DREAM_SHADOWS_ENABLED;
    private static final ForgeConfigSpec.DoubleValue  ELYTHIA_DREAM_SHADOWS_STRENGTH;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_ALREADY_DREAMING;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_CLEAR_WATER;
    private static final ForgeConfigSpec.BooleanValue BROWN_ANT_DESCENT;
    private static final ForgeConfigSpec.BooleanValue RED_ANT_FLIP;
    private static final ForgeConfigSpec.BooleanValue TERMITE_BURROW;
    private static final ForgeConfigSpec.BooleanValue RAINBOW_ANT_PRISM;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_PURPLE_RAIN;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_ECHOES;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_EVENTS;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_RAIN_GROWTH;
    private static final ForgeConfigSpec.BooleanValue THORAXIS_EVENTS;
    private static final ForgeConfigSpec.BooleanValue THROWBACK_ZONES_ENABLED;
    private static final ForgeConfigSpec.IntValue     THROWBACK_TORCH_RADIUS;
    private static final ForgeConfigSpec.IntValue     THROWBACK_WILD_ZONE_MIN_RADIUS;
    private static final ForgeConfigSpec.IntValue     THROWBACK_WILD_ZONE_MAX_RADIUS;
    private static final ForgeConfigSpec.DoubleValue  THROWBACK_WILD_ZONE_CHANCE;
    private static final ForgeConfigSpec.BooleanValue THROWBACK_NO_ATTACK_COOLDOWN;
    private static final ForgeConfigSpec.BooleanValue THROWBACK_CLASSIC_HURT_SOUND;
    private static final ForgeConfigSpec.BooleanValue THROWBACK_CLASSIC_FOOD;
    private static final ForgeConfigSpec.BooleanValue THROWBACK_CLASSIC_MUSIC;
    private static final ForgeConfigSpec.BooleanValue THROWBACK_DENSE_FOG;
    private static final ForgeConfigSpec.BooleanValue THROWBACK_DREAM_HAZE;
    private static final ForgeConfigSpec.DoubleValue  THROWBACK_DREAM_STRENGTH;
    private static final ForgeConfigSpec.BooleanValue THROWBACK_AFTERIMAGES;

    private static final ForgeConfigSpec.DoubleValue  DREAM_SAND_FALLING_BLOCK_GRAVITY_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue  GIANT_LILY_PAD_LAUNCH_DISTANCE;

    private static final ForgeConfigSpec.BooleanValue ICHOR_WITHER_ENABLED;

    private static final ForgeConfigSpec.DoubleValue HUSHWEED_SPORE_LIFETIME_SECONDS;

    private static final ForgeConfigSpec.BooleanValue DISABLE_INFINITY_BOOK_PORTAL_CREATION;
    private static final ForgeConfigSpec.BooleanValue RAINBOW_ANTS_LEAD_TO_INFINITY_DIMENSIONS;
    private static final ForgeConfigSpec.DoubleValue RAINBOW_ANT_INFINITY_EASTER_EGG_CHANCE;

    private static final ForgeConfigSpec.BooleanValue PERMANENT_PORTALS_ENABLED;
    private static final ForgeConfigSpec.BooleanValue PERMANENT_PORTALS_FLINT_AND_STEEL_ENABLED;
    private static final ForgeConfigSpec.BooleanValue ELYTHIA_PORTAL_ENABLED;
    private static final ForgeConfigSpec.BooleanValue THORAXIS_PORTAL_ENABLED;
    private static final ForgeConfigSpec.BooleanValue CAVARYN_PORTAL_ENABLED;
    private static final ForgeConfigSpec.BooleanValue PORTAL_GUN_CAN_FIRE_THROUGH_GLASS;
    private static final ForgeConfigSpec.BooleanValue PORTAL_GUN_CAN_FIRE_THROUGH_LIQUID;
    private static final ForgeConfigSpec.BooleanValue PORTAL_GUN_CAN_PORTALS_RESIZE_WHEN_CREATED;
    private static final ForgeConfigSpec.BooleanValue PORTAL_GUN_CAN_PORTAL_PROJECTILES_CHUNKLOAD;
    private static final ForgeConfigSpec.BooleanValue PORTAL_GUN_SEE_THROUGH_PORTALS;
    private static final ForgeConfigSpec.BooleanValue PORTAL_GUN_FANCY_PORTALS;
    private static final ForgeConfigSpec.IntValue PORTAL_GUN_MAX_SHOOT_DISTANCE;
    private static final ForgeConfigSpec.DoubleValue PORTAL_GUN_MIN_FLOOR_EXIT_SPEED;
    private static final ForgeConfigSpec.IntValue PORTAL_GUN_MAX_LIVE_VIEWS;
    private static final ForgeConfigSpec.DoubleValue PORTAL_GUN_MAX_EXIT_SPEED;
    private static final ForgeConfigSpec.BooleanValue PORTAL_GUN_MOON_PULLS_BLOCKS;
    private static final ForgeConfigSpec.IntValue PORTAL_GUN_MOON_PORTAL_SECONDS;
    private static final ForgeConfigSpec.IntValue PORTAL_GUN_MOON_BLOCK_CAP;

    private static final ForgeConfigSpec.BooleanValue DIAMOND_MINECART_ENABLED;
    private static final ForgeConfigSpec.BooleanValue DIAMOND_MINECART_PLACES_RAILS;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_CRUISE_SPEED;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_MAX_SPEED;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_ACCELERATION;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_DECELERATION;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_COAST_DECELERATION;
    private static final ForgeConfigSpec.BooleanValue DIAMOND_MINECART_MOB_DAMAGE_ENABLED;
    private static final ForgeConfigSpec.DoubleValue  DIAMOND_MINECART_MAX_MOB_DAMAGE;

    private static final ForgeConfigSpec.DoubleValue HOVERBOARD_MAX_SPEED;
    private static final ForgeConfigSpec.DoubleValue HOVERBOARD_ACCELERATION;

    private static final ForgeConfigSpec.BooleanValue DUPLICATOR_TREE_ENABLED;
    private static final ForgeConfigSpec.BooleanValue FOSSILS_ENABLED;
    private static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> FOSSIL_ENTITY_BLACKLIST;
    private static final ForgeConfigSpec.BooleanValue GLOW_VINES_UNDER_LEAVES;
    private static final ForgeConfigSpec.BooleanValue SWING_THROUGH_GRASS_ENABLED;
    private static final ForgeConfigSpec.BooleanValue FABRIC_KEYBINDING_CONFLICT_FIX_ENABLED;
    private static final ForgeConfigSpec.BooleanValue EXPERIMENTAL_SETTINGS_POPUP_DISABLED;
    private static final ForgeConfigSpec.BooleanValue ENTITY_SPECIFIC_FIRE_OVERLAY_ENABLED;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();


        b.push("elythia");
        ELYTHIA_FIREFLY_PARTICLES_ENABLED = b
                .comment("Whether firefly ambient particles appear at night in the Elythia dimension.")
                .define("fireflyParticlesEnabled", true);
        ELYTHIA_FOG_ENABLED = b
                .comment("Whether Elythia uses its denser atmospheric fog. Client-side visual only.")
                .define("fogEnabled", true);
        ELYTHIA_GRASS_WIND_ENABLED = b.define("grassWindEnabled", true);
        PRIMORDIAL_GRASS_DRAW_DISTANCE = b.defineInRange("grassDrawDistance", 96, 32, 512);
        PRIMORDIAL_GRASS_DYNAMIC = b
                .comment("Animated grass drawn by its own renderer. Off draws it as an ordinary static block. Also in Video Settings.")
                .define("grassDynamic", true);
        PRIMORDIAL_GRASS_QUALITY = b
                .comment("Animated grass detail: 0 low, 1 medium, 2 high. Also in Video Settings.")
                .defineInRange("grassQuality", 1, 0, 2);
        ELYTHIA_WIND_SOUNDS_ENABLED = b.define("windSoundsEnabled", true);
        ELYTHIA_WIND_LINES_ENABLED = b.define("windLinesEnabled", true);
        ELYTHIA_GROUND_MIST_ENABLED = b
                .comment("Whether low ground mist drifts over Elythia meadows in the mornings. Client-side visual only.")
                .define("groundMistEnabled", true);
        ELYTHIA_SOFT_CLOUDS_ENABLED = b
                .comment("Whether the low, mid and towering Elythia clouds use soft translucent clouds instead of solid blocky ones. Client-side visual only.")
                .define("softCloudsEnabled", true);
        ELYTHIA_DREAM_SHADOWS_ENABLED = b
                .comment("Whether shadows, caves, and nights in Elythia are tinted lavender instead of fading to black. Client-side visual only.")
                .define("dreamShadowsEnabled", true);
        ELYTHIA_DREAM_SHADOWS_STRENGTH = b
                .comment("Strength of the Elythia lavender shadow tint, from 0.0 to 1.0.")
                .defineInRange("dreamShadowsStrength", 1.0D, 0.0D, 1.0D);
        ELYTHIA_ALREADY_DREAMING = b
                .comment("Whether beds in Elythia set your spawn and let you lie down at any time, but never skip the night.")
                .define("alreadyDreaming", true);
        ELYTHIA_CLEAR_WATER = b
                .comment("Whether water in Elythia is clearer and tinted turquoise. Client-side visual only.")
                .define("clearWater", true);
        BROWN_ANT_DESCENT = b
                .comment("Whether brown ant trips to Elythia play the Descent, sinking into a void and gliding down from the sky, and trips home play the Waking.")
                .define("brownAntDescent", true);
        RED_ANT_FLIP = b
                .comment("Whether red ant trips to and from Thoraxis play the Flip, where you fall upward as the world rolls over.")
                .define("redAntFlip", true);
        TERMITE_BURROW = b
                .comment("Whether termite trips to and from Cavaryn play the Burrow, crumbling through the ground or bursting back out.")
                .define("termiteBurrow", true);
        RAINBOW_ANT_PRISM = b
                .comment("Whether rainbow ant trips to and from Infinity dimensions play the Prism, splitting the world into color.")
                .define("rainbowAntPrism", true);
        ELYTHIA_PURPLE_RAIN = b
                .comment("Whether rain in Elythia is purple and falls slowly. Client-side visual only.")
                .define("purpleRain", true);
        ELYTHIA_ECHOES = b
                .comment("Whether a faint echo of yourself sometimes replays where you walked a few minutes earlier. Client-side visual only.")
                .define("echoes", true);
        ELYTHIA_EVENTS = b
                .comment("Whether Elythia rolls sky, weather, and creature events such as auroras, meteor showers, lumen rain, towering clouds, and deer migrations.")
                .define("events", true);
        ELYTHIA_RAIN_GROWTH = b
                .comment("Whether rain in Elythia makes exposed crops and saplings grow faster. Lumen rain doubles the effect.")
                .define("rainGrowth", true);
        THORAXIS_EVENTS = b
                .comment("Whether Thoraxis rolls events such as weightlessness, temporal bubbles, upward antiwater rain, the watching eyes, blind skies, and dreamstorms.")
                .define("thoraxisEvents", true);
        b.push("throwbackZones");
        THROWBACK_ZONES_ENABLED = b
                .comment("Whether Remembrance Torches create Throwback Zones.")
                .define("enabled", true);
        THROWBACK_TORCH_RADIUS = b
                .comment("How many blocks a placed or structure Remembrance Torch reaches. Nerd pole torches use wildZoneMinRadius and wildZoneMaxRadius instead.")
                .defineInRange("torchRadius", 24, 1, 128);
        THROWBACK_WILD_ZONE_MIN_RADIUS = b
                .comment("Smallest radius in blocks for a nerd pole torch. Applies to newly generated poles.")
                .defineInRange("wildZoneMinRadius", 48, 1, 128);
        THROWBACK_WILD_ZONE_MAX_RADIUS = b
                .comment("Largest radius in blocks for a nerd pole torch. Applies to newly generated poles. Treated as the minimum if set lower than it.")
                .defineInRange("wildZoneMaxRadius", 72, 1, 128);
        THROWBACK_WILD_ZONE_CHANCE = b
                .comment("Chance that each 320x320 block region of Elythia contains a free-standing Throwback Zone.")
                .defineInRange("wildZoneChance", 0.25D, 0.0D, 1.0D);
        THROWBACK_NO_ATTACK_COOLDOWN = b
                .comment("Whether players inside a Throwback Zone have no attack cooldown.")
                .define("noAttackCooldown", true);
        THROWBACK_CLASSIC_HURT_SOUND = b
                .comment("Whether players inside a Throwback Zone use the classic hurt sound.")
                .define("classicHurtSound", true);
        THROWBACK_CLASSIC_FOOD = b
                .comment("Whether hunger is hidden and paused inside a Throwback Zone, with food healing hearts directly like pre-Beta 1.8.")
                .define("classicFood", true);
        THROWBACK_CLASSIC_MUSIC = b
                .comment("Whether classic music plays inside a Throwback Zone.")
                .define("classicMusic", true);
        THROWBACK_DENSE_FOG = b
                .comment("Whether fog closes in inside a Throwback Zone.")
                .define("denseFog", true);
        THROWBACK_DREAM_HAZE = b
                .comment("Whether the dream post effect and Elythia color grade are rendered.")
                .define("dreamHaze", true);
        THROWBACK_DREAM_STRENGTH = b
                .comment("Strength of the dream post effect inside Throwback Zones, from 0.0 to 1.0.")
                .defineInRange("dreamStrength", 1.0D, 0.0D, 1.0D);
        THROWBACK_AFTERIMAGES = b
                .comment("Whether the very faint afterimage part of the dream effect is rendered.")
                .define("afterimages", true);
        b.pop();
        b.pop();


        b.push("giantLilyPad");
        GIANT_LILY_PAD_LAUNCH_DISTANCE = b.comment("Roughly how many blocks a Giant Lily Pad launches players and everything else on it when a player jumps from it.").defineInRange("giantLilyPadLaunchDistance", 4.0D, 0.0D, 32.0D);
        b.pop();

        b.push("dreamSand");
        DREAM_SAND_FALLING_BLOCK_GRAVITY_MULTIPLIER = b.comment("Multiplier applied to dream sand's falling-block gravity, i.e. how slowly it falls as a block.").defineInRange("fallingBlockGravityMultiplier", 0.4D, 0.0D, 16.0D);
        b.pop();


        b.push("ichor");
        ICHOR_WITHER_ENABLED = b
                .comment("If true, players submerged in ichor receive a Wither effect.")
                .define("witherEnabled", true);
        b.pop();


        b.push("hushweed");
        HUSHWEED_SPORE_LIFETIME_SECONDS = b
                .comment("How long (in seconds) a Hushweed spore projectile stays alive before disappearing.")
                .defineInRange("sporeLifetimeSeconds", 5.0D, 0.1D, 120.0D);
        b.pop();


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


        b.push("permanentPortals");
        PERMANENT_PORTALS_ENABLED = b.comment("Master toggle for sacrifice-activated permanent portals.").define("permanentPortalsEnabled", true);
        PERMANENT_PORTALS_FLINT_AND_STEEL_ENABLED = b.comment("Whether flint and steel can ignite permanent portal frames.").define("permanentPortalsFlintAndSteelEnabled", false);
        ELYTHIA_PORTAL_ENABLED = b.comment("Whether mossy ouranwood wood portals to Elythia can activate and function.").define("elythiaPortalEnabled", true);
        THORAXIS_PORTAL_ENABLED = b.comment("Whether Nyxite portals to Thoraxis can activate and function.").define("thoraxisPortalEnabled", true);
        CAVARYN_PORTAL_ENABLED = b.comment("Whether Myrmite portals to Cavaryn can activate and function.").define("cavarynPortalEnabled", true);
        b.pop();

        b.push("portalGun");
        PORTAL_GUN_CAN_FIRE_THROUGH_GLASS = b.define("canFireThroughGlass", false);
        PORTAL_GUN_CAN_FIRE_THROUGH_LIQUID = b.define("canFireThroughLiquid", false);
        PORTAL_GUN_CAN_PORTALS_RESIZE_WHEN_CREATED = b.define("canPortalsResizeWhenCreated", true);
        PORTAL_GUN_CAN_PORTAL_PROJECTILES_CHUNKLOAD = b.define("canPortalProjectilesChunkload", true);
        PORTAL_GUN_SEE_THROUGH_PORTALS = b.define("seeThroughPortals", true);
        PORTAL_GUN_FANCY_PORTALS = b.define("fancyPortals", true);
        PORTAL_GUN_MAX_SHOOT_DISTANCE = b.defineInRange("maxShootDistance", 10000, 1, Integer.MAX_VALUE);
        PORTAL_GUN_MAX_LIVE_VIEWS = b.comment("How many portals can show a live see-through view at once. Each one re-renders the world, so higher values cost frame rate. 0 shows only the animated surface.").defineInRange("maxLiveViews", 8, 0, 32);
        PORTAL_GUN_MIN_FLOOR_EXIT_SPEED = b.comment("Minimum speed in blocks/tick when leaving an upward-facing portal, so the traveller clears it. 0 disables.").defineInRange("minFloorExitSpeed", 0.6D, 0.0D, 4.0D);
        PORTAL_GUN_MAX_EXIT_SPEED = b.comment("Maximum speed in blocks/tick when leaving a portal. 0 keeps momentum uncapped.").defineInRange("maxExitSpeed", 0.0D, 0.0D, 100.0D);
        PORTAL_GUN_MOON_PULLS_BLOCKS = b.comment("Whether a portal opened by shooting the moon rips blocks loose and pulls them into space.").define("moonPullsBlocks", true);
        PORTAL_GUN_MOON_PORTAL_SECONDS = b.comment("How long a moon portal stays open, in seconds.").defineInRange("moonPortalSeconds", 10, 1, 120);
        PORTAL_GUN_MOON_BLOCK_CAP = b.comment("Maximum number of blocks a single moon portal can pull in.").defineInRange("moonBlockCap", 500, 0, 2000);
        b.pop();


        b.push("diamondMinecart");
        DIAMOND_MINECART_ENABLED            = b.comment("Master toggle for the diamond minecart's custom propulsion and rail-laying behavior.") .define("enabled",           true);
        DIAMOND_MINECART_PLACES_RAILS       = b.comment("Whether the cart automatically places normal rails from the rider's inventory.")       .define("placesRails",       true);
        DIAMOND_MINECART_CRUISE_SPEED       = b.comment("Minimum maintained speed while ridden without holding W. Set to 0 for manual control.") .defineInRange("cruiseSpeed",         0.0D,  0.0D, 2.0D);
        DIAMOND_MINECART_MAX_SPEED          = b.comment("Maximum speed in blocks/tick when W is held. Vanilla rideable minecarts cap at 0.4.")  .defineInRange("maxSpeed",            0.55D, 0.05D, 2.0D);
        DIAMOND_MINECART_ACCELERATION       = b.comment("Speed gained per tick while accelerating.")                                             .defineInRange("acceleration",        0.02D, 0.001D, 0.5D);
        DIAMOND_MINECART_DECELERATION       = b.comment("Speed lost per tick while S is held (active braking).")                                .defineInRange("deceleration",        0.05D, 0.001D, 0.5D);
        DIAMOND_MINECART_COAST_DECELERATION = b.comment("Speed lost per tick when neither W nor S is held.")                                     .defineInRange("coastDeceleration",   0.02D, 0.001D, 0.5D);
        DIAMOND_MINECART_MOB_DAMAGE_ENABLED = b.comment("Whether the cart damages mobs it hits while moving.")                                   .define("mobDamageEnabled",  false);
        DIAMOND_MINECART_MAX_MOB_DAMAGE     = b.comment("Maximum collision damage dealt at full speed if mob damage is enabled.")               .defineInRange("maxMobDamage",        4.0D,  0.0D, 200.0D);
        b.pop();


        b.push("hoverboard");
        HOVERBOARD_MAX_SPEED    = b.comment("Maximum hoverboard speed in blocks/tick while moving forward.").defineInRange("maxSpeed", 6.0D, 0.05D, 12.0D);
        HOVERBOARD_ACCELERATION = b.comment("Hoverboard speed gained per tick while moving forward. Lower values take longer to reach top speed.").defineInRange("acceleration", 0.05D, 0.001D, 1.0D);
        b.pop();


        b.push("world");
        DUPLICATOR_TREE_ENABLED              = b.comment("If false, duplicator trees do not generate and duplicator saplings produce nothing.").define("duplicatorTreeEnabled",           true);
        FOSSILS_ENABLED                      = b.comment("If false, Myrmite and Broodstone fossils no longer generate. Existing fossils still smelt into spawn eggs.").define("fossilsEnabled", true);
        FOSSIL_ENTITY_BLACKLIST              = b.comment("Entity ids fossils can never contain. Use modid:* to exclude every entity from a mod. Fossils that already hold a blacklisted entity are rerolled to an allowed one.")
                .defineListAllowEmpty("fossilEntityBlacklist", java.util.List.of(com.craisinlord.antarchy.config.AntarchySettings.defaultFossilEntityBlacklist().split(",")), entry -> entry instanceof String);
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
    static boolean elythiaFogEnabled()                   { return ELYTHIA_FOG_ENABLED.get(); }
    static boolean elythiaGrassWindEnabled() { return ELYTHIA_GRASS_WIND_ENABLED.get(); }
    static boolean elythiaWindSoundsEnabled() { return ELYTHIA_WIND_SOUNDS_ENABLED.get(); }
    static boolean elythiaWindLinesEnabled() { return ELYTHIA_WIND_LINES_ENABLED.get(); }
    static int primordialGrassDrawDistance() { return PRIMORDIAL_GRASS_DRAW_DISTANCE.get(); }
    static boolean primordialGrassDynamic() { return PRIMORDIAL_GRASS_DYNAMIC.get(); }
    static int primordialGrassQuality() { return PRIMORDIAL_GRASS_QUALITY.get(); }
    static void savePrimordialGrass(boolean dynamic, int quality) {
        PRIMORDIAL_GRASS_DYNAMIC.set(dynamic);
        PRIMORDIAL_GRASS_QUALITY.set(quality);
        SPEC.save();
    }
    static boolean elythiaGroundMistEnabled()            { return ELYTHIA_GROUND_MIST_ENABLED.get(); }
    static boolean elythiaSoftCloudsEnabled()            { return ELYTHIA_SOFT_CLOUDS_ENABLED.get(); }
    static boolean elythiaDreamShadowsEnabled()          { return ELYTHIA_DREAM_SHADOWS_ENABLED.get(); }
    static double  elythiaDreamShadowsStrength()         { return ELYTHIA_DREAM_SHADOWS_STRENGTH.get(); }
    static boolean elythiaAlreadyDreaming()             { return ELYTHIA_ALREADY_DREAMING.get(); }
    static boolean elythiaClearWater()                  { return ELYTHIA_CLEAR_WATER.get(); }
    static boolean brownAntDescent()                     { return BROWN_ANT_DESCENT.get(); }
    static boolean redAntFlip()                          { return RED_ANT_FLIP.get(); }
    static boolean termiteBurrow()                       { return TERMITE_BURROW.get(); }
    static boolean rainbowAntPrism()                     { return RAINBOW_ANT_PRISM.get(); }
    static boolean elythiaPurpleRain()                  { return ELYTHIA_PURPLE_RAIN.get(); }
    static boolean elythiaEchoes()                      { return ELYTHIA_ECHOES.get(); }
    static boolean elythiaEvents()                      { return ELYTHIA_EVENTS.get(); }
    static boolean elythiaRainGrowth()                  { return ELYTHIA_RAIN_GROWTH.get(); }
    static boolean thoraxisEvents()                     { return THORAXIS_EVENTS.get(); }
    static boolean throwbackZonesEnabled()               { return THROWBACK_ZONES_ENABLED.get(); }
    static int     throwbackTorchRadius()                { return THROWBACK_TORCH_RADIUS.get(); }
    static int     throwbackWildZoneMinRadius()          { return THROWBACK_WILD_ZONE_MIN_RADIUS.get(); }
    static int     throwbackWildZoneMaxRadius()          { return THROWBACK_WILD_ZONE_MAX_RADIUS.get(); }
    static double  throwbackWildZoneChance()             { return THROWBACK_WILD_ZONE_CHANCE.get(); }
    static boolean throwbackNoAttackCooldown()           { return THROWBACK_NO_ATTACK_COOLDOWN.get(); }
    static boolean throwbackClassicHurtSound()           { return THROWBACK_CLASSIC_HURT_SOUND.get(); }
    static boolean throwbackClassicFood()                { return THROWBACK_CLASSIC_FOOD.get(); }
    static boolean throwbackClassicMusic()               { return THROWBACK_CLASSIC_MUSIC.get(); }
    static boolean throwbackDenseFog()                   { return THROWBACK_DENSE_FOG.get(); }
    static boolean throwbackDreamHaze()                  { return THROWBACK_DREAM_HAZE.get(); }
    static double  throwbackDreamStrength()              { return THROWBACK_DREAM_STRENGTH.get(); }
    static boolean throwbackAfterimages()                { return THROWBACK_AFTERIMAGES.get(); }

    static double  dreamSandFallingBlockGravityMultiplier() { return DREAM_SAND_FALLING_BLOCK_GRAVITY_MULTIPLIER.get(); }
    static double  giantLilyPadLaunchDistance() { return GIANT_LILY_PAD_LAUNCH_DISTANCE.get(); }

    static boolean ichorWitherEnabled()                  { return ICHOR_WITHER_ENABLED.get(); }

    static boolean duplicatorTreeEnabled()               { return DUPLICATOR_TREE_ENABLED.get(); }
    static boolean fossilsEnabled()                      { return FOSSILS_ENABLED.get(); }
    static String  fossilEntityBlacklist()               { return String.join(",", FOSSIL_ENTITY_BLACKLIST.get()); }
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
    public static boolean portalGunCanFireThroughGlass() { return PORTAL_GUN_CAN_FIRE_THROUGH_GLASS.get(); }
    public static boolean portalGunCanFireThroughLiquid() { return PORTAL_GUN_CAN_FIRE_THROUGH_LIQUID.get(); }
    public static boolean portalGunCanPortalsResizeWhenCreated() { return PORTAL_GUN_CAN_PORTALS_RESIZE_WHEN_CREATED.get(); }
    public static boolean portalGunCanPortalProjectilesChunkload() { return PORTAL_GUN_CAN_PORTAL_PROJECTILES_CHUNKLOAD.get(); }
    public static boolean portalGunSeeThroughPortals() { return PORTAL_GUN_SEE_THROUGH_PORTALS.get(); }
    public static boolean portalGunFancyPortals() { return PORTAL_GUN_FANCY_PORTALS.get(); }
    public static int portalGunMaxShootDistance() { return PORTAL_GUN_MAX_SHOOT_DISTANCE.get(); }
    public static double portalGunMinFloorExitSpeed() { return PORTAL_GUN_MIN_FLOOR_EXIT_SPEED.get(); }
    public static int portalGunMaxLiveViews() { return PORTAL_GUN_MAX_LIVE_VIEWS.get(); }
    public static double portalGunMaxExitSpeed() { return PORTAL_GUN_MAX_EXIT_SPEED.get(); }
    public static boolean portalGunMoonPullsBlocks() { return PORTAL_GUN_MOON_PULLS_BLOCKS.get(); }
    public static int portalGunMoonPortalSeconds() { return PORTAL_GUN_MOON_PORTAL_SECONDS.get(); }
    public static int portalGunMoonBlockCap() { return PORTAL_GUN_MOON_BLOCK_CAP.get(); }

    static boolean diamondMinecartEnabled()              { return DIAMOND_MINECART_ENABLED.get(); }
    static boolean diamondMinecartPlacesRails()          { return DIAMOND_MINECART_PLACES_RAILS.get(); }
    static double  diamondMinecartCruiseSpeed()          { return DIAMOND_MINECART_CRUISE_SPEED.get(); }
    static double  diamondMinecartMaxSpeed()             { return DIAMOND_MINECART_MAX_SPEED.get(); }
    static double  diamondMinecartAcceleration()         { return DIAMOND_MINECART_ACCELERATION.get(); }
    static double  diamondMinecartDeceleration()         { return DIAMOND_MINECART_DECELERATION.get(); }
    static double  diamondMinecartCoastDeceleration()    { return DIAMOND_MINECART_COAST_DECELERATION.get(); }
    static boolean diamondMinecartMobDamageEnabled()     { return DIAMOND_MINECART_MOB_DAMAGE_ENABLED.get(); }
    static double  diamondMinecartMaxMobDamage()         { return DIAMOND_MINECART_MAX_MOB_DAMAGE.get(); }
    static double  hoverboardMaxSpeed()                  { return HOVERBOARD_MAX_SPEED.get(); }
    static double  hoverboardAcceleration()              { return HOVERBOARD_ACCELERATION.get(); }
}
