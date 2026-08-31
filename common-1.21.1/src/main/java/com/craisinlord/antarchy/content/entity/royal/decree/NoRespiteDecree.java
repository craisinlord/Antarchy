package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
public final class NoRespiteDecree implements RoyalDecree {
    public String translationKey() { return "decree.antarchy.no_respite"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { }
}
