package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.math.Axis;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class HoverboardRiderPoseMixin<T extends LivingEntity, M extends EntityModel<T>> {
    private static final float HOVERBOARD_RIDER_RENDER_YAW = -90.0F;

    @Shadow
    public abstract M getModel();

    @Inject(
            method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            at = @At("TAIL")
    )
    private void antarchy$rotateHoverboardRider(T entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float scale, CallbackInfo ci) {
        if (entity.getVehicle() instanceof HoverboardEntity) {
            poseStack.mulPose(Axis.YP.rotationDegrees(HOVERBOARD_RIDER_RENDER_YAW));
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/model/EntityModel;riding:Z",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void antarchy$standOnHoverboard(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (entity.getVehicle() instanceof HoverboardEntity) {
            this.getModel().riding = false;
        }
    }
}
