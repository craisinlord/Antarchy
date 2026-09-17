package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
public final class FightMeCowardDecree implements RoyalDecree {
    private static final int APPROACH_TICKS = 80;
    private final Map<UUID, Integer> deadlines = new HashMap<>();
    public String name() { return "FIGHT ME, COWARD!"; }
    public String translationKey() { return "decree.antarchy.fight_me_coward"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        this.deadlines.computeIfAbsent(target.getUUID(), ignored -> target.tickCount + APPROACH_TICKS);
        if (target.distanceToSqr(king) > 784.0D) {
            Vec3 pull = king.position().subtract(target.position()).normalize().scale(0.35D);
            target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.05D, pull.z));
            target.hasImpulse = true;
        }
    }
    @Override
    public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        int deadline = this.deadlines.computeIfAbsent(target.getUUID(), ignored -> target.tickCount + APPROACH_TICKS);
        return target.tickCount >= deadline && target.distanceToSqr(king) > 784.0D
                ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }

    @Override
    public int countdownTicks(KingEntity king, LivingEntity target) {
        if (target.distanceToSqr(king) <= 784.0D) {
            return -1;
        }
        return this.deadlines.getOrDefault(target.getUUID(), target.tickCount + APPROACH_TICKS) - target.tickCount;
    }

    @Override
    public void onEnded() {
        this.deadlines.clear();
    }
}
