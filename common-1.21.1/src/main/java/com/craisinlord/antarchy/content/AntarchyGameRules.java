package com.craisinlord.antarchy.content;

import net.minecraft.world.level.GameRules;

public final class AntarchyGameRules {
    public static GameRules.Key<GameRules.BooleanValue> RULE_DO_ANT_GREIFING;
    public static GameRules.Key<GameRules.BooleanValue> RULE_DO_HERCULES_BEETLE_GREIFING;
    public static GameRules.Key<GameRules.BooleanValue> RULE_DO_TERMITE_GREIFING;
    public static GameRules.Key<GameRules.BooleanValue> RULE_DO_CAVARYN_HORDES;

    private AntarchyGameRules() {
    }

    public static void bootstrap(BooleanRuleRegistrar registrar) {
        if (RULE_DO_ANT_GREIFING != null) {
            return;
        }
        RULE_DO_ANT_GREIFING = registrar.register("doAntGreifing", GameRules.Category.MOBS, true);
        RULE_DO_HERCULES_BEETLE_GREIFING = registrar.register("doHerculesBeetleGreifing", GameRules.Category.MOBS, true);
        RULE_DO_TERMITE_GREIFING = registrar.register("doTermiteGreifing", GameRules.Category.MOBS, true);
        RULE_DO_CAVARYN_HORDES = registrar.register("doCavarynHordes", GameRules.Category.MOBS, true);
    }

    @FunctionalInterface
    public interface BooleanRuleRegistrar {
        GameRules.Key<GameRules.BooleanValue> register(String name, GameRules.Category category, boolean defaultValue);
    }
}
