package com.craisinlord.antarchy.content;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;

@SuppressWarnings("unchecked")
public final class AntarchySoundEvents {
    private static final Supplier<?> UNBOUND = () -> {
        throw new IllegalStateException("Antarchy sound event supplier was accessed before registration finished");
    };

    public static Supplier<SoundEvent> SQUIDZOOKA_FIRE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SHRINK_RAY_CHARGE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SHRINK_RAY_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> GROWTH_RAY_CHARGE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> GROWTH_RAY_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SIZE_RAY_WEAKSHOT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SIZE_RAY_MEDIUMSHOT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SIZE_RAY_CRITSHOT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ANT_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ANT_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ANT_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ANT_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ANT_GATHER = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ANT_NEST = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CLOUD_SHARK_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CLOUD_SHARK_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CLOUD_SHARK_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CLOUD_SHARK_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CLOUD_SHARK_FLY = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CATERPILLAR_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CATERPILLAR_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CATERPILLAR_CRAWL = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BUTTERFLY_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BRUTALFLY_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BRUTALFLY_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ELYTHIA_FIREFLY_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ELYTHIA_OURANWOOD_FOREST_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MISSILE_SQUID_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MISSILE_SQUID_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MISSILE_SQUID_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MISSILE_SQUID_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KRAKEN_FLYING_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KRAKEN_FLYING_SIDEWAYS_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KRAKEN_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KRAKEN_SPIN = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KRAKEN_ROAR = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KRAKEN_SUMMON = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KRAKEN_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KRAKEN_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BASILISK_IDLE_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BASILISK_SLITHER_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BASILISK_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BASILISK_HISS = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BASILISK_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BASILISK_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> THORAXIS_NIGHTMARE_WASTES_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> THORAXIS_NIGHTMARE_WASTES_ADDITIONS = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> THORAXIS_NIGHTMARE_WASTES_MOOD = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> THORAXIS_DREAM_DUNES_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> THORAXIS_DREAM_DUNES_ADDITIONS = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> THORAXIS_DREAM_DUNES_MOOD = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> THORAXIS_LUCID_POOLS_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> THORAXIS_LUCID_POOLS_ADDITIONS = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> THORAXIS_LUCID_POOLS_MOOD = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LUCID_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LUCID_FLYING = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LUCID_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LUCID_BOLT_SOUND = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LUCID_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LUCID_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> REVERIE_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> REVERIE_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> REVERIE_WORRY = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> REVERIE_SAVE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> REVERIE_JOIN_PLAYER = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> REVERIE_ALERT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> FLYING_SQUIRREL_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> FLYING_SQUIRREL_BEG = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> FLYING_SQUIRREL_NUT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> FLYING_SQUIRREL_GLIDE_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> FLYING_SQUIRREL_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> FLYING_SQUIRREL_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> NIGHTMARE_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> NIGHTMARE_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> NIGHTMARE_ROAR = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> NIGHTMARE_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> NIGHTMARE_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> NIGHTMARE_FLAP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TRIFFID_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TRIFFID_GRAB = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TRIFFID_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TRIFFID_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TRIFFID_HISS = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TRIFFID_GROWL = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MANTIS_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MANTIS_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MANTIS_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MANTIS_FLY_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> GRAVITY_GUN_PICKUP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> GRAVITY_GUN_DROP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> GRAVITY_GUN_HOLD_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> GRAVITY_GUN_LAUNCH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> GRAVITY_GUN_DRYFIRE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BED_BUG_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BED_BUG_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BED_BUG_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> WASP_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> WASP_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> WASP_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> WASP_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SCORPION_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SCORPION_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SCORPION_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> EMPEROR_SCORPION_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> EMPEROR_SCORPION_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> EMPEROR_SCORPION_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> EMPEROR_SCORPION_ROAR = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MOLEWORM_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MOLEWORM_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MOLEWORM_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MOLEWORM_DIG = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MOLEVORE_AMBIENT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MOLEVORE_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MOLEVORE_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> MOLEVORE_DIG = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BOMBER_WALK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BOMBER_KNOCK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BOMBER_EXPLODE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TORETERROR_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TORETERROR_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TORETERROR_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TORETERROR_BOMBER_FIRE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TORETERROR_SPIN = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TORETERROR_RICOCHET = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> WATER_CANNON_FIRE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TORETERROR_JUMP_UP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> TORETERROR_JUMP_LAND = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> STINKY_FLY = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> STINK_BUG_FART = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> STINK_BUG_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CAVARYN_HEARTBEAT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ROLLY_POLLY_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CREEPING_HORROR_GROWL = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CREEPING_HORROR_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> CREEPING_HORROR_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LURKING_TERROR_SNARL = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LURKING_TERROR_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LURKING_TERROR_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> LURKING_TERROR_FLY_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> DUCT_TAPE_USE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> HERCULES_BEETLE_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> HERCULES_BEETLE_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> HERCULES_BEETLE_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> HERCULES_BEETLE_CRY = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> HERCULES_BEETLE_CHARGE_START = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> HERCULES_BEETLE_KNOCKED_DOWN = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JUMPY_BUG_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SPIT_BUG_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JUMPY_BUG_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JUMPY_BUG_JUMP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SPIT_BUG_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> SPIT_BUG_SPIT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BRUTALFLY_SPIT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ROLLY_POLLY_WHEEL_MODE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ROLLY_POLLY_NORMAL_MODE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> ROLLY_POLLY_ROLL = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JERRY_YOUNG_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JERRY_YOUNG_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JERRY_YOUNG_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JERRY_YOUNG_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JERRY_ADULT_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JERRY_ADULT_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JERRY_ADULT_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> JERRY_ADULT_ATTACK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> WORM_HOOK_FLYING = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BLOOD_CRYSTAL_KATANA_DASH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BLOOD_CRYSTAL_ARMOR_EQUIP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BLOODGLASS_WARD_HEART_BREAK = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> BLOODGLASS_WARD_HEART_REGEN = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> QUEEN_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> QUEEN_HURT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> QUEEN_DEATH = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> QUEEN_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> QUEEN_ROAR = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_BEAM_SHOOT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_FIREBALL_SHOOT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_DECREE_CAST = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> QUEEN_BEAM_SHOOT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> QUEEN_BEAM_START = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> QUEEN_BEAM_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> QUEEN_BEAM_END = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_STOMP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_ROAR = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_WING_FLAP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> KING_FLY_LOOP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCE_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCE_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCE_SHOOT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCE_FLY = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCE_STEP = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCESS_IDLE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCESS_BITE = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCESS_SHOOT = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCESS_FLY = (Supplier<SoundEvent>) UNBOUND;
    public static Supplier<SoundEvent> PRINCESS_STEP = (Supplier<SoundEvent>) UNBOUND;

    private AntarchySoundEvents() {
    }

    public static void bind(
            Supplier<SoundEvent> squidzookaFire,
            Supplier<SoundEvent> shrinkRayCharge,
            Supplier<SoundEvent> shrinkRayLoop,
            Supplier<SoundEvent> growthRayCharge,
            Supplier<SoundEvent> growthRayLoop,
            Supplier<SoundEvent> sizeRayWeakshot,
            Supplier<SoundEvent> sizeRayMediumshot,
            Supplier<SoundEvent> sizeRayCritshot,
            Supplier<SoundEvent> antAmbient,
            Supplier<SoundEvent> antIdle,
            Supplier<SoundEvent> antHurt,
            Supplier<SoundEvent> antBite,
            Supplier<SoundEvent> antGather,
            Supplier<SoundEvent> antNest,
            Supplier<SoundEvent> cloudSharkBite,
            Supplier<SoundEvent> cloudSharkIdle,
            Supplier<SoundEvent> cloudSharkHurt,
            Supplier<SoundEvent> cloudSharkDeath,
            Supplier<SoundEvent> cloudSharkFly,
            Supplier<SoundEvent> caterpillarIdle,
            Supplier<SoundEvent> caterpillarHurt,
            Supplier<SoundEvent> caterpillarCrawl,
            Supplier<SoundEvent> butterflyHurt,
            Supplier<SoundEvent> brutalflyIdle,
            Supplier<SoundEvent> brutalflyDeath,
            Supplier<SoundEvent> elythiaFireflyAmbient,
            Supplier<SoundEvent> elythiaOuranwoodForestAmbient,
            Supplier<SoundEvent> missileSquidAmbient,
            Supplier<SoundEvent> missileSquidHurt,
            Supplier<SoundEvent> missileSquidDeath,
            Supplier<SoundEvent> missileSquidAttack,
            Supplier<SoundEvent> krakenFlyingLoop,
            Supplier<SoundEvent> krakenFlyingSidewaysLoop,
            Supplier<SoundEvent> krakenAttack,
            Supplier<SoundEvent> krakenSpin,
            Supplier<SoundEvent> krakenRoar,
            Supplier<SoundEvent> krakenSummon,
            Supplier<SoundEvent> krakenHurt,
            Supplier<SoundEvent> krakenDeath,
            Supplier<SoundEvent> basiliskIdleLoop,
            Supplier<SoundEvent> basiliskSlitherLoop,
            Supplier<SoundEvent> basiliskBite,
            Supplier<SoundEvent> basiliskHiss,
            Supplier<SoundEvent> basiliskHurt,
            Supplier<SoundEvent> basiliskDeath,
            Supplier<SoundEvent> thoraxisNightmareWastesAmbient,
            Supplier<SoundEvent> thoraxisNightmareWastesAdditions,
            Supplier<SoundEvent> thoraxisNightmareWastesMood,
            Supplier<SoundEvent> thoraxisDreamDunesAmbient,
            Supplier<SoundEvent> thoraxisDreamDunesAdditions,
            Supplier<SoundEvent> thoraxisDreamDunesMood,
            Supplier<SoundEvent> thoraxisLucidPoolsAmbient,
            Supplier<SoundEvent> thoraxisLucidPoolsAdditions,
            Supplier<SoundEvent> thoraxisLucidPoolsMood,
            Supplier<SoundEvent> lucidAmbient,
            Supplier<SoundEvent> lucidFlying,
            Supplier<SoundEvent> lucidAttack,
            Supplier<SoundEvent> lucidBoltSound,
            Supplier<SoundEvent> lucidHurt,
            Supplier<SoundEvent> lucidDeath,
            Supplier<SoundEvent> reverieIdle,
            Supplier<SoundEvent> reverieHurt,
            Supplier<SoundEvent> reverieWorry,
            Supplier<SoundEvent> reverieSave,
            Supplier<SoundEvent> reverieJoinPlayer,
            Supplier<SoundEvent> reverieAlert,
            Supplier<SoundEvent> flyingSquirrelIdle,
            Supplier<SoundEvent> flyingSquirrelBeg,
            Supplier<SoundEvent> flyingSquirrelNut,
            Supplier<SoundEvent> flyingSquirrelGlideLoop,
            Supplier<SoundEvent> flyingSquirrelHurt,
            Supplier<SoundEvent> flyingSquirrelDeath,
            Supplier<SoundEvent> nightmareIdle,
            Supplier<SoundEvent> nightmareHurt,
            Supplier<SoundEvent> nightmareRoar,
            Supplier<SoundEvent> nightmareDeath,
            Supplier<SoundEvent> nightmareBite,
            Supplier<SoundEvent> nightmareFlap,
            Supplier<SoundEvent> triffidAttack,
            Supplier<SoundEvent> triffidGrab,
            Supplier<SoundEvent> triffidHurt,
            Supplier<SoundEvent> triffidDeath,
            Supplier<SoundEvent> triffidHiss,
            Supplier<SoundEvent> triffidGrowl,
            Supplier<SoundEvent> mantisAmbient,
            Supplier<SoundEvent> mantisHurt,
            Supplier<SoundEvent> mantisAttack,
            Supplier<SoundEvent> mantisFlyLoop,
            Supplier<SoundEvent> gravityGunPickup,
            Supplier<SoundEvent> gravityGunDrop,
            Supplier<SoundEvent> gravityGunHoldLoop,
            Supplier<SoundEvent> gravityGunLaunch,
            Supplier<SoundEvent> gravityGunDryfire,
            Supplier<SoundEvent> bedBugAmbient,
            Supplier<SoundEvent> bedBugHurt,
            Supplier<SoundEvent> bedBugAttack,
            Supplier<SoundEvent> waspIdle,
            Supplier<SoundEvent> waspHurt,
            Supplier<SoundEvent> waspAttack,
            Supplier<SoundEvent> waspDeath,
            Supplier<SoundEvent> scorpionAmbient,
            Supplier<SoundEvent> scorpionHurt,
            Supplier<SoundEvent> scorpionAttack,
            Supplier<SoundEvent> emperorScorpionAmbient,
            Supplier<SoundEvent> emperorScorpionHurt,
            Supplier<SoundEvent> emperorScorpionAttack,
            Supplier<SoundEvent> emperorScorpionRoar,
            Supplier<SoundEvent> molewormAmbient,
            Supplier<SoundEvent> molewormHurt,
            Supplier<SoundEvent> molewormAttack,
            Supplier<SoundEvent> molewormDig,
            Supplier<SoundEvent> molevoreAmbient,
            Supplier<SoundEvent> molevoreHurt,
            Supplier<SoundEvent> molevoreAttack,
            Supplier<SoundEvent> molevoreDig,
            Supplier<SoundEvent> bomberWalk,
            Supplier<SoundEvent> bomberKnock,
            Supplier<SoundEvent> bomberExplode,
            Supplier<SoundEvent> toreterrorIdle,
            Supplier<SoundEvent> toreterrorHurt,
            Supplier<SoundEvent> toreterrorDeath,
            Supplier<SoundEvent> toreterrorBomberFire,
            Supplier<SoundEvent> toreterrorSpin,
            Supplier<SoundEvent> toreterrorRicochet,
            Supplier<SoundEvent> waterCannonFire,
            Supplier<SoundEvent> toreterrorJumpUp,
            Supplier<SoundEvent> toreterrorJumpLand,
            Supplier<SoundEvent> stinkyFly,
            Supplier<SoundEvent> stinkBugFart,
            Supplier<SoundEvent> stinkBugIdle,
            Supplier<SoundEvent> cavarynHeartbeat,
            Supplier<SoundEvent> rollyPollyIdle,
            Supplier<SoundEvent> creepingHorrorGrowl,
            Supplier<SoundEvent> creepingHorrorHurt,
            Supplier<SoundEvent> creepingHorrorBite,
            Supplier<SoundEvent> lurkingTerrorSnarl,
            Supplier<SoundEvent> lurkingTerrorHurt,
            Supplier<SoundEvent> lurkingTerrorBite,
            Supplier<SoundEvent> lurkingTerrorFlyLoop,
            Supplier<SoundEvent> ductTapeUse,
            Supplier<SoundEvent> herculesBeetleIdle,
            Supplier<SoundEvent> herculesBeetleHurt,
            Supplier<SoundEvent> herculesBeetleAttack,
            Supplier<SoundEvent> herculesBeetleCry,
            Supplier<SoundEvent> herculesBeetleChargeStart,
            Supplier<SoundEvent> herculesBeetleKnockedDown,
            Supplier<SoundEvent> jumpyBugIdle,
            Supplier<SoundEvent> spitBugIdle,
            Supplier<SoundEvent> jumpyBugHurt,
            Supplier<SoundEvent> jumpyBugJump,
            Supplier<SoundEvent> spitBugHurt,
            Supplier<SoundEvent> spitBugSpit,
            Supplier<SoundEvent> brutalflySpit,
            Supplier<SoundEvent> rollyPollyWheelMode,
            Supplier<SoundEvent> rollyPollyNormalMode,
            Supplier<SoundEvent> rollyPollyRoll,
            Supplier<SoundEvent> jerryYoungIdle,
            Supplier<SoundEvent> jerryYoungHurt,
            Supplier<SoundEvent> jerryYoungDeath,
            Supplier<SoundEvent> jerryYoungAttack,
            Supplier<SoundEvent> jerryAdultIdle,
            Supplier<SoundEvent> jerryAdultHurt,
            Supplier<SoundEvent> jerryAdultDeath,
            Supplier<SoundEvent> jerryAdultAttack,
            Supplier<SoundEvent> wormHookFlying,
            Supplier<SoundEvent> bloodCrystalKatanaDash,
            Supplier<SoundEvent> bloodCrystalArmorEquip,
            Supplier<SoundEvent> bloodglassWardHeartBreak,
            Supplier<SoundEvent> bloodglassWardHeartRegen,
            Supplier<SoundEvent> kingIdle,
            Supplier<SoundEvent> kingHurt,
            Supplier<SoundEvent> kingDeath,
            Supplier<SoundEvent> kingBite,
            Supplier<SoundEvent> queenIdle,
            Supplier<SoundEvent> queenHurt,
            Supplier<SoundEvent> queenDeath,
            Supplier<SoundEvent> queenBite,
            Supplier<SoundEvent> queenRoar,
            Supplier<SoundEvent> kingBeamShoot,
            Supplier<SoundEvent> kingFireballShoot,
            Supplier<SoundEvent> kingDecreeCast,
            Supplier<SoundEvent> queenBeamShoot,
            Supplier<SoundEvent> queenBeamStart,
            Supplier<SoundEvent> queenBeamLoop,
            Supplier<SoundEvent> queenBeamEnd,
            Supplier<SoundEvent> kingStomp,
            Supplier<SoundEvent> kingRoar,
            Supplier<SoundEvent> kingWingFlap,
            Supplier<SoundEvent> kingFlyLoop,
            Supplier<SoundEvent> princeIdle,
            Supplier<SoundEvent> princeBite,
            Supplier<SoundEvent> princeShoot,
            Supplier<SoundEvent> princeFly,
            Supplier<SoundEvent> princeStep,
            Supplier<SoundEvent> princessIdle,
            Supplier<SoundEvent> princessBite,
            Supplier<SoundEvent> princessShoot,
            Supplier<SoundEvent> princessFly,
            Supplier<SoundEvent> princessStep
    ) {
        SQUIDZOOKA_FIRE = squidzookaFire;
        SHRINK_RAY_CHARGE = shrinkRayCharge;
        SHRINK_RAY_LOOP = shrinkRayLoop;
        GROWTH_RAY_CHARGE = growthRayCharge;
        GROWTH_RAY_LOOP = growthRayLoop;
        SIZE_RAY_WEAKSHOT = sizeRayWeakshot;
        SIZE_RAY_MEDIUMSHOT = sizeRayMediumshot;
        SIZE_RAY_CRITSHOT = sizeRayCritshot;
        ANT_AMBIENT = antAmbient;
        ANT_IDLE = antIdle;
        ANT_HURT = antHurt;
        ANT_BITE = antBite;
        ANT_GATHER = antGather;
        ANT_NEST = antNest;
        CLOUD_SHARK_BITE = cloudSharkBite;
        CLOUD_SHARK_IDLE = cloudSharkIdle;
        CLOUD_SHARK_HURT = cloudSharkHurt;
        CLOUD_SHARK_DEATH = cloudSharkDeath;
        CLOUD_SHARK_FLY = cloudSharkFly;
        CATERPILLAR_IDLE = caterpillarIdle;
        CATERPILLAR_HURT = caterpillarHurt;
        CATERPILLAR_CRAWL = caterpillarCrawl;
        BUTTERFLY_HURT = butterflyHurt;
        BRUTALFLY_IDLE = brutalflyIdle;
        BRUTALFLY_DEATH = brutalflyDeath;
        ELYTHIA_FIREFLY_AMBIENT = elythiaFireflyAmbient;
        ELYTHIA_OURANWOOD_FOREST_AMBIENT = elythiaOuranwoodForestAmbient;
        MISSILE_SQUID_AMBIENT = missileSquidAmbient;
        MISSILE_SQUID_HURT = missileSquidHurt;
        MISSILE_SQUID_DEATH = missileSquidDeath;
        MISSILE_SQUID_ATTACK = missileSquidAttack;
        KRAKEN_FLYING_LOOP = krakenFlyingLoop;
        KRAKEN_FLYING_SIDEWAYS_LOOP = krakenFlyingSidewaysLoop;
        KRAKEN_ATTACK = krakenAttack;
        KRAKEN_SPIN = krakenSpin;
        KRAKEN_ROAR = krakenRoar;
        KRAKEN_SUMMON = krakenSummon;
        KRAKEN_HURT = krakenHurt;
        KRAKEN_DEATH = krakenDeath;
        BASILISK_IDLE_LOOP = basiliskIdleLoop;
        BASILISK_SLITHER_LOOP = basiliskSlitherLoop;
        BASILISK_BITE = basiliskBite;
        BASILISK_HISS = basiliskHiss;
        BASILISK_HURT = basiliskHurt;
        BASILISK_DEATH = basiliskDeath;
        THORAXIS_NIGHTMARE_WASTES_AMBIENT = thoraxisNightmareWastesAmbient;
        THORAXIS_NIGHTMARE_WASTES_ADDITIONS = thoraxisNightmareWastesAdditions;
        THORAXIS_NIGHTMARE_WASTES_MOOD = thoraxisNightmareWastesMood;
        THORAXIS_DREAM_DUNES_AMBIENT = thoraxisDreamDunesAmbient;
        THORAXIS_DREAM_DUNES_ADDITIONS = thoraxisDreamDunesAdditions;
        THORAXIS_DREAM_DUNES_MOOD = thoraxisDreamDunesMood;
        THORAXIS_LUCID_POOLS_AMBIENT = thoraxisLucidPoolsAmbient;
        THORAXIS_LUCID_POOLS_ADDITIONS = thoraxisLucidPoolsAdditions;
        THORAXIS_LUCID_POOLS_MOOD = thoraxisLucidPoolsMood;
        LUCID_AMBIENT = lucidAmbient;
        LUCID_FLYING = lucidFlying;
        LUCID_ATTACK = lucidAttack;
        LUCID_BOLT_SOUND = lucidBoltSound;
        LUCID_HURT = lucidHurt;
        LUCID_DEATH = lucidDeath;
        REVERIE_IDLE = reverieIdle;
        REVERIE_HURT = reverieHurt;
        REVERIE_WORRY = reverieWorry;
        REVERIE_SAVE = reverieSave;
        REVERIE_JOIN_PLAYER = reverieJoinPlayer;
        REVERIE_ALERT = reverieAlert;
        FLYING_SQUIRREL_IDLE = flyingSquirrelIdle;
        FLYING_SQUIRREL_BEG = flyingSquirrelBeg;
        FLYING_SQUIRREL_NUT = flyingSquirrelNut;
        FLYING_SQUIRREL_GLIDE_LOOP = flyingSquirrelGlideLoop;
        FLYING_SQUIRREL_HURT = flyingSquirrelHurt;
        FLYING_SQUIRREL_DEATH = flyingSquirrelDeath;
        NIGHTMARE_IDLE = nightmareIdle;
        NIGHTMARE_HURT = nightmareHurt;
        NIGHTMARE_ROAR = nightmareRoar;
        NIGHTMARE_DEATH = nightmareDeath;
        NIGHTMARE_BITE = nightmareBite;
        NIGHTMARE_FLAP = nightmareFlap;
        TRIFFID_ATTACK = triffidAttack;
        TRIFFID_GRAB = triffidGrab;
        TRIFFID_HURT = triffidHurt;
        TRIFFID_DEATH = triffidDeath;
        TRIFFID_HISS = triffidHiss;
        TRIFFID_GROWL = triffidGrowl;
        MANTIS_AMBIENT = mantisAmbient;
        MANTIS_HURT = mantisHurt;
        MANTIS_ATTACK = mantisAttack;
        MANTIS_FLY_LOOP = mantisFlyLoop;
        GRAVITY_GUN_PICKUP = gravityGunPickup;
        GRAVITY_GUN_DROP = gravityGunDrop;
        GRAVITY_GUN_HOLD_LOOP = gravityGunHoldLoop;
        GRAVITY_GUN_LAUNCH = gravityGunLaunch;
        GRAVITY_GUN_DRYFIRE = gravityGunDryfire;
        BED_BUG_AMBIENT = bedBugAmbient;
        BED_BUG_HURT = bedBugHurt;
        BED_BUG_ATTACK = bedBugAttack;
        WASP_IDLE = waspIdle;
        WASP_HURT = waspHurt;
        WASP_ATTACK = waspAttack;
        WASP_DEATH = waspDeath;
        SCORPION_AMBIENT = scorpionAmbient;
        SCORPION_HURT = scorpionHurt;
        SCORPION_ATTACK = scorpionAttack;
        EMPEROR_SCORPION_AMBIENT = emperorScorpionAmbient;
        EMPEROR_SCORPION_HURT = emperorScorpionHurt;
        EMPEROR_SCORPION_ATTACK = emperorScorpionAttack;
        EMPEROR_SCORPION_ROAR = emperorScorpionRoar;
        MOLEWORM_AMBIENT = molewormAmbient;
        MOLEWORM_HURT = molewormHurt;
        MOLEWORM_ATTACK = molewormAttack;
        MOLEWORM_DIG = molewormDig;
        MOLEVORE_AMBIENT = molevoreAmbient;
        MOLEVORE_HURT = molevoreHurt;
        MOLEVORE_ATTACK = molevoreAttack;
        MOLEVORE_DIG = molevoreDig;
        BOMBER_WALK = bomberWalk;
        BOMBER_KNOCK = bomberKnock;
        BOMBER_EXPLODE = bomberExplode;
        TORETERROR_IDLE = toreterrorIdle;
        TORETERROR_HURT = toreterrorHurt;
        TORETERROR_DEATH = toreterrorDeath;
        TORETERROR_BOMBER_FIRE = toreterrorBomberFire;
        TORETERROR_SPIN = toreterrorSpin;
        TORETERROR_RICOCHET = toreterrorRicochet;
        WATER_CANNON_FIRE = waterCannonFire;
        TORETERROR_JUMP_UP = toreterrorJumpUp;
        TORETERROR_JUMP_LAND = toreterrorJumpLand;
        STINKY_FLY = stinkyFly;
        STINK_BUG_FART = stinkBugFart;
        STINK_BUG_IDLE = stinkBugIdle;
        CAVARYN_HEARTBEAT = cavarynHeartbeat;
        ROLLY_POLLY_IDLE = rollyPollyIdle;
        CREEPING_HORROR_GROWL = creepingHorrorGrowl;
        CREEPING_HORROR_HURT = creepingHorrorHurt;
        CREEPING_HORROR_BITE = creepingHorrorBite;
        LURKING_TERROR_SNARL = lurkingTerrorSnarl;
        LURKING_TERROR_HURT = lurkingTerrorHurt;
        LURKING_TERROR_BITE = lurkingTerrorBite;
        LURKING_TERROR_FLY_LOOP = lurkingTerrorFlyLoop;
        DUCT_TAPE_USE = ductTapeUse;
        HERCULES_BEETLE_IDLE = herculesBeetleIdle;
        HERCULES_BEETLE_HURT = herculesBeetleHurt;
        HERCULES_BEETLE_ATTACK = herculesBeetleAttack;
        HERCULES_BEETLE_CRY = herculesBeetleCry;
        HERCULES_BEETLE_CHARGE_START = herculesBeetleChargeStart;
        HERCULES_BEETLE_KNOCKED_DOWN = herculesBeetleKnockedDown;
        JUMPY_BUG_IDLE = jumpyBugIdle;
        SPIT_BUG_IDLE = spitBugIdle;
        JUMPY_BUG_HURT = jumpyBugHurt;
        JUMPY_BUG_JUMP = jumpyBugJump;
        SPIT_BUG_HURT = spitBugHurt;
        SPIT_BUG_SPIT = spitBugSpit;
        BRUTALFLY_SPIT = brutalflySpit;
        ROLLY_POLLY_WHEEL_MODE = rollyPollyWheelMode;
        ROLLY_POLLY_NORMAL_MODE = rollyPollyNormalMode;
        ROLLY_POLLY_ROLL = rollyPollyRoll;
        JERRY_YOUNG_IDLE = jerryYoungIdle;
        JERRY_YOUNG_HURT = jerryYoungHurt;
        JERRY_YOUNG_DEATH = jerryYoungDeath;
        JERRY_YOUNG_ATTACK = jerryYoungAttack;
        JERRY_ADULT_IDLE = jerryAdultIdle;
        JERRY_ADULT_HURT = jerryAdultHurt;
        JERRY_ADULT_DEATH = jerryAdultDeath;
        JERRY_ADULT_ATTACK = jerryAdultAttack;
        WORM_HOOK_FLYING = wormHookFlying;
        BLOOD_CRYSTAL_KATANA_DASH = bloodCrystalKatanaDash;
        BLOOD_CRYSTAL_ARMOR_EQUIP = bloodCrystalArmorEquip;
        BLOODGLASS_WARD_HEART_BREAK = bloodglassWardHeartBreak;
        BLOODGLASS_WARD_HEART_REGEN = bloodglassWardHeartRegen;
        KING_IDLE = kingIdle;
        KING_HURT = kingHurt;
        KING_DEATH = kingDeath;
        KING_BITE = kingBite;
        QUEEN_IDLE = queenIdle;
        QUEEN_HURT = queenHurt;
        QUEEN_DEATH = queenDeath;
        QUEEN_BITE = queenBite;
        QUEEN_ROAR = queenRoar;
        KING_BEAM_SHOOT = kingBeamShoot;
        KING_FIREBALL_SHOOT = kingFireballShoot;
        KING_DECREE_CAST = kingDecreeCast;
        QUEEN_BEAM_SHOOT = queenBeamShoot;
        QUEEN_BEAM_START = queenBeamStart;
        QUEEN_BEAM_LOOP = queenBeamLoop;
        QUEEN_BEAM_END = queenBeamEnd;
        KING_STOMP = kingStomp;
        KING_ROAR = kingRoar;
        KING_WING_FLAP = kingWingFlap;
        KING_FLY_LOOP = kingFlyLoop;
        PRINCE_IDLE = princeIdle;
        PRINCE_BITE = princeBite;
        PRINCE_SHOOT = princeShoot;
        PRINCE_FLY = princeFly;
        PRINCE_STEP = princeStep;
        PRINCESS_IDLE = princessIdle;
        PRINCESS_BITE = princessBite;
        PRINCESS_SHOOT = princessShoot;
        PRINCESS_FLY = princessFly;
        PRINCESS_STEP = princessStep;
    }
}
