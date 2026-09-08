package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class ComeNoCloserDecree implements RoyalDecree {
    public String translationKey() { return "decree.antarchy.come_no_closer"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { }
    @Override public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        return target.distanceTo(king) <= 16.0D ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }
    @Override
    public int contextWeight(KingEntity king, LivingEntity target) {
        return 4 + king.behaviorScore(KingEntity.Behavior.HUGGING);
    }
}
