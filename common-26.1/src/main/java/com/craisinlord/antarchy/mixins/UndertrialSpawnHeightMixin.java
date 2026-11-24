package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TrialSpawner.class)
public abstract class UndertrialSpawnHeightMixin {
    @WrapOperation(
            method = "spawnMob",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElseGet(Ljava/util/function/Supplier;)Ljava/lang/Object;")
    )
    private Object antarchy$hangFromCeiling(Optional<Vec3> position, Supplier<Vec3> fallback, Operation<Object> original,
                                            @Local(argsOnly = true) BlockPos pos,
                                            @Local(ordinal = 0) Optional<EntityType<?>> type) {
        Object result = original.call(position, fallback);
        if (!UndertrialSpawnContext.isActive() || position.isPresent() || type.isEmpty()
                || !(result instanceof Vec3 spawn)) {
            return result;
        }
        return new Vec3(spawn.x, UndertrialSpawnContext.ceilingFeetY(pos, spawn.y, type.get().getHeight()), spawn.z);
    }
}
