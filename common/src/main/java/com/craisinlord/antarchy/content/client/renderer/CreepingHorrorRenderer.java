package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.CreepingHorrorModel;
import com.craisinlord.antarchy.content.entity.CreepingHorrorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CreepingHorrorRenderer extends GeoEntityRenderer<CreepingHorrorEntity> {
    public CreepingHorrorRenderer(EntityRendererProvider.Context context) {
        super(context, new CreepingHorrorModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public void preRender(PoseStack poseStack, CreepingHorrorEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (animatable.isClimbing()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.translate(0, -0.5, 0.5);
        }
    }
}
