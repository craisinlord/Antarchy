package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class NoRetreatDecree implements RoyalDecree {
    private static final double ALLOWED_RANGE = 26.0D;
    public String translationKey() { return "decree.antarchy.no_retreat"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        if (target.distanceTo(king) > ALLOWED_RANGE) {
            king.invokeJudgment(target);
        }
    }
    @Override
    public int contextWeight(KingEntity king, LivingEntity target) {
        return 4 + king.behaviorScore(KingEntity.Behavior.RETREATING);
    }
}
