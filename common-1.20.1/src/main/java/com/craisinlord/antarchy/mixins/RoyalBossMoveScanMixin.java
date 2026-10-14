package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.stream.Stream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class RoyalBossMoveScanMixin {
    @WrapOperation(
            method = "move",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockStatesIfLoaded(Lnet/minecraft/world/phys/AABB;)Ljava/util/stream/Stream;")
    )
    private Stream<BlockState> antarchy$throttleRoyalFireScan(Level level, AABB bounds, Operation<Stream<BlockState>> original) {
        if ((Object) this instanceof RoyalBossEntity royal && !royal.shouldRunHeavyBlockChecks()) {
            return Stream.empty();
        }
        return original.call(level, bounds);
    }
}
