package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
                    if (PotentNyxiteBlock.isAntiwater(fluidState)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
