package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaArrival;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class AntTeleportHelper {
    private static final double COMPANION_FOLLOW_RADIUS = 16.0D;
    private static final int ARRIVAL_SEARCH_RADIUS = 24;
    private static final int ARRIVAL_VERTICAL_SEARCH = 24;
    private static final int EXPANDED_ARRIVAL_SEARCH_RADIUS = 48;
    private static final int EXPANDED_ARRIVAL_VERTICAL_SEARCH = 48;
    private static final ResourceKey<Level> ELYTHIA = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("antarchy", "elythia"));

    private AntTeleportHelper() {
    }

    public static void teleportPlayerWithCompanions(ServerPlayer player, ServerLevel destination, Vec3 destinationPos) {
        Mob vehicle = player.getVehicle() instanceof Mob mob ? mob : null;
        List<Mob> companions = collectCompanions(player);
        player.teleport(new TeleportTransition(destination, destinationPos, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
        Mob movedVehicle = null;
        for (Mob companion : companions) {
            Mob moved = moveCompanion(companion, destination, destinationPos);
            if (companion == vehicle) {
                movedVehicle = moved;
            }
        }

        if (movedVehicle != null) {
            player.startRiding(movedVehicle, true, true);
        }
    }

    public static List<Mob> collectCompanions(ServerPlayer player) {
        List<Mob> companions = new ArrayList<>();
        if (player.getVehicle() instanceof Mob vehicle) {
            companions.add(vehicle);
        }

        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(COMPANION_FOLLOW_RADIUS), mob -> isFollowingPet(mob, player))) {
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
            return tamable.isTame() && isOwnedBy(tamable.getOwnerReference(), player) && !tamable.isOrderedToSit();
        }

        if (mob instanceof AbstractHorse horse) {
            return horse.isTamed() && isOwnedBy(horse.getOwnerReference(), player);
        }

        return mob instanceof OwnableEntity ownable && isOwnedBy(ownable.getOwnerReference(), player);
    }

    private static boolean isOwnedBy(@Nullable EntityReference<LivingEntity> owner, ServerPlayer player) {
        return owner != null && player.getUUID().equals(owner.getUUID());
    }

    @Nullable
    public static Mob moveCompanion(Mob companion, ServerLevel destination, Vec3 destinationPos) {
        companion.stopRiding();
        companion.ejectPassengers();
        companion.getNavigation().stop();
        if (companion.level() == destination) {
            companion.teleportTo(destinationPos.x, destinationPos.y, destinationPos.z);
            return companion;
        }

        Entity moved = companion.teleport(new TeleportTransition(destination, destinationPos, Vec3.ZERO, companion.getYRot(), companion.getXRot(), TeleportTransition.DO_NOTHING));
        return moved instanceof Mob mob ? mob : null;
    }

    public static InteractionResult teleportPlayerToDimension(ServerPlayer player, ResourceKey<Level> destinationKey) {
        ServerLevel destination = player.level().getServer().getLevel(destinationKey);
        if (destination == null) {
            return InteractionResult.PASS;
        }

        arriveWithCompanions(player, destination);
        return InteractionResult.CONSUME;
    }

    public static InteractionResult teleportPlayerToReturnDestination(ServerPlayer player) {
        ServerLevel destination = resolveReturnDestinationLevel(player);
        arriveWithCompanions(player, destination);
        return InteractionResult.CONSUME;
    }

    public static InteractionResult handleCagedAntTeleport(ServerPlayer player, @Nullable Identifier entityTypeId, @Nullable CompoundTag entityData) {
        if (entityTypeId == null) {
            return InteractionResult.PASS;
        }

        if (!isRightClickDimensionEnabled(entityTypeId)) {
            return InteractionResult.PASS;
        }

        if (entityTypeId.equals(Identifier.fromNamespaceAndPath("antarchy", "brown_ant"))) {
            return teleportToOrFromConfiguredDimension(player, AntarchySettings.brownAntDestinationDimension());
        }
        if (entityTypeId.equals(Identifier.fromNamespaceAndPath("antarchy", "red_ant"))) {
            return teleportToOrFromConfiguredDimension(player, AntarchySettings.redAntDestinationDimension());
        }
        if (entityTypeId.equals(Identifier.fromNamespaceAndPath("antarchy", "termite"))) {
            return teleportToOrFromConfiguredDimension(player, AntarchySettings.termiteDestinationDimension());
        }
        if (entityTypeId.equals(Identifier.fromNamespaceAndPath("antarchy", "rainbow_ant"))) {
            return RainbowAntEntity.teleportPlayerFromStoredData(player, entityData);
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult teleportToOrFromConfiguredDimension(ServerPlayer player, ResourceKey<Level> configuredDimensionKey) {
        ServerLevel destination = player.level().dimension() == configuredDimensionKey
                ? resolveReturnDestinationLevel(player)
                : player.level().getServer().getLevel(configuredDimensionKey);
        if (destination == null) {
            return InteractionResult.PASS;
        }

        arriveWithCompanions(player, destination);
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

            serverPlayer.sendOverlayMessage(Component.translatable(ant.needsReagentMessageKey()));
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

    static boolean isRightClickDimensionEnabled(Identifier entityTypeId) {
        if (entityTypeId.equals(Identifier.fromNamespaceAndPath("antarchy", "brown_ant"))) {
            return AntarchySettings.brownAntRightClickDimension();
        }
        if (entityTypeId.equals(Identifier.fromNamespaceAndPath("antarchy", "red_ant"))) {
            return AntarchySettings.redAntRightClickDimension();
        }
        if (entityTypeId.equals(Identifier.fromNamespaceAndPath("antarchy", "rainbow_ant"))) {
            return AntarchySettings.rainbowAntRightClickDimension();
        }
        if (entityTypeId.equals(Identifier.fromNamespaceAndPath("antarchy", "termite"))) {
            return AntarchySettings.termiteRightClickDimension();
        }
        return true;
    }

    private static InteractionResult activateTeleport(BaseAntEntity ant, ServerPlayer player, ItemStack stack) {
        ant.setTeleportActivatedState(true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.sendOverlayMessage(Component.translatable(ant.activationMessageKey()));
        ant.playSound(SoundEvents.END_PORTAL_SPAWN, 0.8F, 1.1F);
        return InteractionResult.CONSUME;
    }

    private static void arriveWithCompanions(ServerPlayer player, ServerLevel destination) {
        prepareArrival(player, destination, destinationPos -> {
            if (destinationPos == null) {
                failArrival(player);
                return;
            }
            teleportPlayerWithCompanions(player, destination, destinationPos);
            player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        });
    }

    public static void prepareArrival(ServerPlayer player, ServerLevel destination, Consumer<Vec3> onArrival) {
        BlockPos preferredPos = ElythiaArrival.nearestPlateau(destination, preferredArrivalPos(player, destination));
        AntArrivalScheduler.request(player, destination, preferredPos, ARRIVAL_SEARCH_RADIUS, () -> {
            int[] yRange = getDimensionYRange(destination);
            if (yRange == null) {
                onArrival.accept(findSafeArrivalPositionAnywhere(player, destination, preferredPos));
                return;
            }
            Vec3 safePos = findSafeArrivalPositionInYRange(
                    player, destination, preferredPos, yRange[0], yRange[1], ARRIVAL_SEARCH_RADIUS, ARRIVAL_VERTICAL_SEARCH);
            if (safePos != null) {
                onArrival.accept(safePos);
                return;
            }
            AntArrivalScheduler.request(player, destination, preferredPos, EXPANDED_ARRIVAL_SEARCH_RADIUS, () -> {
                Vec3 expandedSafePos = findSafeArrivalPositionInYRange(
                        player, destination, preferredPos, yRange[0], yRange[1],
                        EXPANDED_ARRIVAL_SEARCH_RADIUS, EXPANDED_ARRIVAL_VERTICAL_SEARCH);
                onArrival.accept(expandedSafePos != null
                        ? expandedSafePos
                        : createEmergencyArrivalPlatform(destination, preferredPos, yRange[0], yRange[1]));
            });
        });
    }

    public static ServerLevel resolveReturnDestinationLevel(ServerPlayer player) {
        ResourceKey<Level> respawnDimension = respawnDimension(player);
        ServerLevel destination = player.level().getServer().getLevel(respawnDimension);
        if (destination != null && respawnDimension == Level.OVERWORLD) {
            return destination;
        }

        return player.level().getServer().overworld();
    }

    @Nullable
    public static Vec3 getDestinationPosition(ServerPlayer player, ServerLevel destination) {
        return findSafeArrivalPosition(player, destination, ElythiaArrival.nearestPlateau(destination, preferredArrivalPos(player, destination)));
    }

    private static BlockPos preferredArrivalPos(ServerPlayer player, ServerLevel destination) {
        BlockPos respawnPos = respawnPosition(player);
        if (respawnPos != null && destination.dimension() == respawnDimension(player)) {
            return respawnPos;
        }
        return destination.dimension() == Level.END ? ServerLevel.END_SPAWN_POINT : destination.getRespawnData().pos();
    }

    public static ResourceKey<Level> respawnDimension(ServerPlayer player) {
        ServerPlayer.RespawnConfig config = player.getRespawnConfig();
        return config != null ? config.respawnData().dimension() : Level.OVERWORLD;
    }

    @Nullable
    public static BlockPos respawnPosition(ServerPlayer player) {
        ServerPlayer.RespawnConfig config = player.getRespawnConfig();
        return config != null ? config.respawnData().pos() : null;
    }

    @Nullable
    private static Vec3 findSafeArrivalPosition(ServerPlayer player, ServerLevel destination, BlockPos preferredPos) {
        loadArrivalSearchArea(destination, preferredPos, ARRIVAL_SEARCH_RADIUS);
        int[] yRange = getDimensionYRange(destination);
        if (yRange != null) {
            Vec3 safePos = findSafeArrivalPositionInYRange(
                    player, destination, preferredPos, yRange[0], yRange[1], ARRIVAL_SEARCH_RADIUS, ARRIVAL_VERTICAL_SEARCH);
            if (safePos != null) {
                return safePos;
            }

            loadArrivalSearchArea(destination, preferredPos, EXPANDED_ARRIVAL_SEARCH_RADIUS);
            Vec3 expandedSafePos = findSafeArrivalPositionInYRange(
                    player, destination, preferredPos, yRange[0], yRange[1],
                    EXPANDED_ARRIVAL_SEARCH_RADIUS, EXPANDED_ARRIVAL_VERTICAL_SEARCH);
            return expandedSafePos != null ? expandedSafePos : createEmergencyArrivalPlatform(destination, preferredPos, yRange[0], yRange[1]);
        }

        return findSafeArrivalPositionAnywhere(player, destination, preferredPos);
    }

    @Nullable
    private static Vec3 findSafeArrivalPositionAnywhere(ServerPlayer player, ServerLevel destination, BlockPos preferredPos) {
        Set<BlockPos> candidates = new LinkedHashSet<>();
        BlockPos adjustedPreferredPos = player.adjustSpawnLocation(destination, preferredPos);
        addArrivalCandidate(candidates, preferredPos);
        addArrivalCandidate(candidates, preferredPos.above());
        addArrivalCandidate(candidates, adjustedPreferredPos);

        if (destination.dimensionType().hasSkyLight()) {
            addArrivalCandidate(candidates, PlayerSpawnFinder.getSpawnPosInChunk(destination, ChunkPos.containing(preferredPos)));
            addArrivalCandidate(candidates, PlayerSpawnFinder.getSpawnPosInChunk(destination, ChunkPos.containing(adjustedPreferredPos)));
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

        return createEmergencyArrivalPlatform(destination, preferredPos, destination.getMinY() + 1, destination.getMaxY() - 2);
    }

    @Nullable
    private static Vec3 createEmergencyArrivalPlatform(ServerLevel destination, BlockPos preferredPos, int minY, int maxY) {
        int y = Math.clamp(preferredPos.getY(), minY, maxY);
        int centerX = preferredPos.getX();
        int centerZ = preferredPos.getZ();
        for (int radius = 0; radius <= EXPANDED_ARRIVAL_SEARCH_RADIUS; radius++) {
            for (int xOff = -radius; xOff <= radius; xOff++) {
                for (int zOff = -radius; zOff <= radius; zOff++) {
                    if (radius > 0 && Math.abs(xOff) != radius && Math.abs(zOff) != radius) continue;
                    int x = centerX + xOff;
                    int z = centerZ + zOff;
                    BlockPos floor = new BlockPos(x, y - 1, z);
                    boolean withinBorder = true;
                    for (int dx = -2; dx <= 2 && withinBorder; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            BlockPos check = floor.offset(dx, 0, dz);
                            if (!destination.getWorldBorder().isWithinBounds(check) || !destination.hasChunkAt(check.above(3))) {
                                withinBorder = false;
                                break;
                            }
                        }
                    }
                    if (!withinBorder) continue;
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            BlockPos pad = floor.offset(dx, 0, dz);
                            destination.setBlock(pad, Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
                            for (int dy = 1; dy <= 3; dy++) {
                                destination.setBlock(pad.above(dy), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    }
                    Vec3 safePos = new Vec3(x + 0.5D, y, z + 0.5D);
                    if (isValidArrivalPosition(destination, safePos)) return safePos;
                }
            }
        }
        return null;
    }

    public static void loadArrivalSearchArea(ServerLevel destination, BlockPos preferredPos, int searchRadius) {
        ChunkPos center = ChunkPos.containing(preferredPos);
        int chunkRadius = (searchRadius >> 4) + 1;
        for (int chunkX = center.x() - chunkRadius; chunkX <= center.x() + chunkRadius; chunkX++) {
            for (int chunkZ = center.z() - chunkRadius; chunkZ <= center.z() + chunkRadius; chunkZ++) {
                destination.getChunk(chunkX, chunkZ);
            }
        }
    }

    @Nullable
    private static int[] getDimensionYRange(ServerLevel destination) {
        ResourceKey<Level> dim = destination.dimension();
        if (dim == ELYTHIA) {
            return new int[]{ElythiaArrival.MIN_ARRIVAL_Y, 280};
        }
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
    private static Vec3 findSafeArrivalPositionInYRange(ServerPlayer player, ServerLevel destination, BlockPos preferredPos, int minY, int maxY, int searchRadius, int verticalSearch) {
        Set<BlockPos> surfaceCandidates = new LinkedHashSet<>();
        boolean elythia = destination.dimension() == PermanentPortalType.ELYTHIA.primaryDimension();
        for (int radius = 0; radius <= searchRadius; radius++) {
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
                    if (elythia) {
                        surfaceCandidates.add(surface);
                        surfaceCandidates.add(surface.above());
                    } else {
                        addArrivalCandidate(surfaceCandidates, surface);
                        addArrivalCandidate(surfaceCandidates, destination.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, column));
                    }

                    for (int yOffset = 1; yOffset <= verticalSearch; yOffset++) {
                        surfaceCandidates.add(surface.above(yOffset));
                        if (!elythia) {
                            surfaceCandidates.add(surface.below(yOffset));
                        }
                    }
                }
            }
        }

        for (BlockPos candidate : surfaceCandidates) {
            if (candidate.getY() < minY || candidate.getY() > maxY) {
                continue;
            }
            Vec3 safePos = tryFindSafeDismount(player, destination, candidate);
            if (safePos != null && safePos.y >= minY && safePos.y <= maxY) {
                return safePos;
            }
        }

        if (elythia) {
            return null;
        }

        for (int radius = 0; radius <= searchRadius; radius++) {
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
        if (destination.dimension() == PermanentPortalType.ELYTHIA.primaryDimension()) {
            BlockPos feet = BlockPos.containing(safePos);
            BlockPos supportPos = feet.below();
            if (!destination.hasChunkAt(supportPos)) {
                return false;
            }
            BlockState support = destination.getBlockState(supportPos);
            if (!support.isFaceSturdy(destination, supportPos, Direction.UP)
                    || !support.getFluidState().isEmpty()
                    || support.is(net.minecraft.tags.BlockTags.LEAVES)) {
                return false;
            }

            int nearbySupports = 0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int yOffset = -2; yOffset <= 2; yOffset++) {
                    BlockPos nearby = supportPos.relative(direction).offset(0, yOffset, 0);
                    BlockState nearbyState = destination.getBlockState(nearby);
                    if (nearbyState.isFaceSturdy(destination, nearby, Direction.UP)
                            && nearbyState.getFluidState().isEmpty()
                            && !nearbyState.is(net.minecraft.tags.BlockTags.LEAVES)) {
                        nearbySupports++;
                        break;
                    }
                }
            }
            if (nearbySupports < 2) {
                return false;
            }
        }

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

    private static InteractionResult failArrival(ServerPlayer player) {
        player.sendOverlayMessage(Component.translatable("message.antarchy.teleport_arrival_failed"));
        return InteractionResult.CONSUME;
    }
}
