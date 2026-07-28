package com.craisinlord.antarchy.content.client.renderer.glimmer;

import com.craisinlord.antarchy.content.client.model.glimmer.GlimmerFrogModel;
import com.craisinlord.antarchy.content.client.model.glimmer.GlimmerModel;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GlimmerRenderer extends GeoEntityRenderer<GlimmerEntity> {
    private static final float SHADOW_RADIUS = 0.0F;
    private static final float COOLDOWN_DIM_MIN = 0.35F;

    private final GlimmerFrogModel frogModel;

    public GlimmerRenderer(EntityRendererProvider.Context context) {
        super(context, new GlimmerModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.addRenderLayer(new GlimmerEmissiveLayer(this));
        this.frogModel = new GlimmerFrogModel(context.bakeLayer(ModelLayers.FROG));
    }

    @Override
    public void render(GlimmerEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.getVariant() == GlimmerVariant.FROG) {
            this.renderVanillaModel(this.frogModel, entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderVanillaModel(EntityModel<GlimmerEntity> model, GlimmerEntity entity, float entityYaw, float partialTick,
                                     PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        float growth = entity.getGrowthScale();
        poseStack.scale(growth, growth, growth);

        // Compute body yaw directly from the entity's own tracked fields rather than trusting
        // the ambient entityYaw parameter, so the body pose and the head-offset math below stay
        // self-consistent.
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        float limbSwing = entity.walkAnimation.position(partialTick);
        float limbSwingAmount = entity.walkAnimation.speed(partialTick);
        float ageInTicks = entity.tickCount + partialTick;
        float rawHeadYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot) - bodyYaw;
        float maxHeadYaw = entity.getMaxHeadYRot();
        float netHeadYaw = Mth.clamp(Mth.wrapDegrees(rawHeadYaw), -maxHeadYaw, maxHeadYaw);
        float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        int color = packColor(entity.getFadeAlpha(partialTick), brightnessFor(entity, partialTick));

        ResourceLocation texture = entity.getVariant().getBehavior().texture(entity);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, color);

        ResourceLocation emissiveTexture = entity.getVariant().getBehavior().emissiveTexture(entity);
        VertexConsumer emissiveBuffer = bufferSource.getBuffer(RenderType.eyes(emissiveTexture));
        model.renderToBuffer(poseStack, emissiveBuffer, 0xF000F0, OverlayTexture.NO_OVERLAY, color);

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
    public RenderType getRenderType(GlimmerEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public software.bernie.geckolib.util.Color getRenderColor(GlimmerEntity animatable, float partialTick, int packedLight) {
        float brightness = brightnessFor(animatable, partialTick);
        return software.bernie.geckolib.util.Color.ofRGBA(brightness, brightness, brightness, animatable.getFadeAlpha(partialTick));
    }

    @Override
    public void preRender(PoseStack poseStack, GlimmerEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        this.shadowRadius = SHADOW_RADIUS;
    }

    private static final class GlimmerEmissiveLayer extends GeoRenderLayer<GlimmerEntity> {
        private GlimmerEmissiveLayer(GeoEntityRenderer<GlimmerEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, GlimmerEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            ResourceLocation texture = animatable.getVariant().getBehavior().emissiveTexture(animatable);
            RenderType emissiveType = RenderType.eyes(texture);
            VertexConsumer emissiveBuffer = bufferSource.getBuffer(emissiveType);
            this.getRenderer().reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    emissiveType,
                    emissiveBuffer,
                    partialTick,
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    packColor(animatable.getFadeAlpha(partialTick), brightnessFor(animatable, partialTick))
            );
        }
    }
}
