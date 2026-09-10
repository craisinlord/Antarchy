package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class CloseQuartersDecree implements RoyalDecree {
    public String translationKey() { return "decree.antarchy.close_quarters"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { }
    @Override
    public int contextWeight(KingEntity king, LivingEntity target) {
        return 4 + king.behaviorScore(KingEntity.Behavior.RANGED);
    }
}
