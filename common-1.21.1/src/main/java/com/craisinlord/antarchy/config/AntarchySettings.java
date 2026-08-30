package com.craisinlord.antarchy.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class AntarchySettings {
    private static boolean disableInfinityBookPortalCreation = true;
    private static boolean easterBunnyEnabled = true;
    private static int easterBunnyNaturalSpawnChancePercent = 1;
    private static boolean rainbowAntsLeadToInfinityDimensions = true;
    private static double rainbowAntInfinityEasterEggChance = 0.01D;
    private static boolean brownAntRequiresReagent = false;
    private static boolean brownAntRightClickDimension = true;
    private static boolean redAntRequiresReagent = false;
    private static boolean redAntRightClickDimension = true;
    private static boolean rainbowAntRequiresReagent = true;
    private static boolean rainbowAntRightClickDimension = true;
    private static boolean termiteRequiresReagent = false;
    private static boolean termiteRightClickDimension = true;
    private static boolean antsStealFromChests = true;
    private static boolean duplicatorTreeEnabled = true;
    private static boolean glowVinesUnderLeaves = true;
    private static boolean swingThroughGrassEnabled = true;
    private static boolean fabricKeybindingConflictFixEnabled = true;
    private static boolean experimentalSettingsPopupDisabled = true;
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
    private static double bomberHealth = 15.0D;
    private static double bomberAttackDamage = 4.0D;
    private static double bomberExplosionDamage = 8.0D;
    private static double bomberExplosionRadius = 4.0D;
    private static double redAntAttackDamage = 3.0D;
    private static double ultimateSwordAttackDamage = 34.0D;
    private static double ultimatePickaxeAttackDamage = 24.0D;
    private static double ultimateAxeAttackDamage = 42.0D;
    private static double ultimateShovelAttackDamage = 26.0D;
    private static double ultimateHoeAttackDamage = 8.0D;
    private static double ultimateBowAttackDamage = 18.0D;
    private static double ultimateBowPlayerHeal = 8.0D;
    private static double ultimateBowDrawSpeedMultiplier = 2.5D;
    private static boolean ultimateBowComesEnchantedWithFlame = true;
    private static double ultimateCrossbowAttackDamage = 8.0D;
    private static double ultimateCrossbowChargeSpeedMultiplier = 0.25D;
    private static double ultimateMaceDamageMultiplier = 1.5D;
    private static double battleAxeAttackDamage = 50.0D;
    private static double bigBerthaAttackDamage = 62.0D;
    private static double bigBerthaReachBonus = 3.0D;
    private static double bigBerthaAttackSpeed = -3.0D;
    private static double attitudeAdjusterBaseDamage = 47.0D;
    private static boolean attitudeAdjusterBreaksBlocks = true;
    private static double krakensGraspAttackDamage = 16.0D;
    private static double krakensGraspAttackSpeed = -2.9D;
    private static double krakensGraspThrownDamage = 32.0D;
    private static double krakensGraspLightningDamage = 6.0D;
    private static boolean krakensGraspInnateLoyalty = true;
    private static int krakensGraspInnateLoyaltyLevel = 3;
    private static int krakensGraspTentacleDurationTicks = 100;
    private static double krakensGraspTentacleRadius = 3.0D;
    private static int krakensGraspTentacleSlownessAmplifier = 8;
    private static int krakensGraspTentacleSlownessRefreshTicks = 5;
    private static double scorpionWhipBaseDamage = 10.0D;
    private static double scorpionWhipReachBonus = 5.0D;
    private static int scorpionWhipPoisonDurationTicks = 100;
    private static double scorpionWhipTetherMaxRange = 10.0D;
    private static double scorpionWhipSnapBonusDamage = 6.0D;
    private static double scorpionWhipPullStrength = 0.75D;
    private static double scorpionWhipHeavyPullMultiplier = 0.25D;
    private static double scorpionWhipSelfPullMultiplier = 0.45D;
    private static int bloodCrystalKatanaAttackDamage = 7;
    private static double bloodCrystalKatanaLaunchStrength = 1.1D;
    private static int bloodCrystalKatanaTrailDurationTicks = 12;
    private static double bloodCrystalKatanaReachBonus = 4.0D;
    private static int bloodCrystalKatanaInvulnTicks = 8;
    private static int bloodCrystalKatanaDashCooldownTicks = 4;
    private static double bigBerthaBasiliskCooldownSeconds = 7.0D;
    private static double bigBerthaLucidInvertedDurationSeconds = 3.0D;
    private static double bigBerthaLucidInvertedDamageBonusPercent = 25.0D;
    private static double bigBerthaNoneModeDamageBonusPercent = 15.0D;
    private static double bigBerthaNightmareDamageBonusPercent = 30.0D;
    private static double potentNyxiteInvertedDurationSeconds = 60.0D;
    private static int dimensionalTearLifetimeTicks = 24000;
    private static int dimensionalTearInvertedDurationTicks = 2400;
    private static int dimensionalTearEmergenceMinIntervalTicks = 2400;
    private static int dimensionalTearEmergenceMaxIntervalTicks = 7200;
    private static float dimensionalTearLucidEventChance = 0.65F;
    private static int ultimateToolEnchantability = 25;
    private static int ultimateArmorEnchantability = 10;
    private static int ultimateBowEnchantability = 20;
    private static int ultimateCrossbowEnchantability = 20;
    private static int ultimateMaceEnchantability = 20;
    private static boolean ultimateToolsThreeByThreeEnabled = true;
    private static boolean ultimateArmorComesEnchanted = true;
    private static int ultimateHelmetArmorValue = 6;
    private static int ultimateChestplateArmorValue = 11;
    private static int ultimateLeggingsArmorValue = 9;
    private static int ultimateBootsArmorValue = 6;
    private static double ultimateHelmetArmorToughness = 4.5D;
    private static double ultimateChestplateArmorToughness = 4.5D;
    private static double ultimateLeggingsArmorToughness = 4.5D;
    private static double ultimateBootsArmorToughness = 4.5D;
    private static ResourceKey<Level> brownAntDestinationDimension = dimensionKey("antarchy:elythia");
    private static ResourceKey<Level> redAntDestinationDimension = dimensionKey("antarchy:thoraxis");
    private static ResourceKey<Level> termiteDestinationDimension = dimensionKey("antarchy:cavaryn");
    private static ResourceKey<Level> rainbowAntNonInfinityFallbackDimension = dimensionKey("antarchy:elythia");
    private static double krakenHealth = 1800.0D;
    private static double krakenAttackDamage = 45.0D;
    private static double octopusBombHealth = 100.0D;
    private static double octopusBombAttackDamage = 16.0D;
    private static double brutalflyHealth = 500.0D;
    private static double brutalflySwipeDamage = 15.0D;
    private static double brutalflySpitDamage = 5.0D;
    private static double mantisHealth = 50.0D;
    private static double mantisAttackDamage = 9.0D;
    private static double mantisMovementSpeed = 0.42D;
    private static double mantisFlyingSpeed = 0.77D;
    private static boolean mantisIgnoreLightLevel = false;
    private static double alphaMantisHealth = 380.0D;
    private static double alphaMantisAttackDamage = 18.0D;
    private static double alphaMantisMovementSpeed = 0.546D;
    private static double alphaMantisFlyingSpeed = 0.77D;
    private static int alphaMantisSummonIntervalTicks = 300;
    private static int alphaMantisMaxMinions = 4;
    private static double rollyPollyHealth = 25.0D;
    private static double rollyPollyMovementSpeed = 0.3D;
    private static double rollyPollyRollSpeedMultiplier = 2.2D;
    private static int rollyPollyTameChance = 3;
    private static double rollyPollyBowlingDamage = 3.0D;
    private static double rollyPollyBowlingKnockback = 0.8D;
    private static double rollyPollyArmor = 2.0D;
    private static double rollyPollyRolledArmorBonus = 16.0D;
    private static boolean krakenSquidSpawnEnabled = true;
    private static boolean krakenMassSpawnEnabled = true;
    private static boolean krakenRequireBadOmenToSummon = true;
    private static double squidzookaCooldownSeconds = 1.0D;
    private static double squidzookaLaunchVelocity = 2.1D;
    private static double rpoLauncherCooldownSeconds = 1.0D;
    private static double rpoLauncherLaunchVelocity = 1.8D;
    private static double rpoLauncherExplosionDamage = 34.0D;
    private static double rpoLauncherExplosionRadius = 14.0D;
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
    private static double critterCageMaxCapturableWidth = 1.65D;
    private static double critterCageMaxCapturableHeight = 1.65D;
    private static boolean minersDreamEnabled = true;
    private static int minersDreamMinimumRange = 48;
    private static int minersDreamMaximumRange = 96;
    private static int minersDreamTorchSpacing = 10;
    private static int minersDreamBlocksPerTick = 150;
    private static double lucidAttackRange = 9.0D;
    private static double lucidPearlInvertedDurationSeconds = 6.0D;
    private static int corneaEarNightVisionSeconds = 15;
    private static boolean americanizingEnabled = true;
    private static int americanBonusNutrition = 2;
    private static double americanBonusSaturation = 1.0D;
    private static int americanRegenerationDurationTicks = 60;
    private static int americanRegenerationAmplifier = 0;
    private static boolean dreamSandEnabled = true;
    private static boolean entitySpecificFireOverlayEnabled = true;
    private static double dreamSandJumpVelocityMultiplier = 1.3D;
    private static double dreamSandGravityMultiplier = 0.45D;
    private static double dreamSandEffectDurationSeconds = 7.0D;
    private static double dreamSandFallingBlockGravityMultiplier = 0.4D;
    private static boolean ichorWitherEnabled = true;
    private static int nightmareHelmetArmorValue = 4;
    private static int nightmareChestplateArmorValue = 9;
    private static int nightmareLeggingsArmorValue = 7;
    private static int nightmareBootsArmorValue = 4;
    private static double nightmareHelmetArmorToughness = 4.0D;
    private static double nightmareChestplateArmorToughness = 4.0D;
    private static double nightmareLeggingsArmorToughness = 4.0D;
    private static double nightmareBootsArmorToughness = 4.0D;

    private static double nightmareHelmetDoubleDamageChance = 0.05D;
    private static double nightmareChestplateDoubleDamageChance = 0.07D;
    private static double nightmareLeggingsDoubleDamageChance = 0.07D;
    private static double nightmareBootsDoubleDamageChance = 0.05D;

    private static double nightmareSwordBaseDamage = 18.0D;

    private static double nightmareSwordScalingFactor = 1.5D;

    private static boolean royalArmorComesEnchanted = true;
    private static int royalArmorProtectionLevel = 10;
    private static int royalArmorEnchantability = 30;
    private static int royalArmorDurabilityMultiplier = 93;
    private static int royalGuardianHelmetArmorValue = 5;
    private static int royalGuardianChestplateArmorValue = 10;
    private static int royalGuardianLeggingsArmorValue = 8;
    private static int royalGuardianBootsArmorValue = 5;
    private static double royalGuardianArmorToughness = 4.0D;
    private static double royalGuardianArmorKnockbackResistance = 0.15D;
    private static int royalAssailantHelmetArmorValue = 5;
    private static int royalAssailantChestplateArmorValue = 10;
    private static int royalAssailantLeggingsArmorValue = 8;
    private static int royalAssailantBootsArmorValue = 5;
    private static double royalAssailantArmorToughness = 3.0D;
    private static double royalAssailantArmorKnockbackResistance = 0.15D;
    private static double royalAssailantHelmetDoubleDamageChance = 0.06D;
    private static double royalAssailantChestplateDoubleDamageChance = 0.08D;
    private static double royalAssailantLeggingsDoubleDamageChance = 0.07D;
    private static double royalAssailantBootsDoubleDamageChance = 0.05D;
    private static double royalGuardianSwordAttackDamage = 111.0D;
    private static double royalGuardianSwordAttackSpeed = -2.5D;
    private static double royalAssailantBattleAxeAttackDamage = 151.0D;
    private static double royalAssailantBattleAxeAttackSpeed = -2.9D;
    private static double royalWeaponAttackReachBonus = 1.0D;
    private static double royalWeaponAttackKnockbackBonus = 1.0D;
    private static int royalWeaponDurability = 4608;
    private static int royalWeaponEnchantability = 30;
    private static int royalGuardianShieldDurability = 1008;
    private static double royalBoltDamage = 6.0D;
    private static int royalEggHatchChance = 3;
    private static double kingHealth = 6000.0D;
    private static double queenHealth = 6000.0D;
    private static double kingAttackDamage = 50.0D;
    private static double queenAttackDamage = 45.0D;
    private static double royalBossArmor = 20.0D;
    private static double royalBossFollowRange = 128.0D;
    private static double royalBossMovementSpeed = 0.22D;
    private static double royalBossKnockbackResistance = 1.0D;
    private static double royalBossStepHeight = 3.0D;
    private static double royalBossMaxSingleHitDamage = 250.0D;
    private static double royalBossBiteReach = 10.0D;
    private static double royalBossBiteDamageMultiplier = 1.6D;
    private static int royalBossBiteCooldownTicks = 45;
    private static boolean basiliskPetrifyingGazeEnabled = true;
    private static int basiliskSpawnMaxLightLevel = 5;
    private static double basiliskHealth = 320.0D;
    private static double basiliskAttackDamage = 36.0D;
    private static double basiliskMovementSpeed = 0.18D;
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
    private static double diamondMinecartCruiseSpeed = 0.0D;
    private static double diamondMinecartMaxSpeed = 0.55D;
    private static double diamondMinecartAcceleration = 0.02D;
    private static double diamondMinecartDeceleration = 0.05D;
    private static double diamondMinecartCoastDeceleration = 0.02D;
    private static boolean diamondMinecartMobDamageEnabled = false;
    private static double diamondMinecartMaxMobDamage = 4.0D;
    private static double hoverboardMaxSpeed = 6.0D;
    private static double hoverboardAcceleration = 0.05D;
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
    private static double caterpillarHealth = 6.0D;
    private static double caterpillarMovementSpeed = 0.24D;
    private static double caterpillarPupationTimeSeconds = 600.0D;
    private static double butterflyHealth = 6.0D;
    private static double butterflyMovementSpeed = 0.3D;
    private static double butterflyFlyingSpeed = 0.55D;
    private static double  reverieHealth = 20.0D;
    private static double  reverieInterestRadius = 16.0D;
    private static double  reverieAbandonPlayerDistance = 24.0D;
    private static int     reverieNoticeDurationTicks = 20;
    private static int     reverieInterestDurationTicks = 360;
    private static int     reverieRebindCooldownTicks = 100;
    private static int     reverieDamageReactionDurationTicks = 60;
    private static double  reverieDangerousFallDistance = 4.0D;
    private static double  reverieDangerousFallSpeed = 0.18D;
    private static double  reverieWarningThreatRadius = 10.0D;
    private static double  reverieWarningThreatVerticalRange = 5.0D;
    private static int     reverieDuplicationCooldownTicks = 6000;
    private static final double  reveriePreferredFollowMinDistance = 2.5D;
    private static final double  reveriePreferredFollowMaxDistance = 2.5D;
    private static final double  reverieCatchUpDistance           = 12.0D;
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
    private static double emperorScorpionHealth = 420.0D;
    private static double emperorScorpionAttackDamage = 28.0D;
    private static double emperorScorpionMovementSpeed = 0.384D;
    private static double emperorScorpionArmor = 22.0D;
    private static double emperorScorpionKnockbackResistance = 0.8D;
    private static double emperorScorpionFollowRange = 80.0D;
    private static int emperorScorpionXpReward = 25;
    private static int emperorScorpionClawAnimTicks = 23;
    private static int emperorScorpionClawHitTick = 12;
    private static int emperorScorpionClawCooldownTicks = 28;
    private static int emperorScorpionStingAnimTicks = 25;
    private static int emperorScorpionStingHitTick = 13;
    private static int emperorScorpionStingCooldownTicks = 60;
    private static int emperorScorpionPoisonTicks = 300;
    private static int emperorScorpionWeaknessTicks = 120;
    private static int emperorScorpionSummonIntervalTicks = 140;
    private static int emperorScorpionMaxSummonedScorpions = 4;
    private static int emperorScorpionMaxNearbyScorpions = 4;
    private static int emperorScorpionHardenCooldownTicks = 2400;
    private static double emperorScorpionDamageRange = 25.0D;
    private static int emperorScorpionMinDespawnTicks = 6000;
    private static double scorpionHealth = 30.0D;
    private static double scorpionAttackDamage = 5.0D;
    private static double scorpionMovementSpeed = 0.29D;
    private static double scorpionArmor = 2.0D;
    private static double scorpionKnockbackResistance = 0.2D;
    private static double bedBugHealth = 18.0D;
    private static double bedBugAttackDamage = 4.0D;
    private static double bedBugMovementSpeed = 0.24D;
    private static double bedBugArmor = 2.0D;
    private static double jumpyBugHealth = 100.0D;
    private static double jumpyBugPounceDamage = 12.0D;
    private static double jumpyBugCamouflageAlpha = 0.18D;
    private static double brutalflyArmor = 10.0D;
    private static double brutalflyArmorToughness = 5.0D;
    private static double brutalflyKnockbackResistance = 0.7D;
    private static double brutalflyMovementSpeed = 0.34D;
    private static double brutalflyFlyingSpeed = 0.55D;
    private static double cloudSharkHealth = 40.0D;
    private static double cloudSharkAttackDamage = 5.0D;
    private static double cloudSharkMovementSpeed = 0.34D;
    private static double cloudSharkFlyingSpeed = 0.5D;
    private static double cloudSharkKnockbackResistance = 0.2D;
    private static double krakenMovementSpeed = 0.24D;
    private static double krakenFlyingSpeed = 0.24D;
    private static double krakenKnockbackResistance = 0.8D;
    private static double krakenArmor = 8.0D;
    private static double krakenBossBarRange = 40.0D;
    private static double krakenFollowRange = 96.0D;
    private static double krakenDamageRange = 35.0D;
    private static double herculesBeetleDamageRange = 25.0D;
    private static double toreterrorDamageRange = 25.0D;
    private static double brutalflyDamageRange = 25.0D;
    private static double alphaMantisDamageRange = 25.0D;
    private static float krakenLightningDamagePhaseOne = 12.0F;
    private static double lucidHealth = 70.0D;
    private static double lucidAttackDamage = 4.0D;
    private static double lucidMovementSpeed = 0.22D;
    private static double lucidFlyingSpeed = 0.35D;
    private static double lucidKnockbackResistance = 0.5D;
    private static double vortexHealth = 70.0D;
    private static double vortexAttackDamage = 4.0D;
    private static double vortexMovementSpeed = 0.22D;
    private static double vortexFlyingSpeed = 0.35D;
    private static int vortexMaxActiveVortexes = 3;
    private static int windVortexDurationTicks = 140;
    private static double windVortexPullStrength = 0.32D;
    private static double windVortexLaunchStrength = 1.0D;
    private static boolean eyeOfTheStormEnabled = true;
    private static int eyeOfTheStormUpdraftCooldownTicks = 40;
    private static double eyeOfTheStormUpdraftLaunchStrength = 1.15D;
    private static double eyeOfTheStormUpdraftHeight = 4.0D;
    private static double eyeOfTheStormUpdraftRadius = 1.3D;
    private static int eyeOfTheStormUpdraftDurationTicks = 32;
    private static int eyeOfTheStormSurgeCooldownTicks = 70;
    private static double eyeOfTheStormSurgeRange = 16.0D;
    private static int eyeOfTheStormSurgeDurationTicks = 300;
    private static double eyeOfTheStormSurgeRadius = 4.0D;
    private static double eyeOfTheStormSurgeHeight = 6.0D;
    private static double eyeOfTheStormSurgePullStrength = 0.85D;
    private static double eyeOfTheStormSurgeReturnStrength = 1.4D;
    private static float eyeOfTheStormSurgeDamage = 5.0F;
    private static double vortexLensMinRadius = 1.5D;
    private static double vortexLensMaxRadius = 10.0D;
    private static double vortexLensMinHeight = 3.0D;
    private static double vortexLensMaxHeight = 20.0D;
    private static double vortexLensPullStrength = 0.65D;
    private static double vortexLensPushStrength = 0.65D;
    private static double vortexLensLaunchStrength = 1.4D;
    private static double missileSquidHealth = 60.0D;
    private static double missileSquidAttackDamage = 9.0D;
    private static double missileSquidMovementSpeed = 0.84D;
    private static double missileSquidFlyingSpeed = 0.84D;
    private static double missileSquidKnockbackResistance = 0.15D;
    private static double molewormHealth = 8.0D;
    private static double molewormAttackDamage = 1.0D;
    private static double molewormMovementSpeed = 0.25D;
    private static double triffidHealth = 160.0D;
    private static double triffidAttackDamage = 10.0D;
    private static double triffidGrabDamage = 15.0D;
    private static int triffidSweepHitCooldownTicks = 10;
    private static double molevoreHealth = 140.0D;
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
    private static double nightmareHealth = 380.0D;
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
    private static double ultimateArmorKnockbackResistance = 0.15D;
    private static double nightmareArmorKnockbackResistance = 0.1D;
    private static double primordialArmorKnockbackPerPiece = 0.5D;
    private static double mogglesVisionRadius = 20.0D;
    private static int fallenKingCrownArmorValue = 2;
    private static double fallenKingCrownArmorToughness = 0.0D;
    private static boolean permanentPortalsEnabled = true;
    private static boolean permanentPortalsFlintAndSteelEnabled = false;
    private static boolean elythiaPortalEnabled = true;
    private static boolean thoraxisPortalEnabled = true;
    private static boolean cavarynPortalEnabled = true;
    private static boolean elythiaFireflyParticlesEnabled = true;
    private static int bloodCrystalArmorShieldRechargeTicks = 600;
    private static int bloodCrystalAppleShieldCount = 2;
    private static int bloodCrystalAppleDurationTicks = 2400;
    private static int bloodCrystalAppleShieldRechargeTicks = 600;
    private static int bloodCrystalHardMaxShields = 10;
    // Armor stats (iron defense, gold durability, 0 toughness by default)
    private static int bloodCrystalHelmetDefense = 3;
    private static int bloodCrystalChestplateDefense = 7;
    private static int bloodCrystalLeggingsDefense = 6;
    private static int bloodCrystalBootsDefense = 3;
    private static double bloodCrystalArmorToughness = 2.0D;
    private static int bloodCrystalHelmetDurability = 77;
    private static int bloodCrystalChestplateDurability = 112;
    private static int bloodCrystalLeggingsDurability = 105;
    private static int bloodCrystalBootsDurability = 91;
    private static double toreterrorHealth = 450.0D;
    private static double toreterrorJumpAttackDamage = 14.0D;
    private static double toreterrorJumpAttackKnockback = 2.5D;
    private static double toreterrorSpinDamage = 6.0D;
    private static double toreterrorSpinKnockback = 0.9D;
    private static double toreterrorProjectileDamageMultiplier = 0.5D;
    private static double waterBombDamage = 6.0D;
    private static int waterBombLifetimeTicks = 120;
    private static double waterBombGravity = 0.12D;
    private static double waterBombKnockback = 1.2D;
    private static double creepingHorrorHealth = 15.0D;
    private static double creepingHorrorAttackDamage = 6.0D;
    private static double lurkingTerrorHealth = 15.0D;
    private static double lurkingTerrorAttackDamage = 6.0D;
    private static double manticoreHealth = 40.0D;
    private static double manticoreAttackDamage = 7.0D;
    private static int manticoreStingPoisonTicks = 100;
    private static int queenManticoreCap = 15;
    private static double jerryInfantHealth = 20.0D;
    private static double jerryInfantAttackDamage = 2.0D;
    private static double jerryMatureHealth = 40.0D;
    private static double jerryMatureAttackDamage = 3.5D;
    private static double jerryAlphaHealth = 60.0D;
    private static double jerryAlphaAttackDamage = 6.0D;
    private static double jerryGammaHealth = 60.0D;
    private static double jerryGammaAttackDamage = 7.0D;
    private static double cheepHealth = 8.0D;
    private static double cheepAttackDamage = 3.0D;
    private static double dorrieHealth = 60.0D;
    private static double herculesBeetleHealth = 650.0D;
    private static double herculesBeetleAttackDamage = 30.0D;
    private static double herculesBeetleChargeDamage = 40.0D;
    private static double ouranwoodDeerHealth = 20.0D;
    private static double glimmerHealth = 20.0D;

    private static double lucidArmorToughness = 2.0D;
    private static double triffidArmorToughness = 3.0D;
    private static double basiliskArmorToughness = 5.0D;
    private static double nightmareMobArmorToughness = 5.0D;
    private static double alphaMantisArmorToughness = 4.0D;
    private static double herculesBeetleArmorToughness = 4.0D;
    private static double krakenArmorToughness = 6.0D;
    private static double nightmareMobArmor = 10.0D;

    private static double bossMagicPerHitCapFraction = 0.12D;
    private static int bossMagicWindowTicks = 20;
    private static double bossMagicWindowCapFraction = 0.20D;
    private static double bossMagicWardReductionFraction = 0.5D;
    private static int bossMagicWardDurationTicks = 60;
    private static int bossMagicWardTriggerBreaches = 2;
    private static java.util.List<String> magicBurstDamageNamespaces = new java.util.ArrayList<>(java.util.List.of("irons_spellbooks"));

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

    public static double rainbowAntInfinityEasterEggChance() {
        return rainbowAntInfinityEasterEggChance;
    }

    public static boolean brownAntRightClickDimension() {
        return brownAntRightClickDimension;
    }

    public static boolean redAntRightClickDimension() {
        return redAntRightClickDimension;
    }

    public static boolean rainbowAntRightClickDimension() {
        return rainbowAntRightClickDimension;
    }

    public static boolean termiteRightClickDimension() {
        return termiteRightClickDimension;
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

    public static boolean glowVinesUnderLeaves() {
        return glowVinesUnderLeaves;
    }

    public static boolean swingThroughGrassEnabled() {
        return swingThroughGrassEnabled;
    }

    public static boolean fabricKeybindingConflictFixEnabled() {
        return fabricKeybindingConflictFixEnabled;
    }

    public static boolean experimentalSettingsPopupDisabled() {
        return experimentalSettingsPopupDisabled;
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

    public static double attitudeAdjusterBaseDamage() {
        return attitudeAdjusterBaseDamage;
    }

    public static boolean attitudeAdjusterBreaksBlocks() {
        return attitudeAdjusterBreaksBlocks;
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

    public static double bigBerthaNoneModeDamageBonusPercent() {
        return bigBerthaNoneModeDamageBonusPercent;
    }

    public static double bigBerthaNightmareDamageBonusPercent() {
        return bigBerthaNightmareDamageBonusPercent;
    }

    public static double potentNyxiteInvertedDurationSeconds() {
        return potentNyxiteInvertedDurationSeconds;
    }

    public static int dimensionalTearLifetimeTicks() {
        return dimensionalTearLifetimeTicks;
    }

    public static void setDimensionalTearLifetimeTicks(int value) {
        dimensionalTearLifetimeTicks = value;
    }

    public static int dimensionalTearInvertedDurationTicks() {
        return dimensionalTearInvertedDurationTicks;
    }

    public static void setDimensionalTearInvertedDurationTicks(int value) {
        dimensionalTearInvertedDurationTicks = value;
    }

    public static int dimensionalTearEmergenceMinIntervalTicks() {
        return dimensionalTearEmergenceMinIntervalTicks;
    }

    public static void setDimensionalTearEmergenceMinIntervalTicks(int value) {
        dimensionalTearEmergenceMinIntervalTicks = value;
    }

    public static int dimensionalTearEmergenceMaxIntervalTicks() {
        return dimensionalTearEmergenceMaxIntervalTicks;
    }

    public static void setDimensionalTearEmergenceMaxIntervalTicks(int value) {
        dimensionalTearEmergenceMaxIntervalTicks = value;
    }

    public static float dimensionalTearLucidEventChance() {
        return dimensionalTearLucidEventChance;
    }

    public static void setDimensionalTearLucidEventChance(float value) {
        dimensionalTearLucidEventChance = value;
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

    public static double krakensGraspAttackDamage() { return krakensGraspAttackDamage; }
    public static double krakensGraspAttackSpeed() { return krakensGraspAttackSpeed; }
    public static double krakensGraspThrownDamage() { return krakensGraspThrownDamage; }
    public static boolean krakensGraspInnateLoyalty() { return krakensGraspInnateLoyalty; }
    public static int krakensGraspInnateLoyaltyLevel() { return krakensGraspInnateLoyaltyLevel; }
    public static int krakensGraspTentacleDurationTicks() { return krakensGraspTentacleDurationTicks; }
    public static double krakensGraspTentacleRadius() { return krakensGraspTentacleRadius; }
    public static int krakensGraspTentacleSlownessAmplifier() { return krakensGraspTentacleSlownessAmplifier; }
    public static int krakensGraspTentacleSlownessRefreshTicks() { return krakensGraspTentacleSlownessRefreshTicks; }

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

    public static double alphaMantisHealth() {
        return alphaMantisHealth;
    }

    public static double alphaMantisAttackDamage() {
        return alphaMantisAttackDamage;
    }

    public static double alphaMantisMovementSpeed() {
        return alphaMantisMovementSpeed;
    }

    public static double alphaMantisFlyingSpeed() {
        return alphaMantisFlyingSpeed;
    }

    public static int alphaMantisSummonIntervalTicks() {
        return alphaMantisSummonIntervalTicks;
    }

    public static int alphaMantisMaxMinions() {
        return alphaMantisMaxMinions;
    }

    public static double rollyPollyHealth() {
        return rollyPollyHealth;
    }

    public static double rollyPollyMovementSpeed() {
        return rollyPollyMovementSpeed;
    }

    public static double rollyPollyRollSpeedMultiplier() {
        return rollyPollyRollSpeedMultiplier;
    }

    public static int rollyPollyTameChance() {
        return rollyPollyTameChance;
    }

    public static double rollyPollyBowlingDamage() {
        return rollyPollyBowlingDamage;
    }

    public static double rollyPollyBowlingKnockback() {
        return rollyPollyBowlingKnockback;
    }

    public static double rollyPollyArmor() {
        return rollyPollyArmor;
    }

    public static double rollyPollyRolledArmorBonus() {
        return rollyPollyRolledArmorBonus;
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

    public static boolean krakenSquidSpawnEnabled() {
        return krakenSquidSpawnEnabled;
    }

    public static boolean krakenMassSpawnEnabled() {
        return krakenMassSpawnEnabled;
    }

    public static boolean krakenRequireBadOmenToSummon() {
        return krakenRequireBadOmenToSummon;
    }

    public static double squidzookaCooldownSeconds() {
        return squidzookaCooldownSeconds;
    }

    public static double squidzookaLaunchVelocity() {
        return squidzookaLaunchVelocity;
    }

    public static double rpoLauncherCooldownSeconds() { return rpoLauncherCooldownSeconds; }
    public static double rpoLauncherLaunchVelocity() { return rpoLauncherLaunchVelocity; }
    public static double rpoLauncherExplosionDamage() { return rpoLauncherExplosionDamage; }
    public static double rpoLauncherExplosionRadius() { return rpoLauncherExplosionRadius; }

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

    public static double critterCageMaxCapturableWidth() {
        return critterCageMaxCapturableWidth;
    }

    public static double critterCageMaxCapturableHeight() {
        return critterCageMaxCapturableHeight;
    }

    public static boolean minersDreamEnabled() {
        return minersDreamEnabled;
    }

    public static int minersDreamMinimumRange() {
        return minersDreamMinimumRange;
    }

    public static int minersDreamMaximumRange() {
        return minersDreamMaximumRange;
    }

    public static int minersDreamTorchSpacing() {
        return minersDreamTorchSpacing;
    }

    public static int minersDreamBlocksPerTick() {
        return minersDreamBlocksPerTick;
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

    public static void setRainbowAntInfinityEasterEggChance(double value) {
        rainbowAntInfinityEasterEggChance = value;
    }

    public static void setBrownAntRightClickDimension(boolean value) {
        brownAntRightClickDimension = value;
    }

    public static void setRedAntRightClickDimension(boolean value) {
        redAntRightClickDimension = value;
    }

    public static void setRainbowAntRightClickDimension(boolean value) {
        rainbowAntRightClickDimension = value;
    }

    public static void setTermiteRightClickDimension(boolean value) {
        termiteRightClickDimension = value;
    }

    public static void setPermanentPortalsEnabled(boolean value) {
        permanentPortalsEnabled = value;
    }

    public static void setPermanentPortalsFlintAndSteelEnabled(boolean value) {
        permanentPortalsFlintAndSteelEnabled = value;
    }

    public static void setElythiaPortalEnabled(boolean value) {
        elythiaPortalEnabled = value;
    }

    public static void setThoraxisPortalEnabled(boolean value) {
        thoraxisPortalEnabled = value;
    }

    public static void setCavarynPortalEnabled(boolean value) {
        cavarynPortalEnabled = value;
    }

    public static boolean permanentPortalsEnabled() { return permanentPortalsEnabled; }
    public static boolean permanentPortalsFlintAndSteelEnabled() { return permanentPortalsFlintAndSteelEnabled; }
    public static boolean elythiaPortalEnabled() { return elythiaPortalEnabled; }
    public static boolean thoraxisPortalEnabled() { return thoraxisPortalEnabled; }
    public static boolean cavarynPortalEnabled() { return cavarynPortalEnabled; }

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

    public static void setGlowVinesUnderLeaves(boolean value) {
        glowVinesUnderLeaves = value;
    }

    public static void setSwingThroughGrassEnabled(boolean value) {
        swingThroughGrassEnabled = value;
    }

    public static void setFabricKeybindingConflictFixEnabled(boolean value) {
        fabricKeybindingConflictFixEnabled = value;
    }

    public static void setExperimentalSettingsPopupDisabled(boolean value) {
        experimentalSettingsPopupDisabled = value;
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

    public static void setAttitudeAdjusterBaseDamage(double value) {
        attitudeAdjusterBaseDamage = value;
    }

    public static void setAttitudeAdjusterBreaksBlocks(boolean value) {
        attitudeAdjusterBreaksBlocks = value;
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

    public static void setBigBerthaNoneModeDamageBonusPercent(double value) {
        bigBerthaNoneModeDamageBonusPercent = value;
    }

    public static void setBigBerthaNightmareDamageBonusPercent(double value) {
        bigBerthaNightmareDamageBonusPercent = value;
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

    public static void setKrakensGraspAttackDamage(double value) { krakensGraspAttackDamage = value; }
    public static void setKrakensGraspAttackSpeed(double value) { krakensGraspAttackSpeed = value; }
    public static void setKrakensGraspThrownDamage(double value) { krakensGraspThrownDamage = value; }
    public static void setKrakensGraspLightningDamage(double value) { krakensGraspLightningDamage = value; }
    public static void setKrakensGraspInnateLoyalty(boolean value) { krakensGraspInnateLoyalty = value; }
    public static void setKrakensGraspInnateLoyaltyLevel(int value) { krakensGraspInnateLoyaltyLevel = value; }
    public static void setKrakensGraspTentacleDurationTicks(int value) { krakensGraspTentacleDurationTicks = value; }
    public static void setKrakensGraspTentacleRadius(double value) { krakensGraspTentacleRadius = value; }
    public static void setKrakensGraspTentacleSlownessAmplifier(int value) { krakensGraspTentacleSlownessAmplifier = value; }
    public static void setKrakensGraspTentacleSlownessRefreshTicks(int value) { krakensGraspTentacleSlownessRefreshTicks = value; }

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

    public static void setAlphaMantisHealth(double value) {
        alphaMantisHealth = value;
    }

    public static void setAlphaMantisAttackDamage(double value) {
        alphaMantisAttackDamage = value;
    }

    public static void setAlphaMantisMovementSpeed(double value) {
        alphaMantisMovementSpeed = value;
    }

    public static void setAlphaMantisFlyingSpeed(double value) {
        alphaMantisFlyingSpeed = value;
    }

    public static void setAlphaMantisSummonIntervalTicks(int value) {
        alphaMantisSummonIntervalTicks = value;
    }

    public static void setAlphaMantisMaxMinions(int value) {
        alphaMantisMaxMinions = value;
    }

    public static void setRollyPollyHealth(double value) {
        rollyPollyHealth = value;
    }

    public static void setRollyPollyMovementSpeed(double value) {
        rollyPollyMovementSpeed = value;
    }

    public static void setRollyPollyRollSpeedMultiplier(double value) {
        rollyPollyRollSpeedMultiplier = value;
    }

    public static void setRollyPollyTameChance(int value) {
        rollyPollyTameChance = value;
    }

    public static void setRollyPollyBowlingDamage(double value) {
        rollyPollyBowlingDamage = value;
    }

    public static void setRollyPollyBowlingKnockback(double value) {
        rollyPollyBowlingKnockback = value;
    }

    public static void setRollyPollyArmor(double value) {
        rollyPollyArmor = value;
    }

    public static void setRollyPollyRolledArmorBonus(double value) {
        rollyPollyRolledArmorBonus = value;
    }

    public static void setKrakenSquidSpawnEnabled(boolean value) {
        krakenSquidSpawnEnabled = value;
    }

    public static void setKrakenMassSpawnEnabled(boolean value) {
        krakenMassSpawnEnabled = value;
    }

    public static void setKrakenRequireBadOmenToSummon(boolean value) {
        krakenRequireBadOmenToSummon = value;
    }

    public static void setSquidzookaCooldownSeconds(double value) {
        squidzookaCooldownSeconds = value;
    }

    public static void setSquidzookaLaunchVelocity(double value) {
        squidzookaLaunchVelocity = value;
    }

    public static void setRpoLauncherCooldownSeconds(double value) { rpoLauncherCooldownSeconds = value; }
    public static void setRpoLauncherLaunchVelocity(double value) { rpoLauncherLaunchVelocity = value; }
    public static void setRpoLauncherExplosionDamage(double value) { rpoLauncherExplosionDamage = value; }
    public static void setRpoLauncherExplosionRadius(double value) { rpoLauncherExplosionRadius = value; }

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

    public static void setCritterCageMaxCapturableWidth(double value) {
        critterCageMaxCapturableWidth = value;
    }

    public static void setCritterCageMaxCapturableHeight(double value) {
        critterCageMaxCapturableHeight = value;
    }

    public static void setMinersDreamEnabled(boolean value) {
        minersDreamEnabled = value;
    }

    public static void setMinersDreamMinimumRange(int value) {
        minersDreamMinimumRange = value;
    }

    public static void setMinersDreamMaximumRange(int value) {
        minersDreamMaximumRange = value;
    }

    public static void setMinersDreamTorchSpacing(int value) {
        minersDreamTorchSpacing = value;
    }

    public static void setMinersDreamBlocksPerTick(int value) {
        minersDreamBlocksPerTick = value;
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

    public static boolean americanizingEnabled() {
        return americanizingEnabled;
    }

    public static void setAmericanizingEnabled(boolean value) {
        americanizingEnabled = value;
    }

    public static int americanBonusNutrition() {
        return americanBonusNutrition;
    }

    public static void setAmericanBonusNutrition(int value) {
        americanBonusNutrition = value;
    }

    public static double americanBonusSaturation() {
        return americanBonusSaturation;
    }

    public static void setAmericanBonusSaturation(double value) {
        americanBonusSaturation = value;
    }

    public static int americanRegenerationDurationTicks() {
        return americanRegenerationDurationTicks;
    }

    public static void setAmericanRegenerationDurationTicks(int value) {
        americanRegenerationDurationTicks = value;
    }

    public static int americanRegenerationAmplifier() {
        return americanRegenerationAmplifier;
    }

    public static void setAmericanRegenerationAmplifier(int value) {
        americanRegenerationAmplifier = value;
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

    public static double dreamSandEffectDurationSeconds() {
        return dreamSandEffectDurationSeconds;
    }

    public static void setDreamSandEffectDurationSeconds(double value) {
        dreamSandEffectDurationSeconds = value;
    }

    public static double dreamSandFallingBlockGravityMultiplier() {
        return dreamSandFallingBlockGravityMultiplier;
    }

    public static void setDreamSandFallingBlockGravityMultiplier(double value) {
        dreamSandFallingBlockGravityMultiplier = value;
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

    public static void setMogglesVisionRadius(double v) { mogglesVisionRadius = v; }
    public static double mogglesVisionRadius() { return mogglesVisionRadius; }

    public static double nightmareHelmetDoubleDamageChance() { return nightmareHelmetDoubleDamageChance; }
    public static void setNightmareHelmetDoubleDamageChance(double v) { nightmareHelmetDoubleDamageChance = v; }

    public static double nightmareChestplateDoubleDamageChance() { return nightmareChestplateDoubleDamageChance; }
    public static void setNightmareChestplateDoubleDamageChance(double v) { nightmareChestplateDoubleDamageChance = v; }

    public static double nightmareLeggingsDoubleDamageChance() { return nightmareLeggingsDoubleDamageChance; }
    public static void setNightmareLeggingsDoubleDamageChance(double v) { nightmareLeggingsDoubleDamageChance = v; }

    public static double nightmareBootsDoubleDamageChance() { return nightmareBootsDoubleDamageChance; }
    public static void setNightmareBootsDoubleDamageChance(double v) { nightmareBootsDoubleDamageChance = v; }

    public static boolean royalArmorComesEnchanted() { return royalArmorComesEnchanted; }
    public static void setRoyalArmorComesEnchanted(boolean v) { royalArmorComesEnchanted = v; }

    public static int royalArmorProtectionLevel() { return royalArmorProtectionLevel; }
    public static void setRoyalArmorProtectionLevel(int v) { royalArmorProtectionLevel = v; }

    public static int royalArmorEnchantability() { return royalArmorEnchantability; }
    public static void setRoyalArmorEnchantability(int v) { royalArmorEnchantability = v; }

    public static int royalArmorDurabilityMultiplier() { return royalArmorDurabilityMultiplier; }
    public static void setRoyalArmorDurabilityMultiplier(int v) { royalArmorDurabilityMultiplier = v; }

    public static int royalGuardianHelmetArmorValue() { return royalGuardianHelmetArmorValue; }
    public static void setRoyalGuardianHelmetArmorValue(int v) { royalGuardianHelmetArmorValue = v; }

    public static int royalGuardianChestplateArmorValue() { return royalGuardianChestplateArmorValue; }
    public static void setRoyalGuardianChestplateArmorValue(int v) { royalGuardianChestplateArmorValue = v; }

    public static int royalGuardianLeggingsArmorValue() { return royalGuardianLeggingsArmorValue; }
    public static void setRoyalGuardianLeggingsArmorValue(int v) { royalGuardianLeggingsArmorValue = v; }

    public static int royalGuardianBootsArmorValue() { return royalGuardianBootsArmorValue; }
    public static void setRoyalGuardianBootsArmorValue(int v) { royalGuardianBootsArmorValue = v; }

    public static double royalGuardianArmorToughness() { return royalGuardianArmorToughness; }
    public static void setRoyalGuardianArmorToughness(double v) { royalGuardianArmorToughness = v; }

    public static double royalGuardianArmorKnockbackResistance() { return royalGuardianArmorKnockbackResistance; }
    public static void setRoyalGuardianArmorKnockbackResistance(double v) { royalGuardianArmorKnockbackResistance = v; }

    public static int royalAssailantHelmetArmorValue() { return royalAssailantHelmetArmorValue; }
    public static void setRoyalAssailantHelmetArmorValue(int v) { royalAssailantHelmetArmorValue = v; }

    public static int royalAssailantChestplateArmorValue() { return royalAssailantChestplateArmorValue; }
    public static void setRoyalAssailantChestplateArmorValue(int v) { royalAssailantChestplateArmorValue = v; }

    public static int royalAssailantLeggingsArmorValue() { return royalAssailantLeggingsArmorValue; }
    public static void setRoyalAssailantLeggingsArmorValue(int v) { royalAssailantLeggingsArmorValue = v; }

    public static int royalAssailantBootsArmorValue() { return royalAssailantBootsArmorValue; }
    public static void setRoyalAssailantBootsArmorValue(int v) { royalAssailantBootsArmorValue = v; }

    public static double royalAssailantArmorToughness() { return royalAssailantArmorToughness; }
    public static void setRoyalAssailantArmorToughness(double v) { royalAssailantArmorToughness = v; }

    public static double royalAssailantArmorKnockbackResistance() { return royalAssailantArmorKnockbackResistance; }
    public static void setRoyalAssailantArmorKnockbackResistance(double v) { royalAssailantArmorKnockbackResistance = v; }

    public static double royalAssailantHelmetDoubleDamageChance() { return royalAssailantHelmetDoubleDamageChance; }
    public static void setRoyalAssailantHelmetDoubleDamageChance(double v) { royalAssailantHelmetDoubleDamageChance = v; }

    public static double royalAssailantChestplateDoubleDamageChance() { return royalAssailantChestplateDoubleDamageChance; }
    public static void setRoyalAssailantChestplateDoubleDamageChance(double v) { royalAssailantChestplateDoubleDamageChance = v; }

    public static double royalAssailantLeggingsDoubleDamageChance() { return royalAssailantLeggingsDoubleDamageChance; }
    public static void setRoyalAssailantLeggingsDoubleDamageChance(double v) { royalAssailantLeggingsDoubleDamageChance = v; }

    public static double royalAssailantBootsDoubleDamageChance() { return royalAssailantBootsDoubleDamageChance; }
    public static void setRoyalAssailantBootsDoubleDamageChance(double v) { royalAssailantBootsDoubleDamageChance = v; }

    public static double royalGuardianSwordAttackDamage() { return royalGuardianSwordAttackDamage; }
    public static void setRoyalGuardianSwordAttackDamage(double v) { royalGuardianSwordAttackDamage = v; }

    public static double royalGuardianSwordAttackSpeed() { return royalGuardianSwordAttackSpeed; }
    public static void setRoyalGuardianSwordAttackSpeed(double v) { royalGuardianSwordAttackSpeed = v; }

    public static double royalAssailantBattleAxeAttackDamage() { return royalAssailantBattleAxeAttackDamage; }
    public static void setRoyalAssailantBattleAxeAttackDamage(double v) { royalAssailantBattleAxeAttackDamage = v; }

    public static double royalAssailantBattleAxeAttackSpeed() { return royalAssailantBattleAxeAttackSpeed; }
    public static void setRoyalAssailantBattleAxeAttackSpeed(double v) { royalAssailantBattleAxeAttackSpeed = v; }

    public static double royalWeaponAttackReachBonus() { return royalWeaponAttackReachBonus; }
    public static void setRoyalWeaponAttackReachBonus(double v) { royalWeaponAttackReachBonus = v; }

    public static double royalWeaponAttackKnockbackBonus() { return royalWeaponAttackKnockbackBonus; }
    public static void setRoyalWeaponAttackKnockbackBonus(double v) { royalWeaponAttackKnockbackBonus = v; }

    public static int royalWeaponDurability() { return royalWeaponDurability; }
    public static void setRoyalWeaponDurability(int v) { royalWeaponDurability = v; }

    public static int royalWeaponEnchantability() { return royalWeaponEnchantability; }
    public static void setRoyalWeaponEnchantability(int v) { royalWeaponEnchantability = v; }

    public static int royalGuardianShieldDurability() { return royalGuardianShieldDurability; }
    public static void setRoyalGuardianShieldDurability(int v) { royalGuardianShieldDurability = v; }

    public static double royalBoltDamage() { return royalBoltDamage; }
    public static void setRoyalBoltDamage(double v) { royalBoltDamage = v; }

    public static int royalEggHatchChance() { return royalEggHatchChance; }
    public static void setRoyalEggHatchChance(int v) { royalEggHatchChance = v; }

    public static double kingHealth() { return kingHealth; }
    public static void setKingHealth(double v) { kingHealth = v; }

    public static double queenHealth() { return queenHealth; }
    public static void setQueenHealth(double v) { queenHealth = v; }

    public static double kingAttackDamage() { return kingAttackDamage; }
    public static void setKingAttackDamage(double v) { kingAttackDamage = v; }

    public static double queenAttackDamage() { return queenAttackDamage; }
    public static void setQueenAttackDamage(double v) { queenAttackDamage = v; }

    public static double royalBossArmor() { return royalBossArmor; }
    public static void setRoyalBossArmor(double v) { royalBossArmor = v; }

    public static double royalBossFollowRange() { return royalBossFollowRange; }
    public static void setRoyalBossFollowRange(double v) { royalBossFollowRange = v; }

    public static double royalBossMovementSpeed() { return royalBossMovementSpeed; }
    public static void setRoyalBossMovementSpeed(double v) { royalBossMovementSpeed = v; }

    public static double royalBossKnockbackResistance() { return royalBossKnockbackResistance; }
    public static void setRoyalBossKnockbackResistance(double v) { royalBossKnockbackResistance = v; }

    public static double royalBossStepHeight() { return royalBossStepHeight; }
    public static void setRoyalBossStepHeight(double v) { royalBossStepHeight = v; }

    public static double royalBossMaxSingleHitDamage() { return royalBossMaxSingleHitDamage; }
    public static void setRoyalBossMaxSingleHitDamage(double v) { royalBossMaxSingleHitDamage = v; }

    public static double royalBossBiteReach() { return royalBossBiteReach; }
    public static void setRoyalBossBiteReach(double v) { royalBossBiteReach = v; }

    public static double royalBossBiteDamageMultiplier() { return royalBossBiteDamageMultiplier; }
    public static void setRoyalBossBiteDamageMultiplier(double v) { royalBossBiteDamageMultiplier = v; }

    public static int royalBossBiteCooldownTicks() { return royalBossBiteCooldownTicks; }
    public static void setRoyalBossBiteCooldownTicks(int v) { royalBossBiteCooldownTicks = v; }

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

    public static double hoverboardMaxSpeed() { return hoverboardMaxSpeed; }
    public static void setHoverboardMaxSpeed(double value) { hoverboardMaxSpeed = value; }

    public static double hoverboardAcceleration() { return hoverboardAcceleration; }
    public static void setHoverboardAcceleration(double value) { hoverboardAcceleration = value; }

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

    public static double caterpillarHealth() { return caterpillarHealth; }
    public static void setCaterpillarHealth(double value) { caterpillarHealth = value; }

    public static double caterpillarMovementSpeed() { return caterpillarMovementSpeed; }
    public static void setCaterpillarMovementSpeed(double value) { caterpillarMovementSpeed = value; }

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
    public static void    setReverieDamageReactionDurationTicks(int v) { reverieDamageReactionDurationTicks = v; }

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
    public static double  reveriePreferredFollowMinDistance()    { return reveriePreferredFollowMinDistance; }
    public static double  reveriePreferredFollowMaxDistance()    { return reveriePreferredFollowMaxDistance; }
    public static double  reverieCatchUpDistance()               { return reverieCatchUpDistance; }
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

    public static double scorpionWhipSnapBonusDamage() { return scorpionWhipSnapBonusDamage; }
    public static void setScorpionWhipSnapBonusDamage(double value) { scorpionWhipSnapBonusDamage = value; }

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

    public static double bloodCrystalKatanaReachBonus() { return bloodCrystalKatanaReachBonus; }
    public static void setBloodCrystalKatanaReachBonus(double value) { bloodCrystalKatanaReachBonus = value; }

    public static int bloodCrystalKatanaInvulnTicks() { return bloodCrystalKatanaInvulnTicks; }
    public static void setBloodCrystalKatanaInvulnTicks(int value) { bloodCrystalKatanaInvulnTicks = value; }

    public static int bloodCrystalKatanaDashCooldownTicks() { return bloodCrystalKatanaDashCooldownTicks; }
    public static void setBloodCrystalKatanaDashCooldownTicks(int value) { bloodCrystalKatanaDashCooldownTicks = value; }

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

    public static int emperorScorpionMaxNearbyScorpions() { return emperorScorpionMaxNearbyScorpions; }
    public static void setEmperorScorpionMaxNearbyScorpions(int value) { emperorScorpionMaxNearbyScorpions = value; }

    public static int emperorScorpionHardenCooldownTicks() { return emperorScorpionHardenCooldownTicks; }
    public static void setEmperorScorpionHardenCooldownTicks(int value) { emperorScorpionHardenCooldownTicks = value; }

    public static double emperorScorpionDamageRange() { return emperorScorpionDamageRange; }
    public static void setEmperorScorpionDamageRange(double value) { emperorScorpionDamageRange = value; }

    public static int emperorScorpionMinDespawnTicks() { return emperorScorpionMinDespawnTicks; }
    public static void setEmperorScorpionMinDespawnTicks(int value) { emperorScorpionMinDespawnTicks = value; }

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

    public static double bedBugHealth() { return bedBugHealth; }
    public static void setBedBugHealth(double value) { bedBugHealth = value; }

    public static double bedBugAttackDamage() { return bedBugAttackDamage; }
    public static void setBedBugAttackDamage(double value) { bedBugAttackDamage = value; }

    public static double bedBugMovementSpeed() { return bedBugMovementSpeed; }
    public static void setBedBugMovementSpeed(double value) { bedBugMovementSpeed = value; }

    public static double bedBugArmor() { return bedBugArmor; }
    public static void setBedBugArmor(double value) { bedBugArmor = value; }

    public static double jumpyBugHealth() { return jumpyBugHealth; }
    public static void setJumpyBugHealth(double value) { jumpyBugHealth = value; }

    public static double jumpyBugPounceDamage() { return jumpyBugPounceDamage; }
    public static void setJumpyBugPounceDamage(double value) { jumpyBugPounceDamage = value; }

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

    public static double cloudSharkHealth() { return cloudSharkHealth; }
    public static void setCloudSharkHealth(double value) { cloudSharkHealth = value; }

    public static double cloudSharkAttackDamage() { return cloudSharkAttackDamage; }
    public static void setCloudSharkAttackDamage(double value) { cloudSharkAttackDamage = value; }

    public static double cloudSharkMovementSpeed() { return cloudSharkMovementSpeed; }
    public static void setCloudSharkMovementSpeed(double value) { cloudSharkMovementSpeed = value; }

    public static double cloudSharkFlyingSpeed() { return cloudSharkFlyingSpeed; }
    public static void setCloudSharkFlyingSpeed(double value) { cloudSharkFlyingSpeed = value; }

    public static double cloudSharkKnockbackResistance() { return cloudSharkKnockbackResistance; }
    public static void setCloudSharkKnockbackResistance(double value) { cloudSharkKnockbackResistance = value; }

    public static double krakenMovementSpeed() { return krakenMovementSpeed; }
    public static void setKrakenMovementSpeed(double value) { krakenMovementSpeed = value; }

    public static double krakenFlyingSpeed() { return krakenFlyingSpeed; }
    public static void setKrakenFlyingSpeed(double value) { krakenFlyingSpeed = value; }

    public static double krakenKnockbackResistance() { return krakenKnockbackResistance; }
    public static void setKrakenKnockbackResistance(double value) { krakenKnockbackResistance = value; }

    public static double krakenArmor() { return krakenArmor; }
    public static void setKrakenArmor(double value) { krakenArmor = value; }

    public static double krakenBossBarRange() { return krakenBossBarRange; }
    public static void setKrakenBossBarRange(double value) { krakenBossBarRange = value; }

    public static double krakenFollowRange() { return krakenFollowRange; }
    public static void setKrakenFollowRange(double value) { krakenFollowRange = value; }

    public static double krakenDamageRange() { return krakenDamageRange; }
    public static void setKrakenDamageRange(double value) { krakenDamageRange = value; }

    public static double herculesBeetleDamageRange() { return herculesBeetleDamageRange; }
    public static void setHerculesBeetleDamageRange(double value) { herculesBeetleDamageRange = value; }

    public static double toreterrorDamageRange() { return toreterrorDamageRange; }
    public static void setToreterrorDamageRange(double value) { toreterrorDamageRange = value; }

    public static double brutalflyDamageRange() { return brutalflyDamageRange; }
    public static void setBrutalflyDamageRange(double value) { brutalflyDamageRange = value; }

    public static double alphaMantisDamageRange() { return alphaMantisDamageRange; }
    public static void setAlphaMantisDamageRange(double value) { alphaMantisDamageRange = value; }

    public static float krakenLightningDamagePhaseOne() { return krakenLightningDamagePhaseOne; }
    public static void setKrakenLightningDamagePhaseOne(float value) { krakenLightningDamagePhaseOne = value; }

    public static double lucidHealth() { return lucidHealth; }
    public static void setLucidHealth(double value) { lucidHealth = value; }

    public static double lucidAttackDamage() { return lucidAttackDamage; }
    public static void setLucidAttackDamage(double value) { lucidAttackDamage = value; }

    public static double lucidMovementSpeed() { return lucidMovementSpeed; }
    public static void setLucidMovementSpeed(double value) { lucidMovementSpeed = value; }

    public static double lucidFlyingSpeed() { return lucidFlyingSpeed; }
    public static void setLucidFlyingSpeed(double value) { lucidFlyingSpeed = value; }

    public static double lucidKnockbackResistance() { return lucidKnockbackResistance; }
    public static void setLucidKnockbackResistance(double value) { lucidKnockbackResistance = value; }

    public static double vortexHealth() { return vortexHealth; }
    public static void setVortexHealth(double value) { vortexHealth = value; }

    public static double vortexAttackDamage() { return vortexAttackDamage; }
    public static void setVortexAttackDamage(double value) { vortexAttackDamage = value; }

    public static double vortexMovementSpeed() { return vortexMovementSpeed; }
    public static void setVortexMovementSpeed(double value) { vortexMovementSpeed = value; }

    public static double vortexFlyingSpeed() { return vortexFlyingSpeed; }
    public static void setVortexFlyingSpeed(double value) { vortexFlyingSpeed = value; }

    public static int vortexMaxActiveVortexes() { return vortexMaxActiveVortexes; }
    public static void setVortexMaxActiveVortexes(int value) { vortexMaxActiveVortexes = value; }

    public static int windVortexDurationTicks() { return windVortexDurationTicks; }
    public static void setWindVortexDurationTicks(int value) { windVortexDurationTicks = value; }

    public static double windVortexPullStrength() { return windVortexPullStrength; }
    public static void setWindVortexPullStrength(double value) { windVortexPullStrength = value; }

    public static double windVortexLaunchStrength() { return windVortexLaunchStrength; }
    public static void setWindVortexLaunchStrength(double value) { windVortexLaunchStrength = value; }

    public static boolean eyeOfTheStormEnabled() { return eyeOfTheStormEnabled; }
    public static void setEyeOfTheStormEnabled(boolean value) { eyeOfTheStormEnabled = value; }

    public static int eyeOfTheStormUpdraftCooldownTicks() { return eyeOfTheStormUpdraftCooldownTicks; }
    public static void setEyeOfTheStormUpdraftCooldownTicks(int value) { eyeOfTheStormUpdraftCooldownTicks = value; }

    public static double eyeOfTheStormUpdraftLaunchStrength() { return eyeOfTheStormUpdraftLaunchStrength; }
    public static void setEyeOfTheStormUpdraftLaunchStrength(double value) { eyeOfTheStormUpdraftLaunchStrength = value; }

    public static double eyeOfTheStormUpdraftHeight() { return eyeOfTheStormUpdraftHeight; }
    public static void setEyeOfTheStormUpdraftHeight(double value) { eyeOfTheStormUpdraftHeight = value; }

    public static double eyeOfTheStormUpdraftRadius() { return eyeOfTheStormUpdraftRadius; }
    public static void setEyeOfTheStormUpdraftRadius(double value) { eyeOfTheStormUpdraftRadius = value; }

    public static int eyeOfTheStormUpdraftDurationTicks() { return eyeOfTheStormUpdraftDurationTicks; }
    public static void setEyeOfTheStormUpdraftDurationTicks(int value) { eyeOfTheStormUpdraftDurationTicks = value; }

    public static int eyeOfTheStormSurgeCooldownTicks() { return eyeOfTheStormSurgeCooldownTicks; }
    public static void setEyeOfTheStormSurgeCooldownTicks(int value) { eyeOfTheStormSurgeCooldownTicks = value; }

    public static double eyeOfTheStormSurgeRange() { return eyeOfTheStormSurgeRange; }
    public static void setEyeOfTheStormSurgeRange(double value) { eyeOfTheStormSurgeRange = value; }

    public static int eyeOfTheStormSurgeDurationTicks() { return eyeOfTheStormSurgeDurationTicks; }
    public static void setEyeOfTheStormSurgeDurationTicks(int value) { eyeOfTheStormSurgeDurationTicks = value; }

    public static double eyeOfTheStormSurgeRadius() { return eyeOfTheStormSurgeRadius; }
    public static void setEyeOfTheStormSurgeRadius(double value) { eyeOfTheStormSurgeRadius = value; }

    public static double eyeOfTheStormSurgeHeight() { return eyeOfTheStormSurgeHeight; }
    public static void setEyeOfTheStormSurgeHeight(double value) { eyeOfTheStormSurgeHeight = value; }

    public static double eyeOfTheStormSurgePullStrength() { return eyeOfTheStormSurgePullStrength; }
    public static void setEyeOfTheStormSurgePullStrength(double value) { eyeOfTheStormSurgePullStrength = value; }

    public static double eyeOfTheStormSurgeReturnStrength() { return eyeOfTheStormSurgeReturnStrength; }
    public static void setEyeOfTheStormSurgeReturnStrength(double value) { eyeOfTheStormSurgeReturnStrength = value; }

    public static float eyeOfTheStormSurgeDamage() { return eyeOfTheStormSurgeDamage; }
    public static void setEyeOfTheStormSurgeDamage(float value) { eyeOfTheStormSurgeDamage = value; }

    public static double vortexLensMinRadius() { return vortexLensMinRadius; }
    public static void setVortexLensMinRadius(double value) { vortexLensMinRadius = value; }

    public static double vortexLensMaxRadius() { return vortexLensMaxRadius; }
    public static void setVortexLensMaxRadius(double value) { vortexLensMaxRadius = value; }

    public static double vortexLensMinHeight() { return vortexLensMinHeight; }
    public static void setVortexLensMinHeight(double value) { vortexLensMinHeight = value; }

    public static double vortexLensMaxHeight() { return vortexLensMaxHeight; }
    public static void setVortexLensMaxHeight(double value) { vortexLensMaxHeight = value; }

    public static double vortexLensPullStrength() { return vortexLensPullStrength; }
    public static void setVortexLensPullStrength(double value) { vortexLensPullStrength = value; }

    public static double vortexLensPushStrength() { return vortexLensPushStrength; }
    public static void setVortexLensPushStrength(double value) { vortexLensPushStrength = value; }

    public static double vortexLensLaunchStrength() { return vortexLensLaunchStrength; }
    public static void setVortexLensLaunchStrength(double value) { vortexLensLaunchStrength = value; }

    public static double missileSquidHealth() { return missileSquidHealth; }
    public static void setMissileSquidHealth(double value) { missileSquidHealth = value; }

    public static double missileSquidAttackDamage() { return missileSquidAttackDamage; }
    public static void setMissileSquidAttackDamage(double value) { missileSquidAttackDamage = value; }

    public static double missileSquidMovementSpeed() { return missileSquidMovementSpeed; }
    public static void setMissileSquidMovementSpeed(double value) { missileSquidMovementSpeed = value; }

    public static double missileSquidFlyingSpeed() { return missileSquidFlyingSpeed; }
    public static void setMissileSquidFlyingSpeed(double value) { missileSquidFlyingSpeed = value; }

    public static double missileSquidKnockbackResistance() { return missileSquidKnockbackResistance; }
    public static void setMissileSquidKnockbackResistance(double value) { missileSquidKnockbackResistance = value; }

    public static double molewormHealth() { return molewormHealth; }
    public static void setMolewormHealth(double value) { molewormHealth = value; }

    public static double molewormAttackDamage() { return molewormAttackDamage; }
    public static void setMolewormAttackDamage(double value) { molewormAttackDamage = value; }

    public static double molewormMovementSpeed() { return molewormMovementSpeed; }
    public static void setMolewormMovementSpeed(double value) { molewormMovementSpeed = value; }

    public static double triffidHealth() { return triffidHealth; }
    public static void setTriffidHealth(double value) { triffidHealth = value; }

    public static double triffidAttackDamage() { return triffidAttackDamage; }
    public static void setTriffidAttackDamage(double value) { triffidAttackDamage = value; }

    public static double triffidGrabDamage() { return triffidGrabDamage; }
    public static void setTriffidGrabDamage(double value) { triffidGrabDamage = value; }

    public static int triffidSweepHitCooldownTicks() { return triffidSweepHitCooldownTicks; }
    public static void setTriffidSweepHitCooldownTicks(int value) { triffidSweepHitCooldownTicks = value; }

    public static double molevoreHealth() { return molevoreHealth; }
    public static void setMolevoreHealth(double value) { molevoreHealth = value; }

    public static double molevoreAttackDamage() { return molevoreAttackDamage; }
    public static void setMolevoreAttackDamage(double value) { molevoreAttackDamage = value; }

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

    public static int nightmareDreadTicks() { return nightmareDreadTicks; }
    public static void setNightmareDreadTicks(int value) { nightmareDreadTicks = value; }

    public static int nightmareWeaknessTicks() { return nightmareWeaknessTicks; }
    public static void setNightmareWeaknessTicks(int value) { nightmareWeaknessTicks = value; }

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

    public static double creepingHorrorHealth() { return creepingHorrorHealth; }
    public static void setCreepingHorrorHealth(double v) { creepingHorrorHealth = v; }
    public static double creepingHorrorAttackDamage() { return creepingHorrorAttackDamage; }
    public static void setCreepingHorrorAttackDamage(double v) { creepingHorrorAttackDamage = v; }
    public static double lurkingTerrorHealth() { return lurkingTerrorHealth; }
    public static void setLurkingTerrorHealth(double v) { lurkingTerrorHealth = v; }
    public static double lurkingTerrorAttackDamage() { return lurkingTerrorAttackDamage; }
    public static void setLurkingTerrorAttackDamage(double v) { lurkingTerrorAttackDamage = v; }
    public static double manticoreHealth() { return manticoreHealth; }
    public static void setManticoreHealth(double v) { manticoreHealth = v; }
    public static double manticoreAttackDamage() { return manticoreAttackDamage; }
    public static void setManticoreAttackDamage(double v) { manticoreAttackDamage = v; }
    public static int manticoreStingPoisonTicks() { return manticoreStingPoisonTicks; }
    public static void setManticoreStingPoisonTicks(int v) { manticoreStingPoisonTicks = v; }
    public static int queenManticoreCap() { return queenManticoreCap; }
    public static void setQueenManticoreCap(int v) { queenManticoreCap = v; }
    public static double jerryInfantHealth() { return jerryInfantHealth; }
    public static void setJerryInfantHealth(double v) { jerryInfantHealth = v; }
    public static double jerryInfantAttackDamage() { return jerryInfantAttackDamage; }
    public static void setJerryInfantAttackDamage(double v) { jerryInfantAttackDamage = v; }
    public static double jerryMatureHealth() { return jerryMatureHealth; }
    public static void setJerryMatureHealth(double v) { jerryMatureHealth = v; }
    public static double jerryMatureAttackDamage() { return jerryMatureAttackDamage; }
    public static void setJerryMatureAttackDamage(double v) { jerryMatureAttackDamage = v; }
    public static double jerryAlphaHealth() { return jerryAlphaHealth; }
    public static void setJerryAlphaHealth(double v) { jerryAlphaHealth = v; }
    public static double jerryAlphaAttackDamage() { return jerryAlphaAttackDamage; }
    public static void setJerryAlphaAttackDamage(double v) { jerryAlphaAttackDamage = v; }
    public static double jerryGammaHealth() { return jerryGammaHealth; }
    public static void setJerryGammaHealth(double v) { jerryGammaHealth = v; }
    public static double jerryGammaAttackDamage() { return jerryGammaAttackDamage; }
    public static void setJerryGammaAttackDamage(double v) { jerryGammaAttackDamage = v; }
    public static double cheepHealth() { return cheepHealth; }
    public static void setCheepHealth(double v) { cheepHealth = v; }
    public static double cheepAttackDamage() { return cheepAttackDamage; }
    public static void setCheepAttackDamage(double v) { cheepAttackDamage = v; }
    public static double dorrieHealth() { return dorrieHealth; }
    public static double herculesBeetleHealth() { return herculesBeetleHealth; }
    public static void setHerculesBeetleHealth(double v) { herculesBeetleHealth = v; }
    public static double herculesBeetleAttackDamage() { return herculesBeetleAttackDamage; }
    public static void setHerculesBeetleAttackDamage(double v) { herculesBeetleAttackDamage = v; }
    public static double herculesBeetleChargeDamage() { return herculesBeetleChargeDamage; }
    public static void setHerculesBeetleChargeDamage(double v) { herculesBeetleChargeDamage = v; }
    public static void setDorrieHealth(double v) { dorrieHealth = v; }
    public static double ouranwoodDeerHealth() { return ouranwoodDeerHealth; }
    public static void setOuranwoodDeerHealth(double v) { ouranwoodDeerHealth = v; }
    public static double glimmerHealth() { return glimmerHealth; }
    public static void setGlimmerHealth(double v) { glimmerHealth = v; }

    public static double lucidArmorToughness() { return lucidArmorToughness; }
    public static double triffidArmorToughness() { return triffidArmorToughness; }
    public static double basiliskArmorToughness() { return basiliskArmorToughness; }
    public static double nightmareMobArmorToughness() { return nightmareMobArmorToughness; }
    public static double alphaMantisArmorToughness() { return alphaMantisArmorToughness; }
    public static double herculesBeetleArmorToughness() { return herculesBeetleArmorToughness; }
    public static double krakenArmorToughness() { return krakenArmorToughness; }
    public static double nightmareMobArmor() { return nightmareMobArmor; }

    public static double bossMagicPerHitCapFraction() { return bossMagicPerHitCapFraction; }
    public static int bossMagicWindowTicks() { return bossMagicWindowTicks; }
    public static double bossMagicWindowCapFraction() { return bossMagicWindowCapFraction; }
    public static double bossMagicWardReductionFraction() { return bossMagicWardReductionFraction; }
    public static int bossMagicWardDurationTicks() { return bossMagicWardDurationTicks; }
    public static int bossMagicWardTriggerBreaches() { return bossMagicWardTriggerBreaches; }
    public static java.util.List<String> magicBurstDamageNamespaces() { return magicBurstDamageNamespaces; }
}
