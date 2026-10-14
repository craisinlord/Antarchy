package com.craisinlord.antarchy.mixins.compat.biolith;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceNullGuardMixin {
    @Shadow
    protected abstract Climate.ParameterList<Holder<Biome>> parameters();

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;", at = @At("RETURN"), cancellable = true)
    private void antarchy$neverReturnNullBiome(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        if (cir.getReturnValue() != null) {
            return;
        }

        Climate.ParameterList<Holder<Biome>> parameters = this.parameters();
        Holder<Biome> biome = parameters.findValue(sampler.sample(x, y, z));
        if (biome != null) {
            cir.setReturnValue(biome);
            return;
        }

        for (var entry : parameters.values()) {
            if (entry.getSecond() != null) {
                cir.setReturnValue(entry.getSecond());
                return;
            }
        }
    }
}
