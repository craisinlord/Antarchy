package com.craisinlord.antarchy.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class AntarchySettings {
    private static boolean disableInfinityBookPortalCreation = true;
    private static boolean easterBunnyEnabled = true;
    private static int easterBunnyNaturalSpawnChancePercent = 1;
    private static boolean rainbowAntsLeadToInfinityDimensions = true;
    private static boolean brownAntRequiresReagent = false;
    private static boolean redAntRequiresReagent = false;
    private static boolean rainbowAntRequiresReagent = true;
    private static boolean termiteRequiresReagent = false;
    private static boolean antsStealFromChests = true;
    private static boolean duplicatorTreeEnabled = true;
    private static boolean glowingTorchflowers = true;
    private static boolean glowVinesUnderLeaves = true;
    private static boolean sizeChangingRaysEnabled = true;
    private static double sizeRayMinScale = 0.1D;
    private static double sizeRayMaxScale = 10.0D;
    private static double sizeRayDeltaPerHit = 0.25D;
    private static double shrinkingPotionDelta = 0.3D;
    private static double growthPotionDelta = 0.5D;
    private static double antDanceRadius = 5.0D;
    private static double brownAntHealth = 5.0D;
    private static double redAntHealth = 5.0D;
    private static double rainbowAntHealth = 5.0D;
    private static double termiteHealth = 5.0D;
    private static double waspHealth = 16.0D;
    private static double waspAttackDamage = 4.0D;
    private static double waspMovementSpeed = 0.39D;
    private static double bomberHealth = 10.0D;
    private static double bomberAttackDamage = 4.0D;
    private static double bomberExplosionDamage = 8.0D;
    private static double bomberExplosionRadius = 4.0D;
    private static double redAntAttackDamage = 3.0D;
    private static double ultimateSwordAttackDamage = 30.0D;
    private static double ultimatePickaxeAttackDamage = 22.0D;
    private static double ultimateAxeAttackDamage = 38.0D;
    private static double ultimateShovelAttackDamage = 24.0D;
    private static double ultimateHoeAttackDamage = 8.0D;
    private static double ultimateBowAttackDamage = 18.0D;
    private static double ultimateBowPlayerHeal = 8.0D;
    private static double ultimateBowDrawSpeedMultiplier = 2.5D;
    private static boolean ultimateBowComesEnchantedWithFlame = true;
    private static double ultimateCrossbowAttackDamage = 8.0D;
    private static double ultimateCrossbowChargeSpeedMultiplier = 0.25D;
    private static double ultimateMaceDamageMultiplier = 1.5D;
    private static double battleAxeAttackDamage = 44.0D;
    private static double bigBerthaAttackDamage = 55.0D;
    private static double bigBerthaReachBonus = 3.0D;
    private static double bigBerthaAttackSpeed = -3.0D;
    private static double scorpionWhipBaseDamage = 10.0D;
    private static double scorpionWhipReachBonus = 5.0D;
    private static int scorpionWhipPoisonDurationTicks = 100;
    private static double scorpionWhipTetherMaxRange = 10.0D;
    private static int scorpionWhipReelCooldownTicks = 10;
    private static double scorpionWhipSnapBonusDamage = 6.0D;
    private static int scorpionWhipSnapCooldownTicks = 30;
    private static double scorpionWhipPullStrength = 0.75D;
    private static double scorpionWhipHeavyPullMultiplier = 0.25D;
    private static double scorpionWhipSelfPullMultiplier = 0.45D;
    private static int bloodCrystalKatanaAttackDamage = 7;
    private static double bloodCrystalKatanaLaunchStrength = 1.1D;
    private static int bloodCrystalKatanaTrailDurationTicks = 12;
    private static double bigBerthaBasiliskCooldownSeconds = 7.0D;
    private static double bigBerthaLucidInvertedDurationSeconds = 3.0D;
    private static double bigBerthaLucidInvertedDamageBonusPercent = 25.0D;
    private static double potentNyxiteInvertedDurationSeconds = 2.0D;
    private static int ultimateToolEnchantability = 25;
    private static int ultimateArmorEnchantability = 10;
    private static int ultimateBowEnchantability = 20;
    private static int ultimateCrossbowEnchantability = 20;
    private static int ultimateMaceEnchantability = 20;
    private static boolean ultimateToolsThreeByThreeEnabled = true;
    private static boolean ultimateArmorComesEnchanted = true;
    private static int ultimateHelmetArmorValue = 4;
    private static int ultimateChestplateArmorValue = 9;
    private static int ultimateLeggingsArmorValue = 7;
    private static int ultimateBootsArmorValue = 4;
    private static double ultimateHelmetArmorToughness = 3.0D;
    private static double ultimateChestplateArmorToughness = 3.0D;
    private static double ultimateLeggingsArmorToughness = 3.0D;
    private static double ultimateBootsArmorToughness = 3.0D;
    private static ResourceKey<Level> brownAntDestinationDimension = dimensionKey("antarchy:elythia");
    private static ResourceKey<Level> redAntDestinationDimension = dimensionKey("antarchy:thoraxis");
    private static ResourceKey<Level> termiteDestinationDimension = dimensionKey("antarchy:cavaryn");
    private static ResourceKey<Level> rainbowAntNonInfinityFallbackDimension = dimensionKey("antarchy:elythia");
    private static double krakenHealth = 1500.0D;
    private static double krakenAttackDamage = 45.0D;
    private static double octopusBombHealth = 100.0D;
    private static double octopusBombAttackDamage = 16.0D;
    private static double brutalflyHealth = 400.0D;
    private static double brutalflySwipeDamage = 15.0D;
    private static double brutalflySpitDamage = 5.0D;
    private static double mantisHealth = 50.0D;
    private static double mantisAttackDamage = 9.0D;
    private static double mantisMovementSpeed = 0.42D;
    private static double mantisFlyingSpeed = 0.77D;
    private static boolean mantisIgnoreLightLevel = false;
    private static double krakenProjectileDamageTakenMultiplier = 0.5D;
    private static boolean krakenSquidSpawnEnabled = true;
    private static boolean krakenMassSpawnEnabled = true;
    private static double squidzookaCooldownSeconds = 1.0D;
    private static double squidzookaLaunchVelocity = 2.1D;
    private static double sizeRayCooldownSeconds = 1.0D;
    private static boolean invertProjectilesFromInvertedPlayers = false;
    private static boolean gravityGunEnabled = true;
    private static boolean gravityGunBlocksEnabled = true;
    private static boolean gravityGunEntitiesEnabled = true;
    private static double gravityGunRange = 16.0D;
    private static double gravityGunThrowStrength = 1.8D;
    private static double gravityGunBlastStrength = 1.4D;
    private static double gravityGunCooldownSeconds = 0.35D;
    private static double gravityGunMaxHoldDistance = 10.0D;
    private static double lucidAttackRange = 9.0D;
    private static double lucidPearlInvertedDurationSeconds = 6.0D;
    private static int corneaEarNightVisionSeconds = 15;
    private static int mogglesVisionRadius = 2;
    private static int mogglesVisionMaxLight = 7;
    private static float mogglesVisionAlpha = 0.35f;
    private static boolean dreamSandEnabled = true;
    private static boolean entitySpecificFireOverlayEnabled = true;
    private static double dreamSandJumpVelocityMultiplier = 1.3D;
    private static double dreamSandGravityMultiplier = 0.45D;
    private static double dreamSandFallDamageMultiplier = 0.35D;
    private static double dreamSandEffectDurationSeconds = 7.0D;
    private static boolean ichorWitherEnabled = true;
    private static int nightmareHelmetArmorValue = 3;
    private static int nightmareChestplateArmorValue = 8;
    private static int nightmareLeggingsArmorValue = 6;
    private static int nightmareBootsArmorValue = 3;
    private static double nightmareHelmetArmorToughness = 3.0D;
    private static double nightmareChestplateArmorToughness = 3.0D;
    private static double nightmareLeggingsArmorToughness = 3.0D;
    private static double nightmareBootsArmorToughness = 3.0D;
    
    private static double nightmareArmorDreadAuraRangePerPiece = 1.25D;
    private static double nightmareHelmetDoubleDamageChance = 0.04D;
    private static double nightmareChestplateDoubleDamageChance = 0.06D;
    private static double nightmareLeggingsDoubleDamageChance = 0.05D;
    private static double nightmareBootsDoubleDamageChance = 0.03D;

    private static double nightmareSwordBaseDamage = 15.0D;
    
    private static double nightmareSwordScalingFactor = 1.0D;
    private static boolean basiliskPetrifyingGazeEnabled = true;
    private static int basiliskSpawnMaxLightLevel = 5;
    private static double basiliskHealth = 150.0D;
    private static double basiliskAttackDamage = 36.0D;
    private static double basiliskMovementSpeed = 0.30D;
    private static double basiliskArmor = 6.0D;
    private static double basiliskKnockbackResistance = 0.5D;
    private static double basiliskFollowRange = 28.0D;
    private static int basiliskXpReward = 50;
    private static int basiliskAttackAnimTicks = 18;
    private static int basiliskAttackDamageTick = 9;
    private static int basiliskHissCooldownTicks = 600;
    private static double basiliskAttackReach = 3.0D;
    private static double basiliskGazeRange = 12.0D;
    private static double basiliskGazeDotThreshold = 0.64D;
    private static double basiliskGazeFacingThreshold = 0.45D;
    private static int basiliskHissChargeTicks = 30;
    private static int basiliskPlayerParalyzeTicks = 50;
    private static int basiliskPreyPetrifyCooldownTicks = 500;
    private static double basiliskPreyPetrifyRange = 8.0D;
    private static int basiliskPreyPetrifyTicks = 200;
    private static boolean diamondMinecartEnabled = true;
    private static boolean diamondMinecartPlacesRails = true;
    private static double diamondMinecartCruiseSpeed = 0.6D;
    private static double diamondMinecartMaxSpeed = 1.0D;
    private static double diamondMinecartAcceleration = 0.03D;
    private static double diamondMinecartDeceleration = 0.04D;
    private static double diamondMinecartCoastDeceleration = 0.01D;
    private static boolean diamondMinecartMobDamageEnabled = true;
    private static double diamondMinecartMaxMobDamage = 8.0D;
    private static boolean dreadHallucinationSoundsEnabled = true;
    private static double dreadHallucinationSoundMinInterval = 3.0D;
    private static double dreadHallucinationSoundMaxInterval = 10.0D;
    private static boolean dreadHallucinationMobsEnabled = true;
    private static double dreadHallucinationMobMinInterval = 6.0D;
    private static double dreadHallucinationMobMaxInterval = 16.0D;
    private static double brownAntMovementSpeed = 0.28D;
    private static double redAntMovementSpeed = 0.30D;
    private static double rainbowAntMovementSpeed = 0.28D;
    private static double termiteMovementSpeed = 0.28D;
    private static double brownAntAttackDamage = 1.0D;
    private static double rainbowAntAttackDamage = 1.0D;
    private static double termiteAttackDamage = 1.0D;
    private static double flyingSquirrelHealth = 10.0D;
    private static double flyingSquirrelMovementSpeed = 0.35D;
    private static double flyingSquirrelFollowRange = 16.0D;
    private static double caterpillarHealth = 6.0D;
    private static double caterpillarMovementSpeed = 0.24D;
    private static double caterpillarFollowRange = 14.0D;
    private static double caterpillarPupationTimeSeconds = 600.0D;
    private static double butterflyHealth = 6.0D;
    private static double butterflyMovementSpeed = 0.3D;
    private static double butterflyFlyingSpeed = 0.55D;
    private static double butterflyFollowRange = 32.0D;
    private static double  reverieHealth = 20.0D;
    private static double  reverieInterestRadius = 16.0D;
    private static double  reverieAbandonPlayerDistance = 24.0D;
    private static int     reverieNoticeDurationTicks = 20;
    private static int     reverieInterestDurationTicks = 360;
    private static int     reverieRebindCooldownTicks = 100;
    private static int     reverieDamageReactionDurationTicks = 40;
    private static double  reverieDangerousFallDistance = 4.0D;
    private static double  reverieDangerousFallSpeed = 0.18D;
    private static double  reverieWarningThreatRadius = 10.0D;
    private static double  reverieWarningThreatVerticalRange = 5.0D;
    private static int     reverieDuplicationCooldownTicks = 6000;
    private static final boolean reverieOnlyPlayerDamage         = true;
    private static final double  reveriePreferredFollowMinDistance = 3.0D;
    private static final double  reveriePreferredFollowMaxDistance = 6.0D;
    private static final double  reverieCatchUpDistance           = 12.0D;
    private static final double  reveriePurpleProtectRadius       = 2.7D;
    private static final int     reverieWarningApproachTicks      = 8;
    private static final int     reverieWarningHoverTicks         = 6;
    private static final int     reverieWarningReturnTicks        = 8;
    private static final int     reverieWarningPlayerHoverTicks   = 10;
    private static final double  reverieAmbientTargetMinRadius    = 1.2D;
    private static final double  reverieAmbientTargetMaxRadius    = 4.4D;
    private static final double  reverieAmbientTargetVerticalRange = 1.8D;
    private static final int     reverieAmbientTargetMaxAgeTicks  = 40;
    private static final int     reverieAmbientPulseIntervalMinTicks = 12;
    private static final int     reverieAmbientPulseIntervalMaxTicks = 19;
    private static double emperorScorpionHealth = 100.0D;
    private static double emperorScorpionAttackDamage = 12.0D;
    private static double emperorScorpionMovementSpeed = 0.32D;
    private static double emperorScorpionArmor = 6.0D;
    private static double emperorScorpionKnockbackResistance = 0.6D;
    private static double emperorScorpionFollowRange = 32.0D;
    private static int emperorScorpionXpReward = 25;
    private static int emperorScorpionClawAnimTicks = 12;
    private static int emperorScorpionClawHitTick = 6;
    private static int emperorScorpionClawCooldownTicks = 16;
    private static int emperorScorpionStingAnimTicks = 18;
    private static int emperorScorpionStingHitTick = 9;
    private static int emperorScorpionStingCooldownTicks = 42;
    private static int emperorScorpionPoisonTicks = 100;
    private static int emperorScorpionWeaknessTicks = 80;
    private static int emperorScorpionSummonIntervalTicks = 180;
    private static int emperorScorpionMaxSummonedScorpions = 4;
    private static double scorpionHealth = 30.0D;
    private static double scorpionAttackDamage = 5.0D;
    private static double scorpionMovementSpeed = 0.29D;
    private static double scorpionArmor = 2.0D;
    private static double scorpionKnockbackResistance = 0.2D;
    private static double scorpionFollowRange = 24.0D;
    private static int scorpionXpReward = 5;
    private static int scorpionAttackAnimTicks = 12;
    private static int scorpionAttackHitTick = 6;
    private static int scorpionAttackCooldownTicks = 16;
    private static int scorpionPoisonTicks = 80;
    private static double bedBugHealth = 18.0D;
    private static double bedBugAttackDamage = 4.0D;
    private static double bedBugMovementSpeed = 0.24D;
    private static double bedBugArmor = 2.0D;
    private static double bedBugFollowRange = 20.0D;
    private static int bedBugXpReward = 4;
    private static int bedBugBiteAnimTicks = 10;
    private static int bedBugAttackHitTick = 5;
    private static int bedBugAttackCooldownTicks = 12;
    private static int bedBugMinLayEggDelay = 80;
    private static int bedBugMaxLayEggDelay = 160;
    private static double bedBugFoodSearchRadius = 10.0D;
    private static double bedBugEggGuardRadius = 7.0D;
    private static double bedBugAttackStartReachBuffer = 1.15D;
    private static double bedBugAttackReachBuffer = 0.3D;
    private static double bedBugAttackLungeHorizontalSpeed = 0.42D;
    private static double bedBugAttackLungeVerticalSpeed = 0.1D;
    private static float bedBugHealAmount = 2.0F;
    private static double jumpyBugHealth = 12.0D;
    private static double jumpyBugPounceDamage = 2.0D;
    private static double jumpyBugLatchDamage = 1.0D;
    private static double jumpyBugCamouflageAlpha = 0.18D;
    private static double brutalflyArmor = 10.0D;
    private static double brutalflyArmorToughness = 4.0D;
    private static double brutalflyKnockbackResistance = 0.7D;
    private static double brutalflyMovementSpeed = 0.34D;
    private static double brutalflyFlyingSpeed = 0.55D;
    private static double brutalflyFollowRange = 48.0D;
    private static int brutalflyXpReward = 80;
    private static int brutalflyDeathSpawnCountMin = 5;
    private static int brutalflyDeathSpawnCountMax = 10;
    private static int brutalflySpitWindupTicks = 16;
    private static int brutalflySwipeTicks = 18;
    private static float brutalflySwipeKnockback = 2.8F;
    private static int brutalflyPhaseOneSpitCooldownMin = 70;
    private static int brutalflyPhaseOneSpitCooldownMax = 95;
    private static int brutalflyPhaseTwoSpitCooldownMin = 42;
    private static int brutalflyPhaseTwoSpitCooldownMax = 60;
    private static int brutalflyPhaseThreeSpitCooldownMin = 26;
    private static int brutalflyPhaseThreeSpitCooldownMax = 40;
    private static int brutalflyPhaseOneMeleeCooldownMin = 42;
    private static int brutalflyPhaseOneMeleeCooldownMax = 51;
    private static int brutalflyPhaseTwoMeleeCooldownMin = 30;
    private static int brutalflyPhaseTwoMeleeCooldownMax = 37;
    private static int brutalflyPhaseThreeMeleeCooldownMin = 20;
    private static int brutalflyPhaseThreeMeleeCooldownMax = 26;
    private static double cloudSharkHealth = 40.0D;
    private static double cloudSharkAttackDamage = 5.0D;
    private static double cloudSharkMovementSpeed = 0.34D;
    private static double cloudSharkFlyingSpeed = 0.5D;
    private static double cloudSharkFollowRange = 40.0D;
    private static double cloudSharkKnockbackResistance = 0.2D;
    private static int cloudSharkXpReward = 8;
    private static double krakenMovementSpeed = 0.24D;
    private static double krakenFlyingSpeed = 0.24D;
    private static double krakenFollowRange = 64.0D;
    private static double krakenKnockbackResistance = 0.8D;
    private static double krakenArmor = 8.0D;
    private static int krakenXpReward = 60;
    private static double krakenPhaseTwoSpeedBonus = 0.15D;
    private static double krakenPhaseTwoFlyingSpeedBonus = 0.15D;
    private static double krakenPhaseTwoDamageBonus = 4.0D;
    private static double krakenBossBarRange = 40.0D;
    private static float krakenLightningDamagePhaseOne = 8.0F;
    private static float krakenLightningDamagePhaseTwo = 12.0F;
    private static int krakenLightningAmbientCooldownPhaseOneMin = 80;
    private static int krakenLightningAmbientCooldownPhaseOneMax = 160;
    private static int krakenLightningAmbientCooldownPhaseTwoMin = 35;
    private static int krakenLightningAmbientCooldownPhaseTwoMax = 70;
    private static int krakenLightningStrikeCooldownPhaseOneMin = 60;
    private static int krakenLightningStrikeCooldownPhaseOneMax = 140;
    private static int krakenLightningStrikeCooldownPhaseTwoMin = 26;
    private static int krakenLightningStrikeCooldownPhaseTwoMax = 46;
    private static int krakenPhaseSummonCooldownPhaseOneMin = 90;
    private static int krakenPhaseSummonCooldownPhaseOneMax = 140;
    private static int krakenPhaseSummonCooldownPhaseTwoMin = 90;
    private static int krakenPhaseSummonCooldownPhaseTwoMax = 140;
    private static int krakenPhaseSummonedMinionsMin = 1;
    private static int krakenPhaseSummonedMinionsMax = 2;
    private static int krakenPhaseTwoMinionsNearbyCap = 12;
    private static int krakenPhaseTwoMinionSpawnAttempts = 18;
    private static double lucidHealth = 30.0D;
    private static double lucidAttackDamage = 4.0D;
    private static double lucidMovementSpeed = 0.22D;
    private static double lucidFlyingSpeed = 0.35D;
    private static double lucidFollowRange = 40.0D;
    private static double lucidKnockbackResistance = 0.5D;
    private static int lucidXpReward = 10;
    private static int lucidAttackWindupTicks = 16;
    private static int lucidAttackRecoveryTicks = 8;
    private static int lucidBurstShots = 3;
    private static int lucidBurstIntervalTicks = 6;
    private static int lucidAttackCooldownTicks = 52;
    private static double lucidMinEffectiveAttackRange = 10.0D;
    private static double lucidMinHoverRange = 6.5D;
    private static double lucidCloseRange = 4.5D;
    private static double lucidDefaultHoverHeight = 2.75D;
    private static double lucidHitboxWidthScale = 1.3D;
    private static double lucidHitboxHeightScale = 1.48D;
    private static double lucidHitboxYOffset = 1.08D;
    private static double missileSquidHealth = 50.0D;
    private static double missileSquidAttackDamage = 6.0D;
    private static double missileSquidMovementSpeed = 0.84D;
    private static double missileSquidFlyingSpeed = 0.84D;
    private static double missileSquidFollowRange = 20.0D;
    private static double missileSquidKnockbackResistance = 0.15D;
    private static int missileSquidXpReward = 6;
    private static int missileSquidLatchDamageInterval = 15;
    private static float missileSquidLatchDamage = 2.0F;
    private static double missileSquidAttachRangeSqr = 2.25D;
    private static double molewormHealth = 8.0D;
    private static double molewormAttackDamage = 1.0D;
    private static double molewormMovementSpeed = 0.25D;
    private static int molewormXpReward = 0;
    private static double triffidHealth = 80.0D;
    private static double triffidAttackDamage = 10.0D;
    private static double triffidGrabDamage = 15.0D;
    private static double molevoreHealth = 60.0D;
    private static double molevoreAttackDamage = 10.0D;
    private static double molevoreMovementSpeed = 0.27D;
    private static double molevoreArmor = 6.0D;
    private static double molevoreKnockbackResistance = 0.65D;
    private static double molevoreFollowRange = 28.0D;
    private static int molevoreXpReward = 20;
    private static int molevoreSpinTicks = 12;
    private static int molevoreCooldownTicks = 60;
    private static double molevoreChargeSpeed = 1.15D;
    private static double molevoreBreakRange = 1.8D;
    private static double molevoreBreakVerticalRange = 2.2D;
    private static double molevoreBreakHalfWidth = 1.1D;
    private static double nightmareHealth = 180.0D;
    private static double nightmareAttackDamage = 16.0D;
    private static double nightmareMovementSpeed = 0.28D;
    private static double nightmareFlyingSpeed = 0.3D;
    private static double nightmareFollowRange = 48.0D;
    private static double nightmareKnockbackResistance = 0.75D;
    private static double nightmareArmor = 8.0D;
    private static int nightmareXpReward = 25;
    private static int nightmareAttackTotalTicks = 18;
    private static int nightmareAttackDamageTick = 9;
    private static int nightmareIntroRoarTicks = 32;
    private static int nightmareCombatRoarTicks = 24;
    private static int nightmareDeathTicks = 36;
    private static int nightmareTargetResetTicks = 60;
    private static int nightmareBedBugEggSearchInterval = 30;
    private static int nightmareDreadTicks = 160;
    private static int nightmareWeaknessTicks = 100;
    private static double nightmarePatrolSpeed = 0.34D;
    private static double nightmareCombatFlightSpeed = 0.58D;
    private static double nightmareGroundApproachSpeed = 0.82D;
    private static double nightmareAttackStartRangeSqr = 42.25D;
    private static double nightmareGroundApproachRangeSqr = 16.0D * 16.0D;
    private static double nightmareAttackReachRadius = 2.65D;
    private static double nightmareAttackCommitHorizontalRange = 4.2D;
    private static double nightmareAttackCommitVerticalRange = 2.8D;
    private static double nightmareFlightReengageRangeSqr = 10.0D * 10.0D;
    private static double nightmareCloseApproachRangeSqr = 6.0D * 6.0D;
    private static double nightmareRoarRetryDistanceSqr = 20.0D * 20.0D;
    private static double nightmareBedBugEggBreakRangeSqr = 2.8D * 2.8D;
    private static float nightmareAirPatrolChance = 0.2F;
    private static double battleAxeAttackSpeed = -3.1D;
    private static double basiliskDaggerAttackDamage = 6.0D;
    private static double basiliskDaggerAttackSpeed = -1.8D;
    private static int basiliskDaggerPoisonDurationTicks = 200;
    private static int basiliskDaggerPoisonAmplifier = 2;
    private static double ultimateSwordAttackSpeed = -2.4D;
    private static double ultimatePickaxeAttackSpeed = -2.8D;
    private static double ultimateAxeAttackSpeed = -3.0D;
    private static double ultimateShovelAttackSpeed = -3.0D;
    private static double ultimateHoeAttackSpeed = 0.0D;
    private static double ultimateMaceAttackSpeed = -3.4D;
    private static double nightmareSwordAttackSpeed = -2.4D;
    private static int bigBerthaBasiliskParalyzeDurationTicks = 80;
    private static int bigBerthaKrakenSlowTicks = 80;
    private static double ductTapeRepairPercentPerUse = 0.25D;
    private static double ultimateArmorKnockbackResistance = 0.1D;
    private static double nightmareArmorKnockbackResistance = 0.1D;
    private static double primordialArmorKnockbackPerPiece = 0.5D;
    private static int fallenKingCrownArmorValue = 2;
    private static double fallenKingCrownArmorToughness = 0.0D;
    private static boolean elythiaFireflyParticlesEnabled = true;
    private static int bloodCrystalArmorShieldRechargeTicks = 600;
    private static int bloodCrystalAppleShieldCount = 2;
    private static int bloodCrystalAppleDurationTicks = 2400;
    private static int bloodCrystalAppleShieldRechargeTicks = 600;
    private static int bloodCrystalHardMaxShields = 8;
    // Armor stats (iron defense, gold durability, 0 toughness by default)
    private static int bloodCrystalHelmetDefense = 2;
    private static int bloodCrystalChestplateDefense = 6;
    private static int bloodCrystalLeggingsDefense = 5;
    private static int bloodCrystalBootsDefense = 2;
    private static double bloodCrystalArmorToughness = 0.0;
    private static int bloodCrystalHelmetDurability = 77;
    private static int bloodCrystalChestplateDurability = 112;
    private static int bloodCrystalLeggingsDurability = 105;
    private static int bloodCrystalBootsDurability = 91;
    private static double toreterrorHealth = 300.0D;
    private static double toreterrorJumpAttackDamage = 14.0D;
    private static double toreterrorJumpAttackKnockback = 2.5D;
    private static double toreterrorSpinDamage = 8.0D;
    private static double toreterrorSpinKnockback = 1.5D;
    private static double toreterrorRangedWaterBombChance = 0.5D;
    private static double toreterrorProjectileDamageMultiplier = 0.5D;
    private static double waterBombDamage = 6.0D;
    private static int waterBombLifetimeTicks = 120;
    private static double waterBombGravity = 0.12D;
    private static double waterBombKnockback = 1.2D;
    private static double waterCannonCooldownSeconds = 1.5D;
    private static double creepingHorrorHealth = 15.0D;
    private static double creepingHorrorAttackDamage = 6.0D;
    private static double lurkingTerrorHealth = 15.0D;
    private static double lurkingTerrorAttackDamage = 6.0D;

    private AntarchySettings() {
    }

    private static ResourceKey<Level> dimensionKey(String id) {
        return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(id));
    }

    public static boolean disableInfinityBookPortalCreation() {
        return disableInfinityBookPortalCreation;
    }

    public static boolean easterBunnyEnabled() {
        return easterBunnyEnabled;
    }

    public static int easterBunnyNaturalSpawnChancePercent() {
        return easterBunnyNaturalSpawnChancePercent;
    }

    public static boolean rainbowAntsLeadToInfinityDimensions() {
        return rainbowAntsLeadToInfinityDimensions;
    }

    public static boolean brownAntRequiresReagent() {
        return brownAntRequiresReagent;
    }

    public static boolean redAntRequiresReagent() {
        return redAntRequiresReagent;
    }

    public static boolean rainbowAntRequiresReagent() {
        return rainbowAntRequiresReagent;
    }

    public static boolean termiteRequiresReagent() {
        return termiteRequiresReagent;
    }

    public static boolean antsStealFromChests() {
        return antsStealFromChests;
    }

    public static boolean duplicatorTreeEnabled() {
        return duplicatorTreeEnabled;
    }

    public static boolean glowingTorchflowers() {
        return glowingTorchflowers;
    }

    public static boolean glowVinesUnderLeaves() {
        return glowVinesUnderLeaves;
    }

    public static boolean sizeChangingRaysEnabled() {
        return sizeChangingRaysEnabled;
    }

    public static double sizeRayMinScale() {
        return sizeRayMinScale;
    }

    public static double sizeRayMaxScale() {
        return sizeRayMaxScale;
    }

    public static double sizeRayDeltaPerHit() {
        return sizeRayDeltaPerHit;
    }

    public static double shrinkingPotionDelta() {
        return shrinkingPotionDelta;
    }

    public static double growthPotionDelta() {
        return growthPotionDelta;
    }

    public static double antDanceRadius() {
        return antDanceRadius;
    }

    public static double brownAntHealth() {
        return brownAntHealth;
    }

    public static double redAntHealth() {
        return redAntHealth;
    }

    public static double rainbowAntHealth() {
        return rainbowAntHealth;
    }

    public static double termiteHealth() {
        return termiteHealth;
    }

    public static double redAntAttackDamage() {
        return redAntAttackDamage;
    }

    public static double ultimateSwordAttackDamage() {
        return ultimateSwordAttackDamage;
    }

    public static double ultimatePickaxeAttackDamage() {
        return ultimatePickaxeAttackDamage;
    }

    public static double ultimateAxeAttackDamage() {
        return ultimateAxeAttackDamage;
    }

    public static double ultimateShovelAttackDamage() {
        return ultimateShovelAttackDamage;
    }

    public static double ultimateHoeAttackDamage() {
        return ultimateHoeAttackDamage;
    }

    public static double ultimateBowAttackDamage() {
        return ultimateBowAttackDamage;
    }

    public static double ultimateBowPlayerHeal() {
        return ultimateBowPlayerHeal;
    }

    public static double ultimateBowDrawSpeedMultiplier() {
        return ultimateBowDrawSpeedMultiplier;
    }

    public static boolean ultimateBowComesEnchantedWithFlame() {
        return ultimateBowComesEnchantedWithFlame;
    }

    public static double ultimateCrossbowAttackDamage() {
        return ultimateCrossbowAttackDamage;
    }

    public static double ultimateCrossbowChargeSpeedMultiplier() {
        return ultimateCrossbowChargeSpeedMultiplier;
    }

    public static double ultimateMaceDamageMultiplier() {
        return ultimateMaceDamageMultiplier;
    }

    public static double battleAxeAttackDamage() {
        return battleAxeAttackDamage;
    }

    public static double bigBerthaAttackDamage() {
        return bigBerthaAttackDamage;
    }

    public static double bigBerthaReachBonus() {
        return bigBerthaReachBonus;
    }

    public static double bigBerthaAttackSpeed() {
        return bigBerthaAttackSpeed;
    }

    public static double bigBerthaBasiliskCooldownSeconds() {
        return bigBerthaBasiliskCooldownSeconds;
    }

    public static double bigBerthaLucidInvertedDurationSeconds() {
        return bigBerthaLucidInvertedDurationSeconds;
    }

    public static double bigBerthaLucidInvertedDamageBonusPercent() {
        return bigBerthaLucidInvertedDamageBonusPercent;
    }

    public static double potentNyxiteInvertedDurationSeconds() {
        return potentNyxiteInvertedDurationSeconds;
    }

    public static int ultimateToolEnchantability() {
        return ultimateToolEnchantability;
    }

    public static int ultimateArmorEnchantability() {
        return ultimateArmorEnchantability;
    }

    public static int ultimateBowEnchantability() {
        return ultimateBowEnchantability;
    }

    public static int ultimateCrossbowEnchantability() {
        return ultimateCrossbowEnchantability;
    }

    public static int ultimateMaceEnchantability() {
        return ultimateMaceEnchantability;
    }

    public static boolean ultimateToolsThreeByThreeEnabled() {
        return ultimateToolsThreeByThreeEnabled;
    }

    public static boolean ultimateArmorComesEnchanted() {
        return ultimateArmorComesEnchanted;
    }

    public static int ultimateHelmetArmorValue() {
        return ultimateHelmetArmorValue;
    }

    public static int ultimateChestplateArmorValue() {
        return ultimateChestplateArmorValue;
    }

    public static int ultimateLeggingsArmorValue() {
        return ultimateLeggingsArmorValue;
    }

    public static int ultimateBootsArmorValue() {
        return ultimateBootsArmorValue;
    }

    public static double ultimateHelmetArmorToughness() {
        return ultimateHelmetArmorToughness;
    }

    public static double ultimateChestplateArmorToughness() {
        return ultimateChestplateArmorToughness;
    }

    public static double ultimateLeggingsArmorToughness() {
        return ultimateLeggingsArmorToughness;
    }

    public static double ultimateBootsArmorToughness() {
        return ultimateBootsArmorToughness;
    }

    public static ResourceKey<Level> brownAntDestinationDimension() {
        return brownAntDestinationDimension;
    }

    public static ResourceKey<Level> redAntDestinationDimension() {
        return redAntDestinationDimension;
    }

    public static ResourceKey<Level> termiteDestinationDimension() {
        return termiteDestinationDimension;
    }

    public static ResourceKey<Level> rainbowAntNonInfinityFallbackDimension() {
        return rainbowAntNonInfinityFallbackDimension;
    }

    public static double krakenHealth() {
        return krakenHealth;
    }

    public static double krakenAttackDamage() {
        return krakenAttackDamage;
    }

    public static double octopusBombHealth() {
        return octopusBombHealth;
    }

    public static void setOctopusBombHealth(double value) { octopusBombHealth = value; }

    public static double octopusBombAttackDamage() {
        return octopusBombAttackDamage;
    }

    public static void setOctopusBombAttackDamage(double value) { octopusBombAttackDamage = value; }

    public static double brutalflyHealth() {
        return brutalflyHealth;
    }

    public static double brutalflySwipeDamage() {
        return brutalflySwipeDamage;
    }

    public static double brutalflySpitDamage() {
        return brutalflySpitDamage;
    }

    public static double mantisHealth() {
        return mantisHealth;
    }

    public static double mantisAttackDamage() {
        return mantisAttackDamage;
    }

    public static double mantisMovementSpeed() {
        return mantisMovementSpeed;
    }

    public static double mantisFlyingSpeed() {
        return mantisFlyingSpeed;
    }

    public static boolean mantisIgnoreLightLevel() {
        return mantisIgnoreLightLevel;
    }

    public static void setMantisIgnoreLightLevel(boolean value) {
        mantisIgnoreLightLevel = value;
    }

    public static double waspHealth() {
        return waspHealth;
    }

    public static double waspAttackDamage() {
        return waspAttackDamage;
    }

    public static double waspMovementSpeed() {
        return waspMovementSpeed;
    }

    public static void setWaspMovementSpeed(double value) {
        waspMovementSpeed = value;
    }

    public static double bomberHealth() {
        return bomberHealth;
    }

    public static double bomberAttackDamage() {
        return bomberAttackDamage;
    }

    public static double bomberExplosionDamage() {
        return bomberExplosionDamage;
    }

    public static double bomberExplosionRadius() {
        return bomberExplosionRadius;
    }

    public static double krakenProjectileDamageTakenMultiplier() {
        return krakenProjectileDamageTakenMultiplier;
    }

    public static boolean krakenSquidSpawnEnabled() {
        return krakenSquidSpawnEnabled;
    }

    public static boolean krakenMassSpawnEnabled() {
        return krakenMassSpawnEnabled;
    }

    public static double squidzookaCooldownSeconds() {
        return squidzookaCooldownSeconds;
    }

    public static double squidzookaLaunchVelocity() {
        return squidzookaLaunchVelocity;
    }

    public static double sizeRayCooldownSeconds() {
        return sizeRayCooldownSeconds;
    }

    public static boolean invertProjectilesFromInvertedPlayers() {
        return invertProjectilesFromInvertedPlayers;
    }

    public static boolean gravityGunEnabled() {
        return gravityGunEnabled;
    }

    public static boolean gravityGunBlocksEnabled() {
        return gravityGunBlocksEnabled;
    }

    public static boolean gravityGunEntitiesEnabled() {
        return gravityGunEntitiesEnabled;
    }

    public static double gravityGunRange() {
        return gravityGunRange;
    }

    public static double gravityGunThrowStrength() {
        return gravityGunThrowStrength;
    }

    public static double gravityGunBlastStrength() {
        return gravityGunBlastStrength;
    }

    public static double gravityGunCooldownSeconds() {
        return gravityGunCooldownSeconds;
    }

    public static double gravityGunMaxHoldDistance() {
        return gravityGunMaxHoldDistance;
    }

    public static void setDisableInfinityBookPortalCreation(boolean value) {
        disableInfinityBookPortalCreation = value;
    }

    public static void setEasterBunnyEnabled(boolean value) {
        easterBunnyEnabled = value;
    }

    public static void setEasterBunnyNaturalSpawnChancePercent(int value) {
        easterBunnyNaturalSpawnChancePercent = value;
    }

    public static void setRainbowAntsLeadToInfinityDimensions(boolean value) {
        rainbowAntsLeadToInfinityDimensions = value;
    }

    public static void setBrownAntRequiresReagent(boolean value) {
        brownAntRequiresReagent = value;
    }

    public static void setRedAntRequiresReagent(boolean value) {
        redAntRequiresReagent = value;
    }

    public static void setRainbowAntRequiresReagent(boolean value) {
        rainbowAntRequiresReagent = value;
    }

    public static void setTermiteRequiresReagent(boolean value) {
        termiteRequiresReagent = value;
    }

    public static void setAntsStealFromChests(boolean value) {
        antsStealFromChests = value;
    }

    public static void setDuplicatorTreeEnabled(boolean value) {
        duplicatorTreeEnabled = value;
    }

    public static void setGlowingTorchflowers(boolean value) {
        glowingTorchflowers = value;
    }

    public static void setGlowVinesUnderLeaves(boolean value) {
        glowVinesUnderLeaves = value;
    }

    public static void setSizeChangingRaysEnabled(boolean value) {
        sizeChangingRaysEnabled = value;
    }

    public static void setSizeRayMinScale(double value) {
        sizeRayMinScale = value;
    }

    public static void setSizeRayMaxScale(double value) {
        sizeRayMaxScale = value;
    }

    public static void setSizeRayDeltaPerHit(double value) {
        sizeRayDeltaPerHit = value;
    }

    public static void setShrinkingPotionDelta(double value) {
        shrinkingPotionDelta = value;
    }

    public static void setGrowthPotionDelta(double value) {
        growthPotionDelta = value;
    }

    public static void setAntDanceRadius(double value) {
        antDanceRadius = value;
    }

    public static void setBrownAntHealth(double value) {
        brownAntHealth = value;
    }

    public static void setRedAntHealth(double value) {
        redAntHealth = value;
    }

    public static void setRainbowAntHealth(double value) {
        rainbowAntHealth = value;
    }

    public static void setTermiteHealth(double value) {
        termiteHealth = value;
    }

    public static void setWaspHealth(double value) {
        waspHealth = value;
    }

    public static void setWaspAttackDamage(double value) {
        waspAttackDamage = value;
    }

    public static void setBomberHealth(double value) {
        bomberHealth = value;
    }

    public static void setBomberAttackDamage(double value) {
        bomberAttackDamage = value;
    }

    public static void setBomberExplosionDamage(double value) {
        bomberExplosionDamage = value;
    }

    public static void setBomberExplosionRadius(double value) {
        bomberExplosionRadius = value;
    }

    public static void setRedAntAttackDamage(double value) {
        redAntAttackDamage = value;
    }

    public static void setUltimateSwordAttackDamage(double value) {
        ultimateSwordAttackDamage = value;
    }

    public static void setUltimatePickaxeAttackDamage(double value) {
        ultimatePickaxeAttackDamage = value;
    }

    public static void setUltimateAxeAttackDamage(double value) {
        ultimateAxeAttackDamage = value;
    }

    public static void setUltimateShovelAttackDamage(double value) {
        ultimateShovelAttackDamage = value;
    }

    public static void setUltimateHoeAttackDamage(double value) {
        ultimateHoeAttackDamage = value;
    }

    public static void setUltimateBowAttackDamage(double value) {
        ultimateBowAttackDamage = value;
    }

    public static void setUltimateBowPlayerHeal(double value) {
        ultimateBowPlayerHeal = value;
    }

    public static void setUltimateBowDrawSpeedMultiplier(double value) {
        ultimateBowDrawSpeedMultiplier = value;
    }

    public static void setUltimateBowComesEnchantedWithFlame(boolean value) {
        ultimateBowComesEnchantedWithFlame = value;
    }

    public static void setUltimateCrossbowAttackDamage(double value) {
        ultimateCrossbowAttackDamage = value;
    }

    public static void setUltimateCrossbowChargeSpeedMultiplier(double value) {
        ultimateCrossbowChargeSpeedMultiplier = value;
    }

    public static void setUltimateMaceDamageMultiplier(double value) {
        ultimateMaceDamageMultiplier = value;
    }

    public static void setBattleAxeAttackDamage(double value) {
        battleAxeAttackDamage = value;
    }

    public static void setBigBerthaAttackDamage(double value) {
        bigBerthaAttackDamage = value;
    }

    public static void setBigBerthaReachBonus(double value) {
        bigBerthaReachBonus = value;
    }

    public static void setBigBerthaAttackSpeed(double value) {
        bigBerthaAttackSpeed = value;
    }

    public static void setBigBerthaBasiliskCooldownSeconds(double value) {
        bigBerthaBasiliskCooldownSeconds = value;
    }

    public static void setBigBerthaLucidInvertedDurationSeconds(double value) {
        bigBerthaLucidInvertedDurationSeconds = value;
    }

    public static void setBigBerthaLucidInvertedDamageBonusPercent(double value) {
        bigBerthaLucidInvertedDamageBonusPercent = value;
    }

    public static void setPotentNyxiteInvertedDurationSeconds(double value) {
        potentNyxiteInvertedDurationSeconds = value;
    }

    public static void setUltimateToolEnchantability(int value) {
        ultimateToolEnchantability = value;
    }

    public static void setUltimateArmorEnchantability(int value) {
        ultimateArmorEnchantability = value;
    }

    public static void setUltimateBowEnchantability(int value) {
        ultimateBowEnchantability = value;
    }

    public static void setUltimateCrossbowEnchantability(int value) {
        ultimateCrossbowEnchantability = value;
    }

    public static void setUltimateMaceEnchantability(int value) {
        ultimateMaceEnchantability = value;
    }

    public static void setUltimateToolsThreeByThreeEnabled(boolean value) {
        ultimateToolsThreeByThreeEnabled = value;
    }

    public static void setUltimateArmorComesEnchanted(boolean value) {
        ultimateArmorComesEnchanted = value;
    }

    public static void setUltimateHelmetArmorValue(int value) {
        ultimateHelmetArmorValue = value;
    }

    public static void setUltimateChestplateArmorValue(int value) {
        ultimateChestplateArmorValue = value;
    }

    public static void setUltimateLeggingsArmorValue(int value) {
        ultimateLeggingsArmorValue = value;
    }

    public static void setUltimateBootsArmorValue(int value) {
        ultimateBootsArmorValue = value;
    }

    public static void setUltimateHelmetArmorToughness(double value) {
        ultimateHelmetArmorToughness = value;
    }

    public static void setUltimateChestplateArmorToughness(double value) {
        ultimateChestplateArmorToughness = value;
    }

    public static void setUltimateLeggingsArmorToughness(double value) {
        ultimateLeggingsArmorToughness = value;
    }

    public static void setUltimateBootsArmorToughness(double value) {
        ultimateBootsArmorToughness = value;
    }

    public static void setBrownAntDestinationDimension(String value) {
        brownAntDestinationDimension = dimensionKey(value);
    }

    public static void setRedAntDestinationDimension(String value) {
        redAntDestinationDimension = dimensionKey(value);
    }

    public static void setTermiteDestinationDimension(String value) {
        termiteDestinationDimension = dimensionKey(value);
    }

    public static void setRainbowAntNonInfinityFallbackDimension(String value) {
        rainbowAntNonInfinityFallbackDimension = dimensionKey(value);
    }

    public static void setKrakenHealth(double value) {
        krakenHealth = value;
    }

    public static void setKrakenAttackDamage(double value) {
        krakenAttackDamage = value;
    }

    public static void setBrutalflyHealth(double value) {
        brutalflyHealth = value;
    }

    public static void setBrutalflySwipeDamage(double value) {
        brutalflySwipeDamage = value;
    }

    public static void setBrutalflySpitDamage(double value) {
        brutalflySpitDamage = value;
    }

    public static void setMantisHealth(double value) {
        mantisHealth = value;
    }

    public static void setMantisAttackDamage(double value) {
        mantisAttackDamage = value;
    }

    public static void setMantisMovementSpeed(double value) {
        mantisMovementSpeed = value;
    }

    public static void setMantisFlyingSpeed(double value) {
        mantisFlyingSpeed = value;
    }

    public static void setKrakenProjectileDamageTakenMultiplier(double value) {
        krakenProjectileDamageTakenMultiplier = value;
    }

    public static void setKrakenSquidSpawnEnabled(boolean value) {
        krakenSquidSpawnEnabled = value;
    }

    public static void setKrakenMassSpawnEnabled(boolean value) {
        krakenMassSpawnEnabled = value;
    }

    public static void setSquidzookaCooldownSeconds(double value) {
        squidzookaCooldownSeconds = value;
    }

    public static void setSquidzookaLaunchVelocity(double value) {
        squidzookaLaunchVelocity = value;
    }

    public static void setSizeRayCooldownSeconds(double value) {
        sizeRayCooldownSeconds = value;
    }

    public static void setInvertProjectilesFromInvertedPlayers(boolean value) {
        invertProjectilesFromInvertedPlayers = value;
    }

    public static void setGravityGunEnabled(boolean value) {
        gravityGunEnabled = value;
    }

    public static void setGravityGunBlocksEnabled(boolean value) {
        gravityGunBlocksEnabled = value;
    }

    public static void setGravityGunEntitiesEnabled(boolean value) {
        gravityGunEntitiesEnabled = value;
    }

    public static void setGravityGunRange(double value) {
        gravityGunRange = value;
    }

    public static void setGravityGunThrowStrength(double value) {
        gravityGunThrowStrength = value;
    }

    public static void setGravityGunBlastStrength(double value) {
        gravityGunBlastStrength = value;
    }

    public static void setGravityGunCooldownSeconds(double value) {
        gravityGunCooldownSeconds = value;
    }

    public static void setGravityGunMaxHoldDistance(double value) {
        gravityGunMaxHoldDistance = value;
    }

    public static double lucidAttackRange() {
        return lucidAttackRange;
    }

    public static void setLucidAttackRange(double value) {
        lucidAttackRange = value;
    }

    public static double lucidPearlInvertedDurationSeconds() {
        return lucidPearlInvertedDurationSeconds;
    }

    public static void setLucidPearlInvertedDurationSeconds(double value) {
        lucidPearlInvertedDurationSeconds = value;
    }

    public static int corneaEarNightVisionSeconds() {
        return corneaEarNightVisionSeconds;
    }

    public static void setCorneaEarNightVisionSeconds(int value) {
        corneaEarNightVisionSeconds = value;
    }

    public static int mogglesVisionRadius() {
        return mogglesVisionRadius;
    }

    public static void setMogglesVisionRadius(int value) {
        mogglesVisionRadius = value;
    }

    public static int mogglesVisionMaxLight() {
        return mogglesVisionMaxLight;
    }

    public static void setMogglesVisionMaxLight(int value) {
        mogglesVisionMaxLight = value;
    }

    public static float mogglesVisionAlpha() {
        return mogglesVisionAlpha;
    }

    public static void setMogglesVisionAlpha(float value) {
        mogglesVisionAlpha = value;
    }

    public static boolean dreamSandEnabled() {
        return dreamSandEnabled;
    }

    public static boolean entitySpecificFireOverlayEnabled() {
        return entitySpecificFireOverlayEnabled;
    }

    public static void setDreamSandEnabled(boolean value) {
        dreamSandEnabled = value;
    }

    public static void setEntitySpecificFireOverlayEnabled(boolean value) {
        entitySpecificFireOverlayEnabled = value;
    }

    public static double dreamSandJumpVelocityMultiplier() {
        return dreamSandJumpVelocityMultiplier;
    }

    public static void setDreamSandJumpVelocityMultiplier(double value) {
        dreamSandJumpVelocityMultiplier = value;
    }

    public static double dreamSandGravityMultiplier() {
        return dreamSandGravityMultiplier;
    }

    public static void setDreamSandGravityMultiplier(double value) {
        dreamSandGravityMultiplier = value;
    }

    public static double dreamSandFallDamageMultiplier() {
        return dreamSandFallDamageMultiplier;
    }

    public static void setDreamSandFallDamageMultiplier(double value) {
        dreamSandFallDamageMultiplier = value;
    }

    public static double dreamSandEffectDurationSeconds() {
        return dreamSandEffectDurationSeconds;
    }

    public static void setDreamSandEffectDurationSeconds(double value) {
        dreamSandEffectDurationSeconds = value;
    }

    public static boolean ichorWitherEnabled() {
        return ichorWitherEnabled;
    }

    public static void setIchorWitherEnabled(boolean value) {
        ichorWitherEnabled = value;
    }

    public static int nightmareHelmetArmorValue() { return nightmareHelmetArmorValue; }
    public static void setNightmareHelmetArmorValue(int v) { nightmareHelmetArmorValue = v; }

    public static int nightmareChestplateArmorValue() { return nightmareChestplateArmorValue; }
    public static void setNightmareChestplateArmorValue(int v) { nightmareChestplateArmorValue = v; }

    public static int nightmareLeggingsArmorValue() { return nightmareLeggingsArmorValue; }
    public static void setNightmareLeggingsArmorValue(int v) { nightmareLeggingsArmorValue = v; }

    public static int nightmareBootsArmorValue() { return nightmareBootsArmorValue; }
    public static void setNightmareBootsArmorValue(int v) { nightmareBootsArmorValue = v; }

    public static double nightmareHelmetArmorToughness() { return nightmareHelmetArmorToughness; }
    public static void setNightmareHelmetArmorToughness(double v) { nightmareHelmetArmorToughness = v; }

    public static double nightmareChestplateArmorToughness() { return nightmareChestplateArmorToughness; }
    public static void setNightmareChestplateArmorToughness(double v) { nightmareChestplateArmorToughness = v; }

    public static double nightmareLeggingsArmorToughness() { return nightmareLeggingsArmorToughness; }
    public static void setNightmareLeggingsArmorToughness(double v) { nightmareLeggingsArmorToughness = v; }

    public static double nightmareBootsArmorToughness() { return nightmareBootsArmorToughness; }
    public static void setNightmareBootsArmorToughness(double v) { nightmareBootsArmorToughness = v; }

    public static double nightmareArmorDreadAuraRangePerPiece() { return nightmareArmorDreadAuraRangePerPiece; }
    public static void setNightmareArmorDreadAuraRangePerPiece(double v) { nightmareArmorDreadAuraRangePerPiece = v; }

    public static double nightmareHelmetDoubleDamageChance() { return nightmareHelmetDoubleDamageChance; }
    public static void setNightmareHelmetDoubleDamageChance(double v) { nightmareHelmetDoubleDamageChance = v; }

    public static double nightmareChestplateDoubleDamageChance() { return nightmareChestplateDoubleDamageChance; }
    public static void setNightmareChestplateDoubleDamageChance(double v) { nightmareChestplateDoubleDamageChance = v; }

    public static double nightmareLeggingsDoubleDamageChance() { return nightmareLeggingsDoubleDamageChance; }
    public static void setNightmareLeggingsDoubleDamageChance(double v) { nightmareLeggingsDoubleDamageChance = v; }

    public static double nightmareBootsDoubleDamageChance() { return nightmareBootsDoubleDamageChance; }
    public static void setNightmareBootsDoubleDamageChance(double v) { nightmareBootsDoubleDamageChance = v; }

    public static double nightmareSwordBaseDamage() { return nightmareSwordBaseDamage; }
    public static void setNightmareSwordBaseDamage(double v) { nightmareSwordBaseDamage = v; }

    public static double nightmareSwordScalingFactor() { return nightmareSwordScalingFactor; }
    public static void setNightmareSwordScalingFactor(double v) { nightmareSwordScalingFactor = v; }

    public static boolean basiliskPetrifyingGazeEnabled() {
        return basiliskPetrifyingGazeEnabled;
    }

    public static void setBasiliskPetrifyingGazeEnabled(boolean value) {
        basiliskPetrifyingGazeEnabled = value;
    }

    public static int basiliskSpawnMaxLightLevel() {
        return basiliskSpawnMaxLightLevel;
    }

    public static void setBasiliskSpawnMaxLightLevel(int value) {
        basiliskSpawnMaxLightLevel = value;
    }

    public static double basiliskHealth() { return basiliskHealth; }
    public static void setBasiliskHealth(double value) { basiliskHealth = value; }

    public static double basiliskAttackDamage() { return basiliskAttackDamage; }
    public static void setBasiliskAttackDamage(double value) { basiliskAttackDamage = value; }

    public static double basiliskMovementSpeed() { return basiliskMovementSpeed; }
    public static void setBasiliskMovementSpeed(double value) { basiliskMovementSpeed = value; }

    public static double basiliskArmor() { return basiliskArmor; }
    public static void setBasiliskArmor(double value) { basiliskArmor = value; }

    public static double basiliskKnockbackResistance() { return basiliskKnockbackResistance; }
    public static void setBasiliskKnockbackResistance(double value) { basiliskKnockbackResistance = value; }

    public static double basiliskFollowRange() { return basiliskFollowRange; }
    public static void setBasiliskFollowRange(double value) { basiliskFollowRange = value; }

    public static int basiliskXpReward() { return basiliskXpReward; }
    public static void setBasiliskXpReward(int value) { basiliskXpReward = value; }

    public static int basiliskAttackAnimTicks() { return basiliskAttackAnimTicks; }
    public static void setBasiliskAttackAnimTicks(int value) { basiliskAttackAnimTicks = value; }

    public static int basiliskAttackDamageTick() { return basiliskAttackDamageTick; }
    public static void setBasiliskAttackDamageTick(int value) { basiliskAttackDamageTick = value; }

    public static int basiliskHissCooldownTicks() { return basiliskHissCooldownTicks; }
    public static void setBasiliskHissCooldownTicks(int value) { basiliskHissCooldownTicks = value; }

    public static double basiliskAttackReach() { return basiliskAttackReach; }
    public static void setBasiliskAttackReach(double value) { basiliskAttackReach = value; }

    public static double basiliskGazeRange() { return basiliskGazeRange; }
    public static void setBasiliskGazeRange(double value) { basiliskGazeRange = value; }

    public static double basiliskGazeDotThreshold() { return basiliskGazeDotThreshold; }
    public static void setBasiliskGazeDotThreshold(double value) { basiliskGazeDotThreshold = value; }

    public static double basiliskGazeFacingThreshold() { return basiliskGazeFacingThreshold; }
    public static void setBasiliskGazeFacingThreshold(double value) { basiliskGazeFacingThreshold = value; }

    public static int basiliskHissChargeTicks() { return basiliskHissChargeTicks; }
    public static void setBasiliskHissChargeTicks(int value) { basiliskHissChargeTicks = value; }

    public static int basiliskPlayerParalyzeTicks() { return basiliskPlayerParalyzeTicks; }
    public static void setBasiliskPlayerParalyzeTicks(int value) { basiliskPlayerParalyzeTicks = value; }

    public static int basiliskPreyPetrifyCooldownTicks() { return basiliskPreyPetrifyCooldownTicks; }
    public static void setBasiliskPreyPetrifyCooldownTicks(int value) { basiliskPreyPetrifyCooldownTicks = value; }

    public static double basiliskPreyPetrifyRange() { return basiliskPreyPetrifyRange; }
    public static void setBasiliskPreyPetrifyRange(double value) { basiliskPreyPetrifyRange = value; }

    public static int basiliskPreyPetrifyTicks() { return basiliskPreyPetrifyTicks; }
    public static void setBasiliskPreyPetrifyTicks(int value) { basiliskPreyPetrifyTicks = value; }

    public static boolean diamondMinecartEnabled() { return diamondMinecartEnabled; }
    public static void setDiamondMinecartEnabled(boolean v) { diamondMinecartEnabled = v; }

    public static boolean diamondMinecartPlacesRails() { return diamondMinecartPlacesRails; }
    public static void setDiamondMinecartPlacesRails(boolean v) { diamondMinecartPlacesRails = v; }

    public static double diamondMinecartCruiseSpeed() { return diamondMinecartCruiseSpeed; }
    public static void setDiamondMinecartCruiseSpeed(double v) { diamondMinecartCruiseSpeed = v; }

    public static double diamondMinecartMaxSpeed() { return diamondMinecartMaxSpeed; }
    public static void setDiamondMinecartMaxSpeed(double v) { diamondMinecartMaxSpeed = v; }

    public static double diamondMinecartAcceleration() { return diamondMinecartAcceleration; }
    public static void setDiamondMinecartAcceleration(double v) { diamondMinecartAcceleration = v; }

    public static double diamondMinecartDeceleration() { return diamondMinecartDeceleration; }
    public static void setDiamondMinecartDeceleration(double v) { diamondMinecartDeceleration = v; }

    public static double diamondMinecartCoastDeceleration() { return diamondMinecartCoastDeceleration; }
    public static void setDiamondMinecartCoastDeceleration(double v) { diamondMinecartCoastDeceleration = v; }

    public static boolean diamondMinecartMobDamageEnabled() { return diamondMinecartMobDamageEnabled; }
    public static void setDiamondMinecartMobDamageEnabled(boolean v) { diamondMinecartMobDamageEnabled = v; }

    public static double diamondMinecartMaxMobDamage() { return diamondMinecartMaxMobDamage; }
    public static void setDiamondMinecartMaxMobDamage(double v) { diamondMinecartMaxMobDamage = v; }

    public static boolean dreadHallucinationSoundsEnabled() {
        return dreadHallucinationSoundsEnabled;
    }

    public static void setDreadHallucinationSoundsEnabled(boolean value) {
        dreadHallucinationSoundsEnabled = value;
    }

    public static double dreadHallucinationSoundMinInterval() {
        return dreadHallucinationSoundMinInterval;
    }

    public static void setDreadHallucinationSoundMinInterval(double value) {
        dreadHallucinationSoundMinInterval = value;
    }

    public static double dreadHallucinationSoundMaxInterval() {
        return dreadHallucinationSoundMaxInterval;
    }

    public static void setDreadHallucinationSoundMaxInterval(double value) {
        dreadHallucinationSoundMaxInterval = value;
    }

    public static boolean dreadHallucinationMobsEnabled() {
        return dreadHallucinationMobsEnabled;
    }

    public static void setDreadHallucinationMobsEnabled(boolean value) {
        dreadHallucinationMobsEnabled = value;
    }

    public static double dreadHallucinationMobMinInterval() {
        return dreadHallucinationMobMinInterval;
    }

    public static void setDreadHallucinationMobMinInterval(double value) {
        dreadHallucinationMobMinInterval = value;
    }

    public static double dreadHallucinationMobMaxInterval() {
        return dreadHallucinationMobMaxInterval;
    }

    public static void setDreadHallucinationMobMaxInterval(double value) {
        dreadHallucinationMobMaxInterval = value;
    }

    public static double brownAntMovementSpeed() { return brownAntMovementSpeed; }
    public static void setBrownAntMovementSpeed(double value) { brownAntMovementSpeed = value; }

    public static double redAntMovementSpeed() { return redAntMovementSpeed; }
    public static void setRedAntMovementSpeed(double value) { redAntMovementSpeed = value; }

    public static double rainbowAntMovementSpeed() { return rainbowAntMovementSpeed; }
    public static void setRainbowAntMovementSpeed(double value) { rainbowAntMovementSpeed = value; }

    public static double termiteMovementSpeed() { return termiteMovementSpeed; }
    public static void setTermiteMovementSpeed(double value) { termiteMovementSpeed = value; }

    public static double brownAntAttackDamage() { return brownAntAttackDamage; }
    public static void setBrownAntAttackDamage(double value) { brownAntAttackDamage = value; }

    public static double rainbowAntAttackDamage() { return rainbowAntAttackDamage; }
    public static void setRainbowAntAttackDamage(double value) { rainbowAntAttackDamage = value; }

    public static double termiteAttackDamage() { return termiteAttackDamage; }
    public static void setTermiteAttackDamage(double value) { termiteAttackDamage = value; }

    public static double flyingSquirrelHealth() { return flyingSquirrelHealth; }
    public static void setFlyingSquirrelHealth(double value) { flyingSquirrelHealth = value; }

    public static double flyingSquirrelMovementSpeed() { return flyingSquirrelMovementSpeed; }
    public static void setFlyingSquirrelMovementSpeed(double value) { flyingSquirrelMovementSpeed = value; }

    public static double flyingSquirrelFollowRange() { return flyingSquirrelFollowRange; }
    public static void setFlyingSquirrelFollowRange(double value) { flyingSquirrelFollowRange = value; }

    public static double caterpillarHealth() { return caterpillarHealth; }
    public static void setCaterpillarHealth(double value) { caterpillarHealth = value; }

    public static double caterpillarMovementSpeed() { return caterpillarMovementSpeed; }
    public static void setCaterpillarMovementSpeed(double value) { caterpillarMovementSpeed = value; }

    public static double caterpillarFollowRange() { return caterpillarFollowRange; }
    public static void setCaterpillarFollowRange(double value) { caterpillarFollowRange = value; }

    public static double caterpillarPupationTimeSeconds() { return caterpillarPupationTimeSeconds; }
    public static void setCaterpillarPupationTimeSeconds(double value) { caterpillarPupationTimeSeconds = value; }
    public static int caterpillarPupationTimeTicks() { return Math.max(1, (int)Math.round(caterpillarPupationTimeSeconds * 20.0D)); }

    private static double hushweedSporeLifetimeSeconds = 5.0D;
    public static double hushweedSporeLifetimeSeconds() { return hushweedSporeLifetimeSeconds; }
    public static void setHushweedSporeLifetimeSeconds(double value) { hushweedSporeLifetimeSeconds = value; }
    public static int hushweedSporeLifetimeTicks() { return Math.max(1, (int)Math.round(hushweedSporeLifetimeSeconds * 20.0D)); }

    public static double butterflyHealth() { return butterflyHealth; }
    public static void setButterflyHealth(double value) { butterflyHealth = value; }

    public static double butterflyMovementSpeed() { return butterflyMovementSpeed; }
    public static void setButterflyMovementSpeed(double value) { butterflyMovementSpeed = value; }

    public static double butterflyFlyingSpeed() { return butterflyFlyingSpeed; }
    public static void setButterflyFlyingSpeed(double value) { butterflyFlyingSpeed = value; }

    public static double butterflyFollowRange() { return butterflyFollowRange; }
    public static void setButterflyFollowRange(double value) { butterflyFollowRange = value; }

    public static double  reverieHealth()                      { return reverieHealth; }
    public static void    setReverieHealth(double value)       { reverieHealth = value; }

    public static double  reverieInterestRadius()              { return reverieInterestRadius; }
    public static void    setReverieInterestRadius(double v)   { reverieInterestRadius = v; }

    public static double  reverieAbandonPlayerDistance()           { return reverieAbandonPlayerDistance; }
    public static void    setReverieAbandonPlayerDistance(double v){ reverieAbandonPlayerDistance = v; }

    public static int     reverieNoticeDurationTicks()             { return reverieNoticeDurationTicks; }
    public static void    setReverieNoticeDurationTicks(int v)     { reverieNoticeDurationTicks = v; }

    public static int     reverieInterestDurationTicks()           { return reverieInterestDurationTicks; }
    public static void    setReverieInterestDurationTicks(int v)   { reverieInterestDurationTicks = v; }

    public static int     reverieRebindCooldownTicks()             { return reverieRebindCooldownTicks; }
    public static void    setReverieRebindCooldownTicks(int v)     { reverieRebindCooldownTicks = v; }

    public static int     reverieDamageReactionDurationTicks()     { return reverieDamageReactionDurationTicks; }
    public static void    setReverieDamageReactionDurationTicks(int v){ reverieDamageReactionDurationTicks = v; }

    public static double  reverieDangerousFallDistance()           { return reverieDangerousFallDistance; }
    public static void    setReverieDangerousFallDistance(double v){ reverieDangerousFallDistance = v; }

    public static double  reverieDangerousFallSpeed()              { return reverieDangerousFallSpeed; }
    public static void    setReverieDangerousFallSpeed(double v)   { reverieDangerousFallSpeed = v; }

    public static double  reverieWarningThreatRadius()             { return reverieWarningThreatRadius; }
    public static void    setReverieWarningThreatRadius(double v)  { reverieWarningThreatRadius = v; }

    public static double  reverieWarningThreatVerticalRange()          { return reverieWarningThreatVerticalRange; }
    public static void    setReverieWarningThreatVerticalRange(double v){ reverieWarningThreatVerticalRange = v; }

    public static int     reverieDuplicationCooldownTicks()        { return reverieDuplicationCooldownTicks; }
    public static void    setReverieDuplicationCooldownTicks(int v){ reverieDuplicationCooldownTicks = v; }

    // Hardcoded AI tuning — getters only, no config wiring
    public static boolean reverieOnlyPlayerDamage()              { return reverieOnlyPlayerDamage; }
    public static double  reveriePreferredFollowMinDistance()    { return reveriePreferredFollowMinDistance; }
    public static double  reveriePreferredFollowMaxDistance()    { return reveriePreferredFollowMaxDistance; }
    public static double  reverieCatchUpDistance()               { return reverieCatchUpDistance; }
    public static double  reveriePurpleProtectRadius()           { return reveriePurpleProtectRadius; }
    public static int     reverieWarningApproachTicks()          { return reverieWarningApproachTicks; }
    public static int     reverieWarningHoverTicks()             { return reverieWarningHoverTicks; }
    public static int     reverieWarningReturnTicks()            { return reverieWarningReturnTicks; }
    public static int     reverieWarningPlayerHoverTicks()       { return reverieWarningPlayerHoverTicks; }
    public static double  reverieAmbientTargetMinRadius()        { return reverieAmbientTargetMinRadius; }
    public static double  reverieAmbientTargetMaxRadius()        { return reverieAmbientTargetMaxRadius; }
    public static double  reverieAmbientTargetVerticalRange()    { return reverieAmbientTargetVerticalRange; }
    public static int     reverieAmbientTargetMaxAgeTicks()      { return reverieAmbientTargetMaxAgeTicks; }
    public static int     reverieAmbientPulseIntervalMinTicks()  { return reverieAmbientPulseIntervalMinTicks; }
    public static int     reverieAmbientPulseIntervalMaxTicks()  { return reverieAmbientPulseIntervalMaxTicks; }

    public static double emperorScorpionHealth() { return emperorScorpionHealth; }
    public static void setEmperorScorpionHealth(double value) { emperorScorpionHealth = value; }

    public static double scorpionWhipBaseDamage() { return scorpionWhipBaseDamage; }
    public static void setScorpionWhipBaseDamage(double value) { scorpionWhipBaseDamage = value; }

    public static double scorpionWhipReachBonus() { return scorpionWhipReachBonus; }
    public static void setScorpionWhipReachBonus(double value) { scorpionWhipReachBonus = value; }

    public static int scorpionWhipPoisonDurationTicks() { return scorpionWhipPoisonDurationTicks; }
    public static void setScorpionWhipPoisonDurationTicks(int value) { scorpionWhipPoisonDurationTicks = value; }

    public static double scorpionWhipTetherMaxRange() { return scorpionWhipTetherMaxRange; }
    public static void setScorpionWhipTetherMaxRange(double value) { scorpionWhipTetherMaxRange = value; }

    public static int scorpionWhipReelCooldownTicks() { return scorpionWhipReelCooldownTicks; }
    public static void setScorpionWhipReelCooldownTicks(int value) { scorpionWhipReelCooldownTicks = value; }

    public static double scorpionWhipSnapBonusDamage() { return scorpionWhipSnapBonusDamage; }
    public static void setScorpionWhipSnapBonusDamage(double value) { scorpionWhipSnapBonusDamage = value; }

    public static int scorpionWhipSnapCooldownTicks() { return scorpionWhipSnapCooldownTicks; }
    public static void setScorpionWhipSnapCooldownTicks(int value) { scorpionWhipSnapCooldownTicks = value; }

    public static double scorpionWhipPullStrength() { return scorpionWhipPullStrength; }
    public static void setScorpionWhipPullStrength(double value) { scorpionWhipPullStrength = value; }

    public static double scorpionWhipHeavyPullMultiplier() { return scorpionWhipHeavyPullMultiplier; }
    public static void setScorpionWhipHeavyPullMultiplier(double value) { scorpionWhipHeavyPullMultiplier = value; }

    public static double scorpionWhipSelfPullMultiplier() { return scorpionWhipSelfPullMultiplier; }
    public static void setScorpionWhipSelfPullMultiplier(double value) { scorpionWhipSelfPullMultiplier = value; }

    public static int bloodCrystalKatanaAttackDamage() { return bloodCrystalKatanaAttackDamage; }
    public static void setBloodCrystalKatanaAttackDamage(int value) { bloodCrystalKatanaAttackDamage = value; }

    public static double bloodCrystalKatanaLaunchStrength() { return bloodCrystalKatanaLaunchStrength; }
    public static void setBloodCrystalKatanaLaunchStrength(double value) { bloodCrystalKatanaLaunchStrength = value; }

    public static int bloodCrystalKatanaTrailDurationTicks() { return bloodCrystalKatanaTrailDurationTicks; }
    public static void setBloodCrystalKatanaTrailDurationTicks(int value) { bloodCrystalKatanaTrailDurationTicks = value; }

    public static double emperorScorpionAttackDamage() { return emperorScorpionAttackDamage; }
    public static void setEmperorScorpionAttackDamage(double value) { emperorScorpionAttackDamage = value; }

    public static double emperorScorpionMovementSpeed() { return emperorScorpionMovementSpeed; }
    public static void setEmperorScorpionMovementSpeed(double value) { emperorScorpionMovementSpeed = value; }

    public static double emperorScorpionArmor() { return emperorScorpionArmor; }
    public static void setEmperorScorpionArmor(double value) { emperorScorpionArmor = value; }

    public static double emperorScorpionKnockbackResistance() { return emperorScorpionKnockbackResistance; }
    public static void setEmperorScorpionKnockbackResistance(double value) { emperorScorpionKnockbackResistance = value; }

    public static double emperorScorpionFollowRange() { return emperorScorpionFollowRange; }
    public static void setEmperorScorpionFollowRange(double value) { emperorScorpionFollowRange = value; }

    public static int emperorScorpionXpReward() { return emperorScorpionXpReward; }
    public static void setEmperorScorpionXpReward(int value) { emperorScorpionXpReward = value; }

    public static int emperorScorpionClawAnimTicks() { return emperorScorpionClawAnimTicks; }
    public static void setEmperorScorpionClawAnimTicks(int value) { emperorScorpionClawAnimTicks = value; }

    public static int emperorScorpionClawHitTick() { return emperorScorpionClawHitTick; }
    public static void setEmperorScorpionClawHitTick(int value) { emperorScorpionClawHitTick = value; }

    public static int emperorScorpionClawCooldownTicks() { return emperorScorpionClawCooldownTicks; }
    public static void setEmperorScorpionClawCooldownTicks(int value) { emperorScorpionClawCooldownTicks = value; }

    public static int emperorScorpionStingAnimTicks() { return emperorScorpionStingAnimTicks; }
    public static void setEmperorScorpionStingAnimTicks(int value) { emperorScorpionStingAnimTicks = value; }

    public static int emperorScorpionStingHitTick() { return emperorScorpionStingHitTick; }
    public static void setEmperorScorpionStingHitTick(int value) { emperorScorpionStingHitTick = value; }

    public static int emperorScorpionStingCooldownTicks() { return emperorScorpionStingCooldownTicks; }
    public static void setEmperorScorpionStingCooldownTicks(int value) { emperorScorpionStingCooldownTicks = value; }

    public static int emperorScorpionPoisonTicks() { return emperorScorpionPoisonTicks; }
    public static void setEmperorScorpionPoisonTicks(int value) { emperorScorpionPoisonTicks = value; }

    public static int emperorScorpionWeaknessTicks() { return emperorScorpionWeaknessTicks; }
    public static void setEmperorScorpionWeaknessTicks(int value) { emperorScorpionWeaknessTicks = value; }

    public static int emperorScorpionSummonIntervalTicks() { return emperorScorpionSummonIntervalTicks; }
    public static void setEmperorScorpionSummonIntervalTicks(int value) { emperorScorpionSummonIntervalTicks = value; }

    public static int emperorScorpionMaxSummonedScorpions() { return emperorScorpionMaxSummonedScorpions; }
    public static void setEmperorScorpionMaxSummonedScorpions(int value) { emperorScorpionMaxSummonedScorpions = value; }

    public static double scorpionHealth() { return scorpionHealth; }
    public static void setScorpionHealth(double value) { scorpionHealth = value; }

    public static double scorpionAttackDamage() { return scorpionAttackDamage; }
    public static void setScorpionAttackDamage(double value) { scorpionAttackDamage = value; }

    public static double scorpionMovementSpeed() { return scorpionMovementSpeed; }
    public static void setScorpionMovementSpeed(double value) { scorpionMovementSpeed = value; }

    public static double scorpionArmor() { return scorpionArmor; }
    public static void setScorpionArmor(double value) { scorpionArmor = value; }

    public static double scorpionKnockbackResistance() { return scorpionKnockbackResistance; }
    public static void setScorpionKnockbackResistance(double value) { scorpionKnockbackResistance = value; }

    public static double scorpionFollowRange() { return scorpionFollowRange; }
    public static void setScorpionFollowRange(double value) { scorpionFollowRange = value; }

    public static int scorpionXpReward() { return scorpionXpReward; }
    public static void setScorpionXpReward(int value) { scorpionXpReward = value; }

    public static int scorpionAttackAnimTicks() { return scorpionAttackAnimTicks; }
    public static void setScorpionAttackAnimTicks(int value) { scorpionAttackAnimTicks = value; }

    public static int scorpionAttackHitTick() { return scorpionAttackHitTick; }
    public static void setScorpionAttackHitTick(int value) { scorpionAttackHitTick = value; }

    public static int scorpionAttackCooldownTicks() { return scorpionAttackCooldownTicks; }
    public static void setScorpionAttackCooldownTicks(int value) { scorpionAttackCooldownTicks = value; }

    public static int scorpionPoisonTicks() { return scorpionPoisonTicks; }
    public static void setScorpionPoisonTicks(int value) { scorpionPoisonTicks = value; }

    public static double bedBugHealth() { return bedBugHealth; }
    public static void setBedBugHealth(double value) { bedBugHealth = value; }

    public static double bedBugAttackDamage() { return bedBugAttackDamage; }
    public static void setBedBugAttackDamage(double value) { bedBugAttackDamage = value; }

    public static double bedBugMovementSpeed() { return bedBugMovementSpeed; }
    public static void setBedBugMovementSpeed(double value) { bedBugMovementSpeed = value; }

    public static double bedBugArmor() { return bedBugArmor; }
    public static void setBedBugArmor(double value) { bedBugArmor = value; }

    public static double bedBugFollowRange() { return bedBugFollowRange; }
    public static void setBedBugFollowRange(double value) { bedBugFollowRange = value; }

    public static int bedBugXpReward() { return bedBugXpReward; }
    public static void setBedBugXpReward(int value) { bedBugXpReward = value; }

    public static int bedBugBiteAnimTicks() { return bedBugBiteAnimTicks; }
    public static void setBedBugBiteAnimTicks(int value) { bedBugBiteAnimTicks = value; }

    public static int bedBugAttackHitTick() { return bedBugAttackHitTick; }
    public static void setBedBugAttackHitTick(int value) { bedBugAttackHitTick = value; }

    public static int bedBugAttackCooldownTicks() { return bedBugAttackCooldownTicks; }
    public static void setBedBugAttackCooldownTicks(int value) { bedBugAttackCooldownTicks = value; }

    public static int bedBugMinLayEggDelay() { return bedBugMinLayEggDelay; }
    public static void setBedBugMinLayEggDelay(int value) { bedBugMinLayEggDelay = value; }

    public static int bedBugMaxLayEggDelay() { return bedBugMaxLayEggDelay; }
    public static void setBedBugMaxLayEggDelay(int value) { bedBugMaxLayEggDelay = value; }

    public static double bedBugFoodSearchRadius() { return bedBugFoodSearchRadius; }
    public static void setBedBugFoodSearchRadius(double value) { bedBugFoodSearchRadius = value; }

    public static double bedBugEggGuardRadius() { return bedBugEggGuardRadius; }
    public static void setBedBugEggGuardRadius(double value) { bedBugEggGuardRadius = value; }

    public static double bedBugAttackStartReachBuffer() { return bedBugAttackStartReachBuffer; }
    public static void setBedBugAttackStartReachBuffer(double value) { bedBugAttackStartReachBuffer = value; }

    public static double bedBugAttackReachBuffer() { return bedBugAttackReachBuffer; }
    public static void setBedBugAttackReachBuffer(double value) { bedBugAttackReachBuffer = value; }

    public static double bedBugAttackLungeHorizontalSpeed() { return bedBugAttackLungeHorizontalSpeed; }
    public static void setBedBugAttackLungeHorizontalSpeed(double value) { bedBugAttackLungeHorizontalSpeed = value; }

    public static double bedBugAttackLungeVerticalSpeed() { return bedBugAttackLungeVerticalSpeed; }
    public static void setBedBugAttackLungeVerticalSpeed(double value) { bedBugAttackLungeVerticalSpeed = value; }

    public static float bedBugHealAmount() { return bedBugHealAmount; }
    public static void setBedBugHealAmount(float value) { bedBugHealAmount = value; }

    public static double jumpyBugHealth() { return jumpyBugHealth; }
    public static void setJumpyBugHealth(double value) { jumpyBugHealth = value; }

    public static double jumpyBugPounceDamage() { return jumpyBugPounceDamage; }
    public static void setJumpyBugPounceDamage(double value) { jumpyBugPounceDamage = value; }

    public static double jumpyBugLatchDamage() { return jumpyBugLatchDamage; }
    public static void setJumpyBugLatchDamage(double value) { jumpyBugLatchDamage = value; }

    public static double jumpyBugCamouflageAlpha() { return jumpyBugCamouflageAlpha; }
    public static void setJumpyBugCamouflageAlpha(double value) { jumpyBugCamouflageAlpha = value; }

    public static double brutalflyArmor() { return brutalflyArmor; }
    public static void setBrutalflyArmor(double value) { brutalflyArmor = value; }

    public static double brutalflyArmorToughness() { return brutalflyArmorToughness; }
    public static void setBrutalflyArmorToughness(double value) { brutalflyArmorToughness = value; }

    public static double brutalflyKnockbackResistance() { return brutalflyKnockbackResistance; }
    public static void setBrutalflyKnockbackResistance(double value) { brutalflyKnockbackResistance = value; }

    public static double brutalflyMovementSpeed() { return brutalflyMovementSpeed; }
    public static void setBrutalflyMovementSpeed(double value) { brutalflyMovementSpeed = value; }

    public static double brutalflyFlyingSpeed() { return brutalflyFlyingSpeed; }
    public static void setBrutalflyFlyingSpeed(double value) { brutalflyFlyingSpeed = value; }

    public static double brutalflyFollowRange() { return brutalflyFollowRange; }
    public static void setBrutalflyFollowRange(double value) { brutalflyFollowRange = value; }

    public static int brutalflyXpReward() { return brutalflyXpReward; }
    public static void setBrutalflyXpReward(int value) { brutalflyXpReward = value; }

    public static int brutalflyDeathSpawnCountMin() { return brutalflyDeathSpawnCountMin; }
    public static void setBrutalflyDeathSpawnCountMin(int value) { brutalflyDeathSpawnCountMin = value; }

    public static int brutalflyDeathSpawnCountMax() { return brutalflyDeathSpawnCountMax; }
    public static void setBrutalflyDeathSpawnCountMax(int value) { brutalflyDeathSpawnCountMax = value; }

    public static int brutalflySpitWindupTicks() { return brutalflySpitWindupTicks; }
    public static void setBrutalflySpitWindupTicks(int value) { brutalflySpitWindupTicks = value; }

    public static int brutalflySwipeTicks() { return brutalflySwipeTicks; }
    public static void setBrutalflySwipeTicks(int value) { brutalflySwipeTicks = value; }

    public static float brutalflySwipeKnockback() { return brutalflySwipeKnockback; }
    public static void setBrutalflySwipeKnockback(float value) { brutalflySwipeKnockback = value; }

    public static int brutalflyPhaseOneSpitCooldownMin() { return brutalflyPhaseOneSpitCooldownMin; }
    public static void setBrutalflyPhaseOneSpitCooldownMin(int value) { brutalflyPhaseOneSpitCooldownMin = value; }

    public static int brutalflyPhaseOneSpitCooldownMax() { return brutalflyPhaseOneSpitCooldownMax; }
    public static void setBrutalflyPhaseOneSpitCooldownMax(int value) { brutalflyPhaseOneSpitCooldownMax = value; }

    public static int brutalflyPhaseTwoSpitCooldownMin() { return brutalflyPhaseTwoSpitCooldownMin; }
    public static void setBrutalflyPhaseTwoSpitCooldownMin(int value) { brutalflyPhaseTwoSpitCooldownMin = value; }

    public static int brutalflyPhaseTwoSpitCooldownMax() { return brutalflyPhaseTwoSpitCooldownMax; }
    public static void setBrutalflyPhaseTwoSpitCooldownMax(int value) { brutalflyPhaseTwoSpitCooldownMax = value; }

    public static int brutalflyPhaseThreeSpitCooldownMin() { return brutalflyPhaseThreeSpitCooldownMin; }
    public static void setBrutalflyPhaseThreeSpitCooldownMin(int value) { brutalflyPhaseThreeSpitCooldownMin = value; }

    public static int brutalflyPhaseThreeSpitCooldownMax() { return brutalflyPhaseThreeSpitCooldownMax; }
    public static void setBrutalflyPhaseThreeSpitCooldownMax(int value) { brutalflyPhaseThreeSpitCooldownMax = value; }

    public static int brutalflyPhaseOneMeleeCooldownMin() { return brutalflyPhaseOneMeleeCooldownMin; }
    public static void setBrutalflyPhaseOneMeleeCooldownMin(int value) { brutalflyPhaseOneMeleeCooldownMin = value; }

    public static int brutalflyPhaseOneMeleeCooldownMax() { return brutalflyPhaseOneMeleeCooldownMax; }
    public static void setBrutalflyPhaseOneMeleeCooldownMax(int value) { brutalflyPhaseOneMeleeCooldownMax = value; }

    public static int brutalflyPhaseTwoMeleeCooldownMin() { return brutalflyPhaseTwoMeleeCooldownMin; }
    public static void setBrutalflyPhaseTwoMeleeCooldownMin(int value) { brutalflyPhaseTwoMeleeCooldownMin = value; }

    public static int brutalflyPhaseTwoMeleeCooldownMax() { return brutalflyPhaseTwoMeleeCooldownMax; }
    public static void setBrutalflyPhaseTwoMeleeCooldownMax(int value) { brutalflyPhaseTwoMeleeCooldownMax = value; }

    public static int brutalflyPhaseThreeMeleeCooldownMin() { return brutalflyPhaseThreeMeleeCooldownMin; }
    public static void setBrutalflyPhaseThreeMeleeCooldownMin(int value) { brutalflyPhaseThreeMeleeCooldownMin = value; }

    public static int brutalflyPhaseThreeMeleeCooldownMax() { return brutalflyPhaseThreeMeleeCooldownMax; }
    public static void setBrutalflyPhaseThreeMeleeCooldownMax(int value) { brutalflyPhaseThreeMeleeCooldownMax = value; }

    public static double cloudSharkHealth() { return cloudSharkHealth; }
    public static void setCloudSharkHealth(double value) { cloudSharkHealth = value; }

    public static double cloudSharkAttackDamage() { return cloudSharkAttackDamage; }
    public static void setCloudSharkAttackDamage(double value) { cloudSharkAttackDamage = value; }

    public static double cloudSharkMovementSpeed() { return cloudSharkMovementSpeed; }
    public static void setCloudSharkMovementSpeed(double value) { cloudSharkMovementSpeed = value; }

    public static double cloudSharkFlyingSpeed() { return cloudSharkFlyingSpeed; }
    public static void setCloudSharkFlyingSpeed(double value) { cloudSharkFlyingSpeed = value; }

    public static double cloudSharkFollowRange() { return cloudSharkFollowRange; }
    public static void setCloudSharkFollowRange(double value) { cloudSharkFollowRange = value; }

    public static double cloudSharkKnockbackResistance() { return cloudSharkKnockbackResistance; }
    public static void setCloudSharkKnockbackResistance(double value) { cloudSharkKnockbackResistance = value; }

    public static int cloudSharkXpReward() { return cloudSharkXpReward; }
    public static void setCloudSharkXpReward(int value) { cloudSharkXpReward = value; }

    public static double krakenMovementSpeed() { return krakenMovementSpeed; }
    public static void setKrakenMovementSpeed(double value) { krakenMovementSpeed = value; }

    public static double krakenFlyingSpeed() { return krakenFlyingSpeed; }
    public static void setKrakenFlyingSpeed(double value) { krakenFlyingSpeed = value; }

    public static double krakenFollowRange() { return krakenFollowRange; }
    public static void setKrakenFollowRange(double value) { krakenFollowRange = value; }

    public static double krakenKnockbackResistance() { return krakenKnockbackResistance; }
    public static void setKrakenKnockbackResistance(double value) { krakenKnockbackResistance = value; }

    public static double krakenArmor() { return krakenArmor; }
    public static void setKrakenArmor(double value) { krakenArmor = value; }

    public static int krakenXpReward() { return krakenXpReward; }
    public static void setKrakenXpReward(int value) { krakenXpReward = value; }

    public static double krakenPhaseTwoSpeedBonus() { return krakenPhaseTwoSpeedBonus; }
    public static void setKrakenPhaseTwoSpeedBonus(double value) { krakenPhaseTwoSpeedBonus = value; }

    public static double krakenPhaseTwoFlyingSpeedBonus() { return krakenPhaseTwoFlyingSpeedBonus; }
    public static void setKrakenPhaseTwoFlyingSpeedBonus(double value) { krakenPhaseTwoFlyingSpeedBonus = value; }

    public static double krakenPhaseTwoDamageBonus() { return krakenPhaseTwoDamageBonus; }
    public static void setKrakenPhaseTwoDamageBonus(double value) { krakenPhaseTwoDamageBonus = value; }

    public static double krakenBossBarRange() { return krakenBossBarRange; }
    public static void setKrakenBossBarRange(double value) { krakenBossBarRange = value; }

    public static float krakenLightningDamagePhaseOne() { return krakenLightningDamagePhaseOne; }
    public static void setKrakenLightningDamagePhaseOne(float value) { krakenLightningDamagePhaseOne = value; }

    public static float krakenLightningDamagePhaseTwo() { return krakenLightningDamagePhaseTwo; }
    public static void setKrakenLightningDamagePhaseTwo(float value) { krakenLightningDamagePhaseTwo = value; }

    public static int krakenLightningAmbientCooldownPhaseOneMin() { return krakenLightningAmbientCooldownPhaseOneMin; }
    public static void setKrakenLightningAmbientCooldownPhaseOneMin(int value) { krakenLightningAmbientCooldownPhaseOneMin = value; }

    public static int krakenLightningAmbientCooldownPhaseOneMax() { return krakenLightningAmbientCooldownPhaseOneMax; }
    public static void setKrakenLightningAmbientCooldownPhaseOneMax(int value) { krakenLightningAmbientCooldownPhaseOneMax = value; }

    public static int krakenLightningAmbientCooldownPhaseTwoMin() { return krakenLightningAmbientCooldownPhaseTwoMin; }
    public static void setKrakenLightningAmbientCooldownPhaseTwoMin(int value) { krakenLightningAmbientCooldownPhaseTwoMin = value; }

    public static int krakenLightningAmbientCooldownPhaseTwoMax() { return krakenLightningAmbientCooldownPhaseTwoMax; }
    public static void setKrakenLightningAmbientCooldownPhaseTwoMax(int value) { krakenLightningAmbientCooldownPhaseTwoMax = value; }

    public static int krakenLightningStrikeCooldownPhaseOneMin() { return krakenLightningStrikeCooldownPhaseOneMin; }
    public static void setKrakenLightningStrikeCooldownPhaseOneMin(int value) { krakenLightningStrikeCooldownPhaseOneMin = value; }

    public static int krakenLightningStrikeCooldownPhaseOneMax() { return krakenLightningStrikeCooldownPhaseOneMax; }
    public static void setKrakenLightningStrikeCooldownPhaseOneMax(int value) { krakenLightningStrikeCooldownPhaseOneMax = value; }

    public static int krakenLightningStrikeCooldownPhaseTwoMin() { return krakenLightningStrikeCooldownPhaseTwoMin; }
    public static void setKrakenLightningStrikeCooldownPhaseTwoMin(int value) { krakenLightningStrikeCooldownPhaseTwoMin = value; }

    public static int krakenLightningStrikeCooldownPhaseTwoMax() { return krakenLightningStrikeCooldownPhaseTwoMax; }
    public static void setKrakenLightningStrikeCooldownPhaseTwoMax(int value) { krakenLightningStrikeCooldownPhaseTwoMax = value; }

    public static int krakenPhaseSummonCooldownPhaseOneMin() { return krakenPhaseSummonCooldownPhaseOneMin; }
    public static void setKrakenPhaseSummonCooldownPhaseOneMin(int value) { krakenPhaseSummonCooldownPhaseOneMin = value; }

    public static int krakenPhaseSummonCooldownPhaseOneMax() { return krakenPhaseSummonCooldownPhaseOneMax; }
    public static void setKrakenPhaseSummonCooldownPhaseOneMax(int value) { krakenPhaseSummonCooldownPhaseOneMax = value; }

    public static int krakenPhaseSummonCooldownPhaseTwoMin() { return krakenPhaseSummonCooldownPhaseTwoMin; }
    public static void setKrakenPhaseSummonCooldownPhaseTwoMin(int value) { krakenPhaseSummonCooldownPhaseTwoMin = value; }

    public static int krakenPhaseSummonCooldownPhaseTwoMax() { return krakenPhaseSummonCooldownPhaseTwoMax; }
    public static void setKrakenPhaseSummonCooldownPhaseTwoMax(int value) { krakenPhaseSummonCooldownPhaseTwoMax = value; }

    public static int krakenPhaseSummonedMinionsMin() { return krakenPhaseSummonedMinionsMin; }
    public static void setKrakenPhaseSummonedMinionsMin(int value) { krakenPhaseSummonedMinionsMin = value; }

    public static int krakenPhaseSummonedMinionsMax() { return krakenPhaseSummonedMinionsMax; }
    public static void setKrakenPhaseSummonedMinionsMax(int value) { krakenPhaseSummonedMinionsMax = value; }

    public static int krakenPhaseTwoMinionsNearbyCap() { return krakenPhaseTwoMinionsNearbyCap; }
    public static void setKrakenPhaseTwoMinionsNearbyCap(int value) { krakenPhaseTwoMinionsNearbyCap = value; }

    public static int krakenPhaseTwoMinionSpawnAttempts() { return krakenPhaseTwoMinionSpawnAttempts; }
    public static void setKrakenPhaseTwoMinionSpawnAttempts(int value) { krakenPhaseTwoMinionSpawnAttempts = value; }

    public static double lucidHealth() { return lucidHealth; }
    public static void setLucidHealth(double value) { lucidHealth = value; }

    public static double lucidAttackDamage() { return lucidAttackDamage; }
    public static void setLucidAttackDamage(double value) { lucidAttackDamage = value; }

    public static double lucidMovementSpeed() { return lucidMovementSpeed; }
    public static void setLucidMovementSpeed(double value) { lucidMovementSpeed = value; }

    public static double lucidFlyingSpeed() { return lucidFlyingSpeed; }
    public static void setLucidFlyingSpeed(double value) { lucidFlyingSpeed = value; }

    public static double lucidFollowRange() { return lucidFollowRange; }
    public static void setLucidFollowRange(double value) { lucidFollowRange = value; }

    public static double lucidKnockbackResistance() { return lucidKnockbackResistance; }
    public static void setLucidKnockbackResistance(double value) { lucidKnockbackResistance = value; }

    public static int lucidXpReward() { return lucidXpReward; }
    public static void setLucidXpReward(int value) { lucidXpReward = value; }

    public static int lucidAttackWindupTicks() { return lucidAttackWindupTicks; }
    public static void setLucidAttackWindupTicks(int value) { lucidAttackWindupTicks = value; }

    public static int lucidAttackRecoveryTicks() { return lucidAttackRecoveryTicks; }
    public static void setLucidAttackRecoveryTicks(int value) { lucidAttackRecoveryTicks = value; }

    public static int lucidBurstShots() { return lucidBurstShots; }
    public static void setLucidBurstShots(int value) { lucidBurstShots = value; }

    public static int lucidBurstIntervalTicks() { return lucidBurstIntervalTicks; }
    public static void setLucidBurstIntervalTicks(int value) { lucidBurstIntervalTicks = value; }

    public static int lucidAttackCooldownTicks() { return lucidAttackCooldownTicks; }
    public static void setLucidAttackCooldownTicks(int value) { lucidAttackCooldownTicks = value; }

    public static double lucidMinEffectiveAttackRange() { return lucidMinEffectiveAttackRange; }
    public static void setLucidMinEffectiveAttackRange(double value) { lucidMinEffectiveAttackRange = value; }

    public static double lucidMinHoverRange() { return lucidMinHoverRange; }
    public static void setLucidMinHoverRange(double value) { lucidMinHoverRange = value; }

    public static double lucidCloseRange() { return lucidCloseRange; }
    public static void setLucidCloseRange(double value) { lucidCloseRange = value; }

    public static double lucidDefaultHoverHeight() { return lucidDefaultHoverHeight; }
    public static void setLucidDefaultHoverHeight(double value) { lucidDefaultHoverHeight = value; }

    public static double lucidHitboxWidthScale() { return lucidHitboxWidthScale; }
    public static void setLucidHitboxWidthScale(double value) { lucidHitboxWidthScale = value; }

    public static double lucidHitboxHeightScale() { return lucidHitboxHeightScale; }
    public static void setLucidHitboxHeightScale(double value) { lucidHitboxHeightScale = value; }

    public static double lucidHitboxYOffset() { return lucidHitboxYOffset; }
    public static void setLucidHitboxYOffset(double value) { lucidHitboxYOffset = value; }

    public static double missileSquidHealth() { return missileSquidHealth; }
    public static void setMissileSquidHealth(double value) { missileSquidHealth = value; }

    public static double missileSquidAttackDamage() { return missileSquidAttackDamage; }
    public static void setMissileSquidAttackDamage(double value) { missileSquidAttackDamage = value; }

    public static double missileSquidMovementSpeed() { return missileSquidMovementSpeed; }
    public static void setMissileSquidMovementSpeed(double value) { missileSquidMovementSpeed = value; }

    public static double missileSquidFlyingSpeed() { return missileSquidFlyingSpeed; }
    public static void setMissileSquidFlyingSpeed(double value) { missileSquidFlyingSpeed = value; }

    public static double missileSquidFollowRange() { return missileSquidFollowRange; }
    public static void setMissileSquidFollowRange(double value) { missileSquidFollowRange = value; }

    public static double missileSquidKnockbackResistance() { return missileSquidKnockbackResistance; }
    public static void setMissileSquidKnockbackResistance(double value) { missileSquidKnockbackResistance = value; }

    public static int missileSquidXpReward() { return missileSquidXpReward; }
    public static void setMissileSquidXpReward(int value) { missileSquidXpReward = value; }

    public static int missileSquidLatchDamageInterval() { return missileSquidLatchDamageInterval; }
    public static void setMissileSquidLatchDamageInterval(int value) { missileSquidLatchDamageInterval = value; }

    public static float missileSquidLatchDamage() { return missileSquidLatchDamage; }
    public static void setMissileSquidLatchDamage(float value) { missileSquidLatchDamage = value; }

    public static double missileSquidAttachRangeSqr() { return missileSquidAttachRangeSqr; }
    public static void setMissileSquidAttachRangeSqr(double value) { missileSquidAttachRangeSqr = value; }

    public static double molewormHealth() { return molewormHealth; }
    public static void setMolewormHealth(double value) { molewormHealth = value; }

    public static double molewormAttackDamage() { return molewormAttackDamage; }
    public static void setMolewormAttackDamage(double value) { molewormAttackDamage = value; }

    public static double molewormMovementSpeed() { return molewormMovementSpeed; }
    public static void setMolewormMovementSpeed(double value) { molewormMovementSpeed = value; }

    public static int molewormXpReward() { return molewormXpReward; }
    public static void setMolewormXpReward(int value) { molewormXpReward = value; }

    public static double triffidHealth() { return triffidHealth; }
    public static void setTriffidHealth(double value) { triffidHealth = value; }

    public static double triffidAttackDamage() { return triffidAttackDamage; }
    public static void setTriffidAttackDamage(double value) { triffidAttackDamage = value; }

    public static double triffidGrabDamage() { return triffidGrabDamage; }
    public static void setTriffidGrabDamage(double value) { triffidGrabDamage = value; }

    public static double molevoreHealth() { return molevoreHealth; }
    public static void setMolevoreHealth(double value) { molevoreHealth = value; }

    public static double molevoreAttackDamage() { return molevoreAttackDamage; }
    public static void setMolevoreAttackDamage(double value) { molevoreAttackDamage = value; }

    public static double molevoreMovementSpeed() { return molevoreMovementSpeed; }
    public static void setMolevoreMovementSpeed(double value) { molevoreMovementSpeed = value; }

    public static double molevoreArmor() { return molevoreArmor; }
    public static void setMolevoreArmor(double value) { molevoreArmor = value; }

    public static double molevoreKnockbackResistance() { return molevoreKnockbackResistance; }
    public static void setMolevoreKnockbackResistance(double value) { molevoreKnockbackResistance = value; }

    public static double molevoreFollowRange() { return molevoreFollowRange; }
    public static void setMolevoreFollowRange(double value) { molevoreFollowRange = value; }

    public static int molevoreXpReward() { return molevoreXpReward; }
    public static void setMolevoreXpReward(int value) { molevoreXpReward = value; }

    public static int molevoreSpinTicks() { return molevoreSpinTicks; }
    public static void setMolevoreSpinTicks(int value) { molevoreSpinTicks = value; }

    public static int molevoreCooldownTicks() { return molevoreCooldownTicks; }
    public static void setMolevoreCooldownTicks(int value) { molevoreCooldownTicks = value; }

    public static double molevoreChargeSpeed() { return molevoreChargeSpeed; }
    public static void setMolevoreChargeSpeed(double value) { molevoreChargeSpeed = value; }

    public static double molevoreBreakRange() { return molevoreBreakRange; }
    public static void setMolevoreBreakRange(double value) { molevoreBreakRange = value; }

    public static double molevoreBreakVerticalRange() { return molevoreBreakVerticalRange; }
    public static void setMolevoreBreakVerticalRange(double value) { molevoreBreakVerticalRange = value; }

    public static double molevoreBreakHalfWidth() { return molevoreBreakHalfWidth; }
    public static void setMolevoreBreakHalfWidth(double value) { molevoreBreakHalfWidth = value; }

    public static double nightmareHealth() { return nightmareHealth; }
    public static void setNightmareHealth(double value) { nightmareHealth = value; }

    public static double nightmareAttackDamage() { return nightmareAttackDamage; }
    public static void setNightmareAttackDamage(double value) { nightmareAttackDamage = value; }

    public static double nightmareMovementSpeed() { return nightmareMovementSpeed; }
    public static void setNightmareMovementSpeed(double value) { nightmareMovementSpeed = value; }

    public static double nightmareFlyingSpeed() { return nightmareFlyingSpeed; }
    public static void setNightmareFlyingSpeed(double value) { nightmareFlyingSpeed = value; }

    public static double nightmareFollowRange() { return nightmareFollowRange; }
    public static void setNightmareFollowRange(double value) { nightmareFollowRange = value; }

    public static double nightmareKnockbackResistance() { return nightmareKnockbackResistance; }
    public static void setNightmareKnockbackResistance(double value) { nightmareKnockbackResistance = value; }

    public static double nightmareArmor() { return nightmareArmor; }
    public static void setNightmareArmor(double value) { nightmareArmor = value; }

    public static int nightmareXpReward() { return nightmareXpReward; }
    public static void setNightmareXpReward(int value) { nightmareXpReward = value; }

    public static int nightmareAttackTotalTicks() { return nightmareAttackTotalTicks; }
    public static void setNightmareAttackTotalTicks(int value) { nightmareAttackTotalTicks = value; }

    public static int nightmareAttackDamageTick() { return nightmareAttackDamageTick; }
    public static void setNightmareAttackDamageTick(int value) { nightmareAttackDamageTick = value; }

    public static int nightmareIntroRoarTicks() { return nightmareIntroRoarTicks; }
    public static void setNightmareIntroRoarTicks(int value) { nightmareIntroRoarTicks = value; }

    public static int nightmareCombatRoarTicks() { return nightmareCombatRoarTicks; }
    public static void setNightmareCombatRoarTicks(int value) { nightmareCombatRoarTicks = value; }

    public static int nightmareDeathTicks() { return nightmareDeathTicks; }
    public static void setNightmareDeathTicks(int value) { nightmareDeathTicks = value; }

    public static int nightmareTargetResetTicks() { return nightmareTargetResetTicks; }
    public static void setNightmareTargetResetTicks(int value) { nightmareTargetResetTicks = value; }

    public static int nightmareBedBugEggSearchInterval() { return nightmareBedBugEggSearchInterval; }
    public static void setNightmareBedBugEggSearchInterval(int value) { nightmareBedBugEggSearchInterval = value; }

    public static int nightmareDreadTicks() { return nightmareDreadTicks; }
    public static void setNightmareDreadTicks(int value) { nightmareDreadTicks = value; }

    public static int nightmareWeaknessTicks() { return nightmareWeaknessTicks; }
    public static void setNightmareWeaknessTicks(int value) { nightmareWeaknessTicks = value; }

    public static double nightmarePatrolSpeed() { return nightmarePatrolSpeed; }
    public static void setNightmarePatrolSpeed(double value) { nightmarePatrolSpeed = value; }

    public static double nightmareCombatFlightSpeed() { return nightmareCombatFlightSpeed; }
    public static void setNightmareCombatFlightSpeed(double value) { nightmareCombatFlightSpeed = value; }

    public static double nightmareGroundApproachSpeed() { return nightmareGroundApproachSpeed; }
    public static void setNightmareGroundApproachSpeed(double value) { nightmareGroundApproachSpeed = value; }

    public static double nightmareAttackStartRangeSqr() { return nightmareAttackStartRangeSqr; }
    public static void setNightmareAttackStartRangeSqr(double value) { nightmareAttackStartRangeSqr = value; }

    public static double nightmareGroundApproachRangeSqr() { return nightmareGroundApproachRangeSqr; }
    public static void setNightmareGroundApproachRangeSqr(double value) { nightmareGroundApproachRangeSqr = value; }

    public static double nightmareAttackReachRadius() { return nightmareAttackReachRadius; }
    public static void setNightmareAttackReachRadius(double value) { nightmareAttackReachRadius = value; }

    public static double nightmareAttackCommitHorizontalRange() { return nightmareAttackCommitHorizontalRange; }
    public static void setNightmareAttackCommitHorizontalRange(double value) { nightmareAttackCommitHorizontalRange = value; }

    public static double nightmareAttackCommitVerticalRange() { return nightmareAttackCommitVerticalRange; }
    public static void setNightmareAttackCommitVerticalRange(double value) { nightmareAttackCommitVerticalRange = value; }

    public static double nightmareFlightReengageRangeSqr() { return nightmareFlightReengageRangeSqr; }
    public static void setNightmareFlightReengageRangeSqr(double value) { nightmareFlightReengageRangeSqr = value; }

    public static double nightmareCloseApproachRangeSqr() { return nightmareCloseApproachRangeSqr; }
    public static void setNightmareCloseApproachRangeSqr(double value) { nightmareCloseApproachRangeSqr = value; }

    public static double nightmareRoarRetryDistanceSqr() { return nightmareRoarRetryDistanceSqr; }
    public static void setNightmareRoarRetryDistanceSqr(double value) { nightmareRoarRetryDistanceSqr = value; }

    public static double nightmareBedBugEggBreakRangeSqr() { return nightmareBedBugEggBreakRangeSqr; }
    public static void setNightmareBedBugEggBreakRangeSqr(double value) { nightmareBedBugEggBreakRangeSqr = value; }

    public static float nightmareAirPatrolChance() { return nightmareAirPatrolChance; }
    public static void setNightmareAirPatrolChance(float value) { nightmareAirPatrolChance = value; }

    public static double battleAxeAttackSpeed() { return battleAxeAttackSpeed; }
    public static void setBattleAxeAttackSpeed(double value) { battleAxeAttackSpeed = value; }

    public static double basiliskDaggerAttackDamage() { return basiliskDaggerAttackDamage; }
    public static void setBasiliskDaggerAttackDamage(double value) { basiliskDaggerAttackDamage = value; }

    public static double basiliskDaggerAttackSpeed() { return basiliskDaggerAttackSpeed; }
    public static void setBasiliskDaggerAttackSpeed(double value) { basiliskDaggerAttackSpeed = value; }

    public static int basiliskDaggerPoisonDurationTicks() { return basiliskDaggerPoisonDurationTicks; }
    public static void setBasiliskDaggerPoisonDurationTicks(int value) { basiliskDaggerPoisonDurationTicks = value; }

    public static int basiliskDaggerPoisonAmplifier() { return basiliskDaggerPoisonAmplifier; }
    public static void setBasiliskDaggerPoisonAmplifier(int value) { basiliskDaggerPoisonAmplifier = value; }

    public static double ultimateSwordAttackSpeed() { return ultimateSwordAttackSpeed; }
    public static void setUltimateSwordAttackSpeed(double value) { ultimateSwordAttackSpeed = value; }

    public static double ultimatePickaxeAttackSpeed() { return ultimatePickaxeAttackSpeed; }
    public static void setUltimatePickaxeAttackSpeed(double value) { ultimatePickaxeAttackSpeed = value; }

    public static double ultimateAxeAttackSpeed() { return ultimateAxeAttackSpeed; }
    public static void setUltimateAxeAttackSpeed(double value) { ultimateAxeAttackSpeed = value; }

    public static double ultimateShovelAttackSpeed() { return ultimateShovelAttackSpeed; }
    public static void setUltimateShovelAttackSpeed(double value) { ultimateShovelAttackSpeed = value; }

    public static double ultimateHoeAttackSpeed() { return ultimateHoeAttackSpeed; }
    public static void setUltimateHoeAttackSpeed(double value) { ultimateHoeAttackSpeed = value; }

    public static double ultimateMaceAttackSpeed() { return ultimateMaceAttackSpeed; }
    public static void setUltimateMaceAttackSpeed(double value) { ultimateMaceAttackSpeed = value; }

    public static double nightmareSwordAttackSpeed() { return nightmareSwordAttackSpeed; }
    public static void setNightmareSwordAttackSpeed(double value) { nightmareSwordAttackSpeed = value; }

    public static int bigBerthaBasiliskParalyzeDurationTicks() { return bigBerthaBasiliskParalyzeDurationTicks; }
    public static void setBigBerthaBasiliskParalyzeDurationTicks(int value) { bigBerthaBasiliskParalyzeDurationTicks = value; }

    public static int bigBerthaKrakenSlowTicks() { return bigBerthaKrakenSlowTicks; }
    public static void setBigBerthaKrakenSlowTicks(int value) { bigBerthaKrakenSlowTicks = value; }

    public static double ductTapeRepairPercentPerUse() { return ductTapeRepairPercentPerUse; }
    public static void setDuctTapeRepairPercentPerUse(double value) { ductTapeRepairPercentPerUse = value; }

    public static double ultimateArmorKnockbackResistance() { return ultimateArmorKnockbackResistance; }
    public static void setUltimateArmorKnockbackResistance(double value) { ultimateArmorKnockbackResistance = value; }

    public static double nightmareArmorKnockbackResistance() { return nightmareArmorKnockbackResistance; }
    public static void setNightmareArmorKnockbackResistance(double value) { nightmareArmorKnockbackResistance = value; }

    public static double primordialArmorKnockbackPerPiece() { return primordialArmorKnockbackPerPiece; }
    public static void setPrimordialArmorKnockbackPerPiece(double value) { primordialArmorKnockbackPerPiece = value; }

    public static int fallenKingCrownArmorValue() { return fallenKingCrownArmorValue; }
    public static void setFallenKingCrownArmorValue(int value) { fallenKingCrownArmorValue = value; }

    public static double fallenKingCrownArmorToughness() { return fallenKingCrownArmorToughness; }
    public static void setFallenKingCrownArmorToughness(double value) { fallenKingCrownArmorToughness = value; }

    public static boolean elythiaFireflyParticlesEnabled() { return elythiaFireflyParticlesEnabled; }
    public static void setElythiaFireflyParticlesEnabled(boolean value) { elythiaFireflyParticlesEnabled = value; }

    public static int bloodCrystalArmorShieldRechargeTicks() { return bloodCrystalArmorShieldRechargeTicks; }
    public static void setBloodCrystalArmorShieldRechargeTicks(int v) { bloodCrystalArmorShieldRechargeTicks = v; }

    public static int bloodCrystalAppleShieldCount() { return bloodCrystalAppleShieldCount; }
    public static void setBloodCrystalAppleShieldCount(int v) { bloodCrystalAppleShieldCount = v; }

    public static int bloodCrystalHelmetDefense() { return bloodCrystalHelmetDefense; }
    public static void setBloodCrystalHelmetDefense(int v) { bloodCrystalHelmetDefense = v; }
    public static int bloodCrystalChestplateDefense() { return bloodCrystalChestplateDefense; }
    public static void setBloodCrystalChestplateDefense(int v) { bloodCrystalChestplateDefense = v; }
    public static int bloodCrystalLeggingsDefense() { return bloodCrystalLeggingsDefense; }
    public static void setBloodCrystalLeggingsDefense(int v) { bloodCrystalLeggingsDefense = v; }
    public static int bloodCrystalBootsDefense() { return bloodCrystalBootsDefense; }
    public static void setBloodCrystalBootsDefense(int v) { bloodCrystalBootsDefense = v; }
    public static double bloodCrystalArmorToughness() { return bloodCrystalArmorToughness; }
    public static void setBloodCrystalArmorToughness(double v) { bloodCrystalArmorToughness = v; }
    public static int bloodCrystalHelmetDurability() { return bloodCrystalHelmetDurability; }
    public static void setBloodCrystalHelmetDurability(int v) { bloodCrystalHelmetDurability = v; }
    public static int bloodCrystalChestplateDurability() { return bloodCrystalChestplateDurability; }
    public static void setBloodCrystalChestplateDurability(int v) { bloodCrystalChestplateDurability = v; }
    public static int bloodCrystalLeggingsDurability() { return bloodCrystalLeggingsDurability; }
    public static void setBloodCrystalLeggingsDurability(int v) { bloodCrystalLeggingsDurability = v; }
    public static int bloodCrystalBootsDurability() { return bloodCrystalBootsDurability; }
    public static void setBloodCrystalBootsDurability(int v) { bloodCrystalBootsDurability = v; }

    public static int bloodCrystalAppleDurationTicks() { return bloodCrystalAppleDurationTicks; }
    public static void setBloodCrystalAppleDurationTicks(int v) { bloodCrystalAppleDurationTicks = v; }

    public static int bloodCrystalAppleShieldRechargeTicks() { return bloodCrystalAppleShieldRechargeTicks; }
    public static void setBloodCrystalAppleShieldRechargeTicks(int v) { bloodCrystalAppleShieldRechargeTicks = v; }

    public static int bloodCrystalHardMaxShields() { return bloodCrystalHardMaxShields; }
    public static void setBloodCrystalHardMaxShields(int v) { bloodCrystalHardMaxShields = v; }

    public static double toreterrorHealth() { return toreterrorHealth; }
    public static void setToreterrorHealth(double v) { toreterrorHealth = v; }
    public static double toreterrorJumpAttackDamage() { return toreterrorJumpAttackDamage; }
    public static void setToreterrorJumpAttackDamage(double v) { toreterrorJumpAttackDamage = v; }
    public static double toreterrorJumpAttackKnockback() { return toreterrorJumpAttackKnockback; }
    public static void setToreterrorJumpAttackKnockback(double v) { toreterrorJumpAttackKnockback = v; }
    public static double toreterrorSpinDamage() { return toreterrorSpinDamage; }
    public static void setToreterrorSpinDamage(double v) { toreterrorSpinDamage = v; }
    public static double toreterrorSpinKnockback() { return toreterrorSpinKnockback; }
    public static void setToreterrorSpinKnockback(double v) { toreterrorSpinKnockback = v; }
    public static double toreterrorRangedWaterBombChance() { return toreterrorRangedWaterBombChance; }
    public static void setToreterrorRangedWaterBombChance(double v) { toreterrorRangedWaterBombChance = v; }
    public static double toreterrorProjectileDamageMultiplier() { return toreterrorProjectileDamageMultiplier; }
    public static void setToreterrorProjectileDamageMultiplier(double v) { toreterrorProjectileDamageMultiplier = v; }
    public static double waterBombDamage() { return waterBombDamage; }
    public static void setWaterBombDamage(double v) { waterBombDamage = v; }
    public static int waterBombLifetimeTicks() { return waterBombLifetimeTicks; }
    public static void setWaterBombLifetimeTicks(int v) { waterBombLifetimeTicks = v; }
    public static double waterBombGravity() { return waterBombGravity; }
    public static void setWaterBombGravity(double v) { waterBombGravity = v; }
    public static double waterBombKnockback() { return waterBombKnockback; }
    public static void setWaterBombKnockback(double v) { waterBombKnockback = v; }
    public static double waterCannonCooldownSeconds() { return waterCannonCooldownSeconds; }
    public static void setWaterCannonCooldownSeconds(double v) { waterCannonCooldownSeconds = v; }

    public static double creepingHorrorHealth() { return creepingHorrorHealth; }
    public static void setCreepingHorrorHealth(double v) { creepingHorrorHealth = v; }
    public static double creepingHorrorAttackDamage() { return creepingHorrorAttackDamage; }
    public static void setCreepingHorrorAttackDamage(double v) { creepingHorrorAttackDamage = v; }
    public static double lurkingTerrorHealth() { return lurkingTerrorHealth; }
    public static void setLurkingTerrorHealth(double v) { lurkingTerrorHealth = v; }
    public static double lurkingTerrorAttackDamage() { return lurkingTerrorAttackDamage; }
    public static void setLurkingTerrorAttackDamage(double v) { lurkingTerrorAttackDamage = v; }
}
