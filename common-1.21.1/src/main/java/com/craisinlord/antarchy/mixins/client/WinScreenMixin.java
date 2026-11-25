package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WinScreen.class)
public abstract class WinScreenMixin {
    @Unique
    private static final ResourceLocation ANTARCHY$END_POEM = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "texts/end.txt");

    @ModifyExpressionValue(method = "init", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/WinScreen;END_POEM_LOCATION:Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation antarchy$antEndPoem(ResourceLocation original) {
        return AntarchySettings.antEndPoem() ? ANTARCHY$END_POEM : original;
    }
}
