package com.craisinlord.antarchy.neoforge;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AntarchyMobsConfig {
    public static final ModConfigSpec SPEC;


    // Ants

    private static final ModConfigSpec.BooleanValue ANTS_STEAL_FROM_CHESTS;

    private static final ModConfigSpec.BooleanValue BROWN_ANT_REQUIRES_REAGENT;
    private static final ModConfigSpec.DoubleValue  BROWN_ANT_HEALTH;
    private static final ModConfigSpec.DoubleValue  BROWN_ANT_MOVEMENT_SPEED;
    private static final ModConfigSpec.DoubleValue  BROWN_ANT_ATTACK_DAMAGE;
    private static final ModConfigSpec.ConfigValue<String> BROWN_ANT_DESTINATION_DIMENSION;

    private static final ModConfigSpec.BooleanValue RED_ANT_REQUIRES_REAGENT;
    private static final ModConfigSpec.DoubleValue  RED_ANT_HEALTH;
    private static final ModConfigSpec.DoubleValue  RED_ANT_MOVEMENT_SPEED;
    private static final ModConfigSpec.DoubleValue  RED_ANT_ATTACK_DAMAGE;
    private static final ModConfigSpec.ConfigValue<String> RED_ANT_DESTINATION_DIMENSION;

    private static final ModConfigSpec.BooleanValue RAINBOW_ANT_REQUIRES_REAGENT;
    private static final ModConfigSpec.DoubleValue  RAINBOW_ANT_HEALTH;
    private static final ModConfigSpec.DoubleValue  RAINBOW_ANT_MOVEMENT_SPEED;
    private static final ModConfigSpec.DoubleValue  RAINBOW_ANT_ATTACK_DAMAGE;
    private static final ModConfigSpec.ConfigValue<String> RAINBOW_ANT_NON_INFINITY_FALLBACK_DIMENSION;

    private static final ModConfigSpec.BooleanValue TERMITE_REQUIRES_REAGENT;
    private static final ModConfigSpec.DoubleValue  TERMITE_HEALTH;
    private static final ModConfigSpec.DoubleValue  TERMITE_MOVEMENT_SPEED;
    private static final ModConfigSpec.DoubleValue  TERMITE_ATTACK_DAMAGE;
    private static final ModConfigSpec.ConfigValue<String> TERMITE_DESTINATION_DIMENSION;



    // Easter Bunny

    private static final ModConfigSpec.BooleanValue EASTER_BUNNY_ENABLED;
    private static final ModConfigSpec.IntValue     EASTER_BUNNY_NATURAL_SPAWN_CHANCE_PERCENT;


    // Wasp

    private static final ModConfigSpec.DoubleValue WASP_HEALTH;
    private static final ModConfigSpec.DoubleValue WASP_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue WASP_MOVEMENT_SPEED;


    // Bomber

    private static final ModConfigSpec.DoubleValue BOMBER_HEALTH;
    private static final ModConfigSpec.DoubleValue BOMBER_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue BOMBER_EXPLOSION_DAMAGE;
    private static final ModConfigSpec.DoubleValue BOMBER_EXPLOSION_RADIUS;


    // Kraken

    private static final ModConfigSpec.DoubleValue  KRAKEN_HEALTH;
    private static final ModConfigSpec.DoubleValue  KRAKEN_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  KRAKEN_PROJECTILE_DAMAGE_TAKEN_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue KRAKEN_SQUID_SPAWN_ENABLED;
    private static final ModConfigSpec.BooleanValue KRAKEN_MASS_SPAWN_ENABLED;


    // Brutalfly

    private static final ModConfigSpec.DoubleValue BRUTALFLY_HEALTH;
    private static final ModConfigSpec.DoubleValue BRUTALFLY_SWIPE_DAMAGE;
    private static final ModConfigSpec.DoubleValue BRUTALFLY_SPIT_DAMAGE;


    // Mantis

    private static final ModConfigSpec.DoubleValue  MANTIS_HEALTH;
    private static final ModConfigSpec.DoubleValue  MANTIS_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  MANTIS_MOVEMENT_SPEED;
    private static final ModConfigSpec.DoubleValue  MANTIS_FLYING_SPEED;
    private static final ModConfigSpec.BooleanValue MANTIS_IGNORE_LIGHT_LEVEL;


    // Triffid

    private static final ModConfigSpec.DoubleValue TRIFFID_HEALTH;
    private static final ModConfigSpec.DoubleValue TRIFFID_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue TRIFFID_GRAB_DAMAGE;


    // Caterpillar

    private static final ModConfigSpec.DoubleValue CATERPILLAR_PUPATION_TIME_SECONDS;


    // Ant Dance

    private static final ModConfigSpec.DoubleValue ANT_DANCE_RADIUS;


    // Reverie

    private static final ModConfigSpec.DoubleValue  REVERIE_HEALTH;
    private static final ModConfigSpec.DoubleValue  REVERIE_INTEREST_RADIUS;
    private static final ModConfigSpec.DoubleValue  REVERIE_ABANDON_PLAYER_DISTANCE;
    private static final ModConfigSpec.IntValue     REVERIE_NOTICE_DURATION_TICKS;
    private static final ModConfigSpec.IntValue     REVERIE_INTEREST_DURATION_TICKS;
    private static final ModConfigSpec.IntValue     REVERIE_REBIND_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue     REVERIE_DAMAGE_REACTION_DURATION_TICKS;
    private static final ModConfigSpec.DoubleValue  REVERIE_DANGEROUS_FALL_DISTANCE;
    private static final ModConfigSpec.DoubleValue  REVERIE_DANGEROUS_FALL_SPEED;
    private static final ModConfigSpec.DoubleValue  REVERIE_WARNING_THREAT_RADIUS;
    private static final ModConfigSpec.DoubleValue  REVERIE_WARNING_THREAT_VERTICAL_RANGE;
    private static final ModConfigSpec.IntValue     REVERIE_DUPLICATION_COOLDOWN_TICKS;


    // Nightmare

    private static final ModConfigSpec.DoubleValue  NIGHTMARE_HEALTH;
    private static final ModConfigSpec.DoubleValue  NIGHTMARE_ATTACK_DAMAGE;


    // Basilisk

    private static final ModConfigSpec.BooleanValue BASILISK_PETRIFYING_GAZE_ENABLED;
    private static final ModConfigSpec.IntValue     BASILISK_SPAWN_MAX_LIGHT_LEVEL;
    private static final ModConfigSpec.DoubleValue  BASILISK_HEALTH;
    private static final ModConfigSpec.DoubleValue  BASILISK_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  BASILISK_MOVEMENT_SPEED;
    private static final ModConfigSpec.DoubleValue  BASILISK_ARMOR;
    private static final ModConfigSpec.DoubleValue  BASILISK_KNOCKBACK_RESISTANCE;
    private static final ModConfigSpec.DoubleValue  BASILISK_FOLLOW_RANGE;
    private static final ModConfigSpec.IntValue     BASILISK_XP_REWARD;
    private static final ModConfigSpec.IntValue     BASILISK_ATTACK_ANIM_TICKS;
    private static final ModConfigSpec.IntValue     BASILISK_ATTACK_DAMAGE_TICK;
    private static final ModConfigSpec.IntValue     BASILISK_HISS_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue  BASILISK_ATTACK_REACH;
    private static final ModConfigSpec.DoubleValue  BASILISK_GAZE_RANGE;
    private static final ModConfigSpec.DoubleValue  BASILISK_GAZE_DOT_THRESHOLD;
    private static final ModConfigSpec.DoubleValue  BASILISK_GAZE_FACING_THRESHOLD;
    private static final ModConfigSpec.IntValue     BASILISK_HISS_CHARGE_TICKS;
    private static final ModConfigSpec.IntValue     BASILISK_PLAYER_PARALYZE_TICKS;
    private static final ModConfigSpec.IntValue     BASILISK_PREY_PETRIFY_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue  BASILISK_PREY_PETRIFY_RANGE;
    private static final ModConfigSpec.IntValue     BASILISK_PREY_PETRIFY_TICKS;


    // Toreterror

    private static final ModConfigSpec.DoubleValue  TORETERROR_HEALTH;
    private static final ModConfigSpec.DoubleValue  TORETERROR_JUMP_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  TORETERROR_JUMP_ATTACK_KNOCKBACK;
    private static final ModConfigSpec.DoubleValue  TORETERROR_SPIN_DAMAGE;
    private static final ModConfigSpec.DoubleValue  TORETERROR_SPIN_KNOCKBACK;
    private static final ModConfigSpec.DoubleValue  TORETERROR_RANGED_WATER_BOMB_CHANCE;
    private static final ModConfigSpec.DoubleValue  WATER_BOMB_DAMAGE;
    private static final ModConfigSpec.IntValue     WATER_BOMB_LIFETIME_TICKS;
    private static final ModConfigSpec.DoubleValue  WATER_BOMB_GRAVITY;
    private static final ModConfigSpec.DoubleValue  WATER_BOMB_KNOCKBACK;
    private static final ModConfigSpec.DoubleValue  WATER_CANNON_COOLDOWN_SECONDS;


    // Lucid

    private static final ModConfigSpec.DoubleValue LUCID_ATTACK_RANGE;
    private static final ModConfigSpec.DoubleValue LUCID_PEARL_INVERTED_DURATION_SECONDS;


    // Dread effect

    private static final ModConfigSpec.BooleanValue DREAD_HALLUCINATION_SOUNDS_ENABLED;
    private static final ModConfigSpec.DoubleValue  DREAD_HALLUCINATION_SOUND_MIN_INTERVAL;
    private static final ModConfigSpec.DoubleValue  DREAD_HALLUCINATION_SOUND_MAX_INTERVAL;
    private static final ModConfigSpec.BooleanValue DREAD_HALLUCINATION_MOBS_ENABLED;
    private static final ModConfigSpec.DoubleValue  DREAD_HALLUCINATION_MOB_MIN_INTERVAL;
    private static final ModConfigSpec.DoubleValue  DREAD_HALLUCINATION_MOB_MAX_INTERVAL;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();


        // Ants (shared)

        ANTS_STEAL_FROM_CHESTS = b
                .comment("Whether ants will steal edible items from nearby chest inventories.")
                .define("antsStealFromChests", true);

        b.push("brownAnt");
        BROWN_ANT_REQUIRES_REAGENT = b
                .comment(
                    "If true, brown ants must be activated once with a reagent before they teleport.",
                    "Reagent tag:        data/antarchy/tags/item/brown_ant_activation_items.json",
                    "Breeding foods tag: data/antarchy/tags/item/brown_ant_breeding_foods.json"
                )
                .define("requiresReagent", false);
        BROWN_ANT_HEALTH          = b.comment("Base max health.")        .defineInRange("health",        5.0D, 0.1D, 1024.0D);
        BROWN_ANT_MOVEMENT_SPEED  = b.comment("Base movement speed.")    .defineInRange("movementSpeed", 0.28D, 0.0D, 10.0D);
        BROWN_ANT_ATTACK_DAMAGE   = b.comment("Base attack damage.")     .defineInRange("attackDamage",  1.0D, 0.0D, 1024.0D);
        BROWN_ANT_DESTINATION_DIMENSION = b
                .comment("Dimension id brown ants teleport players to.")
                .define("destinationDimension", "antarchy:elythia", AntarchyMobsConfig::isValidDimensionId);
        b.pop();

        b.push("redAnt");
        RED_ANT_REQUIRES_REAGENT = b
                .comment(
                    "If true, red ants must be activated once with a reagent before they teleport.",
                    "Reagent tag:        data/antarchy/tags/item/red_ant_activation_items.json",
                    "Breeding foods tag: data/antarchy/tags/item/red_ant_breeding_foods.json"
                )
                .define("requiresReagent", false);
        RED_ANT_HEALTH         = b.comment("Base max health.")        .defineInRange("health",        5.0D, 0.1D, 1024.0D);
        RED_ANT_MOVEMENT_SPEED = b.comment("Base movement speed.")    .defineInRange("movementSpeed", 0.30D, 0.0D, 10.0D);
        RED_ANT_ATTACK_DAMAGE  = b.comment("Base attack damage.")     .defineInRange("attackDamage",  3.0D, 0.0D, 1024.0D);
        RED_ANT_DESTINATION_DIMENSION = b
                .comment("Dimension id red ants teleport players to.")
                .define("destinationDimension", "antarchy:thoraxis", AntarchyMobsConfig::isValidDimensionId);
        b.pop();

        b.push("rainbowAnt");
        RAINBOW_ANT_REQUIRES_REAGENT = b
                .comment(
                    "If true, rainbow ants must be activated with a reagent before opening an Infinity destination.",
                    "Reagent tag:        data/antarchy/tags/item/rainbow_ant_activation_items.json",
                    "Breeding foods tag: data/antarchy/tags/item/rainbow_ant_breeding_foods.json"
                )
                .define("requiresReagent", true);
        RAINBOW_ANT_HEALTH         = b.comment("Base max health.")        .defineInRange("health",        5.0D, 0.1D, 1024.0D);
        RAINBOW_ANT_MOVEMENT_SPEED = b.comment("Base movement speed.")    .defineInRange("movementSpeed", 0.28D, 0.0D, 10.0D);
        RAINBOW_ANT_ATTACK_DAMAGE  = b.comment("Base attack damage.")     .defineInRange("attackDamage",  1.0D, 0.0D, 1024.0D);
        RAINBOW_ANT_NON_INFINITY_FALLBACK_DIMENSION = b
                .comment("Dimension used by rainbow ants when Infinity behavior is disabled.")
                .define("nonInfinityFallbackDimension", "antarchy:elythia", AntarchyMobsConfig::isValidDimensionId);
        b.pop();

        b.push("termite");
        TERMITE_REQUIRES_REAGENT = b
                .comment(
                    "If true, termites must be activated once with a reagent before they teleport.",
                    "Reagent tag:        data/antarchy/tags/item/termite_activation_items.json",
                    "Breeding foods tag: data/antarchy/tags/item/termite_breeding_foods.json"
                )
                .define("requiresReagent", false);
        TERMITE_HEALTH         = b.comment("Base max health.")        .defineInRange("health",        5.0D, 0.1D, 1024.0D);
        TERMITE_MOVEMENT_SPEED = b.comment("Base movement speed.")    .defineInRange("movementSpeed", 0.28D, 0.0D, 10.0D);
        TERMITE_ATTACK_DAMAGE  = b.comment("Base attack damage.")     .defineInRange("attackDamage",  1.0D, 0.0D, 1024.0D);
        TERMITE_DESTINATION_DIMENSION = b
                .comment("Dimension id termites teleport players to.")
                .define("destinationDimension", "antarchy:cavaryn", AntarchyMobsConfig::isValidDimensionId);
        b.pop();


        // Easter Bunny

        b.push("easterBunny");
        EASTER_BUNNY_ENABLED = b
                .comment(
                    "If false, Easter Bunnies stop laying spawn eggs.",
                    "Blacklist tag: data/antarchy/tags/item/easter_bunny_spawn_egg_blacklist.json"
                )
                .define("enabled", true);
        EASTER_BUNNY_NATURAL_SPAWN_CHANCE_PERCENT = b
                .comment(
                    "Percent chance a naturally spawned rabbit is replaced by an Easter Bunny.",
                    "Only applies to natural and chunk-generation spawns, not breeding."
                )
                .defineInRange("naturalSpawnChancePercent", 1, 0, 100);
        b.pop();


        // Wasp

        b.push("wasp");
        WASP_HEALTH         = b.comment("Base max health.")                            .defineInRange("health",       16.0D, 1.0D, 32768.0D);
        WASP_ATTACK_DAMAGE  = b.comment("Base attack damage.")                         .defineInRange("attackDamage", 4.0D,  0.0D, 1024.0D);
        WASP_MOVEMENT_SPEED = b.comment("Base wasp speed used for both flying and movement. Vanilla bee default is 0.3, so 0.39 is 30% faster.").defineInRange("movementSpeed", 0.39D, 0.0D, 10.0D);
        b.pop();


        // Bomber

        b.push("bomber");
        BOMBER_HEALTH           = b.comment("Base max health.")                                        .defineInRange("health",          10.0D, 1.0D, 32768.0D);
        BOMBER_ATTACK_DAMAGE    = b.comment("Direct damage dealt on contact before detonation.")       .defineInRange("attackDamage",     4.0D,  0.0D, 1024.0D);
        BOMBER_EXPLOSION_DAMAGE = b.comment("Extra damage dealt at the center of the explosion.")      .defineInRange("explosionDamage",  8.0D,  0.0D, 1024.0D);
        BOMBER_EXPLOSION_RADIUS = b.comment("Explosion radius when a bomber detonates. TNT uses 4.0.").defineInRange("explosionRadius",  4.0D,  0.0D, 128.0D);
        b.pop();


        // Kraken

        b.push("kraken");
        KRAKEN_HEALTH                            = b.comment("Base max health.")                                                                                            .defineInRange("health",                          1500.0D, 1.0D, 32768.0D);
        KRAKEN_ATTACK_DAMAGE                     = b.comment("Base attack damage.")                                                                                         .defineInRange("attackDamage",                     45.0D,  0.0D, 1024.0D);
        KRAKEN_PROJECTILE_DAMAGE_TAKEN_MULTIPLIER = b.comment("Damage multiplier from projectiles. 0.5 means 50% damage.")                                                  .defineInRange("projectileDamageTakenMultiplier",  0.5D,   0.0D, 10.0D);
        KRAKEN_SQUID_SPAWN_ENABLED               = b.comment("If true, killing a Missile Squid has a 1/100 chance to spawn a single Kraken nearby.")                       .define("squidSpawnEnabled",  true);
        KRAKEN_MASS_SPAWN_ENABLED                = b.comment("If true, killing a Missile Squid has a 1/100 chance to spawn 10 Krakens. Independent from the single spawn.").define("massSpawnEnabled",  true);
        b.pop();


        // Brutalfly

        b.push("brutalfly");
        BRUTALFLY_HEALTH      = b.comment("Base max health.")                                          .defineInRange("health",      200.0D, 1.0D, 32768.0D);
        BRUTALFLY_SWIPE_DAMAGE = b.comment("Damage dealt by a claw swipe.")                           .defineInRange("swipeDamage",  15.0D, 0.0D, 1024.0D);
        BRUTALFLY_SPIT_DAMAGE  = b.comment("Direct hit damage from a spit orb before lingering effect.").defineInRange("spitDamage",   5.0D, 0.0D, 1024.0D);
        b.pop();


        // Mantis

        b.push("mantis");
        MANTIS_HEALTH             = b.comment("Base max health.")                                                                                                 .defineInRange("health",            50.0D, 1.0D, 32768.0D);
        MANTIS_ATTACK_DAMAGE      = b.comment("Base attack damage.")                                                                                              .defineInRange("attackDamage",       9.0D, 0.0D, 1024.0D);
        MANTIS_MOVEMENT_SPEED     = b.comment("Base movement speed.")                                                                                             .defineInRange("movementSpeed",      0.42D, 0.0D, 10.0D);
        MANTIS_FLYING_SPEED       = b.comment("Base flying speed.")                                                                                               .defineInRange("flyingSpeed",        0.77D, 0.0D, 10.0D);
        MANTIS_IGNORE_LIGHT_LEVEL = b.comment("If false, mantises only spawn naturally in darkness (standard monster rules). If true, light level is ignored.").define("ignoreLightLevel", false);
        b.pop();


        // Triffid

        b.push("triffid");
        TRIFFID_HEALTH        = b.comment("Base max health.")                     .defineInRange("health",       80.0D, 1.0D, 32768.0D);
        TRIFFID_ATTACK_DAMAGE = b.comment("Damage dealt by the sweep attack.")   .defineInRange("attackDamage", 10.0D, 0.0D, 1024.0D);
        TRIFFID_GRAB_DAMAGE   = b.comment("Damage dealt by the grab attack.")    .defineInRange("grabDamage",   15.0D, 0.0D, 1024.0D);
        b.pop();


        // Caterpillar

        b.push("caterpillar");
        CATERPILLAR_PUPATION_TIME_SECONDS = b
                .comment("Time in seconds before a caterpillar emerges from its chrysalis.")
                .defineInRange("pupationTimeSeconds", 600.0D, 1.0D, 3600.0D);
        b.pop();


        // Ant Dance

        b.push("antDance");
        ANT_DANCE_RADIUS = b
                .comment("Radius in blocks where ants will dance around a playing jukebox.")
                .defineInRange("radius", 5.0D, 1.0D, 32.0D);
        b.pop();


        // Reverie

        b.push("reverie");
        REVERIE_HEALTH                         = b.comment("Base max health.")                                                                                         .defineInRange("health",                    20.0D, 1.0D, 1024.0D);
        REVERIE_INTEREST_RADIUS                = b.comment("How far the Reverie notices and starts following a player.")                                                .defineInRange("interestRadius",            16.0D, 1.0D, 64.0D);
        REVERIE_ABANDON_PLAYER_DISTANCE        = b.comment("Distance where the Reverie gives up and returns to ambient wandering.")                                     .defineInRange("abandonPlayerDistance",     24.0D, 1.0D, 96.0D);
        REVERIE_NOTICE_DURATION_TICKS          = b.comment("Ticks the Reverie watches a player before becoming bound.")                                                .defineInRange("noticeDurationTicks",          20, 1, 200);
        REVERIE_INTEREST_DURATION_TICKS        = b.comment("Ticks the Reverie stays interested after a color/state change.")                                           .defineInRange("interestDurationTicks",       360, 20, 7200);
        REVERIE_REBIND_COOLDOWN_TICKS          = b.comment("Ticks the Reverie waits after unbinding before it can bind again.")                                        .defineInRange("rebindCooldownTicks",         100, 0, 24000);
        REVERIE_DAMAGE_REACTION_DURATION_TICKS = b.comment("Ticks the Reverie stays in its hurt warning reaction after being damaged.")                                .defineInRange("damageReactionDurationTicks",  40, 1, 400);
        REVERIE_DANGEROUS_FALL_DISTANCE        = b.comment("Minimum fall distance before the Reverie considers its player in danger. Vanilla fall damage starts at 3.").defineInRange("dangerousFallDistance",      3.0D, 0.0D, 64.0D);
        REVERIE_DANGEROUS_FALL_SPEED           = b.comment("Minimum vertical speed before a dangerous fall triggers purple protection.")                                .defineInRange("dangerousFallSpeed",         0.18D, 0.0D, 4.0D);
        REVERIE_WARNING_THREAT_RADIUS          = b.comment("Horizontal scan radius for hostiles that trigger red warning mode.")                                        .defineInRange("warningThreatRadius",        10.0D, 1.0D, 64.0D);
        REVERIE_WARNING_THREAT_VERTICAL_RANGE  = b.comment("Vertical scan range for hostiles that trigger red warning mode.")                                           .defineInRange("warningThreatVerticalRange",  5.0D, 1.0D, 32.0D);
        REVERIE_DUPLICATION_COOLDOWN_TICKS     = b.comment("Cooldown in ticks before Blood Crystal can duplicate the Reverie again.")                                   .defineInRange("duplicationCooldownTicks",    6000, 20, 24000);
        b.pop();

        // Nightmare

        b.push("nightmare");
        NIGHTMARE_HEALTH = b.comment("Base max health.").defineInRange("health", 180.0D, 1.0D, 32768.0D);
        NIGHTMARE_ATTACK_DAMAGE = b.comment("Base attack damage.").defineInRange("attackDamage", 16.0D, 0.0D, 1024.0D);
        b.pop();


        // Basilisk

        b.push("basilisk");
        BASILISK_PETRIFYING_GAZE_ENABLED = b
                .comment("If false, the Basilisk will not use its petrifying gaze and will never apply the Paralyzed effect.")
                .define("petrifyingGazeEnabled", true);
        BASILISK_SPAWN_MAX_LIGHT_LEVEL = b
                .comment("Maximum local raw light level where basilisk natural spawning is allowed.")
                .defineInRange("spawnMaxLightLevel", 5, 0, 15);
        BASILISK_HEALTH = b.comment("Base max health.").defineInRange("health", 150.0D, 1.0D, 32768.0D);
        BASILISK_ATTACK_DAMAGE = b.comment("Base attack damage.").defineInRange("attackDamage", 18.0D, 0.0D, 1024.0D);
        BASILISK_MOVEMENT_SPEED = b.comment("Base movement speed.").defineInRange("movementSpeed", 0.35D, 0.0D, 10.0D);
        BASILISK_ARMOR = b.comment("Base armor value.").defineInRange("armor", 6.0D, 0.0D, 1024.0D);
        BASILISK_KNOCKBACK_RESISTANCE = b.comment("Base knockback resistance.").defineInRange("knockbackResistance", 0.5D, 0.0D, 1.0D);
        BASILISK_FOLLOW_RANGE = b.comment("Base follow range.").defineInRange("followRange", 28.0D, 1.0D, 128.0D);
        BASILISK_XP_REWARD = b.comment("Base experience reward.").defineInRange("xpReward", 50, 0, 100000);
        BASILISK_ATTACK_ANIM_TICKS = b.comment("Ticks for the attack animation window.").defineInRange("attackAnimTicks", 18, 1, 400);
        BASILISK_ATTACK_DAMAGE_TICK = b.comment("Tick during the attack animation when damage is applied.").defineInRange("attackDamageTick", 9, 0, 400);
        BASILISK_HISS_COOLDOWN_TICKS = b.comment("Ticks between petrifying gaze attempts.").defineInRange("hissCooldownTicks", 600, 0, 20000);
        BASILISK_ATTACK_REACH = b.comment("Forward bite reach in blocks measured from the basilisk's center.").defineInRange("attackReach", 3.0D, 0.0D, 128.0D);
        BASILISK_GAZE_RANGE = b.comment("Range in blocks for petrifying gaze checks.").defineInRange("gazeRange", 12.0D, 0.0D, 128.0D);
        BASILISK_GAZE_DOT_THRESHOLD = b.comment("Dot threshold for the player gaze check.").defineInRange("gazeDotThreshold", 0.64D, -1.0D, 1.0D);
        BASILISK_GAZE_FACING_THRESHOLD = b.comment("Dot threshold for the Basilisk facing check.").defineInRange("gazeFacingThreshold", 0.45D, -1.0D, 1.0D);
        BASILISK_HISS_CHARGE_TICKS = b.comment("Ticks a player must keep looking to trigger petrification.").defineInRange("hissChargeTicks", 30, 0, 2000);
        BASILISK_PLAYER_PARALYZE_TICKS = b.comment("Ticks the Paralyzed effect lasts on players.").defineInRange("playerParalyzeTicks", 50, 0, 20000);
        BASILISK_PREY_PETRIFY_COOLDOWN_TICKS = b.comment("Ticks between prey petrification checks.").defineInRange("preyPetrifyCooldownTicks", 500, 0, 20000);
        BASILISK_PREY_PETRIFY_RANGE = b.comment("Range in blocks for prey petrification checks.").defineInRange("preyPetrifyRange", 8.0D, 0.0D, 128.0D);
        BASILISK_PREY_PETRIFY_TICKS = b.comment("Ticks the prey petrify effect lasts.").defineInRange("preyPetrifyTicks", 200, 0, 20000);
        b.pop();


        // Lucid

        b.push("lucid");
        LUCID_ATTACK_RANGE = b
                .comment("Attack range in blocks. The Lucid fires its gravity blast when a target enters this radius.")
                .defineInRange("attackRange", 9.0D, 1.0D, 32.0D);
        LUCID_PEARL_INVERTED_DURATION_SECONDS = b
                .comment("How long the Inverted effect from a Lucid pearl lasts, in seconds.")
                .defineInRange("pearlInvertedDurationSeconds", 6.0D, 0.0D, 3600.0D);
        b.pop();


        // Dread effect

        b.push("dread");

        b.push("hallucinationSounds");
        DREAD_HALLUCINATION_SOUNDS_ENABLED     = b.comment("If false, the Dread effect will not play random hallucination sounds. Sound pool tag: data/antarchy/tags/sound_event/dread_hallucination_sounds.json").define("enabled", true);
        DREAD_HALLUCINATION_SOUND_MIN_INTERVAL = b.comment("Minimum seconds between hallucination sound triggers.").defineInRange("minIntervalSeconds",  3.0D, 0.1D, 300.0D);
        DREAD_HALLUCINATION_SOUND_MAX_INTERVAL = b.comment("Maximum seconds between hallucination sound triggers.").defineInRange("maxIntervalSeconds", 10.0D, 0.1D, 300.0D);
        b.pop();

        b.push("hallucinationMobs");
        DREAD_HALLUCINATION_MOBS_ENABLED     = b.comment("If false, the Dread effect will not render apparitions.").define("enabled", true);
        DREAD_HALLUCINATION_MOB_MIN_INTERVAL = b.comment("Minimum seconds between apparition spawns.").defineInRange("minIntervalSeconds",  6.0D, 0.1D, 300.0D);
        DREAD_HALLUCINATION_MOB_MAX_INTERVAL = b.comment("Maximum seconds between apparition spawns.").defineInRange("maxIntervalSeconds", 16.0D, 0.1D, 300.0D);
        b.pop();

        b.pop(); // dread

        b.push("toreterror");
        TORETERROR_HEALTH                  = b.comment("Base max health.").defineInRange("health", 300.0D, 1.0D, 32768.0D);
        TORETERROR_JUMP_ATTACK_DAMAGE      = b.comment("Damage dealt by the jump shockwave.").defineInRange("jumpAttackDamage", 14.0D, 0.0D, 1024.0D);
        TORETERROR_JUMP_ATTACK_KNOCKBACK   = b.comment("Knockback strength of the jump shockwave.").defineInRange("jumpAttackKnockback", 2.5D, 0.0D, 10.0D);
        TORETERROR_SPIN_DAMAGE             = b.comment("Damage per spin-attack tick.").defineInRange("spinDamage", 8.0D, 0.0D, 1024.0D);
        TORETERROR_SPIN_KNOCKBACK          = b.comment("Knockback strength of the spin attack.").defineInRange("spinKnockback", 1.5D, 0.0D, 10.0D);
        TORETERROR_RANGED_WATER_BOMB_CHANCE = b.comment("Chance (0.0-1.0) the ranged attack fires a Water Bomb instead of Bombers.").defineInRange("rangedWaterBombChance", 0.5D, 0.0D, 1.0D);
        WATER_BOMB_DAMAGE                  = b.comment("Damage dealt by a Water Bomb hit.").defineInRange("waterBombDamage", 6.0D, 0.0D, 1024.0D);
        WATER_BOMB_LIFETIME_TICKS          = b.comment("Ticks before a Water Bomb despawns.").defineInRange("waterBombLifetimeTicks", 120, 1, 6000);
        WATER_BOMB_GRAVITY                 = b.comment("Gravity applied to Water Bombs (higher = steeper arc).").defineInRange("waterBombGravity", 0.12D, 0.0D, 2.0D);
        WATER_BOMB_KNOCKBACK               = b.comment("Knockback strength when a Water Bomb hits an entity.").defineInRange("waterBombKnockback", 1.2D, 0.0D, 10.0D);
        WATER_CANNON_COOLDOWN_SECONDS      = b.comment("Cooldown in seconds between Water Cannon shots.").defineInRange("waterCannonCooldownSeconds", 1.5D, 0.0D, 300.0D);
        b.pop();

        SPEC = b.build();
    }

    private AntarchyMobsConfig() {}

    private static boolean isValidDimensionId(Object value) {
        return value instanceof String s && net.minecraft.resources.ResourceLocation.tryParse(s) != null;
    }

    static boolean antsStealFromChests()                    { return ANTS_STEAL_FROM_CHESTS.get(); }

    static boolean brownAntRequiresReagent()                { return BROWN_ANT_REQUIRES_REAGENT.get(); }
    static double  brownAntHealth()                         { return BROWN_ANT_HEALTH.get(); }
    static double  brownAntMovementSpeed()                  { return BROWN_ANT_MOVEMENT_SPEED.get(); }
    static double  brownAntAttackDamage()                   { return BROWN_ANT_ATTACK_DAMAGE.get(); }
    static String  brownAntDestinationDimension()           { return BROWN_ANT_DESTINATION_DIMENSION.get(); }

    static boolean redAntRequiresReagent()                  { return RED_ANT_REQUIRES_REAGENT.get(); }
    static double  redAntHealth()                           { return RED_ANT_HEALTH.get(); }
    static double  redAntMovementSpeed()                    { return RED_ANT_MOVEMENT_SPEED.get(); }
    static double  redAntAttackDamage()                     { return RED_ANT_ATTACK_DAMAGE.get(); }
    static String  redAntDestinationDimension()             { return RED_ANT_DESTINATION_DIMENSION.get(); }

    static boolean rainbowAntRequiresReagent()              { return RAINBOW_ANT_REQUIRES_REAGENT.get(); }
    static double  rainbowAntHealth()                       { return RAINBOW_ANT_HEALTH.get(); }
    static double  rainbowAntMovementSpeed()                { return RAINBOW_ANT_MOVEMENT_SPEED.get(); }
    static double  rainbowAntAttackDamage()                 { return RAINBOW_ANT_ATTACK_DAMAGE.get(); }
    static String  rainbowAntNonInfinityFallbackDimension() { return RAINBOW_ANT_NON_INFINITY_FALLBACK_DIMENSION.get(); }

    static boolean termiteRequiresReagent()                 { return TERMITE_REQUIRES_REAGENT.get(); }
    static double  termiteHealth()                          { return TERMITE_HEALTH.get(); }
    static double  termiteMovementSpeed()                   { return TERMITE_MOVEMENT_SPEED.get(); }
    static double  termiteAttackDamage()                    { return TERMITE_ATTACK_DAMAGE.get(); }
    static String  termiteDestinationDimension()            { return TERMITE_DESTINATION_DIMENSION.get(); }


    static boolean easterBunnyEnabled()                     { return EASTER_BUNNY_ENABLED.get(); }
    static int     easterBunnyNaturalSpawnChancePercent()   { return EASTER_BUNNY_NATURAL_SPAWN_CHANCE_PERCENT.get(); }

    static double  waspHealth()                             { return WASP_HEALTH.get(); }
    static double  waspAttackDamage()                       { return WASP_ATTACK_DAMAGE.get(); }
    static double  waspMovementSpeed()                      { return WASP_MOVEMENT_SPEED.get(); }

    static double  bomberHealth()                           { return BOMBER_HEALTH.get(); }
    static double  bomberAttackDamage()                     { return BOMBER_ATTACK_DAMAGE.get(); }
    static double  bomberExplosionDamage()                  { return BOMBER_EXPLOSION_DAMAGE.get(); }
    static double  bomberExplosionRadius()                  { return BOMBER_EXPLOSION_RADIUS.get(); }

    static double  krakenHealth()                           { return KRAKEN_HEALTH.get(); }
    static double  krakenAttackDamage()                     { return KRAKEN_ATTACK_DAMAGE.get(); }
    static double  krakenProjectileDamageTakenMultiplier()  { return KRAKEN_PROJECTILE_DAMAGE_TAKEN_MULTIPLIER.get(); }
    static boolean krakenSquidSpawnEnabled()                { return KRAKEN_SQUID_SPAWN_ENABLED.get(); }
    static boolean krakenMassSpawnEnabled()                 { return KRAKEN_MASS_SPAWN_ENABLED.get(); }

    static double  brutalflyHealth()                        { return BRUTALFLY_HEALTH.get(); }
    static double  brutalflySwipeDamage()                   { return BRUTALFLY_SWIPE_DAMAGE.get(); }
    static double  brutalflySpitDamage()                    { return BRUTALFLY_SPIT_DAMAGE.get(); }

    static double  mantisHealth()                           { return MANTIS_HEALTH.get(); }
    static double  mantisAttackDamage()                     { return MANTIS_ATTACK_DAMAGE.get(); }
    static double  mantisMovementSpeed()                    { return MANTIS_MOVEMENT_SPEED.get(); }
    static double  mantisFlyingSpeed()                      { return MANTIS_FLYING_SPEED.get(); }
    static boolean mantisIgnoreLightLevel()                 { return MANTIS_IGNORE_LIGHT_LEVEL.get(); }

    static double  triffidHealth()                          { return TRIFFID_HEALTH.get(); }
    static double  triffidAttackDamage()                    { return TRIFFID_ATTACK_DAMAGE.get(); }
    static double  triffidGrabDamage()                      { return TRIFFID_GRAB_DAMAGE.get(); }

    static double  caterpillarPupationTimeSeconds()         { return CATERPILLAR_PUPATION_TIME_SECONDS.get(); }
    static double  antDanceRadius()                         { return ANT_DANCE_RADIUS.get(); }

    static double  reverieHealth()                      { return REVERIE_HEALTH.get(); }
    static double  reverieInterestRadius()              { return REVERIE_INTEREST_RADIUS.get(); }
    static double  reverieAbandonPlayerDistance()       { return REVERIE_ABANDON_PLAYER_DISTANCE.get(); }
    static int     reverieNoticeDurationTicks()         { return REVERIE_NOTICE_DURATION_TICKS.get(); }
    static int     reverieInterestDurationTicks()       { return REVERIE_INTEREST_DURATION_TICKS.get(); }
    static int     reverieRebindCooldownTicks()         { return REVERIE_REBIND_COOLDOWN_TICKS.get(); }
    static int     reverieDamageReactionDurationTicks() { return REVERIE_DAMAGE_REACTION_DURATION_TICKS.get(); }
    static double  reverieDangerousFallDistance()       { return REVERIE_DANGEROUS_FALL_DISTANCE.get(); }
    static double  reverieDangerousFallSpeed()          { return REVERIE_DANGEROUS_FALL_SPEED.get(); }
    static double  reverieWarningThreatRadius()         { return REVERIE_WARNING_THREAT_RADIUS.get(); }
    static double  reverieWarningThreatVerticalRange()  { return REVERIE_WARNING_THREAT_VERTICAL_RANGE.get(); }
    static int     reverieDuplicationCooldownTicks()    { return REVERIE_DUPLICATION_COOLDOWN_TICKS.get(); }

    static double  nightmareHealth()                    { return NIGHTMARE_HEALTH.get(); }
    static double  nightmareAttackDamage()              { return NIGHTMARE_ATTACK_DAMAGE.get(); }

    static boolean basiliskPetrifyingGazeEnabled()          { return BASILISK_PETRIFYING_GAZE_ENABLED.get(); }
    static int     basiliskSpawnMaxLightLevel()             { return BASILISK_SPAWN_MAX_LIGHT_LEVEL.get(); }
    static double  basiliskHealth()                         { return BASILISK_HEALTH.get(); }
    static double  basiliskAttackDamage()                   { return BASILISK_ATTACK_DAMAGE.get(); }
    static double  basiliskMovementSpeed()                  { return BASILISK_MOVEMENT_SPEED.get(); }
    static double  basiliskArmor()                          { return BASILISK_ARMOR.get(); }
    static double  basiliskKnockbackResistance()            { return BASILISK_KNOCKBACK_RESISTANCE.get(); }
    static double  basiliskFollowRange()                    { return BASILISK_FOLLOW_RANGE.get(); }
    static int     basiliskXpReward()                       { return BASILISK_XP_REWARD.get(); }
    static int     basiliskAttackAnimTicks()                { return BASILISK_ATTACK_ANIM_TICKS.get(); }
    static int     basiliskAttackDamageTick()               { return BASILISK_ATTACK_DAMAGE_TICK.get(); }
    static int     basiliskHissCooldownTicks()              { return BASILISK_HISS_COOLDOWN_TICKS.get(); }
    static double  basiliskAttackReach()                    { return BASILISK_ATTACK_REACH.get(); }
    static double  basiliskGazeRange()                      { return BASILISK_GAZE_RANGE.get(); }
    static double  basiliskGazeDotThreshold()               { return BASILISK_GAZE_DOT_THRESHOLD.get(); }
    static double  basiliskGazeFacingThreshold()            { return BASILISK_GAZE_FACING_THRESHOLD.get(); }
    static int     basiliskHissChargeTicks()                { return BASILISK_HISS_CHARGE_TICKS.get(); }
    static int     basiliskPlayerParalyzeTicks()            { return BASILISK_PLAYER_PARALYZE_TICKS.get(); }
    static int     basiliskPreyPetrifyCooldownTicks()       { return BASILISK_PREY_PETRIFY_COOLDOWN_TICKS.get(); }
    static double  basiliskPreyPetrifyRange()               { return BASILISK_PREY_PETRIFY_RANGE.get(); }
    static int     basiliskPreyPetrifyTicks()               { return BASILISK_PREY_PETRIFY_TICKS.get(); }

    static double  lucidAttackRange()                       { return LUCID_ATTACK_RANGE.get(); }
    static double  lucidPearlInvertedDurationSeconds()      { return LUCID_PEARL_INVERTED_DURATION_SECONDS.get(); }

    static double  toreterrorHealth()                       { return TORETERROR_HEALTH.get(); }
    static double  toreterrorJumpAttackDamage()             { return TORETERROR_JUMP_ATTACK_DAMAGE.get(); }
    static double  toreterrorJumpAttackKnockback()          { return TORETERROR_JUMP_ATTACK_KNOCKBACK.get(); }
    static double  toreterrorSpinDamage()                   { return TORETERROR_SPIN_DAMAGE.get(); }
    static double  toreterrorSpinKnockback()                { return TORETERROR_SPIN_KNOCKBACK.get(); }
    static double  toreterrorRangedWaterBombChance()        { return TORETERROR_RANGED_WATER_BOMB_CHANCE.get(); }
    static double  waterBombDamage()                        { return WATER_BOMB_DAMAGE.get(); }
    static int     waterBombLifetimeTicks()                 { return WATER_BOMB_LIFETIME_TICKS.get(); }
    static double  waterBombGravity()                       { return WATER_BOMB_GRAVITY.get(); }
    static double  waterBombKnockback()                     { return WATER_BOMB_KNOCKBACK.get(); }
    static double  waterCannonCooldownSeconds()             { return WATER_CANNON_COOLDOWN_SECONDS.get(); }

    static boolean dreadHallucinationSoundsEnabled()        { return DREAD_HALLUCINATION_SOUNDS_ENABLED.get(); }
    static double  dreadHallucinationSoundMinInterval()     { return DREAD_HALLUCINATION_SOUND_MIN_INTERVAL.get(); }
    static double  dreadHallucinationSoundMaxInterval()     { return DREAD_HALLUCINATION_SOUND_MAX_INTERVAL.get(); }
    static boolean dreadHallucinationMobsEnabled()          { return DREAD_HALLUCINATION_MOBS_ENABLED.get(); }
    static double  dreadHallucinationMobMinInterval()       { return DREAD_HALLUCINATION_MOB_MIN_INTERVAL.get(); }
    static double  dreadHallucinationMobMaxInterval()       { return DREAD_HALLUCINATION_MOB_MAX_INTERVAL.get(); }
}
