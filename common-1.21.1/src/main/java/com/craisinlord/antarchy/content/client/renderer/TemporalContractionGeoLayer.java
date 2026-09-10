package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.ContractionAfterimages;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Deque;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class TemporalContractionGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private static final int RGB = 0xFFD35A;
    private static final float BASE_ALPHA = 0.42F;
    private static final int SAMPLE_STRIDE = 2;

    public TemporalContractionGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                       MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (!(animatable instanceof Entity entity) || !(entity instanceof LivingEntity living)
                || living.isInvisible() || RoyalEffectHooks.contractedHolder() == null
                || !living.hasEffect(RoyalEffectHooks.contractedHolder())) {
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

        Matrix4f inverse = new Matrix4f(poseStack.last().pose()).invert();
        RenderType afterimageType = RenderType.entityTranslucent(this.getRenderer().getTextureLocation(animatable));
        VertexConsumer afterimageBuffer = bufferSource.getBuffer(afterimageType);

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

            poseStack.pushPose();
            poseStack.translate(local.x, local.y, local.z);
            this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, afterimageType,
                    afterimageBuffer, partialTick, 0xF000F0, packedOverlay, color);
            poseStack.popPose();
        }
    }
}
