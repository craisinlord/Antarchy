package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO FIX ON FABRIC SIDE
@Mixin(GameRenderer.class)
public abstract class NightmareCameraShakeMixin {
//    private static final double MAX_SHAKE_RANGE = 32.0D;
//
//    @Shadow @Final private Minecraft minecraft;
//
//    @Redirect(
//            method = "renderLevel",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
//    )
//    private void applyNightmareCameraShake(GameRenderer instance, PoseStack poseStack, float partialTick) {
//        instance.bobView(poseStack, partialTick);
//        if (minecraft.level == null || minecraft.player == null) {
//            return;
//        }
//
//        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
//        float shakeStrength = 0.0F;
//
//        for (NightmareEntity nightmare : minecraft.level.getEntitiesOfClass(
//                NightmareEntity.class,
//                minecraft.player.getBoundingBox().inflate(MAX_SHAKE_RANGE),
//                e -> e.isAlive() && e.isRoaring()
//        )) {
//            double distance = Math.sqrt(cameraPos.distanceToSqr(nightmare.position().add(0.0D, nightmare.getBbHeight() * 0.5D, 0.0D)));
//            if (distance > MAX_SHAKE_RANGE) {
//                continue;
//            }
//            shakeStrength += (float) ((1.0D - distance / MAX_SHAKE_RANGE) * 1.35D);
//        }
//
//        if (shakeStrength <= 0.0F) {
//            return;
//        }
//
//        float time = (float) (minecraft.player.tickCount + partialTick);
//        float yawOffset = Mth.sin(time * 1.85F) * shakeStrength * 1.8F;
//        float pitchOffset = Mth.cos(time * 2.15F) * shakeStrength * 1.45F;
//        float rollOffset = Mth.sin(time * 2.65F) * shakeStrength * 1.05F;
//
//        poseStack.mulPose(Axis.YP.rotationDegrees(yawOffset));
//        poseStack.mulPose(Axis.XP.rotationDegrees(pitchOffset));
//        poseStack.mulPose(Axis.ZP.rotationDegrees(rollOffset));
//    }
}
