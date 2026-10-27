package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.CavarynSkyRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class CavarynSkyMixin {
    @Shadow
    private ClientLevel level;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(
            method = "extract",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;cloudColor:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void antarchy$skipCavarynClouds(CallbackInfo ci) {
        if (this.level != null && CavarynSkyRenderer.shouldRender(this.level)) {
            this.levelRenderState.cloudColor = 0;
        }
    }
}
