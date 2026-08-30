package com.craisinlord.antarchy.content.entity.royal.decree;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface RoyalDecree {
    String name();
    void apply(ServerLevel level, KingEntity king, LivingEntity target);
}
