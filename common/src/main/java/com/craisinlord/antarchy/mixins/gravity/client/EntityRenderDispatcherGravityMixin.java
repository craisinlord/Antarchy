package com.craisinlord.antarchy.mixins.gravity.client;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
/*
 * Rotates entity rendering for inverted gravity.
 */
public abstract class EntityRenderDispatcherGravityMixin {

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", ordinal = 0, shift = At.Shift.AFTER)
    )
    private <E extends Entity> void antarchy$rotateEntityRenderPose(
            E entity,
            double x,
            double y,
            double z,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        float flipProgress = AntarchyGravityApi.getGravityFlipProgress(entity, partialTick);
        if (!AntarchyGravityApi.isGravityInverted(entity) && flipProgress <= 0.0F) {
            return;
        }

        poseStack.mulPose(AntarchyGravityRotationUtil.getCameraRotationQuaternion(entity, partialTick));
    }

    @ModifyVariable(
            method = "renderHitbox",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;", ordinal = 0),
            ordinal = 0
    )
    private static AABB antarchy$rotateRenderedHitbox(
            AABB box,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            Entity entity,
            float red,
            float green,
            float blue,
            float partialTick
    ) {
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return box;
        }

        return AntarchyGravityRotationUtil.boxWorldToPlayer(
                box,
                AntarchyGravityApi.getGravityDirection(entity)
        );
    }

    @WrapOperation(
            method = "renderHitbox",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;", ordinal = 0)
    )
    private static Vec3 antarchy$rotateRenderedHitboxViewVector(Entity entity, float partialTick, Operation<Vec3> original) {
        Vec3 viewVector = original.call(entity, partialTick);
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return viewVector;
        }

        return AntarchyGravityRotationUtil.vecWorldToPlayer(
                viewVector,
                AntarchyGravityApi.getGravityDirection(entity)
        );
    }
}
