package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class AntiwaterLivingEntityMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void antarchy$moveInAntiwater(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!antarchy$isTouchingAntiwaterLocal(entity)) {
            return;
        }

        double prevY = entity.getY();
        float swimSpeed = entity.isSprinting() ? 0.026F : 0.02F;
        entity.moveRelative(swimSpeed, travelVector);
        entity.move(MoverType.SELF, entity.getDeltaMovement());

        Vec3 motion = entity.getDeltaMovement();
        if (entity.horizontalCollision && entity.onClimbable()) {
            motion = new Vec3(motion.x, 0.2D, motion.z);
        }

        AntarchyGravityDirection gravityDirection = antarchy$getEffectiveGravityDirection(entity);
        Vec3 worldMotion = gravityDirection.isInverted()
                ? AntarchyGravityRotationUtil.vecPlayerToWorld(motion, gravityDirection)
                : motion;

        double drag = entity.isSwimming() ? 0.9D : 0.8D;
        worldMotion = worldMotion.scale(drag);
        boolean flying = entity instanceof Player player && player.getAbilities().flying;
        if (!flying) {
            worldMotion = worldMotion.add(0.0D, 0.005D, 0.0D);
        }

        entity.setDeltaMovement(
                gravityDirection.isInverted()
                        ? AntarchyGravityRotationUtil.vecWorldToPlayer(worldMotion, gravityDirection)
                        : worldMotion
        );

        if (entity.horizontalCollision && entity.isFree(
                worldMotion.x,
                worldMotion.y + 0.6D - entity.getY() + prevY,
                worldMotion.z
        )) {
            Vec3 boostedWorldMotion = new Vec3(worldMotion.x, 0.3D, worldMotion.z);
            entity.setDeltaMovement(
                    gravityDirection.isInverted()
                            ? AntarchyGravityRotationUtil.vecWorldToPlayer(boostedWorldMotion, gravityDirection)
                            : boostedWorldMotion
            );
        }

        ci.cancel();
    }

    private AntarchyGravityDirection antarchy$getEffectiveGravityDirection(LivingEntity entity) {
        AntarchyGravityDirection gravityDirection = AntarchyGravityApi.getGravityDirection(entity);
        if (gravityDirection.isInverted() || !entity.hasEffect(AntarchyObjects.INVERTED_EFFECT.get())) {
            return gravityDirection;
        }

        return AntarchyGravityDirection.UP;
    }

    private boolean antarchy$isTouchingAntiwaterLocal(LivingEntity entity) {
        AABB box = entity.getBoundingBox().inflate(0.05D);
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
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
