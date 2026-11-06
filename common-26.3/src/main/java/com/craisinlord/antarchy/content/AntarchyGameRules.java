package com.craisinlord.antarchy.content;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public final class AntarchyGameRules {
    public static GameRule<Boolean> RULE_DO_ANT_GREIFING;
    public static GameRule<Boolean> RULE_DO_HERCULES_BEETLE_GREIFING;
    public static GameRule<Boolean> RULE_DO_TERMITE_GREIFING;
    public static GameRule<Boolean> RULE_DO_CAVARYN_HORDES;

    private AntarchyGameRules() {
    }

    public static void bootstrap(BooleanRuleRegistrar registrar) {
        if (RULE_DO_ANT_GREIFING != null) {
            return;
        }
        RULE_DO_ANT_GREIFING = registrar.register("do_ant_greifing", booleanRule(GameRuleCategory.MOBS, true));
        RULE_DO_HERCULES_BEETLE_GREIFING = registrar.register("do_hercules_beetle_greifing", booleanRule(GameRuleCategory.MOBS, true));
        RULE_DO_TERMITE_GREIFING = registrar.register("do_termite_greifing", booleanRule(GameRuleCategory.MOBS, true));
        RULE_DO_CAVARYN_HORDES = registrar.register("do_cavaryn_hordes", booleanRule(GameRuleCategory.MOBS, true));
    }

    public static GameRule<Boolean> booleanRule(GameRuleCategory category, boolean defaultValue) {
        return new GameRule<>(
                category,
                GameRuleType.BOOL,
                BoolArgumentType.bool(),
                GameRuleTypeVisitor::visitBoolean,
                Codec.BOOL,
                value -> value ? 1 : 0,
                defaultValue,
                FeatureFlagSet.of()
        );
    }

    @FunctionalInterface
    public interface BooleanRuleRegistrar {
        GameRule<Boolean> register(String name, GameRule<Boolean> rule);
    }
}
