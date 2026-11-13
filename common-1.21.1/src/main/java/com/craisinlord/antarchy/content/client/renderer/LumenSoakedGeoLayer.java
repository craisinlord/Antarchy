package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.effect.LumenSoakedHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class LumenSoakedGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    public LumenSoakedGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                       MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (!(animatable instanceof Entity entity) || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        if (livingEntity.isInvisible() || !LumenSoakedHooks.isSoaked(livingEntity)) {
            return;
        }

        RenderType spotsRenderType = RenderType.entityTranslucentEmissive(LumenSoakedLivingLayer.SPOTS_TEXTURE);
        VertexConsumer spotsBuffer = bufferSource.getBuffer(spotsRenderType);
        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                spotsRenderType,
                spotsBuffer,
                partialTick,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                0xFFFFFFFF
        );
    }
}
