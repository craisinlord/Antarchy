package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.Color;

public final class GlimmerEchoRendering {
    public static final float GLIMMER_RED = 0.62F;
    public static final float GLIMMER_GREEN = 0.88F;
    public static final float GLIMMER_BLUE = 1.0F;
    public static final int GLIMMER_RGB = 0x9EE0FF;
    private static final float GLIMMER_ALPHA = 0.78F;
    private static final Color GLIMMER_COLOR = Color.ofRGBA(1.0F, 1.0F, 1.0F, GLIMMER_ALPHA);
    private static final int GLIMMER_EMISSIVE_COLOR = ((int) (GLIMMER_ALPHA * 255.0F) << 24) | 0xFFFFFF;

    private GlimmerEchoRendering() {
    }

    public static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/" + name + "_glimmer.png");
    }

    public static ResourceLocation emissiveTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/" + name + "_glimmer_emissive.png");
    }

    public static ResourceLocation textureLocation(Entity entity, ResourceLocation glimmer, ResourceLocation normal) {
        return GlimmerEchoes.isGlimmerEcho(entity) ? glimmer : normal;
    }

    public static RenderType renderType(Entity entity, ResourceLocation texture, RenderType normal) {
        return GlimmerEchoes.isGlimmerEcho(entity) ? RenderType.entityTranslucent(texture) : normal;
    }

    public static Color renderColor(Entity entity, Color normal) {
        return GlimmerEchoes.isGlimmerEcho(entity) ? GLIMMER_COLOR : normal;
    }

    public static final class EmissiveLayer<T extends Entity & GeoAnimatable> extends GeoRenderLayer<T> {
        private final ResourceLocation emissiveTexture;

        public EmissiveLayer(GeoRenderer<T> renderer, ResourceLocation emissiveTexture) {
            super(renderer);
            this.emissiveTexture = emissiveTexture;
        }

        @Override
        public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            if (!GlimmerEchoes.isGlimmerEcho(animatable)) {
                return;
            }
            RenderType emissiveType = RenderType.eyes(this.emissiveTexture);
            this.getRenderer().reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    emissiveType,
                    bufferSource.getBuffer(emissiveType),
                    partialTick,
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    GLIMMER_EMISSIVE_COLOR
            );
        }
    }
}
