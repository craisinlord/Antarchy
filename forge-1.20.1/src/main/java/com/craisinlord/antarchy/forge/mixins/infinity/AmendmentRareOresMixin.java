package com.craisinlord.antarchy.forge.mixins.infinity;

import net.lerariemann.infinity.util.config.Amendment;
import net.lerariemann.infinity.util.core.ConfigType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(value = Amendment.class, remap = false)

public class AmendmentRareOresMixin {
    @Inject(method = "getAmendmentList", at = @At("RETURN"), remap = false)
    private static void antarchy$addRareOreAmendment(CallbackInfoReturnable<Map<ConfigType, List<Amendment>>> cir) {
        Map<ConfigType, List<Amendment>> data = cir.getReturnValue();
        List<Amendment> list = data.computeIfAbsent(ConfigType.BLOCKS, k -> new ArrayList<>());
        list.add(new Amendment(
                ConfigType.BLOCKS,
                new Amendment.UniversalModSelector(),
                new Amendment.MatchingBlockTagSelector("antarchy:infinity_dimension_rare_ores"),
                new Amendment.SetValue(0.02)
        ));
        data.computeIfAbsent(ConfigType.EFFECTS, k -> new ArrayList<>()).add(new Amendment(
                ConfigType.EFFECTS,
                new Amendment.MatchingModSelector("antarchy"),
                new Amendment.MatchingSelector("antarchy:paralyzed"),
                new Amendment.SetValue(0.0)
        ));
    }
}
