package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.model.BomberModel;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import com.geckolib.constant.dataticket.DataTicket;
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

public class BomberRenderer extends GeoEntityRenderer<BomberEntity, LivingEntityRenderState> {
    public static final DataTicket<Boolean> FLASHING = DataTicket.create("antarchy_bomber_flashing", Boolean.class);
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/bomber.png");

    public BomberRenderer(EntityRendererProvider.Context context) {
        super(context, new BomberModel());
        this.shadowRadius = 0.12F;
        this.withRenderLayer(new BomberFlashLayer(this));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    public static final class BomberFlashLayer extends GeoRenderLayer<BomberEntity, Void, LivingEntityRenderState> {
        private static final RenderType FLASH_TYPE = RenderTypes.eyes(TEXTURE);

        private BomberFlashLayer(GeoRenderer<BomberEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void addRenderData(BomberEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            renderState.addGeckolibData(FLASHING, animatable.isFlashing());
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender() || !renderPassInfo.getOrDefaultGeckolibData(FLASHING, false)) {
                return;
            }
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, FLASH_TYPE, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
                poseStack.popPose();
            });
        }
    }
}
