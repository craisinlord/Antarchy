package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class StandTallDecree implements RoyalDecree {
    public String translationKey() { return "decree.antarchy.stand_tall"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        if (target.isCrouching()) {
            king.invokeJudgment(target);
        }
    }
}
