package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class ParalyzedStoneGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private static final ResourceLocation STONE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");
    private static final int STONE_TINT = 0xFFB1ADA4;

    public ParalyzedStoneGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                       MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (!(animatable instanceof Entity entity) || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        if (!livingEntity.hasEffect(AntarchyObjects.PARALYZED_EFFECT.get())) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.02f, 1.02f, 1.02f);
        RenderType stoneRenderType = RenderType.entityCutoutNoCull(STONE_TEXTURE);
        VertexConsumer stoneBuffer = bufferSource.getBuffer(stoneRenderType);
        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                stoneRenderType,
                stoneBuffer,
                partialTick,
                packedLight,
                packedOverlay,
                STONE_TINT
        );
        poseStack.popPose();
    }
}
