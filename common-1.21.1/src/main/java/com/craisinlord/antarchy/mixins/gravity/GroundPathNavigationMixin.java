package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.GravityWalkNodeEvaluator;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GroundPathNavigation.class)
public abstract class GroundPathNavigationMixin {
    @Inject(method = "createPathFinder", at = @At("HEAD"), cancellable = true)
    private void antarchy$useGravityEvaluator(int maxVisitedNodes, CallbackInfoReturnable<PathFinder> cir) {
        var accessor = (PathNavigationAccessor) (Object) this;
        NodeEvaluator evaluator = new GravityWalkNodeEvaluator();
        // GroundPathNavigation enables door passing on its vanilla evaluator.
        // Preserve that setting when swapping in the gravity-aware evaluator.
        evaluator.setCanPassDoors(true);
        accessor.antarchy$setNodeEvaluator(evaluator);
        cir.setReturnValue(new PathFinder(evaluator, maxVisitedNodes));
    }

    /**
     * GroundPathNavigation normalizes entity destinations toward the floor
     * before delegating to PathNavigation. For inverted mobs, replace that
     * final destination with a bounded, collision-free ceiling anchor.
     */
    @org.spongepowered.asm.mixin.injection.ModifyArg(
            method = "createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;"
            ),
            index = 0
    )
    private BlockPos antarchy$projectInvertedDestination(BlockPos normalizedDestination) {
        Mob mob = ((PathNavigationAccessor) (Object) this).antarchy$getMob();
        if (!AntarchyGravityApi.isGravityInverted(mob)) {
            return normalizedDestination;
        }

        EntityDimensions dimensions = mob.getDimensions(mob.getPose());
        int centerX = normalizedDestination.getX();
        int centerZ = normalizedDestination.getZ();
        int baseY = normalizedDestination.getY();
        int minY = Math.max(mob.level().getMinBuildHeight(), baseY - 32);
        int maxY = Math.min(mob.level().getMaxBuildHeight() - 2, baseY + 32);
        for (int candidateY = baseY; candidateY <= maxY; candidateY++) {
            BlockPos support = new BlockPos(centerX, candidateY + 1, centerZ);
            if (!mob.level().getBlockState(support).isFaceSturdy(mob.level(), support, Direction.DOWN)) {
                continue;
            }

            double topY = candidateY + 1.0D - 1.0E-3D;
            double halfWidth = dimensions.width() / 2.0D;
            AABB body = new AABB(
                    centerX + 0.5D - halfWidth,
                    topY - dimensions.height(),
                    centerZ + 0.5D - halfWidth,
                    centerX + 0.5D + halfWidth,
                    topY,
                    centerZ + 0.5D + halfWidth
            ).deflate(1.0E-4D);
            if (mob.level().noCollision(mob, body)) {
                return new BlockPos(centerX, candidateY, centerZ);
            }
        }

        return normalizedDestination;
    }

    @Inject(method = "getTempMobPos", at = @At("HEAD"), cancellable = true)
    private void antarchy$useInvertedMobAnchor(CallbackInfoReturnable<Vec3> cir) {
        Mob mob = ((PathNavigationAccessor) (Object) this).antarchy$getMob();
        if (AntarchyGravityApi.isGravityInverted(mob)) {
            cir.setReturnValue(new Vec3(mob.getX(), mob.getY(), mob.getZ()));
        }
    }
}
