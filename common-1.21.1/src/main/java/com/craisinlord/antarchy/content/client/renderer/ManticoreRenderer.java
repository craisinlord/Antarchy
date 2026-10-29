package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ManticoreModel;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class ManticoreRenderer extends GeoEntityRenderer<ManticoreEntity> {
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("manticore");

    public ManticoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ManticoreModel());
        this.shadowRadius = 0.9F;
        this.addRenderLayer(new GlimmerEchoRendering.EmissiveLayer<>(this, GlimmerEchoRendering.emissiveTexture("manticore")));
    }

    @Override
    public ResourceLocation getTextureLocation(ManticoreEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, super.getTextureLocation(animatable));
    }

    @Override
    public RenderType getRenderType(ManticoreEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return GlimmerEchoRendering.renderType(animatable, texture, super.getRenderType(animatable, texture, bufferSource, partialTick));
    }

    @Override
    public Color getRenderColor(ManticoreEntity animatable, float partialTick, int packedLight) {
        return GlimmerEchoRendering.renderColor(animatable, super.getRenderColor(animatable, partialTick, packedLight));
    }
}
