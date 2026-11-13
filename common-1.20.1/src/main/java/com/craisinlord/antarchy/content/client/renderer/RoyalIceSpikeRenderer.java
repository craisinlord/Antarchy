package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalIceSpikeModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoyalIceSpikeRenderer extends GeoEntityRenderer<RoyalIceSpikeEntity> {
    public RoyalIceSpikeRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalIceSpikeModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderType getRenderType(RoyalIceSpikeEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return GlimmerEchoRendering.renderType(animatable, texture,
                super.getRenderType(animatable, texture, bufferSource, partialTick));
    }

    @Override
    public Color getRenderColor(RoyalIceSpikeEntity animatable, float partialTick, int packedLight) {
        return GlimmerEchoRendering.renderColor(animatable, super.getRenderColor(animatable, partialTick, packedLight));
    }
}
