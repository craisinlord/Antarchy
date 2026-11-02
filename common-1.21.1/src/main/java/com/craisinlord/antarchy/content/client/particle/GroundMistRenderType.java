package com.craisinlord.antarchy.content.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public final class GroundMistRenderType implements ParticleRenderType {
    public static final GroundMistRenderType INSTANCE = new GroundMistRenderType();

    private GroundMistRenderType() {
    }

    @Override
    public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
        RenderSystem.depthMask(false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public String toString() {
        return "ANTARCHY_GROUND_MIST";
    }
}
