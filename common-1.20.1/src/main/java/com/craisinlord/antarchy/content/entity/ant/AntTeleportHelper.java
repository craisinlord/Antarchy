package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class AntTeleportHelper {
    private static final double COMPANION_FOLLOW_RADIUS = 16.0D;

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

        companion.unRide();
        Entity moved = companion.getType().create(destination);
        if (moved == null) {
            return null;
        }

        moved.restoreFrom(companion);
        moved.moveTo(destinationPos.x, destinationPos.y, destinationPos.z, companion.getYRot(), companion.getXRot());
        moved.setDeltaMovement(Vec3.ZERO);
        companion.discard();
        destination.addDuringTeleport(moved);
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

        if (entityTypeId.equals(new ResourceLocation("antarchy", "brown_ant"))) {
            return teleportToOrFromConfiguredDimension(player, AntarchySettings.brownAntDestinationDimension());
        }
        if (entityTypeId.equals(new ResourceLocation("antarchy", "red_ant"))) {
            return teleportToOrFromConfiguredDimension(player, AntarchySettings.redAntDestinationDimension());
        }
        if (entityTypeId.equals(new ResourceLocation("antarchy", "termite"))) {
            return teleportToOrFromConfiguredDimension(player, AntarchySettings.termiteDestinationDimension());
        }
        if (entityTypeId.equals(new ResourceLocation("antarchy", "rainbow_ant"))) {
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
        if (entityTypeId.equals(new ResourceLocation("antarchy", "brown_ant"))) {
            return AntarchySettings.brownAntRightClickDimension();
        }
        if (entityTypeId.equals(new ResourceLocation("antarchy", "red_ant"))) {
            return AntarchySettings.redAntRightClickDimension();
        }
        if (entityTypeId.equals(new ResourceLocation("antarchy", "rainbow_ant"))) {
            return AntarchySettings.rainbowAntRightClickDimension();
        }
        if (entityTypeId.equals(new ResourceLocation("antarchy", "termite"))) {
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

        if (destination.hasChunkAt(preferredPos)) {
            return Vec3.atBottomCenterOf(preferredPos);
        }

        return Vec3.atBottomCenterOf(getClampedFallbackPosition(destination, preferredPos));
    }

    @Nullable
    private static Vec3 findSafeArrivalPosition(ServerPlayer player, ServerLevel destination, BlockPos preferredPos) {
        int[] yRange = getDimensionYRange(destination);
        if (yRange != null) {
            return findSafeArrivalPositionInYRange(player, destination, preferredPos, yRange[0], yRange[1]);
        }

        Set<BlockPos> candidates = new LinkedHashSet<>();
        BlockPos adjustedPreferredPos = preferredPos;
        addArrivalCandidate(candidates, preferredPos);
        addArrivalCandidate(candidates, preferredPos.above());
        addArrivalCandidate(candidates, adjustedPreferredPos);

        if (destination.dimensionType().hasSkyLight()) {
            addArrivalCandidate(candidates, PlayerRespawnLogic.getSpawnPosInChunk(destination, new ChunkPos(preferredPos)));
            addArrivalCandidate(candidates, PlayerRespawnLogic.getSpawnPosInChunk(destination, new ChunkPos(adjustedPreferredPos)));
        }

        for (int radius = 0; radius <= 4; radius++) {
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

    @Nullable
    private static int[] getDimensionYRange(ServerLevel destination) {
        ResourceKey<Level> dim = destination.dimension();
        if (dim == AntarchySettings.termiteDestinationDimension()) {
            return new int[]{100, 200};
        }
        if (dim == AntarchySettings.brownAntDestinationDimension()) {
            return new int[]{75, 120};
        }
        return null;
    }

    @Nullable
    private static Vec3 findSafeArrivalPositionInYRange(ServerPlayer player, ServerLevel destination, BlockPos preferredPos, int minY, int maxY) {
        for (int radius = 0; radius <= 8; radius++) {
            for (int xOff = -radius; xOff <= radius; xOff++) {
                for (int zOff = -radius; zOff <= radius; zOff++) {
                    if (radius > 0 && Math.abs(xOff) != radius && Math.abs(zOff) != radius) {
                        continue;
                    }
                    int x = preferredPos.getX() + xOff;
                    int z = preferredPos.getZ() + zOff;
                    for (int y = maxY; y >= minY + 1; y--) {
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
}
