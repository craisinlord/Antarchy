package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.anthology.time.client.ContractionAfterimages;
import com.geckolib.constant.dataticket.DataTicket;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TemporalContractionAfterimageLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    public static final DataTicket<List<Afterimage>> AFTERIMAGES =
            DataTicket.create("antarchy_living_temporal_afterimages", new TypeToken<List<Afterimage>>() {});
    private static final float BASE_ALPHA = 0.42F;
    private static final int SAMPLE_STRIDE = 2;
    private final LivingEntityRenderer<?, S, M> livingRenderer;

    @SuppressWarnings("unchecked")
    public TemporalContractionAfterimageLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
        this.livingRenderer = (LivingEntityRenderer<?, S, M>) renderer;
    }

    public static void extract(LivingEntity entity, LivingEntityRenderState state, float partialTick) {
        state.getDataMap().remove(AFTERIMAGES);
        if (entity.isSpectator() || !ContractionAfterimages.isActive(entity)) {
            return;
        }
        Deque<ContractionAfterimages.Sample> samples = ContractionAfterimages.samples(entity);
        if (samples == null || samples.isEmpty()) {
            return;
        }

        double curX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double curY = Mth.lerp(partialTick, entity.yo, entity.getY());
        double curZ = Mth.lerp(partialTick, entity.zo, entity.getZ());
        int now = (int) (entity.level().getGameTime() & 0x7fffffffL);

        List<Afterimage> afterimages = new ArrayList<>();
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
            afterimages.add(new Afterimage((float) dx, (float) dy, (float) dz, alpha << 24 | ContractionAfterimages.colorFor(entity),
                    sample.limbPos, sample.limbSpeed, sample.ageInTicks, sample.bodyYaw, sample.headYaw - sample.bodyYaw, sample.xRot));
        }
        if (!afterimages.isEmpty()) {
            state.addGeckolibData(AFTERIMAGES, afterimages);
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
        List<Afterimage> afterimages = state.getGeckolibData(AFTERIMAGES);
        if (afterimages == null) {
            return;
        }

        Matrix4f inverse = new Matrix4f(poseStack.last().pose()).invert();
        M model = this.getParentModel();
        RenderType type = RenderTypes.entityTranslucent(this.livingRenderer.getTextureLocation(state));
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);

        for (Afterimage afterimage : afterimages) {
            Vector3f local = inverse.transformDirection(new Vector3f(afterimage.dx(), afterimage.dy(), afterimage.dz()));
            poseStack.pushPose();
            poseStack.translate(local.x, local.y, local.z);
            collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
                float oldWalkPos = state.walkAnimationPos;
                float oldWalkSpeed = state.walkAnimationSpeed;
                float oldAge = state.ageInTicks;
                float oldBodyRot = state.bodyRot;
                float oldYRot = state.yRot;
                float oldXRot = state.xRot;
                state.walkAnimationPos = afterimage.limbPos();
                state.walkAnimationSpeed = afterimage.limbSpeed();
                state.ageInTicks = afterimage.ageInTicks();
                state.bodyRot = afterimage.bodyYaw();
                state.yRot = afterimage.netHeadYaw();
                state.xRot = afterimage.xRot();
                model.setupAnim(state);
                PoseStack renderPose = new PoseStack();
                renderPose.last().set(pose);
                model.renderToBuffer(renderPose, buffer, LightCoordsUtil.FULL_BRIGHT, overlay, afterimage.color());
                state.walkAnimationPos = oldWalkPos;
                state.walkAnimationSpeed = oldWalkSpeed;
                state.ageInTicks = oldAge;
                state.bodyRot = oldBodyRot;
                state.yRot = oldYRot;
                state.xRot = oldXRot;
                model.setupAnim(state);
            });
            poseStack.popPose();
        }
    }

    public record Afterimage(float dx, float dy, float dz, int color, float limbPos, float limbSpeed, float ageInTicks,
                             float bodyYaw, float netHeadYaw, float xRot) {
    }
}
