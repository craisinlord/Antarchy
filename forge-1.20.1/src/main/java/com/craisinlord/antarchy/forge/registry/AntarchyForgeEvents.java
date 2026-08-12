package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.recipe.CustomBrewingRecipes;
import com.craisinlord.antarchy.content.block.DuctTapeBlock;
import com.craisinlord.antarchy.content.dispenser.RpoLauncherDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.SizeRayDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.SquidzookaDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.WaterCannonDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.AntimetalMinecartDispenseBehavior;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.craisinlord.antarchy.content.bloodglass.BloodglassAccess;
import com.craisinlord.antarchy.content.entity.*;
import com.craisinlord.antarchy.content.entity.ant.RainbowAntEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEyeProjectileEntity;
import com.craisinlord.antarchy.content.entity.multipart.MultipartFramework;
import com.craisinlord.antarchy.content.entity.trades.DrTrayaurusTradeManager;
import com.craisinlord.antarchy.content.horde.CavarynHordeManager;
import com.craisinlord.antarchy.content.item.*;
import com.craisinlord.antarchy.content.item.ultimate.UltimateGearHelper;
import com.craisinlord.antarchy.content.movement.DreamSandLowGravityAccess;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.portal.PermanentPortalManager;
import com.craisinlord.antarchy.forge.AntarchyForgeFluidTypes;
import com.craisinlord.antarchy.forge.entity.multipart.MultipartPartEntity;
import com.craisinlord.antarchy.forge.network.AntarchyGravityNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import com.craisinlord.antarchy.content.item.MinersDreamExcavationManager;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

public final class AntarchyForgeEvents {
    private AntarchyForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyForgeEvents::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onMissileSquidDeath);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onPermanentPortalSacrifice);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onLivingBreathe);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleNaturalRabbitReplacement);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBlockToolModification);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onMobEffectApplicable);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onInvertedEffectRemoved);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onInvertedEffectExpired);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleUltimateBowDamage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleUltimateCrossbowDamage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleNightmareArmorDoubleDamage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleScorpionWhipAttackEntity);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleAttitudeAdjusterAttackEntity);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleScorpionWhipLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleScorpionWhipRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleScorpionWhipRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedAttackEntity);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleParalyzedEntityInteractSpecific);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleDreamSandJump);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickDreadAndIchor);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickScorpionWhips);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickDreamSandLowGravity);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickMinersDreamExcavations);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleAntiwaterDamage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleAntiwaterFall);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleDreamSandFall);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodCrystalBootsFall);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassShield);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodCrystalArmorEquip);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassWardApplied);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassWardRemoved);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassWardExpired);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickBloodglassRecharge);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassPlayerDeath);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassRespawn);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleBloodglassLogin);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleCavarynHordeKill);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleCavarynHordeBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickCavarynHordes);
        modEventBus.addListener(AntarchyForgeEvents::modifyEntityAttributes);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickOverheadInversion);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickDuctTapeStickiness);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleDreadDeath);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleFallenKingCrownKill);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleDreadBedSleep);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleStartTracking);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleDreamSandLogout);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeLogin);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeLogout);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeDeath);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeRespawn);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::handleTigerEyeDimensionChange);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::tickTigerEyeCamouflage);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::registerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onVillagerTrades);
        MinecraftForge.EVENT_BUS.addListener(AntarchyForgeEvents::onWandererTrades);
    }

    static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.FARMER) {
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
            if (missileSquid.isSpawnedByKraken()) return;
        } else if (event.getEntity().getType() != EntityType.SQUID) {
            return;
        }
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        if (!(event.getEntity().getKillCredit() instanceof Player killer)) return;

        boolean requireBadOmen = AntarchySettings.krakenRequireBadOmenToSummon();
        int badOmenLevel = badOmenLevel(killer);
        if (requireBadOmen && badOmenLevel <= 0) return;

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
        if (event.phase != TickEvent.Phase.END) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            CavarynHordeManager.tick(level);
            com.craisinlord.antarchy.content.horde.CavarynCreatureSpawner.tick(level);
        }
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
            if (kraken == null) continue;
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
    }
    static void handleDreamSandLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearDreamSandLowGravity(event.getEntity());
    }
    private static void clearDreamSandLowGravity(LivingEntity livingEntity) {
        DreamSandLowGravityAccess access = dreamSandAccess(livingEntity);
        access.antarchy$setDreamSandLowGravityActive(false);
        access.antarchy$setDreamSandLowGravityTicksRemaining(0);
        access.antarchy$setDreamSandLandingGraceTicks(0);
    }
    private static DreamSandLowGravityAccess dreamSandAccess(LivingEntity livingEntity) {
        return (DreamSandLowGravityAccess) livingEntity;
    }

    static void handleStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget().level().isClientSide()) {
            return;
        }

        AntarchyGravityNetworking.syncEntity(event.getTarget());
        if (event.getTarget() instanceof ServerPlayer trackedPlayer && event.getEntity() instanceof ServerPlayer trackingPlayer) {
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.syncTo(trackingPlayer, trackedPlayer);
        }
    }

    static void handleTigerEyeLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.sync(player);
        }
    }

    static void handleTigerEyeLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController.deactivate(player, false);
        }
    }

    static void handleTigerEyeDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController.deactivate(player, false);
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.sync(player);
        }
    }

    static void handleTigerEyeRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController.deactivate(player, false);
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.sync(player);
        }
    }

    static void handleTigerEyeDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController.deactivate(player, false);
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.sync(player);
        }
    }

    static void tickTigerEyeCamouflage(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player instanceof ServerPlayer player
                && com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController.validateOrDeactivate(player)) {
            com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.sync(player);
        }
    }

    private static final String DREAD_BED_BUG_NEXT_SPAWN_KEY = Antarchy.MODID + ":dread_bed_bug_next_spawn";
    private static final int DREAD_BED_BUG_SPAWN_COOLDOWN_TICKS = 6000;
    static void handleDreadBedSleep(net.minecraftforge.event.entity.player.PlayerSleepInBedEvent event) {
        if (!event.getEntity().hasEffect(AntarchyForgeMisc.DREAD.get())) {
            return;
        }
        Player player = event.getEntity();
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.antarchy.dread_too_scared_to_sleep"), true);
        event.setResult(net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM);

        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        long gameTime = level.getGameTime();
        long nextSpawnTime = player.getPersistentData().getLong(DREAD_BED_BUG_NEXT_SPAWN_KEY);
        if (nextSpawnTime > gameTime) {
            return;
        }

        net.minecraft.core.BlockPos bedPos = event.getPos();
        com.craisinlord.antarchy.content.entity.BedBugEntity bedBug = AntarchyObjects.BED_BUG.get().create(level);
        if (bedBug == null) {
            return;
        }

        player.getPersistentData().putLong(DREAD_BED_BUG_NEXT_SPAWN_KEY, gameTime + DREAD_BED_BUG_SPAWN_COOLDOWN_TICKS);
        bedBug.moveTo(bedPos.getX() + 0.5D, bedPos.getY() + 0.5D, bedPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        bedBug.finalizeSpawn(level, level.getCurrentDifficultyAt(bedPos),
                net.minecraft.world.entity.MobSpawnType.EVENT, null, null);
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
        EasterBunnyEntity easterBunny = AntarchyForgeEntites.EASTER_BUNNY.get().create(level);
        if (easterBunny == null) {
            return;
        }

        easterBunny.moveTo(event.getX(), event.getY(), event.getZ(), rabbit.getYRot(), rabbit.getXRot());
        easterBunny.finalizeSpawn(level, level.getCurrentDifficultyAt(event.getEntity().blockPosition()), MobSpawnType.NATURAL, null, null);
        level.addFreshEntity(easterBunny);
        event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
    }

    static void tickDuctTapeStickiness(LivingEvent.LivingTickEvent event) {
        if (DuctTapeBlock.shouldTickStuckEntity(event.getEntity())) {
            DuctTapeBlock.tickStuckEntity(event.getEntity());
        }
    }

    static void handleDreadDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        clearDreamSandLowGravity(entity);
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

        if (!player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(AntarchyForgeItems.FALLEN_KING_CROWN.get())) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 6000, 0, false, true, true));
    }

    private static final int ANTIMETAL_INVERTED_REFRESH_TICKS = 20;

    static void tickOverheadInversion(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level().isClientSide()) {
            return;
        }
        if (livingEntity instanceof Player player && player.isSpectator()) {
            return;
        }
        if (!isDirectlyBelowAntimetal(livingEntity)) {
            return;
        }

        MobEffectInstance existingInverted = livingEntity.getEffect(AntarchyForgeMisc.INVERTED.get());
        if (existingInverted == null || existingInverted.getDuration() <= 5) {
            livingEntity.addEffect(new MobEffectInstance(AntarchyForgeMisc.INVERTED.get(), ANTIMETAL_INVERTED_REFRESH_TICKS, 0, false, false, false));
            spawnAntimetalInversionParticles(livingEntity);
        }
    }

    private static boolean isInOrBelowAntimetalScaffolding(LivingEntity entity) {
        AABB bounds = entity.getBoundingBox().deflate(1.0E-3D);
        int minX = net.minecraft.util.Mth.floor(bounds.minX);
        int maxX = net.minecraft.util.Mth.floor(bounds.maxX);
        int minY = net.minecraft.util.Mth.floor(bounds.minY);
        int maxY = net.minecraft.util.Mth.floor(bounds.maxY);
        int minZ = net.minecraft.util.Mth.floor(bounds.minZ);
        int maxZ = net.minecraft.util.Mth.floor(bounds.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (entity.level().getBlockState(new net.minecraft.core.BlockPos(x, y, z))
                            .getBlock() instanceof com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock) {
                        return true;
                    }
                }
                net.minecraft.core.BlockPos abovePos = new net.minecraft.core.BlockPos(x, maxY + 1, z);
                if (entity.level().getBlockState(abovePos)
                        .getBlock() instanceof com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isDirectlyBelowAntimetal(LivingEntity entity) {
        AABB bounds = entity.getBoundingBox().deflate(1.0E-3D);

        int minX = net.minecraft.util.Mth.floor(bounds.minX);
        int maxX = net.minecraft.util.Mth.floor(bounds.maxX);
        int minY = net.minecraft.util.Mth.floor(bounds.minY);
        int maxY = net.minecraft.util.Mth.floor(bounds.maxY);
        int minZ = net.minecraft.util.Mth.floor(bounds.minZ);
        int maxZ = net.minecraft.util.Mth.floor(bounds.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    net.minecraft.core.BlockPos crystalPos = new net.minecraft.core.BlockPos(x, y + 1, z);
                    if (entity.level().getBlockState(crystalPos).is(AntarchyTags.Blocks.ANTIMETAL_INVERSION_BLOCKS)) {
                        return true;
                    }
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
                new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1.0F, 0.1F, 0.1F), 1.0F),
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
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
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
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        BloodglassAccess access = (BloodglassAccess) sp;
        int bcCount = 0;
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET}) {
            if (sp.getItemBySlot(slot).getItem() instanceof BloodCrystalArmorItem) bcCount++;
        }
        int shields = Math.min(bcCount, AntarchySettings.bloodCrystalHardMaxShields());
        access.antarchy$setArmorShieldsActive(shields);
        access.antarchy$setArmorShieldLostCount(0);
        access.antarchy$setArmorRechargeTimer(0);
        syncBloodglass(sp);
    }

    static void handleBloodglassLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        BloodglassAccess access = (BloodglassAccess) sp;
        if (sp.hasEffect(AntarchyForgeMisc.BLOODGLASS_WARD.get())) {
            net.minecraft.world.effect.MobEffectInstance effect = sp.getEffect(AntarchyForgeMisc.BLOODGLASS_WARD.get());
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
                new com.craisinlord.antarchy.content.network.BloodglassStatePayload(
                        access.antarchy$getTotalShieldsActive(),
                        access.antarchy$getTotalShieldsMax()
                )
        );
    }

    static void handleBloodglassWardApplied(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() != AntarchyForgeMisc.BLOODGLASS_WARD.get()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        BloodglassAccess access = (BloodglassAccess) player;
        int targetCap = Math.min(
                event.getEffectInstance().getAmplifier() + 1,
                Math.max(0, AntarchySettings.bloodCrystalHardMaxShields()
                        - access.antarchy$getArmorShieldsActive() - access.antarchy$getArmorShieldLostCount())
        );
        int currentCap = access.antarchy$getAppleShieldsActive() + access.antarchy$getAppleShieldLostCount();
        // Only grant newly-added capacity (e.g. first application, or a higher amplifier).
        // A mere duration refresh of an already-active ward must not reset shields that are
        // already lost, nor restart their recharge timer.
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
        if (event.getEffect() != AntarchyForgeMisc.BLOODGLASS_WARD.get()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        clearAppleShields(player);
    }

    static void handleBloodglassWardExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance().getEffect() != AntarchyForgeMisc.BLOODGLASS_WARD.get()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        clearAppleShields(player);
    }

    static void tickBloodglassRecharge(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

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
                    net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.6f);
        }
    }
    static void handleBloodCrystalArmorEquip(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        net.minecraft.world.entity.EquipmentSlot slot = event.getSlot();
        if (slot != net.minecraft.world.entity.EquipmentSlot.HEAD
                && slot != net.minecraft.world.entity.EquipmentSlot.CHEST
                && slot != net.minecraft.world.entity.EquipmentSlot.LEGS
                && slot != net.minecraft.world.entity.EquipmentSlot.FEET) return;

        boolean fromIsBC = event.getFrom().getItem() instanceof BloodCrystalArmorItem;
        boolean toIsBC = event.getTo().getItem() instanceof BloodCrystalArmorItem;

        BloodglassAccess access = (BloodglassAccess) player;

        if (fromIsBC && !toIsBC) {
            // Unequipped BC armor, remove one shield slot
            if (access.antarchy$getArmorShieldsActive() > 0) {
                access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() - 1);
            } else if (access.antarchy$getArmorShieldLostCount() > 0) {
                access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() - 1);
                if (access.antarchy$getArmorShieldLostCount() == 0) {
                    access.antarchy$setArmorRechargeTimer(0);
                }
            }
        } else if (!fromIsBC && toIsBC) {
            // Newly equipped BC armor,only add if not already accounted for (by respawn init)
            int bcPieces = countBCArmorPieces(player);
            int currentTotal = access.antarchy$getArmorShieldsActive() + access.antarchy$getArmorShieldLostCount();
            if (currentTotal < bcPieces) {
                access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() + 1);
                access.antarchy$setArmorRechargeTimer(AntarchySettings.bloodCrystalArmorShieldRechargeTicks());
            }
        } else if (fromIsBC) {
            // BC armor replaced with another BC armor (or same after re-equip sequence)
            // Remove old shield, start recharging new one and reset timer
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
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET}) {
            if (player.getItemBySlot(slot).getItem() instanceof BloodCrystalArmorItem) count++;
        }
        return count;
    }

    static void handleBloodCrystalBootsFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!com.craisinlord.antarchy.content.gravity.AntarchyGravityApi.isGravityInverted(player)) return;
        if (!(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof BloodCrystalArmorItem)) return;
        event.setCanceled(true);
    }

    static void handleBloodglassShield(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (event.getAmount() <= 1.0f) return;

        if (event.getSource().is(AntarchyTags.DamageType.BYPASSES_BLOODGLASS)) return;

        BloodglassAccess access = (BloodglassAccess) player;
        int totalActive = access.antarchy$getTotalShieldsActive();
        if (totalActive <= 0) return;

        // Consume apple shields first, then armor shields
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

        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) player.level();

        serverLevel.broadcastEntityEvent(player, (byte) 2);

        serverLevel.playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.GLASS_BREAK,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);

        serverLevel.sendParticles(
                new net.minecraft.core.particles.DustParticleOptions(
                        new org.joml.Vector3f(0.85f, 0.18f, 0.38f), 1.2f),
                player.getX(), player.getEyeY(), player.getZ(),
                35, 0.35, 0.55, 0.35, 0.12);

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

        // Shared item code now scales arrow base damage directly so attribute and projectile
        // modifiers stay in the damage pipeline. Leave non-player hits untouched here.
    }

    static void handleUltimateCrossbowDamage(LivingHurtEvent event) {
        if (!UltimateGearHelper.isUltimateCrossbowProjectile(event.getSource().getDirectEntity())) {
            return;
        }
    }



    static void handleNightmareArmorDoubleDamage(LivingHurtEvent event) {
        net.minecraft.world.damagesource.DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker)) return;
        if (!attacker.getAttributes().hasAttribute(AntarchyForgeMisc.DOUBLE_DAMAGE_CHANCE.get())) return;
        double chance = attacker.getAttributeValue(AntarchyForgeMisc.DOUBLE_DAMAGE_CHANCE.get());
        if (chance <= 0.0) return;
        if (attacker.getRandom().nextDouble() < chance) {
            event.setAmount(event.getAmount() * 2.0F);
            LivingEntity victim = event.getEntity();
            if (victim.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, victim.blockPosition(),
                        AntarchySoundEvents.NIGHTMARE_BITE.get(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.9f, 0.85f + victim.getRandom().nextFloat() * 0.3f);
                serverLevel.sendParticles(
                        new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.55f, 0.0f, 0.05f), 1.4f),
                        victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
                        30, 0.4, 0.5, 0.4, 0.15);
            }
        }
    }

    static void handleAntiwaterDamage(LivingHurtEvent event) {
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

    static void handleScorpionWhipLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof ScorpionWhipItem) || !ScorpionWhipTetherManager.hasTether(player)) {
            return;
        }
        event.setCanceled(true);
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

    static void handleDreamSandJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!AntarchySettings.dreamSandEnabled() || isDreamSandLowGravityBlacklisted(livingEntity)) {
            clearDreamSandLowGravity(livingEntity);
        } else if (isStandingOnDreamSand(livingEntity)) {
            livingEntity.setDeltaMovement(
                    livingEntity.getDeltaMovement().x,
                    livingEntity.getDeltaMovement().y * AntarchySettings.dreamSandJumpVelocityMultiplier(),
                    livingEntity.getDeltaMovement().z
            );
            DreamSandLowGravityAccess access = dreamSandAccess(livingEntity);
            access.antarchy$setDreamSandLowGravityActive(true);
            access.antarchy$setDreamSandLowGravityTicksRemaining((int) Math.max(1L, Math.round(AntarchySettings.dreamSandEffectDurationSeconds() * 20.0D)));
            access.antarchy$setDreamSandLandingGraceTicks(0);
        }

    }

    static void tickDreadAndIchor(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }
        if (player.isInFluidType(AntarchyForgeFluidTypes.ICHOR_TYPE.get()) && AntarchySettings.ichorWitherEnabled()) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true, true));
        }
    }

    static void tickScorpionWhips(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player instanceof ServerPlayer player) {
            ScorpionWhipTetherManager.tick(player);
        }
    }

    static void tickMinersDreamExcavations(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            AttitudeAdjusterSlamManager.tick(level);
            MinersDreamExcavationManager.tick(level);
        }
    }

    static void tickDreamSandLowGravity(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();

        DreamSandLowGravityAccess access = dreamSandAccess(livingEntity);
        int landingGraceTicks = access.antarchy$getDreamSandLandingGraceTicks();
        if (landingGraceTicks > 0) {
            access.antarchy$setDreamSandLandingGraceTicks(landingGraceTicks - 1);
        }

        if (!AntarchySettings.dreamSandEnabled() || isDreamSandLowGravityBlacklisted(livingEntity) || !access.antarchy$isDreamSandLowGravityActive()) {
            return;
        }

        int remainingTicks = access.antarchy$getDreamSandLowGravityTicksRemaining();
        if (remainingTicks > 0) {
            access.antarchy$setDreamSandLowGravityTicksRemaining(remainingTicks - 1);
        }
        if (access.antarchy$getDreamSandLowGravityTicksRemaining() <= 0) {
            clearDreamSandLowGravity(livingEntity);
            return;
        }

        if (isOnSolidGround(livingEntity)) {
            access.antarchy$setDreamSandLowGravityActive(false);
            access.antarchy$setDreamSandLowGravityTicksRemaining(0);
            access.antarchy$setDreamSandLandingGraceTicks(2);
        } else if (!livingEntity.onGround() && livingEntity.getDeltaMovement().y < 0.0D) {
            livingEntity.setDeltaMovement(
                    livingEntity.getDeltaMovement().x,
                    livingEntity.getDeltaMovement().y * AntarchySettings.dreamSandGravityMultiplier(),
                    livingEntity.getDeltaMovement().z
            );
        }
    }

    private static boolean isOnSolidGround(LivingEntity livingEntity) {
        return livingEntity.onGround() && livingEntity.getBlockStateOn().blocksMotion();
    }

    private static boolean isStandingOnDreamSand(LivingEntity livingEntity) {
        return livingEntity.level().getBlockState(livingEntity.getOnPosLegacy()).is(AntarchyForgeBlocks.DREAM_SAND.get());
    }

    static void handleAntiwaterFall(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (intersectsAntiwater(livingEntity, livingEntity.getBoundingBox().inflate(0.05D))) {
            event.setCanceled(true);
        }
    }

    static void handleDreamSandFall(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntity();
        DreamSandLowGravityAccess access = dreamSandAccess(livingEntity);
        if (!AntarchySettings.dreamSandEnabled()
                || isDreamSandLowGravityBlacklisted(livingEntity)
                || (!access.antarchy$isDreamSandLowGravityActive() && access.antarchy$getDreamSandLandingGraceTicks() <= 0)) {
            return;
        }

        event.setCanceled(true);
    }

    private static boolean isDreamSandLowGravityBlacklisted(LivingEntity livingEntity) {
        return livingEntity.getType().is(AntarchyTags.Entities.DREAM_SAND_LOW_GRAVITY_BLACKLIST);
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
        if (event.getEffectInstance() == null) return;
        if (event.getEffectInstance().getEffect() != AntarchyForgeMisc.INVERTED.get()) return;
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            com.craisinlord.antarchy.content.gravity.AntarchyGravityApi.setGravityDirection(
                    entity,
                    com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection.DOWN,
                    new com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition(12));
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

    private static void addPotionMix(net.minecraft.world.item.alchemy.Potion input, Item ingredient, net.minecraft.world.item.alchemy.Potion output) {
        net.minecraftforge.common.brewing.BrewingRecipeRegistry.addRecipe(new net.minecraftforge.common.brewing.IBrewingRecipe() {
            @Override
            public boolean isInput(ItemStack stack) {
                return stack.is(Items.POTION) && net.minecraft.world.item.alchemy.PotionUtils.getPotion(stack) == input;
            }

            @Override
            public boolean isIngredient(ItemStack stack) {
                return stack.is(ingredient);
            }

            @Override
            public ItemStack getOutput(ItemStack input1, ItemStack ingredient1) {
                return net.minecraft.world.item.alchemy.PotionUtils.setPotion(new ItemStack(input1.getItem()), output);
            }
        });
    }

    static void registerBrewingRecipes() {
        net.minecraft.tags.TagKey<net.minecraft.world.item.Item> rootsTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                new net.minecraft.resources.ResourceLocation(Antarchy.MODID, "roots")
        );
        net.minecraftforge.common.brewing.BrewingRecipeRegistry.addRecipe(new BrewingRecipe(
                net.minecraft.world.item.crafting.Ingredient.of(Items.POTION),
                net.minecraft.world.item.crafting.Ingredient.of(AntarchyForgeItems.CORN.get()),
                new ItemStack(AntarchyForgeItems.HIGH_FRUCTOSE_CORN_SYRUP.get())
        ) {
            @Override
            public boolean isInput(ItemStack stack) {
                return stack.is(Items.POTION) && net.minecraft.world.item.alchemy.PotionUtils.getPotion(stack) == net.minecraft.world.item.alchemy.Potions.WATER;
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
        net.minecraftforge.common.brewing.BrewingRecipeRegistry.addRecipe(new BrewingRecipe(
                net.minecraft.world.item.crafting.Ingredient.of(AntarchyForgeItems.HIGH_FRUCTOSE_CORN_SYRUP.get()),
                net.minecraft.world.item.crafting.Ingredient.of(rootsTag),
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
        addPotionMix(AntarchyForgeMisc.INVERSION.get(), Items.REDSTONE, AntarchyForgeMisc.LONG_INVERSION.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.STINK_BUG.get(), AntarchyForgeMisc.STINKY_POTION.get());
        addPotionMix(AntarchyForgeMisc.STINKY_POTION.get(), Items.REDSTONE, AntarchyForgeMisc.LONG_STINKY.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.BASILISK_FANG.get(), AntarchyForgeMisc.PARALYSIS.get());
        addPotionMix(AntarchyForgeMisc.PARALYSIS.get(), Items.REDSTONE, AntarchyForgeMisc.LONG_PARALYSIS.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.MOLEWORM_ITEM.get(), AntarchyForgeMisc.HASTE.get());
        addPotionMix(AntarchyForgeMisc.HASTE.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.STRONG_HASTE.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.CLOUD_SHARK_FIN.get(), Potions.SLOW_FALLING);
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.JUMPY_BUG_LEG.get(), Potions.LEAPING);
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.CORNEA_EAR.get(), Potions.NIGHT_VISION);
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.URANIUM_NUGGET.get(), AntarchyForgeMisc.SHRINKING.get());
        addPotionMix(AntarchyForgeMisc.SHRINKING.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.STRONG_SHRINKING.get());
        addPotionMix(AntarchyForgeMisc.STRONG_SHRINKING.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.EXTREME_SHRINKING.get());
        addPotionMix(Potions.AWKWARD, AntarchyForgeItems.TITANIUM_NUGGET.get(), AntarchyForgeMisc.GROWING.get());
        addPotionMix(AntarchyForgeMisc.GROWING.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.STRONG_GROWING.get());
        addPotionMix(AntarchyForgeMisc.STRONG_GROWING.get(), Items.GLOWSTONE_DUST, AntarchyForgeMisc.EXTREME_GROWING.get());
    }

    static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance() == null) {
            return;
        }

        if (event.getEffectInstance().getEffect() == AntarchyForgeMisc.DREAD.get() && !(event.getEntity() instanceof Player)) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            return;
        }

        if (event.getEntity().getType().is(AntarchyTags.Entities.INVERTED_IMMUNE)
                && event.getEffectInstance().getEffect() == AntarchyForgeMisc.INVERTED.get()) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            return;
        }

        if (event.getEntity().getType().is(AntarchyTags.Entities.SIZE_CHANGING_IMMUNE)
                && (event.getEffectInstance().getEffect() == AntarchyForgeMisc.SHRINKING_EFFECT.get() || event.getEffectInstance().getEffect() == AntarchyForgeMisc.GROWTH_EFFECT.get())) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            return;
        }

        if (event.getEntity().getType().is(AntarchyTags.Entities.PARALYSIS_IMMUNE)
                && event.getEffectInstance().getEffect() == AntarchyForgeMisc.PARALYZED.get()) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            return;
        }

        if (event.getEntity() instanceof Player player
                && event.getEffectInstance().getEffect() == AntarchyForgeMisc.INVERTED.get()
                && !player.hasEffect(AntarchyForgeMisc.INVERTED.get())) {
            notifyNearbyReveriesOfInversionChange(player);
        }
    }


    static void onInvertedEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() != AntarchyForgeMisc.INVERTED.get()) return;
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            com.craisinlord.antarchy.content.gravity.AntarchyGravityApi.setGravityDirection(
                    entity,
                    com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection.DOWN,
                    new com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition(12));
            if (entity instanceof Player player) {
                notifyNearbyReveriesOfInversionChange(player);
            }
        }
    }

    static void handleBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() != net.minecraftforge.common.ToolActions.AXE_STRIP) {
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

        if (state.is(AntarchyForgeBlocks.PEACH_WOOD.get())) {
            event.setFinalState(AntarchyForgeBlocks.STRIPPED_PEACH_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
        }
    }

    static void onCommonSetup(FMLCommonSetupEvent event) {
        BloodCrystalShardItem.SYNC_BLOODGLASS = AntarchyForgeEvents::syncBloodglass;
        event.enqueueWork(() -> {
            registerBrewingRecipes();
            DispenserBlock.registerBehavior(AntarchyForgeItems.SHRINK_RAY.get(), new SizeRayDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.GROWTH_RAY.get(), new SizeRayDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.SQUIDZOOKA.get(), new SquidzookaDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.RPO_LAUNCHER.get(), new RpoLauncherDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyForgeItems.WATER_CANNON.get(), new WaterCannonDispenseBehavior());
            AntimetalMinecartDispenseBehavior antimetalMinecartDispenseBehavior = new AntimetalMinecartDispenseBehavior();
            DispenserBlock.registerBehavior(Items.MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.CHEST_MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.FURNACE_MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.TNT_MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.HOPPER_MINECART, antimetalMinecartDispenseBehavior);
            DispenserBlock.registerBehavior(Items.COMMAND_BLOCK_MINECART, antimetalMinecartDispenseBehavior);
            LucidEyeProjectileEntity.defaultItemSupplier = () -> AntarchyForgeItems.LUCID_PEARL.get();
            LucidEntity.invertedEffectSupplier = () -> AntarchyForgeMisc.INVERTED.get();
            FluidInteractionRegistry.addInteraction(
                    AntarchyForgeFluidTypes.ANTIWATER_TYPE.get(),
                    new FluidInteractionRegistry.InteractionInformation(
                            ForgeMod.WATER_TYPE.get(),
                            fluidState -> fluidState.isSource()
                                    ? AntarchyForgeBlocks.ANTIMETAL.get().defaultBlockState()
                                    : AntarchyForgeBlocks.NYXITE.get().defaultBlockState()
                    )
            );
            LucidEntity.boltEntityTypeSupplier = () -> AntarchyForgeEntites.LUCID_BOLT.get();
            LucidBoltEntity.invertedEffectSupplier = () -> AntarchyForgeMisc.INVERTED.get();
            LucidEyeProjectileEntity.invertedEffectSupplier = () -> AntarchyForgeMisc.INVERTED.get();
            MultipartFramework.bootstrap(
                    (owner, partIndex, spec) -> new MultipartPartEntity((Entity) owner, owner, partIndex, spec),
                    new MultipartFramework.NetworkBridge() {
                        @Override
                        public void sendAttack(java.util.UUID parentId, int partIndex, float damage) {
                            AntarchyForgeNetworkCore.sendToServer(
                                    new com.craisinlord.antarchy.content.entity.multipart.network.MultipartAttackPayload(parentId, partIndex, damage)
                            );
                        }

                        @Override
                        public void sendInteract(java.util.UUID parentId, int partIndex, int handId) {
                            AntarchyForgeNetworkCore.sendToServer(
                                    new com.craisinlord.antarchy.content.entity.multipart.network.MultipartInteractPayload(parentId, partIndex, handId)
                            );
                        }
                    }
            );
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.UMBRAL_MOSS_BLOCK.get().asItem(), 0.65f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.UMBRAL_MOSS_CARPET.get().asItem(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.BLUSH_MOSS_BLOCK.get().asItem(), 0.65f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.BLUSH_MOSS_CARPET.get().asItem(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.HUSHWEED.get().asItem(), 0.65f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.CORNEA_EAR.get(), 0.65f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeBlocks.PEACH_LEAVES.get().asItem(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.PEACH_SAPLING_ITEM.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.NADIR_SAPLING_ITEM.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.CORN.get(), 0.65f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.CORN_SEEDS.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(AntarchyForgeItems.PEACH.get(), 0.65f);
        });
    }
}
