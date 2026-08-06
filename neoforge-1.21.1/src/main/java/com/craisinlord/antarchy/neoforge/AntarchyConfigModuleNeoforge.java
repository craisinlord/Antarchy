package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.network.ImpactShakeSync;
import com.craisinlord.antarchy.config.ConfigResetGuard;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakePayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakeSync;
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
        ConfigResetGuard.wipeIfNeeded(
                configDir,
                configDir.resolve("antarchy_mobs.toml"),
                configDir.resolve("antarchy_tools.toml"),
                configDir.resolve("antarchy_misc.toml")
        );
        modContainer.registerConfig(ModConfig.Type.COMMON, AntarchyMobsConfig.SPEC,  "antarchy/antarchy_mobs.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, AntarchyToolsConfig.SPEC, "antarchy/antarchy_tools.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, AntarchyMiscConfig.SPEC,  "antarchy/antarchy_misc.toml");
        HerculesBeetleImpactShakeSync.setSink((player, ticks) -> PacketDistributor.sendToPlayer(player, new HerculesBeetleImpactShakePayload(ticks)));
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

        AntarchySettings.setJumpyBugHealth(AntarchyMobsConfig.jumpyBugHealth());
        AntarchySettings.setJumpyBugPounceDamage(AntarchyMobsConfig.jumpyBugPounceDamage());
        AntarchySettings.setJumpyBugLatchDamage(AntarchyMobsConfig.jumpyBugLatchDamage());
        AntarchySettings.setJumpyBugCamouflageAlpha(AntarchyMobsConfig.jumpyBugCamouflageAlpha());

        AntarchySettings.setEmperorScorpionDamageRange(AntarchyMobsConfig.emperorScorpionDamageRange());
        AntarchySettings.setEmperorScorpionMinDespawnTicks(AntarchyMobsConfig.emperorScorpionMinDespawnTicks());

        AntarchySettings.setKrakenHealth(AntarchyMobsConfig.krakenHealth());
        AntarchySettings.setKrakenAttackDamage(AntarchyMobsConfig.krakenAttackDamage());
        AntarchySettings.setKrakenProjectileDamageTakenMultiplier(AntarchyMobsConfig.krakenProjectileDamageTakenMultiplier());
        AntarchySettings.setKrakenSquidSpawnEnabled(AntarchyMobsConfig.krakenSquidSpawnEnabled());
        AntarchySettings.setKrakenMassSpawnEnabled(AntarchyMobsConfig.krakenMassSpawnEnabled());
        AntarchySettings.setKrakenRequireBadOmenToSummon(AntarchyMobsConfig.krakenRequireBadOmenToSummon());
        AntarchySettings.setKrakenFollowRange(AntarchyMobsConfig.krakenFollowRange());
        AntarchySettings.setKrakenDamageRange(AntarchyMobsConfig.krakenDamageRange());

        AntarchySettings.setBrutalflyHealth(AntarchyMobsConfig.brutalflyHealth());
        AntarchySettings.setBrutalflySwipeDamage(AntarchyMobsConfig.brutalflySwipeDamage());
        AntarchySettings.setBrutalflySpitDamage(AntarchyMobsConfig.brutalflySpitDamage());
        AntarchySettings.setBrutalflyDamageRange(AntarchyMobsConfig.brutalflyDamageRange());

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
        AntarchySettings.setRollyPollyTumbleDamage(AntarchyMobsConfig.rollyPollyTumbleDamage());
        AntarchySettings.setRollyPollyTameChance(AntarchyMobsConfig.rollyPollyTameChance());
        AntarchySettings.setRollyPollyBowlingDamage(AntarchyMobsConfig.rollyPollyBowlingDamage());
        AntarchySettings.setRollyPollyBowlingKnockback(AntarchyMobsConfig.rollyPollyBowlingKnockback());
        AntarchySettings.setRollyPollyArmor(AntarchyMobsConfig.rollyPollyArmor());
        AntarchySettings.setRollyPollyRolledArmorBonus(AntarchyMobsConfig.rollyPollyRolledArmorBonus());

        AntarchySettings.setTriffidHealth(AntarchyMobsConfig.triffidHealth());
        AntarchySettings.setTriffidAttackDamage(AntarchyMobsConfig.triffidAttackDamage());
        AntarchySettings.setTriffidGrabDamage(AntarchyMobsConfig.triffidGrabDamage());

        AntarchySettings.setCaterpillarPupationTimeSeconds(AntarchyMobsConfig.caterpillarPupationTimeSeconds());

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
        AntarchySettings.setToreterrorRangedWaterBombChance(AntarchyMobsConfig.toreterrorRangedWaterBombChance());
        AntarchySettings.setToreterrorProjectileDamageMultiplier(AntarchyMobsConfig.toreterrorProjectileDamageMultiplier());
        AntarchySettings.setWaterBombDamage(AntarchyMobsConfig.waterBombDamage());
        AntarchySettings.setWaterBombLifetimeTicks(AntarchyMobsConfig.waterBombLifetimeTicks());
        AntarchySettings.setWaterBombGravity(AntarchyMobsConfig.waterBombGravity());
        AntarchySettings.setWaterBombKnockback(AntarchyMobsConfig.waterBombKnockback());
        AntarchySettings.setWaterCannonCooldownSeconds(AntarchyMobsConfig.waterCannonCooldownSeconds());

        AntarchySettings.setCreepingHorrorHealth(AntarchyMobsConfig.creepingHorrorHealth());
        AntarchySettings.setCreepingHorrorAttackDamage(AntarchyMobsConfig.creepingHorrorAttackDamage());
        AntarchySettings.setLurkingTerrorHealth(AntarchyMobsConfig.lurkingTerrorHealth());
        AntarchySettings.setLurkingTerrorAttackDamage(AntarchyMobsConfig.lurkingTerrorAttackDamage());
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
        AntarchySettings.setScorpionWhipReelCooldownTicks(AntarchyToolsConfig.scorpionWhipReelCooldownTicks());
        AntarchySettings.setScorpionWhipSnapBonusDamage(AntarchyToolsConfig.scorpionWhipSnapBonusDamage());
        AntarchySettings.setScorpionWhipSnapCooldownTicks(AntarchyToolsConfig.scorpionWhipSnapCooldownTicks());
        AntarchySettings.setScorpionWhipPullStrength(AntarchyToolsConfig.scorpionWhipPullStrength());
        AntarchySettings.setScorpionWhipHeavyPullMultiplier(AntarchyToolsConfig.scorpionWhipHeavyPullMultiplier());
        AntarchySettings.setScorpionWhipSelfPullMultiplier(AntarchyToolsConfig.scorpionWhipSelfPullMultiplier());
        AntarchySettings.setBloodCrystalKatanaAttackDamage(AntarchyToolsConfig.bloodCrystalKatanaAttackDamage());
        AntarchySettings.setBloodCrystalKatanaLaunchStrength(AntarchyToolsConfig.bloodCrystalKatanaLaunchStrength());
        AntarchySettings.setBloodCrystalKatanaTrailDurationTicks(AntarchyToolsConfig.bloodCrystalKatanaTrailDurationTicks());

        AntarchySettings.setNightmareHelmetArmorValue(AntarchyToolsConfig.nightmareHelmetArmorValue());
        AntarchySettings.setNightmareChestplateArmorValue(AntarchyToolsConfig.nightmareChestplateArmorValue());
        AntarchySettings.setNightmareLeggingsArmorValue(AntarchyToolsConfig.nightmareLeggingsArmorValue());
        AntarchySettings.setNightmareBootsArmorValue(AntarchyToolsConfig.nightmareBootsArmorValue());
        AntarchySettings.setNightmareHelmetArmorToughness(AntarchyToolsConfig.nightmareHelmetArmorToughness());
        AntarchySettings.setNightmareChestplateArmorToughness(AntarchyToolsConfig.nightmareChestplateArmorToughness());
        AntarchySettings.setNightmareLeggingsArmorToughness(AntarchyToolsConfig.nightmareLeggingsArmorToughness());
        AntarchySettings.setNightmareBootsArmorToughness(AntarchyToolsConfig.nightmareBootsArmorToughness());
        AntarchySettings.setNightmareArmorKnockbackResistance(AntarchyToolsConfig.nightmareArmorKnockbackResistance());
        AntarchySettings.setNightmareArmorDreadAuraRangePerPiece(AntarchyToolsConfig.nightmareArmorDreadAuraRangePerPiece());
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
        AntarchySettings.setAntDanceRadius(AntarchyMobsConfig.antDanceRadius());

        AntarchySettings.setMogglesVisionRadius(AntarchyToolsConfig.mogglesVisionRadius());
        AntarchySettings.setMogglesVisionMaxLight(AntarchyToolsConfig.mogglesVisionMaxLight());
        AntarchySettings.setMogglesVisionAlpha(AntarchyToolsConfig.mogglesVisionAlpha());
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
        AntarchySettings.setDreamSandFallDamageMultiplier(AntarchyMiscConfig.dreamSandFallDamageMultiplier());
        AntarchySettings.setDreamSandEffectDurationSeconds(AntarchyMiscConfig.dreamSandEffectDurationSeconds());

        AntarchySettings.setIchorWitherEnabled(AntarchyMiscConfig.ichorWitherEnabled());

        AntarchySettings.setDuplicatorTreeEnabled(AntarchyMiscConfig.duplicatorTreeEnabled());
        AntarchySettings.setGlowingTorchflowers(AntarchyMiscConfig.glowingTorchflowers());
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
