package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ReverieModel;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ReverieRenderer extends GeoEntityRenderer<ReverieEntity> {
    private static final float VISUAL_SCALE = 0.7F;
    private static final float SHADOW_RADIUS = 0.12F;
    private static final int CROSSFADE_TICKS = 12;
    private final Map<Integer, MoodTransitionState> transitions = new HashMap<>();

    public ReverieRenderer(EntityRendererProvider.Context context) {
        super(context, new ReverieModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.withScale(VISUAL_SCALE);
        this.addRenderLayer(new ReverieCrossfadeLayer(this));
    }

    @Override
    public void render(ReverieEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.updateTransition(entity);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public void preRender(PoseStack poseStack, ReverieEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public RenderType getRenderType(ReverieEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    protected int getBlockLightLevel(ReverieEntity entity, BlockPos pos) {
        return 15;
    }

    @Nullable
    ReverieEntity.Mood getPreviousMood(ReverieEntity entity) {
        MoodTransitionState state = this.transitions.get(entity.getId());
        if (state == null || state.currentMood == state.previousMood) {
            return null;
        }
        return state.previousMood;
    }

    float getPreviousMoodAlpha(ReverieEntity entity, float partialTick) {
        return 1.0F - this.getTransitionProgress(entity, partialTick);
    }

    private void updateTransition(ReverieEntity entity) {
        ReverieEntity.Mood mood = entity.getMood();
        MoodTransitionState state = this.transitions.computeIfAbsent(entity.getId(), ignored ->
                new MoodTransitionState(mood, mood, entity.tickCount));

        if (state.currentMood != mood) {
            state.previousMood = state.currentMood;
            state.currentMood = mood;
            state.transitionStartTick = entity.tickCount;
        } else if (state.currentMood == state.previousMood && entity.tickCount - state.transitionStartTick > CROSSFADE_TICKS) {
            this.transitions.remove(entity.getId());
        }
    }

    private float getTransitionProgress(ReverieEntity entity, float partialTick) {
        MoodTransitionState state = this.transitions.get(entity.getId());
        if (state == null || state.currentMood == state.previousMood) {
            return 1.0F;
        }

        float elapsed = (entity.tickCount - state.transitionStartTick) + partialTick;
        if (elapsed >= CROSSFADE_TICKS) {
            state.previousMood = state.currentMood;
            return 1.0F;
        }

        return Math.max(0.0F, Math.min(1.0F, elapsed / CROSSFADE_TICKS));
    }

    private static final class MoodTransitionState {
        private ReverieEntity.Mood previousMood;
        private ReverieEntity.Mood currentMood;
        private int transitionStartTick;

        private MoodTransitionState(ReverieEntity.Mood previousMood, ReverieEntity.Mood currentMood, int transitionStartTick) {
            this.previousMood = previousMood;
            this.currentMood = currentMood;
            this.transitionStartTick = transitionStartTick;
        }
    }

    private static final class ReverieCrossfadeLayer extends GeoRenderLayer<ReverieEntity> {
        private ReverieCrossfadeLayer(ReverieRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, ReverieEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            ReverieRenderer renderer = (ReverieRenderer) this.getRenderer();
            ReverieEntity.Mood previousMood = renderer.getPreviousMood(animatable);
            if (previousMood == null) {
                return;
            }

            float alpha = renderer.getPreviousMoodAlpha(animatable, partialTick);
            if (alpha <= 0.0F) {
                return;
            }

            ResourceLocation previousTexture = ReverieModel.textureForMood(previousMood);
            RenderType previousRenderType = RenderType.entityTranslucent(previousTexture);
            VertexConsumer previousBuffer = bufferSource.getBuffer(previousRenderType);
            int color = ((Math.max(0, Math.min(255, Math.round(alpha * 255.0F))) & 0xFF) << 24) | 0x00FFFFFF;
            this.getRenderer().reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    previousRenderType,
                    previousBuffer,
                    partialTick,
                    packedLight,
                    packedOverlay,
                    color
            );
        }
    }
}
