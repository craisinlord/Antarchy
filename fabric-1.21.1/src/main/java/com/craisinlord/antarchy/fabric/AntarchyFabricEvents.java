package com.craisinlord.antarchy.fabric;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricBlocks;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;

import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.ant.RainbowAntEntity;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.DuctTapeBlock;
import com.craisinlord.antarchy.content.item.AttitudeAdjusterItem;
import com.craisinlord.antarchy.content.item.AttitudeAdjusterSlamManager;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.content.horde.CavarynHordeManager;
import com.craisinlord.antarchy.content.portal.PermanentPortalManager;
import com.craisinlord.antarchy.content.command.CavarynCommand;
import com.craisinlord.antarchy.content.command.CaterpillarCommand;
import com.craisinlord.antarchy.content.command.GravityCommand;
import com.craisinlord.antarchy.content.item.MinersDreamExcavationManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import com.craisinlord.antarchy.content.dispenser.SquidzookaDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.RpoLauncherDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.SizeRayDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.WaterCannonDispenseBehavior;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AntarchyFabricEvents {
    private static final AntarchyGravityTransition INVERTED_CLEAR_TRANSITION = new AntarchyGravityTransition(12);
    private static final Set<UUID> invertedPlayers = new HashSet<>();

    private AntarchyFabricEvents() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CavarynCommand.register(dispatcher);
            GravityCommand.register(dispatcher);
            CaterpillarCommand.register(dispatcher);
        });

        DispenserBlock.registerBehavior(AntarchyFabricItems.SQUIDZOOKA.get(), new SquidzookaDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.RPO_LAUNCHER.get(), new RpoLauncherDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.WATER_CANNON.get(), new WaterCannonDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.SHRINK_RAY.get(), new SizeRayDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.GROWTH_RAY.get(), new SizeRayDispenseBehavior());
        AntarchyFabricDispenserBehaviors.register();
        registerTrades();

        ComposterBlock.COMPOSTABLES.put(AntarchyFabricBlocks.UMBRAL_MOSS_BLOCK.get().asItem(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricBlocks.UMBRAL_MOSS_CARPET.get().asItem(), 0.3f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricBlocks.BLUSH_MOSS_BLOCK.get().asItem(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricBlocks.BLUSH_MOSS_CARPET.get().asItem(), 0.3f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricBlocks.HUSHWEED.get().asItem(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricItems.CORNEA_EAR.get(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricBlocks.PEACH_LEAVES.get().asItem(), 0.3f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricItems.PEACH_SAPLING_ITEM.get(), 0.3f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricItems.CORN.get(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricItems.CORN_SEEDS.get(), 0.3f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricItems.PEACH.get(), 0.65f);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer sp) {
                BloodglassManager.handleDeath(sp);
            }
            PermanentPortalManager.handleSacrifice(entity);
            if (entity instanceof RainbowAntEntity rainbowAnt) {
                rainbowAnt.tryIgniteInfinityPortalOnDeath();
            }
            if (entity.getKillCredit() instanceof ServerPlayer killer) {
                CavarynHordeManager.recordMobKill(killer, entity);
            }
            if (entity instanceof MissileSquidEntity missileSquid) {
                if (missileSquid.isSpawnedByKraken()) {
                    return;
                }
            } else if (entity.getType() != net.minecraft.world.entity.EntityType.SQUID) {
                return;
            }
            if (!(entity.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            if (!(entity.getKillCredit() instanceof Player killer)) {
                return;
            }

            boolean requireBadOmen = AntarchySettings.krakenRequireBadOmenToSummon();
            int badOmenLevel = badOmenLevel(killer);
            if (requireBadOmen && badOmenLevel <= 0) {
                return;
            }

            BlockPos deathPos = entity.blockPosition();
            if (AntarchySettings.krakenSquidSpawnEnabled()) {
                int chanceDenominator = requireBadOmen ? Math.max(1, 100 / badOmenLevel) : 100;
                if (serverLevel.random.nextInt(chanceDenominator) == 0) {
                    spawnKrakens(serverLevel, deathPos, 1);
                }
            }
            if (AntarchySettings.krakenMassSpawnEnabled() && serverLevel.random.nextInt(500) == 0) {
                spawnKrakens(serverLevel, deathPos, 10);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide && player.getItemInHand(hand).getItem() instanceof AttitudeAdjusterItem && player.getAttackStrengthScale(0.5F) >= 0.95F) {
                AttitudeAdjusterSlamManager.markSpecialHit(player);
            }
            return InteractionResult.PASS;
        });
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
                CavarynHordeManager.recordBlockBreak(serverPlayer, state, pos);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Set<UUID> activeThisTick = new HashSet<>();
            for (ServerLevel level : server.getAllLevels()) {
                CavarynHordeManager.tick(level);
                com.craisinlord.antarchy.content.horde.CavarynCreatureSpawner.tick(level);
                tickInvertedPlayers(level, activeThisTick);
                tickDuctTapePlayers(level);
                tickIchorPlayers(level);
                tickBloodglassRecharge(level);
                AttitudeAdjusterSlamManager.tick(level);
                MinersDreamExcavationManager.tick(level);
            }
            invertedPlayers.retainAll(activeThisTick);
        });
    }

    private static void registerTrades() {
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 1, factories ->
                factories.add((trader, random) -> new MerchantOffer(
                        new ItemCost(AntarchyFabricItems.CORN.get(), 20),
                        new ItemStack(Items.EMERALD),
                        16,
                        2,
                        0.05F
                )));
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.BUTCHER, 1, factories ->
                factories.add((trader, random) -> new MerchantOffer(
                        new ItemCost(AntarchyFabricItems.COOKED_CORNDOG.get(), 5),
                        new ItemStack(Items.EMERALD),
                        16,
                        2,
                        0.05F
                )));
        TradeOfferHelper.registerWanderingTraderOffers(0, factories ->
                factories.add((trader, random) -> new MerchantOffer(
                        new ItemCost(Items.EMERALD, 1),
                        new ItemStack(AntarchyFabricItems.CORN_SEEDS.get(), 3),
                        12,
                        2,
                        0.05F
                )));
    }

    private static int badOmenLevel(Player player) {
        MobEffectInstance effect = player.getEffect(MobEffects.BAD_OMEN);
        return effect != null ? effect.getAmplifier() + 1 : 0;
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

    private static void tickInvertedPlayers(ServerLevel level, Set<UUID> activeThisTick) {
        for (Player player : level.players()) {
            LivingEntity livingEntity = player;

            if (livingEntity.hasEffect(AntarchyObjects.INVERTED_EFFECT.get())) {
                activeThisTick.add(player.getUUID());
                invertedPlayers.add(player.getUUID());
                continue;
            }

            if (!invertedPlayers.remove(player.getUUID())) {
                continue;
            }

            if (AntarchyGravityApi.getGravityDirection(player) == AntarchyGravityDirection.UP
                    || AntarchyGravityApi.isGravityForced(player)) {
                AntarchyGravityApi.setGravityDirection(player, AntarchyGravityDirection.DOWN, INVERTED_CLEAR_TRANSITION);
            }
            notifyNearbyReveriesOfInversionChange(player);
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

    private static void tickDuctTapePlayers(ServerLevel level) {
        for (Player player : level.players()) {
            if (DuctTapeBlock.shouldTickStuckEntity(player)) {
                DuctTapeBlock.tickStuckEntity(player);
            }
        }
    }

    private static void tickIchorPlayers(ServerLevel level) {
        if (!AntarchySettings.ichorWitherEnabled()) {
            return;
        }

        for (Player player : level.players()) {
            if (!antarchy$isInIchor(player)) {
                continue;
            }
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true, true));
        }
    }

    private static void tickBloodglassRecharge(ServerLevel level) {
        for (Player player : level.players()) {
            if (player instanceof ServerPlayer sp) {
                BloodglassManager.tickRecharge(sp);
            }
        }
    }

    private static boolean antarchy$isInIchor(LivingEntity entity) {
        BlockPos min = BlockPos.containing(entity.getBoundingBox().minX, entity.getBoundingBox().minY, entity.getBoundingBox().minZ);
        BlockPos max = BlockPos.containing(entity.getBoundingBox().maxX, entity.getBoundingBox().maxY, entity.getBoundingBox().maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    if (AntarchyFluidChecks.isIchor(entity.level().getFluidState(cursor))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
