package com.craisinlord.antarchy.content.entity.royal.decree;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class KneelDecree implements RoyalDecree {
    private static final long KNEEL_TICKS = 100L;
    private final Map<UUID, Long> deadlines = new HashMap<>();
    public String translationKey() {
        return "decree.antarchy.kneel";
    }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        this.deadlines.computeIfAbsent(target.getUUID(), ignored -> level.getGameTime() + KNEEL_TICKS);
    }

    @Override
    public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        if (target.isCrouching()) {
            return Evaluation.COMPLETE;
        }
        long deadline = this.deadlines.computeIfAbsent(target.getUUID(), ignored -> level.getGameTime() + KNEEL_TICKS);
        return level.getGameTime() >= deadline ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }

    @Override
    public int countdownTicks(KingEntity king, LivingEntity target) {
        long now = king.level().getGameTime();
        return (int) Math.max(0L, this.deadlines.getOrDefault(target.getUUID(), now + KNEEL_TICKS) - now);
    }

    @Override
    public void onEnded() {
        this.deadlines.clear();
    }
}
