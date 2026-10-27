package com.craisinlord.antarchy.content.client.renderer.glimmer;

import com.craisinlord.antarchy.content.client.model.glimmer.GlimmerFrogModel;
import com.craisinlord.antarchy.content.client.model.glimmer.GlimmerModel;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

public class GlimmerRenderer extends GeoEntityRenderer<GlimmerEntity, LivingEntityRenderState> {
    private static final DataTicket<GlimmerFrogModel.State> FROG_STATE = DataTicket.create("antarchy_glimmer_frog_state", GlimmerFrogModel.State.class);
    private static final DataTicket<Identifier> EMISSIVE_TEXTURE = DataTicket.create("antarchy_glimmer_emissive_texture", Identifier.class);
    private static final DataTicket<Integer> EMISSIVE_COLOR = DataTicket.create("antarchy_glimmer_emissive_color", Integer.class);
    private static final float SHADOW_RADIUS = 0.0F;
    private static final float COOLDOWN_DIM_MIN = 0.35F;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final GlimmerFrogModel frogModel;

    public GlimmerRenderer(EntityRendererProvider.Context context) {
        super(context, new GlimmerModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.withRenderLayer(new GlimmerEmissiveLayer(this));
        this.frogModel = new GlimmerFrogModel(context.bakeLayer(ModelLayers.FROG));
    }

    @Override
    public void addRenderData(GlimmerEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        int color = packColor(animatable.getFadeAlpha(partialTick), brightnessFor(animatable, partialTick));
        Identifier emissiveTexture = animatable.getVariant().getBehavior().emissiveTexture(animatable);
        renderState.addGeckolibData(EMISSIVE_TEXTURE, emissiveTexture);
        renderState.addGeckolibData(EMISSIVE_COLOR, color);
        if (animatable.getVariant() == GlimmerVariant.FROG) {
            renderState.addGeckolibData(FROG_STATE, extractFrogState(animatable, partialTick, color, emissiveTexture));
        }
    }

    private static GlimmerFrogModel.State extractFrogState(GlimmerEntity entity, float partialTick, int color, Identifier emissiveTexture) {
        GlimmerFrogModel.State frog = new GlimmerFrogModel.State();
        frog.growth = entity.getGrowthScale();
        frog.bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        frog.limbSwing = entity.walkAnimation.position(partialTick);
        frog.limbSwingAmount = entity.walkAnimation.speed(partialTick);
        frog.ageInTicks = entity.tickCount + partialTick;
        float rawHeadYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot) - frog.bodyYaw;
        float maxHeadYaw = entity.getMaxHeadYRot();
        frog.netHeadYaw = Mth.clamp(Mth.wrapDegrees(rawHeadYaw), -maxHeadYaw, maxHeadYaw);
        frog.headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        frog.color = color;
        frog.texture = entity.getVariant().getBehavior().texture(entity);
        frog.emissiveTexture = emissiveTexture;
        return frog;
    }

    @Override
    public void submit(LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector renderTasks, CameraRenderState cameraState) {
        GlimmerFrogModel.State frog = renderState.getGeckolibData(FROG_STATE);
        if (frog != null) {
            this.submitVanillaModel(frog, renderState, poseStack, renderTasks);
            return;
        }
        super.submit(renderState, poseStack, renderTasks, cameraState);
    }

    private void submitVanillaModel(GlimmerFrogModel.State frog, LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector renderTasks) {
        poseStack.pushPose();
        float growth = frog.growth;
        poseStack.scale(growth, growth, growth);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0F - frog.bodyYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        renderTasks.submitModel(this.frogModel, frog, poseStack, RenderTypes.entityCutout(frog.texture),
                renderState.lightCoords, OverlayTexture.NO_OVERLAY, frog.color, null, renderState.outlineColor);
        renderTasks.submitModel(this.frogModel, frog, poseStack, RenderTypes.eyes(frog.emissiveTexture),
                FULL_BRIGHT, OverlayTexture.NO_OVERLAY, frog.color, null, renderState.outlineColor);

        poseStack.popPose();
    }

    private static int packColor(float alpha, float brightness) {
        int alphaByte = Mth.clamp(Math.round(alpha * 255.0F), 0, 255);
        int colorByte = Mth.clamp(Math.round(brightness * 255.0F), 0, 255);
        return (alphaByte << 24) | (colorByte << 16) | (colorByte << 8) | colorByte;
    }

    private static float brightnessFor(GlimmerEntity entity, float partialTick) {
        float cooldownFraction = entity.getAbilityCooldownFraction(partialTick);
        return Mth.lerp(cooldownFraction, 1.0F, COOLDOWN_DIM_MIN);
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    public int getRenderColor(GlimmerEntity animatable, Void relatedObject, float partialTick) {
        float brightness = brightnessFor(animatable, partialTick);
        return ARGB.colorFromFloat(animatable.getFadeAlpha(partialTick), brightness, brightness, brightness);
    }

    private static final class GlimmerEmissiveLayer extends GeoRenderLayer<GlimmerEntity, Void, LivingEntityRenderState> {
        private GlimmerEmissiveLayer(GlimmerRenderer renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }

            Identifier texture = renderPassInfo.getGeckolibData(EMISSIVE_TEXTURE);
            if (texture == null) {
                return;
            }

            int color = renderPassInfo.getOrDefaultGeckolibData(EMISSIVE_COLOR, -1);
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, RenderTypes.eyes(texture), (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color));
                poseStack.popPose();
            });
        }
    }
}
