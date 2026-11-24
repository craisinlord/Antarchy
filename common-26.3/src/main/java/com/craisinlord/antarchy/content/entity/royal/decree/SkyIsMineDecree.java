package com.craisinlord.antarchy.content.entity.royal.decree;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import com.craisinlord.anthology.gravity.GravityApi;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class SkyIsMineDecree implements RoyalDecree {
    private static final int AIRBORNE_LIMIT = 30;
    private static final int FLIGHT_WEIGHT = 3;
    private static final int HIGH_ABOVE_GROUND_BLOCKS = 3;
    private final Map<UUID, Integer> airborneTicks = new HashMap<>();

    public String translationKey() {
        return "decree.antarchy.the_sky_is_mine";
    }

    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { }

    @Override
    public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        if (target.onGround() || target.isPassenger() || target.isInWater() || target.isInLava() || target.onClimbable()) {
            this.airborneTicks.remove(target.getUUID());
            return Evaluation.COMPLIANT;
        }
        boolean flying = target instanceof ServerPlayer player && player.isFallFlying();
        int weight = flying || this.isHighAboveGround(level, target) ? FLIGHT_WEIGHT : 1;
        return this.airborneTicks.merge(target.getUUID(), weight, Integer::sum) > AIRBORNE_LIMIT
                ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }

    private boolean isHighAboveGround(ServerLevel level, LivingEntity target) {
        if (GravityApi.isGravityInverted(target)) {
            return false;
        }
        BlockPos feet = target.blockPosition();
        for (int depth = 1; depth <= HIGH_ABOVE_GROUND_BLOCKS; depth++) {
            BlockPos below = feet.below(depth);
            if (!level.isLoaded(below) || !level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int contextWeight(KingEntity king, LivingEntity target) {
        return 4 + king.behaviorScore(KingEntity.Behavior.AIRBORNE);
    }

    @Override
    public int durationTicks() {
        return 240;
    }

    @Override
    public void onKingDisplaced(LivingEntity target) {
        this.airborneTicks.remove(target.getUUID());
    }

    @Override
    public void onEnded() {
        this.airborneTicks.clear();
    }
}
