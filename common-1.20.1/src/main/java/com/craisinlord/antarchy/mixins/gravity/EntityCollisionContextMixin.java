package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityCollisionContext.class)
/*
 * Uses gravity-aware entity bounds for block collision checks.
 */
public abstract class EntityCollisionContextMixin {

    @Shadow @Final private double entityBottom;
    @Shadow @Final @Nullable private Entity entity;

    @Redirect(
            method = "<init>(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getY()D")
    )
    private static double antarchy$fixCollisionContextBottom(Entity entity) {
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return entity.getY();
        }

        return AntarchyGravityRotationUtil.boxWorldToPlayer(entity.getBoundingBox(), direction).minY;
    }

    @Inject(method = "isAbove", at = @At("HEAD"), cancellable = true)
    private void antarchy$fixIsAbove(VoxelShape shape, BlockPos pos, boolean canAscend, CallbackInfoReturnable<Boolean> cir) {
        if (this.entity == null) {
            return;
        }

        if (shape.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(this.entity);
        if (!direction.isInverted()) {
            return;
        }

        double baseMinY = AntarchyGravityRotationUtil.boxWorldToPlayer(new AABB(pos), direction).minY;
        double shapeMaxY = AntarchyGravityRotationUtil.boxWorldToPlayer(
                shape.bounds().inflate(-1.0E-5D),
                direction
        ).maxY;
        cir.setReturnValue(this.entityBottom > baseMinY + shapeMaxY);
    }
}
