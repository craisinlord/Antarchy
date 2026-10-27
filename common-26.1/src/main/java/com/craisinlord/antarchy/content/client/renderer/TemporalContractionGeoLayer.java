package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.anthology.time.client.ContractionAfterimages;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

public final class TemporalContractionGeoLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T, O, R> {
    public static final DataTicket<List<Afterimage>> AFTERIMAGES =
            DataTicket.create("antarchy_geo_temporal_afterimages", new TypeToken<List<Afterimage>>() {});
    private static final float BASE_ALPHA = 0.42F;
    private static final int SAMPLE_STRIDE = 2;

    public TemporalContractionGeoLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    public void addRenderData(T animatable, @Nullable O relatedObject, R renderState, float partialTick) {
        List<Afterimage> afterimages = new ArrayList<>();
        renderState.addGeckolibData(AFTERIMAGES, afterimages);
        if (!(animatable instanceof Entity entity) || !(entity instanceof LivingEntity living)
                || !ContractionAfterimages.isActive(living)) {
            return;
        }
        Deque<ContractionAfterimages.Sample> samples = ContractionAfterimages.samples(living);
        if (samples == null || samples.isEmpty()) {
            return;
        }

        double curX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double curY = Mth.lerp(partialTick, entity.yo, entity.getY());
        double curZ = Mth.lerp(partialTick, entity.zo, entity.getZ());
        int now = (int) (entity.level().getGameTime() & 0x7fffffffL);

        int index = 0;
        for (ContractionAfterimages.Sample sample : samples) {
            if (index++ % SAMPLE_STRIDE != 0) {
                continue;
            }
            double dx = sample.x - curX;
            double dy = sample.y - curY;
            double dz = sample.z - curZ;
            if (dx * dx + dy * dy + dz * dz < 0.0025D) {
                continue;
            }
            float fade = ContractionAfterimages.fade(sample, now);
            if (fade <= 0.02F) {
                continue;
            }

            int alpha = (int) (BASE_ALPHA * fade * 255.0F) & 0xFF;
            int color = alpha << 24 | ContractionAfterimages.colorFor(entity);
            afterimages.add(new Afterimage(dx, dy, dz, color));
        }
    }

    @Override
    public void submitRenderTask(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        List<Afterimage> afterimages = renderPassInfo.getOrDefaultGeckolibData(AFTERIMAGES, List.of());
        if (!renderPassInfo.willRender() || afterimages.isEmpty()) {
            return;
        }

        PoseStack poseStack = renderPassInfo.poseStack();
        int packedOverlay = renderPassInfo.packedOverlay();
        Matrix4f inverse = new Matrix4f(poseStack.last().pose()).invert();
        RenderType afterimageType = RenderTypes.entityTranslucent(this.getRenderer().getTextureLocation(renderPassInfo.renderState()));

        for (Afterimage afterimage : afterimages) {
            Vector3f local = inverse.transformDirection(new Vector3f((float) afterimage.dx(), (float) afterimage.dy(), (float) afterimage.dz()));
            int color = afterimage.color();
            poseStack.pushPose();
            poseStack.translate(local.x, local.y, local.z);
            renderTasks.order(1).submitCustomGeometry(poseStack, afterimageType, (pose, buffer) -> {
                PoseStack renderPose = renderPassInfo.poseStack();
                renderPose.pushPose();
                renderPose.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, LightCoordsUtil.FULL_BRIGHT, packedOverlay, color));
                renderPose.popPose();
            });
            poseStack.popPose();
        }
    }

    public record Afterimage(double dx, double dy, double dz, int color) {
    }
}
