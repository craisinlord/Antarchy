package com.craisinlord.antarchy.content.entity.royal.decree;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface RoyalDecree {
    enum Evaluation {
        COMPLIANT,
        VIOLATED,
        COMPLETE
    }

    String translationKey();
    void apply(ServerLevel level, KingEntity king, LivingEntity target);

    default Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        return Evaluation.COMPLIANT;
    }

    default int countdownTicks(KingEntity king, LivingEntity target) {
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
