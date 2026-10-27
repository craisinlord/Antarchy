package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ReverieModel;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

public class ReverieRenderer extends GeoEntityRenderer<ReverieEntity, LivingEntityRenderState> {
    private static final DataTicket<ReverieEntity.Mood> PREVIOUS_MOOD = DataTicket.create("antarchy_reverie_previous_mood", ReverieEntity.Mood.class);
    private static final DataTicket<Float> PREVIOUS_MOOD_ALPHA = DataTicket.create("antarchy_reverie_previous_mood_alpha", Float.class);
    private static final float VISUAL_SCALE = 0.7F;
    private static final float SHADOW_RADIUS = 0.12F;
    private static final int CROSSFADE_TICKS = 12;
    private final Map<Integer, MoodTransitionState> transitions = new HashMap<>();

    public ReverieRenderer(EntityRendererProvider.Context context) {
        super(context, new ReverieModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.withScale(VISUAL_SCALE);
        this.withRenderLayer(new ReverieCrossfadeLayer(this));
    }

    @Override
    public void addRenderData(ReverieEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        this.updateTransition(animatable);
        ReverieEntity.Mood previousMood = this.getPreviousMood(animatable);
        if (previousMood != null) {
            renderState.addGeckolibData(PREVIOUS_MOOD, previousMood);
            renderState.addGeckolibData(PREVIOUS_MOOD_ALPHA, this.getPreviousMoodAlpha(animatable, partialTick));
        }
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
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

    private static final class ReverieCrossfadeLayer extends GeoRenderLayer<ReverieEntity, Void, LivingEntityRenderState> {
        private ReverieCrossfadeLayer(ReverieRenderer renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }

            ReverieEntity.Mood previousMood = renderPassInfo.getGeckolibData(PREVIOUS_MOOD);
            if (previousMood == null) {
                return;
            }

            float alpha = renderPassInfo.getOrDefaultGeckolibData(PREVIOUS_MOOD_ALPHA, 0.0F);
            if (alpha <= 0.0F) {
                return;
            }

            Identifier previousTexture = ReverieModel.textureForMood(previousMood);
            RenderType previousRenderType = RenderTypes.entityTranslucent(previousTexture);
            int color = ((Math.max(0, Math.min(255, Math.round(alpha * 255.0F))) & 0xFF) << 24) | 0x00FFFFFF;
            int light = renderPassInfo.packedLight();
            int overlay = renderPassInfo.packedOverlay();
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, previousRenderType, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, light, overlay, color));
                poseStack.popPose();
            });
        }
    }
}
