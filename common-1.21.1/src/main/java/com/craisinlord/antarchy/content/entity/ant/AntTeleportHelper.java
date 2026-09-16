package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class AntTeleportHelper {
    private static final double COMPANION_FOLLOW_RADIUS = 16.0D;
    private static final int ARRIVAL_SEARCH_RADIUS = 24;
    private static final int ARRIVAL_VERTICAL_SEARCH = 24;

    private AntTeleportHelper() {
    }

    public static void teleportPlayerWithCompanions(ServerPlayer player, ServerLevel destination, Vec3 destinationPos) {
        Mob vehicle = player.getVehicle() instanceof Mob mob ? mob : null;
        List<Mob> companions = collectCompanions(player);
        player.teleportTo(destination, destinationPos.x, destinationPos.y, destinationPos.z, player.getYRot(), player.getXRot());
        Mob movedVehicle = null;
        for (Mob companion : companions) {
            Mob moved = moveCompanion(companion, destination, destinationPos);
            if (companion == vehicle) {
                movedVehicle = moved;
            }
        }

        if (movedVehicle != null) {
            player.startRiding(movedVehicle, true);
        }
    }

    private static List<Mob> collectCompanions(ServerPlayer player) {
        List<Mob> companions = new ArrayList<>();
        if (player.getVehicle() instanceof Mob vehicle) {
            companions.add(vehicle);
        }

        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(COMPANION_FOLLOW_RADIUS), mob -> isFollowingPet(mob, player))) {
            if (!companions.contains(mob)) {
                companions.add(mob);
            }
        }

        return companions;
    }

    private static boolean isFollowingPet(Mob mob, ServerPlayer player) {
        if (!mob.isAlive()) {
            return false;
        }

        if (mob instanceof TamableAnimal tamable) {
            return tamable.isTame() && player.getUUID().equals(tamable.getOwnerUUID()) && !tamable.isOrderedToSit();
        }

        if (mob instanceof AbstractHorse horse) {
            return horse.isTamed() && player.getUUID().equals(horse.getOwnerUUID());
        }

        return mob instanceof OwnableEntity ownable && player.getUUID().equals(ownable.getOwnerUUID());
    }

    @Nullable
    private static Mob moveCompanion(Mob companion, ServerLevel destination, Vec3 destinationPos) {
        companion.stopRiding();
        companion.ejectPassengers();
        companion.getNavigation().stop();
        if (companion.level() == destination) {
            companion.teleportTo(destinationPos.x, destinationPos.y, destinationPos.z);
            return companion;
        }

        Entity moved = companion.changeDimension(new DimensionTransition(destination, destinationPos, Vec3.ZERO, companion.getYRot(), companion.getXRot(), DimensionTransition.DO_NOTHING));
        return moved instanceof Mob mob ? mob : null;
    }

    public static InteractionResult teleportPlayerToDimension(ServerPlayer player, ResourceKey<Level> destinationKey) {
        ServerLevel destination = player.server.getLevel(destinationKey);
        if (destination == null) {
            return InteractionResult.PASS;
        }

        Vec3 destinationPos = getDestinationPosition(player, destination);
        teleportPlayerWithCompanions(player, destination, destinationPos);
        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    public static InteractionResult teleportPlayerToReturnDestination(ServerPlayer player) {
        ServerLevel destination = resolveReturnDestinationLevel(player);
        Vec3 destinationPos = getDestinationPosition(player, destination);
        teleportPlayerWithCompanions(player, destination, destinationPos);
        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    public static InteractionResult handleCagedAntTeleport(ServerPlayer player, @Nullable ResourceLocation entityTypeId, @Nullable CompoundTag entityData) {
        if (entityTypeId == null) {
            return InteractionResult.PASS;
        }

        if (!isRightClickDimensionEnabled(entityTypeId)) {
            return InteractionResult.PASS;
        }

        if (entityTypeId.equals(ResourceLocation.fromNamespaceAndPath("antarchy", "brown_ant"))) {
            return teleportToOrFromConfiguredDimension(player, AntarchySettings.brownAntDestinationDimension());
        }
        if (entityTypeId.equals(ResourceLocation.fromNamespaceAndPath("antarchy", "red_ant"))) {
            return teleportToOrFromConfiguredDimension(player, AntarchySettings.redAntDestinationDimension());
        }
        if (entityTypeId.equals(ResourceLocation.fromNamespaceAndPath("antarchy", "termite"))) {
            return teleportToOrFromConfiguredDimension(player, AntarchySettings.termiteDestinationDimension());
        }
        if (entityTypeId.equals(ResourceLocation.fromNamespaceAndPath("antarchy", "rainbow_ant"))) {
            return RainbowAntEntity.teleportPlayerFromStoredData(player, entityData);
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult teleportToOrFromConfiguredDimension(ServerPlayer player, ResourceKey<Level> configuredDimensionKey) {
        ServerLevel destination = player.serverLevel().dimension() == configuredDimensionKey
                ? resolveReturnDestinationLevel(player)
                : player.server.getLevel(configuredDimensionKey);
        if (destination == null) {
            return InteractionResult.PASS;
        }

        Vec3 destinationPos = getDestinationPosition(player, destination);
        teleportPlayerWithCompanions(player, destination, destinationPos);
        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    static InteractionResult handleInteraction(BaseAntEntity ant, Player player, ItemStack itemStack) {
        if (!isRightClickDimensionEnabled(ant)) {
            return InteractionResult.PASS;
        }

        if (ant.requiresActivationReagent() && !ant.isTeleportActivatedState()) {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.SUCCESS;
            }

            if (itemStack.is(ant.activationItemsTag())) {
                return activateTeleport(ant, serverPlayer, itemStack);
            }

            if (!itemStack.isEmpty()) {
                return InteractionResult.PASS;
            }

            serverPlayer.displayClientMessage(Component.translatable(ant.needsReagentMessageKey()), true);
            return InteractionResult.CONSUME;
        }

        if (!itemStack.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        return teleportToOrFromConfiguredDimension(serverPlayer, ant.destinationDimension());
    }

    static boolean isRightClickDimensionEnabled(BaseAntEntity ant) {
        return ant instanceof BrownAntEntity
                ? AntarchySettings.brownAntRightClickDimension()
                : ant instanceof RedAntEntity
                        ? AntarchySettings.redAntRightClickDimension()
                        : ant instanceof RainbowAntEntity
                                ? AntarchySettings.rainbowAntRightClickDimension()
                                : ant instanceof TermiteEntity && AntarchySettings.termiteRightClickDimension();
    }

    static boolean isRightClickDimensionEnabled(ResourceLocation entityTypeId) {
        if (entityTypeId.equals(ResourceLocation.fromNamespaceAndPath("antarchy", "brown_ant"))) {
            return AntarchySettings.brownAntRightClickDimension();
        }
        if (entityTypeId.equals(ResourceLocation.fromNamespaceAndPath("antarchy", "red_ant"))) {
            return AntarchySettings.redAntRightClickDimension();
        }
        if (entityTypeId.equals(ResourceLocation.fromNamespaceAndPath("antarchy", "rainbow_ant"))) {
            return AntarchySettings.rainbowAntRightClickDimension();
        }
        if (entityTypeId.equals(ResourceLocation.fromNamespaceAndPath("antarchy", "termite"))) {
            return AntarchySettings.termiteRightClickDimension();
        }
        return true;
    }

    private static InteractionResult activateTeleport(BaseAntEntity ant, ServerPlayer player, ItemStack stack) {
        ant.setTeleportActivatedState(true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.displayClientMessage(Component.translatable(ant.activationMessageKey()), true);
        ant.playSound(SoundEvents.END_PORTAL_SPAWN, 0.8F, 1.1F);
        return InteractionResult.CONSUME;
    }

    public static ServerLevel resolveReturnDestinationLevel(ServerPlayer player) {
        ResourceKey<Level> respawnDimension = player.getRespawnDimension();
        ServerLevel destination = player.server.getLevel(respawnDimension);
        if (destination != null && respawnDimension == Level.OVERWORLD) {
            return destination;
        }

        return player.server.overworld();
    }

    public static Vec3 getDestinationPosition(ServerPlayer player, ServerLevel destination) {
        BlockPos respawnPos = player.getRespawnPosition();
        BlockPos preferredPos;
        if (respawnPos != null && destination.dimension() == player.getRespawnDimension()) {
            preferredPos = respawnPos;
        } else {
            preferredPos = destination.dimension() == Level.END ? ServerLevel.END_SPAWN_POINT : destination.getSharedSpawnPos();
        }

        Vec3 safeArrivalPos = findSafeArrivalPosition(player, destination, preferredPos);
        if (safeArrivalPos != null) {
            return safeArrivalPos;
        }

        return createEmergencyArrivalPlatform(destination, findEmergencyPlatformPosition(destination, preferredPos));
    }

    @Nullable
    private static Vec3 findSafeArrivalPosition(ServerPlayer player, ServerLevel destination, BlockPos preferredPos) {
        loadArrivalSearchArea(destination, preferredPos);
        int[] yRange = getDimensionYRange(destination);
        if (yRange != null) {
            return findSafeArrivalPositionInYRange(player, destination, preferredPos, yRange[0], yRange[1]);
        }

        Set<BlockPos> candidates = new LinkedHashSet<>();
        BlockPos adjustedPreferredPos = player.adjustSpawnLocation(destination, preferredPos);
        addArrivalCandidate(candidates, preferredPos);
        addArrivalCandidate(candidates, preferredPos.above());
        addArrivalCandidate(candidates, adjustedPreferredPos);

        if (destination.dimensionType().hasSkyLight()) {
            addArrivalCandidate(candidates, PlayerRespawnLogic.getSpawnPosInChunk(destination, new ChunkPos(preferredPos)));
            addArrivalCandidate(candidates, PlayerRespawnLogic.getSpawnPosInChunk(destination, new ChunkPos(adjustedPreferredPos)));
        }

        for (int radius = 0; radius <= ARRIVAL_SEARCH_RADIUS; radius++) {
            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    if (radius > 0 && Math.abs(xOffset) != radius && Math.abs(zOffset) != radius) {
                        continue;
                    }

                    BlockPos searchPos = preferredPos.offset(xOffset, 0, zOffset);
                    if (destination.hasChunkAt(searchPos)) {
                        addArrivalCandidate(candidates, destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, searchPos));
                        addArrivalCandidate(candidates, destination.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, searchPos));
                    }
                }
            }
        }

        for (BlockPos candidate : candidates) {
            Vec3 safePos = tryFindSafeDismount(player, destination, candidate);
            if (safePos != null) {
                return safePos;
            }
        }

        return null;
    }

    /**
     * Arrival validation needs neighboring chunks for collision checks. Load a
     * bounded area up front so unloaded terrain is not mistaken for an unsafe
     * destination and sent straight to the emergency-platform path.
     */
    private static void loadArrivalSearchArea(ServerLevel destination, BlockPos preferredPos) {
        ChunkPos center = new ChunkPos(preferredPos);
        int chunkRadius = (ARRIVAL_SEARCH_RADIUS >> 4) + 1;
        for (int chunkX = center.x - chunkRadius; chunkX <= center.x + chunkRadius; chunkX++) {
            for (int chunkZ = center.z - chunkRadius; chunkZ <= center.z + chunkRadius; chunkZ++) {
                destination.getChunk(chunkX, chunkZ);
            }
        }
    }

    @Nullable
    private static int[] getDimensionYRange(ServerLevel destination) {
        ResourceKey<Level> dim = destination.dimension();
        if (dim == PermanentPortalType.ELYTHIA.primaryDimension()) {
            return new int[]{64, 240};
        }
        if (dim == AntarchySettings.termiteDestinationDimension()) {
            return new int[]{100, 200};
        }
        if (dim == AntarchySettings.brownAntDestinationDimension()) {
            return new int[]{60, 240};
        }
        if (dim == AntarchySettings.redAntDestinationDimension()) {
            return new int[]{0, 220};
        }
        return null;
    }

    @Nullable
    private static Vec3 findSafeArrivalPositionInYRange(ServerPlayer player, ServerLevel destination, BlockPos preferredPos, int minY, int maxY) {
        Set<BlockPos> surfaceCandidates = new LinkedHashSet<>();
        for (int radius = 0; radius <= ARRIVAL_SEARCH_RADIUS; radius++) {
            for (int xOff = -radius; xOff <= radius; xOff++) {
                for (int zOff = -radius; zOff <= radius; zOff++) {
                    if (radius > 0 && Math.abs(xOff) != radius && Math.abs(zOff) != radius) {
                        continue;
                    }
                    int x = preferredPos.getX() + xOff;
                    int z = preferredPos.getZ() + zOff;
                    BlockPos column = new BlockPos(x, preferredPos.getY(), z);
                    if (!destination.hasChunkAt(column)) {
                        continue;
                    }

                    BlockPos surface = destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
                    addArrivalCandidate(surfaceCandidates, surface);
                    addArrivalCandidate(surfaceCandidates, destination.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, column));

                    for (int yOffset = 1; yOffset <= ARRIVAL_VERTICAL_SEARCH; yOffset++) {
                        surfaceCandidates.add(surface.above(yOffset));
                        surfaceCandidates.add(surface.below(yOffset));
                    }
                }
            }
        }

        // Prefer actual generated surfaces. This is especially important in
        // Elythia, where the configured spawn height is not necessarily the
        // playable surface.
        for (BlockPos candidate : surfaceCandidates) {
            if (candidate.getY() < minY || candidate.getY() > maxY) {
                continue;
            }
            Vec3 safePos = tryFindSafeDismount(player, destination, candidate);
            if (safePos != null && safePos.y >= minY && safePos.y <= maxY) {
                return safePos;
            }
        }

        // Heightmaps can be stale or unsuitable around structures, so retain a
        // bounded exhaustive search as a final generated-terrain attempt.
        for (int radius = 0; radius <= ARRIVAL_SEARCH_RADIUS; radius++) {
            for (int xOff = -radius; xOff <= radius; xOff++) {
                for (int zOff = -radius; zOff <= radius; zOff++) {
                    if (radius > 0 && Math.abs(xOff) != radius && Math.abs(zOff) != radius) {
                        continue;
                    }
                    int x = preferredPos.getX() + xOff;
                    int z = preferredPos.getZ() + zOff;
                    for (int y = minY + 1; y <= maxY; y++) {
                        Vec3 safePos = tryFindSafeDismount(player, destination, new BlockPos(x, y, z));
                        if (safePos != null && safePos.y >= minY && safePos.y <= maxY) {
                            return safePos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void addArrivalCandidate(Set<BlockPos> candidates, @Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }

        candidates.add(pos);
        candidates.add(pos.above());
        if (pos.getY() > Integer.MIN_VALUE) {
            candidates.add(pos.below());
        }
    }

    @Nullable
    private static Vec3 tryFindSafeDismount(ServerPlayer player, ServerLevel destination, BlockPos candidate) {
        if (!isSafeDismountAreaLoaded(destination, candidate)) {
            return null;
        }

        Vec3 safePos = DismountHelper.findSafeDismountLocation(player.getType(), destination, candidate, true);
        if (safePos != null && isValidArrivalPosition(destination, safePos)) {
            return safePos;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos offsetCandidate = candidate.relative(direction);
            if (!isSafeDismountAreaLoaded(destination, offsetCandidate)) {
                continue;
            }

            safePos = DismountHelper.findSafeDismountLocation(player.getType(), destination, offsetCandidate, true);
            if (safePos != null && isValidArrivalPosition(destination, safePos)) {
                return safePos;
            }
        }

        return null;
    }

    private static boolean isValidArrivalPosition(ServerLevel destination, Vec3 safePos) {
        if (destination.dimension() != AntarchySettings.termiteDestinationDimension()) {
            return true;
        }

        BlockPos standPos = BlockPos.containing(safePos);
        if (!destination.hasChunkAt(standPos) || !destination.hasChunkAt(standPos.below())) {
            return false;
        }

        return !destination.getBlockState(standPos.below()).is(Blocks.BEDROCK);
    }

    private static boolean isSafeDismountAreaLoaded(ServerLevel destination, BlockPos center) {
        if (!destination.hasChunkAt(center) || !destination.hasChunkAt(center.above()) || !destination.hasChunkAt(center.below())) {
            return false;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos offset = center.relative(direction);
            if (!destination.hasChunkAt(offset) || !destination.hasChunkAt(offset.above()) || !destination.hasChunkAt(offset.below())) {
                return false;
            }
        }

        return true;
    }

    private static BlockPos getClampedFallbackPosition(ServerLevel destination, BlockPos preferredPos) {
        int minY = destination.getMinBuildHeight() + 1;
        int maxY = destination.getMaxBuildHeight() - 1;
        return new BlockPos(preferredPos.getX(), Mth.clamp(preferredPos.getY(), minY, maxY), preferredPos.getZ());
    }

    /**
     * Keep the emergency platform grounded in generated terrain when possible.
     * A configured/shared spawn can be an arbitrary coordinate in a custom
     * dimension, so using its Y directly can strand the player in the sky.
     */
    private static BlockPos findEmergencyPlatformPosition(ServerLevel destination, BlockPos preferredPos) {
        BlockPos best = null;
        int bestDistance = Integer.MAX_VALUE;
        int minY = destination.getMinBuildHeight() + 1;
        int maxY = destination.getMaxBuildHeight() - 2;

        for (int radius = 0; radius <= 8; radius++) {
            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    if (radius > 0 && Math.abs(xOffset) != radius && Math.abs(zOffset) != radius) {
                        continue;
                    }

                    BlockPos searchPos = preferredPos.offset(xOffset, 0, zOffset);
                    if (!destination.hasChunkAt(searchPos)) {
                        continue;
                    }

                    BlockPos surface = destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, searchPos);
                    if (surface.getY() < minY || surface.getY() > maxY) {
                        continue;
                    }

                    int distance = Math.abs(xOffset) + Math.abs(zOffset);
                    if (distance < bestDistance) {
                        best = surface;
                        bestDistance = distance;
                    }
                }
            }
            if (best != null) {
                break;
            }
        }

        return best != null ? best : getClampedFallbackPosition(destination, preferredPos);
    }

    /**
     * Last-resort landing location for custom dimensions whose generated spawn
     * area has no valid player-sized space. The normal search always runs first;
     * this only modifies terrain when there is no safe generated location.
     */
    private static Vec3 createEmergencyArrivalPlatform(ServerLevel destination, BlockPos preferredPos) {
        BlockPos center = getClampedFallbackPosition(destination, preferredPos);
        int minY = destination.getMinBuildHeight() + 1;
        int maxY = destination.getMaxBuildHeight() - 2;
        center = new BlockPos(center.getX(), Mth.clamp(center.getY(), minY, maxY), center.getZ());

        destination.getChunkAt(center);
        BlockState platformState = emergencyPlatformState(destination);

        for (int xOffset = -2; xOffset <= 2; xOffset++) {
            for (int zOffset = -2; zOffset <= 2; zOffset++) {
                BlockPos column = center.offset(xOffset, 0, zOffset);
                destination.setBlock(column.below(), platformState, Block.UPDATE_ALL);
                for (int yOffset = 0; yOffset <= 3; yOffset++) {
                    destination.removeBlock(column.above(yOffset), false);
                }
            }
        }

        return Vec3.atBottomCenterOf(center);
    }

    private static BlockState emergencyPlatformState(ServerLevel destination) {
        if (destination.dimension() == PermanentPortalType.ELYTHIA.primaryDimension()) {
            return PermanentPortalType.ELYTHIA.platformBlock().defaultBlockState();
        }
        if (destination.dimension() == PermanentPortalType.THORAXIS.primaryDimension()) {
            return PermanentPortalType.THORAXIS.platformBlock().defaultBlockState();
        }
        if (destination.dimension() == PermanentPortalType.CAVARYN.primaryDimension()) {
            return PermanentPortalType.CAVARYN.platformBlock().defaultBlockState();
        }
        if (destination.dimension() == Level.NETHER) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (destination.dimension() == Level.END) {
            return Blocks.END_STONE.defaultBlockState();
        }
        return Blocks.DIRT.defaultBlockState();
    }
}
