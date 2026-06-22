package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.WaspModel;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WaspRenderer extends GeoEntityRenderer<WaspEntity> {
    public WaspRenderer(EntityRendererProvider.Context context) {
        super(context, new WaspModel());
        this.shadowRadius = 0.55F;
    }

    @Override
    public void preRender(PoseStack poseStack, WaspEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        poseStack.translate(0.0D, -0.5D, 0.0D);
    }
}
