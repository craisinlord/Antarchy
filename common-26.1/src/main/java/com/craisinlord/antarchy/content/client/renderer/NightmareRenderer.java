package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.NightmareModel;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class NightmareRenderer extends GeoEntityRenderer<NightmareEntity, LivingEntityRenderState> {
    private static final DataTicket<Float> BODY_PITCH = DataTicket.create("antarchy_nightmare_body_pitch", Float.class);
    private static final DataTicket<Float> BODY_HEIGHT = DataTicket.create("antarchy_nightmare_body_height", Float.class);
    private static final DataTicket<Boolean> HEAD_TRACKING = DataTicket.create("antarchy_nightmare_head_tracking", Boolean.class);

    public NightmareRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareModel());
        this.shadowRadius = 1.8F;
        this.withScale(1.15F);
        this.withRenderLayer(new NightmareEmissiveLayer(this));
    }

    @Override
    public void addRenderData(NightmareEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(BODY_PITCH, animatable.getBodyPitch(partialTick));
        renderState.addGeckolibData(BODY_HEIGHT, animatable.getBbHeight());
        renderState.addGeckolibData(HEAD_TRACKING, animatable.getActiveSpecialAnimation() == 0 && !animatable.isDeadOrDying());
    }

    @Override
    protected void applyRotations(RenderPassInfo<LivingEntityRenderState> renderPassInfo, PoseStack poseStack, float nativeScale) {
        super.applyRotations(renderPassInfo, poseStack, nativeScale);
        float bodyPitch = renderPassInfo.getOrDefaultGeckolibData(BODY_PITCH, 0.0F);
        if (bodyPitch != 0.0F) {
            float pivotY = renderPassInfo.getOrDefaultGeckolibData(BODY_HEIGHT, 0.0F) * 0.5F;
            poseStack.translate(0.0F, pivotY, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-bodyPitch));
            poseStack.translate(0.0F, -pivotY, 0.0F);
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        if (renderPassInfo.getOrDefaultGeckolibData(HEAD_TRACKING, false)) {
            NightmareModel.applyHeadTracking(renderPassInfo.renderState(), snapshots);
        }
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }

    private static final class NightmareEmissiveLayer extends TextureLayerGeoLayer<NightmareEntity, Void, LivingEntityRenderState> {
        private NightmareEmissiveLayer(GeoEntityRenderer<NightmareEntity, LivingEntityRenderState> renderer) {
            super(renderer, NightmareModel.defaultEmissiveTexture(), RenderTypes::eyes);
        }

        @Override
        protected Identifier getTextureResource(LivingEntityRenderState renderState) {
            return NightmareModel.emissiveTextureFor(renderState);
        }
    }
}
