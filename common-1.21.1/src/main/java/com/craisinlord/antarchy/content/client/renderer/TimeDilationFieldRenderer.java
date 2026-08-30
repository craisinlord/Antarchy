package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.time.TimeDilationFieldEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TimeDilationFieldRenderer extends EntityRenderer<TimeDilationFieldEntity> {
    public TimeDilationFieldRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(TimeDilationFieldEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(TimeDilationFieldEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/unknown_pack.png");
    }
}
