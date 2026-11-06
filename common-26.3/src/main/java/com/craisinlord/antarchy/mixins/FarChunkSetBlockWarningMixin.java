package com.craisinlord.antarchy.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldGenRegion.class)
public abstract class FarChunkSetBlockWarningMixin {
    @Shadow
    @Final
    private ChunkStep generatingStep;

    @Inject(method = "warnIfReadOutsideWriteZone", at = @At("HEAD"), cancellable = true)
    private void antarchy$suppressSpawnStepReadWarning(int chunkX, int chunkZ, CallbackInfo callback) {
        if (this.generatingStep.targetStatus() == ChunkStatus.SPAWN) {
            callback.cancel();
        }
    }

    @WrapOperation(
            method = "ensureCanWrite",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;logAndPauseIfInIde(Ljava/lang/String;)V")
    )
    private void antarchy$suppressFarChunkWarning(String message, Operation<Void> original) {
    }
}
