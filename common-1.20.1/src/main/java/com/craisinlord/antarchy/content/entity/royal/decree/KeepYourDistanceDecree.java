package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class KeepYourDistanceDecree implements RoyalDecree {
    private static final double INNER_RADIUS = 16.0D;
    public String translationKey() { return "decree.antarchy.keep_your_distance"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { }
    @Override public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        return target.distanceTo(king) <= INNER_RADIUS ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }
    @Override
    public int contextWeight(KingEntity king, LivingEntity target) {
        return 4 + king.behaviorScore(KingEntity.Behavior.HUGGING);
    }
}
