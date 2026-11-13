package com.craisinlord.antarchy.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Aquifer.NoiseBasedAquifer.class, priority = 1500)
public abstract class AquiferSubstanceGuardMixin {
    @WrapMethod(method = "computeSubstance")
    private BlockState antarchy$solidOnGridOverflow(int blockX, int blockY, int blockZ, double density,
                                                   Operation<BlockState> original) {
        try {
            return original.call(blockX, blockY, blockZ, density);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
