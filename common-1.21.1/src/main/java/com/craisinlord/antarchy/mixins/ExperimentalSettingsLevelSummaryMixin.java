package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelSummary.class, priority = 1001)
public abstract class ExperimentalSettingsLevelSummaryMixin {
    @Inject(method = "isExperimental", at = @At("RETURN"), cancellable = true)
    private void antarchy$hideExperimentalStatus(CallbackInfoReturnable<Boolean> cir) {
        if (AntarchySettings.experimentalSettingsPopupDisabled()) {
            cir.setReturnValue(false);
        }
    }
}
