package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.EmperorScorpionModel;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class EmperorScorpionRenderer extends GeoEntityRenderer<EmperorScorpionEntity, LivingEntityRenderState> {
    public static final DataTicket<Float> HARDEN_ALPHA = DataTicket.create("antarchy_emperor_scorpion_harden_alpha", Float.class);
    private static final int CROSSFADE_TICKS = 12;
    private final Map<Integer, HardenTransitionState> transitions = new HashMap<>();

    public EmperorScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new EmperorScorpionModel());
        this.shadowRadius = 3.0F;
        this.withRenderLayer(new EmperorScorpionHardenLayer(this));
        this.withRenderLayer(new EmperorScorpionEmissiveLayer(this));
    }

    @Override
    public void addRenderData(EmperorScorpionEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        this.updateTransition(animatable);
        renderState.addGeckolibData(HARDEN_ALPHA, this.getHardenAlpha(animatable, partialTick));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }

    private static void submitModelCopy(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks,
                                        RenderType renderType, int light, int overlay, int colour) {
        PoseStack poseStack = renderPassInfo.poseStack();
        renderTasks.order(1).submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            poseStack.pushPose();
            poseStack.last().set(pose);
            renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, light, overlay, colour));
            poseStack.popPose();
        });
    }

    private static final class EmperorScorpionEmissiveLayer extends GeoRenderLayer<EmperorScorpionEntity, Void, LivingEntityRenderState> {
        private EmperorScorpionEmissiveLayer(GeoRenderer<EmperorScorpionEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }

            submitModelCopy(renderPassInfo, renderTasks, RenderTypes.eyes(EmperorScorpionModel.EMISSIVE_TEXTURE),
                    0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
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

    private static final class EmperorScorpionHardenLayer extends GeoRenderLayer<EmperorScorpionEntity, Void, LivingEntityRenderState> {
        private EmperorScorpionHardenLayer(GeoRenderer<EmperorScorpionEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }

            float alpha = renderPassInfo.getOrDefaultGeckolibData(HARDEN_ALPHA, 0.0F);
            if (alpha <= 0.0F) {
                return;
            }

            int color = ((Math.max(0, Math.min(255, Math.round(alpha * 255.0F))) & 0xFF) << 24) | 0x00FFFFFF;
            submitModelCopy(renderPassInfo, renderTasks, RenderTypes.entityTranslucent(EmperorScorpionModel.HARDEN_TEXTURE),
                    renderPassInfo.packedLight(), renderPassInfo.packedOverlay(), color);
        }
    }
}
