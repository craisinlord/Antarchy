package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.SectionUploadBudget;
import java.util.Queue;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionRenderDispatcher.class)
public abstract class SectionUploadBudgetMixin {
    @Shadow
    @Final
    private Queue<Runnable> toUpload;

    @Shadow
    private volatile boolean closed;

    @Inject(method = "uploadAllPendingUploads", at = @At("HEAD"), cancellable = true)
    private void antarchy$budgetUploads(CallbackInfo callback) {
        if (this.closed) return;
        SectionUploadBudget.runFrame(this.toUpload);
        callback.cancel();
    }
}
