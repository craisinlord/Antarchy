package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ToreterrorModel;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class ToreterrorRenderer extends GeoEntityRenderer<ToreterrorEntity> {
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("toreterror");

    public ToreterrorRenderer(EntityRendererProvider.Context context) {
        super(context, new ToreterrorModel());
        this.shadowRadius = 1.6F;
        this.addRenderLayer(new GlimmerEchoRendering.EmissiveLayer<>(this, GlimmerEchoRendering.emissiveTexture("toreterror")));
    }

    @Override
    protected float getDeathMaxRotation(ToreterrorEntity animatable) {
        return 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(ToreterrorEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, super.getTextureLocation(animatable));
    }

    @Override
    public RenderType getRenderType(ToreterrorEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return GlimmerEchoRendering.renderType(animatable, texture, super.getRenderType(animatable, texture, bufferSource, partialTick));
    }

    @Override
    public Color getRenderColor(ToreterrorEntity animatable, float partialTick, int packedLight) {
        return GlimmerEchoRendering.renderColor(animatable, super.getRenderColor(animatable, partialTick, packedLight));
    }
}
