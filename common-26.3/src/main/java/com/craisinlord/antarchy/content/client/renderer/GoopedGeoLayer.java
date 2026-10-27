package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.effect.GoopedEffectHooks;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class GoopedGeoLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T, O, R> {
    public static final DataTicket<Boolean> GOOPED = DataTicket.create("antarchy_geo_gooped", Boolean.class);
    private static final int GOOPED_TINT = 0x6632FF55;

    public GoopedGeoLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    public void addRenderData(T animatable, @Nullable O relatedObject, R renderState, float partialTick) {
        Holder<MobEffect> gooped = GoopedEffectHooks.holder();
        renderState.addGeckolibData(GOOPED, gooped != null
                && animatable instanceof LivingEntity livingEntity
                && livingEntity.hasEffect(gooped));
    }

    @Override
    public void submitRenderTask(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        if (!renderPassInfo.willRender() || !renderPassInfo.getOrDefaultGeckolibData(GOOPED, false)) {
            return;
        }

        PoseStack poseStack = renderPassInfo.poseStack();
        int packedLight = renderPassInfo.packedLight();
        int packedOverlay = renderPassInfo.packedOverlay();
        RenderType goopedRenderType = RenderTypes.entityTranslucent(this.getRenderer().getTextureLocation(renderPassInfo.renderState()));
        poseStack.pushPose();
        poseStack.scale(1.018F, 1.018F, 1.018F);
        renderTasks.order(1).submitCustomGeometry(poseStack, goopedRenderType, (pose, buffer) -> {
            PoseStack renderPose = renderPassInfo.poseStack();
            renderPose.pushPose();
            renderPose.last().set(pose);
            renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, packedLight, packedOverlay, GOOPED_TINT));
            renderPose.popPose();
        });
        poseStack.popPose();
    }
}
