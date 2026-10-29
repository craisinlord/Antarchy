package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.EmperorScorpionModel;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.Color;

public class EmperorScorpionRenderer extends GeoEntityRenderer<EmperorScorpionEntity> {
    private static final int CROSSFADE_TICKS = 12;
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("emperor_scorpion");
    private static final ResourceLocation GLIMMER_HARDEN_TEXTURE = GlimmerEchoRendering.texture("emperor_scorpion_harden");
    private final Map<Integer, HardenTransitionState> transitions = new HashMap<>();

    public EmperorScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new EmperorScorpionModel());
        this.shadowRadius = 3.0F;
        this.addRenderLayer(new EmperorScorpionHardenLayer(this));
        this.addRenderLayer(new EmperorScorpionEmissiveLayer(this));
        this.addRenderLayer(new GlimmerEchoRendering.EmissiveLayer<>(this, GlimmerEchoRendering.emissiveTexture("emperor_scorpion")));
    }

    @Override
    public ResourceLocation getTextureLocation(EmperorScorpionEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, super.getTextureLocation(animatable));
    }

    @Override
    public Color getRenderColor(EmperorScorpionEntity animatable, float partialTick, int packedLight) {
        return GlimmerEchoRendering.renderColor(animatable, super.getRenderColor(animatable, partialTick, packedLight));
    }

    @Override
    public void render(EmperorScorpionEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.updateTransition(entity);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public RenderType getRenderType(EmperorScorpionEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return GlimmerEchoRendering.renderType(animatable, texture, RenderType.entityCutoutNoCull(texture));
    }

    @Override
    protected float getDeathMaxRotation(EmperorScorpionEntity animatable) {
        return 0.0F;
    }

    private static final class EmperorScorpionEmissiveLayer extends GeoRenderLayer<EmperorScorpionEntity> {
        private EmperorScorpionEmissiveLayer(GeoEntityRenderer<EmperorScorpionEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, EmperorScorpionEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            if (GlimmerEchoes.isGlimmerEcho(animatable)) {
                return;
            }
            RenderType emissiveType = RenderType.eyes(EmperorScorpionModel.EMISSIVE_TEXTURE);
            VertexConsumer emissiveBuffer = bufferSource.getBuffer(emissiveType);
            this.getRenderer().reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    emissiveType,
                    emissiveBuffer,
                    partialTick,
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    0xFFFFFFFF
            );
        }
    }

    private void updateTransition(EmperorScorpionEntity entity) {
        int phase = entity.getHardenPhaseIndex();
        HardenTransitionState state = this.transitions.computeIfAbsent(entity.getId(), ignored ->
                new HardenTransitionState(phase, entity.tickCount));

        if (state.currentPhase != phase) {
            state.currentPhase = phase;
            state.transitionStartTick = entity.tickCount;
        } else if (phase == 0
                && entity.tickCount - state.transitionStartTick > CROSSFADE_TICKS) {
            this.transitions.remove(entity.getId());
        }
    }

    private float getHardenAlpha(EmperorScorpionEntity entity, float partialTick) {
        HardenTransitionState state = this.transitions.get(entity.getId());
        if (state == null) {
            return 0.0F;
        }

        float elapsed = (entity.tickCount - state.transitionStartTick) + partialTick;
        return switch (state.currentPhase) {
            case 2 -> Math.max(0.0F, Math.min(1.0F, elapsed / 11.0F));
            case 3 -> 1.0F;
            case 4 -> Math.max(0.0F, Math.min(1.0F, 1.0F - (elapsed / 20.0F)));
            default -> 0.0F;
        };
    }

    private static final class HardenTransitionState {
        private int currentPhase;
        private int transitionStartTick;

        private HardenTransitionState(int currentPhase, int transitionStartTick) {
            this.currentPhase = currentPhase;
            this.transitionStartTick = transitionStartTick;
        }
    }

    private static final class EmperorScorpionHardenLayer extends GeoRenderLayer<EmperorScorpionEntity> {
        private EmperorScorpionHardenLayer(EmperorScorpionRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, EmperorScorpionEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            EmperorScorpionRenderer renderer = (EmperorScorpionRenderer) this.getRenderer();
            float alpha = renderer.getHardenAlpha(animatable, partialTick);
            if (alpha <= 0.0F) {
                return;
            }

            if (GlimmerEchoes.isGlimmerEcho(animatable)) {
                alpha *= GlimmerEchoRendering.renderColor(animatable, Color.WHITE).getAlphaFloat();
            }
            ResourceLocation hardenTexture = GlimmerEchoRendering.textureLocation(animatable, GLIMMER_HARDEN_TEXTURE, EmperorScorpionModel.HARDEN_TEXTURE);
            RenderType hardenType = RenderType.entityTranslucent(hardenTexture);
            VertexConsumer hardenBuffer = bufferSource.getBuffer(hardenType);
            int color = ((Math.max(0, Math.min(255, Math.round(alpha * 255.0F))) & 0xFF) << 24) | 0x00FFFFFF;
            this.getRenderer().reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    hardenType,
                    hardenBuffer,
                    partialTick,
                    packedLight,
                    packedOverlay,
                    color
            );
        }
    }
}
