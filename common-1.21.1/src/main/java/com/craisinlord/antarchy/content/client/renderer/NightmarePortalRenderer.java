package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.NightmarePortalModel;
import com.craisinlord.antarchy.content.entity.nightmare.NightmarePortalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NightmarePortalRenderer extends GeoEntityRenderer<NightmarePortalEntity> {
    public NightmarePortalRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmarePortalModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(NightmarePortalEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        BakedGeoModel model = this.getGeoModel().getBakedModel(this.getGeoModel().getModelResource(entity));
        RenderType renderType = RenderType.eyes(NightmarePortalModel.TEXTURE);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        poseStack.pushPose();
        poseStack.scale(1.0F, 1.0F, 1.0F);
        this.reRender(model, poseStack, bufferSource, entity, renderType, buffer, partialTick, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
    }

    @Override
    public @Nullable RenderType getRenderType(NightmarePortalEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.eyes(NightmarePortalModel.TEXTURE);
    }
}
