package com.craisinlord.antarchy.content.entity.royal.decree;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class DoNotFleeDecree implements RoyalDecree {
    private static final int FLEE_BUDGET_TICKS = 15;
    private static final double FLEE_SPEED_THRESHOLD = 0.15D;
    private final Map<UUID, Vec3> lastPositions = new HashMap<>();
    private final Map<UUID, Integer> fleeTicks = new HashMap<>();

    public String translationKey() {
        return "decree.antarchy.do_not_flee";
    }

    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { }

    @Override
    public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        UUID id = target.getUUID();
        Vec3 position = target.position();
        Vec3 last = this.lastPositions.put(id, position);
        if (last == null || !target.isSprinting()) {
            return Evaluation.COMPLIANT;
        }
        Vec3 away = position.subtract(king.position()).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() < 1.0E-4D) {
            return Evaluation.COMPLIANT;
        }
        double outward = position.subtract(last).multiply(1.0D, 0.0D, 1.0D).dot(away.normalize());
        if (outward < FLEE_SPEED_THRESHOLD) {
            return Evaluation.COMPLIANT;
        }
        return this.fleeTicks.merge(id, 1, Integer::sum) > FLEE_BUDGET_TICKS
                ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }

    @Override
    public int contextWeight(KingEntity king, LivingEntity target) {
        return 4 + king.behaviorScore(KingEntity.Behavior.RETREATING) / 2 + king.behaviorScore(KingEntity.Behavior.SPRINTING) / 2;
    }

    @Override
    public int durationTicks() {
        return 200;
    }

    @Override
    public boolean suppressesAttack(String attackId) {
        return "stomp".equals(attackId);
    }

    @Override
    public void onKingDisplaced(LivingEntity target) {
        this.lastPositions.remove(target.getUUID());
    }

    @Override
    public void onWarned(ServerLevel level, KingEntity king, LivingEntity target) {
        this.lastPositions.remove(target.getUUID());
        this.fleeTicks.remove(target.getUUID());
    }

    @Override
    public void onEnded() {
        this.lastPositions.clear();
        this.fleeTicks.clear();
    }
}
