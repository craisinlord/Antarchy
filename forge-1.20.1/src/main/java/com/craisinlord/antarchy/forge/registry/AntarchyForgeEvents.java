package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.compat.infinity.InfinityConfigRefresh;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.bloodglass.BloodglassAccess;
import com.craisinlord.antarchy.content.boss.BossCombatUtil;
import com.craisinlord.antarchy.content.dispenser.AntimetalMinecartDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.AnvilCannonDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.BugSprayDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.EyeOfTheStormDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.RpoLauncherDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.SizeRayDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.SquidzookaDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.VortexChargeDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.WaterCannonDispenseBehavior;
import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import com.craisinlord.antarchy.content.entity.*;
import com.craisinlord.antarchy.content.entity.ant.AntArrivalScheduler;
import com.craisinlord.antarchy.content.entity.ant.RainbowAntEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerNightSpawner;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenRitualManager;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEyeProjectileEntity;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkSkySpawner;
import com.craisinlord.antarchy.content.entity.trades.ComputerScientistTradeManager;
import com.craisinlord.antarchy.content.entity.trades.DrTrayaurusTradeManager;
import com.craisinlord.antarchy.content.entity.trades.PestControlTradeManager;
import com.craisinlord.antarchy.content.entity.vortex.VortexChargeProjectileEntity;
import com.craisinlord.antarchy.content.entity.vortex.VortexEntity;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.craisinlord.antarchy.content.horde.CavarynCreatureSpawner;
import com.craisinlord.antarchy.content.horde.CavarynHordeManager;
import com.craisinlord.antarchy.content.item.*;
import com.craisinlord.antarchy.content.item.ultimate.UltimateGearHelper;
import com.craisinlord.antarchy.content.loot.AntarchyLootInjections;
import com.craisinlord.antarchy.content.network.BloodglassStatePayload;
import com.craisinlord.antarchy.content.portal.PermanentPortalManager;
import com.craisinlord.antarchy.content.recipe.CustomBrewingRecipes;
import com.craisinlord.antarchy.content.throwback.ThrowbackZoneTracker;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync;
import com.craisinlord.antarchy.content.transit.AntTransit;
import com.craisinlord.antarchy.content.weather.ElythiaEvents;
import com.craisinlord.antarchy.content.weather.ElythiaRainGrowth;
import com.craisinlord.antarchy.content.weather.ThoraxisEvents;
import com.craisinlord.antarchy.content.worldgen.elythia.KingsTreeGridFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.craisinlord.antarchy.forge.AntarchyForgeFluidTypes;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;
import com.craisinlord.antarchy.mixins.PotionBrewingInvoker;
import com.craisinlord.anthology.effect.AnthologyEffects;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.MountedGravityHelper;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.joml.Vector3f;

public final class AntarchyForgeEvents {
    private static final double OVERHEAD_INVERSION_WARN_MS = 10.0D;
    private static final long OVERHEAD_INVERSION_WARN_INTERVAL_TICKS = 100L;
    private static long lastOverheadInversionWarnTick = Long.MIN_VALUE / 2L;

    private AntarchyForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyForgeEvents::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onMissileSquidDeath);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onPermanentPortalSacrifice);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onLivingBreathe);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onLootTableLoad);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleNaturalRabbitReplacement);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBlockToolModification);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onMobEffectApplicable);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onInvertedEffectRemoved);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onInvertedEffectExpired);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleUltimateBowDamage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleUltimateCrossbowDamage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleNightmareArmorDoubleDamage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBossMagicBurstMitigation);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleScorpionWhipAttackEntity);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleAttitudeAdjusterAttackEntity);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handlePortalGunAttackEntity);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleScorpionWhipLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handlePortalGunLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleScorpionWhipRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleKrakenRitualRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleScorpionWhipRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleGiantFryingPanRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedAttackEntity);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedEntityInteractSpecific);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickDreadAndIchor);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickScorpionWhips);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickWormHooks);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickAttitudeAdjusterSlams);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleAntiwaterDamage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleAntiwaterFall);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodCrystalBootsFall);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassShield);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodCrystalArmorEquip);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassWardApplied);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassWardRemoved);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassWardExpired);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickBloodglassRecharge);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickAntArrivals);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassPlayerDeath);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassRespawn);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassLogin);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleCavarynHordeKill);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleCavarynHordeBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickCavarynHordes);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickThoraxisUnderside);
        modEventBus.addListener(AntarchyForgeEvents::modifyEntityAttributes);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickOverheadInversion);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleDreadDeath);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleFallenKingCrownKill);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleDreadBedSleep);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleStartTracking);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeLogout);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeDeath);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeRespawn);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeLogin);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeDimensionChange);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickTigerEyeCamouflage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::registerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onVillagerTrades);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onWandererTrades);
    }

    static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == AntarchyForgeMisc.COMPUTER_SCIENTIST.get()) {
            int[] slots = ComputerScientistTradeManager.slotsPerLevel();
            for (int level = 1; level <= slots.length; level++) {
                for (int index = 0; index < slots[level - 1]; index++) {
                    event.getTrades().computeIfAbsent(level, ignored -> new ArrayList<>())
                            .add(ComputerScientistTradeManager.listing(level, index));
                }
            }
        } else if (event.getType() == AntarchyForgeMisc.PEST_CONTROL.get()) {
            int[] slots = PestControlTradeManager.slotsPerLevel();
            for (int level = 1; level <= slots.length; level++) {
                for (int index = 0; index < slots[level - 1]; index++) {
                    event.getTrades().computeIfAbsent(level, ignored -> new ArrayList<>())
                            .add(PestControlTradeManager.listing(level, index));
                }
            }
        } else if (event.getType() == VillagerProfession.FARMER) {
            event.getTrades().get(1).add(emeraldForItems(AntarchyObjects.CORN.get(), 20, 16, 2));
        } else if (event.getType() == VillagerProfession.BUTCHER) {
            event.getTrades().get(1).add(emeraldForItems(AntarchyObjects.COOKED_CORNDOG.get(), 5, 16, 2));
        }
    }

    static void onWandererTrades(WandererTradesEvent event) {
        event.getGenericTrades().add(itemsForEmerald(AntarchyObjects.CORN_SEEDS.get(), 1, 3, 12, 2));
    }

    private static VillagerTrades.ItemListing emeraldForItems(Item item, int count, int maxUses, int villagerXp) {
        return (trader, random) -> new MerchantOffer(new ItemStack(item, count), new ItemStack(Items.EMERALD), maxUses, villagerXp, 0.05F);
    }

    private static VillagerTrades.ItemListing itemsForEmerald(Item item, int emeraldCost, int count, int maxUses, int villagerXp) {
        return (trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, emeraldCost), new ItemStack(item, count), maxUses, villagerXp, 0.05F);
    }

    public static void onMissileSquidDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof MissileSquidEntity missileSquid) {
            if (missileSquid.isSpawnedByKraken()) {
                return;
            }
        } else if (event.getEntity().getType() != EntityType.SQUID) {
            return;
        }
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(event.getEntity().getKillCredit() instanceof Player killer)) {
            return;
        }

        boolean requireBadOmen = AntarchySettings.krakenRequireBadOmenToSummon();
        int badOmenLevel = badOmenLevel(killer);
        if (requireBadOmen && badOmenLevel <= 0) {
            return;
        }

        BlockPos deathPos = event.getEntity().blockPosition();

        if (AntarchySettings.krakenSquidSpawnEnabled()) {
            int chanceDenominator = requireBadOmen ? Math.max(1, 100 / badOmenLevel) : 100;
            if (serverLevel.random.nextInt(chanceDenominator) == 0) {
                spawnKrakens(serverLevel, deathPos, 1);
            }
        }

        if (AntarchySettings.krakenMassSpawnEnabled() && serverLevel.random.nextInt(500) == 0) {
            spawnKrakens(serverLevel, deathPos, 10);
        }
    }

    static void handleCavarynHordeKill(LivingDeathEvent event) {
        if (event.getEntity().getKillCredit() instanceof ServerPlayer player) {
            CavarynHordeManager.recordMobKill(player, event.getEntity());
        }
    }

    static void handleCavarynHordeBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            CavarynHordeManager.recordBlockBreak(player, event.getState(), event.getPos());
        }
    }

    static void tickCavarynHordes(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            CavarynHordeManager.tick(level);
            ThrowbackZoneTracker.tick(level);
            CavarynCreatureSpawner.tick(level);
            GlimmerNightSpawner.tick(level);
            StratosharkSkySpawner.tick(level);
            KingsTreeGridFeature.tickPendingSpawns(level);
            KrakenRitualManager.tick(level);
        }
    }

    static void tickThoraxisUnderside(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ThoraxisUndersideManager.tick(level);
                ElythiaEvents.tick(level);
                ElythiaRainGrowth.tick(level);
                ThoraxisEvents.tick(level);
        }
    }

    static void tickAntArrivals(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        AntArrivalScheduler.tick(event.getServer());
        AntTransit.tick(event.getServer());
    }

    private static int badOmenLevel(Player player) {
        MobEffectInstance effect = player.getEffect(MobEffects.BAD_OMEN);
        return effect != null ? effect.getAmplifier() + 1 : 0;
    }

    public static void onPermanentPortalSacrifice(LivingDeathEvent event) {
        PermanentPortalManager.handleSacrifice(event.getEntity());
        if (event.getEntity() instanceof RainbowAntEntity rainbowAnt) {
            rainbowAnt.tryIgniteInfinityPortalOnDeath();
        }
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (AntarchyLootInjections.addsPhone(event.getName())) {
            event.getTable().addPool(AntarchyLootInjections.phonePool().build());
        }
    }

    public static void onLivingBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getEntity().getEyeInFluidType() != AntarchyForgeFluidTypes.ANTIWATER_TYPE.get()) {
            return;
        }

        event.setConsumeAirAmount(event.getConsumeAirAmount() * 2);
    }

    private static void spawnKrakens(ServerLevel level, BlockPos origin, int count) {
        for (int i = 0; i < count; i++) {
            BlockPos spawnPos = findKrakenSpawnPos(level, origin);
            KrakenEntity kraken = AntarchyObjects.KRAKEN.get().create(level);
            if (kraken == null) {
                continue;
            }
            kraken.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F, 0.0F);
            kraken.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
            level.addFreshEntity(kraken);
        }
    }

    private static BlockPos findKrakenSpawnPos(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < 20; attempt++) {
            BlockPos candidate = origin.offset(
                    level.random.nextInt(33) - 16,
                    level.random.nextInt(9) - 4,
                    level.random.nextInt(33) - 16
            );
            if (level.getFluidState(candidate).is(FluidTags.WATER)) {
                return candidate;
            }
        }
        return origin.above(3);
    }

    static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DrTrayaurusTradeManager());
        event.addListener(new ComputerScientistTradeManager());
        event.addListener(new PestControlTradeManager());
    }
    static void handleStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget().level().isClientSide()) {
            return;
        }

        if (event.getTarget() instanceof ServerPlayer trackedPlayer && event.getEntity() instanceof ServerPlayer trackingPlayer) {
            TigerEyeCamouflageSync.syncTo(trackingPlayer, trackedPlayer);
        }
    }

    static void handleTigerEyeLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TigerEyeCamouflageController.deactivate(player, false);
        }
    }

    static void handleTigerEyeDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TigerEyeCamouflageController.deactivate(player, false);
            TigerEyeCamouflageSync.sync(player);
        }
    }

    static void handleTigerEyeRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TigerEyeCamouflageController.deactivate(player, false);
            TigerEyeCamouflageSync.sync(player);
        }
    }

    static void handleTigerEyeLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TigerEyeCamouflageSync.sync(player);
        }
    }

    static void handleTigerEyeDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TigerEyeCamouflageController.deactivate(player, false);
            TigerEyeCamouflageSync.sync(player);
        }
    }

    static void tickTigerEyeCamouflage(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.player instanceof ServerPlayer player && TigerEyeCamouflageController.validateOrDeactivate(player)) {
            TigerEyeCamouflageSync.sync(player);
        }
    }

    private static final String DREAD_BED_BUG_NEXT_SPAWN_KEY = Antarchy.MODID + ":dread_bed_bug_next_spawn";
    private static final int DREAD_BED_BUG_SPAWN_COOLDOWN_TICKS = 6000;
    static void handleDreadBedSleep(PlayerSleepInBedEvent event) {
        if (!event.getEntity().hasEffect(AntarchyForgeMisc.DREAD.get())) {
            return;
        }
        event.getEntity().displayClientMessage(Component.translatable("message.antarchy.dread_too_scared_to_sleep"), true);
        event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);

        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getEntity();
        long gameTime = level.getGameTime();
        long nextSpawnTime = player.getPersistentData().getLong(DREAD_BED_BUG_NEXT_SPAWN_KEY);
        if (nextSpawnTime > gameTime) {
            return;
        }

        BlockPos bedPos = event.getPos();
        BedBugEntity bedBug = AntarchyObjects.BED_BUG.get().create(level);
        if (bedBug == null) {
            return;
        }

        player.getPersistentData().putLong(DREAD_BED_BUG_NEXT_SPAWN_KEY, gameTime + DREAD_BED_BUG_SPAWN_COOLDOWN_TICKS);
        bedBug.moveTo(bedPos.getX() + 0.5D, bedPos.getY() + 0.5D, bedPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        bedBug.finalizeSpawn(level, level.getCurrentDifficultyAt(bedPos),
                MobSpawnType.EVENT, null, null);
        level.addFreshEntity(bedBug);
    }

    static void handleNaturalRabbitReplacement(MobSpawnEvent.PositionCheck event) {
        if (!AntarchySettings.easterBunnyEnabled()) {
            return;
        }
        if (event.getSpawnType() != MobSpawnType.NATURAL || !(event.getEntity() instanceof Rabbit rabbit)) {
            return;
        }
        if (rabbit instanceof EasterBunnyEntity) {
            return;
        }

        int chancePercent = AntarchySettings.easterBunnyNaturalSpawnChancePercent();
        if (chancePercent <= 0 || event.getLevel().getRandom().nextInt(100) >= chancePercent) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        EasterBunnyEntity easterBunny = AntarchyForgeEntities.EASTER_BUNNY.get().create(level);
        if (easterBunny == null) {
            return;
        }

        easterBunny.moveTo(event.getX(), event.getY(), event.getZ(), rabbit.getYRot(), rabbit.getXRot());
        easterBunny.finalizeSpawn(level, level.getCurrentDifficultyAt(event.getEntity().blockPosition()), MobSpawnType.NATURAL, null, null);
        level.addFreshEntity(easterBunny);
        event.setResult(Event.Result.DENY);
    }

    static void handleDreadDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(AntarchyForgeMisc.DREAD.get())) {
            return;
        }

        entity.removeEffect(AntarchyForgeMisc.DREAD.get());
    }

    static void handleFallenKingCrownKill(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Enemy)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(AntarchyForgeItems.FALLEN_KING_CROWN.get())) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 6000, 0, false, true, true));
    }

    private static final int ANTIMETAL_INVERTED_REFRESH_TICKS = 20;
    private static final int ANTIMETAL_INVERTED_REFRESH_THRESHOLD_TICKS = 12;
    private static final int ANTIMETAL_PLAYER_SCAN_INTERVAL_TICKS = 2;
    private static final int ANTIMETAL_MOB_SCAN_INTERVAL_TICKS = 10;

    static void tickOverheadInversion(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level().isClientSide()) {
            return;
        }
        if (livingEntity instanceof Player player && player.isSpectator()) {
            return;
        }

        MobEffectInstance existingInverted = livingEntity.getEffect(AnthologyEffects.inverted());
        long gameTime = livingEntity.level().getGameTime();
        if (!shouldScanOverheadAntimetal(livingEntity, existingInverted, gameTime)) {
            return;
        }

        long startNanos = System.nanoTime();
        boolean directlyBelowAntimetal = isDirectlyBelowAntimetal(livingEntity);
        double elapsedMs = (System.nanoTime() - startNanos) / 1_000_000.0D;
        if (elapsedMs >= OVERHEAD_INVERSION_WARN_MS
                && gameTime - lastOverheadInversionWarnTick >= OVERHEAD_INVERSION_WARN_INTERVAL_TICKS) {
            lastOverheadInversionWarnTick = gameTime;
            Antarchy.LOGGER.warn(
                    "[antarchy-gravity] slow overhead antimetal scan targetType={} uuid={} pos=({}, {}, {}) dim={} elapsedMs={} inverted={} forced={} gameTime={}",
                    livingEntity.getType(), livingEntity.getUUID(), livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                    livingEntity.level().dimension().location(), elapsedMs,
                    GravityApi.isGravityInverted(livingEntity),
                    GravityApi.isGravityForced(livingEntity),
                    gameTime
            );
        }
        if (!directlyBelowAntimetal) {
            return;
        }

        if (existingInverted == null || existingInverted.getDuration() <= ANTIMETAL_INVERTED_REFRESH_THRESHOLD_TICKS) {
            livingEntity.addEffect(new MobEffectInstance(AnthologyEffects.inverted(), ANTIMETAL_INVERTED_REFRESH_TICKS, 0, false, false, false));
            spawnAntimetalInversionParticles(livingEntity);
        }
    }

    private static boolean shouldScanOverheadAntimetal(LivingEntity entity, MobEffectInstance existingInverted, long gameTime) {
        if (existingInverted != null && existingInverted.getDuration() <= ANTIMETAL_INVERTED_REFRESH_THRESHOLD_TICKS) {
            return true;
        }
        int interval = entity instanceof Player ? ANTIMETAL_PLAYER_SCAN_INTERVAL_TICKS : ANTIMETAL_MOB_SCAN_INTERVAL_TICKS;
        long phase = (long) entity.getUUID().hashCode() + gameTime;
        return Math.floorMod(phase, interval) == 0L;
    }

    private static boolean isDirectlyBelowAntimetal(LivingEntity entity) {
        AABB bounds = entity.getBoundingBox().deflate(1.0E-3D);

        int minX = Mth.floor(bounds.minX);
        int maxX = Mth.floor(bounds.maxX);
        int maxY = Mth.floor(bounds.maxY);
        int minZ = Mth.floor(bounds.minZ);
        int maxZ = Mth.floor(bounds.maxZ);

        BlockPos.MutableBlockPos crystalPos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                crystalPos.set(x, maxY + 1, z);
                if (entity.level().getBlockState(crystalPos).is(AntarchyTags.Blocks.ANTIMETAL_INVERSION_BLOCKS)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void spawnAntimetalInversionParticles(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = entity.getX();
        double y = entity.getBoundingBox().maxY + 0.1D;
        double z = entity.getZ();
        serverLevel.sendParticles(
                new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 1.0F),
                x,
                y,
                z,
                6,
                0.2D,
                0.05D,
                0.2D,
                0.0D
        );
    }

    static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AntarchyForgeMisc.BLOODGLASS_MAX_HEARTS.get());
        event.add(EntityType.PLAYER, AntarchyForgeMisc.DOUBLE_DAMAGE_CHANCE.get());
    }

    static void handleBloodglassPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) {
            return;
        }
        BloodglassAccess access = (BloodglassAccess) sp;
        access.antarchy$setArmorShieldsActive(0);
        access.antarchy$setArmorShieldLostCount(0);
        access.antarchy$setArmorRechargeTimer(0);
        access.antarchy$setAppleShieldsActive(0);
        access.antarchy$setAppleShieldLostCount(0);
        access.antarchy$setAppleRechargeTimer(0);
        syncBloodglass(sp);
    }

    static void handleBloodglassRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) {
            return;
        }
        BloodglassAccess access = (BloodglassAccess) sp;
        int bcCount = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET}) {
            if (sp.getItemBySlot(slot).getItem() instanceof BloodCrystalArmorItem) bcCount++;
        }
        int shields = Math.min(bcCount, AntarchySettings.bloodCrystalHardMaxShields());
        access.antarchy$setArmorShieldsActive(shields);
        access.antarchy$setArmorShieldLostCount(0);
        access.antarchy$setArmorRechargeTimer(0);
        syncBloodglass(sp);
    }

    static void handleBloodglassLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) {
            return;
        }
        BloodglassAccess access = (BloodglassAccess) sp;
        if (sp.hasEffect(AntarchyForgeMisc.BLOODGLASS_WARD.get())) {
            MobEffectInstance effect = sp.getEffect(AntarchyForgeMisc.BLOODGLASS_WARD.get());
            if (effect != null) {
                int cap = Math.max(0, AntarchySettings.bloodCrystalHardMaxShields()
                        - access.antarchy$getArmorShieldsActive()
                        - access.antarchy$getArmorShieldLostCount());
                int shieldCount = Math.min(effect.getAmplifier() + 1, cap);
                access.antarchy$setAppleShieldsActive(shieldCount);
                access.antarchy$setAppleShieldLostCount(0);
                access.antarchy$setAppleRechargeTimer(0);
            }
        }
        syncBloodglass(sp);
    }

    private static void syncBloodglass(ServerPlayer player) {
        BloodglassAccess access = (BloodglassAccess) player;
        AntarchyForgeNetworkCore.sendToPlayer(player,
                new BloodglassStatePayload(
                        access.antarchy$getTotalShieldsActive(),
                        access.antarchy$getTotalShieldsMax()
                )
        );
    }

    static void handleBloodglassWardApplied(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() != AntarchyForgeMisc.BLOODGLASS_WARD.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }

        BloodglassAccess access = (BloodglassAccess) player;
        int targetCap = Math.min(
                event.getEffectInstance().getAmplifier() + 1,
                Math.max(0, AntarchySettings.bloodCrystalHardMaxShields()
                        - access.antarchy$getArmorShieldsActive() - access.antarchy$getArmorShieldLostCount())
        );
        int currentCap = access.antarchy$getAppleShieldsActive() + access.antarchy$getAppleShieldLostCount();

        if (targetCap > currentCap) {
            access.antarchy$setAppleShieldsActive(access.antarchy$getAppleShieldsActive() + (targetCap - currentCap));
            syncBloodglass((ServerPlayer) player);
        }
    }

    private static void clearAppleShields(Player player) {
        BloodglassAccess access = (BloodglassAccess) player;
        access.antarchy$setAppleShieldsActive(0);
        access.antarchy$setAppleShieldLostCount(0);
        access.antarchy$setAppleRechargeTimer(0);
        if (player instanceof ServerPlayer sp) syncBloodglass(sp);
    }

    static void handleBloodglassWardRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() != AntarchyForgeMisc.BLOODGLASS_WARD.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }
        clearAppleShields(player);
    }

    static void handleBloodglassWardExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance().getEffect() != AntarchyForgeMisc.BLOODGLASS_WARD.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }
        clearAppleShields(player);
    }

    static void tickBloodglassRecharge(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }

        BloodglassAccess access = (BloodglassAccess) player;
        boolean changed = false;
        int hardCap = AntarchySettings.bloodCrystalHardMaxShields();

        if (access.antarchy$getArmorShieldLostCount() > 0) {
            int timer = access.antarchy$getArmorRechargeTimer();
            if (timer > 1) {
                access.antarchy$setArmorRechargeTimer(timer - 1);
            } else if (timer == 1) {
                int currentTotal = access.antarchy$getTotalShieldsActive();
                if (currentTotal < hardCap) {
                    access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() + 1);
                    access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() - 1);
                    changed = true;
                }
                if (access.antarchy$getArmorShieldLostCount() > 0) {
                    access.antarchy$setArmorRechargeTimer(AntarchySettings.bloodCrystalArmorShieldRechargeTicks());
                } else {
                    access.antarchy$setArmorRechargeTimer(0);
                }
            }
        }

        if (access.antarchy$getAppleShieldLostCount() > 0 && player.hasEffect(AntarchyForgeMisc.BLOODGLASS_WARD.get())) {
            int timer = access.antarchy$getAppleRechargeTimer();
            if (timer > 1) {
                access.antarchy$setAppleRechargeTimer(timer - 1);
            } else if (timer == 1) {
                int currentTotal = access.antarchy$getTotalShieldsActive();
                if (currentTotal < hardCap) {
                    access.antarchy$setAppleShieldsActive(access.antarchy$getAppleShieldsActive() + 1);
                    access.antarchy$setAppleShieldLostCount(access.antarchy$getAppleShieldLostCount() - 1);
                    changed = true;
                }
                if (access.antarchy$getAppleShieldLostCount() > 0) {
                    access.antarchy$setAppleRechargeTimer(AntarchySettings.bloodCrystalAppleShieldRechargeTicks());
                } else {
                    access.antarchy$setAppleRechargeTimer(0);
                }
            }
        }

        if (changed && player instanceof ServerPlayer sp) {
            syncBloodglass(sp);
            sp.playNotifySound(
                    AntarchySoundEvents.BLOODGLASS_WARD_HEART_REGEN.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
    static void handleBloodCrystalArmorEquip(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }

        EquipmentSlot slot = event.getSlot();
        if (slot != EquipmentSlot.HEAD
                && slot != EquipmentSlot.CHEST
                && slot != EquipmentSlot.LEGS
                && slot != EquipmentSlot.FEET) return;

        boolean fromIsBC = event.getFrom().getItem() instanceof BloodCrystalArmorItem;
        boolean toIsBC = event.getTo().getItem() instanceof BloodCrystalArmorItem;

        BloodglassAccess access = (BloodglassAccess) player;

        if (fromIsBC && !toIsBC) {

            if (access.antarchy$getArmorShieldsActive() > 0) {
                access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() - 1);
            } else if (access.antarchy$getArmorShieldLostCount() > 0) {
                access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() - 1);
                if (access.antarchy$getArmorShieldLostCount() == 0) {
                    access.antarchy$setArmorRechargeTimer(0);
                }
            }
        } else if (!fromIsBC && toIsBC) {

            int bcPieces = countBCArmorPieces(player);
            int currentTotal = access.antarchy$getArmorShieldsActive() + access.antarchy$getArmorShieldLostCount();
            if (currentTotal < bcPieces) {
                access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() + 1);
                access.antarchy$setArmorRechargeTimer(AntarchySettings.bloodCrystalArmorShieldRechargeTicks());
            }
        } else if (fromIsBC) {

            if (access.antarchy$getArmorShieldsActive() > 0) {
                access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() - 1);
            } else if (access.antarchy$getArmorShieldLostCount() > 0) {
                access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() - 1);
            }
            access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() + 1);
            access.antarchy$setArmorRechargeTimer(AntarchySettings.bloodCrystalArmorShieldRechargeTicks());
        }

        syncBloodglass((ServerPlayer) player);
    }

    private static int countBCArmorPieces(Player player) {
        int count = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET}) {
            if (player.getItemBySlot(slot).getItem() instanceof BloodCrystalArmorItem) count++;
        }
        return count;
    }

    static void handleBloodCrystalBootsFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!GravityApi.isGravityInverted(player)) {
            return;
        }
        if (AntarchyEnchantments.featherRisingLevel(player) <= 0) {
            return;
        }
        event.setCanceled(true);
    }

    static void handleBloodglassShield(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }

        if (event.getAmount() <= 1.0F) {
            return;
        }

        if (event.getSource().is(AntarchyTags.DamageType.BYPASSES_BLOODGLASS)) {
            return;
        }

        BloodglassAccess access = (BloodglassAccess) player;
        int totalActive = access.antarchy$getTotalShieldsActive();
        if (totalActive <= 0) {
            return;
        }

        if (access.antarchy$getAppleShieldsActive() > 0) {
            access.antarchy$setAppleShieldsActive(access.antarchy$getAppleShieldsActive() - 1);
            access.antarchy$setAppleShieldLostCount(access.antarchy$getAppleShieldLostCount() + 1);
            if (access.antarchy$getAppleRechargeTimer() == 0) {
                access.antarchy$setAppleRechargeTimer(AntarchySettings.bloodCrystalAppleShieldRechargeTicks());
            }
        } else {
            access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() - 1);
            access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() + 1);
            if (access.antarchy$getArmorRechargeTimer() == 0) {
                access.antarchy$setArmorRechargeTimer(AntarchySettings.bloodCrystalArmorShieldRechargeTicks());
            }
        }

        event.setCanceled(true);

        ServerLevel serverLevel = (ServerLevel) player.level();

        serverLevel.broadcastEntityEvent(player, (byte) 2);

        serverLevel.playSound(null, player.blockPosition(),
                AntarchySoundEvents.BLOODGLASS_WARD_HEART_BREAK.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);

        serverLevel.sendParticles(
                new DustParticleOptions(
                        new Vector3f(0.85F, 0.18F, 0.38F), 1.2F),
                player.getX(), player.getEyeY(), player.getZ(),
                35, 0.35D, 0.55D, 0.35D, 0.12D);

        syncBloodglass((ServerPlayer) player);
    }
    static void handleUltimateBowDamage(LivingHurtEvent event) {
        if (!UltimateGearHelper.isUltimateBowArrow(event.getSource().getDirectEntity())) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target instanceof Player player) {
            event.setAmount(0.0F);
            player.invulnerableTime = 0;
            player.clearFire();
            player.heal((float) AntarchySettings.ultimateBowPlayerHeal());
            return;
        }

    }

    static void handleUltimateCrossbowDamage(LivingHurtEvent event) {
        if (!UltimateGearHelper.isUltimateCrossbowProjectile(event.getSource().getDirectEntity())) {
            return;
        }
    }

    static void handleNightmareArmorDoubleDamage(LivingHurtEvent event) {
        if (RoyalGuardianSwordAbilities.isApplyingSecondaryDamage()) {
            return;
        }
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if (!attacker.getAttributes().hasAttribute(AntarchyForgeMisc.DOUBLE_DAMAGE_CHANCE.get())) {
            return;
        }
        double chance = attacker.getAttributeValue(AntarchyForgeMisc.DOUBLE_DAMAGE_CHANCE.get());
        if (chance <= 0.0D) {
            return;
        }
        if (attacker.getRandom().nextDouble() < chance) {
            event.setAmount(event.getAmount() * 2.0F);
            LivingEntity victim = event.getEntity();
            if (victim.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, victim.blockPosition(),
                        AntarchySoundEvents.NIGHTMARE_BITE.get(),
                        SoundSource.PLAYERS, 0.9F, 0.85F + victim.getRandom().nextFloat() * 0.3F);
                serverLevel.sendParticles(
                        new DustParticleOptions(new Vector3f(0.55F, 0.0F, 0.05F), 1.4F),
                        victim.getX(), victim.getY() + victim.getBbHeight() * 0.5D, victim.getZ(),
                        30, 0.4D, 0.5D, 0.4D, 0.15D);
            }
        }
    }

    static void handleBossMagicBurstMitigation(LivingHurtEvent event) {
        LivingEntity boss = event.getEntity();
        if (!boss.getType().is(AntarchyTags.Entities.SPELL_RESISTANT_BOSS)) {
            return;
        }
        if (!BossCombatUtil.isMagicBurstSource(event.getSource())) {
            return;
        }

        float maxHealth = boss.getMaxHealth();
        float amount = event.getAmount();

        float perHitCap = maxHealth * (float) AntarchySettings.bossMagicPerHitCapFraction();
        if (amount > perHitCap) {
            amount = perHitCap;
        }

        float windowCap = maxHealth * (float) AntarchySettings.bossMagicWindowCapFraction();
        float remainingBudget = BossCombatUtil.remainingWindowBudget(boss, windowCap);
        boolean breachedWindow = amount > remainingBudget;
        if (breachedWindow) {
            amount = Math.max(0.0F, remainingBudget);
        }

        BossCombatUtil.trackMagicBurstWindow(boss, amount, AntarchySettings.bossMagicWindowTicks());

        if (breachedWindow) {
            int breaches = BossCombatUtil.recordMagicWindowBreach(boss);
            if (breaches >= AntarchySettings.bossMagicWardTriggerBreaches()) {
                BossCombatUtil.triggerMagicWard(boss, AntarchySettings.bossMagicWardDurationTicks());
            }
        }

        event.setAmount(amount);
    }

    static void handleAntiwaterDamage(LivingAttackEvent event) {
        if (!event.getSource().is(DamageTypes.FALL)) {
            return;
        }

        LivingEntity livingEntity = event.getEntity();
        if (!intersectsAntiwater(livingEntity, livingEntity.getBoundingBox().inflate(0.05D))) {
            return;
        }

        event.setCanceled(true);
    }

    static void handleScorpionWhipAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof ScorpionWhipItem) || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        if (ScorpionWhipTetherManager.hasTether(player)) {
            event.setCanceled(true);
            if (ScorpionWhipTetherManager.isTetheredTo(player, target)) {
                player.resetAttackStrengthTicker();
            }
        }
    }

    static void handleAttitudeAdjusterAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof AttitudeAdjusterItem) || player.getAttackStrengthScale(0.5F) < 0.95F) {
            return;
        }
        AttitudeAdjusterSlamManager.markSpecialHit(player);
    }

    static void handlePortalGunAttackEntity(AttackEntityEvent event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof PortalGunItem
                || event.getEntity().getOffhandItem().getItem() instanceof PortalGunItem) {
            event.setCanceled(true);
        }
    }

    static void handleScorpionWhipLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof ScorpionWhipItem) || !ScorpionWhipTetherManager.hasTether(player)) {
            return;
        }
        event.setCanceled(true);
    }

    static void handlePortalGunLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity().getMainHandItem().getItem() instanceof PortalGunItem)) {
            return;
        }
        event.setCanceled(true);
    }

    static void handleKrakenRitualRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = KrakenRitualManager.tryStart(event.getEntity(), event.getLevel(), event.getHand(), event.getPos());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    static void handleScorpionWhipRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof ScorpionWhipItem)) {
            return;
        }
        if (!ScorpionWhipTetherManager.hasTether(player)) {
            return;
        }
        if (ScorpionWhipTetherManager.pullAndDetach(player)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    static void handleGiantFryingPanRightClickItem(PlayerInteractEvent.RightClickItem event) {
        InteractionResult result = GiantFryingPanItem.handleMainHandUse(event.getLevel(), event.getEntity(), event.getHand());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    static void handleScorpionWhipRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof ScorpionWhipItem)) {
            return;
        }
        if (!ScorpionWhipTetherManager.hasTether(player)) {
            return;
        }
        if (ScorpionWhipTetherManager.pullAndDetach(player)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    static void handleParalyzedAttackEntity(AttackEntityEvent event) {
        if (isParalyzed(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    static void handleParalyzedLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isParalyzed(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    static void handleParalyzedRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isParalyzed(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    static void handleParalyzedRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isParalyzed(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    static void handleParalyzedEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isParalyzed(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    static void handleParalyzedEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isParalyzed(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    private static boolean isParalyzed(Player player) {
        return player.hasEffect(AntarchyForgeMisc.PARALYZED.get());
    }

    static void tickDreadAndIchor(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }
        if (player.isInFluidType(AntarchyForgeFluidTypes.ICHOR_TYPE.get()) && AntarchySettings.ichorWitherEnabled()) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true, true));
        }
    }

    static void tickScorpionWhips(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            ScorpionWhipTetherManager.tick(player);
        }
    }

    static void tickWormHooks(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            WormHookTetherManager.tick(player);
        }
    }

    static void tickAttitudeAdjusterSlams(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            AttitudeAdjusterSlamManager.tick(level);
        }
    }

    static void handleAntiwaterFall(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (intersectsAntiwater(livingEntity, livingEntity.getBoundingBox().inflate(0.05D))) {
            event.setCanceled(true);
        }
    }

    private static boolean intersectsAntiwater(Entity entity, AABB box) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = entity.level().getFluidState(cursor);
                    if (AntarchyFluidChecks.isAntiwater(fluidState)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static void onInvertedEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null) {
            return;
        }
        if (event.getEffectInstance().getEffect() != AnthologyEffects.inverted()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            MountedGravityHelper.syncConnectedStackIgnoring(entity, entity);
            if (entity instanceof Player player) {
                notifyNearbyReveriesOfInversionChange(player);
            }
        }
    }

    private static void notifyNearbyReveriesOfInversionChange(Player player) {
        for (ReverieEntity reverie : player.level().getEntitiesOfClass(
                ReverieEntity.class,
                player.getBoundingBox().inflate(48.0D)
        )) {
            reverie.syncFocusInversionState();
        }
    }

    private static void addPotionMix(Potion input, Item ingredient, Potion output) {
        PotionBrewingInvoker.antarchy$addMix(input, ingredient, output);
    }

    static void registerBrewingRecipes() {
        TagKey<Item> rootsTag = TagKey.create(
                Registries.ITEM,
                new ResourceLocation(Antarchy.MODID, "roots")
        );
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(
                Ingredient.of(Items.POTION),
                Ingredient.of(AntarchyForgeItems.CORN.get()),
                new ItemStack(AntarchyForgeItems.HIGH_FRUCTOSE_CORN_SYRUP.get())
        ) {
            @Override
            public boolean isInput(ItemStack stack) {
                return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
            }

            @Override
            public boolean isIngredient(ItemStack stack) {
                return stack.is(AntarchyForgeItems.CORN.get());
            }

            @Override
            public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
                return CustomBrewingRecipes.getOutput(input, ingredient);
            }
        });
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(
                Ingredient.of(AntarchyForgeItems.HIGH_FRUCTOSE_CORN_SYRUP.get()),
                Ingredient.of(rootsTag),
                new ItemStack(AntarchyForgeItems.ROOT_BEER.get())
        ) {
            @Override
            public boolean isInput(ItemStack stack) {
                return stack.is(AntarchyForgeItems.HIGH_FRUCTOSE_CORN_SYRUP.get());
            }

            @Override
            public boolean isIngredient(ItemStack stack) {
                return stack.is(rootsTag);
            }

            @Override
            public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
                return CustomBrewingRecipes.getOutput(input, ingredient);
            }
        });
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.LUCID_EYE.get(), AntarchyForgeMisc.INVERSION.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.FADED_MEMORY.get(), AntarchyForgeMisc.REMEMBRANCE_POTION.get());
        addPotionMix(AntarchyForgeMisc.REMEMBRANCE_POTION.get(), Items.REDSTONE, AntarchyForgeMisc.LONG_REMEMBRANCE.get());
        addPotionMix(AntarchyForgeMisc.INVERSION.get(), Items.REDSTONE, AntarchyForgeMisc.LONG_INVERSION.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.STINK_BUG.get(), AntarchyForgeMisc.STINKY_POTION.get());
        addPotionMix(AntarchyForgeMisc.STINKY_POTION.get(), Items.REDSTONE, AntarchyForgeMisc.LONG_STINKY.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.BASILISK_FANG.get(), AntarchyForgeMisc.PARALYSIS.get());
        addPotionMix(AntarchyForgeMisc.PARALYSIS.get(), Items.REDSTONE, AntarchyForgeMisc.LONG_PARALYSIS.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.MOLEWORM_ITEM.get(), AntarchyForgeMisc.HASTE.get());
        addPotionMix(AntarchyForgeMisc.HASTE.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.STRONG_HASTE.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.STRATOSHARK_FIN.get(), Potions.SLOW_FALLING);
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.SPRINGBUG_LEG.get(), Potions.LEAPING);
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.CORNEA_EAR.get(), Potions.NIGHT_VISION);
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.URANIUM_NUGGET.get(), AntarchyForgeMisc.SHRINKING.get());
        addPotionMix(AntarchyForgeMisc.SHRINKING.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.STRONG_SHRINKING.get());
        addPotionMix(AntarchyForgeMisc.STRONG_SHRINKING.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.EXTREME_SHRINKING.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.TITANIUM_NUGGET.get(), AntarchyForgeMisc.GROWING.get());
        addPotionMix(AntarchyForgeMisc.GROWING.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.STRONG_GROWING.get());
        addPotionMix(AntarchyForgeMisc.STRONG_GROWING.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.EXTREME_GROWING.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.KING_SCALE.get(), AntarchyForgeMisc.COMMAND.get());
        addPotionMix(AntarchyForgeMisc.COMMAND.get(), Items.REDSTONE, AntarchyForgeMisc.LONG_COMMAND.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.QUEEN_SCALE.get(), AntarchyForgeMisc.TIME_DILATION.get());
        addPotionMix(AntarchyForgeMisc.TIME_DILATION.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.LONG_TIME_DILATION.get());
        addPotionMix(AntarchyForgeMisc.TIME_DILATION.get(), Items.FERMENTED_SPIDER_EYE, AntarchyForgeMisc.TIME_CONTRACTION.get());
        addPotionMix(AntarchyForgeMisc.TIME_CONTRACTION.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.LONG_TIME_CONTRACTION.get());

    }

    static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance() == null) {
            return;
        }

        if (event.getEffectInstance().getEffect() == AntarchyForgeMisc.DREAD.get() && !(event.getEntity() instanceof Player)) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (event.getEntity().getType().is(AntarchyTags.Entities.INVERTED_IMMUNE)
                && event.getEffectInstance().getEffect() == AnthologyEffects.inverted()) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (event.getEntity().getType().is(AntarchyTags.Entities.SIZE_CHANGING_IMMUNE)
                && (event.getEffectInstance().getEffect() == AntarchyForgeMisc.SHRINKING_EFFECT.get() || event.getEffectInstance().getEffect() == AntarchyForgeMisc.GROWTH_EFFECT.get())) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (event.getEntity().getType().is(AntarchyTags.Entities.PARALYSIS_IMMUNE)
                && event.getEffectInstance().getEffect() == AntarchyForgeMisc.PARALYZED.get()) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (event.getEntity() instanceof Player player
                && event.getEffectInstance().getEffect() == AnthologyEffects.inverted()
                && !player.hasEffect(AnthologyEffects.inverted())) {
            notifyNearbyReveriesOfInversionChange(player);
        }
    }

    static void onInvertedEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() != AnthologyEffects.inverted()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            MountedGravityHelper.syncConnectedStackIgnoring(entity, entity);
            if (entity instanceof Player player) {
                notifyNearbyReveriesOfInversionChange(player);
            }
        }
    }

    static void handleBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() != ToolActions.AXE_STRIP) {
            return;
        }

        BlockState state = event.getState();
        if (!state.hasProperty(RotatedPillarBlock.AXIS)) {
            return;
        }

        if (state.is(AntarchyForgeBlocks.OURANWOOD_LOG.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_OURANWOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.MOSSY_OURANWOOD_LOG.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_OURANWOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.OURANWOOD_WOOD.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_OURANWOOD_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.MOSSY_OURANWOOD_WOOD.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_OURANWOOD_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.PEACH_LOG.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_PEACH_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.CRANBERRY_LOG.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_CRANBERRY_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.CRANBERRY_WOOD.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_CRANBERRY_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }
        if (state.is(AntarchyForgeBlocks.PEACH_WOOD.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_PEACH_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.NADIR_LOG.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_NADIR_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.NADIR_WOOD.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_NADIR_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.ROYAL_LOG.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_ROYAL_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyForgeBlocks.ROYAL_WOOD.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_ROYAL_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
        }
    }

    static void onCommonSetup(FMLCommonSetupEvent event) {
        BloodCrystalShardItem.setBloodglassSync(AntarchyForgeEvents::syncBloodglass);
        if (InfinityCompat.get().isAvailable()) {
            InfinityConfigRefresh.refreshIfStale(FMLPaths.CONFIGDIR.get());
        }
        event.enqueueWork(() -> {
            registerBrewingRecipes();
            DispenserBlock.registerBehavior(AntarchyForgeItems.SHRINK_RAY.get(), new SizeRayDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.GROWTH_RAY.get(), new SizeRayDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.SQUIDZOOKA.get(), new SquidzookaDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.RPO_LAUNCHER.get(), new RpoLauncherDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.ANVIL_CANNON.get(), new AnvilCannonDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.WATER_CANNON.get(), new WaterCannonDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.VORTEX_CHARGE.get(), new VortexChargeDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.EYE_OF_THE_STORM.get(), new EyeOfTheStormDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.BUG_SPRAY.get(), new BugSprayDispenseBehavior());
            AntimetalMinecartDispenseBehavior antimetalMinecartDispenseBehavior = new AntimetalMinecartDispenseBehavior();
            DispenserBlock.registerBehavior(Items.MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.CHEST_MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.FURNACE_MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.TNT_MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.HOPPER_MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.COMMAND_BLOCK_MINECART, antimetalMinecartDispenseBehavior);
            LucidEyeProjectileEntity.defaultItemSupplier = () -> AntarchyForgeItems.LUCID_PEARL.get();
            LucidEntity.invertedEffectSupplier = AnthologyEffects::inverted;
            FluidInteractionRegistry.addInteraction(
                    AntarchyForgeFluidTypes.ANTIWATER_TYPE.get(),
                    new FluidInteractionRegistry.InteractionInformation(
                            ForgeMod.WATER_TYPE.get(),
                            fluidState -> fluidState.isSource()
                                    ? AntarchyForgeBlocks.ANTIMETAL.get().defaultBlockState()
                                    : AntarchyForgeBlocks.NYXITE.get().defaultBlockState()
                    )
            );
            LucidEntity.boltEntityTypeSupplier = () -> AntarchyForgeEntities.LUCID_BOLT.get();
            LucidBoltEntity.invertedEffectSupplier = AnthologyEffects::inverted;
            LucidEyeProjectileEntity.invertedEffectSupplier = AnthologyEffects::inverted;
            VortexEntity.windVortexTypeSupplier = () -> AntarchyForgeEntities.WIND_VORTEX.get();
            VortexChargeProjectileEntity.defaultItemSupplier = () -> AntarchyForgeItems.VORTEX_CHARGE.get();
            VortexChargeProjectileEntity.projectileTypeSupplier = () -> AntarchyForgeEntities.VORTEX_CHARGE_PROJECTILE.get();
            VortexChargeProjectileEntity.windVortexTypeSupplier = () -> AntarchyForgeEntities.WIND_VORTEX.get();
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.UMBRAL_MOSS_BLOCK.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.UMBRAL_MOSS_CARPET.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.BLUSH_MOSS_BLOCK.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.PRIMORDIAL_MOSS_BLOCK.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.PRIMORDIAL_MOSS_CARPET.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.BLUSH_MOSS_CARPET.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.HUSHWEED.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.CORNEA_EAR.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.PEACH_LEAVES.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.PEACH_SAPLING_ITEM.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.NADIR_SAPLING_ITEM.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.NADIR_FERN_ITEM.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.LARGE_NADIR_FERN_ITEM.get(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.DUSKBELL_ITEM.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.WHIRLFLOWER_ITEM.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.GOREVINE_ITEM.get(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.CORN.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.CORN_SEEDS.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.PEACH.get(), 0.65F);
        });
    }
}
