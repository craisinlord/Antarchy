package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class CloseQuartersDecree implements RoyalDecree {
    public String name() { return "CLOSE QUARTERS"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { if (target.distanceTo(king) <= 12.0D) target.hurt(king.damageSources().mobAttack(king), 4.0F); }
}
