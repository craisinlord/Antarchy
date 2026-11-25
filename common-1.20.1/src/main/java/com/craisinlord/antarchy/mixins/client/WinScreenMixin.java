package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.WinScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WinScreen.class)
public abstract class WinScreenMixin {
    @ModifyExpressionValue(method = "init", at = @At(value = "CONSTANT", args = "stringValue=texts/end.txt"))
    private String antarchy$antEndPoem(String original) {
        return AntarchySettings.antEndPoem() ? Antarchy.MODID + ":texts/end.txt" : original;
    }
}
