package com.craisinlord.antarchy.content.entity.royal.decree;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class HandsOffTheCrownDecree implements RoyalDecree {
    public String translationKey() {
        return "decree.antarchy.hands_off_the_crown";
    }

    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { }

    @Override
    public int durationTicks() {
        return 160;
    }

    @Override
    public boolean suppressesAttack(String attackId) {
        return "stomp".equals(attackId) || "staggered_volley".equals(attackId) || "ice_spikes".equals(attackId);
    }
}
