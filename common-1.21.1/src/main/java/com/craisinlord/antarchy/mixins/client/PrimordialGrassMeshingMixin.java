package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.block.PrimordialGrassBlock;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionCompiler.class)
public abstract class PrimordialGrassMeshingMixin {
    @Inject(method = "compile*", at = @At("HEAD"))
    private void antarchy$beginGrassMeshing(CallbackInfoReturnable<SectionCompiler.Results> callback, @Local(argsOnly = true) SectionPos section) {
        PrimordialGrassBlock.beginMeshing(section.x(), section.y(), section.z());
    }

    @Inject(method = "compile*", at = @At("RETURN"))
    private void antarchy$endGrassMeshing(CallbackInfoReturnable<SectionCompiler.Results> callback) {
        PrimordialGrassBlock.endMeshing();
    }
}
