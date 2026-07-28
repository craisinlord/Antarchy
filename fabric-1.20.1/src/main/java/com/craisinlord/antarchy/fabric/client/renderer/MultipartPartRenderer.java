package com.craisinlord.antarchy.fabric.client.renderer;

import com.craisinlord.antarchy.fabric.entity.multipart.MultipartPartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MultipartPartRenderer extends EntityRenderer<MultipartPartEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/misc/white.png");

    public MultipartPartRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MultipartPartEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(MultipartPartEntity entity) {
        return TEXTURE;
    }
}
