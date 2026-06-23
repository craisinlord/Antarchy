package com.craisinlord.antarchy.neoforge;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AntarchyToolsConfig {
    public static final ModConfigSpec SPEC;


    // Basilisk Dagger

    private static final ModConfigSpec.DoubleValue BASILISK_DAGGER_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue BASILISK_DAGGER_ATTACK_SPEED;
    private static final ModConfigSpec.IntValue    BASILISK_DAGGER_POISON_DURATION_TICKS;
    private static final ModConfigSpec.IntValue    BASILISK_DAGGER_POISON_AMPLIFIER;


    // Ultimate Tools

    private static final ModConfigSpec.DoubleValue  ULTIMATE_SWORD_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_SWORD_ATTACK_SPEED;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_PICKAXE_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_PICKAXE_ATTACK_SPEED;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_AXE_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_AXE_ATTACK_SPEED;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_SHOVEL_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_SHOVEL_ATTACK_SPEED;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_HOE_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_HOE_ATTACK_SPEED;
    private static final ModConfigSpec.IntValue     ULTIMATE_TOOL_ENCHANTABILITY;
    private static final ModConfigSpec.BooleanValue ULTIMATE_TOOLS_THREE_BY_THREE_ENABLED;


    // Ultimate Bow

    private static final ModConfigSpec.DoubleValue  ULTIMATE_BOW_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_BOW_PLAYER_HEAL;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_BOW_DRAW_SPEED_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue ULTIMATE_BOW_COMES_ENCHANTED_WITH_FLAME;
    private static final ModConfigSpec.IntValue     ULTIMATE_BOW_ENCHANTABILITY;


    // Ultimate Crossbow

    private static final ModConfigSpec.DoubleValue ULTIMATE_CROSSBOW_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue ULTIMATE_CROSSBOW_CHARGE_SPEED_MULTIPLIER;
    private static final ModConfigSpec.IntValue    ULTIMATE_CROSSBOW_ENCHANTABILITY;


    // Ultimate Mace

    private static final ModConfigSpec.DoubleValue ULTIMATE_MACE_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue ULTIMATE_MACE_ATTACK_SPEED;
    private static final ModConfigSpec.IntValue    ULTIMATE_MACE_ENCHANTABILITY;


    // Ultimate Armor

    private static final ModConfigSpec.BooleanValue ULTIMATE_ARMOR_COMES_ENCHANTED;
    private static final ModConfigSpec.IntValue     ULTIMATE_ARMOR_ENCHANTABILITY;
    private static final ModConfigSpec.IntValue     ULTIMATE_HELMET_ARMOR_VALUE;
    private static final ModConfigSpec.IntValue     ULTIMATE_CHESTPLATE_ARMOR_VALUE;
    private static final ModConfigSpec.IntValue     ULTIMATE_LEGGINGS_ARMOR_VALUE;
    private static final ModConfigSpec.IntValue     ULTIMATE_BOOTS_ARMOR_VALUE;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_HELMET_ARMOR_TOUGHNESS;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_CHESTPLATE_ARMOR_TOUGHNESS;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_LEGGINGS_ARMOR_TOUGHNESS;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_BOOTS_ARMOR_TOUGHNESS;
    private static final ModConfigSpec.DoubleValue  ULTIMATE_ARMOR_KNOCKBACK_RESISTANCE;


    // Battle Axe

    private static final ModConfigSpec.DoubleValue BATTLE_AXE_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue BATTLE_AXE_ATTACK_SPEED;


    // Big Bertha

    private static final ModConfigSpec.DoubleValue BIG_BERTHA_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue BIG_BERTHA_REACH_BONUS;
    private static final ModConfigSpec.DoubleValue BIG_BERTHA_ATTACK_SPEED;
    private static final ModConfigSpec.IntValue    BIG_BERTHA_BASILISK_PARALYZE_DURATION_TICKS;
    private static final ModConfigSpec.IntValue    BIG_BERTHA_KRAKEN_SLOW_TICKS;
    private static final ModConfigSpec.DoubleValue BIG_BERTHA_BASILISK_COOLDOWN_SECONDS;
    private static final ModConfigSpec.DoubleValue BIG_BERTHA_LUCID_INVERTED_DURATION_SECONDS;
    private static final ModConfigSpec.DoubleValue BIG_BERTHA_LUCID_INVERTED_DAMAGE_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue SCORPION_WHIP_BASE_DAMAGE;
    private static final ModConfigSpec.DoubleValue SCORPION_WHIP_REACH_BONUS;
    private static final ModConfigSpec.IntValue    SCORPION_WHIP_POISON_DURATION_TICKS;
    private static final ModConfigSpec.DoubleValue SCORPION_WHIP_TETHER_MAX_RANGE;
    private static final ModConfigSpec.IntValue    SCORPION_WHIP_REEL_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue SCORPION_WHIP_SNAP_BONUS_DAMAGE;
    private static final ModConfigSpec.IntValue    SCORPION_WHIP_SNAP_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue SCORPION_WHIP_PULL_STRENGTH;
    private static final ModConfigSpec.DoubleValue SCORPION_WHIP_HEAVY_PULL_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue SCORPION_WHIP_SELF_PULL_MULTIPLIER;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_KATANA_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue BLOOD_CRYSTAL_KATANA_LAUNCH_STRENGTH;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_KATANA_TRAIL_DURATION_TICKS;


    // Nightmare Gear

    private static final ModConfigSpec.IntValue    NIGHTMARE_HELMET_ARMOR_VALUE;
    private static final ModConfigSpec.IntValue    NIGHTMARE_CHESTPLATE_ARMOR_VALUE;
    private static final ModConfigSpec.IntValue    NIGHTMARE_LEGGINGS_ARMOR_VALUE;
    private static final ModConfigSpec.IntValue    NIGHTMARE_BOOTS_ARMOR_VALUE;
    private static final ModConfigSpec.DoubleValue NIGHTMARE_HELMET_ARMOR_TOUGHNESS;
    private static final ModConfigSpec.DoubleValue NIGHTMARE_CHESTPLATE_ARMOR_TOUGHNESS;
    private static final ModConfigSpec.DoubleValue NIGHTMARE_LEGGINGS_ARMOR_TOUGHNESS;
    private static final ModConfigSpec.DoubleValue NIGHTMARE_BOOTS_ARMOR_TOUGHNESS;
    private static final ModConfigSpec.DoubleValue NIGHTMARE_ARMOR_KNOCKBACK_RESISTANCE;
    private static final ModConfigSpec.DoubleValue NIGHTMARE_ARMOR_DREAD_AURA_RANGE_PER_PIECE;
    private static final ModConfigSpec.DoubleValue NIGHTMARE_SWORD_BASE_DAMAGE;
    private static final ModConfigSpec.DoubleValue NIGHTMARE_SWORD_ATTACK_SPEED;
    private static final ModConfigSpec.DoubleValue NIGHTMARE_SWORD_SCALING_FACTOR;


    // Fallen King's Crown

    private static final ModConfigSpec.IntValue    FALLEN_KING_CROWN_ARMOR_VALUE;
    private static final ModConfigSpec.DoubleValue FALLEN_KING_CROWN_ARMOR_TOUGHNESS;


    // Blood Crystal Armor

    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_ARMOR_SHIELD_RECHARGE_TICKS;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_APPLE_SHIELD_COUNT;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_APPLE_DURATION_TICKS;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_APPLE_SHIELD_RECHARGE_TICKS;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_HARD_MAX_SHIELDS;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_HELMET_DEFENSE;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_CHESTPLATE_DEFENSE;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_LEGGINGS_DEFENSE;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_BOOTS_DEFENSE;
    private static final ModConfigSpec.DoubleValue BLOOD_CRYSTAL_ARMOR_TOUGHNESS;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_HELMET_DURABILITY;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_CHESTPLATE_DURABILITY;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_LEGGINGS_DURABILITY;
    private static final ModConfigSpec.IntValue    BLOOD_CRYSTAL_BOOTS_DURABILITY;


    // Squidzooka

    private static final ModConfigSpec.DoubleValue SQUIDZOOKA_COOLDOWN_SECONDS;
    private static final ModConfigSpec.DoubleValue SQUIDZOOKA_LAUNCH_VELOCITY;


    // Size Rays

    private static final ModConfigSpec.BooleanValue SIZE_CHANGING_RAYS_ENABLED;
    private static final ModConfigSpec.DoubleValue  SIZE_RAY_COOLDOWN_SECONDS;
    private static final ModConfigSpec.DoubleValue  SIZE_RAY_MIN_SCALE;
    private static final ModConfigSpec.DoubleValue  SIZE_RAY_MAX_SCALE;
    private static final ModConfigSpec.DoubleValue  SIZE_RAY_DELTA_PER_HIT;
    private static final ModConfigSpec.DoubleValue  SHRINKING_POTION_DELTA;
    private static final ModConfigSpec.DoubleValue  GROWTH_POTION_DELTA;


    // Gravity Gun

    private static final ModConfigSpec.BooleanValue GRAVITY_GUN_ENABLED;
    private static final ModConfigSpec.BooleanValue GRAVITY_GUN_BLOCKS_ENABLED;
    private static final ModConfigSpec.BooleanValue GRAVITY_GUN_ENTITIES_ENABLED;
    private static final ModConfigSpec.DoubleValue  GRAVITY_GUN_RANGE;
    private static final ModConfigSpec.DoubleValue  GRAVITY_GUN_THROW_STRENGTH;
    private static final ModConfigSpec.DoubleValue  GRAVITY_GUN_BLAST_STRENGTH;
    private static final ModConfigSpec.DoubleValue  GRAVITY_GUN_COOLDOWN_SECONDS;
    private static final ModConfigSpec.DoubleValue  GRAVITY_GUN_MAX_HOLD_DISTANCE;


    // Miscellaneous items / consumables

    private static final ModConfigSpec.DoubleValue  DUCT_TAPE_REPAIR_PERCENT_PER_USE;
    private static final ModConfigSpec.DoubleValue  POTENT_NYXITE_INVERTED_DURATION_SECONDS;
    private static final ModConfigSpec.BooleanValue INVERT_PROJECTILES_FROM_INVERTED_PLAYERS;
    private static final ModConfigSpec.IntValue     CORNEA_EAR_NIGHT_VISION_SECONDS;

    // Moggles

    private static final ModConfigSpec.IntValue    MOGGLES_VISION_RADIUS;
    private static final ModConfigSpec.IntValue    MOGGLES_VISION_MAX_LIGHT;
    private static final ModConfigSpec.DoubleValue MOGGLES_VISION_ALPHA;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();


        // Basilisk Dagger

        b.push("basiliskDagger");
        BASILISK_DAGGER_ATTACK_DAMAGE         = b.comment("Attack damage.")                        .defineInRange("attackDamage",         6.0D, 0.0D, 1024.0D);
        BASILISK_DAGGER_ATTACK_SPEED          = b.comment("Attack speed.")                         .defineInRange("attackSpeed",          -1.8D, -10.0D, 10.0D);
        BASILISK_DAGGER_POISON_DURATION_TICKS = b.comment("Poison duration applied on hit.")       .defineInRange("poisonDurationTicks",   200, 0, 20000);
        BASILISK_DAGGER_POISON_AMPLIFIER      = b.comment("Poison level applied on hit (0 = I).")  .defineInRange("poisonAmplifier",         2, 0, 10);
        b.pop();


        // Ultimate Tools

        b.push("ultimateTools");
        ULTIMATE_SWORD_ATTACK_DAMAGE   = b.comment("Attack damage of the Ultimate Sword.")                                          .defineInRange("swordAttackDamage",    30.0D,          0.0D, 1024.0D);
        ULTIMATE_SWORD_ATTACK_SPEED    = b.comment("Attack speed of the Ultimate Sword.")                                           .defineInRange("swordAttackSpeed",      -2.4D,        -10.0D, 10.0D);
        ULTIMATE_PICKAXE_ATTACK_DAMAGE = b.comment("Attack damage of the Ultimate Pickaxe.")                                        .defineInRange("pickaxeAttackDamage",  22.0D, 0.0D, 1024.0D);
        ULTIMATE_PICKAXE_ATTACK_SPEED  = b.comment("Attack speed of the Ultimate Pickaxe.")                                         .defineInRange("pickaxeAttackSpeed",    -2.8D,        -10.0D, 10.0D);
        ULTIMATE_AXE_ATTACK_DAMAGE     = b.comment("Attack damage of the Ultimate Axe.")                                            .defineInRange("axeAttackDamage",      38.0D, 0.0D, 1024.0D);
        ULTIMATE_AXE_ATTACK_SPEED      = b.comment("Attack speed of the Ultimate Axe.")                                             .defineInRange("axeAttackSpeed",        -3.0D,        -10.0D, 10.0D);
        ULTIMATE_SHOVEL_ATTACK_DAMAGE  = b.comment("Attack damage of the Ultimate Shovel.")                                         .defineInRange("shovelAttackDamage",   24.0D, 0.0D, 1024.0D);
        ULTIMATE_SHOVEL_ATTACK_SPEED   = b.comment("Attack speed of the Ultimate Shovel.")                                          .defineInRange("shovelAttackSpeed",     -3.0D,        -10.0D, 10.0D);
        ULTIMATE_HOE_ATTACK_DAMAGE     = b.comment("Attack damage of the Ultimate Hoe.")                                            .defineInRange("hoeAttackDamage",       8.0D, 0.0D, 1024.0D);
        ULTIMATE_HOE_ATTACK_SPEED      = b.comment("Attack speed of the Ultimate Hoe.")                                             .defineInRange("hoeAttackSpeed",         0.0D,        -10.0D, 10.0D);
        ULTIMATE_TOOL_ENCHANTABILITY   = b.comment("Enchantability for all Ultimate tools.")                                        .defineInRange("toolEnchantability",      25,           0, 100);
        ULTIMATE_TOOLS_THREE_BY_THREE_ENABLED = b
                .comment("If false, Ultimate pickaxe, axe, shovel, and hoe lose their 3x3 mining mode and tooltip.")
                .define("threeByThreeEnabled", true);
        b.pop();


        // Ultimate Bow

        b.push("ultimateBow");
        ULTIMATE_BOW_ATTACK_DAMAGE              = b.comment("Damage dealt to non-player targets.")                                        .defineInRange("attackDamage",            18.0D, 0.0D, 1024.0D);
        ULTIMATE_BOW_PLAYER_HEAL                = b.comment("Health restored to players hit by arrows from this bow.")                    .defineInRange("playerHeal",               8.0D, 0.0D, 1024.0D);
        ULTIMATE_BOW_DRAW_SPEED_MULTIPLIER      = b.comment("Draw speed relative to a normal bow. 2.5 = 2.5x as fast.")                 .defineInRange("drawSpeedMultiplier",      2.5D, 0.1D, 16.0D);
        ULTIMATE_BOW_COMES_ENCHANTED_WITH_FLAME = b.comment("If true, Ultimate Bows are automatically given and keep the Flame enchant.") .define("comesEnchantedWithFlame", true);
        ULTIMATE_BOW_ENCHANTABILITY             = b.comment("Enchantability of the Ultimate Bow.")                                        .defineInRange("enchantability",             20, 0, 100);
        b.pop();


        // Ultimate Crossbow

        b.push("ultimateCrossbow");
        ULTIMATE_CROSSBOW_ATTACK_DAMAGE           = b.comment("Damage dealt by projectiles fired from the Ultimate Crossbow.").defineInRange("attackDamage",           8.0D, 0.0D, 1024.0D);
        ULTIMATE_CROSSBOW_CHARGE_SPEED_MULTIPLIER = b
                .comment(
                    "Charge speed relative to a normal crossbow. Below 1.0 is slower.",
                    "Default 0.25 means 4x slower (16-arrow full load ~5 s). Quick Charge still applies."
                )
                .defineInRange("chargeSpeedMultiplier",  0.25D, 0.01D, 16.0D);
        ULTIMATE_CROSSBOW_ENCHANTABILITY          = b.comment("Enchantability of the Ultimate Crossbow.").defineInRange("enchantability", 20, 0, 100);
        b.pop();


        // Ultimate Mace

        b.push("ultimateMace");
        ULTIMATE_MACE_DAMAGE_MULTIPLIER = b.comment("Multiplier on both base damage and smash bonus. 1.5 = 50% stronger than a normal mace.").defineInRange("damageMultiplier", 1.5D, 0.1D, 16.0D);
        ULTIMATE_MACE_ATTACK_SPEED      = b.comment("Attack speed of the Ultimate Mace.")                                         .defineInRange("attackSpeed",       -3.4D, -10.0D, 10.0D);
        ULTIMATE_MACE_ENCHANTABILITY    = b.comment("Enchantability of the Ultimate Mace.")                                       .defineInRange("enchantability",       20, 0, 100);
        b.pop();


        // Ultimate Armor

        b.push("ultimateArmor");
        ULTIMATE_ARMOR_COMES_ENCHANTED       = b.comment("If true, newly crafted Ultimate Armor has Protection, Fire Protection, Projectile Protection, and Blast Protection.").define("comesEnchanted", true);
        ULTIMATE_ARMOR_ENCHANTABILITY        = b.comment("Enchantability for all Ultimate Armor pieces.")                          .defineInRange("enchantability",        10, 0, 100);
        ULTIMATE_HELMET_ARMOR_VALUE          = b.comment("Armor points of the Ultimate Helmet.")                                   .defineInRange("helmetArmor",            4, 0, 100);
        ULTIMATE_CHESTPLATE_ARMOR_VALUE      = b.comment("Armor points of the Ultimate Chestplate.")                               .defineInRange("chestplateArmor",        9, 0, 100);
        ULTIMATE_LEGGINGS_ARMOR_VALUE        = b.comment("Armor points of the Ultimate Leggings.")                                 .defineInRange("leggingsArmor",          7, 0, 100);
        ULTIMATE_BOOTS_ARMOR_VALUE           = b.comment("Armor points of the Ultimate Boots.")                                    .defineInRange("bootsArmor",             4, 0, 100);
        ULTIMATE_HELMET_ARMOR_TOUGHNESS      = b.comment("Armor toughness of the Ultimate Helmet.")                                .defineInRange("helmetToughness",       3.0D, 0.0D, 100.0D);
        ULTIMATE_CHESTPLATE_ARMOR_TOUGHNESS  = b.comment("Armor toughness of the Ultimate Chestplate.")                            .defineInRange("chestplateToughness",   3.0D, 0.0D, 100.0D);
        ULTIMATE_LEGGINGS_ARMOR_TOUGHNESS    = b.comment("Armor toughness of the Ultimate Leggings.")                              .defineInRange("leggingsToughness",     3.0D, 0.0D, 100.0D);
        ULTIMATE_BOOTS_ARMOR_TOUGHNESS       = b.comment("Armor toughness of the Ultimate Boots.")                                 .defineInRange("bootsToughness",        3.0D, 0.0D, 100.0D);
        ULTIMATE_ARMOR_KNOCKBACK_RESISTANCE  = b.comment("Knockback resistance granted by each Ultimate Armor piece.")             .defineInRange("knockbackResistance",   0.1D, 0.0D, 1.0D);
        b.pop();


        // Battle Axe

        b.push("battleAxe");
        BATTLE_AXE_ATTACK_DAMAGE = b.comment("Attack damage of the Battle Axe.").defineInRange("attackDamage", 44.0D, 0.0D, 4096.0D);
        BATTLE_AXE_ATTACK_SPEED  = b.comment("Attack speed of the Battle Axe.") .defineInRange("attackSpeed",  -3.1D, -10.0D, 10.0D);
        b.pop();


        // Big Bertha

        b.push("bigBertha");
        BIG_BERTHA_ATTACK_DAMAGE                     = b.comment("Attack damage of Big Bertha.")                                            .defineInRange("attackDamage",                   55.0D, 0.0D, 4096.0D);
        BIG_BERTHA_REACH_BONUS                       = b.comment("Extra attack range in blocks while held in main hand.")                    .defineInRange("reachBonus",                      3.0D, 0.0D, 32.0D);
        BIG_BERTHA_ATTACK_SPEED                      = b.comment("Attack speed of Big Bertha.")                                             .defineInRange("attackSpeed",                    -3.0D, -10.0D, 10.0D);
        BIG_BERTHA_BASILISK_PARALYZE_DURATION_TICKS  = b.comment("Paralyze duration used by Basilisk mode.")                                .defineInRange("basiliskParalyzeDurationTicks",    80, 0, 20000);
        BIG_BERTHA_KRAKEN_SLOW_TICKS                 = b.comment("Slow duration used by Kraken mode.")                                      .defineInRange("krakenSlowTicks",                  80, 0, 20000);
        BIG_BERTHA_BASILISK_COOLDOWN_SECONDS         = b.comment("Cooldown in seconds after using Basilisk mode's right-click paralyze.")   .defineInRange("basiliskModeCooldownSeconds",     7.0D, 0.0D, 3600.0D);
        BIG_BERTHA_LUCID_INVERTED_DURATION_SECONDS   = b.comment("How long Lucid mode's inversion lasts on a target, in seconds.")          .defineInRange("lucidModeInvertedDurationSeconds", 3.0D, 0.0D, 3600.0D);
        BIG_BERTHA_LUCID_INVERTED_DAMAGE_BONUS_PERCENT = b.comment("Bonus damage percent from Lucid mode while the wielder is inverted.")    .defineInRange("lucidModeInvertedDamageBonusPercent", 25.0D, 0.0D, 1000.0D);
        b.pop();

        b.push("scorpionWhip");
        SCORPION_WHIP_BASE_DAMAGE = b.comment("Base lash damage of the Scorpion Stinger Whip.").defineInRange("baseDamage", 10.0D, 0.0D, 4096.0D);
        SCORPION_WHIP_REACH_BONUS = b.comment("Extra entity interaction range in blocks while held in main hand.").defineInRange("reachBonus", 5.0D, 0.0D, 32.0D);
        SCORPION_WHIP_POISON_DURATION_TICKS = b.comment("Poison duration applied on lash.").defineInRange("poisonDurationTicks", 100, 0, 20000);
        SCORPION_WHIP_TETHER_MAX_RANGE = b.comment("Maximum tether distance before it breaks.").defineInRange("tetherMaxRange", 10.0D, 0.0D, 128.0D);
        SCORPION_WHIP_REEL_COOLDOWN_TICKS = b.comment("Cooldown in ticks between reel pulls.").defineInRange("reelCooldownTicks", 10, 0, 20000);
        SCORPION_WHIP_SNAP_BONUS_DAMAGE = b.comment("Bonus damage dealt when snapping the tether free.").defineInRange("snapBonusDamage", 6.0D, 0.0D, 4096.0D);
        SCORPION_WHIP_SNAP_COOLDOWN_TICKS = b.comment("Cooldown in ticks after a snap release.").defineInRange("snapCooldownTicks", 30, 0, 20000);
        SCORPION_WHIP_PULL_STRENGTH = b.comment("Pull strength applied to normal tethered targets.").defineInRange("pullStrength", 0.75D, 0.0D, 8.0D);
        SCORPION_WHIP_HEAVY_PULL_MULTIPLIER = b.comment("Reduced pull strength applied to heavy targets.").defineInRange("heavyPullMultiplier", 0.25D, 0.0D, 8.0D);
        SCORPION_WHIP_SELF_PULL_MULTIPLIER = b.comment("Self-pull strength toward heavy tethered targets.").defineInRange("selfPullMultiplier", 0.45D, 0.0D, 8.0D);
        b.pop();

        b.push("bloodCrystalKatana");
        BLOOD_CRYSTAL_KATANA_ATTACK_DAMAGE = b.comment("Attack damage of the Blood Crystal Katana.").defineInRange("attackDamage", 7, 0, 1024);
        BLOOD_CRYSTAL_KATANA_LAUNCH_STRENGTH = b.comment("Forward launch strength applied to the wielder when the katana hits.").defineInRange("launchStrength", 1.1D, 0.0D, 8.0D);
        BLOOD_CRYSTAL_KATANA_TRAIL_DURATION_TICKS = b.comment("How long the crimson trail lingers after a katana hit, in ticks.").defineInRange("trailDurationTicks", 12, 0, 200);
        b.pop();


        // Nightmare Gear

        b.push("nightmareArmor");
        NIGHTMARE_HELMET_ARMOR_VALUE              = b.comment("Armor points of the Nightmare Helmet. (Netherite default: 3)")               .defineInRange("helmetArmor",           3, 0, 100);
        NIGHTMARE_CHESTPLATE_ARMOR_VALUE          = b.comment("Armor points of the Nightmare Chestplate. (Netherite default: 8)")           .defineInRange("chestplateArmor",       8, 0, 100);
        NIGHTMARE_LEGGINGS_ARMOR_VALUE            = b.comment("Armor points of the Nightmare Leggings. (Netherite default: 6)")             .defineInRange("leggingsArmor",         6, 0, 100);
        NIGHTMARE_BOOTS_ARMOR_VALUE               = b.comment("Armor points of the Nightmare Boots. (Netherite default: 3)")                .defineInRange("bootsArmor",            3, 0, 100);
        NIGHTMARE_HELMET_ARMOR_TOUGHNESS          = b.comment("Armor toughness of the Nightmare Helmet.")                                   .defineInRange("helmetToughness",      3.0D, 0.0D, 100.0D);
        NIGHTMARE_CHESTPLATE_ARMOR_TOUGHNESS      = b.comment("Armor toughness of the Nightmare Chestplate.")                               .defineInRange("chestplateToughness",  3.0D, 0.0D, 100.0D);
        NIGHTMARE_LEGGINGS_ARMOR_TOUGHNESS        = b.comment("Armor toughness of the Nightmare Leggings.")                                 .defineInRange("leggingsToughness",    3.0D, 0.0D, 100.0D);
        NIGHTMARE_BOOTS_ARMOR_TOUGHNESS           = b.comment("Armor toughness of the Nightmare Boots.")                                    .defineInRange("bootsToughness",       3.0D, 0.0D, 100.0D);
        NIGHTMARE_ARMOR_KNOCKBACK_RESISTANCE      = b.comment("Knockback resistance granted by each Nightmare Armor piece.")                .defineInRange("knockbackResistance",  0.1D, 0.0D, 1.0D);
        NIGHTMARE_ARMOR_DREAD_AURA_RANGE_PER_PIECE = b
                .comment(
                    "Dread Aura radius added per Nightmare Armor piece worn.",
                    "Full set (4 pieces) gives 5.0 blocks by default."
                )
                .defineInRange("dreadAuraRangePerPiece", 1.25D, 0.0D, 64.0D);
        b.pop();

        b.push("nightmareSword");
        NIGHTMARE_SWORD_BASE_DAMAGE     = b
                .comment(
                    "Base attack damage. Also the minimum damage at full health.",
                    "Formula: damage = baseDamage * (1 + missingHealthFraction * scalingFactor)"
                )
                .defineInRange("baseDamage",    30.0D, 0.0D, 1024.0D);
        NIGHTMARE_SWORD_ATTACK_SPEED    = b.comment("Attack speed of the Nightmare Sword.").defineInRange("attackSpeed",  -2.4D, -10.0D, 10.0D);
        NIGHTMARE_SWORD_SCALING_FACTOR  = b
                .comment(
                    "How much damage scales with missing health.",
                    "At 1.0 and 0 HP remaining: damage doubles. At full health: always baseDamage."
                )
                .defineInRange("scalingFactor",  1.0D, 0.0D, 100.0D);
        b.pop();

        b.push("fallenKingCrown");
        FALLEN_KING_CROWN_ARMOR_VALUE = b.comment("Armor value of the Fallen King's Crown. Default: 2 (gold helmet).").defineInRange("armorValue", 2, 0, 100);
        FALLEN_KING_CROWN_ARMOR_TOUGHNESS = b.comment("Armor toughness of the Fallen King's Crown. Default: 0.0 (gold helmet).").defineInRange("armorToughness", 0.0D, 0.0D, 100.0D);
        b.pop();


        // Blood Crystal Armor

        b.push("bloodCrystalArmor");
        BLOOD_CRYSTAL_ARMOR_SHIELD_RECHARGE_TICKS = b.comment("Ticks (20/s) for each armor Bloodglass Shield to recharge after shattering. Default: 600 (30s).").defineInRange("armorShieldRechargeTicks", 600, 1, 72000);
        BLOOD_CRYSTAL_APPLE_SHIELD_COUNT          = b.comment("Number of Bloodglass Shields granted by a Blood Crystal Apple (Bloodglass Ward level). Default: 2.").defineInRange("appleShieldCount", 2, 1, 8);
        BLOOD_CRYSTAL_APPLE_DURATION_TICKS        = b.comment("Duration in ticks of the Bloodglass Ward effect from a Blood Crystal Apple. Default: 2400 (120s).").defineInRange("appleDurationTicks", 2400, 1, 144000);
        BLOOD_CRYSTAL_APPLE_SHIELD_RECHARGE_TICKS = b.comment("Ticks for each apple Bloodglass Shield to recharge while Bloodglass Ward is active. Default: 600 (30s).").defineInRange("appleShieldRechargeTicks", 600, 1, 72000);
        BLOOD_CRYSTAL_HARD_MAX_SHIELDS            = b.comment("Hard cap on total Bloodglass Shields from all sources combined. Default: 8.").defineInRange("hardMaxShields", 8, 1, 20);
        BLOOD_CRYSTAL_HELMET_DEFENSE      = b.comment("Armor defense value for Blood Crystal Helmet. Default: 2 (iron).").defineInRange("helmetDefense", 2, 0, 30);
        BLOOD_CRYSTAL_CHESTPLATE_DEFENSE  = b.comment("Armor defense value for Blood Crystal Chestplate. Default: 6 (iron).").defineInRange("chestplateDefense", 6, 0, 30);
        BLOOD_CRYSTAL_LEGGINGS_DEFENSE    = b.comment("Armor defense value for Blood Crystal Leggings. Default: 5 (iron).").defineInRange("leggingsDefense", 5, 0, 30);
        BLOOD_CRYSTAL_BOOTS_DEFENSE       = b.comment("Armor defense value for Blood Crystal Boots. Default: 2 (iron).").defineInRange("bootsDefense", 2, 0, 30);
        BLOOD_CRYSTAL_ARMOR_TOUGHNESS     = b.comment("Armor toughness for all Blood Crystal armor pieces. Default: 0.0 (iron).").defineInRange("armorToughness", 0.0, 0.0, 20.0);
        BLOOD_CRYSTAL_HELMET_DURABILITY   = b.comment("Max durability of Blood Crystal Helmet. Default: 77 (gold).").defineInRange("helmetDurability", 77, 1, 2000);
        BLOOD_CRYSTAL_CHESTPLATE_DURABILITY = b.comment("Max durability of Blood Crystal Chestplate. Default: 112 (gold).").defineInRange("chestplateDurability", 112, 1, 2000);
        BLOOD_CRYSTAL_LEGGINGS_DURABILITY = b.comment("Max durability of Blood Crystal Leggings. Default: 105 (gold).").defineInRange("leggingsDurability", 105, 1, 2000);
        BLOOD_CRYSTAL_BOOTS_DURABILITY    = b.comment("Max durability of Blood Crystal Boots. Default: 91 (gold).").defineInRange("bootsDurability", 91, 1, 2000);
        b.pop();


        // Squidzooka

        b.push("squidzooka");
        SQUIDZOOKA_COOLDOWN_SECONDS  = b.comment("Cooldown in seconds after firing. Set to 0 to disable.").defineInRange("cooldownSeconds", 1.0D, 0.0D, 3600.0D);
        SQUIDZOOKA_LAUNCH_VELOCITY   = b.comment("Initial launch velocity applied to the Missile Squid.")  .defineInRange("launchVelocity",  2.1D, 0.1D, 20.0D);
        b.pop();


        // Size Rays

        b.push("sizeRays");
        SIZE_CHANGING_RAYS_ENABLED = b
                .comment(
                    "If false, shrink and grow rays cannot be fired by players or dispensers.",
                    "Immune entities tag: data/antarchy/tags/entity_types/size_changing_immune.json"
                )
                .define("enabled", true);
        SIZE_RAY_COOLDOWN_SECONDS  = b.comment("Cooldown in seconds after firing. Set to 0 to disable.")                                       .defineInRange("cooldownSeconds",    1.0D, 0.0D, 3600.0D);
        SIZE_RAY_MIN_SCALE         = b.comment("Minimum entity scale the shrink ray can reach. 1.0 is normal size.")                           .defineInRange("minScale",           0.1D, 0.01D, 64.0D);
        SIZE_RAY_MAX_SCALE         = b.comment("Maximum entity scale the growth ray can reach. 1.0 is normal size.")                           .defineInRange("maxScale",           5.0D, 0.01D, 64.0D);
        SIZE_RAY_DELTA_PER_HIT     = b.comment("Scale delta per projectile hit. 0.25 changes scale by 25% of normal per hit.")                 .defineInRange("deltaPerHit",        0.25D, 0.001D, 64.0D);
        SHRINKING_POTION_DELTA     = b.comment("Scale delta per level of Shrinking Potion. Level II applies double this value.")               .defineInRange("shrinkingPotionDelta", 0.3D, 0.001D, 64.0D);
        GROWTH_POTION_DELTA        = b.comment("Scale delta per level of Growth Potion. Level II applies double this value.")                  .defineInRange("growthPotionDelta",    0.5D, 0.001D, 64.0D);
        b.pop();


        // Gravity

        b.push("gravity");
        INVERT_PROJECTILES_FROM_INVERTED_PLAYERS = b
                .comment(
                    "If true, projectiles fired by inverted players keep their inverted launch and gravity.",
                    "Affects bows, crossbows, throwables, and firework boosts."
                )
                .define("invertProjectilesFromInvertedPlayers", false);
        b.pop();


        // Gravity Gun

        b.push("gravityGun");
        GRAVITY_GUN_ENABLED            = b.comment("Master toggle for the Gravity Gun.")                                          .define("enabled",             true);
        GRAVITY_GUN_BLOCKS_ENABLED     = b.comment("If false, the Gravity Gun cannot grab blocks.")                               .define("blocksEnabled",        true);
        GRAVITY_GUN_ENTITIES_ENABLED   = b.comment("If false, the Gravity Gun cannot grab entities.")                             .define("entitiesEnabled",      true);
        GRAVITY_GUN_RANGE              = b.comment("Search range for a target in blocks.")                                        .defineInRange("range",        16.0D, 1.0D, 64.0D);
        GRAVITY_GUN_THROW_STRENGTH     = b.comment("Velocity multiplier when throwing a held target.")                            .defineInRange("throwStrength",  1.8D, 0.1D, 10.0D);
        GRAVITY_GUN_BLAST_STRENGTH     = b.comment("Velocity multiplier for the push blast.")                                     .defineInRange("blastStrength",  1.4D, 0.1D, 10.0D);
        GRAVITY_GUN_COOLDOWN_SECONDS   = b.comment("Cooldown in seconds after any Gravity Gun action.")                           .defineInRange("cooldownSeconds", 0.35D, 0.0D, 10.0D);
        GRAVITY_GUN_MAX_HOLD_DISTANCE  = b.comment("Maximum distance a held target can be pushed from the player, in blocks.")    .defineInRange("maxHoldDistance", 10.0D, 1.5D, 64.0D);
        b.pop();


        // Miscellaneous items / consumables

        b.push("ductTape");
        DUCT_TAPE_REPAIR_PERCENT_PER_USE = b
                .comment("Fraction of max durability repaired per use. 0.25 = 25% per application.")
                .defineInRange("repairPercentPerUse", 0.25D, 0.0D, 1.0D);
        b.pop();

        b.push("potentNyxite");
        POTENT_NYXITE_INVERTED_DURATION_SECONDS = b
                .comment("How long the Inverted effect from Potent Nyxite eruptions lasts, in seconds.")
                .defineInRange("invertedDurationSeconds", 10.0D, 0.0D, 3600.0D);
        b.pop();

        b.push("corneaEar");
        CORNEA_EAR_NIGHT_VISION_SECONDS = b
                .comment("How long Cornea Ear grants Night Vision after being eaten, in seconds.")
                .defineInRange("nightVisionSeconds", 15, 0, 600);
        b.pop();

        b.push("moggles");
        MOGGLES_VISION_RADIUS    = b.comment("Radius in blocks of the see-through effect (cube side = 2r+1).").defineInRange("visionRadius",   2, 1, 16);
        MOGGLES_VISION_MAX_LIGHT = b.comment("Maximum block light level where Moggles vision activates.")     .defineInRange("visionMaxLight",  7, 0, 15);
        MOGGLES_VISION_ALPHA     = b.comment("Alpha of blocks made visible by Moggles. 0 = invisible, 1 = opaque.").defineInRange("visionAlpha", 0.35D, 0.05D, 1.0D);
        b.pop();

        SPEC = b.build();
    }

    private AntarchyToolsConfig() {}

    static double  basiliskDaggerAttackDamage()              { return BASILISK_DAGGER_ATTACK_DAMAGE.get(); }
    static double  basiliskDaggerAttackSpeed()               { return BASILISK_DAGGER_ATTACK_SPEED.get(); }
    static int     basiliskDaggerPoisonDurationTicks()       { return BASILISK_DAGGER_POISON_DURATION_TICKS.get(); }
    static int     basiliskDaggerPoisonAmplifier()           { return BASILISK_DAGGER_POISON_AMPLIFIER.get(); }

    static double  ultimateSwordAttackDamage()               { return ULTIMATE_SWORD_ATTACK_DAMAGE.get(); }
    static double  ultimateSwordAttackSpeed()                { return ULTIMATE_SWORD_ATTACK_SPEED.get(); }
    static double  ultimatePickaxeAttackDamage()             { return ULTIMATE_PICKAXE_ATTACK_DAMAGE.get(); }
    static double  ultimatePickaxeAttackSpeed()              { return ULTIMATE_PICKAXE_ATTACK_SPEED.get(); }
    static double  ultimateAxeAttackDamage()                 { return ULTIMATE_AXE_ATTACK_DAMAGE.get(); }
    static double  ultimateAxeAttackSpeed()                  { return ULTIMATE_AXE_ATTACK_SPEED.get(); }
    static double  ultimateShovelAttackDamage()              { return ULTIMATE_SHOVEL_ATTACK_DAMAGE.get(); }
    static double  ultimateShovelAttackSpeed()               { return ULTIMATE_SHOVEL_ATTACK_SPEED.get(); }
    static double  ultimateHoeAttackDamage()                 { return ULTIMATE_HOE_ATTACK_DAMAGE.get(); }
    static double  ultimateHoeAttackSpeed()                  { return ULTIMATE_HOE_ATTACK_SPEED.get(); }
    static int     ultimateToolEnchantability()              { return ULTIMATE_TOOL_ENCHANTABILITY.get(); }
    static boolean ultimateToolsThreeByThreeEnabled()        { return ULTIMATE_TOOLS_THREE_BY_THREE_ENABLED.get(); }

    static double  ultimateBowAttackDamage()                 { return ULTIMATE_BOW_ATTACK_DAMAGE.get(); }
    static double  ultimateBowPlayerHeal()                   { return ULTIMATE_BOW_PLAYER_HEAL.get(); }
    static double  ultimateBowDrawSpeedMultiplier()          { return ULTIMATE_BOW_DRAW_SPEED_MULTIPLIER.get(); }
    static boolean ultimateBowComesEnchantedWithFlame()      { return ULTIMATE_BOW_COMES_ENCHANTED_WITH_FLAME.get(); }
    static int     ultimateBowEnchantability()               { return ULTIMATE_BOW_ENCHANTABILITY.get(); }

    static double  ultimateCrossbowAttackDamage()            { return ULTIMATE_CROSSBOW_ATTACK_DAMAGE.get(); }
    static double  ultimateCrossbowChargeSpeedMultiplier()   { return ULTIMATE_CROSSBOW_CHARGE_SPEED_MULTIPLIER.get(); }
    static int     ultimateCrossbowEnchantability()          { return ULTIMATE_CROSSBOW_ENCHANTABILITY.get(); }

    static double  ultimateMaceDamageMultiplier()            { return ULTIMATE_MACE_DAMAGE_MULTIPLIER.get(); }
    static double  ultimateMaceAttackSpeed()                 { return ULTIMATE_MACE_ATTACK_SPEED.get(); }
    static int     ultimateMaceEnchantability()              { return ULTIMATE_MACE_ENCHANTABILITY.get(); }

    static boolean ultimateArmorComesEnchanted()             { return ULTIMATE_ARMOR_COMES_ENCHANTED.get(); }
    static int     ultimateArmorEnchantability()             { return ULTIMATE_ARMOR_ENCHANTABILITY.get(); }
    static int     ultimateHelmetArmorValue()                { return ULTIMATE_HELMET_ARMOR_VALUE.get(); }
    static int     ultimateChestplateArmorValue()            { return ULTIMATE_CHESTPLATE_ARMOR_VALUE.get(); }
    static int     ultimateLeggingsArmorValue()              { return ULTIMATE_LEGGINGS_ARMOR_VALUE.get(); }
    static int     ultimateBootsArmorValue()                 { return ULTIMATE_BOOTS_ARMOR_VALUE.get(); }
    static double  ultimateHelmetArmorToughness()            { return ULTIMATE_HELMET_ARMOR_TOUGHNESS.get(); }
    static double  ultimateChestplateArmorToughness()        { return ULTIMATE_CHESTPLATE_ARMOR_TOUGHNESS.get(); }
    static double  ultimateLeggingsArmorToughness()          { return ULTIMATE_LEGGINGS_ARMOR_TOUGHNESS.get(); }
    static double  ultimateBootsArmorToughness()             { return ULTIMATE_BOOTS_ARMOR_TOUGHNESS.get(); }
    static double  ultimateArmorKnockbackResistance()        { return ULTIMATE_ARMOR_KNOCKBACK_RESISTANCE.get(); }

    static double  battleAxeAttackDamage()                   { return BATTLE_AXE_ATTACK_DAMAGE.get(); }
    static double  battleAxeAttackSpeed()                    { return BATTLE_AXE_ATTACK_SPEED.get(); }

    static double  bigBerthaAttackDamage()                   { return BIG_BERTHA_ATTACK_DAMAGE.get(); }
    static double  bigBerthaReachBonus()                     { return BIG_BERTHA_REACH_BONUS.get(); }
    static double  bigBerthaAttackSpeed()                    { return BIG_BERTHA_ATTACK_SPEED.get(); }
    static int     bigBerthaBasiliskParalyzeDurationTicks()  { return BIG_BERTHA_BASILISK_PARALYZE_DURATION_TICKS.get(); }
    static int     bigBerthaKrakenSlowTicks()                { return BIG_BERTHA_KRAKEN_SLOW_TICKS.get(); }
    static double  bigBerthaBasiliskCooldownSeconds()        { return BIG_BERTHA_BASILISK_COOLDOWN_SECONDS.get(); }
    static double  bigBerthaLucidInvertedDurationSeconds()   { return BIG_BERTHA_LUCID_INVERTED_DURATION_SECONDS.get(); }
    static double  bigBerthaLucidInvertedDamageBonusPercent(){ return BIG_BERTHA_LUCID_INVERTED_DAMAGE_BONUS_PERCENT.get(); }
    static double  scorpionWhipBaseDamage()                  { return SCORPION_WHIP_BASE_DAMAGE.get(); }
    static double  scorpionWhipReachBonus()                  { return SCORPION_WHIP_REACH_BONUS.get(); }
    static int     scorpionWhipPoisonDurationTicks()         { return SCORPION_WHIP_POISON_DURATION_TICKS.get(); }
    static double  scorpionWhipTetherMaxRange()              { return SCORPION_WHIP_TETHER_MAX_RANGE.get(); }
    static int     scorpionWhipReelCooldownTicks()           { return SCORPION_WHIP_REEL_COOLDOWN_TICKS.get(); }
    static double  scorpionWhipSnapBonusDamage()             { return SCORPION_WHIP_SNAP_BONUS_DAMAGE.get(); }
    static int     scorpionWhipSnapCooldownTicks()           { return SCORPION_WHIP_SNAP_COOLDOWN_TICKS.get(); }
    static double  scorpionWhipPullStrength()                { return SCORPION_WHIP_PULL_STRENGTH.get(); }
    static double  scorpionWhipHeavyPullMultiplier()         { return SCORPION_WHIP_HEAVY_PULL_MULTIPLIER.get(); }
    static double  scorpionWhipSelfPullMultiplier()          { return SCORPION_WHIP_SELF_PULL_MULTIPLIER.get(); }
    static int     bloodCrystalKatanaAttackDamage()          { return BLOOD_CRYSTAL_KATANA_ATTACK_DAMAGE.get(); }
    static double  bloodCrystalKatanaLaunchStrength()        { return BLOOD_CRYSTAL_KATANA_LAUNCH_STRENGTH.get(); }
    static int     bloodCrystalKatanaTrailDurationTicks()    { return BLOOD_CRYSTAL_KATANA_TRAIL_DURATION_TICKS.get(); }

    static int     nightmareHelmetArmorValue()               { return NIGHTMARE_HELMET_ARMOR_VALUE.get(); }
    static int     nightmareChestplateArmorValue()           { return NIGHTMARE_CHESTPLATE_ARMOR_VALUE.get(); }
    static int     nightmareLeggingsArmorValue()             { return NIGHTMARE_LEGGINGS_ARMOR_VALUE.get(); }
    static int     nightmareBootsArmorValue()                { return NIGHTMARE_BOOTS_ARMOR_VALUE.get(); }
    static double  nightmareHelmetArmorToughness()           { return NIGHTMARE_HELMET_ARMOR_TOUGHNESS.get(); }
    static double  nightmareChestplateArmorToughness()       { return NIGHTMARE_CHESTPLATE_ARMOR_TOUGHNESS.get(); }
    static double  nightmareLeggingsArmorToughness()         { return NIGHTMARE_LEGGINGS_ARMOR_TOUGHNESS.get(); }
    static double  nightmareBootsArmorToughness()            { return NIGHTMARE_BOOTS_ARMOR_TOUGHNESS.get(); }
    static double  nightmareArmorKnockbackResistance()       { return NIGHTMARE_ARMOR_KNOCKBACK_RESISTANCE.get(); }
    static double  nightmareArmorDreadAuraRangePerPiece()    { return NIGHTMARE_ARMOR_DREAD_AURA_RANGE_PER_PIECE.get(); }
    static double  nightmareSwordBaseDamage()                { return NIGHTMARE_SWORD_BASE_DAMAGE.get(); }
    static double  nightmareSwordAttackSpeed()               { return NIGHTMARE_SWORD_ATTACK_SPEED.get(); }
    static double  nightmareSwordScalingFactor()             { return NIGHTMARE_SWORD_SCALING_FACTOR.get(); }

    static int     fallenKingCrownArmorValue()               { return FALLEN_KING_CROWN_ARMOR_VALUE.get(); }
    static double  fallenKingCrownArmorToughness()           { return FALLEN_KING_CROWN_ARMOR_TOUGHNESS.get(); }

    static int     bloodCrystalArmorShieldRechargeTicks() { return BLOOD_CRYSTAL_ARMOR_SHIELD_RECHARGE_TICKS.get(); }
    static int     bloodCrystalAppleShieldCount()         { return BLOOD_CRYSTAL_APPLE_SHIELD_COUNT.get(); }
    static int     bloodCrystalAppleDurationTicks()       { return BLOOD_CRYSTAL_APPLE_DURATION_TICKS.get(); }
    static int     bloodCrystalAppleShieldRechargeTicks() { return BLOOD_CRYSTAL_APPLE_SHIELD_RECHARGE_TICKS.get(); }
    static int     bloodCrystalHardMaxShields()           { return BLOOD_CRYSTAL_HARD_MAX_SHIELDS.get(); }
    static int     bloodCrystalHelmetDefense()            { return BLOOD_CRYSTAL_HELMET_DEFENSE.get(); }
    static int     bloodCrystalChestplateDefense()        { return BLOOD_CRYSTAL_CHESTPLATE_DEFENSE.get(); }
    static int     bloodCrystalLeggingsDefense()          { return BLOOD_CRYSTAL_LEGGINGS_DEFENSE.get(); }
    static int     bloodCrystalBootsDefense()             { return BLOOD_CRYSTAL_BOOTS_DEFENSE.get(); }
    static double  bloodCrystalArmorToughness()           { return BLOOD_CRYSTAL_ARMOR_TOUGHNESS.get(); }
    static int     bloodCrystalHelmetDurability()         { return BLOOD_CRYSTAL_HELMET_DURABILITY.get(); }
    static int     bloodCrystalChestplateDurability()     { return BLOOD_CRYSTAL_CHESTPLATE_DURABILITY.get(); }
    static int     bloodCrystalLeggingsDurability()       { return BLOOD_CRYSTAL_LEGGINGS_DURABILITY.get(); }
    static int     bloodCrystalBootsDurability()          { return BLOOD_CRYSTAL_BOOTS_DURABILITY.get(); }

    static double  squidzookaCooldownSeconds()               { return SQUIDZOOKA_COOLDOWN_SECONDS.get(); }
    static double  squidzookaLaunchVelocity()                { return SQUIDZOOKA_LAUNCH_VELOCITY.get(); }

    static boolean sizeChangingRaysEnabled()                 { return SIZE_CHANGING_RAYS_ENABLED.get(); }
    static double  sizeRayCooldownSeconds()                  { return SIZE_RAY_COOLDOWN_SECONDS.get(); }
    static double  sizeRayMinScale()                         { return SIZE_RAY_MIN_SCALE.get(); }
    static double  sizeRayMaxScale()                         { return SIZE_RAY_MAX_SCALE.get(); }
    static double  sizeRayDeltaPerHit()                      { return SIZE_RAY_DELTA_PER_HIT.get(); }
    static double  shrinkingPotionDelta()                    { return SHRINKING_POTION_DELTA.get(); }
    static double  growthPotionDelta()                       { return GROWTH_POTION_DELTA.get(); }

    static boolean invertProjectilesFromInvertedPlayers()    { return INVERT_PROJECTILES_FROM_INVERTED_PLAYERS.get(); }

    static boolean gravityGunEnabled()                       { return GRAVITY_GUN_ENABLED.get(); }
    static boolean gravityGunBlocksEnabled()                 { return GRAVITY_GUN_BLOCKS_ENABLED.get(); }
    static boolean gravityGunEntitiesEnabled()               { return GRAVITY_GUN_ENTITIES_ENABLED.get(); }
    static double  gravityGunRange()                         { return GRAVITY_GUN_RANGE.get(); }
    static double  gravityGunThrowStrength()                 { return GRAVITY_GUN_THROW_STRENGTH.get(); }
    static double  gravityGunBlastStrength()                 { return GRAVITY_GUN_BLAST_STRENGTH.get(); }
    static double  gravityGunCooldownSeconds()               { return GRAVITY_GUN_COOLDOWN_SECONDS.get(); }
    static double  gravityGunMaxHoldDistance()               { return GRAVITY_GUN_MAX_HOLD_DISTANCE.get(); }

    static double  ductTapeRepairPercentPerUse()             { return DUCT_TAPE_REPAIR_PERCENT_PER_USE.get(); }
    static double  potentNyxiteInvertedDurationSeconds()     { return POTENT_NYXITE_INVERTED_DURATION_SECONDS.get(); }
    static int     corneaEarNightVisionSeconds()             { return CORNEA_EAR_NIGHT_VISION_SECONDS.get(); }

    static int     mogglesVisionRadius()                     { return MOGGLES_VISION_RADIUS.get(); }
    static int     mogglesVisionMaxLight()                   { return MOGGLES_VISION_MAX_LIGHT.get(); }
    static float   mogglesVisionAlpha()                      { return MOGGLES_VISION_ALPHA.get().floatValue(); }

}
