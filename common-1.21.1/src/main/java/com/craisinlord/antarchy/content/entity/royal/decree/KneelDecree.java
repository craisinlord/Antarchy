package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class KneelDecree implements RoyalDecree {
    private final Map<UUID, Integer> deadlines = new HashMap<>();
    public String translationKey() { return "decree.antarchy.kneel"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        UUID id = target.getUUID();
        deadlines.computeIfAbsent(id, ignored -> target.tickCount + 100);
    }

    @Override
    public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        if (target.isCrouching()) {
            return Evaluation.COMPLETE;
        }
        int deadline = deadlines.computeIfAbsent(target.getUUID(), ignored -> target.tickCount + 100);
        return target.tickCount >= deadline ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }

    @Override
    public int countdownTicks(LivingEntity target) {
        return this.deadlines.getOrDefault(target.getUUID(), target.tickCount + 100) - target.tickCount;
    }

    @Override
    public void onEnded() {
        this.deadlines.clear();
    }
}
