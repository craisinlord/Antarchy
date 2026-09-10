package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalBlackHoleModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalAssailantBlackHoleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class RoyalAssailantBlackHoleRenderer extends GeoEntityRenderer<RoyalAssailantBlackHoleEntity> {
    public RoyalAssailantBlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalBlackHoleModel<>());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(RoyalAssailantBlackHoleEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, 0xF000F0);
    }

    @Override
    public @Nullable RenderType getRenderType(RoyalAssailantBlackHoleEntity animatable, ResourceLocation texture,
                                               @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(this.getGeoModel().getTextureResource(animatable));
    }
}
