package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
/*
 * Fixes move/landing logic that assumes down is always down.
 */
public abstract class EntityMoveMixin {
    @Unique
    private static final Logger ANTARCHY_FALL_LOGGER = LoggerFactory.getLogger("Antarchy/FallDamage");

    @Unique
    private double antarchy$lastWorldMoveY = 0.0;
    @Unique
    private double antarchy$lastPlayerMoveY = 0.0;
    @Unique
    private AABB antarchy$moveStartBox;

    @Inject(method = "move", at = @At("HEAD"))
    private void antarchy$captureMoveStart(MoverType type, Vec3 movement, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        this.antarchy$moveStartBox = entity.getBoundingBox();
        this.antarchy$lastPlayerMoveY = movement.y;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Vec3 antarchy$movePlayerToWorld(Vec3 movement) {
        Entity entity = (Entity) (Object) this;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return movement;
        }

        Vec3 world = AntarchyGravityRotationUtil.vecPlayerToWorld(movement, direction);
        this.antarchy$lastWorldMoveY = world.y;
        return world;
    }

    @ModifyArg(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            ),
            index = 0
    )
    private Vec3 antarchy$moveMaskPlayerToWorld(Vec3 multiplier) {
        Entity entity = (Entity) (Object) this;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return multiplier;
        }

        return AntarchyGravityRotationUtil.maskPlayerToWorld(multiplier, direction);
    }

    @ModifyVariable(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
                    ordinal = 0
            ),
            ordinal = 0
    )
    private Vec3 antarchy$moveWorldToPlayerAttempted(Vec3 movement) {
        Entity entity = (Entity) (Object) this;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return movement;
        }

        return AntarchyGravityRotationUtil.vecWorldToPlayer(movement, direction);
    }

    @ModifyVariable(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
                    ordinal = 0
            ),
            ordinal = 1
    )
    private Vec3 antarchy$moveWorldToPlayerResolved(Vec3 movement) {
        Entity entity = (Entity) (Object) this;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return movement;
        }

        return AntarchyGravityRotationUtil.vecWorldToPlayer(movement, direction);
    }

    
    @Inject(method = "move", at = @At("TAIL"))
    private void antarchy$fixVerticalCollisionBelow(MoverType type, Vec3 movement, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }

        if (this.antarchy$lastPlayerMoveY < 0.0D && this.antarchy$moveStartBox != null) {
            AABB endBox = entity.getBoundingBox();
            Vec3 delta = endBox.getCenter().subtract(this.antarchy$moveStartBox.getCenter());
            AABB sweptBox = this.antarchy$moveStartBox.expandTowards(delta).minmax(endBox).inflate(0.05D);
            if (antarchy$intersectsAntiwater(entity, sweptBox)) {
                float previousFallDistance = entity.fallDistance;
                entity.resetFallDistance();
                if (!entity.level().isClientSide && ANTARCHY_FALL_LOGGER.isDebugEnabled()) {
                    ANTARCHY_FALL_LOGGER.debug(
                            "[FallDamage] Antiwater contact reset during move: entity={} startBox={} endBox={} sweptBox={} playerMoveY={} worldMoveY={} previousFallDistance={}",
                            entity.getClass().getSimpleName(),
                            this.antarchy$moveStartBox,
                            endBox,
                            sweptBox,
                            this.antarchy$lastPlayerMoveY,
                            this.antarchy$lastWorldMoveY,
                            previousFallDistance
                    );
                }
            }
        }

        if (!entity.verticalCollision) return;
        boolean landedOnCeiling = this.antarchy$lastWorldMoveY > 0.0;
        entity.verticalCollisionBelow = landedOnCeiling;
        if (landedOnCeiling) {
            entity.setOnGround(true);
        }
    }

    @Inject(method = "collide", at = @At("HEAD"), cancellable = true)
    private void antarchy$invertedStepUp(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }

        AABB aabb = entity.getBoundingBox();
        List<VoxelShape> entityShapes = entity.level().getEntityCollisions(entity, aabb.expandTowards(movement));
        Vec3 collided = movement.lengthSqr() == 0.0 ? movement : Entity.collideBoundingBox(entity, movement, aabb, entity.level(), entityShapes);

        boolean movedX = movement.x != collided.x;
        boolean movedY = movement.y != collided.y;
        boolean movedZ = movement.z != collided.z;
        boolean pressingIntoCeiling = movedY && movement.y > 0.0D;
        float maxUpStep = entity.maxUpStep();

        if (!(maxUpStep > 0.0F) || !(pressingIntoCeiling || entity.onGround()) || !(movedX || movedZ)) {
            cir.setReturnValue(collided);
            return;
        }

        AABB steppedBox = pressingIntoCeiling ? aabb.move(0.0, collided.y, 0.0) : aabb;
        AABB searchBox = steppedBox.expandTowards(movement.x, -(double) maxUpStep, movement.z);
        if (!pressingIntoCeiling) {
            searchBox = searchBox.expandTowards(0.0, 1.0E-5D, 0.0);
        }

        List<VoxelShape> stepShapes = new ArrayList<>(entityShapes);
        entity.level().getBlockCollisions(entity, searchBox).forEach(stepShapes::add);

        float currentStepY = (float) collided.y;
        float[] candidateHeights = antarchy$collectCandidateStepDownHeights(steppedBox, stepShapes, maxUpStep, currentStepY);
        for (float candidate : candidateHeights) {
            Vec3 stepped = Entity.collideBoundingBox(entity, new Vec3(movement.x, -(double) candidate, movement.z), steppedBox, entity.level(), stepShapes);
            if (stepped.horizontalDistanceSqr() > collided.horizontalDistanceSqr()) {
                double offset = steppedBox.maxY - aabb.maxY;
                cir.setReturnValue(stepped.add(0.0, offset, 0.0));
                return;
            }
        }

        cir.setReturnValue(collided);
    }

    @Unique
    private static float[] antarchy$collectCandidateStepDownHeights(AABB box, List<VoxelShape> shapes, float maxStep, float currentY) {
        TreeSet<Float> heights = new TreeSet<>();
        for (VoxelShape shape : shapes) {
            for (double coord : shape.getCoords(Direction.Axis.Y)) {
                float height = (float) (box.maxY - coord);
                if (height >= 0.0F && height != currentY && height <= maxStep) {
                    heights.add(height);
                }
            }
        }

        float[] result = new float[heights.size()];
        int i = 0;
        for (float height : heights) {
            result[i++] = height;
        }
        return result;
    }

    @Unique
    private boolean antarchy$intersectsAntiwater(Entity entity, AABB box) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = entity.level().getFluidState(cursor);
                    if (AntarchyFluidChecks.isAntiwater(fluidState)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
