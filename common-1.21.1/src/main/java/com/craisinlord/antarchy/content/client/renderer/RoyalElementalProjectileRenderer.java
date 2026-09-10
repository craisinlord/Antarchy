package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalElementalProjectileModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalElementalProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoyalElementalProjectileRenderer extends GeoEntityRenderer<RoyalElementalProjectileEntity> {
    public RoyalElementalProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalElementalProjectileModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderType getRenderType(RoyalElementalProjectileEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return animatable.isIceball()
                ? RenderType.entityCutoutNoCull(texture)
                : RenderType.entityTranslucentEmissive(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, RoyalElementalProjectileEntity animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, colour);
        float scale = animatable.isIceball() ? 0.75F : 0.72F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    protected void applyRotations(RoyalElementalProjectileEntity entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick, float nativeScale) {
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        if (!entity.isIceball()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }
}
