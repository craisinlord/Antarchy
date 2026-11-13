package com.craisinlord.antarchy.fabric.mixin.infinity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.codexarchonic.infinity.util.InfinityMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = InfinityMethods.class)
public interface InfinityBiomeCheckMixin {
    @WrapMethod(method = "isBiomeInfinity(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Z")
    private static boolean antarchy$notInfinityWhenUnreadable(LevelAccessor level, BlockPos pos, Operation<Boolean> original) {
        try {
            return original.call(level, pos);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
