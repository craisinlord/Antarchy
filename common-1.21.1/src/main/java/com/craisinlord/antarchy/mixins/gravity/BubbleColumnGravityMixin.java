package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BubbleColumnBlock.class)
public abstract class BubbleColumnGravityMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void antarchy$applyBubbleForces(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }

        boolean drag = state.getValue(BubbleColumnBlock.DRAG_DOWN);
        Vec3 velocity = AntarchyGravityApi.getWorldVelocity(entity);
        double vertical = drag ? Math.max(-0.9D, velocity.y - 0.03D) : Math.min(1.8D, velocity.y + 0.1D);
        AntarchyGravityApi.setWorldVelocity(entity, new Vec3(velocity.x, vertical, velocity.z));
        entity.onAboveBubbleCol(!drag);
        if (drag) {
            entity.resetFallDistance();
        }
        ci.cancel();
    }
}
