package com.craisinlord.antarchy.mixins.compat.iris;

import com.craisinlord.antarchy.content.client.compat.IrisBlockIdCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
        "net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping",
        "net.coderbot.iris.block_rendering.BlockMaterialMapping"
}, remap = false)
public abstract class IrisPrimordialGrassBlockIdMixin {
    @Inject(method = "createBlockStateIdMap", at = @At("RETURN"), require = 0, remap = false)
    private static void antarchy$aliasPrimordialGrass(CallbackInfoReturnable<Object> callback) {
        IrisBlockIdCompat.aliasPrimordialGrass(callback.getReturnValue());
    }
}
