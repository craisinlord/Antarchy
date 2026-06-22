package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
/*
 * Rotates entity frame math into local gravity space.
 */
public abstract class EntityGravityFrameMixin {

    @Shadow protected abstract void onBelowWorld();

    @Inject(method = "makeBoundingBox", at = @At("RETURN"), cancellable = true)
    private void antarchy$rotateBoundingBox(CallbackInfoReturnable<AABB> cir) {
        Entity entity = (Entity) (Object) this;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return;
        }
        Vec3 origin = entity.position();
        AABB localBox = cir.getReturnValue().move(origin.scale(-1.0D));
        localBox = localBox.move(0.0D, -1.0E-6D, 0.0D);
        cir.setReturnValue(AntarchyGravityRotationUtil.boxPlayerToWorld(localBox, direction).move(origin));
    }

    @Inject(method = "getEyeY", at = @At("HEAD"), cancellable = true)
    private void antarchy$fixEyeY(CallbackInfoReturnable<Double> cir) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }
        cir.setReturnValue(entity.position().add(AntarchyGravityRotationUtil.getEyeOffset(entity, entity.getEyeHeight())).y);
    }

    @Inject(method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void antarchy$fixEyePosition(CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }
        cir.setReturnValue(entity.position().add(AntarchyGravityRotationUtil.getEyeOffset(entity, entity.getEyeHeight())));
    }

    @Inject(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void antarchy$fixEyePositionInterpolated(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        float flipProgress = AntarchyGravityApi.getGravityFlipProgress(entity, partialTick);
        if (flipProgress <= 0.0F) {
            return;
        }
        cir.setReturnValue(entity.getPosition(partialTick).add(
                AntarchyGravityRotationUtil.getEyeOffset(entity, entity.getEyeHeight(), partialTick)
        ));
    }

    @Inject(method = "getViewVector", at = @At("RETURN"), cancellable = true)
    private void antarchy$rotateViewVector(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return;
        }
        cir.setReturnValue(AntarchyGravityRotationUtil.vecPlayerToWorld(cir.getReturnValue(), direction));
    }

    @Inject(method = "getUpVector", at = @At("RETURN"), cancellable = true)
    private void antarchy$rotateUpVector(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return;
        }
        cir.setReturnValue(AntarchyGravityRotationUtil.vecPlayerToWorld(cir.getReturnValue(), direction));
    }

    @Inject(method = "getForward", at = @At("RETURN"), cancellable = true)
    private void antarchy$rotateForwardVector(CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return;
        }
        cir.setReturnValue(AntarchyGravityRotationUtil.vecPlayerToWorld(cir.getReturnValue(), direction));
    }

    @Inject(method = "getOnPos(F)Lnet/minecraft/core/BlockPos;", at = @At("HEAD"), cancellable = true)
    private void antarchy$fixSupportingBlockPos(float offset, CallbackInfoReturnable<BlockPos> cir) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }
        cir.setReturnValue(AntarchyGravityRotationUtil.getSupportingBlockPos(entity, offset));
    }

    @Inject(method = "getBlockPosBelowThatAffectsMyMovement",
            at = @At("HEAD"), cancellable = true)
    private void antarchy$fixVelocityAffectingPos(CallbackInfoReturnable<BlockPos> cir) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }
        cir.setReturnValue(BlockPos.containing(entity.position().add(0.0D, 0.5000001D, 0.0D)));
    }

    @Inject(method = "getOnPosLegacy", at = @At("HEAD"), cancellable = true)
    private void antarchy$fixLandingPos(CallbackInfoReturnable<BlockPos> cir) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }
        cir.setReturnValue(BlockPos.containing(entity.position().add(0.0D, 0.20000000298023224D, 0.0D)));
    }

    @WrapOperation(method = "isInWall",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;ofSize(Lnet/minecraft/world/phys/Vec3;DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0))
    private AABB antarchy$fixIsInWallAABB(Vec3 center, double x, double y, double z, Operation<AABB> original) {
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection((Entity) (Object) this);
        if (!direction.isInverted()) {
            return original.call(center, x, y, z);
        }
        Vec3 rotated = AntarchyGravityRotationUtil.vecPlayerToWorld(new Vec3(x, y, z), direction);
        return original.call(center, rotated.x, rotated.y, rotated.z);
    }

    @WrapOperation(method = "isFree(DDD)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0))
    private AABB antarchy$fixIsFreeMoveAABB(AABB instance, double x, double y, double z, Operation<AABB> original) {
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection((Entity) (Object) this);
        if (!direction.isInverted()) {
            return original.call(instance, x, y, z);
        }
        Vec3 worldOffset = AntarchyGravityRotationUtil.vecPlayerToWorld(new Vec3(x, y, z), direction);
        return original.call(instance, worldOffset.x, worldOffset.y, worldOffset.z);
    }

    @ModifyVariable(method = "updateFluidOnEyes", at = @At(value = "STORE"), ordinal = 0)
    private double antarchy$fixFluidEyeY(double eyeY) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return eyeY;
        }
        return entity.getEyePosition().y();
    }

    @ModifyVariable(method = "updateFluidOnEyes", at = @At(value = "STORE"), ordinal = 0)
    private BlockPos antarchy$fixFluidEyeBlockPos(BlockPos pos) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return pos;
        }
        return BlockPos.containing(entity.getEyePosition());
    }

    @Inject(method = "spawnSprintParticle", at = @At("HEAD"), cancellable = true)
    private void antarchy$fixSprintParticle(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return;
        }
        ci.cancel();
        Vec3 ceilPos = entity.position().add(0.0D, 0.20000000298023224D, 0.0D);
        BlockPos blockPos = BlockPos.containing(ceilPos);
        var blockState = entity.level().getBlockState(blockPos);
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            double w = entity.getBbWidth();
            Vec3 particlePos = entity.position().add(
                    (entity.getRandom().nextDouble() - 0.5D) * w,
                     0.1D,
                    (entity.getRandom().nextDouble() - 0.5D) * w
            );
            Vec3 dm = entity.getDeltaMovement();
            Vec3 particleVel = AntarchyGravityRotationUtil.vecPlayerToWorld(
                    new Vec3(dm.x * -4.0D, -1.5D, dm.z * -4.0D),
                    direction
            );
            entity.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                    particlePos.x, particlePos.y, particlePos.z,
                    particleVel.x, particleVel.y, particleVel.z
            );
        }
    }

    @Inject(method = "checkBelowWorld", at = @At("HEAD"), cancellable = true)
    private void antarchy$checkAboveWorldForInverted(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }
        if (entity.getY() > entity.level().getMaxBuildHeight() + 256) {
            this.onBelowWorld();
            ci.cancel();
        }
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"), cancellable = true)
    private void antarchy$fixEntityPush(Entity other, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        boolean selfInverted = AntarchyGravityApi.isGravityInverted(self);
        boolean otherInverted = AntarchyGravityApi.isGravityInverted(other);
        if (selfInverted == otherInverted) {
            return;
        }
        ci.cancel();
        if (self.isPassengerOfSameVehicle(other)) {
            return;
        }
        if (other.noPhysics || self.noPhysics) {
            return;
        }
        Vec3 worldOffset = other.getBoundingBox().getCenter().subtract(self.getBoundingBox().getCenter());
        AntarchyGravityDirection selfDir = AntarchyGravityApi.getGravityDirection(self);
        Vec3 localOffset = AntarchyGravityRotationUtil.vecWorldToPlayer(worldOffset, selfDir);
        double dx = localOffset.x;
        double dz = localOffset.z;
        double dist = Mth.absMax(dx, dz);
        if (dist >= 0.009999999776482582D) {
            dist = Math.sqrt(dist);
            dx /= dist;
            dz /= dist;
            double scale = Math.min(1.0D / dist, 1.0D) * 0.05D;
            dx *= scale;
            dz *= scale;
            if (!self.isVehicle()) {
                self.push(AntarchyGravityRotationUtil.vecPlayerToWorld(new Vec3(-dx, 0.0D, -dz), selfDir));
            }
            if (!other.isVehicle()) {
                AntarchyGravityDirection otherDir = AntarchyGravityApi.getGravityDirection(other);
                other.push(AntarchyGravityRotationUtil.vecPlayerToWorld(new Vec3(dx, 0.0D, dz), otherDir));
            }
        }
    }
}
