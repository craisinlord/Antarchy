package com.craisinlord.antarchy.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.WorldGenRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldGenRegion.class)
public abstract class FarChunkSetBlockWarningMixin {
    @WrapOperation(
            method = "ensureCanWrite",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;logAndPauseIfInIde(Ljava/lang/String;)V")
    )
    private void antarchy$suppressFarChunkWarning(String message, Operation<Void> original) {
    }
}
