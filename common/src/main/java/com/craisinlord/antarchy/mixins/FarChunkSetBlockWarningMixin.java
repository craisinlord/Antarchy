package com.craisinlord.antarchy.mixins;

import net.minecraft.server.level.WorldGenRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldGenRegion.class)
public abstract class FarChunkSetBlockWarningMixin {
    @Redirect(
            method = "ensureCanWrite",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;logAndPauseIfInIde(Ljava/lang/String;)V")
    )
    private void antarchy$suppressFarChunkWarning(String message) {
    }
}
