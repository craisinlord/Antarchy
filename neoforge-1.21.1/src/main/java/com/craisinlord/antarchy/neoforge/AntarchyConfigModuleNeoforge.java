package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.network.ImpactShakeSync;
import com.craisinlord.antarchy.config.ConfigResetGuard;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakePayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakeSync;
import com.craisinlord.antarchy.content.network.KingJudgmentFlashPayload;
import com.craisinlord.antarchy.content.network.KingJudgmentFlashSync;
import com.craisinlord.antarchy.content.network.HordeIntensitySync;
import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

public final class AntarchyConfigModuleNeoforge {
    private AntarchyConfigModuleNeoforge() {}

    public static void init(ModContainer modContainer) {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("antarchy");
        ConfigResetGuard.migrateIfNeeded(
                configDir,
                configDir.resolve("antarchy_mobs.toml"),
                configDir.resolve("antarchy_tools.toml"),
                configDir.resolve("antarchy_misc.toml")
        );
        modContainer.registerConfig(ModConfig.Type.COMMON, AntarchyMobsConfig.SPEC,  "antarchy/antarchy_mobs.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, AntarchyToolsConfig.SPEC, "antarchy/antarchy_tools.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, AntarchyMiscConfig.SPEC,  "antarchy/antarchy_misc.toml");
        HerculesBeetleImpactShakeSync.setSink((player, ticks) -> PacketDistributor.sendToPlayer(player, new HerculesBeetleImpactShakePayload(ticks)));
        KingJudgmentFlashSync.setSink((player, ticks) -> PacketDistributor.sendToPlayer(player, new KingJudgmentFlashPayload(ticks)));
        ImpactShakeSync.setSink(PacketDistributor::sendToPlayer);
        HordeIntensitySync.setSink(PacketDistributor::sendToPlayer);
        AntarchyNeoforge.modEventBusTempHolder.addListener(AntarchyConfigModuleNeoforge::onConfigChange);
    }

    private static void onConfigChange(ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Reloading) {
            return;
        }
        var spec = event.getConfig().getSpec();
        if (spec == AntarchyMobsConfig.SPEC) {
            bakeMobsConfig();
            return;
        }

        if (spec == AntarchyToolsConfig.SPEC) {
            bakeToolsConfig();
            return;
        }

        if (spec == AntarchyMiscConfig.SPEC) {
            bakeMiscConfig();
        }
    }

    private static void bakeMobsConfig() {
        AntarchySettings.setAntsStealFromChests(AntarchyMobsConfig.antsStealFromChests());

        AntarchySettings.setBrownAntRequiresReagent(AntarchyMobsConfig.brownAntRequiresReagent());
        AntarchySettings.setBrownAntRightClickDimension(AntarchyMobsConfig.brownAntRightClickDimension());
        AntarchySettings.setBrownAntHealth(AntarchyMobsConfig.brownAntHealth());
        AntarchySettings.setBrownAntMovementSpeed(AntarchyMobsConfig.brownAntMovementSpeed());
        AntarchySettings.setBrownAntAttackDamage(AntarchyMobsConfig.brownAntAttackDamage());
        AntarchySettings.setBrownAntDestinationDimension(AntarchyMobsConfig.brownAntDestinationDimension());

        AntarchySettings.setRedAntRequiresReagent(AntarchyMobsConfig.redAntRequiresReagent());
        AntarchySettings.setRedAntRightClickDimension(AntarchyMobsConfig.redAntRightClickDimension());
        AntarchySettings.setRedAntHealth(AntarchyMobsConfig.redAntHealth());
        AntarchySettings.setRedAntMovementSpeed(AntarchyMobsConfig.redAntMovementSpeed());
        AntarchySettings.setRedAntAttackDamage(AntarchyMobsConfig.redAntAttackDamage());
        AntarchySettings.setRedAntDestinationDimension(AntarchyMobsConfig.redAntDestinationDimension());

        AntarchySettings.setRainbowAntRequiresReagent(AntarchyMobsConfig.rainbowAntRequiresReagent());
        AntarchySettings.setRainbowAntRightClickDimension(AntarchyMobsConfig.rainbowAntRightClickDimension());
        AntarchySettings.setRainbowAntHealth(AntarchyMobsConfig.rainbowAntHealth());
        AntarchySettings.setRainbowAntMovementSpeed(AntarchyMobsConfig.rainbowAntMovementSpeed());
        AntarchySettings.setRainbowAntAttackDamage(AntarchyMobsConfig.rainbowAntAttackDamage());
        AntarchySettings.setRainbowAntNonInfinityFallbackDimension(AntarchyMobsConfig.rainbowAntNonInfinityFallbackDimension());

        AntarchySettings.setTermiteRequiresReagent(AntarchyMobsConfig.termiteRequiresReagent());
        AntarchySettings.setTermiteRightClickDimension(AntarchyMobsConfig.termiteRightClickDimension());
        AntarchySettings.setTermiteHealth(AntarchyMobsConfig.termiteHealth());
        AntarchySettings.setTermiteMovementSpeed(AntarchyMobsConfig.termiteMovementSpeed());
        AntarchySettings.setTermiteAttackDamage(AntarchyMobsConfig.termiteAttackDamage());
        AntarchySettings.setTermiteDestinationDimension(AntarchyMobsConfig.termiteDestinationDimension());

        AntarchySettings.setEasterBunnyEnabled(AntarchyMobsConfig.easterBunnyEnabled());
        AntarchySettings.setEasterBunnyNaturalSpawnChancePercent(AntarchyMobsConfig.easterBunnyNaturalSpawnChancePercent());

        AntarchySettings.setWaspHealth(AntarchyMobsConfig.waspHealth());
        AntarchySettings.setWaspAttackDamage(AntarchyMobsConfig.waspAttackDamage());
        AntarchySettings.setWaspMovementSpeed(AntarchyMobsConfig.waspMovementSpeed());

        AntarchySettings.setBomberHealth(AntarchyMobsConfig.bomberHealth());
        AntarchySettings.setBomberAttackDamage(AntarchyMobsConfig.bomberAttackDamage());
        AntarchySettings.setBomberExplosionDamage(AntarchyMobsConfig.bomberExplosionDamage());
        AntarchySettings.setBomberExplosionRadius(AntarchyMobsConfig.bomberExplosionRadius());

        AntarchySettings.setSpringbugHealth(AntarchyMobsConfig.springbugHealth());
        AntarchySettings.setSpringbugPounceDamage(AntarchyMobsConfig.springbugPounceDamage());
        AntarchySettings.setSpringbugCamouflageAlpha(AntarchyMobsConfig.springbugCamouflageAlpha());

        AntarchySettings.setEmperorScorpionHealth(AntarchyMobsConfig.emperorScorpionHealth());
        AntarchySettings.setEmperorScorpionAttackDamage(AntarchyMobsConfig.emperorScorpionAttackDamage());
        AntarchySettings.setEmperorScorpionMovementSpeed(AntarchyMobsConfig.emperorScorpionMovementSpeed());
        AntarchySettings.setEmperorScorpionArmor(AntarchyMobsConfig.emperorScorpionArmor());
        AntarchySettings.setEmperorScorpionKnockbackResistance(AntarchyMobsConfig.emperorScorpionKnockbackResistance());
        AntarchySettings.setEmperorScorpionFollowRange(AntarchyMobsConfig.emperorScorpionFollowRange());
        AntarchySettings.setEmperorScorpionXpReward(AntarchyMobsConfig.emperorScorpionXpReward());
        AntarchySettings.setEmperorScorpionClawAnimTicks(AntarchyMobsConfig.emperorScorpionClawAnimTicks());
        AntarchySettings.setEmperorScorpionClawHitTick(AntarchyMobsConfig.emperorScorpionClawHitTick());
        AntarchySettings.setEmperorScorpionClawCooldownTicks(AntarchyMobsConfig.emperorScorpionClawCooldownTicks());
        AntarchySettings.setEmperorScorpionStingAnimTicks(AntarchyMobsConfig.emperorScorpionStingAnimTicks());
        AntarchySettings.setEmperorScorpionStingHitTick(AntarchyMobsConfig.emperorScorpionStingHitTick());
        AntarchySettings.setEmperorScorpionStingCooldownTicks(AntarchyMobsConfig.emperorScorpionStingCooldownTicks());
        AntarchySettings.setEmperorScorpionPoisonTicks(AntarchyMobsConfig.emperorScorpionPoisonTicks());
        AntarchySettings.setEmperorScorpionWeaknessTicks(AntarchyMobsConfig.emperorScorpionWeaknessTicks());
        AntarchySettings.setEmperorScorpionSummonIntervalTicks(AntarchyMobsConfig.emperorScorpionSummonIntervalTicks());
        AntarchySettings.setEmperorScorpionMaxSummonedScorpions(AntarchyMobsConfig.emperorScorpionMaxSummonedScorpions());
        AntarchySettings.setEmperorScorpionMaxNearbyScorpions(AntarchyMobsConfig.emperorScorpionMaxNearbyScorpions());
        AntarchySettings.setEmperorScorpionHardenCooldownTicks(AntarchyMobsConfig.emperorScorpionHardenCooldownTicks());
        AntarchySettings.setEmperorScorpionDamageRange(AntarchyMobsConfig.emperorScorpionDamageRange());
        AntarchySettings.setEmperorScorpionMinDespawnTicks(AntarchyMobsConfig.emperorScorpionMinDespawnTicks());

        AntarchySettings.setScorpionHealth(AntarchyMobsConfig.scorpionHealth());
        AntarchySettings.setScorpionAttackDamage(AntarchyMobsConfig.scorpionAttackDamage());
        AntarchySettings.setScorpionMovementSpeed(AntarchyMobsConfig.scorpionMovementSpeed());
        AntarchySettings.setScorpionArmor(AntarchyMobsConfig.scorpionArmor());
        AntarchySettings.setScorpionKnockbackResistance(AntarchyMobsConfig.scorpionKnockbackResistance());

        AntarchySettings.setKrakenHealth(AntarchyMobsConfig.krakenHealth());
        AntarchySettings.setKrakenAttackDamage(AntarchyMobsConfig.krakenAttackDamage());
        AntarchySettings.setKrakenSquidSpawnEnabled(AntarchyMobsConfig.krakenSquidSpawnEnabled());
        AntarchySettings.setKrakenMassSpawnEnabled(AntarchyMobsConfig.krakenMassSpawnEnabled());
        AntarchySettings.setKrakenRequireBadOmenToSummon(AntarchyMobsConfig.krakenRequireBadOmenToSummon());
        AntarchySettings.setKrakenFollowRange(AntarchyMobsConfig.krakenFollowRange());
        AntarchySettings.setKrakenDamageRange(AntarchyMobsConfig.krakenDamageRange());
        AntarchySettings.setKrakenArmor(AntarchyMobsConfig.krakenArmor());
        AntarchySettings.setKrakenKnockbackResistance(AntarchyMobsConfig.krakenKnockbackResistance());
        AntarchySettings.setKrakenMovementSpeed(AntarchyMobsConfig.krakenMovementSpeed());
        AntarchySettings.setKrakenFlyingSpeed(AntarchyMobsConfig.krakenFlyingSpeed());
        AntarchySettings.setKrakenBossBarRange(AntarchyMobsConfig.krakenBossBarRange());
        AntarchySettings.setKrakenLightningDamagePhaseOne(AntarchyMobsConfig.krakenLightningDamagePhaseOne());

        AntarchySettings.setKingHealth(AntarchyMobsConfig.kingHealth());
        AntarchySettings.setQueenHealth(AntarchyMobsConfig.queenHealth());
        AntarchySettings.setKingAttackDamage(AntarchyMobsConfig.kingAttackDamage());
        AntarchySettings.setQueenAttackDamage(AntarchyMobsConfig.queenAttackDamage());
        AntarchySettings.setRoyalBossArmor(AntarchyMobsConfig.royalBossArmor());
        AntarchySettings.setKingArmor(AntarchyMobsConfig.kingArmor());
        AntarchySettings.setRoyalBossFollowRange(AntarchyMobsConfig.royalBossFollowRange());
        AntarchySettings.setRoyalBossMovementSpeed(AntarchyMobsConfig.royalBossMovementSpeed());
        AntarchySettings.setRoyalBossKnockbackResistance(AntarchyMobsConfig.royalBossKnockbackResistance());
        AntarchySettings.setRoyalBossStepHeight(AntarchyMobsConfig.royalBossStepHeight());
        AntarchySettings.setRoyalBossMaxSingleHitDamage(AntarchyMobsConfig.royalBossMaxSingleHitDamage());
        AntarchySettings.setRoyalBossBiteReach(AntarchyMobsConfig.royalBossBiteReach());
        AntarchySettings.setRoyalBossBiteDamageMultiplier(AntarchyMobsConfig.royalBossBiteDamageMultiplier());
        AntarchySettings.setRoyalBossBiteCooldownTicks(AntarchyMobsConfig.royalBossBiteCooldownTicks());
        AntarchySettings.setRoyalBossMultiplayerScalingEnabled(AntarchyMobsConfig.royalBossMultiplayerScalingEnabled());
        AntarchySettings.setRoyalBossScalingMaxPlayers(AntarchyMobsConfig.royalBossScalingMaxPlayers());
        AntarchySettings.setRoyalBossHealthPerAdditionalPlayer(AntarchyMobsConfig.royalBossHealthPerAdditionalPlayer());
        AntarchySettings.setRoyalBossDamagePerAdditionalPlayer(AntarchyMobsConfig.royalBossDamagePerAdditionalPlayer());
        AntarchySettings.setRoyalBoundaryRadius(AntarchyMobsConfig.royalBoundaryRadius());
        AntarchySettings.setRoyalBoundaryWarningRadius(AntarchyMobsConfig.royalBoundaryWarningRadius());
        AntarchySettings.setRoyalBoundaryGraceTicks(AntarchyMobsConfig.royalBoundaryGraceTicks());
        AntarchySettings.setQueenBeamDamage(AntarchyMobsConfig.queenBeamDamage());
        AntarchySettings.setQueenBeamRange(AntarchyMobsConfig.queenBeamRange());
        AntarchySettings.setQueenBeamMinimumRange(AntarchyMobsConfig.queenBeamMinimumRange());
        AntarchySettings.setQueenBeamDurationTicks(AntarchyMobsConfig.queenBeamDurationTicks());
        AntarchySettings.setQueenBeamWindupTicks(AntarchyMobsConfig.queenBeamWindupTicks());
        AntarchySettings.setQueenBeamTravelTicks(AntarchyMobsConfig.queenBeamTravelTicks());
        AntarchySettings.setQueenBeamCooldownTicks(AntarchyMobsConfig.queenBeamCooldownTicks());
        AntarchySettings.setQueenBeamTracking(AntarchyMobsConfig.queenBeamTracking());
        AntarchySettings.setQueenBeamTerrainRadius(AntarchyMobsConfig.queenBeamTerrainRadius());
        AntarchySettings.setQueenBeamTerrainCap(AntarchyMobsConfig.queenBeamTerrainCap());
        AntarchySettings.setQueenBeamPhaseOneCap(AntarchyMobsConfig.queenBeamPhaseOneCap());
        AntarchySettings.setQueenBeamPhaseTwoCap(AntarchyMobsConfig.queenBeamPhaseTwoCap());
        AntarchySettings.setQueenBeamPhaseThreeCap(AntarchyMobsConfig.queenBeamPhaseThreeCap());
        AntarchySettings.setQueenBeamPhaseThreeMaxVolleyChance(AntarchyMobsConfig.queenBeamPhaseThreeMaxVolleyChance());
        AntarchySettings.setKingBeamDamage(AntarchyMobsConfig.kingBeamDamage());
        AntarchySettings.setKingBeamRange(AntarchyMobsConfig.kingBeamRange());
        AntarchySettings.setKingBeamMinimumRange(AntarchyMobsConfig.kingBeamMinimumRange());
        AntarchySettings.setKingBeamDurationTicks(AntarchyMobsConfig.kingBeamDurationTicks());
        AntarchySettings.setKingBeamWindupTicks(AntarchyMobsConfig.kingBeamWindupTicks());
        AntarchySettings.setKingBeamTravelTicks(AntarchyMobsConfig.kingBeamTravelTicks());
        AntarchySettings.setKingBeamCooldownTicks(AntarchyMobsConfig.kingBeamCooldownTicks());
        AntarchySettings.setKingBeamTracking(AntarchyMobsConfig.kingBeamTracking());
        AntarchySettings.setKingBeamTerrainRadius(AntarchyMobsConfig.kingBeamTerrainRadius());
        AntarchySettings.setKingBeamTerrainCap(AntarchyMobsConfig.kingBeamTerrainCap());
        AntarchySettings.setRoyalDecreeCooldownTicks(AntarchyMobsConfig.royalDecreeCooldownTicks());
        AntarchySettings.setKingRoyalMusterCooldownTicks(AntarchyMobsConfig.kingRoyalMusterCooldownTicks());
        AntarchySettings.setKingRoyalMusterWindupTicks(AntarchyMobsConfig.kingRoyalMusterWindupTicks());
        AntarchySettings.setKingRoyalMusterDurationTicks(AntarchyMobsConfig.kingRoyalMusterDurationTicks());
        AntarchySettings.setKingRoyalMusterRadius(AntarchyMobsConfig.kingRoyalMusterRadius());
        AntarchySettings.setKingRoyalMusterCap(AntarchyMobsConfig.kingRoyalMusterCap());
        AntarchySettings.setQueenBlackHoleRadius(AntarchyMobsConfig.queenBlackHoleRadius());
        AntarchySettings.setQueenBlackHoleActiveTicks(AntarchyMobsConfig.queenBlackHoleActiveTicks());
        AntarchySettings.setQueenBlackHolePullStrength(AntarchyMobsConfig.queenBlackHolePullStrength());
        AntarchySettings.setQueenBlackHoleBlockSuctionCap(AntarchyMobsConfig.queenBlackHoleBlockSuctionCap());
        AntarchySettings.setKingFireballDamage(AntarchyMobsConfig.kingFireballDamage());
        AntarchySettings.setKingFireballRadius(AntarchyMobsConfig.kingFireballRadius());
        AntarchySettings.setKingFireballCooldownTicks(AntarchyMobsConfig.kingFireballCooldownTicks());
        AntarchySettings.setKingIceballDamage(AntarchyMobsConfig.kingIceballDamage());
        AntarchySettings.setKingIceballRadius(AntarchyMobsConfig.kingIceballRadius());
        AntarchySettings.setKingIceballCooldownTicks(AntarchyMobsConfig.kingIceballCooldownTicks());
        AntarchySettings.setKingIceSpikeDamage(AntarchyMobsConfig.kingIceSpikeDamage());
        AntarchySettings.setKingIceSpikeCooldownTicks(AntarchyMobsConfig.kingIceSpikeCooldownTicks());
        AntarchySettings.setKingElementalTerrainCap(AntarchyMobsConfig.kingElementalTerrainCap());
        AntarchySettings.setRoyalBossSoundVolume(AntarchyMobsConfig.royalBossSoundVolume());

        AntarchySettings.setRoyalEggHatchChance(AntarchyMobsConfig.royalEggHatchChance());
        AntarchySettings.setRoyalBoltDamage(AntarchyMobsConfig.royalBoltDamage());
        AntarchySettings.setPrinceHealth(AntarchyMobsConfig.princeHealth());
        AntarchySettings.setPrinceAttackDamage(AntarchyMobsConfig.princeAttackDamage());
        AntarchySettings.setPrinceMovementSpeed(AntarchyMobsConfig.princeMovementSpeed());
        AntarchySettings.setPrinceFlyingSpeed(AntarchyMobsConfig.princeFlyingSpeed());
        AntarchySettings.setPrinceArmor(AntarchyMobsConfig.princeArmor());
        AntarchySettings.setPrinceKnockbackResistance(AntarchyMobsConfig.princeKnockbackResistance());
        AntarchySettings.setPrinceFollowRange(AntarchyMobsConfig.princeFollowRange());
        AntarchySettings.setPrincessHealth(AntarchyMobsConfig.princessHealth());
        AntarchySettings.setPrincessAttackDamage(AntarchyMobsConfig.princessAttackDamage());
        AntarchySettings.setPrincessMovementSpeed(AntarchyMobsConfig.princessMovementSpeed());
        AntarchySettings.setPrincessFlyingSpeed(AntarchyMobsConfig.princessFlyingSpeed());
        AntarchySettings.setPrincessArmor(AntarchyMobsConfig.princessArmor());
        AntarchySettings.setPrincessKnockbackResistance(AntarchyMobsConfig.princessKnockbackResistance());
        AntarchySettings.setPrincessFollowRange(AntarchyMobsConfig.princessFollowRange());

        AntarchySettings.setBrutalflyHealth(AntarchyMobsConfig.brutalflyHealth());
        AntarchySettings.setBrutalflySwipeDamage(AntarchyMobsConfig.brutalflySwipeDamage());
        AntarchySettings.setBrutalflySpitDamage(AntarchyMobsConfig.brutalflySpitDamage());
        AntarchySettings.setBrutalflyDamageRange(AntarchyMobsConfig.brutalflyDamageRange());
        AntarchySettings.setBrutalflyArmor(AntarchyMobsConfig.brutalflyArmor());
        AntarchySettings.setBrutalflyArmorToughness(AntarchyMobsConfig.brutalflyArmorToughness());
        AntarchySettings.setBrutalflyKnockbackResistance(AntarchyMobsConfig.brutalflyKnockbackResistance());
        AntarchySettings.setBrutalflyMovementSpeed(AntarchyMobsConfig.brutalflyMovementSpeed());
        AntarchySettings.setBrutalflyFlyingSpeed(AntarchyMobsConfig.brutalflyFlyingSpeed());

        AntarchySettings.setMantisHealth(AntarchyMobsConfig.mantisHealth());
        AntarchySettings.setMantisAttackDamage(AntarchyMobsConfig.mantisAttackDamage());
        AntarchySettings.setMantisMovementSpeed(AntarchyMobsConfig.mantisMovementSpeed());
        AntarchySettings.setMantisFlyingSpeed(AntarchyMobsConfig.mantisFlyingSpeed());
        AntarchySettings.setMantisIgnoreLightLevel(AntarchyMobsConfig.mantisIgnoreLightLevel());

        AntarchySettings.setAlphaMantisHealth(AntarchyMobsConfig.alphaMantisHealth());
        AntarchySettings.setAlphaMantisAttackDamage(AntarchyMobsConfig.alphaMantisAttackDamage());
        AntarchySettings.setAlphaMantisMovementSpeed(AntarchyMobsConfig.alphaMantisMovementSpeed());
        AntarchySettings.setAlphaMantisFlyingSpeed(AntarchyMobsConfig.alphaMantisFlyingSpeed());
        AntarchySettings.setAlphaMantisSummonIntervalTicks(AntarchyMobsConfig.alphaMantisSummonIntervalTicks());
        AntarchySettings.setAlphaMantisMaxMinions(AntarchyMobsConfig.alphaMantisMaxMinions());
        AntarchySettings.setAlphaMantisDamageRange(AntarchyMobsConfig.alphaMantisDamageRange());

        AntarchySettings.setRollyPollyHealth(AntarchyMobsConfig.rollyPollyHealth());
        AntarchySettings.setRollyPollyMovementSpeed(AntarchyMobsConfig.rollyPollyMovementSpeed());
        AntarchySettings.setRollyPollyRollSpeedMultiplier(AntarchyMobsConfig.rollyPollyRollSpeedMultiplier());
        AntarchySettings.setRollyPollyTameChance(AntarchyMobsConfig.rollyPollyTameChance());
        AntarchySettings.setRollyPollyBowlingDamage(AntarchyMobsConfig.rollyPollyBowlingDamage());
        AntarchySettings.setRollyPollyBowlingKnockback(AntarchyMobsConfig.rollyPollyBowlingKnockback());
        AntarchySettings.setRollyPollyArmor(AntarchyMobsConfig.rollyPollyArmor());
        AntarchySettings.setRollyPollyRolledArmorBonus(AntarchyMobsConfig.rollyPollyRolledArmorBonus());

        AntarchySettings.setFlytrapHealth(AntarchyMobsConfig.flytrapHealth());
        AntarchySettings.setFlytrapAttackDamage(AntarchyMobsConfig.flytrapAttackDamage());
        AntarchySettings.setFlytrapGrabDamage(AntarchyMobsConfig.flytrapGrabDamage());
        AntarchySettings.setFlytrapSweepHitCooldownTicks(AntarchyMobsConfig.flytrapSweepHitCooldownTicks());

        AntarchySettings.setCaterpillarHealth(AntarchyMobsConfig.caterpillarHealth());
        AntarchySettings.setCaterpillarMovementSpeed(AntarchyMobsConfig.caterpillarMovementSpeed());
        AntarchySettings.setCaterpillarPupationTimeSeconds(AntarchyMobsConfig.caterpillarPupationTimeSeconds());
        AntarchySettings.setFlyingSquirrelHealth(AntarchyMobsConfig.flyingSquirrelHealth());
        AntarchySettings.setFlyingSquirrelMovementSpeed(AntarchyMobsConfig.flyingSquirrelMovementSpeed());
        AntarchySettings.setButterflyHealth(AntarchyMobsConfig.butterflyHealth());
        AntarchySettings.setButterflyMovementSpeed(AntarchyMobsConfig.butterflyMovementSpeed());
        AntarchySettings.setButterflyFlyingSpeed(AntarchyMobsConfig.butterflyFlyingSpeed());
        AntarchySettings.setBedBugHealth(AntarchyMobsConfig.bedBugHealth());
        AntarchySettings.setBedBugAttackDamage(AntarchyMobsConfig.bedBugAttackDamage());
        AntarchySettings.setBedBugMovementSpeed(AntarchyMobsConfig.bedBugMovementSpeed());
        AntarchySettings.setBedBugArmor(AntarchyMobsConfig.bedBugArmor());

        AntarchySettings.setStratosharkHealth(AntarchyMobsConfig.stratosharkHealth());
        AntarchySettings.setStratosharkAttackDamage(AntarchyMobsConfig.stratosharkAttackDamage());
        AntarchySettings.setStratosharkMovementSpeed(AntarchyMobsConfig.stratosharkMovementSpeed());
        AntarchySettings.setStratosharkFlyingSpeed(AntarchyMobsConfig.stratosharkFlyingSpeed());
        AntarchySettings.setStratosharkKnockbackResistance(AntarchyMobsConfig.stratosharkKnockbackResistance());
        AntarchySettings.setMissileSquidHealth(AntarchyMobsConfig.missileSquidHealth());
        AntarchySettings.setMissileSquidAttackDamage(AntarchyMobsConfig.missileSquidAttackDamage());
        AntarchySettings.setMissileSquidMovementSpeed(AntarchyMobsConfig.missileSquidMovementSpeed());
        AntarchySettings.setMissileSquidFlyingSpeed(AntarchyMobsConfig.missileSquidFlyingSpeed());
        AntarchySettings.setMissileSquidKnockbackResistance(AntarchyMobsConfig.missileSquidKnockbackResistance());
        AntarchySettings.setMolewormHealth(AntarchyMobsConfig.molewormHealth());
        AntarchySettings.setMolewormAttackDamage(AntarchyMobsConfig.molewormAttackDamage());
        AntarchySettings.setMolewormMovementSpeed(AntarchyMobsConfig.molewormMovementSpeed());
        AntarchySettings.setMolevoreHealth(AntarchyMobsConfig.molevoreHealth());
        AntarchySettings.setMolevoreAttackDamage(AntarchyMobsConfig.molevoreAttackDamage());
        AntarchySettings.setMolevoreSpinTicks(AntarchyMobsConfig.molevoreSpinTicks());
        AntarchySettings.setMolevoreCooldownTicks(AntarchyMobsConfig.molevoreCooldownTicks());
        AntarchySettings.setMolevoreChargeSpeed(AntarchyMobsConfig.molevoreChargeSpeed());
        AntarchySettings.setMolevoreBreakRange(AntarchyMobsConfig.molevoreBreakRange());
        AntarchySettings.setMolevoreBreakVerticalRange(AntarchyMobsConfig.molevoreBreakVerticalRange());
        AntarchySettings.setMolevoreBreakHalfWidth(AntarchyMobsConfig.molevoreBreakHalfWidth());
        AntarchySettings.setOctopusBombHealth(AntarchyMobsConfig.octopusBombHealth());
        AntarchySettings.setOctopusBombAttackDamage(AntarchyMobsConfig.octopusBombAttackDamage());

        AntarchySettings.setReverieHealth(AntarchyMobsConfig.reverieHealth());
        AntarchySettings.setReverieInterestRadius(AntarchyMobsConfig.reverieInterestRadius());
        AntarchySettings.setReverieAbandonPlayerDistance(AntarchyMobsConfig.reverieAbandonPlayerDistance());
        AntarchySettings.setReverieNoticeDurationTicks(AntarchyMobsConfig.reverieNoticeDurationTicks());
        AntarchySettings.setReverieInterestDurationTicks(AntarchyMobsConfig.reverieInterestDurationTicks());
        AntarchySettings.setReverieRebindCooldownTicks(AntarchyMobsConfig.reverieRebindCooldownTicks());
        AntarchySettings.setReverieDamageReactionDurationTicks(AntarchyMobsConfig.reverieDamageReactionDurationTicks());
        AntarchySettings.setReverieDangerousFallDistance(AntarchyMobsConfig.reverieDangerousFallDistance());
        AntarchySettings.setReverieDangerousFallSpeed(AntarchyMobsConfig.reverieDangerousFallSpeed());
        AntarchySettings.setReverieWarningThreatRadius(AntarchyMobsConfig.reverieWarningThreatRadius());
        AntarchySettings.setReverieWarningThreatVerticalRange(AntarchyMobsConfig.reverieWarningThreatVerticalRange());
        AntarchySettings.setReverieDuplicationCooldownTicks(AntarchyMobsConfig.reverieDuplicationCooldownTicks());

        AntarchySettings.setNightmareHealth(AntarchyMobsConfig.nightmareHealth());
        AntarchySettings.setNightmareAttackDamage(AntarchyMobsConfig.nightmareAttackDamage());
        AntarchySettings.setNightmareMovementSpeed(AntarchyMobsConfig.nightmareMovementSpeed());
        AntarchySettings.setNightmareWeaknessTicks(AntarchyMobsConfig.nightmareWeaknessTicks());
        AntarchySettings.setNightmareDreadTicks(AntarchyMobsConfig.nightmareDreadTicks());
        AntarchySettings.setDimensionalTearLifetimeTicks(AntarchyMobsConfig.dimensionalTearLifetimeTicks());
        AntarchySettings.setDimensionalTearInvertedDurationTicks(AntarchyMobsConfig.dimensionalTearInvertedDurationTicks());
        AntarchySettings.setDimensionalTearEmergenceMinIntervalTicks(AntarchyMobsConfig.dimensionalTearEmergenceMinIntervalTicks());
        AntarchySettings.setDimensionalTearEmergenceMaxIntervalTicks(AntarchyMobsConfig.dimensionalTearEmergenceMaxIntervalTicks());
        AntarchySettings.setDimensionalTearLucidEventChance(AntarchyMobsConfig.dimensionalTearLucidEventChance());
        AntarchySettings.setDimensionalTearsSpawnNaturally(AntarchyMobsConfig.dimensionalTearsSpawnNaturally());

        AntarchySettings.setBasiliskPetrifyingGazeEnabled(AntarchyMobsConfig.basiliskPetrifyingGazeEnabled());
        AntarchySettings.setBasiliskSpawnMaxLightLevel(AntarchyMobsConfig.basiliskSpawnMaxLightLevel());
        AntarchySettings.setBasiliskHealth(AntarchyMobsConfig.basiliskHealth());
        AntarchySettings.setBasiliskAttackDamage(AntarchyMobsConfig.basiliskAttackDamage());
        AntarchySettings.setBasiliskMovementSpeed(AntarchyMobsConfig.basiliskMovementSpeed());
        AntarchySettings.setBasiliskArmor(AntarchyMobsConfig.basiliskArmor());
        AntarchySettings.setBasiliskKnockbackResistance(AntarchyMobsConfig.basiliskKnockbackResistance());
        AntarchySettings.setBasiliskFollowRange(AntarchyMobsConfig.basiliskFollowRange());
        AntarchySettings.setBasiliskXpReward(AntarchyMobsConfig.basiliskXpReward());
        AntarchySettings.setBasiliskAttackAnimTicks(AntarchyMobsConfig.basiliskAttackAnimTicks());
        AntarchySettings.setBasiliskAttackDamageTick(AntarchyMobsConfig.basiliskAttackDamageTick());
        AntarchySettings.setBasiliskHissCooldownTicks(AntarchyMobsConfig.basiliskHissCooldownTicks());
        AntarchySettings.setBasiliskAttackReach(AntarchyMobsConfig.basiliskAttackReach());
        AntarchySettings.setBasiliskGazeRange(AntarchyMobsConfig.basiliskGazeRange());
        AntarchySettings.setBasiliskGazeDotThreshold(AntarchyMobsConfig.basiliskGazeDotThreshold());
        AntarchySettings.setBasiliskGazeFacingThreshold(AntarchyMobsConfig.basiliskGazeFacingThreshold());
        AntarchySettings.setBasiliskHissChargeTicks(AntarchyMobsConfig.basiliskHissChargeTicks());
        AntarchySettings.setBasiliskPlayerParalyzeTicks(AntarchyMobsConfig.basiliskPlayerParalyzeTicks());
        AntarchySettings.setBasiliskPreyPetrifyCooldownTicks(AntarchyMobsConfig.basiliskPreyPetrifyCooldownTicks());
        AntarchySettings.setBasiliskPreyPetrifyRange(AntarchyMobsConfig.basiliskPreyPetrifyRange());
        AntarchySettings.setBasiliskPreyPetrifyTicks(AntarchyMobsConfig.basiliskPreyPetrifyTicks());

        AntarchySettings.setLucidAttackRange(AntarchyMobsConfig.lucidAttackRange());
        AntarchySettings.setLucidPearlInvertedDurationSeconds(AntarchyMobsConfig.lucidPearlInvertedDurationSeconds());
        AntarchySettings.setLucidHealth(AntarchyMobsConfig.lucidHealth());
        AntarchySettings.setLucidAttackDamage(AntarchyMobsConfig.lucidAttackDamage());
        AntarchySettings.setLucidMovementSpeed(AntarchyMobsConfig.lucidMovementSpeed());
        AntarchySettings.setLucidFlyingSpeed(AntarchyMobsConfig.lucidFlyingSpeed());
        AntarchySettings.setLucidKnockbackResistance(AntarchyMobsConfig.lucidKnockbackResistance());
        AntarchySettings.setVortexHealth(AntarchyMobsConfig.vortexHealth());
        AntarchySettings.setVortexAttackDamage(AntarchyMobsConfig.vortexAttackDamage());
        AntarchySettings.setVortexMovementSpeed(AntarchyMobsConfig.vortexMovementSpeed());
        AntarchySettings.setVortexFlyingSpeed(AntarchyMobsConfig.vortexFlyingSpeed());
        AntarchySettings.setVortexMaxActiveVortexes(AntarchyMobsConfig.vortexMaxActiveVortexes());
        AntarchySettings.setWindVortexDurationTicks(AntarchyMobsConfig.windVortexDurationTicks());
        AntarchySettings.setWindVortexPullStrength(AntarchyMobsConfig.windVortexPullStrength());
        AntarchySettings.setWindVortexLaunchStrength(AntarchyMobsConfig.windVortexLaunchStrength());
        AntarchySettings.setVortexLensMinRadius(AntarchyMobsConfig.vortexLensMinRadius());
        AntarchySettings.setVortexLensMaxRadius(AntarchyMobsConfig.vortexLensMaxRadius());
        AntarchySettings.setVortexLensMinHeight(AntarchyMobsConfig.vortexLensMinHeight());
        AntarchySettings.setVortexLensMaxHeight(AntarchyMobsConfig.vortexLensMaxHeight());
        AntarchySettings.setVortexLensPullStrength(AntarchyMobsConfig.vortexLensPullStrength());
        AntarchySettings.setVortexLensPushStrength(AntarchyMobsConfig.vortexLensPushStrength());
        AntarchySettings.setVortexLensLaunchStrength(AntarchyMobsConfig.vortexLensLaunchStrength());

        AntarchySettings.setDreadHallucinationSoundsEnabled(AntarchyMobsConfig.dreadHallucinationSoundsEnabled());
        AntarchySettings.setDreadHallucinationSoundMinInterval(AntarchyMobsConfig.dreadHallucinationSoundMinInterval());
        AntarchySettings.setDreadHallucinationSoundMaxInterval(AntarchyMobsConfig.dreadHallucinationSoundMaxInterval());
        AntarchySettings.setDreadHallucinationMobsEnabled(AntarchyMobsConfig.dreadHallucinationMobsEnabled());
        AntarchySettings.setDreadHallucinationMobMinInterval(AntarchyMobsConfig.dreadHallucinationMobMinInterval());
        AntarchySettings.setDreadHallucinationMobMaxInterval(AntarchyMobsConfig.dreadHallucinationMobMaxInterval());

        AntarchySettings.setToreterrorHealth(AntarchyMobsConfig.toreterrorHealth());
        AntarchySettings.setToreterrorDamageRange(AntarchyMobsConfig.toreterrorDamageRange());
        AntarchySettings.setToreterrorJumpAttackDamage(AntarchyMobsConfig.toreterrorJumpAttackDamage());
        AntarchySettings.setToreterrorJumpAttackKnockback(AntarchyMobsConfig.toreterrorJumpAttackKnockback());
        AntarchySettings.setToreterrorSpinDamage(AntarchyMobsConfig.toreterrorSpinDamage());
        AntarchySettings.setToreterrorSpinKnockback(AntarchyMobsConfig.toreterrorSpinKnockback());
        AntarchySettings.setToreterrorProjectileDamageMultiplier(AntarchyMobsConfig.toreterrorProjectileDamageMultiplier());
        AntarchySettings.setWaterBombDamage(AntarchyMobsConfig.waterBombDamage());
        AntarchySettings.setWaterBombLifetimeTicks(AntarchyMobsConfig.waterBombLifetimeTicks());
        AntarchySettings.setWaterBombGravity(AntarchyMobsConfig.waterBombGravity());
        AntarchySettings.setWaterBombKnockback(AntarchyMobsConfig.waterBombKnockback());

        AntarchySettings.setCrawlingBlightHealth(AntarchyMobsConfig.crawlingBlightHealth());
        AntarchySettings.setCrawlingBlightAttackDamage(AntarchyMobsConfig.crawlingBlightAttackDamage());
        AntarchySettings.setSkulkingFrightHealth(AntarchyMobsConfig.skulkingFrightHealth());
        AntarchySettings.setSkulkingFrightAttackDamage(AntarchyMobsConfig.skulkingFrightAttackDamage());
        AntarchySettings.setManticoreHealth(AntarchyMobsConfig.manticoreHealth());
        AntarchySettings.setManticoreAttackDamage(AntarchyMobsConfig.manticoreAttackDamage());
        AntarchySettings.setManticoreStingPoisonTicks(AntarchyMobsConfig.manticoreStingPoisonTicks());
        AntarchySettings.setQueenManticoreCap(AntarchyMobsConfig.queenManticoreCap());
        AntarchySettings.setQueenManticoreSummonCooldownTicks(AntarchyMobsConfig.queenManticoreSummonCooldownTicks());
        AntarchySettings.setQueenManticoreSummonCount(AntarchyMobsConfig.queenManticoreSummonCount());
        AntarchySettings.setQueenManticoreSummonRange(AntarchyMobsConfig.queenManticoreSummonRange());
        AntarchySettings.setJerryInfantHealth(AntarchyMobsConfig.jerryInfantHealth());
        AntarchySettings.setJerryInfantAttackDamage(AntarchyMobsConfig.jerryInfantAttackDamage());
        AntarchySettings.setJerryMatureHealth(AntarchyMobsConfig.jerryMatureHealth());
        AntarchySettings.setJerryMatureAttackDamage(AntarchyMobsConfig.jerryMatureAttackDamage());
        AntarchySettings.setJerryAlphaHealth(AntarchyMobsConfig.jerryAlphaHealth());
        AntarchySettings.setJerryAlphaAttackDamage(AntarchyMobsConfig.jerryAlphaAttackDamage());
        AntarchySettings.setJerryGammaHealth(AntarchyMobsConfig.jerryGammaHealth());
        AntarchySettings.setJerryGammaAttackDamage(AntarchyMobsConfig.jerryGammaAttackDamage());
        AntarchySettings.setCheepHealth(AntarchyMobsConfig.cheepHealth());
        AntarchySettings.setCheepAttackDamage(AntarchyMobsConfig.cheepAttackDamage());
        AntarchySettings.setDorrieHealth(AntarchyMobsConfig.dorrieHealth());
        AntarchySettings.setHerculesBeetleHealth(AntarchyMobsConfig.herculesBeetleHealth());
        AntarchySettings.setHerculesBeetleAttackDamage(AntarchyMobsConfig.herculesBeetleAttackDamage());
        AntarchySettings.setHerculesBeetleChargeDamage(AntarchyMobsConfig.herculesBeetleChargeDamage());
        AntarchySettings.setHerculesBeetleDamageRange(AntarchyMobsConfig.herculesBeetleDamageRange());
        AntarchySettings.setOuranwoodDeerHealth(AntarchyMobsConfig.ouranwoodDeerHealth());
        AntarchySettings.setGlimmerHealth(AntarchyMobsConfig.glimmerHealth());
        AntarchySettings.setAntDanceRadius(AntarchyMobsConfig.antDanceRadius());
    }

    private static void bakeToolsConfig() {
        AntarchySettings.setBasiliskDaggerAttackDamage(AntarchyToolsConfig.basiliskDaggerAttackDamage());
        AntarchySettings.setBasiliskDaggerAttackSpeed(AntarchyToolsConfig.basiliskDaggerAttackSpeed());
        AntarchySettings.setBasiliskDaggerPoisonDurationTicks(AntarchyToolsConfig.basiliskDaggerPoisonDurationTicks());
        AntarchySettings.setBasiliskDaggerPoisonAmplifier(AntarchyToolsConfig.basiliskDaggerPoisonAmplifier());

        AntarchySettings.setUltimateSwordAttackDamage(AntarchyToolsConfig.ultimateSwordAttackDamage());
        AntarchySettings.setUltimateSwordAttackSpeed(AntarchyToolsConfig.ultimateSwordAttackSpeed());
        AntarchySettings.setUltimatePickaxeAttackDamage(AntarchyToolsConfig.ultimatePickaxeAttackDamage());
        AntarchySettings.setUltimatePickaxeAttackSpeed(AntarchyToolsConfig.ultimatePickaxeAttackSpeed());
        AntarchySettings.setUltimateAxeAttackDamage(AntarchyToolsConfig.ultimateAxeAttackDamage());
        AntarchySettings.setUltimateAxeAttackSpeed(AntarchyToolsConfig.ultimateAxeAttackSpeed());
        AntarchySettings.setUltimateShovelAttackDamage(AntarchyToolsConfig.ultimateShovelAttackDamage());
        AntarchySettings.setUltimateShovelAttackSpeed(AntarchyToolsConfig.ultimateShovelAttackSpeed());
        AntarchySettings.setUltimateHoeAttackDamage(AntarchyToolsConfig.ultimateHoeAttackDamage());
        AntarchySettings.setUltimateHoeAttackSpeed(AntarchyToolsConfig.ultimateHoeAttackSpeed());
        AntarchySettings.setUltimateToolEnchantability(AntarchyToolsConfig.ultimateToolEnchantability());
        AntarchySettings.setUltimateToolsThreeByThreeEnabled(AntarchyToolsConfig.ultimateToolsThreeByThreeEnabled());

        AntarchySettings.setUltimateBowAttackDamage(AntarchyToolsConfig.ultimateBowAttackDamage());
        AntarchySettings.setUltimateBowPlayerHeal(AntarchyToolsConfig.ultimateBowPlayerHeal());
        AntarchySettings.setUltimateBowDrawSpeedMultiplier(AntarchyToolsConfig.ultimateBowDrawSpeedMultiplier());
        AntarchySettings.setUltimateBowComesEnchantedWithFlame(AntarchyToolsConfig.ultimateBowComesEnchantedWithFlame());
        AntarchySettings.setUltimateBowEnchantability(AntarchyToolsConfig.ultimateBowEnchantability());

        AntarchySettings.setUltimateCrossbowAttackDamage(AntarchyToolsConfig.ultimateCrossbowAttackDamage());
        AntarchySettings.setUltimateCrossbowChargeSpeedMultiplier(AntarchyToolsConfig.ultimateCrossbowChargeSpeedMultiplier());
        AntarchySettings.setUltimateCrossbowEnchantability(AntarchyToolsConfig.ultimateCrossbowEnchantability());

        AntarchySettings.setUltimateMaceDamageMultiplier(AntarchyToolsConfig.ultimateMaceDamageMultiplier());
        AntarchySettings.setUltimateMaceAttackSpeed(AntarchyToolsConfig.ultimateMaceAttackSpeed());
        AntarchySettings.setUltimateMaceEnchantability(AntarchyToolsConfig.ultimateMaceEnchantability());

        AntarchySettings.setUltimateArmorComesEnchanted(AntarchyToolsConfig.ultimateArmorComesEnchanted());
        AntarchySettings.setUltimateArmorEnchantability(AntarchyToolsConfig.ultimateArmorEnchantability());
        AntarchySettings.setUltimateHelmetArmorValue(AntarchyToolsConfig.ultimateHelmetArmorValue());
        AntarchySettings.setUltimateChestplateArmorValue(AntarchyToolsConfig.ultimateChestplateArmorValue());
        AntarchySettings.setUltimateLeggingsArmorValue(AntarchyToolsConfig.ultimateLeggingsArmorValue());
        AntarchySettings.setUltimateBootsArmorValue(AntarchyToolsConfig.ultimateBootsArmorValue());
        AntarchySettings.setUltimateHelmetArmorToughness(AntarchyToolsConfig.ultimateHelmetArmorToughness());
        AntarchySettings.setUltimateChestplateArmorToughness(AntarchyToolsConfig.ultimateChestplateArmorToughness());
        AntarchySettings.setUltimateLeggingsArmorToughness(AntarchyToolsConfig.ultimateLeggingsArmorToughness());
        AntarchySettings.setUltimateBootsArmorToughness(AntarchyToolsConfig.ultimateBootsArmorToughness());
        AntarchySettings.setUltimateArmorKnockbackResistance(AntarchyToolsConfig.ultimateArmorKnockbackResistance());

        AntarchySettings.setRoyalArmorComesEnchanted(AntarchyToolsConfig.royalArmorComesEnchanted());
        AntarchySettings.setRoyalArmorProtectionLevel(AntarchyToolsConfig.royalArmorProtectionLevel());
        AntarchySettings.setRoyalArmorEnchantability(AntarchyToolsConfig.royalArmorEnchantability());
        AntarchySettings.setRoyalArmorDurabilityMultiplier(AntarchyToolsConfig.royalArmorDurabilityMultiplier());
        AntarchySettings.setRoyalGuardianHelmetArmorValue(AntarchyToolsConfig.royalGuardianHelmetArmorValue());
        AntarchySettings.setRoyalGuardianChestplateArmorValue(AntarchyToolsConfig.royalGuardianChestplateArmorValue());
        AntarchySettings.setRoyalGuardianLeggingsArmorValue(AntarchyToolsConfig.royalGuardianLeggingsArmorValue());
        AntarchySettings.setRoyalGuardianBootsArmorValue(AntarchyToolsConfig.royalGuardianBootsArmorValue());
        AntarchySettings.setRoyalGuardianArmorToughness(AntarchyToolsConfig.royalGuardianArmorToughness());
        AntarchySettings.setRoyalGuardianArmorKnockbackResistance(AntarchyToolsConfig.royalGuardianArmorKnockbackResistance());
        AntarchySettings.setRoyalGuardianHelmetJudgmentBonus(AntarchyToolsConfig.royalGuardianHelmetJudgmentBonus());
        AntarchySettings.setRoyalGuardianChestplateJudgmentBonus(AntarchyToolsConfig.royalGuardianChestplateJudgmentBonus());
        AntarchySettings.setRoyalGuardianLeggingsJudgmentBonus(AntarchyToolsConfig.royalGuardianLeggingsJudgmentBonus());
        AntarchySettings.setRoyalGuardianBootsJudgmentBonus(AntarchyToolsConfig.royalGuardianBootsJudgmentBonus());
        AntarchySettings.setRoyalGuardianJudgmentDurationTicks(AntarchyToolsConfig.royalGuardianJudgmentDurationTicks());
        AntarchySettings.setRoyalAssailantHelmetArmorValue(AntarchyToolsConfig.royalAssailantHelmetArmorValue());
        AntarchySettings.setRoyalAssailantChestplateArmorValue(AntarchyToolsConfig.royalAssailantChestplateArmorValue());
        AntarchySettings.setRoyalAssailantLeggingsArmorValue(AntarchyToolsConfig.royalAssailantLeggingsArmorValue());
        AntarchySettings.setRoyalAssailantBootsArmorValue(AntarchyToolsConfig.royalAssailantBootsArmorValue());
        AntarchySettings.setRoyalAssailantArmorToughness(AntarchyToolsConfig.royalAssailantArmorToughness());
        AntarchySettings.setRoyalAssailantArmorKnockbackResistance(AntarchyToolsConfig.royalAssailantArmorKnockbackResistance());
        AntarchySettings.setRoyalAssailantHelmetDoubleDamageChance(AntarchyToolsConfig.royalAssailantHelmetDoubleDamageChance());
        AntarchySettings.setRoyalAssailantChestplateDoubleDamageChance(AntarchyToolsConfig.royalAssailantChestplateDoubleDamageChance());
        AntarchySettings.setRoyalAssailantLeggingsDoubleDamageChance(AntarchyToolsConfig.royalAssailantLeggingsDoubleDamageChance());
        AntarchySettings.setRoyalAssailantBootsDoubleDamageChance(AntarchyToolsConfig.royalAssailantBootsDoubleDamageChance());
        AntarchySettings.setRoyalGuardianSwordAttackDamage(AntarchyToolsConfig.royalGuardianSwordAttackDamage());
        AntarchySettings.setGiantFryingPanAttackDamage(AntarchyToolsConfig.giantFryingPanAttackDamage());
        AntarchySettings.setGiantFryingPanCookTimeTicks(AntarchyToolsConfig.giantFryingPanCookTimeTicks());
        AntarchySettings.setRoyalGuardianSwordAttackSpeed(AntarchyToolsConfig.royalGuardianSwordAttackSpeed());
        AntarchySettings.setRoyalGuardianSwordElementalCooldownTicks(AntarchyToolsConfig.royalGuardianSwordElementalCooldownTicks());
        AntarchySettings.setRoyalGuardianSwordFirePrimaryDamage(AntarchyToolsConfig.royalGuardianSwordFirePrimaryDamage());
        AntarchySettings.setRoyalGuardianSwordFireSplashDamage(AntarchyToolsConfig.royalGuardianSwordFireSplashDamage());
        AntarchySettings.setRoyalGuardianSwordFrostSpikeDamage(AntarchyToolsConfig.royalGuardianSwordFrostSpikeDamage());
        AntarchySettings.setRoyalGuardianSwordStormPrimaryDamage(AntarchyToolsConfig.royalGuardianSwordStormPrimaryDamage());
        AntarchySettings.setRoyalGuardianSwordStormJumpDamage(AntarchyToolsConfig.royalGuardianSwordStormJumpDamage());
        AntarchySettings.setRoyalGuardianSwordStormMaxJumps(AntarchyToolsConfig.royalGuardianSwordStormMaxJumps());
        AntarchySettings.setRoyalGuardianSwordStormJumpRange(AntarchyToolsConfig.royalGuardianSwordStormJumpRange());
        AntarchySettings.setRoyalAssailantBattleAxeAttackDamage(AntarchyToolsConfig.royalAssailantBattleAxeAttackDamage());
        AntarchySettings.setRoyalAssailantBattleAxeAttackSpeed(AntarchyToolsConfig.royalAssailantBattleAxeAttackSpeed());
        AntarchySettings.setRoyalWeaponAttackReachBonus(AntarchyToolsConfig.royalWeaponAttackReachBonus());
        AntarchySettings.setRoyalWeaponAttackKnockbackBonus(AntarchyToolsConfig.royalWeaponAttackKnockbackBonus());
        AntarchySettings.setRoyalWeaponDurability(AntarchyToolsConfig.royalWeaponDurability());
        AntarchySettings.setRoyalWeaponEnchantability(AntarchyToolsConfig.royalWeaponEnchantability());
        AntarchySettings.setRoyalGuardianShieldDurability(AntarchyToolsConfig.royalGuardianShieldDurability());
        AntarchySettings.setRoyalGuardianMusterRange(AntarchyToolsConfig.royalGuardianMusterRange());
        AntarchySettings.setRoyalGuardianMusterDurationTicks(AntarchyToolsConfig.royalGuardianMusterDurationTicks());
        AntarchySettings.setRoyalGuardianMusterCooldownTicks(AntarchyToolsConfig.royalGuardianMusterCooldownTicks());
        AntarchySettings.setRoyalGuardianMusterMobCap(AntarchyToolsConfig.royalGuardianMusterMobCap());
        AntarchySettings.setRoyalGuardianMusterDamageReduction(AntarchyToolsConfig.royalGuardianMusterDamageReduction());
        AntarchySettings.setRoyalGuardianMusterAttackBonus(AntarchyToolsConfig.royalGuardianMusterAttackBonus());
        AntarchySettings.setRoyalGuardianMusterSpeedBonus(AntarchyToolsConfig.royalGuardianMusterSpeedBonus());
        AntarchySettings.setRoyalGuardianMusterPassiveDamageFloor(AntarchyToolsConfig.royalGuardianMusterPassiveDamageFloor());
        AntarchySettings.setRoyalGuardianBoundaryRadius(AntarchyToolsConfig.royalGuardianBoundaryRadius());
        AntarchySettings.setRoyalGuardianBoundaryDurationTicks(AntarchyToolsConfig.royalGuardianBoundaryDurationTicks());
        AntarchySettings.setRoyalGuardianBoundaryCooldownTicks(AntarchyToolsConfig.royalGuardianBoundaryCooldownTicks());
        AntarchySettings.setRoyalGuardianBoundaryStrengthAmplifier(AntarchyToolsConfig.royalGuardianBoundaryStrengthAmplifier());
        AntarchySettings.setRoyalGuardianBoundaryArrestTicks(AntarchyToolsConfig.royalGuardianBoundaryArrestTicks());
        AntarchySettings.setRoyalGuardianBoundaryDropTicks(AntarchyToolsConfig.royalGuardianBoundaryDropTicks());
        AntarchySettings.setRoyalGuardianBoundaryProjectileCap(AntarchyToolsConfig.royalGuardianBoundaryProjectileCap());

        AntarchySettings.setBattleAxeAttackDamage(AntarchyToolsConfig.battleAxeAttackDamage());
        AntarchySettings.setBattleAxeAttackSpeed(AntarchyToolsConfig.battleAxeAttackSpeed());

        AntarchySettings.setBigBerthaAttackDamage(AntarchyToolsConfig.bigBerthaAttackDamage());
        AntarchySettings.setBigBerthaReachBonus(AntarchyToolsConfig.bigBerthaReachBonus());
        AntarchySettings.setBigBerthaAttackSpeed(AntarchyToolsConfig.bigBerthaAttackSpeed());
        AntarchySettings.setAttitudeAdjusterBaseDamage(AntarchyToolsConfig.attitudeAdjusterBaseDamage());
        AntarchySettings.setAttitudeAdjusterBreaksBlocks(AntarchyToolsConfig.attitudeAdjusterBreaksBlocks());
        AntarchySettings.setBigBerthaBasiliskParalyzeDurationTicks(AntarchyToolsConfig.bigBerthaBasiliskParalyzeDurationTicks());
        AntarchySettings.setBigBerthaKrakenSlowTicks(AntarchyToolsConfig.bigBerthaKrakenSlowTicks());
        AntarchySettings.setBigBerthaBasiliskCooldownSeconds(AntarchyToolsConfig.bigBerthaBasiliskCooldownSeconds());
        AntarchySettings.setBigBerthaLucidInvertedDurationSeconds(AntarchyToolsConfig.bigBerthaLucidInvertedDurationSeconds());
        AntarchySettings.setBigBerthaLucidInvertedDamageBonusPercent(AntarchyToolsConfig.bigBerthaLucidInvertedDamageBonusPercent());
        AntarchySettings.setBigBerthaNoneModeDamageBonusPercent(AntarchyToolsConfig.bigBerthaNoneModeDamageBonusPercent());
        AntarchySettings.setBigBerthaNightmareDamageBonusPercent(AntarchyToolsConfig.bigBerthaNightmareDamageBonusPercent());
        AntarchySettings.setKrakensGraspAttackDamage(AntarchyToolsConfig.krakensGraspAttackDamage());
        AntarchySettings.setKrakensGraspAttackSpeed(AntarchyToolsConfig.krakensGraspAttackSpeed());
        AntarchySettings.setKrakensGraspThrownDamage(AntarchyToolsConfig.krakensGraspThrownDamage());
        AntarchySettings.setKrakensGraspLightningDamage(AntarchyToolsConfig.krakensGraspLightningDamage());
        AntarchySettings.setKrakensGraspInnateLoyalty(AntarchyToolsConfig.krakensGraspInnateLoyalty());
        AntarchySettings.setKrakensGraspInnateLoyaltyLevel(AntarchyToolsConfig.krakensGraspInnateLoyaltyLevel());
        AntarchySettings.setKrakensGraspTentacleDurationTicks(AntarchyToolsConfig.krakensGraspTentacleDurationTicks());
        AntarchySettings.setKrakensGraspTentacleRadius(AntarchyToolsConfig.krakensGraspTentacleRadius());
        AntarchySettings.setKrakensGraspTentacleSlownessAmplifier(AntarchyToolsConfig.krakensGraspTentacleSlownessAmplifier());
        AntarchySettings.setKrakensGraspTentacleSlownessRefreshTicks(AntarchyToolsConfig.krakensGraspTentacleSlownessRefreshTicks());
        AntarchySettings.setScorpionWhipBaseDamage(AntarchyToolsConfig.scorpionWhipBaseDamage());
        AntarchySettings.setScorpionWhipReachBonus(AntarchyToolsConfig.scorpionWhipReachBonus());
        AntarchySettings.setScorpionWhipPoisonDurationTicks(AntarchyToolsConfig.scorpionWhipPoisonDurationTicks());
        AntarchySettings.setScorpionWhipTetherMaxRange(AntarchyToolsConfig.scorpionWhipTetherMaxRange());
        AntarchySettings.setScorpionWhipSnapBonusDamage(AntarchyToolsConfig.scorpionWhipSnapBonusDamage());
        AntarchySettings.setScorpionWhipPullStrength(AntarchyToolsConfig.scorpionWhipPullStrength());
        AntarchySettings.setScorpionWhipHeavyPullMultiplier(AntarchyToolsConfig.scorpionWhipHeavyPullMultiplier());
        AntarchySettings.setScorpionWhipSelfPullMultiplier(AntarchyToolsConfig.scorpionWhipSelfPullMultiplier());
        AntarchySettings.setBloodCrystalKatanaAttackDamage(AntarchyToolsConfig.bloodCrystalKatanaAttackDamage());
        AntarchySettings.setBloodCrystalKatanaLaunchStrength(AntarchyToolsConfig.bloodCrystalKatanaLaunchStrength());
        AntarchySettings.setBloodCrystalKatanaTrailDurationTicks(AntarchyToolsConfig.bloodCrystalKatanaTrailDurationTicks());
        AntarchySettings.setBloodCrystalKatanaReachBonus(AntarchyToolsConfig.bloodCrystalKatanaReachBonus());
        AntarchySettings.setBloodCrystalKatanaInvulnTicks(AntarchyToolsConfig.bloodCrystalKatanaInvulnTicks());
        AntarchySettings.setBloodCrystalKatanaDashCooldownTicks(AntarchyToolsConfig.bloodCrystalKatanaDashCooldownTicks());

        AntarchySettings.setNightmareHelmetArmorValue(AntarchyToolsConfig.nightmareHelmetArmorValue());
        AntarchySettings.setNightmareChestplateArmorValue(AntarchyToolsConfig.nightmareChestplateArmorValue());
        AntarchySettings.setNightmareLeggingsArmorValue(AntarchyToolsConfig.nightmareLeggingsArmorValue());
        AntarchySettings.setNightmareBootsArmorValue(AntarchyToolsConfig.nightmareBootsArmorValue());
        AntarchySettings.setNightmareHelmetArmorToughness(AntarchyToolsConfig.nightmareHelmetArmorToughness());
        AntarchySettings.setNightmareChestplateArmorToughness(AntarchyToolsConfig.nightmareChestplateArmorToughness());
        AntarchySettings.setNightmareLeggingsArmorToughness(AntarchyToolsConfig.nightmareLeggingsArmorToughness());
        AntarchySettings.setNightmareBootsArmorToughness(AntarchyToolsConfig.nightmareBootsArmorToughness());
        AntarchySettings.setNightmareArmorKnockbackResistance(AntarchyToolsConfig.nightmareArmorKnockbackResistance());
        AntarchySettings.setNightmareHelmetDoubleDamageChance(AntarchyToolsConfig.nightmareHelmetDoubleDamageChance());
        AntarchySettings.setNightmareChestplateDoubleDamageChance(AntarchyToolsConfig.nightmareChestplateDoubleDamageChance());
        AntarchySettings.setNightmareLeggingsDoubleDamageChance(AntarchyToolsConfig.nightmareLeggingsDoubleDamageChance());
        AntarchySettings.setNightmareBootsDoubleDamageChance(AntarchyToolsConfig.nightmareBootsDoubleDamageChance());
        AntarchySettings.setPrimordialArmorKnockbackPerPiece(AntarchyToolsConfig.primordialArmorKnockbackPerPiece());
        AntarchySettings.setNightmareSwordBaseDamage(AntarchyToolsConfig.nightmareSwordBaseDamage());
        AntarchySettings.setNightmareSwordAttackSpeed(AntarchyToolsConfig.nightmareSwordAttackSpeed());
        AntarchySettings.setNightmareSwordScalingFactor(AntarchyToolsConfig.nightmareSwordScalingFactor());
        AntarchySettings.setFallenKingCrownArmorValue(AntarchyToolsConfig.fallenKingCrownArmorValue());
        AntarchySettings.setFallenKingCrownArmorToughness(AntarchyToolsConfig.fallenKingCrownArmorToughness());

        AntarchySettings.setSquidzookaCooldownSeconds(AntarchyToolsConfig.squidzookaCooldownSeconds());
        AntarchySettings.setSquidzookaLaunchVelocity(AntarchyToolsConfig.squidzookaLaunchVelocity());
        AntarchySettings.setRpoLauncherCooldownSeconds(AntarchyToolsConfig.rpoLauncherCooldownSeconds());
        AntarchySettings.setRpoLauncherLaunchVelocity(AntarchyToolsConfig.rpoLauncherLaunchVelocity());
        AntarchySettings.setRpoLauncherExplosionDamage(AntarchyToolsConfig.rpoLauncherExplosionDamage());
        AntarchySettings.setRpoLauncherExplosionRadius(AntarchyToolsConfig.rpoLauncherExplosionRadius());

        AntarchySettings.setSizeChangingRaysEnabled(AntarchyToolsConfig.sizeChangingRaysEnabled());
        AntarchySettings.setSizeRayCooldownSeconds(AntarchyToolsConfig.sizeRayCooldownSeconds());
        AntarchySettings.setSizeRayMinScale(AntarchyToolsConfig.sizeRayMinScale());
        AntarchySettings.setSizeRayMaxScale(AntarchyToolsConfig.sizeRayMaxScale());
        AntarchySettings.setSizeRayDeltaPerHit(AntarchyToolsConfig.sizeRayDeltaPerHit());
        AntarchySettings.setShrinkingPotionDelta(AntarchyToolsConfig.shrinkingPotionDelta());
        AntarchySettings.setGrowthPotionDelta(AntarchyToolsConfig.growthPotionDelta());

        AntarchySettings.setInvertProjectilesFromInvertedPlayers(AntarchyToolsConfig.invertProjectilesFromInvertedPlayers());

        AntarchySettings.setGravityGunEnabled(AntarchyToolsConfig.gravityGunEnabled());
        AntarchySettings.setGravityGunBlocksEnabled(AntarchyToolsConfig.gravityGunBlocksEnabled());
        AntarchySettings.setGravityGunEntitiesEnabled(AntarchyToolsConfig.gravityGunEntitiesEnabled());
        AntarchySettings.setGravityGunRange(AntarchyToolsConfig.gravityGunRange());
        AntarchySettings.setGravityGunThrowStrength(AntarchyToolsConfig.gravityGunThrowStrength());
        AntarchySettings.setGravityGunBlastStrength(AntarchyToolsConfig.gravityGunBlastStrength());
        AntarchySettings.setGravityGunCooldownSeconds(AntarchyToolsConfig.gravityGunCooldownSeconds());
        AntarchySettings.setGravityGunMaxHoldDistance(AntarchyToolsConfig.gravityGunMaxHoldDistance());
        AntarchySettings.setEyeOfTheStormEnabled(AntarchyToolsConfig.eyeOfTheStormEnabled());
        AntarchySettings.setEyeOfTheStormUpdraftCooldownTicks(AntarchyToolsConfig.eyeOfTheStormUpdraftCooldownTicks());
        AntarchySettings.setEyeOfTheStormUpdraftLaunchStrength(AntarchyToolsConfig.eyeOfTheStormUpdraftLaunchStrength());
        AntarchySettings.setEyeOfTheStormUpdraftHeight(AntarchyToolsConfig.eyeOfTheStormUpdraftHeight());
        AntarchySettings.setEyeOfTheStormUpdraftRadius(AntarchyToolsConfig.eyeOfTheStormUpdraftRadius());
        AntarchySettings.setEyeOfTheStormUpdraftDurationTicks(AntarchyToolsConfig.eyeOfTheStormUpdraftDurationTicks());
        AntarchySettings.setEyeOfTheStormSurgeCooldownTicks(AntarchyToolsConfig.eyeOfTheStormSurgeCooldownTicks());
        AntarchySettings.setEyeOfTheStormSurgeRange(AntarchyToolsConfig.eyeOfTheStormSurgeRange());
        AntarchySettings.setEyeOfTheStormSurgeDurationTicks(AntarchyToolsConfig.eyeOfTheStormSurgeDurationTicks());
        AntarchySettings.setEyeOfTheStormSurgeRadius(AntarchyToolsConfig.eyeOfTheStormSurgeRadius());
        AntarchySettings.setEyeOfTheStormSurgeHeight(AntarchyToolsConfig.eyeOfTheStormSurgeHeight());
        AntarchySettings.setEyeOfTheStormSurgePullStrength(AntarchyToolsConfig.eyeOfTheStormSurgePullStrength());
        AntarchySettings.setEyeOfTheStormSurgeReturnStrength(AntarchyToolsConfig.eyeOfTheStormSurgeReturnStrength());
        AntarchySettings.setEyeOfTheStormSurgeDamage(AntarchyToolsConfig.eyeOfTheStormSurgeDamage());
        AntarchySettings.setCritterCageMaxCapturableWidth(AntarchyToolsConfig.critterCageMaxCapturableWidth());
        AntarchySettings.setCritterCageMaxCapturableHeight(AntarchyToolsConfig.critterCageMaxCapturableHeight());

        AntarchySettings.setMinersDreamEnabled(AntarchyToolsConfig.minersDreamEnabled());
        AntarchySettings.setMinersDreamMinimumRange(AntarchyToolsConfig.minersDreamMinimumRange());
        AntarchySettings.setMinersDreamMaximumRange(AntarchyToolsConfig.minersDreamMaximumRange());
        AntarchySettings.setMinersDreamTorchSpacing(AntarchyToolsConfig.minersDreamTorchSpacing());
        AntarchySettings.setMinersDreamBlocksPerTick(AntarchyToolsConfig.minersDreamBlocksPerTick());

        AntarchySettings.setDuctTapeRepairPercentPerUse(AntarchyToolsConfig.ductTapeRepairPercentPerUse());
        AntarchySettings.setPotentNyxiteInvertedDurationSeconds(AntarchyToolsConfig.potentNyxiteInvertedDurationSeconds());
        AntarchySettings.setCorneaEarNightVisionSeconds(AntarchyToolsConfig.corneaEarNightVisionSeconds());
        AntarchySettings.setAmericanizingEnabled(AntarchyToolsConfig.americanizingEnabled());
        AntarchySettings.setAmericanBonusNutrition(AntarchyToolsConfig.americanBonusNutrition());
        AntarchySettings.setAmericanBonusSaturation(AntarchyToolsConfig.americanBonusSaturation());
        AntarchySettings.setAmericanRegenerationDurationTicks(AntarchyToolsConfig.americanRegenerationDurationTicks());
        AntarchySettings.setAmericanRegenerationAmplifier(AntarchyToolsConfig.americanRegenerationAmplifier());

        AntarchySettings.setMogglesVisionRadius(AntarchyToolsConfig.mogglesVisionRadius());
    }

    private static void bakeMiscConfig() {
        AntarchySettings.setRainbowAntsLeadToInfinityDimensions(AntarchyMiscConfig.rainbowAntsLeadToInfinityDimensions());
        AntarchySettings.setRainbowAntInfinityEasterEggChance(AntarchyMiscConfig.rainbowAntInfinityEasterEggChance());
        AntarchySettings.setDisableInfinityBookPortalCreation(AntarchyMiscConfig.disableInfinityBookPortalCreation());
        AntarchySettings.setHushweedSporeLifetimeSeconds(AntarchyMiscConfig.hushweedSporeLifetimeSeconds());
        AntarchySettings.setDiamondMinecartEnabled(AntarchyMiscConfig.diamondMinecartEnabled());
        AntarchySettings.setDiamondMinecartPlacesRails(AntarchyMiscConfig.diamondMinecartPlacesRails());
        AntarchySettings.setDiamondMinecartCruiseSpeed(AntarchyMiscConfig.diamondMinecartCruiseSpeed());
        AntarchySettings.setDiamondMinecartMaxSpeed(AntarchyMiscConfig.diamondMinecartMaxSpeed());
        AntarchySettings.setDiamondMinecartAcceleration(AntarchyMiscConfig.diamondMinecartAcceleration());
        AntarchySettings.setDiamondMinecartDeceleration(AntarchyMiscConfig.diamondMinecartDeceleration());
        AntarchySettings.setDiamondMinecartCoastDeceleration(AntarchyMiscConfig.diamondMinecartCoastDeceleration());
        AntarchySettings.setDiamondMinecartMobDamageEnabled(AntarchyMiscConfig.diamondMinecartMobDamageEnabled());
        AntarchySettings.setDiamondMinecartMaxMobDamage(AntarchyMiscConfig.diamondMinecartMaxMobDamage());
        AntarchySettings.setHoverboardMaxSpeed(AntarchyMiscConfig.hoverboardMaxSpeed());
        AntarchySettings.setHoverboardAcceleration(AntarchyMiscConfig.hoverboardAcceleration());
        AntarchySettings.setPermanentPortalsEnabled(AntarchyMiscConfig.permanentPortalsEnabled());
        AntarchySettings.setPermanentPortalsFlintAndSteelEnabled(AntarchyMiscConfig.permanentPortalsFlintAndSteelEnabled());
        AntarchySettings.setElythiaPortalEnabled(AntarchyMiscConfig.elythiaPortalEnabled());
        AntarchySettings.setThoraxisPortalEnabled(AntarchyMiscConfig.thoraxisPortalEnabled());
        AntarchySettings.setCavarynPortalEnabled(AntarchyMiscConfig.cavarynPortalEnabled());


        // Misc

        AntarchySettings.setElythiaFireflyParticlesEnabled(AntarchyMiscConfig.elythiaFireflyParticlesEnabled());

        AntarchySettings.setDreamSandEnabled(AntarchyMiscConfig.dreamSandEnabled());
        AntarchySettings.setDreamSandJumpVelocityMultiplier(AntarchyMiscConfig.dreamSandJumpVelocityMultiplier());
        AntarchySettings.setDreamSandGravityMultiplier(AntarchyMiscConfig.dreamSandGravityMultiplier());
        AntarchySettings.setDreamSandFallingBlockGravityMultiplier(AntarchyMiscConfig.dreamSandFallingBlockGravityMultiplier());
        AntarchySettings.setDreamSandEffectDurationSeconds(AntarchyMiscConfig.dreamSandEffectDurationSeconds());

        AntarchySettings.setIchorWitherEnabled(AntarchyMiscConfig.ichorWitherEnabled());

        AntarchySettings.setDuplicatorTreeEnabled(AntarchyMiscConfig.duplicatorTreeEnabled());
        AntarchySettings.setGlowVinesUnderLeaves(AntarchyMiscConfig.glowVinesUnderLeaves());
        AntarchySettings.setSwingThroughGrassEnabled(AntarchyMiscConfig.swingThroughGrassEnabled());
        AntarchySettings.setFabricKeybindingConflictFixEnabled(AntarchyMiscConfig.fabricKeybindingConflictFixEnabled());
        AntarchySettings.setExperimentalSettingsPopupDisabled(AntarchyMiscConfig.experimentalSettingsPopupDisabled());
        AntarchySettings.setEntitySpecificFireOverlayEnabled(AntarchyMiscConfig.entitySpecificFireOverlayEnabled());

        AntarchySettings.setBloodCrystalArmorShieldRechargeTicks(AntarchyToolsConfig.bloodCrystalArmorShieldRechargeTicks());
        AntarchySettings.setBloodCrystalAppleShieldCount(AntarchyToolsConfig.bloodCrystalAppleShieldCount());
        AntarchySettings.setBloodCrystalAppleDurationTicks(AntarchyToolsConfig.bloodCrystalAppleDurationTicks());
        AntarchySettings.setBloodCrystalAppleShieldRechargeTicks(AntarchyToolsConfig.bloodCrystalAppleShieldRechargeTicks());
        AntarchySettings.setBloodCrystalHardMaxShields(AntarchyToolsConfig.bloodCrystalHardMaxShields());
        AntarchySettings.setBloodCrystalHelmetDefense(AntarchyToolsConfig.bloodCrystalHelmetDefense());
        AntarchySettings.setBloodCrystalChestplateDefense(AntarchyToolsConfig.bloodCrystalChestplateDefense());
        AntarchySettings.setBloodCrystalLeggingsDefense(AntarchyToolsConfig.bloodCrystalLeggingsDefense());
        AntarchySettings.setBloodCrystalBootsDefense(AntarchyToolsConfig.bloodCrystalBootsDefense());
        AntarchySettings.setBloodCrystalArmorToughness(AntarchyToolsConfig.bloodCrystalArmorToughness());
        AntarchySettings.setBloodCrystalHelmetDurability(AntarchyToolsConfig.bloodCrystalHelmetDurability());
        AntarchySettings.setBloodCrystalChestplateDurability(AntarchyToolsConfig.bloodCrystalChestplateDurability());
        AntarchySettings.setBloodCrystalLeggingsDurability(AntarchyToolsConfig.bloodCrystalLeggingsDurability());
        AntarchySettings.setBloodCrystalBootsDurability(AntarchyToolsConfig.bloodCrystalBootsDurability());
    }
}
