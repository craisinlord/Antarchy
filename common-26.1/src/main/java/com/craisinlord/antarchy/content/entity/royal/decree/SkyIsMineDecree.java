package com.craisinlord.antarchy.content.entity.royal.decree;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class SkyIsMineDecree implements RoyalDecree {
    private static final int AIRBORNE_LIMIT_TICKS = 10;
    private final Map<UUID, Integer> airborneTicks = new HashMap<>();
    public String translationKey() {
        return "decree.antarchy.the_sky_is_mine";
    }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { }
    @Override public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        if (target.onGround() || target.isPassenger() || target.isInWater() || target.isInLava() || target.onClimbable()) {
            this.airborneTicks.remove(target.getUUID());
            return Evaluation.COMPLIANT;
        }
        return this.airborneTicks.merge(target.getUUID(), 1, Integer::sum) > AIRBORNE_LIMIT_TICKS
                ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }
    @Override
    public int contextWeight(KingEntity king, LivingEntity target) {
        return 4 + king.behaviorScore(KingEntity.Behavior.AIRBORNE);
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
