package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.BasiliskModel;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BasiliskRenderer extends GeoEntityRenderer<BasiliskEntity> {

    public BasiliskRenderer(EntityRendererProvider.Context context) {
        super(context, new BasiliskModel());
        this.shadowRadius = 1.0F;
    }

    @Override
    public void preRender(PoseStack poseStack, BasiliskEntity animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (animatable.isDeadOrDying()) {
            poseStack.translate(0.0D, -0.35D, 0.0D);
        }
    }

    @Override
    protected float getDeathMaxRotation(BasiliskEntity animatable) {
        return 0.0F;
    }
}
