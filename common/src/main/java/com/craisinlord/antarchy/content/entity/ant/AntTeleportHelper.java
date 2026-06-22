package com.craisinlord.antarchy.content.entity.ant;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class AntTeleportHelper {
    private AntTeleportHelper() {
    }

    static InteractionResult handleInteraction(BaseAntEntity ant, Player player, ItemStack itemStack) {
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

        ServerLevel configuredDestination = resolveConfiguredDestinationLevel(ant, serverPlayer.serverLevel());
        ServerLevel destination = configuredDestination != null && serverPlayer.serverLevel().dimension() == configuredDestination.dimension()
                ? resolveReturnDestinationLevel(serverPlayer)
                : configuredDestination;
        if (destination == null) {
            return InteractionResult.PASS;
        }

        Vec3 destinationPos = getDestinationPosition(serverPlayer, destination);
        serverPlayer.teleportTo(destination, destinationPos.x, destinationPos.y, destinationPos.z, serverPlayer.getYRot(), serverPlayer.getXRot());
        serverPlayer.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
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

    @Nullable
    private static ServerLevel resolveConfiguredDestinationLevel(BaseAntEntity ant, ServerLevel serverLevel) {
        return serverLevel.getServer().getLevel(ant.destinationDimension());
    }

    public static ServerLevel resolveReturnDestinationLevel(ServerPlayer player) {
        ResourceKey<Level> respawnDimension = player.getRespawnDimension();
        ServerLevel destination = player.server.getLevel(respawnDimension);
        if (destination != null && respawnDimension == Level.OVERWORLD) {
            return destination;
        }

        // If the player's respawn is not in the overworld, return them to overworld spawn instead.
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

        BlockPos fallbackPos = player.adjustSpawnLocation(destination, preferredPos);
        return Vec3.atBottomCenterOf(fallbackPos);
    }

    @Nullable
    private static Vec3 findSafeArrivalPosition(ServerPlayer player, ServerLevel destination, BlockPos preferredPos) {
        Set<BlockPos> candidates = new LinkedHashSet<>();
        BlockPos adjustedPreferredPos = player.adjustSpawnLocation(destination, preferredPos);
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
                    addArrivalCandidate(candidates, destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, searchPos));
                    addArrivalCandidate(candidates, destination.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, searchPos));
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
        Vec3 safePos = DismountHelper.findSafeDismountLocation(player.getType(), destination, candidate, true);
        if (safePos != null) {
            return safePos;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            safePos = DismountHelper.findSafeDismountLocation(player.getType(), destination, candidate.relative(direction), true);
            if (safePos != null) {
                return safePos;
            }
        }

        return null;
    }
}
