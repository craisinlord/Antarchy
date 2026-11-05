package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
import com.craisinlord.antarchy.fabric.util.CustomFluidPhysicsChecks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntity.class, priority = 900)
public abstract class WaterlikeFluidLivingEntityMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void antarchy$moveInCustomFluid(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        int touching = CustomFluidPhysicsChecks.classifyTouchingFluid(entity);
        if (touching == CustomFluidPhysicsChecks.TOUCHING_NONE) {
            return;
        }

        if (touching == CustomFluidPhysicsChecks.TOUCHING_ANTIWATER) {
            antarchy$moveInAntiwater(entity, travelVector);
        } else {
            antarchy$moveInWaterlikeFluid(entity, travelVector);
        }
        ci.cancel();
    }

    private static void antarchy$moveInAntiwater(LivingEntity entity, Vec3 travelVector) {
        double prevY = entity.getY();
        float swimSpeed = entity.isSprinting() ? 0.026F : 0.02F;
        entity.moveRelative(swimSpeed, travelVector);
        entity.move(MoverType.SELF, entity.getDeltaMovement());

        Vec3 motion = entity.getDeltaMovement();
        double verticalInput = travelVector.y;
        if (verticalInput == 0.0D && entity instanceof Player player) {
            verticalInput = player.isShiftKeyDown() ? -1.0D : ((LivingEntityJumpingAccessor) entity).antarchy$isJumping() ? 1.0D : 0.0D;
        }

        if (entity.horizontalCollision && entity.onClimbable()) {
            motion = new Vec3(motion.x, 0.2D, motion.z);
        }

        GravityDirection gravityDirection = antarchy$getEffectiveGravityDirection(entity);
        Vec3 worldMotion = gravityDirection.isInverted()
                ? GravityRotationUtil.vecPlayerToWorld(motion, gravityDirection)
                : motion;

        double drag = entity.isSwimming() ? 0.9D : 0.8D;
        worldMotion = worldMotion.scale(drag);
        Vec3 antiwaterFlow = CustomFluidPhysicsChecks.getAntiwaterFlow(entity);
        if (antiwaterFlow.lengthSqr() > 1.0E-6D) {
            worldMotion = worldMotion.add(antiwaterFlow.normalize().scale(0.02D));
        }
        boolean flying = entity instanceof Player player && player.getAbilities().flying;
        if (!flying) {
            worldMotion = worldMotion.add(0.0D, 0.005D + verticalInput * 0.04D, 0.0D);
        }

        entity.setDeltaMovement(
                gravityDirection.isInverted()
                        ? GravityRotationUtil.vecWorldToPlayer(worldMotion, gravityDirection)
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
                            ? GravityRotationUtil.vecWorldToPlayer(boostedWorldMotion, gravityDirection)
                            : boostedWorldMotion
            );
        }
    }

    private static GravityDirection antarchy$getEffectiveGravityDirection(LivingEntity entity) {
        GravityDirection gravityDirection = GravityApi.getGravityDirection(entity);
        if (gravityDirection.isInverted() || !entity.hasEffect(AntarchyObjects.INVERTED_EFFECT.get())) {
            return gravityDirection;
        }

        return GravityDirection.UP;
    }

    private static void antarchy$moveInWaterlikeFluid(LivingEntity entity, Vec3 travelVector) {
        double prevY = entity.getY();
        float swimSpeed = entity.isSprinting() ? 0.024F : 0.02F;
        entity.moveRelative(swimSpeed, travelVector);
        entity.move(MoverType.SELF, entity.getDeltaMovement());

        Vec3 motion = entity.getDeltaMovement();
        double verticalInput = travelVector.y;
        if (verticalInput == 0.0D && entity instanceof Player player) {
            verticalInput = player.isShiftKeyDown() ? -1.0D : ((LivingEntityJumpingAccessor) entity).antarchy$isJumping() ? 1.0D : 0.0D;
        }

        if (entity.horizontalCollision && entity.onClimbable()) {
            motion = new Vec3(motion.x, 0.2D, motion.z);
        }

        boolean flying = entity instanceof Player player && player.getAbilities().flying;
        double verticalDrift = verticalInput * 0.04D;
        if (!flying) {
            verticalDrift -= 0.004D;
        }

        Vec3 nextMotion = new Vec3(
                motion.x * 0.8D,
                motion.y * 0.8D + verticalDrift,
                motion.z * 0.8D
        );
        Vec3 fluidFlow = CustomFluidPhysicsChecks.getWaterlikeFlow(entity);
        if (fluidFlow.lengthSqr() > 1.0E-6D) {
            nextMotion = nextMotion.add(fluidFlow.normalize().scale(0.014D));
        }
        entity.setDeltaMovement(nextMotion);

        if (entity.horizontalCollision && entity.isFree(
                nextMotion.x,
                nextMotion.y + 0.6D - entity.getY() + prevY,
                nextMotion.z
        )) {
            entity.setDeltaMovement(nextMotion.x, 0.3D, nextMotion.z);
        }
    }
}
