package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.FlyingSquirrelModel;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FlyingSquirrelRenderer extends GeoEntityRenderer<FlyingSquirrelEntity> {
    private static final float ADULT_SHADOW_RADIUS = 0.3F;
    private static final float BABY_SCALE = 0.6F;

    public FlyingSquirrelRenderer(EntityRendererProvider.Context context) {
        super(context, new FlyingSquirrelModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
    }

    @Override
    public void preRender(PoseStack poseStack, FlyingSquirrelEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        if (animatable.isBaby()) {
            poseStack.scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
            this.shadowRadius = ADULT_SHADOW_RADIUS * BABY_SCALE;
        } else {
            this.shadowRadius = ADULT_SHADOW_RADIUS;
        }
    }

    @Override
    protected void applyRotations(FlyingSquirrelEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        float appliedYaw = rotationYaw;
        float appliedPitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());

        if (animatable.isOnShoulder() && animatable.getVehicle() instanceof Player player) {
            appliedYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
            appliedPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        }

        super.applyRotations(animatable, poseStack, ageInTicks, appliedYaw, partialTick, nativeScale);

        if (animatable.isOnShoulder()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-appliedPitch));
        }
    }
}
