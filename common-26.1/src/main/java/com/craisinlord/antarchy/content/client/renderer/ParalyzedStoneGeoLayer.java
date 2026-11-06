package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.AntarchyObjects;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class ParalyzedStoneGeoLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T, O, R> {
    public static final DataTicket<Boolean> PARALYZED = DataTicket.create("antarchy_geo_paralyzed", Boolean.class);
    private static final Identifier STONE_TEXTURE = Identifier.withDefaultNamespace("textures/block/stone.png");
    private static final RenderType STONE_RENDER_TYPE = RenderTypes.entityCutout(STONE_TEXTURE);
    private static final int STONE_TINT = 0xFFB1ADA4;

    public ParalyzedStoneGeoLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    public void addRenderData(T animatable, @Nullable O relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(PARALYZED, animatable instanceof LivingEntity livingEntity
                && livingEntity.hasEffect(AntarchyObjects.PARALYZED_EFFECT.get()));
    }

    @Override
    public void submitRenderTask(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        if (!renderPassInfo.willRender() || !renderPassInfo.getOrDefaultGeckolibData(PARALYZED, false)) {
            return;
        }

        PoseStack poseStack = renderPassInfo.poseStack();
        int packedLight = renderPassInfo.packedLight();
        int packedOverlay = renderPassInfo.packedOverlay();
        poseStack.pushPose();
        poseStack.scale(1.02f, 1.02f, 1.02f);
        renderTasks.order(1).submitCustomGeometry(poseStack, STONE_RENDER_TYPE, (pose, buffer) -> {
            PoseStack renderPose = renderPassInfo.poseStack();
            renderPose.pushPose();
            renderPose.last().set(pose);
            renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, packedLight, packedOverlay, STONE_TINT));
            renderPose.popPose();
        });
        poseStack.popPose();
    }
}
