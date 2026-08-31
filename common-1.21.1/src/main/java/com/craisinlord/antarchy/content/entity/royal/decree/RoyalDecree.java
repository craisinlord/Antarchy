package com.craisinlord.antarchy.content.entity.royal.decree;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface RoyalDecree {
    String translationKey();
    void apply(ServerLevel level, KingEntity king, LivingEntity target);

    default int countdownTicks(LivingEntity target) {
        return -1;
    }

    default String instructionKey() {
        return this.translationKey() + ".subtitle";
    }

    default int contextWeight(KingEntity king, LivingEntity target) {
        return 4;
    }

    default void onEnded() {
    }
}
