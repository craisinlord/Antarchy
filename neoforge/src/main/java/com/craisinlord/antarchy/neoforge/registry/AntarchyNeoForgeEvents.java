package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.block.DuctTapeBlock;
import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.bloodglass.BloodglassAccess;
import com.craisinlord.antarchy.content.entity.*;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEyeProjectileEntity;
import com.craisinlord.antarchy.content.entity.multipart.MultipartFramework;
import com.craisinlord.antarchy.content.entity.trades.DrTrayaurusTradeManager;
import com.craisinlord.antarchy.content.item.*;
import com.craisinlord.antarchy.content.item.ultimate.UltimateGearHelper;
import com.craisinlord.antarchy.content.movement.DreamSandLowGravityAccess;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.neoforge.AntarchyNeoForgeFluidTypes;
import com.craisinlord.antarchy.neoforge.entity.multipart.MultipartPartEntity;
import com.craisinlord.antarchy.neoforge.network.AntarchyGravityNetworking;
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
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.network.PacketDistributor;

public final class AntarchyNeoForgeEvents {
    private AntarchyNeoForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyNeoForgeEvents::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::onMissileSquidDeath);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::onLivingBreathe);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::registerBrewingRecipes);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleNaturalRabbitReplacement);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBlockToolModification);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::onMobEffectApplicable);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::onInvertedEffectRemoved);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::onInvertedEffectExpired);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleUltimateBowDamage);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleUltimateCrossbowDamage);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleNightmareSwordDamage);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleNightmareArmorDoubleDamage);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleScorpionWhipAttackEntity);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleScorpionWhipLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleScorpionWhipRightClickBlock);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleScorpionWhipRightClickItem);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleParalyzedAttackEntity);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleParalyzedLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleParalyzedRightClickBlock);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleParalyzedRightClickItem);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleParalyzedEntityInteract);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleParalyzedEntityInteractSpecific);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleDreamSandJump);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::tickDreadAndIchor);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::tickScorpionWhips);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::tickDreamSandLowGravity);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleAntiwaterDamage);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleAntiwaterFall);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleDreamSandFall);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBloodCrystalBootsFall);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBloodglassShield);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBloodCrystalArmorEquip);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBloodglassWardApplied);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBloodglassWardRemoved);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBloodglassWardExpired);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::tickBloodglassRecharge);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBloodglassPlayerDeath);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBloodglassRespawn);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleBloodglassLogin);
        modEventBus.addListener(AntarchyNeoForgeEvents::modifyEntityAttributes);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::tickOverheadInversion);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::tickDuctTapeStickiness);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleDreadDeath);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleFallenKingCrownKill);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleDreadBedSleep);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleStartTracking);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::handleDreamSandLogout);
        NeoForge.EVENT_BUS.addListener(AntarchyNeoForgeEvents::registerReloadListeners);
    }

    public static void onMissileSquidDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof MissileSquidEntity)) return;
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        BlockPos deathPos = event.getEntity().blockPosition();

        if (AntarchySettings.krakenSquidSpawnEnabled() && serverLevel.random.nextInt(100) == 0) {
            spawnKrakens(serverLevel, deathPos, 1);
        }

        if (AntarchySettings.krakenMassSpawnEnabled() && serverLevel.random.nextInt(100) == 0) {
            spawnKrakens(serverLevel, deathPos, 10);
        }
    }

    public static void onLivingBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getEntity().getEyeInFluidType() != AntarchyNeoForgeFluidTypes.ANTIWATER_TYPE.get()) {
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
            kraken.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null);
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
    }

    private static final String DREAD_BED_BUG_NEXT_SPAWN_KEY = Antarchy.MODID + ":dread_bed_bug_next_spawn";
    private static final int DREAD_BED_BUG_SPAWN_COOLDOWN_TICKS = 6000;
    static void handleDreadBedSleep(net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent event) {
        if (!event.getEntity().hasEffect(AntarchyNeoforgeMisc.DREAD)) {
            return;
        }
        if (event.getEntity() instanceof Player player) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.antarchy.dread_too_scared_to_sleep"), true);
        }
        event.setProblem(net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM);

        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) event.getLevel();
        Player player = event.getEntity();
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
                net.minecraft.world.entity.MobSpawnType.EVENT, null);
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
        EasterBunnyEntity easterBunny = AntarchyNeoforgeEntites.EASTER_BUNNY.get().create(level);
        if (easterBunny == null) {
            return;
        }

        easterBunny.moveTo(event.getX(), event.getY(), event.getZ(), rabbit.getYRot(), rabbit.getXRot());
        easterBunny.finalizeSpawn(level, level.getCurrentDifficultyAt(event.getEntity().blockPosition()), MobSpawnType.NATURAL, null);
        level.addFreshEntity(easterBunny);
        event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
    }

    static void tickDuctTapeStickiness(EntityTickEvent.Post event) {
        DuctTapeBlock.tickStuckEntity(event.getEntity());
    }

    static void handleDreadDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        clearDreamSandLowGravity(entity);
        if (!entity.hasEffect(AntarchyNeoforgeMisc.DREAD)) {
            return;
        }

        entity.removeEffect(AntarchyNeoforgeMisc.DREAD);
    }

    static void handleFallenKingCrownKill(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Enemy)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (!player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(AntarchyNeoforgeItems.FALLEN_KING_CROWN.get())) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 6000, 0, false, true, true));
    }

    private static final int ANTIMETAL_INVERTED_REFRESH_TICKS = 20;

    static void tickOverheadInversion(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }
        if (livingEntity.level().isClientSide()) {
            return;
        }
        if (livingEntity instanceof Player player && player.isSpectator()) {
            return;
        }
        if (!isDirectlyBelowAntimetal(livingEntity)) {
            return;
        }

        MobEffectInstance existingInverted = livingEntity.getEffect(AntarchyNeoforgeMisc.INVERTED);
        if (existingInverted == null || existingInverted.getDuration() <= 5) {
            livingEntity.addEffect(new MobEffectInstance(AntarchyNeoforgeMisc.INVERTED, ANTIMETAL_INVERTED_REFRESH_TICKS, 0, false, false, false));
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
                    if (entity.level().getBlockState(crystalPos).is(AntarchyNeoforgeBlocks.ANTIMETAL.get())
                            || entity.level().getBlockState(crystalPos).is(AntarchyNeoforgeBlocks.POLISHED_ANTIMETAL.get())) {
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
        event.add(EntityType.PLAYER, AntarchyNeoforgeMisc.BLOODGLASS_MAX_HEARTS);
        event.add(EntityType.PLAYER, AntarchyNeoforgeMisc.DOUBLE_DAMAGE_CHANCE);
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
        if (sp.hasEffect(AntarchyNeoforgeMisc.BLOODGLASS_WARD)) {
            net.minecraft.world.effect.MobEffectInstance effect = sp.getEffect(AntarchyNeoforgeMisc.BLOODGLASS_WARD);
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
        PacketDistributor.sendToPlayer(player,
                new com.craisinlord.antarchy.content.network.BloodglassStatePayload(
                        access.antarchy$getTotalShieldsActive(),
                        access.antarchy$getTotalShieldsMax()
                )
        );
    }

    static void handleBloodglassWardApplied(MobEffectEvent.Added event) {
        if (!event.getEffectInstance().is(AntarchyNeoforgeMisc.BLOODGLASS_WARD)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        BloodglassAccess access = (BloodglassAccess) player;
        int shieldCount = Math.min(
                event.getEffectInstance().getAmplifier() + 1,
                Math.max(0, AntarchySettings.bloodCrystalHardMaxShields()
                        - access.antarchy$getArmorShieldsActive() - access.antarchy$getArmorShieldLostCount())
        );
        access.antarchy$setAppleShieldsActive(shieldCount);
        access.antarchy$setAppleShieldLostCount(0);
        access.antarchy$setAppleRechargeTimer(0);
        syncBloodglass((ServerPlayer) player);
    }

    private static void clearAppleShields(Player player) {
        BloodglassAccess access = (BloodglassAccess) player;
        access.antarchy$setAppleShieldsActive(0);
        access.antarchy$setAppleShieldLostCount(0);
        access.antarchy$setAppleRechargeTimer(0);
        if (player instanceof ServerPlayer sp) syncBloodglass(sp);
    }

    static void handleBloodglassWardRemoved(MobEffectEvent.Remove event) {
        if (!event.getEffect().is(AntarchyNeoforgeMisc.BLOODGLASS_WARD)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        clearAppleShields(player);
    }

    static void handleBloodglassWardExpired(MobEffectEvent.Expired event) {
        if (!event.getEffectInstance().is(AntarchyNeoforgeMisc.BLOODGLASS_WARD)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        clearAppleShields(player);
    }

    static void tickBloodglassRecharge(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
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

        if (access.antarchy$getAppleShieldLostCount() > 0 && player.hasEffect(AntarchyNeoforgeMisc.BLOODGLASS_WARD)) {
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

    static void handleBloodglassShield(LivingIncomingDamageEvent event) {
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
    static void handleUltimateBowDamage(LivingIncomingDamageEvent event) {
        if (!UltimateGearHelper.isUltimateBowArrow(event.getSource().getDirectEntity())) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target instanceof Player player) {
            event.setAmount(0.0F);
            event.setInvulnerabilityTicks(0);
            player.clearFire();
            player.heal((float) AntarchySettings.ultimateBowPlayerHeal());
            return;
        }

        // Shared item code now scales arrow base damage directly so attribute and projectile
        // modifiers stay in the damage pipeline. Leave non-player hits untouched here.
    }

    static void handleUltimateCrossbowDamage(LivingIncomingDamageEvent event) {
        if (!UltimateGearHelper.isUltimateCrossbowProjectile(event.getSource().getDirectEntity())) {
            return;
        }
    }


    static void handleNightmareSwordDamage(LivingIncomingDamageEvent event) {
        net.minecraft.world.damagesource.DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player attacker)) return;
        if (!(attacker.getMainHandItem().getItem() instanceof NightmareSwordItem)) return;

        float maxHealth = attacker.getMaxHealth();
        if (maxHealth <= 0) return;
        float missingFraction = (maxHealth - attacker.getHealth()) / maxHealth;
        float baseDamage = (float) AntarchySettings.nightmareSwordBaseDamage();
        float scalingFactor = (float) AntarchySettings.nightmareSwordScalingFactor();

        event.setAmount(baseDamage + missingFraction * scalingFactor * baseDamage);
    }

    static void handleNightmareArmorDoubleDamage(LivingIncomingDamageEvent event) {
        net.minecraft.world.damagesource.DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker)) return;
        if (!attacker.getAttributes().hasAttribute(AntarchyNeoforgeMisc.DOUBLE_DAMAGE_CHANCE)) return;
        double chance = attacker.getAttributeValue(AntarchyNeoforgeMisc.DOUBLE_DAMAGE_CHANCE);
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

    private static boolean isWearingFullNightmareArmor(Player player) {
        for (ItemStack stack : player.getArmorSlots()) {
            if (!(stack.getItem() instanceof NightmareArmorItem)) return false;
        }
        return true;
    }

    static void handleAntiwaterDamage(LivingIncomingDamageEvent event) {
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
        return player.hasEffect(AntarchyNeoforgeMisc.PARALYZED);
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

    static void tickDreadAndIchor(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (player.isInFluidType(AntarchyNeoForgeFluidTypes.ICHOR_TYPE.get()) && AntarchySettings.ichorWitherEnabled()) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true, true));
        }
    }

    static void tickScorpionWhips(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ScorpionWhipTetherManager.tick(player);
        }
    }

    static void tickDreamSandLowGravity(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }

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
        return livingEntity.level().getBlockState(livingEntity.getOnPosLegacy()).is(AntarchyNeoforgeBlocks.DREAM_SAND.get());
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
                    if (PotentNyxiteBlock.isAntiwater(fluidState)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static void onInvertedEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null) return;
        if (!event.getEffectInstance().is(AntarchyNeoforgeMisc.INVERTED)) return;
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

    static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, AntarchyNeoforgeItems.LUCID_EYE.get(), AntarchyNeoforgeMisc.INVERSION);
        event.getBuilder().addMix(AntarchyNeoforgeMisc.INVERSION, Items.REDSTONE, AntarchyNeoforgeMisc.LONG_INVERSION);
        event.getBuilder().addMix(Potions.AWKWARD, AntarchyNeoforgeItems.BASILISK_FANG.get(), AntarchyNeoforgeMisc.PARALYSIS);
        event.getBuilder().addMix(AntarchyNeoforgeMisc.PARALYSIS, Items.REDSTONE, AntarchyNeoforgeMisc.LONG_PARALYSIS);
        event.getBuilder().addMix(Potions.AWKWARD, AntarchyNeoforgeItems.MOLEWORM_ITEM.get(), AntarchyNeoforgeMisc.HASTE);
        event.getBuilder().addMix(AntarchyNeoforgeMisc.HASTE, Items.GLOWSTONE_DUST, AntarchyNeoforgeMisc.STRONG_HASTE);
        event.getBuilder().addMix(Potions.AWKWARD, AntarchyNeoforgeItems.CLOUD_SHARK_FIN.get(), Potions.SLOW_FALLING);
        event.getBuilder().addMix(Potions.AWKWARD, AntarchyNeoforgeItems.JUMPY_BUG_LEG.get(), Potions.LEAPING);
        event.getBuilder().addMix(Potions.AWKWARD, AntarchyNeoforgeItems.CORNEA_EAR.get(), Potions.NIGHT_VISION);
        event.getBuilder().addMix(Potions.AWKWARD, AntarchyNeoforgeItems.URANIUM_NUGGET.get(), AntarchyNeoforgeMisc.SHRINKING);
        event.getBuilder().addMix(AntarchyNeoforgeMisc.SHRINKING, Items.GLOWSTONE_DUST, AntarchyNeoforgeMisc.STRONG_SHRINKING);
        event.getBuilder().addMix(AntarchyNeoforgeMisc.STRONG_SHRINKING, Items.GLOWSTONE_DUST, AntarchyNeoforgeMisc.EXTREME_SHRINKING);
        event.getBuilder().addMix(Potions.AWKWARD, AntarchyNeoforgeItems.TITANIUM_NUGGET.get(), AntarchyNeoforgeMisc.GROWING);
        event.getBuilder().addMix(AntarchyNeoforgeMisc.GROWING, Items.GLOWSTONE_DUST, AntarchyNeoforgeMisc.STRONG_GROWING);
        event.getBuilder().addMix(AntarchyNeoforgeMisc.STRONG_GROWING, Items.GLOWSTONE_DUST, AntarchyNeoforgeMisc.EXTREME_GROWING);

    }

    static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance() == null) {
            return;
        }

        if (event.getEffectInstance().is(AntarchyNeoforgeMisc.DREAD) && !(event.getEntity() instanceof Player)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }

        if (event.getEntity() instanceof Player player && isWearingFullNightmareArmor(player)
                && (event.getEffectInstance().is(net.minecraft.world.effect.MobEffects.WITHER)
                || event.getEffectInstance().is(AntarchyNeoforgeMisc.DREAD))) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }

        if (event.getEntity().getType().is(AntarchyTags.Entities.INVERTED_IMMUNE)
                && event.getEffectInstance().is(AntarchyNeoforgeMisc.INVERTED)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }

        if (event.getEntity().getType().is(AntarchyTags.Entities.SIZE_CHANGING_IMMUNE)
                && (event.getEffectInstance().is(AntarchyNeoforgeMisc.SHRINKING_EFFECT) || event.getEffectInstance().is(AntarchyNeoforgeMisc.GROWTH_EFFECT))) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }

        if (event.getEntity().getType().is(AntarchyTags.Entities.PARALYSIS_IMMUNE)
                && event.getEffectInstance().is(AntarchyNeoforgeMisc.PARALYZED)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }

        if (event.getEntity() instanceof Player player
                && event.getEffectInstance().is(AntarchyNeoforgeMisc.INVERTED)
                && !player.hasEffect(AntarchyNeoforgeMisc.INVERTED)) {
            notifyNearbyReveriesOfInversionChange(player);
        }
    }


    static void onInvertedEffectRemoved(MobEffectEvent.Remove event) {
        if (!event.getEffect().is(AntarchyNeoforgeMisc.INVERTED)) return;
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
        if (event.getItemAbility() != ItemAbilities.AXE_STRIP) {
            return;
        }

        BlockState state = event.getState();
        if (!state.hasProperty(RotatedPillarBlock.AXIS)) {
            return;
        }

        if (state.is(AntarchyNeoforgeBlocks.OURANWOOD_LOG.get())) {
            event.setFinalState(AntarchyNeoforgeBlocks.STRIPPED_OURANWOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyNeoforgeBlocks.MOSSY_OURANWOOD_LOG.get())) {
            event.setFinalState(AntarchyNeoforgeBlocks.STRIPPED_OURANWOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyNeoforgeBlocks.OURANWOOD_WOOD.get())) {
            event.setFinalState(AntarchyNeoforgeBlocks.STRIPPED_OURANWOOD_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
            return;
        }

        if (state.is(AntarchyNeoforgeBlocks.MOSSY_OURANWOOD_WOOD.get())) {
            event.setFinalState(AntarchyNeoforgeBlocks.STRIPPED_OURANWOOD_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
        }
    }

    static void onCommonSetup(FMLCommonSetupEvent event) {
        BloodCrystalShardItem.SYNC_BLOODGLASS = AntarchyNeoForgeEvents::syncBloodglass;
        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(AntarchyNeoforgeItems.SHRINK_RAY.get(), new com.craisinlord.antarchy.content.SizeRayDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyNeoforgeItems.GROWTH_RAY.get(), new com.craisinlord.antarchy.content.SizeRayDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyNeoforgeItems.SQUIDZOOKA.get(), new com.craisinlord.antarchy.content.SquidzookaDispenseBehavior());
            DispenserBlock.registerBehavior(AntarchyNeoforgeItems.WATER_CANNON.get(), new com.craisinlord.antarchy.content.WaterCannonDispenseBehavior());
            LucidEyeProjectileEntity.defaultItemSupplier = () -> AntarchyNeoforgeItems.LUCID_PEARL.get();
            LucidEntity.invertedEffectSupplier = () -> AntarchyNeoforgeMisc.INVERTED;
            FluidInteractionRegistry.addInteraction(
                    AntarchyNeoForgeFluidTypes.ANTIWATER_TYPE.get(),
                    new FluidInteractionRegistry.InteractionInformation(
                            NeoForgeMod.WATER_TYPE.value(),
                            fluidState -> fluidState.isSource()
                                    ? AntarchyNeoforgeBlocks.ANTIMETAL.get().defaultBlockState()
                                    : AntarchyNeoforgeBlocks.NYXITE.get().defaultBlockState()
                    )
            );
            LucidEntity.boltEntityTypeSupplier = () -> AntarchyNeoforgeEntites.LUCID_BOLT.get();
            LucidBoltEntity.invertedEffectSupplier = () -> AntarchyNeoforgeMisc.INVERTED;
            LucidEyeProjectileEntity.invertedEffectSupplier = () -> AntarchyNeoforgeMisc.INVERTED;
            MultipartFramework.bootstrap(
                    (owner, partIndex, spec) -> new MultipartPartEntity((Entity) owner, owner, partIndex, spec),
                    new MultipartFramework.NetworkBridge() {
                        @Override
                        public void sendAttack(java.util.UUID parentId, int partIndex, float damage) {
                            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                                    new com.craisinlord.antarchy.content.entity.multipart.network.MultipartAttackPayload(parentId, partIndex, damage)
                            );
                        }

                        @Override
                        public void sendInteract(java.util.UUID parentId, int partIndex, int handId) {
                            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                                    new com.craisinlord.antarchy.content.entity.multipart.network.MultipartInteractPayload(parentId, partIndex, handId)
                            );
                        }
                    }
            );
            ComposterBlock.COMPOSTABLES.put(AntarchyNeoforgeBlocks.UMBRAL_MOSS_BLOCK.get().asItem(), 0.65f);
            ComposterBlock.COMPOSTABLES.put(AntarchyNeoforgeBlocks.UMBRAL_MOSS_CARPET.get().asItem(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(AntarchyNeoforgeBlocks.HUSHWEED.get().asItem(), 0.65f);
            ComposterBlock.COMPOSTABLES.put(AntarchyNeoforgeItems.CORNEA_EAR.get(), 0.65f);
        });
    }
}
