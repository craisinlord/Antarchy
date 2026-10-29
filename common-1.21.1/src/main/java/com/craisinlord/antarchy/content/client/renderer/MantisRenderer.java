package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.MantisModel;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class MantisRenderer extends GeoEntityRenderer<MantisEntity> {
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("mantis");

    public MantisRenderer(EntityRendererProvider.Context context) {
        super(context, new MantisModel());
        this.shadowRadius = 1.0F;
        this.withScale(1.25F);
        this.addRenderLayer(new GlimmerEchoRendering.EmissiveLayer<>(this, GlimmerEchoRendering.emissiveTexture("mantis")));
    }

    @Override
    public ResourceLocation getTextureLocation(MantisEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, super.getTextureLocation(animatable));
    }

    @Override
    public RenderType getRenderType(MantisEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return GlimmerEchoRendering.renderType(animatable, texture, super.getRenderType(animatable, texture, bufferSource, partialTick));
    }

    @Override
    public Color getRenderColor(MantisEntity animatable, float partialTick, int packedLight) {
        return GlimmerEchoRendering.renderColor(animatable, super.getRenderColor(animatable, partialTick, packedLight));
    }
}
