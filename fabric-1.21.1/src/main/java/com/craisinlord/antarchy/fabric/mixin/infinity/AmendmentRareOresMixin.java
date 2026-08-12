package com.craisinlord.antarchy.fabric.mixin.infinity;

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
/*
 * Makes Antarchy's uranium/titanium ores and blocks much rarer in Infinite Dimensions'
 * procedural dimensions. This is done natively via Infinity's own amendment system rather
 * than relying on players editing infinity's config, keyed off the antarchy:infinity_dimension_rare_ores
 * block tag so the list stays data-driven and native to Antarchy.
 */
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
    }
}
