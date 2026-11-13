package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.MissileSquidModel;
import com.craisinlord.antarchy.content.client.renderer.GlimmerEchoRendering;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class MissileSquidRenderer extends GeoEntityRenderer<MissileSquidEntity, LivingEntityRenderState> {
    public MissileSquidRenderer(EntityRendererProvider.Context context) {
        super(context, new MissileSquidModel());
        this.shadowRadius = 1.3F;
        this.withRenderLayer(new MissileSquidEmissiveLayer(this));
    }

    @Override
    public int getRenderColor(MissileSquidEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(), super.getRenderColor(animatable, relatedObject, partialTick));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState state, Identifier texture) {
        return state.getOrDefaultGeckolibData(MissileSquidModel.GLIMMER_ECHO, false)
                ? RenderTypes.entityTranslucent(texture) : super.getRenderType(state, texture);
    }

    @Override
    public Vec3 getRenderOffset(LivingEntityRenderState renderState) {
        return new Vec3(0.0D, -2.0D, 0.0D);
    }

    private static final class MissileSquidEmissiveLayer extends GeoRenderLayer<MissileSquidEntity, Void, LivingEntityRenderState> {
        private MissileSquidEmissiveLayer(GeoRenderer<MissileSquidEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> info, SubmitNodeCollector tasks) {
            if (!info.willRender()) {
                return;
            }
            boolean glimmer = info.getOrDefaultGeckolibData(MissileSquidModel.GLIMMER_ECHO, false);
            RenderType type = RenderTypes.eyes(glimmer ? MissileSquidModel.GLIMMER_EMISSIVE_TEXTURE : MissileSquidModel.EMISSIVE_TEXTURE);
            PoseStack stack = info.poseStack();
            tasks.order(1).submitCustomGeometry(stack, type, (pose, buffer) -> {
                stack.pushPose();
                stack.last().set(pose);
                info.renderPosed(() -> info.model().render(info, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY,
                        glimmer ? GlimmerEchoRendering.emissiveColor() : 0xFFFFFFFF));
                stack.popPose();
            });
        }
    }
}
