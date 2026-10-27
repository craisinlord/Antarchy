package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.util.AntarchyLivingRenderStateAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HoverboardRiderModelMixin {
    private static final float HOVERBOARD_RIDER_RENDER_YAW = -90.0F;
    private static final float HOVERBOARD_RIDER_TORSO_ROLL = -15.0F;

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    @Final
    public ModelPart body;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void antarchy$faceHoverboardRiderHeadForward(HumanoidRenderState state, CallbackInfo ci) {
        if (((AntarchyLivingRenderStateAccess) state).antarchy$isOnHoverboard()) {
            this.head.yRot += (float) Math.toRadians(HOVERBOARD_RIDER_RENDER_YAW);
            this.body.zRot += (float) Math.toRadians(HOVERBOARD_RIDER_TORSO_ROLL);
        }
    }
}
