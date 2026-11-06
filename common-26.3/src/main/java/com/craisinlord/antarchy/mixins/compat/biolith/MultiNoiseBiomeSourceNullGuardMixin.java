package com.craisinlord.antarchy.mixins.compat.biolith;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceNullGuardMixin {
    @Shadow
    protected abstract Climate.ParameterList<Holder<Biome>> parameters();

    @WrapMethod(method = "getNoiseBiome(Lnet/minecraft/world/level/biome/Climate$TargetPoint;)Lnet/minecraft/core/Holder;")
    private Holder<Biome> antarchy$neverReturnNullBiome(Climate.TargetPoint target, Operation<Holder<Biome>> original) {
        Holder<Biome> biome = original.call(target);
        if (biome != null) {
            return biome;
        }

        Climate.ParameterList<Holder<Biome>> parameters = this.parameters();
        biome = parameters.findValue(target);
        if (biome != null) {
            return biome;
        }

        for (var entry : parameters.values()) {
            if (entry.getSecond() != null) {
                return entry.getSecond();
            }
        }
        return null;
    }
}
