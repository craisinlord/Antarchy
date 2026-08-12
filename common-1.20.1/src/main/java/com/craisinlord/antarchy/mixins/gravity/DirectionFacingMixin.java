package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Comparator;

@Mixin(Direction.class)
/*
 * Fixes facing math that assumes gravity is always down.
 */
public abstract class DirectionFacingMixin {

    
    @Inject(method = "orderedByNearest", at = @At("HEAD"), cancellable = true)
    private static void antarchy$fixEntityFacingOrder(Entity entity, CallbackInfoReturnable<Direction[]> cir) {
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }
        Vec3 look = entity.getViewVector(1.0F);

        Direction[] sorted = Direction.values();
        Arrays.sort(sorted, Comparator.comparingDouble(
                (Direction d) -> d.getStepX() * look.x + d.getStepY() * look.y + d.getStepZ() * look.z
        ).reversed());

        cir.setReturnValue(sorted);
    }
}
