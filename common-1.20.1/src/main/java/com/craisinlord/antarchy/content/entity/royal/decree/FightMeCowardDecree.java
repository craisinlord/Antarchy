package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
public final class FightMeCowardDecree implements RoyalDecree {
    private static final long APPROACH_TICKS = 80L;
    private static final int EXIT_GRACE_TICKS = 40;
    private static final int PULL_INTERVAL_TICKS = 10;
    private static final double REQUIRED_RANGE_SQR = 784.0D;
    private final Map<UUID, Long> deadlines = new HashMap<>();
    private final Map<UUID, Integer> outsideTicks = new HashMap<>();
    public String name() { return "FIGHT ME, COWARD!"; }
    public String translationKey() { return "decree.antarchy.fight_me_coward"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        this.deadlines.computeIfAbsent(target.getUUID(), ignored -> level.getGameTime() + APPROACH_TICKS);
        if (target.distanceToSqr(king) > REQUIRED_RANGE_SQR && target.tickCount % PULL_INTERVAL_TICKS == 0) {
            Vec3 toKing = king.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toKing.lengthSqr() > 1.0E-4D) {
                Vec3 pull = toKing.normalize().scale(0.6D);
                target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.1D, pull.z));
                target.hasImpulse = true;
                target.hurtMarked = true;
            }
        }
    }
    @Override
    public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        UUID id = target.getUUID();
        long deadline = this.deadlines.computeIfAbsent(id, ignored -> level.getGameTime() + APPROACH_TICKS);
        if (target.distanceToSqr(king) <= REQUIRED_RANGE_SQR) {
            this.outsideTicks.remove(id);
            return Evaluation.COMPLIANT;
        }
        if (level.getGameTime() < deadline) {
            return Evaluation.COMPLIANT;
        }
        return this.outsideTicks.merge(id, 1, Integer::sum) > EXIT_GRACE_TICKS
                ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }

    @Override
    public int countdownTicks(KingEntity king, LivingEntity target) {
        if (target.distanceToSqr(king) <= REQUIRED_RANGE_SQR) {
            return -1;
        }
        long now = king.level().getGameTime();
        return (int) Math.max(0L, this.deadlines.getOrDefault(target.getUUID(), now + APPROACH_TICKS) - now);
    }

    @Override
    public void onKingDisplaced(LivingEntity target) {
        this.outsideTicks.remove(target.getUUID());
    }

    @Override
    public void onEnded() {
        this.deadlines.clear();
        this.outsideTicks.clear();
    }
}
