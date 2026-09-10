package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class AdvanceDecree implements RoyalDecree {
    private static final double REQUIRED_RANGE = 14.0D;
    public String translationKey() { return "decree.antarchy.advance"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { king.setDecreeRetreatPressure(true); }
    @Override public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        return target.distanceToSqr(king) > REQUIRED_RANGE * REQUIRED_RANGE ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }
    @Override
    public int contextWeight(KingEntity king, LivingEntity target) {
        return 4 + king.behaviorScore(KingEntity.Behavior.KEEPING_FAR);
    }
}
