package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class ShowNoMercyDecree implements RoyalDecree {
    private static final long IDLE_DAMAGE_LIMIT_TICKS = 80L;
    public String translationKey() { return "decree.antarchy.show_no_mercy"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        if (king.ticksSincePlayerDamage(target) > IDLE_DAMAGE_LIMIT_TICKS) {
            king.invokeJudgment(target);
        }
    }
}
