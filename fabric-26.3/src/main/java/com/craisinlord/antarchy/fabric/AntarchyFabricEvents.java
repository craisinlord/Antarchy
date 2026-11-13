package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.command.CaterpillarCommand;
import com.craisinlord.antarchy.content.command.CavarynCommand;
import com.craisinlord.antarchy.content.command.DimensionResetCommand;
import com.craisinlord.antarchy.content.command.DimensionalTearCommand;
import com.craisinlord.antarchy.content.command.ElythiaCommand;
import com.craisinlord.antarchy.content.command.PortalGunCommand;
import com.craisinlord.antarchy.content.command.QueenLocateCommand;
import com.craisinlord.antarchy.content.command.RoyalCommand;
import com.craisinlord.antarchy.content.command.ThoraxisCommand;
import com.craisinlord.antarchy.content.dispenser.AnvilCannonDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.BugSprayDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.RpoLauncherDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.SizeRayDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.SquidzookaDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.WaterCannonDispenseBehavior;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.ant.AntArrivalScheduler;
import com.craisinlord.antarchy.content.entity.ant.RainbowAntEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerNightSpawner;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenRitualManager;
import com.craisinlord.antarchy.content.entity.stratoshark.SharknadoManager;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkSkySpawner;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.craisinlord.antarchy.content.horde.CavarynCreatureSpawner;
import com.craisinlord.antarchy.content.horde.CavarynHordeManager;
import com.craisinlord.antarchy.content.item.AttitudeAdjusterItem;
import com.craisinlord.antarchy.content.item.AttitudeAdjusterSlamManager;
import com.craisinlord.antarchy.content.item.GiantFryingPanItem;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.item.WormHookTetherManager;
import com.craisinlord.antarchy.content.migration.DimensionResetManager;
import com.craisinlord.antarchy.content.portal.PermanentPortalManager;
import com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync;
import com.craisinlord.antarchy.content.portalgun.PortalGunProjectileEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunResetManager;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import com.craisinlord.antarchy.content.throwback.ThrowbackZoneTracker;
import com.craisinlord.antarchy.content.transit.AntTransit;
import com.craisinlord.antarchy.content.weather.ElythiaEvents;
import com.craisinlord.antarchy.content.weather.ElythiaRainGrowth;
import com.craisinlord.antarchy.content.weather.ThoraxisEvents;
import com.craisinlord.antarchy.content.worldgen.elythia.KingsTreeGridFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityTransition;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DispenserBlock;

public final class AntarchyFabricEvents {
    private static final GravityTransition INVERTED_CLEAR_TRANSITION = new GravityTransition(12);
    private static final Set<UUID> invertedPlayers = new HashSet<>();

    private AntarchyFabricEvents() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PortalGunProjectileEntity.releaseAllProjectileTickets());
        ServerLifecycleEvents.SERVER_STOPPING.register(PortalGunResetManager::clear);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> AntArrivalScheduler.clear());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PortalGunMoonAimSync.clear());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> AntTransit.clear());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> SharknadoManager.clearAll());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> KingsTreeGridFeature.clearTileCache());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PortalGunSavedData.clearMissingGuns(handler.player);
            DimensionResetManager.onPlayerJoin(handler.player);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CavarynCommand.register(dispatcher);
            ElythiaCommand.register(dispatcher);
            DimensionResetCommand.register(dispatcher);
            ThoraxisCommand.register(dispatcher);
            CaterpillarCommand.register(dispatcher);
            DimensionalTearCommand.register(dispatcher);
            QueenLocateCommand.register(dispatcher);
            RoyalCommand.register(dispatcher);
            PortalGunCommand.register(dispatcher);
        });

        DispenserBlock.registerBehavior(AntarchyFabricItems.SQUIDZOOKA.get(), new SquidzookaDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.RPO_LAUNCHER.get(), new RpoLauncherDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.ANVIL_CANNON.get(), new AnvilCannonDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.WATER_CANNON.get(), new WaterCannonDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.SHRINK_RAY.get(), new SizeRayDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.GROWTH_RAY.get(), new SizeRayDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.BUG_SPRAY.get(), new BugSprayDispenseBehavior());
        AntarchyFabricDispenserBehaviors.register();

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
            } else if (entity.getType() != EntityTypes.SQUID) {
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
                if (serverLevel.getRandom().nextInt(chanceDenominator) == 0) {
                    spawnKrakens(serverLevel, deathPos, 1);
                }
            }
            if (AntarchySettings.krakenMassSpawnEnabled() && serverLevel.getRandom().nextInt(500) == 0) {
                spawnKrakens(serverLevel, deathPos, 10);
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            InteractionResult result = GiantFryingPanItem.handleMainHandUse(world, player, hand);
            if (result == InteractionResult.PASS) {
                return InteractionResult.PASS;
            }
            return result.consumesAction()
                    ? (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME)
                    : InteractionResult.FAIL;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.getItemInHand(hand).getItem() instanceof PortalGunItem) {
                return InteractionResult.FAIL;
            }
            if (!world.isClientSide() && player.getItemInHand(hand).getItem() instanceof AttitudeAdjusterItem && player.getAttackStrengthScale(0.5F) >= 0.95F) {
                AttitudeAdjusterSlamManager.markSpecialHit(player);
            }
            return InteractionResult.PASS;
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.getItemInHand(hand).getItem() instanceof PortalGunItem) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                CavarynHordeManager.recordBlockBreak(serverPlayer, state, pos);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> KrakenRitualManager.tryStart(player, world, hand, hitResult.getBlockPos()));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            AntTransit.tick(server);
            Set<UUID> activeThisTick = new HashSet<>();
            for (ServerLevel level : server.getAllLevels()) {
                CavarynHordeManager.tick(level);
                CavarynCreatureSpawner.tick(level);
                GlimmerNightSpawner.tick(level);
                StratosharkSkySpawner.tick(level);
                ThoraxisUndersideManager.tick(level);
                ThoraxisEvents.tick(level);
                ElythiaEvents.tick(level);
                ThrowbackZoneTracker.tick(level);
                ElythiaRainGrowth.tick(level);
                KingsTreeGridFeature.tickPendingSpawns(level);
                KrakenRitualManager.tick(level);
                tickInvertedPlayers(level, activeThisTick);
                tickIchorPlayers(level);
                tickBloodglassRecharge(level);
                AttitudeAdjusterSlamManager.tick(level);
                for (ServerPlayer player : level.players()) {
                    WormHookTetherManager.tick(player);
                }
            }
            AntArrivalScheduler.tick(server);
            invertedPlayers.retainAll(activeThisTick);
        });
    }

    private static int badOmenLevel(Player player) {
        MobEffectInstance effect = player.getEffect(MobEffects.BAD_OMEN);
        return effect != null ? effect.getAmplifier() + 1 : 0;
    }

    private static void spawnKrakens(ServerLevel level, BlockPos origin, int count) {
        for (int i = 0; i < count; i++) {
            BlockPos spawnPos = findKrakenSpawnPos(level, origin);
            KrakenEntity kraken = AntarchyObjects.KRAKEN.get().create(level, EntitySpawnReason.EVENT);
            if (kraken == null) {
                continue;
            }
            kraken.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            kraken.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.EVENT, null);
            level.addFreshEntity(kraken);
        }
    }

    private static BlockPos findKrakenSpawnPos(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < 20; attempt++) {
            BlockPos candidate = origin.offset(
                    level.getRandom().nextInt(33) - 16,
                    level.getRandom().nextInt(9) - 4,
                    level.getRandom().nextInt(33) - 16
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

            if (GravityApi.getGravityDirection(player) == GravityDirection.UP
                    || GravityApi.isGravityForced(player)) {
                GravityApi.setGravityDirection(player, GravityDirection.DOWN, INVERTED_CLEAR_TRANSITION);
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
