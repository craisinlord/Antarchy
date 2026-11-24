package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnContext;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TrialSpawner.class)
public abstract class UndertrialSpawnHeightMixin {
    @ModifyVariable(method = "spawnMob", at = @At("STORE"), ordinal = 1)
    private double antarchy$hangFromCeiling(double y, @Local(argsOnly = true) BlockPos pos, @Local ListTag position,
                                            @Local Optional<EntityType<?>> type) {
        if (!UndertrialSpawnContext.isActive() || position.size() >= 2 || type.isEmpty()) {
            return y;
        }
        return UndertrialSpawnContext.ceilingFeetY(pos, y, type.get().getHeight());
    }
}
