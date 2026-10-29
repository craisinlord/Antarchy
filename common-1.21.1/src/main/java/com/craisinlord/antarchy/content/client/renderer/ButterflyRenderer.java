package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ButterflyModel;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class ButterflyRenderer extends GeoEntityRenderer<ButterflyEntity> {
    private static final float VISUAL_SCALE = 0.8F;
    private static final float SHADOW_RADIUS = 0.18F;
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("butterfly");

    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.withScale(VISUAL_SCALE);
        this.addRenderLayer(new GlimmerEchoRendering.EmissiveLayer<>(this, GlimmerEchoRendering.emissiveTexture("butterfly")));
    }

    @Override
    public ResourceLocation getTextureLocation(ButterflyEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, super.getTextureLocation(animatable));
    }

    @Override
    public RenderType getRenderType(ButterflyEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return GlimmerEchoRendering.renderType(animatable, texture, super.getRenderType(animatable, texture, bufferSource, partialTick));
    }

    @Override
    public Color getRenderColor(ButterflyEntity animatable, float partialTick, int packedLight) {
        return GlimmerEchoRendering.renderColor(animatable, super.getRenderColor(animatable, partialTick, packedLight));
    }

    @Override
    public void preRender(PoseStack poseStack, ButterflyEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        this.shadowRadius = SHADOW_RADIUS;
    }
}
