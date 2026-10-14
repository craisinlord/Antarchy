package com.craisinlord.antarchy.content.block.entity.undertrial;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface UndertrialPlayerDetector {
    UndertrialPlayerDetector NO_CREATIVE_PLAYERS = (level, selector, pos, range, requireLineOfSight) -> collect(level, pos,
            selector.getPlayers(level, player -> player.blockPosition().closerThan(pos, range) && !player.isCreative() && !player.isSpectator()),
            requireLineOfSight);
    UndertrialPlayerDetector INCLUDING_CREATIVE_PLAYERS = (level, selector, pos, range, requireLineOfSight) -> collect(level, pos,
            selector.getPlayers(level, player -> player.blockPosition().closerThan(pos, range) && !player.isSpectator()),
            requireLineOfSight);

    List<UUID> detect(ServerLevel level, EntitySelector selector, BlockPos pos, double range, boolean requireLineOfSight);

    private static List<UUID> collect(ServerLevel level, BlockPos pos, List<? extends Player> players, boolean requireLineOfSight) {
        List<UUID> result = new ArrayList<>();
        for (Player player : players) {
            if (!requireLineOfSight || inLineOfSight(level, pos.getCenter(), player.getEyePosition())) {
                result.add(player.getUUID());
            }
        }
        return List.copyOf(result);
    }

    static boolean inLineOfSight(Level level, Vec3 target, Vec3 from) {
        BlockHitResult hit = level.clip(new ClipContext(from, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (Entity) null));
        return hit.getBlockPos().equals(BlockPos.containing(target)) || hit.getType() == HitResult.Type.MISS;
    }

    interface EntitySelector {
        EntitySelector SELECT_FROM_LEVEL = (level, predicate) -> level.getPlayers(predicate::test);

        List<? extends Player> getPlayers(ServerLevel level, Predicate<? super Player> predicate);
    }
}
