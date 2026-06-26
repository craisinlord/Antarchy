package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.DuctTapeBlock;
import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.content.command.CaterpillarCommand;
import com.craisinlord.antarchy.content.command.GravityCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import com.craisinlord.antarchy.content.SquidzookaDispenseBehavior;
import com.craisinlord.antarchy.content.WaterCannonDispenseBehavior;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

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
            GravityCommand.register(dispatcher);
            CaterpillarCommand.register(dispatcher);
        });

        DispenserBlock.registerBehavior(AntarchyFabricContent.SQUIDZOOKA.get(), new SquidzookaDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricContent.WATER_CANNON.get(), new WaterCannonDispenseBehavior());

        ComposterBlock.COMPOSTABLES.put(AntarchyFabricContent.UMBRAL_MOSS_BLOCK.get().asItem(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricContent.UMBRAL_MOSS_CARPET.get().asItem(), 0.3f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricContent.AMBER_MOSS_BLOCK.get().asItem(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricContent.AMBER_MOSS_CARPET.get().asItem(), 0.3f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricContent.CREEPVINE.get().asItem(), 0.3f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricContent.HUSHWEED.get().asItem(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(AntarchyFabricContent.CORNEA_EAR.get(), 0.65f);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer sp) {
                BloodglassManager.handleDeath(sp);
            }
            if (!(entity instanceof MissileSquidEntity missileSquid)) {
                return;
            }
            if (missileSquid.isSpawnedByKraken()) {
                return;
            }
            if (!(entity.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            BlockPos deathPos = entity.blockPosition();
            if (AntarchySettings.krakenSquidSpawnEnabled() && serverLevel.random.nextInt(100) == 0) {
                spawnKrakens(serverLevel, deathPos, 1);
            }
            if (AntarchySettings.krakenMassSpawnEnabled() && serverLevel.random.nextInt(100) == 0) {
                spawnKrakens(serverLevel, deathPos, 10);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Set<UUID> activeThisTick = new HashSet<>();
            for (ServerLevel level : server.getAllLevels()) {
                tickInvertedPlayers(level, activeThisTick);
                tickDuctTapePlayers(level);
                tickIchorPlayers(level);
                tickBloodglassRecharge(level);
            }
            invertedPlayers.retainAll(activeThisTick);
        });
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
            DuctTapeBlock.tickStuckEntity(player);
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
                    if (PotentNyxiteBlock.isIchor(entity.level().getFluidState(cursor))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
