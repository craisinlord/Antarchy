package com.craisinlord.antarchy.fabric.mixin.infinity;

import net.lerariemann.infinity.dimensions.features.fluid_springs.RandomSpring;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(value = RandomSpring.class, remap = false)
/*
 * Skips bad fluid picks for Infinity random springs on Fabric.
 */
public abstract class RandomSpringMixin {
    @Redirect(
            method = "feature",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/lerariemann/infinity/util/core/RandomProvider;randomName(Ljava/util/Random;Lnet/lerariemann/infinity/util/core/ConfigType;)Ljava/lang/String;"
            )
    )
    private String antarchy$useFluidIdForSpring(RandomProvider provider, Random random, ConfigType configType) {
        if (configType != ConfigType.FLUIDS) {
            return provider.randomName(random, configType);
        }

        CompoundTag fluidTag = provider.randomElement(random, configType);
        String fluidId = fluidTag.getString("fluidName");
        if (!fluidId.isEmpty()) {
            return fluidId;
        }
        return fluidTag.getString("Name");
    }
}
