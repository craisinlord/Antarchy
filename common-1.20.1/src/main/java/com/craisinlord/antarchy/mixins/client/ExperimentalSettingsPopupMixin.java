package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public abstract class ExperimentalSettingsPopupMixin {
    @Inject(method = "confirmWorldCreation", at = @At("HEAD"), cancellable = true)
    private static void antarchy$skipExperimentalSettingsPopup(Minecraft minecraft, CreateWorldScreen screen, Lifecycle lifecycle, Runnable callback, boolean skipWarnings, CallbackInfo ci) {
        if (AntarchySettings.experimentalSettingsPopupDisabled() && lifecycle == Lifecycle.experimental() && !skipWarnings) {
            callback.run();
            ci.cancel();
        }
    }
}
