package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.ContractionAfterimages;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Deque;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class TemporalContractionAfterimageLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final int RGB = 0xFFD35A;
    private static final float BASE_ALPHA = 0.42F;
    private static final int SAMPLE_STRIDE = 2;

    public TemporalContractionAfterimageLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
                       float headPitch) {
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

        Matrix4f inverse = new Matrix4f(poseStack.last().pose()).invert();
        M model = this.getParentModel();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));

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

            Vector3f local = inverse.transformDirection(new Vector3f((float) dx, (float) dy, (float) dz));
            int alpha = (int) (BASE_ALPHA * fade * 255.0F) & 0xFF;
            int color = alpha << 24 | RGB;

            model.setupAnim(entity, sample.limbPos, sample.limbSpeed, sample.ageInTicks,
                    sample.headYaw - sample.bodyYaw, sample.xRot);
            poseStack.pushPose();
            poseStack.translate(local.x, local.y, local.z);
            model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                    LivingEntityRenderer.getOverlayCoords(entity, 0.0F), color);
            poseStack.popPose();
        }

        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
